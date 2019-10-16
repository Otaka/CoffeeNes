package com.smalljnes.mappers;

import com.smalljnes.Cpu;
import com.smalljnes.Mirroring;

/**
 * @author Dmitry
 */
public abstract class AbstractMapper {

    byte[] rom;
    boolean chrRam = false;
    int[] prgMap = new int[4];
    int[] chrMap = new int[8];
    byte[] prg;
    byte[] chr;
    byte[] prgRam;
    int prgSize, chrSize, prgRamSize;
    protected Cpu cpu;

    public AbstractMapper(byte[] rom) {
        prgSize = (rom[4] & 0xFF) * 0x4000;
        chrSize = (rom[5] & 0xFF) * 0x2000;
        prgRamSize = (rom[8] == 0) ? 0x2000 : (rom[8] & 0xFF) * 0x2000;

        prg = new byte[prgSize];
        System.arraycopy(rom, 16, prg, 0, prgSize);

        prgRam = new byte[prgRamSize];

        if (chrSize > 0) {
            chr = new byte[chrSize];
            System.arraycopy(rom, 16 + prgSize, chr, 0, chrSize);
        } else {
            chrRam = true;
            chrSize = 0x2000;
            chr = new byte[chrSize];
        }
        this.rom = rom;
    }

    public void cartridgeInserted(Cpu cpu) {
        this.cpu=cpu;
        if (cpu.getPpu() != null) {
            cpu.getPpu().setMirrorMode(((rom[6] & 0x1) == 0) ? Mirroring.HORIZONTAL : Mirroring.VERTICAL);
        }
    }

    public byte read(int address, boolean debugInspect) {
        if (address >= 0x8000) {
            return prg[prgMap[(address - 0x8000) / 0x2000] + ((address - 0x8000) % 0x2000)];
        } else {
            int tAddress=(address - 0x6000);
            if(tAddress<0 ||tAddress>=prgRam.length){
                return 0;
            }
            return prgRam[tAddress];
        }
    }

    public byte chrRead(int address) {
        return chr[chrMap[address / 0x400] + (address % 0x400)];
    }

    public void chrWrite(int address, byte value) {
        //do nothing by default
    }

    public void signalScanline() {

    }

    void mapPrg(int pageKBs, int slot, int bank) {
        if (bank < 0) {
            bank = (prgSize / (0x400 * pageKBs)) + bank;
        }

        for (int i = 0; i < (pageKBs / 8); i++) {
            prgMap[(pageKBs / 8) * slot + i] = (pageKBs * 0x400 * bank + 0x2000 * i) % prgSize;
        }
    }

    void mapChr(int pageKBs, int slot, int bank) {
        for (int i = 0; i < pageKBs; i++) {
            chrMap[pageKBs * slot + i] = (pageKBs * 0x400 * bank + 0x400 * i) % chrSize;
        }
    }

    public void write(int address, byte value) {
       
    }
}
