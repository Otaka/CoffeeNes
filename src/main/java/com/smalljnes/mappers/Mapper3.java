package com.smalljnes.mappers;

import com.smalljnes.Cpu;
import com.smalljnes.Mirroring;

/**
 * @author sad
 */
public class Mapper3 extends AbstractMapper {

    byte regs;
    boolean verticalMirroring;
    boolean prg_size_16k;

    public Mapper3(byte[] rom) {
        super(rom);
        prg_size_16k = rom[4] == 1;
        verticalMirroring = (rom[6] & 0x01) > 0;
        regs = 0;
       
    }

    @Override
    public void cartridgeInserted(Cpu cpu) {
        super.cartridgeInserted(cpu);
        apply();
    }

    void apply() {
        if (prg_size_16k) {
            mapPrg(16, 0, 0);
            mapPrg(16, 1, 0);
        } else {
            /* no mirroring */
            mapPrg(16, 0, 0);
            mapPrg(16, 1, 1);
        }

        /* 8k bankswitched CHR */
        mapChr(8, 0, regs & 0b11);

        /* mirroring is based on the header (soldered) */
        cpu.getPpu().setMirrorMode(verticalMirroring ? Mirroring.VERTICAL : Mirroring.HORIZONTAL);
    }

    @Override
    public void chrWrite(int address, byte value) {
        chr[address] = value;
    }

    @Override
    public void write(int address, byte value) {
        if ((address & 0x8000) > 0) {
            regs = value;
            apply();
        }
    }

    @Override
    public String toString() {
        return "CNROM";
    }
    
}
