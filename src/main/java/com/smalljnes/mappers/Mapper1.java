package com.smalljnes.mappers;

import com.smalljnes.Cpu;
import com.smalljnes.Mirroring;

/**
 * @author sad
 */
public class Mapper1 extends AbstractMapper {

    byte regs[] = new byte[4];
    byte tmpReg;
    int writeN;

    public Mapper1(byte[] rom) {
        super(rom);
        regs[0] = 0x0C;
        writeN = tmpReg = regs[1] = regs[2] = regs[3] = 0;
    }

    @Override
    public void cartridgeInserted(Cpu cpu) {
        super.cartridgeInserted(cpu);
        apply();
    }

    @Override
    public void write(int address, byte value) {
        // PRG RAM write;
        if (address < 0x8000) {
            prgRam[address - 0x6000] = value;
        } // Mapper register write:
        else if ((address & 0x8000) > 0) {
            // Reset:
            if ((value & 0x80) > 0) {
                writeN = 0;
                tmpReg = 0;
                regs[0] |= 0x0C;
                apply();
            } else {
                // Write a bit into the temporary register:
                tmpReg = (byte) ((((value & 1) << 4) | (tmpReg >> 1)) & 0xFF);
                // Finished writing all the bits:
                if (++writeN == 5) {
                    regs[(address >> 13) & 0b11] = tmpReg;
                    writeN = 0;
                    tmpReg = 0;
                    apply();
                }
            }
        }
    }

    void apply() {
        // 16KB PRG:
        if ((regs[0] & 0b1000) > 0) {
            // 0x8000 swappable, 0xC000 fixed to bank 0x0F:
            if ((regs[0] & 0b100) > 0) {
                mapPrg(16, 0, regs[3] & 0xF);
                mapPrg(16, 1, 0xF);
            } // 0x8000 fixed to bank 0x00, 0xC000 swappable:
            else {
                mapPrg(16, 0, 0);
                mapPrg(16, 1, regs[3] & 0xF);
            }
        } // 32KB PRG:
        else {
            mapPrg(32, 0, (regs[3] & 0xF) >> 1);
        }
        // 4KB CHR:
        if ((regs[0] & 0b10000) > 0) {
            mapChr(4, 0, regs[1]);
            mapChr(4, 1, regs[2]);
        } // 8KB CHR:
        else {
            mapChr(8, 0, regs[1] >> 1);
        }

        // Set mirroring:
        switch (regs[0] & 0b11) {
            case 2:
                cpu.getPpu().setMirrorMode(Mirroring.VERTICAL);
                break;
            case 3:
                cpu.getPpu().setMirrorMode(Mirroring.HORIZONTAL);
                break;
        }
    }

    @Override
    public void chrWrite(int address, byte value) {
        chr[address] = value;
    }
    
    @Override
    public String toString() {
        return "MMC1";
    }
    
}
