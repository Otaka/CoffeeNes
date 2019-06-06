package com.smalljnes;

/**
 * @author Dmitry
 */
public class Addr {

    public int r;

    public int getCX() {
        return r & 0b011111;
    }

    public int getCY() {
        return (r >> 5) & 0b00011111;
    }

    public int getNT() {
        return (r >> 10) & 0b00000011;
    }

    public int getFY() {
        return (r >> 12) & 0b00000111;
    }

    public int getL() {
        return r & 0xFF;
    }

    public int getH() {
        return (r >> 8) & 0b01111111;
    }

    public int getAddr() {
        return r & 0x3FFF;
    }

    public void setR(int value) {
        r = value & 0x7FFF;
    }

    public void setAddr(int value) {
        r = value & 0x3FFF;
    }

    public void setL(int value) {
        r = (r & 0xFF00) | (value & 0xFF);
    }

    public void setH(int value) {
        r = (r & 0x00FF) | ((value & 0x7F) << 8);
    }

    public void setCX(int value) {
        r = (r & 0xFFE0) | (value & 0b11111);
    }

    public void setCY(int value) {
        r = (r & (~(0b11111 << 5))) | ((value & 0b11111) << 5);
    }

    public void setNt(int value) {
        r = (r & (~(0b11 << 10))) | ((value & 0b11) << 10);
    }

    public void setFY(int value) {
        r = (r & (~(0b111 << 12))) | ((value & 0b111) << 12);
    }
}
