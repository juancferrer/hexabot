/* Name: main.c
 * Author: Juan Carlos Ferrer
 * Copyright: 2012
 * License: BSD
 */

#include <string.h>
#include <avr/io.h>
#include <avr/wdt.h>
#include <avr/interrupt.h>
#include <util/delay.h>

#include "uart.c" 
#include "motor.cpp"

// 5 char command buffer  
// "LEFT_MOTOR_DIRECTION|LEFT_MOTOR_POWER|RIGHT_MOTOR_DIRECTION|RIGHT_MOTOR_POWER|\n"
#define IN_BUFFER_SIZE 5 
#define flip(x) (((x << 4) & 0xF0) | (x >> 4))
#define bit_at(x,i) !!((x) & (1 << (i)))

typedef struct{
    volatile char buffer[IN_BUFFER_SIZE];
    volatile unsigned char bytesReceived;
    volatile unsigned char cmdAvailable;
    enum CMD_SECTIONS{
        LEFT_MOTOR_DIR=0, LEFT_MOTOR_POW, RIGHT_MOTOR_DIR, RIGHT_MOTOR_POW,
    };
}UART0;
volatile UART0 uart0; //Global instance of our uart0 object


typedef struct{
    volatile unsigned char noCmdCounter; //Increment when timer0 overflows
    volatile unsigned char emergency;
}TIMER1;
volatile TIMER1 timer;


//Setup the motors, consuming timer0 and timer 2
Motor leftMotor = Motor(&TCCR0A, &TCCR0B, &OCR0A, &OCR0B);
Motor rightMotor = Motor(&TCCR2A, &TCCR2B, &OCR2A, &OCR2B);


//Handle receiving data on UART0
ISR(USART_RX_vect){
    uart0.buffer[uart0.bytesReceived++] = UDR0; // Read the buffer
    if(uart0.bytesReceived == IN_BUFFER_SIZE){
        //We've received a whole command
        uart0.bytesReceived = 0; //Reset
        uart0.cmdAvailable = 1; //Enable the flag, handle the command in main loop
    }
}

//Handle timer1 overflow for bookeeping
ISR(TIMER1_OVF_vect){
    timer.noCmdCounter += 1;
    if(timer.noCmdCounter > 5){
        //Haven't received a cmd in a long time, emergency shutdown
        timer.emergency = 1;
        timer.noCmdCounter = 0;
    }
}

//Handle receiving RFID data on PORTK
//ISR(PCINT2_vect){
    /* Weigand protocol
     * D1(1) D0(1) - 0x03 == No change
     * D1(0) D0(1) - 0x01 == logical 1
     * D1(1) D0(0) - 0x02 == logical 0
     */
/*
    unsigned char data = PINK; //Read the state of the pins when entering ISR
    //Now figure out which ones changed, and update values if needed
    if(data == 0xFF) return; //Pins changed back to non transmitting state, return
    //If we're here, then we received data
    //Go by 2s, see if it's a 1 or 0
    for(unsigned char i=0; i<8; i+=2){
        unsigned char temp  = (data >> i) & MASK; //Pull out the 2 bits we want
        if(temp == 0x01){
            //Received a '1', set the corresponding bit to one
            if(controller.readersBitCount[i/2] < 8)
                controller.readers[i/2][0] |= (1 << controller.readersBitCount[i/2]);
            if(controller.readersBitCount[i/2] >= 8 && controller.readersBitCount[i/2] < 16)
                controller.readers[i/2][1] |= (1 << controller.readersBitCount[i/2] - 8);
            if(controller.readersBitCount[i/2] >=16 && controller.readersBitCount[i/2] < 24)
                controller.readers[i/2][2] |= (1 << controller.readersBitCount[i/2] - 16);
            if(controller.readersBitCount[i/2] >=24 && controller.readersBitCount[i/2] < 32)
                controller.readers[i/2][3] |= (1 << controller.readersBitCount[i/2] - 24);
            if(controller.readersBitCount[i/2] >=32 && controller.readersBitCount[i/2] < 40)
                controller.readers[i/2][4] |= (1 << controller.readersBitCount[i/2] - 32);
            controller.readersBitCount[i/2]++;
        }
        else if(temp == 0x02){
            //Received a '0', just increment the bit count
            controller.readersBitCount[i/2]++;
        }
    }
}
*/

/*
void init_portk(void){
    //PORTK arduino pins ANALOG8-ANALOG15, ALL inputs
    //These are the PCINT16-PCINT23 pins
    DDRK = 0x00; 
    PORTK = 0xFF; //Enable internal pullups

    PCICR |= (1 << PCIE2); // Turn on pin change interrupt PCIE2 for pins PCINT16-PCINT23
    PCMSK2 = 0xFF; //Now enable ALL PCINT16-PCINT23 pins to trigger PCIE2 interrupt
    
    sei(); //Enable global interrupt
}

void init_portc(void){
    //PORTC arduino pins DIGITAL30-DIGITAL37, ALL outputs
    //These pins "open" the doors
    DDRC = 0xFF;
    PORTC = 0x00; //All low for now

    //DIGITAL30 == READER1
    //DIGITAL31 == READER2
    //...see DOORS enum
}
*/

void init_timers(void){
    // Timer0 for Channel C(PD5), D(PD6)
    // set PWM for 50% duty cycle
    //OCR0A = 128;
    // set to non inverting mode
    //TCCR0A |= (1 << COM0A1);
    // set to phase-correct PWM Mode
    TCCR0A |= _BV(WGM00);
    // set prescaler to 1024 and starts PWM
    TCCR0B |= _BV(CS02)| _BV(CS00);

    //Timer2 for Channel A(PD3), B(PB3)
    // set PWM for 50% duty cycle
    //OCR2A = 128;
    // set to non inverting mode
    //TCCR2A |= (1 << COM2A1);
    // set to phase-correct (8-bit) PWM Mode
    TCCR2A |= _BV(WGM20);
    // set prescaler to 1024 and starts PWM
    TCCR2B |= _BV(CS22)| _BV(CS20);
}

void initPortD(void){
    //PORTD set to PWM for channel A(PD3),C(PD5),D(PD6)
    DDRD |= _BV(DDD3) | _BV(DDD5) | _BV(DDD6); 
}

void initPortB(void){
    //PORTB set to PWM for channel B(PB3)
    DDRB |= _BV(DDB3);
}

void initSystemTimer(void){
    //Use timer1 as system bookeeping
    TIMSK1 |= _BV(TOIE1); //Enable timer1 overflow interrupt
    // set prescaler to 64 and starts the counter
    TCCR1B |= _BV(CS11) | _BV(CS10);
}

int main(void)
{
    initPortB();
    initPortD();
    sei(); //Enable global interrupt
    initUart0();
    initSystemTimer();
    stdout = &uart0Out;
    printf("Begin\n");

    /*
    //Clear out all the controller data
    memset(controller.readers, 0, sizeof(controller.readers));
    memset(controller.readersBitCount, 0, sizeof(controller.readersBitCount));
    */
    for(;;){
        if(timer.emergency){ 
            timer.emergency = 0; //Reset emergency
            if(leftMotor.getSpeed() || rightMotor.getSpeed()){
                // Shutdown motors if they're running
                leftMotor.set(0, 0);
                rightMotor.set(0, 0);
                }
        }

        if(uart0.cmdAvailable){
            /*
            printf("LDir: %s, LPow: %d, RDir: %s, RPow:%d\n", 
                    (char *)uart0.buffer[uart0.LEFT_MOTOR_DIR], 
                    uart0.buffer[uart0.LEFT_MOTOR_POW], 
                    (char *)uart0.buffer[uart0.RIGHT_MOTOR_DIR],
                    uart0.buffer[uart0.RIGHT_MOTOR_POW]);
            */
            leftMotor.set(uart0.buffer[uart0.LEFT_MOTOR_DIR], 
                    uart0.buffer[uart0.LEFT_MOTOR_POW]);
            rightMotor.set(uart0.buffer[uart0.RIGHT_MOTOR_DIR], 
                    uart0.buffer[uart0.RIGHT_MOTOR_POW]);
            uart0.cmdAvailable=0; //Consume this message
            timer.noCmdCounter=0; //Reset the no command counter
        }
        /*
        for(int i=20; i < 255; i+=10){
            //Go Forward
            printf("Forward speed: %d\n", leftMotor.getSpeed());
            leftMotor.set(0, i);
            _delay_ms(500); 
        }

        for(int i=20; i < 255; i+=10){
            //Go Reverse
            leftMotor.set(1, i);
            printf("Reverse speed: %d\n", leftMotor.getSpeed());
            _delay_ms(500); 
        }

        leftMotor.set(0,0);

        for(int i=20; i < 255; i+=10){
            //Go Forward
            printf("Forward speed: %d\n", rightMotor.getSpeed());
            rightMotor.set(0, i);
            _delay_ms(500); 
        }

        for(int i=20; i < 255; i+=10){
            printf("Forward speed: %d\n", rightMotor.getSpeed());
            rightMotor.set(1, i);
            _delay_ms(500); 
        }

        rightMotor.set(0,0);
        */
    
        /*
        if(uart0.cmdAvailable){
            unsigned char door = uart0.buffer[READERID]; //Number of the RFID reader
            unsigned char cmd = uart0.buffer[COMMAND]; //Command: 0=DENIED 1=ALLOW, etc..
            uart0.cmdAvailable = 0; //Reset the flag
            //Do something with the command here
            if(cmd == ALLOW){
                //Set a pin high, open the door, whatevez
                //printf("Door %x YOU MAY PASS\n", door);
                switch(door){
                    case DOOR0:
                        PORTC |= (1 << DOOR0_PIN);
                        break;
                    case DOOR1:
                        PORTC |= (1 << DOOR1_PIN);
                        break;
                    case DOOR2:
                        PORTC |= (1 << DOOR2_PIN);
                        break;
                    case DOOR3:
                        PORTC |= (1 << DOOR3_PIN);
                        break;
                }
                _delay_ms(2000); //Don't delay in the real world
                PORTC = 0x00;
            }
            else if(cmd == DENIED){
                //Log and report to authorities
                //printf("Door %x DENIED\n", door);
                switch(door){
                    case DOOR0:
                        PORTC &= ~(1 << DOOR0_PIN);
                        break;
                    case DOOR1:
                        PORTC &= ~(1 << DOOR1_PIN);
                        break;
                    case DOOR2:
                        PORTC &= ~(1 << DOOR2_PIN);
                        break;
                    case DOOR3:
                        PORTC &= ~(1 << DOOR3_PIN);
                        break;
                }
            }
        }
        //Loop through all the readers
        for(char i=0; i<sizeof(controller.readers); i++){
            //If we've read the correct number of bits, print "<READER_ID>:<CARD_ID>"
            if(controller.readersBitCount[i] == 26 || controller.readersBitCount[i] == 35){
                printf("%d:", i);
                for(char j=0; j<sizeof(controller.readers[i]); j++){
                    for(char k=0; k<8; k++){
                        //Print the individual bits read as ascii '1' or '0'
                        printf("%d", bit_at(controller.readers[i][j], k));
                    }
                    //Get ready for the next card swipe
                }
                controller.readersBitCount[i] = 0x00;
                memset(controller.readers[i], 0, sizeof(controller.readers[i]));
            }
        }
        */
    }
    return 0;   /* never reached */
}
