package com.smalljnes;

/**
 * @author Dmitry
 */
public class Nes {

    private Cpu cpu;
    private Ppu ppu;
    private Apu apu;
    private Joypad joypad;
    private Cartridge cartridge;
    private OnDrawFrame onDrawFrameEvent;

    public Nes() {
        this(true, true, true);
    }

    public Nes(boolean hasPpu, boolean hasApu, boolean hasJoypad) {
        cpu = new Cpu();
        if (hasPpu) {
            ppu = new Ppu(this);
            cpu.setPpu(ppu);
        }
        if (hasApu) {
            apu = new Apu();
            cpu.setApu(apu);
        }
        if (hasJoypad) {
            joypad = new Joypad();
            cpu.setJoypad(joypad);
        }
    }

    public OnDrawFrame getOnDrawFrameEvent() {
        return onDrawFrameEvent;
    }

    public Joypad getJoypad() {
        return joypad;
    }

    public void setOnDrawFrameEvent(OnDrawFrame onDrawFrameEvent) {
        this.onDrawFrameEvent = onDrawFrameEvent;
    }

    public void insertCartridge(Cartridge cartridge) {
        this.cartridge = cartridge;
        cpu.setCartrige(cartridge);
        cartridge.cartridgeInserted(cpu);
    }

    public void powerUp() {
        if (cartridge == null) {
            throw new IllegalStateException("Please insert cartridge before power up");
        }
        cpu.powerUp();
        if (ppu != null) {
            ppu.reset();
        }
        if (apu != null) {
            apu.reset();
        }
        
    }

    public void start() {
        throw new IllegalStateException("Full run is not implemented yet");
    }

    public Cpu getCpu() {
        return cpu;
    }

    public Apu getApu() {
        return apu;
    }

    public Cartridge getCartridge() {
        return cartridge;
    }

    public Ppu getPpu() {
        return ppu;
    }

}
