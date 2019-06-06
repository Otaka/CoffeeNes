package com.smalljnes;

/**
 * @author Dmitry
 */
public class Joypad {

    private byte joypad_bits[] = new byte[2];
    boolean strobe;

    byte joyPadButtonsState;

    public void setJoyPadButtonsState(byte joyPadButtonsState) {
        this.joyPadButtonsState = joyPadButtonsState;
    }

    public void writeStrobe(boolean value) {
        // Read the joypad data on strobe's transition 1 -> 0.
        if (strobe && !value) {
            for (int i = 0; i < 2; i++) {
                joypad_bits[i] = get_joypad_state(i);
            }
        }
        
        strobe = value;
    }

    private byte get_joypad_state(int joypad) {
        if (joypad == 0) {
            return joyPadButtonsState;
        } else {
            return 0;
        }
    }

    public byte readState(int n) {
        if (strobe) {
            return (byte) (0x40 | (get_joypad_state(n) & 1));
        }

        // Get the status of a button and shift the register:
        int joypadValue = joypad_bits[n] & 0xFF;
        byte j = (byte) (0x40 | (joypadValue & 1));
        joypad_bits[n] = (byte) (joypadValue >> 1);
        return j;
    }
}
