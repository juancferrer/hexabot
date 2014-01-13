//Borrowed from here: http://www.nongnu.org/avr-libc/user-manual/group__avr__stdio.html#gaf41f158c022cbb6203ccd87d27301226
//
#include <stdio.h>
#include <util/setbaud.h>

#undef FDEV_SETUP_STREAM  // Needed to make it work with cpp
//http://www.avrfreaks.net/index.php?name=PNphpBB2&file=viewtopic&p=308100
#define FDEV_SETUP_STREAM(p, g, f) { 0, 0, f, 0, 0, p, g, 0 }
void initUart0(void);
static int uart0Putchar(char c, FILE *stream);
static FILE uart0Out = FDEV_SETUP_STREAM(uart0Putchar, NULL, _FDEV_SETUP_WRITE);

void initUart0(void){
    //Magic macros: http://www.nongnu.org/avr-libc/user-manual/group__util__setbaud.html
    UBRR0H = UBRRH_VALUE; 
    UBRR0L = UBRRL_VALUE; 

    #if USE_2X
    UCSR0A |= (1 << U2X0);
    #else 
    UCSR0A &= ~(1 << U2X0);
    #endif 

    // 8 bits, no parity, no bullshit, defaults work
    //UCSR0C  |= (1 << UCSZ01) | (1 << UCSZ00);
    
    //TX/RX enabled, RX interrupt enable
    UCSR0B |= (1 << TXEN0) | (1 <<RXEN0) | (1 << RXCIE0);
}

static int uart0Putchar(char c, FILE *stream){
    if (c == '\n')
        uart0Putchar('\r', stream);
    loop_until_bit_is_set(UCSR0A, UDRE0);
    UDR0 = c;
    return 0;
}
