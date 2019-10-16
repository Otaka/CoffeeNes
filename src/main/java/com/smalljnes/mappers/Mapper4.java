package com.smalljnes.mappers;

import com.smalljnes.Cpu;
import com.smalljnes.Mirroring;

/**
 * @author sad
 */
public class Mapper4 extends AbstractMapper {

    byte reg8000;
    byte[] regs = new byte[8];
    boolean horizontalMirroring;
    byte irqPeriod;
    byte irqCounter;
    boolean irqEnabled;

    public Mapper4(byte[] rom) {
        super(rom);
    }

    @Override
    public void cartridgeInserted(Cpu cpu) {
        super.cartridgeInserted(cpu);
        horizontalMirroring = true;
        irqEnabled = false;
        irqPeriod = irqCounter = 0;
        mapPrg(8, 3, -1);
        apply();
    }

    void apply() {
        cpu.printStatus("MP:apply. regs[7]=" + (regs[7] & 0xFF) + " reg8000=" + (reg8000 & 0xFF));
        mapPrg(8, 1, regs[7] & 0xFF);
        if ((((reg8000 & 0xFF) & (1 << 6))) == 0) {
            cpu.printStatus("MP:apply. PRG Mode 0 regs[6]=" + (regs[6] & 0xFF));
            mapPrg(8, 0, regs[6] & 0xFF);
            mapPrg(8, 2, -2);
        } else {
            cpu.printStatus("MP:apply. PRG Mode 1 regs[6]=" + (regs[6] & 0xFF));
            mapPrg(8, 0, -2);
            mapPrg(8, 2, regs[6] & 0xFF);
        }

        // CHR Mode 0:
        if ((reg8000 & (1 << 7)) == 0) {
            cpu.printStatus("MP:apply. CHR Mode 0 regs[0]=" + (regs[0] & 0xFF));
            cpu.printStatus("MP:apply. CHR Mode 0 regs[1]=" + (regs[1] & 0xFF));
            mapChr(2, 0, (regs[0] & 0xFF) >> 1);
            mapChr(2, 1, (regs[1] & 0xFF) >> 1);
            for (int i = 0; i < 4; i++) {
                mapChr(1, 4 + i, (regs[2 + i] & 0xFF));
            }
        } else {
            cpu.printStatus("MP:apply. CHR Mode 1 regs[0]=" + (regs[0] & 0xFF));
            cpu.printStatus("MP:apply. CHR Mode 1 regs[1]=" + (regs[1] & 0xFF));
            for (int i = 0; i < 4; i++) {
                mapChr(1, i, (regs[2 + i] & 0xFF));
            }
            mapChr(2, 2, (regs[0] & 0xFF) >> 1);
            mapChr(2, 3, (regs[1] & 0xFF) >> 1);
        }

        cpu.printStatus("MP:apply.Mirror=" + (horizontalMirroring?1:0));
        cpu.getPpu().setMirrorMode(horizontalMirroring ? Mirroring.HORIZONTAL : Mirroring.VERTICAL);
    }

    @Override
    public void chrWrite(int address, byte value) {
        chr[address] = value;
    }

    @Override
    public void write(int addr, byte v) {
        if (addr < 0x8000) {
            cpu.printStatus("MP:writeRam:addr=" + (addr) + " v=" + (v & 0xFF));
            prgRam[addr - 0x6000] = v;
        } else if ((addr & 0x8000) > 0) {
            switch (addr & 0xE001) {
                case 0x8000:
                    reg8000 = v;
                    cpu.printStatus("MP:setreg1:r8000=" + (reg8000 & 0xFF) + " v=" + (v & 0xFF));
                    break;
                case 0x8001:
                    regs[reg8000 & 0b111] = v;
                    cpu.printStatus("MP:setreg2:r8000=" + (reg8000 & 0xFF) + " v=" + (v & 0xFF));
                    break;
                case 0xA000:
                    cpu.printStatus("MP:mirror=" + (v & 0xFF));
                    horizontalMirroring = ((v & 0xFF) & 0x1) > 0;
                    break;
                case 0xC000:
                    irqPeriod = v;
                    cpu.printStatus("MP:irqPeriod=" + (v & 0xFF));
                    break;
                case 0xC001:
                    cpu.printStatus("MP:C001");
                    irqCounter = 0;
                    break;
                case 0xE000:
                    cpu.setIrq(irqEnabled = false);
                    cpu.printStatus("MP:E000");
                    break;
                case 0xE001:
                    irqEnabled = true;
                    cpu.printStatus("MP:E000");
                    break;
            }
            apply();
        }
        //return v;
    }

    @Override
    public void signalScanline() {
        cpu.printStatus("MP:scanline irqCounter=" + (irqCounter & 0xFF));
        if (irqCounter == 0) {
            irqCounter = irqPeriod;
        } else {
            irqCounter = (byte) ((irqCounter & 0xFF) - 1);
        }

        if (irqEnabled && irqCounter == 0) {
            cpu.printStatus("MP:scanline irq");
            cpu.setIrq(true);
        }
    }

    @Override
    public String toString() {
        return "MMC3";
    }
}
