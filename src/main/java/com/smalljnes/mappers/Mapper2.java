package com.smalljnes.mappers;

import com.smalljnes.Cpu;
import com.smalljnes.Mirroring;

/**
 * @author sad
 */
public class Mapper2 extends AbstractMapper {

    byte regs;
    boolean verticalMirroring;

    public Mapper2(byte[] rom) {
        super(rom);
        regs = 0;
        verticalMirroring = (rom[6] & 0x01) > 0;
    }

    @Override
    public void cartridgeInserted(Cpu cpu) {
        super.cartridgeInserted(cpu);
        apply();
    }

    private void apply() {
        mapPrg(16, 0, regs & 0xF);
        mapPrg(16, 1, 0xF);

        /* 8k of CHR */
        mapChr(8, 0, 0);

        /* mirroring is based on the header (soldered) */
        cpu.getPpu().setMirrorMode(verticalMirroring ? Mirroring.VERTICAL : Mirroring.HORIZONTAL);
    }

    @Override
    public void write(int address, byte value) {
        if ((address & 0x8000) > 0) {
            regs = value;
            apply();
        }
    }

    @Override
    public void chrWrite(int address, byte value) {
        chr[address] = value;
    }

    @Override
    public String toString() {
        return "UxROM";
    }
    
    
}
