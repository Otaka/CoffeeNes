package com.smalljnes;

/**
 * @author Dmitry
 */
public class Apu {
    byte mock=0;

    public void setMock(byte mock) {
        this.mock = mock;
    }
    
    void write(int elapsed,int address, byte value) {

    }
    
    byte read(int elapsed,int address) {
        return mock;
    }
    
    void reset(){
        
    }
}
