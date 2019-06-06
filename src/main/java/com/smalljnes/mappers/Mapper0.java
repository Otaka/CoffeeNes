package com.smalljnes.mappers;

import com.smalljnes.Cpu;

/**
 * @author Dmitry<br>
 * The generic designation NROM refers to the Nintendo cartridge boards NES-NROM-128, NES-NROM-256<br>
 * All Banks are fixed,
 */
public class Mapper0 extends AbstractMapper{

    public Mapper0(byte[] rom) {
        super(rom);
        mapPrg(32, 0, 0);
        mapChr(8, 0, 0);
    }

    @Override
    public String toString() {
        return "NROM";
        
    }

}
