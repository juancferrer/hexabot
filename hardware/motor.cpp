class Motor{
    volatile uint8_t *timerARegister, *timerBRegister;
    volatile uint8_t *dataARegister, *dataBRegister;
    int direction, speed, current;
    public:
        Motor(volatile uint8_t *timerAReg, volatile uint8_t *timerBReg, 
                volatile uint8_t *dataAReg, volatile uint8_t *dataBReg);
        void set(int direction, int speed);
        int getSpeed();
        int getCurrent();
};

Motor::Motor(volatile uint8_t *timerAReg, volatile uint8_t *timerBReg, 
        volatile uint8_t *dataAReg, volatile uint8_t *dataBReg){
    //Store the pointers for later use
    timerARegister = timerAReg;
    timerBRegister = timerBReg;
    dataARegister = dataAReg;
    dataBRegister = dataBReg;

    *timerARegister |= _BV(WGM00); //Enable phase correct PWM
    //Set prescaler to 1024 == 16mhz/1024 = 15.624khz
    *timerBRegister |= _BV(CS02)| _BV(CS00); 

    //Set both channels to 0
    *dataARegister = 0;
    *dataBRegister = 0;
}

void Motor::set(int direction, int speed){
    if(direction == 1){ //Reverse
        *timerARegister |= _BV(COM2A1); //Set PWM on Channel A(OC2B)
        *timerARegister &= ~_BV(COM2B1); //Disable PWM Channel B(OC2A)
        *dataARegister = speed;
    }
    else if (direction == 0){ //Forward
        *timerARegister |= _BV(COM2B1); //Set PWM on Channel A(OC2A)
        *timerARegister &= ~_BV(COM2A1); //Disable PWM Channel B(OC2B)
        *dataBRegister = speed;
    }
    this->direction = direction;
    this->speed = speed;
}

int Motor::getSpeed(){
    return this->speed;
}

int Motor::getCurrent(){
    return this->current;
}
