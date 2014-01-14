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
    }
    return 0;   /* never reached */
}
