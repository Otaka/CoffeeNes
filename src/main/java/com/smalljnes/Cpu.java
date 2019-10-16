package com.smalljnes;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * @author Dmitry
 */
public class Cpu {

    final int TOTAL_CYCLES = 29781;
    final int NMI_INTERRUPT = 0;
    final int RESET_INTERRUPT = 1;
    final int IRQ_INTERRUPT = 2;
    final int BRK_INTERRUPT = 3;
    final int[] interruptVectors = new int[]{0xFFFA, 0xFFFC, 0xFFFE, 0xFFFE};

    private Ppu ppu;
    private Apu apu;
    private Joypad joypad;
    private Cartridge cartrige;

    byte[] ram = new byte[0x800];
    byte A, X, Y, S;
    int PC;
    boolean nmi, irq;
    int C, Z, I, D, V, N;//flags
    int remainingCycles = 0;

    public void setNmi(boolean nmi) {
        printStatus("C:setNmi");
        this.nmi = nmi;
    }

    public void setIrq(boolean value) {
        printStatus("C:setIrq");
        this.irq = value;
    }

    public Apu getApu() {
        return apu;
    }

    void setApu(Apu apu) {
        this.apu = apu;
    }

    void setPpu(Ppu ppu) {
        this.ppu = ppu;
    }

    public Joypad getJoypad() {
        return joypad;
    }

    public void setJoypad(Joypad joypad) {
        this.joypad = joypad;
    }

    public Ppu getPpu() {
        return ppu;
    }

    public Cartridge getCartrige() {
        return cartrige;
    }

    void setCartrige(Cartridge cartrige) {
        this.cartrige = cartrige;
    }

    public void executeOneInstruction() {
        int opcode = read((PC++) & 0xFFFF, false) & 0xFF;
        //PC++;
        switch (opcode) {
            case 0x00:
                interrupt(BRK_INTERRUPT);
                break;
            case 0x01:
                ora(indirectZXAddressing());
                break;
            case 0x05:
                ora(zeroPageAddressing());
                break;
            case 0x06:
                asl(zeroPageAddressing());
                break;
            case 0x08:
                php();
                break;
            case 0x09:
                ora(immediateAddressing());
                break;
            case 0x0A:
                asl_a();
                break;
            case 0x0D:
                ora(absoluteAddressing());
                break;
            case 0x0E:
                asl(absoluteAddressing());
                break;
            case 0x10:
                br(N, 0);
                break;
            case 0x11:
                ora(indirectZYAddressing(false));
                break;
            case 0x15:
                ora(zeroPageWithXAddressing());
                break;
            case 0x16:
                asl(zeroPageWithXAddressing());
                break;
            case 0x18:
                C = 0;
                cpuTick();
                break;
            case 0x19:
                ora(absoluteWithYAddressing(false));
                break;
            case 0x1D:
                ora(absoluteWithXAddressing(false));
                break;
            case 0x1E:
                asl(absoluteWithXAddressing(true));
                break;
            case 0x20:
                jsr();
                break;
            case 0x21:
                and(indirectZXAddressing());
                break;
            case 0x24:
                bit(zeroPageAddressing());
                break;
            case 0x25:
                and(zeroPageAddressing());
                break;
            case 0x26:
                rol(zeroPageAddressing());
                break;
            case 0x28:
                plp();
                break;
            case 0x29:
                and(immediateAddressing());
                break;
            case 0x2A:
                rol_a();
                break;
            case 0x2C:
                bit(absoluteAddressing());
                break;
            case 0x2D:
                and(absoluteAddressing());
                break;
            case 0x2E:
                rol(absoluteAddressing());
                break;
            case 0x30:
                br(N, 1);
                break;
            case 0x31:
                and(indirectZYAddressing(false));
                break;
            case 0x35:
                and(zeroPageWithXAddressing());
                break;
            case 0x36:
                rol(zeroPageWithXAddressing());
                break;
            case 0x38:
                C = 1;
                cpuTick();
                break;
            case 0x39:
                and(absoluteWithYAddressing(false));
                break;
            case 0x3D:
                and(absoluteWithXAddressing(false));
                break;
            case 0x3E:
                rol(absoluteWithXAddressing(true));
                break;
            case 0x40:
                rti();
                break;
            case 0x41:
                eor(indirectZXAddressing());
                break;
            case 0x45:
                eor(zeroPageAddressing());
                break;
            case 0x46:
                lsr(zeroPageAddressing());
                break;
            case 0x48:
                pha();
                break;
            case 0x49:
                eor(immediateAddressing());
                break;
            case 0x4A:
                lsr_a();
                break;
            case 0x4C:
                jmp();
                break;
            case 0x4D:
                eor(absoluteAddressing());
                break;
            case 0x4E:
                lsr(absoluteAddressing());
                break;
            case 0x50:
                br(V, 0);
                break;
            case 0x51:
                eor(indirectZYAddressing(false));
                break;
            case 0x55:
                eor(zeroPageWithXAddressing());
                break;
            case 0x56:
                lsr(zeroPageWithXAddressing());
                break;
            case 0x58:
                I = 0;
                cpuTick();
                break;
            case 0x59:
                eor(absoluteWithYAddressing(false));
                break;
            case 0x5D:
                eor(absoluteWithXAddressing(false));
                break;
            case 0x5E:
                lsr(_abx());
                break;
            case 0x60:
                rts();
                break;
            case 0x61:
                adc(indirectZXAddressing());
                break;
            case 0x65:
                adc(zeroPageAddressing());
                break;
            case 0x66:
                ror(zeroPageAddressing());
                break;
            case 0x68:
                pla();
                break;
            case 0x69:
                adc(immediateAddressing());
                break;
            case 0x6A:
                ror_a();
                break;
            case 0x6C:
                jmp_ind();
                break;
            case 0x6D:
                adc(absoluteAddressing());
                break;
            case 0x6E:
                ror(absoluteAddressing());
                break;
            case 0x70:
                br(V, 1);
                break;
            case 0x71:
                adc(indirectZYAddressing(false));
                break;
            case 0x75:
                adc(zeroPageWithXAddressing());
                break;
            case 0x76:
                ror(zeroPageWithXAddressing());
                break;
            case 0x78:
                I = 1;
                cpuTick();
                break;
            case 0x79:
                adc(absoluteWithYAddressing(false));
                break;
            case 0x7D:
                adc(absoluteWithXAddressing(false));
                break;
            case 0x7E:
                ror(absoluteWithXAddressing(true));
                break;
            case 0x81:
                st(A, indirectZXAddressing());
                break;
            case 0x84:
                st(Y, zeroPageAddressing());
                break;
            case 0x85:
                st(A, zeroPageAddressing());
                break;
            case 0x86:
                st(X, zeroPageAddressing());
                break;
            case 0x88:
                decY();
                break;
            case 0x8A:
                trToA(X);
                break;
            case 0x8C:
                st(Y, absoluteAddressing());
                break;
            case 0x8D:
                st(A, absoluteAddressing());
                break;
            case 0x8E:
                st(X, absoluteAddressing());
                break;
            case 0x90:
                br(C, 0);
                break;
            case 0x91:
                stIZY(A);
                break;
            case 0x94:
                st(Y, zeroPageWithXAddressing());
                break;
            case 0x95:
                st(A, zeroPageWithXAddressing());
                break;
            case 0x96:
                st(X, zeroPageWithYAddressing());
                break;
            case 0x98:
                trToA(Y);
                break;
            case 0x99:
                stAbsY(A);
                break;
            case 0x9A:
                trX2S();
                break;
            case 0x9D:
                stAbsX(A);
                break;
            case 0xA0:
                ldY(immediateAddressing());
                break;
            case 0xA1:
                ldA(indirectZXAddressing());
                break;
            case 0xA2:
                ldX(immediateAddressing());
                break;
            case 0xA4:
                ldY(zeroPageAddressing());
                break;
            case 0xA5:
                ldA(zeroPageAddressing());
                break;
            case 0xA6:
                ldX(zeroPageAddressing());
                break;
            case 0xA8:
                trToY(A);
                break;
            case 0xA9:
                ldA(immediateAddressing());
                break;
            case 0xAA:
                trToX(A);
                break;
            case 0xAC:
                ldY(absoluteAddressing());
                break;
            case 0xAD:
                ldA(absoluteAddressing());
                break;
            case 0xAE:
                ldX(absoluteAddressing());
                break;
            case 0xB0:
                br(C, 1);
                break;
            case 0xB1:
                ldA(indirectZYAddressing(false));
                break;
            case 0xB4:
                ldY(zeroPageWithXAddressing());
                break;
            case 0xB5:
                ldA(zeroPageWithXAddressing());
                break;
            case 0xB6:
                ldX(zeroPageWithYAddressing());
                break;
            case 0xB8:
                V = 0;
                cpuTick();
                break;
            case 0xB9:
                ldA(absoluteWithYAddressing(false));
                break;
            case 0xBA:
                trToX(S);
                break;
            case 0xBC:
                ldY(absoluteWithXAddressing(false));
                break;
            case 0xBD:
                ldA(absoluteWithXAddressing(false));
                break;
            case 0xBE:
                ldX(absoluteWithYAddressing(false));
                break;
            case 0xC0:
                cmp(Y, immediateAddressing());
                break;
            case 0xC1:
                cmp(A, indirectZXAddressing());
                break;
            case 0xC4:
                cmp(Y, zeroPageAddressing());
                break;
            case 0xC5:
                cmp(A, zeroPageAddressing());
                break;
            case 0xC6:
                dec(zeroPageAddressing());
                break;
            case 0xC8:
                incY();
                break;
            case 0xC9:
                cmp(A, immediateAddressing());
                break;
            case 0xCA:
                decX();
                break;
            case 0xCC:
                cmp(Y, absoluteAddressing());
                break;
            case 0xCD:
                cmp(A, absoluteAddressing());
                break;
            case 0xCE:
                dec(absoluteAddressing());
                break;
            case 0xD0:
                br(Z, 0);
                break;
            case 0xD1:
                cmp(A, indirectZYAddressing(false));
                break;
            case 0xD5:
                cmp(A, zeroPageWithXAddressing());
                break;
            case 0xD6:
                dec(zeroPageWithXAddressing());
                break;
            case 0xD8:
                D = 0;
                cpuTick();
                break;
            case 0xD9:
                cmp(A, absoluteWithYAddressing(false));
                break;
            case 0xDD:
                cmp(A, absoluteWithXAddressing(false));
                break;
            case 0xDE:
                dec(_abx());
                break;
            case 0xE0:
                cmp(X, immediateAddressing());
                break;
            case 0xE1:
                sbc(indirectZXAddressing());
                break;
            case 0xE4:
                cmp(X, zeroPageAddressing());
                break;
            case 0xE5:
                sbc(zeroPageAddressing());
                break;
            case 0xE6:
                inc(zeroPageAddressing());
                break;
            case 0xE8:
                incX();
                break;
            case 0xE9:
                sbc(immediateAddressing());
                break;
            case 0xEA:
                nop();
                break;
            case 0xEC:
                cmp(X, absoluteAddressing());
                break;
            case 0xED:
                sbc(absoluteAddressing());
                break;
            case 0xEE:
                inc(absoluteAddressing());
                break;
            case 0xF0:
                br(Z, 1);
                break;
            case 0xF1:
                sbc(indirectZYAddressing(false));
                break;
            case 0xF5:
                sbc(zeroPageWithXAddressing());
                break;
            case 0xF6:
                inc(zeroPageWithXAddressing());
                break;
            case 0xF8:
                D = 1;
                cpuTick();
                break;
            case 0xF9:
                sbc(absoluteWithYAddressing(false));
                break;
            case 0xFD:
                sbc(absoluteWithXAddressing(false));
                break;
            case 0xFE:
                inc(absoluteWithXAddressing(true));
                break;
            // illegal instructions
            case 0x04:
            case 0x44:
            case 0x64:
                illegalNop(zeroPageAddressing());
                break;
            case 0x0C:
                illegalNop(absoluteAddressing());
                break;
            case 0x14:
            case 0x34:
            case 0x54:
            case 0x74:
            case 0xD4:
            case 0xF4:
                illegalNop(zeroPageWithXAddressing());
                break;
            case 0x1A:
            case 0x3A:
            case 0x5A:
            case 0x7A:
            case 0xDA:
            case 0xFA:
                nop();
                break;
            case 0x80:
            case 0x82:
            case 0x89:
            case 0xC2:
            case 0xE2:
                illegalNop(immediateAddressing());
                break;
            case 0x1C:
            case 0x3C:
            case 0x5C:
            case 0x7C:
            case 0xDC:
            case 0xFC:
                illegalNop(absoluteWithXAddressing(false));
                break;
            case 0xA3:
                illegalLax(indirectZXAddressing());
                break;
            case 0xA7:
                illegalLax(zeroPageAddressing());
                break;
            case 0xAF:
                illegalLax(absoluteAddressing());
                break;
            case 0xB3:
                illegalLax(indirectZYAddressing(false));
                break;
            case 0xB7:
                illegalLax(zeroPageWithYAddressing());
                break;
            case 0xBF:
                illegalLax(absoluteWithYAddressing(false));
                break;
            case 0x83:
                illegalSax(indirectZXAddressing());
                break;
            case 0x87:
                illegalSax(zeroPageAddressing());
                break;
            case 0x8F:
                illegalSax(absoluteAddressing());
                break;
            case 0x97:
                illegalSax(zeroPageWithYAddressing());
                break;
            case 0xEB:
                sbc(immediateAddressing());
                break;
            case 0xC3:
                illegalDCP(indirectZXAddressing());
                break;
            case 0xC7:
                illegalDCP(zeroPageAddressing());
                break;
            case 0xCF:
                illegalDCP(absoluteAddressing());
                break;
            case 0xD3:
                illegalDCP(indirectZYAddressing(false));
                break;
            case 0xD7:
                illegalDCP(zeroPageWithXAddressing());
                break;
            case 0xDB:
                illegalDCP(absoluteWithYAddressing(false));
                break;
            case 0xDF:
                illegalDCP(absoluteWithXAddressing(false));
                break;
            case 0xE3:
                illegalISB(indirectZXAddressing());
                break;
            case 0xE7:
                illegalISB(zeroPageAddressing());
                break;
            case 0xEF:
                illegalISB(absoluteAddressing());
                break;
            case 0xF3:
                illegalISB(indirectZYAddressing(false));
                break;
            case 0xF7:
                illegalISB(zeroPageWithXAddressing());
                break;
            case 0xFB:
                illegalISB(absoluteWithYAddressing(false));
                break;
            case 0xFF:
                illegalISB(absoluteWithXAddressing(false));
                break;

            case 0x03:
                illegalSLO(indirectZXAddressing());
                break;
            case 0x07:
                illegalSLO(zeroPageAddressing());
                break;
            case 0x0F:
                illegalSLO(absoluteAddressing());
                break;
            case 0x13:
                illegalSLO(indirectZYAddressing(false));
                break;
            case 0x17:
                illegalSLO(zeroPageWithXAddressing());
                break;
            case 0x1B:
                illegalSLO(absoluteWithYAddressing(false));
                break;
            case 0x1F:
                illegalSLO(absoluteWithXAddressing(false));
                break;

            case 0x23:
                illegalRLA(indirectZXAddressing());
                break;
            case 0x27:
                illegalRLA(zeroPageAddressing());
                break;
            case 0x2F:
                illegalRLA(absoluteAddressing());
                break;
            case 0x33:
                illegalRLA(indirectZYAddressing(false));
                break;
            case 0x37:
                illegalRLA(zeroPageWithXAddressing());
                break;
            case 0x3B:
                illegalRLA(absoluteWithYAddressing(false));
                break;
            case 0x3F:
                illegalRLA(absoluteWithXAddressing(false));
                break;

            case 0x43:
                illegalSRE(indirectZXAddressing());
                break;
            case 0x47:
                illegalSRE(zeroPageAddressing());
                break;
            case 0x4F:
                illegalSRE(absoluteAddressing());
                break;
            case 0x53:
                illegalSRE(indirectZYAddressing(false));
                break;
            case 0x57:
                illegalSRE(zeroPageWithXAddressing());
                break;
            case 0x5B:
                illegalSRE(absoluteWithYAddressing(false));
                break;
            case 0x5F:
                illegalSRE(absoluteWithXAddressing(false));
                break;

            case 0x63:
                illegalRRA(indirectZXAddressing());
                break;
            case 0x67:
                illegalRRA(zeroPageAddressing());
                break;
            case 0x6F:
                illegalRRA(absoluteAddressing());
                break;
            case 0x73:
                illegalRRA(indirectZYAddressing(false));
                break;
            case 0x77:
                illegalRRA(zeroPageWithXAddressing());
                break;
            case 0x7B:
                illegalRRA(absoluteWithYAddressing(false));
                break;
            case 0x7F:
                illegalRRA(absoluteWithXAddressing(false));
                break;

            default:
                throw new IllegalStateException("Instruction [0x" + Utils.toHex((byte) (opcode & 0xFF)) + "] is not implemented");
        }
    }

    void clearFlags() {
        N = V = D = I = Z = C = 0;
    }

    byte getFlagByteValue() {
        return (byte) (C | Z << 1 | I << 2 | D << 3 | 1 << 5 | V << 6 | N << 7);
    }

    void setFlagsFromByteValue(byte p) {
        C = (p & 0b1);
        Z = (p & 0b10) >> 1;
        I = (p & 0b100) >> 2;
        D = (p & 0b1000) >> 3;
        V = (p & 0b1000000) >> 6;
        N = (p & 0b10000000) >> 7;
    }

    int elapsed() {
        return TOTAL_CYCLES - remainingCycles;
    }

    void cpuTick() {
        if (ppu != null) {
            ppu.step();
            ppu.step();
            ppu.step();
        }
        remainingCycles--;
    }

    //updatind flags
    void updateCv(byte x, byte y, short r) {
        C = (r > 0xFF) ? 1 : 0;
        V = (~(x ^ y) & (x ^ r) & 0x80) == 0 ? 0 : 1;
    }

    void updateNz(byte x) {
        N = (x & 0x80) == 0 ? 0 : 1;
        Z = (x == 0) ? 1 : 0;
    }

    boolean cross(int a, byte i) {
        return (((a & 0xFFFF) + (i & 0xFF)) & 0xFF00) != (a & 0xFF00);
    }

    boolean jumpCross(int a, byte i) {
        return (((a & 0xFFFF) + i) & 0xFF00) != (a & 0xFF00);
    }

    void dma_oam(int bank) {
        for (int i = 0; i < 256; i++) {
            write(0x2014, read(bank * 0x100 + i, false));
        }
    }

    void write(int address, byte value) {
        cpuTick();
        if (address >= 0x0000 && address <= 0x1FFF) {
            ram[address % 0x800] = value;
            return;
        } else if (address >= 0x2000 && address <= 0x3FFF) {
            if (ppu != null) {
                ppu.writeRegister(address, value);
            }
            return;
        } else if ((address >= 0x4000 && address <= 0x4013)
                || (address == 0x4015)
                || (address == 0x4017)) {
            if (apu != null) {
                apu.write(elapsed(), address, value);
            }
            return;
        } else if (address == 0x4014) {
            dma_oam((int) value);
            return;
        } else if (address == 0x4016) {
            if (joypad != null) {
                joypad.writeStrobe((value & 1) > 0);
            }
            return;
        } else if (address >= 0x4018 && address <= 0xFFFF) {
            if (cartrige != null) {
                cartrige.write(address, value);
            }
            return;
        }
        throw new IllegalArgumentException("Address [0x" + Integer.toHexString(address) + "] is not handled for writing");
    }

    byte read(int address, boolean debugInspect) {
        if (!debugInspect) {
            cpuTick();
        }
        if (address >= 0x0000 && address <= 0x1FFF) {
            return ram[address % 0x800];
        } else if (address >= 0x2000 && address <= 0x3FFF) {
            if (ppu != null) {
                return (byte) ppu.readRegister(address % 8, debugInspect);
            }
            return 0;
        } else if ((address >= 0x4000 && address <= 0x4013) || address == 0x4015) {
            if (apu != null) {
                return apu.read(elapsed(), address);
            }
            return (byte) 0xFF;
        } else if (address >= 0x4016 && address <= 0x4017) {
            if (joypad != null) {
                return joypad.readState(address - 0x4016);
            }
            return 0;
        } else if (address >= 0x4018 && address <= 0xFFFF) {
            if (cartrige != null) {
                return cartrige.read(address, false);
            }
        }
        throw new IllegalArgumentException("Address [0x" + Integer.toHexString(address) + "] is not handled for reading");
    }

    int read16_d(int a, int b, boolean debugInspect) {
        return ((read(a, debugInspect) & 0xFF)
                | ((read(b, debugInspect) & 0xFF) << 8))
                & 0xFFFF;
    }

    int read16(int a, boolean debugInspect) {
        return read16_d(a, a + 1, debugInspect);
    }

    void push(byte v) {
        int oldStack = S & 0xFF;
        S = (byte) ((S & 0xFF) - 1);
        write(0x100 + oldStack, v);
    }

    byte pop() {
        S = (byte) ((S & 0xFF) + 1);
        return read(0x100 + (S & 0xFF), false);
    }

    int immediateAddressing() {
        return (int) PC++;
    }

    int immediate16Addressing() {
        PC += 2;
        return (int) PC - 2;
    }

    int absoluteAddressing() {
        int address = immediate16Addressing();
        return read16(address, false);
    }
    
    private int _abx(){
        cpuTick();
        return absoluteAddressing()+(X&0xFF);
    }

    int absoluteWithXAddressing(boolean alwaysExtraTick) {
        int a = absoluteAddressing();
        if (alwaysExtraTick || cross((short) (a & 0xFFFF), X)) {
            cpuTick();//extra tick
        }

        return (a + (X & 0xFF)) & 0xFFFF;
    }

    int absoluteWithYAddressing(boolean alwaysExtraTick) {
        int a = absoluteAddressing();
        if (alwaysExtraTick || cross((short) (a & 0xFFFF), Y)) {
            cpuTick();//extra tick
        }

        return (a + (Y & 0xFF)) & 0xFFFF;
    }

    int zeroPageAddressing() {
        int address = immediateAddressing();
        return read(address, false) & 0xFF;
    }

    int zeroPageWithXAddressing() {
        cpuTick();
        int immediate = immediateAddressing();
        int valueUnderImmediate = read(immediate, false) & 0xFF;
        return (valueUnderImmediate + (X & 0xFF)) % 0x100;
    }

    int zeroPageWithYAddressing() {
        cpuTick();
        int address = immediateAddressing();
        return ((read(address, false) & 0xFF) + (Y & 0xFF)) % 0x100;
    }

    int indirectZXAddressing() {
        int i = zeroPageWithXAddressing() & 0xFF;
        return read16_d(i, (i + 1) % 0x100, false);
    }

    int _indirectZYAddressing() {
        int i = zeroPageAddressing() & 0xFF;
        return read16_d(i, (i + 1) % 0x100, false) + (Y & 0xFF);
    }

    int indirectZYAddressing(boolean alwaysExtraTick) {
        int a = _indirectZYAddressing() & 0xFFFF;
        if (alwaysExtraTick || cross(a - (Y & 0xFF), Y)) {
            cpuTick();
        }
        return a;
    }

    void interrupt(int interruptType) {
        printStatus("C:interrupt");
        cpuTick();
        if (interruptType != BRK_INTERRUPT) {
            cpuTick();
        }
        if (interruptType == RESET_INTERRUPT) {
            S = (byte) ((S & 0xFF) - 3);
            cpuTick();
            cpuTick();
            cpuTick();
        } else {
            //push return values and flags
            push((byte) ((PC >> 8) & 0xFF));
            push((byte) (PC & 0xFF));
            push((byte) (getFlagByteValue() | ((interruptType == BRK_INTERRUPT) ? 1 : 0) << 4));
        }
        I = 1;
        PC = read16(interruptVectors[interruptType], false);
        if (interruptType == NMI_INTERRUPT) {
            nmi = false;
        }
    }

    void rts() {
        printStatus("RTS");
        cpuTick();
        cpuTick();
        int v1 = pop() & 0xFF;
        int v2 = pop() & 0xFF;
        PC = ((v1 | (v2 << 8)) + 1) & 0xFFFF;
        cpuTick();
    }

    void rti() {
        printStatus("RTI");
        plp();
        int value1 = pop() & 0xFF;
        int value2 = pop() & 0xFF;
        PC = (value1 | (value2 << 8)) & 0xFFFF;
    }

    void jmp_ind() {
        printStatus("C:JMP_I");
        int address = immediate16Addressing();
        int i = read16(address, false);
        PC = read16_d(i, (i & 0xFF00) | ((i + 1) % 0x100), false);
    }

    void jmp() {
        printStatus("C:JMP");
        int address = immediate16Addressing();
        PC = read16(address, false);
    }

    void trToA(byte value) {
        printStatus("C:TR");
        A = value;
        updateNz(A);
        cpuTick();
    }

    void trToX(byte value) {
        printStatus("C:TR");
        X = value;
        updateNz(X);
        cpuTick();
    }

    void trToY(byte value) {
        printStatus("C:TR");
        Y = value;
        updateNz(Y);
        cpuTick();
    }

    void trX2S() {
        printStatus("C:TXS");
        S = X;
        cpuTick();
    }

    void ldA(int address) {
        int p = read(address, false) & 0xFF;
        printStatus("LD");
        A = (byte) p;
        updateNz(A);
    }

    void ldX(int address) {
        int p = read(address, false) & 0xFF;
        printStatus("LD");
        X = (byte) p;
        updateNz(X);
    }

    void ldY(int address) {
        int p = read(address, false) & 0xFF;
        printStatus("LD");
        Y = (byte) p;
        updateNz(Y);
    }

    void st(byte value, int address) {
        printStatus("ST $" + address + ":" + (value & 0xFF));
        write(address, value);
    }
    
    void stAbsX(byte value) {
        cpuTick();
        int address=absoluteWithXAddressing(false);
        printStatus("ST $" + address + ":" + (value & 0xFF));
        write(address, value);
    }
    
    void stAbsY(byte value) {
        cpuTick();
        int address=absoluteWithYAddressing(false);
        printStatus("ST $" + address + ":" + (value & 0xFF));
        write(address, value);
    }
    
    void stIZY(byte value) {
        cpuTick();
        int address=_indirectZYAddressing();
        printStatus("ST $" + address + ":" + (value & 0xFF));
        write(address, value);
    }

    void nop() {
        printStatus("C:nop");
        cpuTick();
    }

    void br(int flag, int expectedFlagValue) {
        printStatus("C:BR");
        byte jumpOffset = read(immediateAddressing(), false);
        if (flag == expectedFlagValue) {
            printStatus("C:BR JUMP");
            cpuTick();
            //disable while debug MMC3 
           /* if (jumpCross(PC, jumpOffset)) {
                cpuTick();
            }
*/
            PC += jumpOffset;
        }
    }

    void jsr() {
        printStatus("C:JSR");
        int addressToPush = (PC & 0xFFFF) + 1;
        cpuTick();
        push((byte) ((addressToPush >> 8) & 0xFF));
        push((byte) ((addressToPush) & 0xFF));
        PC = read16(immediate16Addressing(), false);
    }

    void adc(int address) {
        int p = read(address, false) & 0xFF;
        printStatus("ADC");
        short r = (short) ((A & 0xFF) + p + C);
        updateCv(A, (byte) (p & 0xFF), r);
        A = (byte) r;
        updateNz(A);
    }

    void cmp(byte r, int address) {
        int p = read(address, false) & 0xFF;
        printStatus("CMP");
        int rValue = r & 0xFF;
        updateNz((byte) (rValue - p));
        C = rValue >= p ? 1 : 0;
    }

    void sbc(int address) {
        int value = read(address, false) & 0xFF;
        printStatus("SBC");
        int result;
        result = (A & 0xFF) - value - ((C == 1) ? 0 : 1);

        C = ((result >> 8 == 0) ? 1 : 0);
        // set overflow flag
        V = ((((A ^ value) & 0x80) != 0) && (((A ^ result) & 0x80) != 0)) ? 1 : 0;
        A = (byte) (result & 0xff);   //0
        Z = (A == 0) ? 1 : 0;
        N = ((result & 0x80) != 0) ? 1 : 0;
    }

    void and(int address) {
        int value = read(address, false) & 0xFF;
        printStatus("AND");
        A = (byte) ((A & 0xFF) & value);
        updateNz(A);
    }

    void bit(int address) {
        int value = read(address, false) & 0xFF;
        printStatus("BIT");
        Z = ((A & 0xFF) & value) > 0 ? 0 : 1;
        N = (value & 0x80) > 0 ? 1 : 0;
        V = (value & 0x40) > 0 ? 1 : 0;
    }

    void eor(int address) {
        int value = read(address, false) & 0xFF;
        printStatus("EOR");
        A = (byte) ((A & 0xFF) ^ value);
        updateNz(A);
    }

    void ora(int address) {
        byte value = read(address, false);
        printStatus("ORA");
        A = (byte) ((A & 0xFF) | value);
        updateNz(A);
    }

    void php() {
        printStatus("C:PHP");
        cpuTick();
        push((byte) (getFlagByteValue() | 1 << 4));//B flag set
    }

    void plp() {
        printStatus("C:PLP");
        cpuTick();
        cpuTick();
        byte flagValue = pop();
        flagValue = (byte) (flagValue & 0xef);
        flagValue = (byte) (flagValue | 0x20);
        setFlagsFromByteValue(flagValue);
    }

    void pla() {
        printStatus("C:PLA");
        cpuTick();
        cpuTick();
        A = pop();
        updateNz(A);
    }

    void pha() {
        printStatus("C:PHA");
        cpuTick();
        push(A);
    }

    void asl(int address) {
        byte value = read(address, false);
        printStatus("ASL");
        C = (value & 0x80) == 0 ? 0 : 1;
        cpuTick();
        value = (byte) ((value & 0xFF) << 1);

        write(address, value);
        updateNz(value);
    }

    void lsr(int address) {
        byte value = read(address, false);
        printStatus("LSR");
        C = value & 0x01;
        value = (byte) ((value & 0xFF) >> 1);

        write(address, value);
        cpuTick();
        updateNz(value);
    }

    void asl_a() {
        printStatus("ASL_A");
        byte value = A;
        C = ((value & 0xFF) & 0x80) == 0 ? 0 : 1;
        A = (byte) ((value & 0xFF) << 1);
        updateNz(A);
        cpuTick();
    }

    void lsr_a() {
        printStatus("LSR_A");
        byte value = A;
        C = value & 0x01;
        A = (byte) ((value & 0xFF) >> 1);
        updateNz(A);
        cpuTick();
    }

    void rol_a() {
        printStatus("ROL_A");
        int c = C;
        C = (A & 0x80) == 0 ? 0 : 1;
        A = (byte) (((A & 0xFF) << 1) | c);
        updateNz(A);
        cpuTick();
    }

    void ror_a() {
        printStatus("ROR_A");
        int c = C << 7;
        C = A & 0x01;
        A = (byte) (c | ((A & 0xFF) >> 1));
        updateNz(A);
        cpuTick();
    }

    void rol(int address) {
        byte value = read(address, false);
        printStatus("ROL");
        int c = C;
        C = ((value & 0x80) == 0) ? 0 : 1;

        cpuTick();
        value = (byte) (((value & 0xFF) << 1) | c);
        write(address, value);
        updateNz(value);
    }

    void ror(int address) {
        byte value = read(address, false);
        printStatus("ROR");
        int c = C << 7;
        C = value & 0x1;
        cpuTick();

        value = (byte) (c | ((value & 0xFF) >> 1));
        write(address, value);
        updateNz(value);
    }

    void dec(int address) {
        byte value = read(address, false);
        printStatus("DEC");
        cpuTick();
        value--;

        write(address, value);
        updateNz(value);
    }

    void decX() {
        printStatus("DEX");
        X--;
        updateNz(X);
        cpuTick();
    }

    void incX() {
        printStatus("INX");
        X++;
        updateNz(X);
        cpuTick();
    }

    void decY() {
        printStatus("DEX");
        Y--;
        updateNz(Y);
        cpuTick();
    }

    void incY() {
        printStatus("INX");
        Y++;
        updateNz(Y);
        cpuTick();
    }

    void inc(int address) {
        byte value = read(address, false);
        printStatus("INC");
        cpuTick();
        value++;

        write(address, value);
        updateNz(value);
    }

    void illegalLax(int address) {
        int p = read(address, false) & 0xFF;
        A = (byte) p;
        X = (byte) p;
        updateNz(A);
    }

    void illegalSax(int address) {
        write(address, (byte) ((A & 0xFF) & (X & 0xFF)));
    }

    void illegalNop(int address) {
        read(address, true);
        //do nothing
        cpuTick();
    }

    void illegalDCP(int address) {
        int value = read(address, true) & 0xFF;
        value = (value - 1) & 0xFF;
        cpuTick();
        write(address, (byte) value);
        cpuTick();
        int aValue = A & 0xFF;
        updateNz((byte) (aValue - value));
        C = aValue >= value ? 1 : 0;
    }

    void illegalISB(int address) {
        int value = read(address, true) & 0xFF;
        value = (value + 1) & 0xFF;
        cpuTick();
        write(address, (byte) value);
        cpuTick();
        //final int value = ram.read(addr);//64
        int result;
        result = (A & 0xFF) - value - ((C == 1) ? 0 : 1);
        C = ((result >> 8 == 0) ? 1 : 0);
        // set overflow flag
        V = ((((A ^ value) & 0x80) != 0) && (((A ^ result) & 0x80) != 0)) ? 1 : 0;
        A = (byte) (result & 0xff);   //0
        Z = (A == 0) ? 1 : 0;
        N = ((result & 0x80) != 0) ? 1 : 0;
    }

    void illegalSLO(int address) {
        byte value = read(address, false);
        C = (value & 0x80) == 0 ? 0 : 1;
        value = (byte) ((value & 0xFF) << 1);

        write(address, value);
        cpuTick();
        //byte value = read(address, false);
        A = (byte) ((A & 0xFF) | value);
        updateNz(A);
    }

    void illegalRLA(int address) {
        byte value = read(address, false);
        int c = C;
        C = ((value & 0x80) == 0) ? 0 : 1;

        value = (byte) (((value & 0xFF) << 1) | c);
        write(address, value);
        cpuTick();
        A = (byte) ((A & 0xFF) & (value & 0xFF));
        updateNz(A);
    }

    void illegalSRE(int address) {
        byte value = read(address, false);
        C = value & 0x01;
        value = (byte) ((value & 0xFF) >> 1);
        write(address, value);
        cpuTick();
        A = (byte) ((A & 0xFF) ^ value);
        updateNz(A);
    }

    void illegalRRA(int address) {
        int value = read(address, false) & 0xFF;
        int c = C << 7;
        C = value & 0x1;

        value = c | (value >> 1);
        value = value & 0xFF;
        write(address, (byte) (value));
        cpuTick();
        short r = (short) ((A & 0xFF) + value + C);
        updateCv(A, (byte) value, r);
        A = (byte) r;
        updateNz(A);
    }

    public void runFrame() {
        remainingCycles += TOTAL_CYCLES;
        int lastPC = 0;
        try {
            while (remainingCycles > 0) {
                lastPC = PC;
                if (lastPC == 51533) {
                    disableDebug();
                }
                if (nmi) {
                    interrupt(NMI_INTERRUPT);
                } else if (irq && I == 0) {
                    interrupt(IRQ_INTERRUPT);
                }

                executeOneInstruction();
            }
        } catch (Exception ex) {
            System.err.println("Error while executing instruction " + lastPC);
            ex.printStackTrace();
        }
    }
/*
    static {
        try {
            System.setProperty("line.separator", "\n");
            debugLogStream = new PrintStream(new BufferedOutputStream(new FileOutputStream("c:/temp/cpuJava.log")));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
*/
    public void powerUp() {
        remainingCycles = 0;
        setFlagsFromByteValue((byte) 0x4);
        A = X = Y = 0;
        S = (byte) 0x0;
        PC = 0x34;
        Arrays.fill(ram, (byte) 0);
        nmi = irq = false;
        interrupt(RESET_INTERRUPT);
    }

    boolean allowDebug = false;
    static PrintStream debugLogStream;
    boolean printToStdout = false;

    void disableDebug() {
        allowDebug = false;
        if (debugLogStream != null) {
            debugLogStream.close();
        }

        debugLogStream = null;
    }

    int printIndex = -1;

    public void printStatus(String str) {
        if (allowDebug) {
            printIndex++;
            if (printIndex == 257189) {
                //debug set apu mock value
                apu.setMock((byte)11);
            }
            
            if (printIndex == 2059381) {
                int x=0;
             //   printToStdout=true;
                
            }
            String message=String.format("%d %s PC=%d A=%d X=%d Y=%d S=%d P=%d", printIndex, str, PC, A & 0xFF, X & 0xFF, Y & 0xFF, S & 0xFF, getFlagByteValue() & 0xFF);
            debugLogStream.println(message);
            if(printToStdout){
                System.out.println(message);
            }

        }
    }
}
