package com.smalljnes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry
 */
public class Mos6502Decompiler {

    private Map<Integer, String> instructions = new HashMap<>();

    public Mos6502Decompiler() {
        instructions.put(0x00, "BRK");
        instructions.put(0x01, "ORA ${izx}");
        instructions.put(0x05, "ORA ${zp}");
        instructions.put(0x06, "ASL ${zp}");
        instructions.put(0x08, "PHP");
        instructions.put(0x09, "ORA ${imm}");
        instructions.put(0x0A, "ASL A");
        instructions.put(0x0D, "ORA ${abs}");
        instructions.put(0x0E, "ASL ${abs}");
        instructions.put(0x10, "BPL ${rel}");
        instructions.put(0x11, "ORA ${izy}");
        instructions.put(0x15, "ORA ${zpx}");
        instructions.put(0x16, "ASL ${zpx}");
        instructions.put(0x18, "CLC");
        instructions.put(0x19, "ORA ${aby}");
        instructions.put(0x1D, "ORA ${abx}");
        instructions.put(0x1E, "ASL ${abx}");
        instructions.put(0x20, "JSR ${rel16}");
        instructions.put(0x21, "AND ${izx}");
        instructions.put(0x24, "BIT ${zp}");
        instructions.put(0x25, "AND ${zp}");
        instructions.put(0x26, "ROL ${zp}");
        instructions.put(0x28, "PLP");
        instructions.put(0x29, "AND ${imm}");
        instructions.put(0x2A, "ROL A");
        instructions.put(0x2C, "BIT ${abs}");
        instructions.put(0x2D, "AND ${abs}");
        instructions.put(0x2E, "ROL ${abs}");
        instructions.put(0x30, "BMI ${rel}");
        instructions.put(0x31, "AND ${izy}");
        instructions.put(0x35, "AND ${zpx}");
        instructions.put(0x36, "ROL ${zpx}");
        instructions.put(0x38, "SEC");
        instructions.put(0x39, "AND ${aby}");
        instructions.put(0x3D, "AND ${abx}");
        instructions.put(0x3E, "ROL ${abx}");
        instructions.put(0x40, "RTI");
        instructions.put(0x41, "EOR ${izx}");
        instructions.put(0x45, "EOR ${zp}");
        instructions.put(0x46, "LSR ${zp}");
        instructions.put(0x48, "PHA");
        instructions.put(0x49, "EOR ${imm}");
        instructions.put(0x4A, "LSR A");
        instructions.put(0x4C, "JMP ${imm16}");
        instructions.put(0x4D, "EOR ${abs}");
        instructions.put(0x4E, "LSR ${abs}");
        instructions.put(0x50, "BVC ${rel}");
        instructions.put(0x51, "EOR ${izy}");
        instructions.put(0x55, "EOR ${zpx}");
        instructions.put(0x56, "LSR ${zpx}");
        instructions.put(0x58, "CLI");
        instructions.put(0x59, "EOR ${aby}");
        instructions.put(0x5D, "EOR ${abx}");
        instructions.put(0x5E, "LSR ${abx}");
        instructions.put(0x60, "RTS");
        instructions.put(0x61, "ADC ${izx}");
        instructions.put(0x65, "ADC ${zp}");
        instructions.put(0x66, "ROR ${zp}");
        instructions.put(0x68, "PLA");
        instructions.put(0x69, "ADC ${imm}");
        instructions.put(0x6A, "ROR A");
        instructions.put(0x6C, "JMP ${absjump}");
        instructions.put(0x6D, "ADC ${abs}");
        instructions.put(0x6E, "ROR ${abs}");
        instructions.put(0x70, "BVS ${rel}");
        instructions.put(0x71, "ADC ${izy}");
        instructions.put(0x75, "ADC ${zpx}");
        instructions.put(0x76, "ROR ${zpx}");
        instructions.put(0x78, "SEI");
        instructions.put(0x79, "ADC ${aby}");
        instructions.put(0x7D, "ADC ${abx}");
        instructions.put(0x7E, "ROR ${abx}");

        instructions.put(0x81, "STA ${izx}");
        instructions.put(0x84, "STY ${zp}");
        instructions.put(0x85, "STA ${zp}");
        instructions.put(0x86, "STX ${zp}");
        instructions.put(0x88, "DEY");
        instructions.put(0x8A, "TXA");
        instructions.put(0x8C, "STY ${abs}");
        instructions.put(0x8D, "STA ${abs}");
        instructions.put(0x8E, "STX ${abs}");
        instructions.put(0x90, "BCC ${rel}");
        instructions.put(0x91, "STA ${izy}");
        instructions.put(0x94, "STY ${zpx}");
        instructions.put(0x95, "STA ${zpx}");
        instructions.put(0x96, "STX ${zpy}");
        instructions.put(0x98, "TYA");
        instructions.put(0x99, "STA ${aby}");
        instructions.put(0x9A, "TXS");
        instructions.put(0x9D, "STA ${abx}");
        instructions.put(0xA0, "LDY ${imm}");
        instructions.put(0xA1, "LDA ${izx}");
        instructions.put(0xA2, "LDX ${imm}");
        instructions.put(0xA4, "LDY ${zp}");
        instructions.put(0xA5, "LDA ${zp}");
        instructions.put(0xA6, "LDX ${zp}");
        instructions.put(0xA8, "TAY");
        instructions.put(0xA9, "LDA ${imm}");
        instructions.put(0xAA, "TAX");
        instructions.put(0xAC, "LDY ${abs}");
        instructions.put(0xAD, "LDA ${abs}");
        instructions.put(0xAE, "LDX ${abs}");
        instructions.put(0xB0, "BCS ${rel}");
        instructions.put(0xB1, "LDA ${izy}");
        instructions.put(0xB4, "LDY ${zpx}");
        instructions.put(0xB5, "LDA ${zpx}");
        instructions.put(0xB6, "LDX ${zpy}");
        instructions.put(0xB8, "CLV");
        instructions.put(0xB9, "LDA ${aby}");
        instructions.put(0xBA, "TSX");
        instructions.put(0xBC, "LDY ${abx}");
        instructions.put(0xBD, "LDA ${abx}");
        instructions.put(0xBE, "LDX ${aby}");
        instructions.put(0xC0, "CPY ${imm}");
        instructions.put(0xC1, "CMP ${izx}");
        instructions.put(0xC4, "CPY ${zp}");
        instructions.put(0xC5, "CMP ${zp}");
        instructions.put(0xC6, "DEC ${zp}");
        instructions.put(0xC8, "INY");
        instructions.put(0xC9, "CMP ${imm}");
        instructions.put(0xCA, "DEX");
        instructions.put(0xCC, "CPY ${abs}");
        instructions.put(0xCD, "CMP ${abs}");
        instructions.put(0xCE, "DEC ${abs}");
        instructions.put(0xD0, "BNE ${rel}");
        instructions.put(0xD1, "CMP ${izy}");
        instructions.put(0xD5, "CMP ${zpx}");
        instructions.put(0xD6, "DEC ${zpx}");
        instructions.put(0xD8, "CLD");
        instructions.put(0xD9, "CMP ${aby}");
        instructions.put(0xDD, "CMP ${abx}");
        instructions.put(0xDE, "DEC ${abx}");
        instructions.put(0xE0, "CPX ${imm}");
        instructions.put(0xE1, "SBC ${izx}");
        instructions.put(0xE4, "CPX ${zp}");
        instructions.put(0xE5, "SBC ${zp}");
        instructions.put(0xE6, "INC ${zp}");
        instructions.put(0xE8, "INX");
        instructions.put(0xE9, "SBC ${imm}");
        instructions.put(0xEA, "NOP");
        instructions.put(0xEC, "CPX ${abs}");
        instructions.put(0xED, "SBC ${abs}");
        instructions.put(0xEE, "INC ${abs}");
        instructions.put(0xF0, "BEQ ${rel}");
        instructions.put(0xF1, "SBC ${izy}");
        instructions.put(0xF5, "SBC ${zpx}");
        instructions.put(0xF6, "INC ${zpx}");
        instructions.put(0xF8, "SED");
        instructions.put(0xF9, "SBC ${aby}");
        instructions.put(0xFD, "SBC ${abx}");
        instructions.put(0xFE, "INC ${abx}");
        //illegal instructions
        instructions.put(0x04, "*NOP ${zp}");
        instructions.put(0x44, "*NOP ${zp}");
        instructions.put(0x64, "*NOP ${zp}");
        instructions.put(0x0C, "*NOP ${abs}");
        instructions.put(0x14, "*NOP ${zpx}");
        instructions.put(0x34, "*NOP ${zpx}");
        instructions.put(0x54, "*NOP ${zpx}");
        instructions.put(0x74, "*NOP ${zpx}");
        instructions.put(0xD4, "*NOP ${zpx}");
        instructions.put(0xF4, "*NOP ${zpx}");
        instructions.put(0x1A, "*NOP");
        instructions.put(0x3A, "*NOP");
        instructions.put(0x5A, "*NOP");
        instructions.put(0x7A, "*NOP");
        instructions.put(0xDA, "*NOP");
        instructions.put(0xFA, "*NOP");
        instructions.put(0x80, "*NOP ${imm}");
        instructions.put(0x1C, "*NOP ${abx}");
        instructions.put(0x3C, "*NOP ${abx}");
        instructions.put(0x5C, "*NOP ${abx}");
        instructions.put(0x7C, "*NOP ${abx}");
        instructions.put(0xDC, "*NOP ${abx}");
        instructions.put(0xFC, "*NOP ${abx}");
        instructions.put(0xA3, "*LAX ${izx}");
        instructions.put(0xA7, "*LAX ${zp}");
        instructions.put(0xAF, "*LAX ${abs}");
        instructions.put(0xB3, "*LAX ${izy}");
        instructions.put(0xB7, "*LAX ${zpy}");
        instructions.put(0xBF, "*LAX ${aby}");
        instructions.put(0x83, "*SAX ${izx}");
        instructions.put(0x87, "*SAX ${zp}");
        instructions.put(0x8F, "*SAX ${abs}");
        instructions.put(0x97, "*SAX ${zpy}");
        instructions.put(0xEB, "*SBC ${imm}");
        instructions.put(0xC3, "*DCP ${izx}");
        instructions.put(0xC7, "*DCP ${zp}");
        instructions.put(0xCF, "*DCP ${abs}");
        instructions.put(0xD3, "*DCP ${izy}");
        instructions.put(0xD7, "*DCP ${zpx}");
        instructions.put(0xDB, "*DCP ${aby}");
        instructions.put(0xDF, "*DCP ${abx}");
        instructions.put(0xE3, "*ISB ${izx}");
        instructions.put(0xE7, "*ISB ${zp}");
        instructions.put(0xEF, "*ISB ${abs}");
        instructions.put(0xF3, "*ISB ${izy}");
        instructions.put(0xF7, "*ISB ${zpx}");
        instructions.put(0xFB, "*ISB ${aby}");
        instructions.put(0xFF, "*ISB ${abx}");
        instructions.put(0x03, "*SLO ${izx}");
        instructions.put(0x07, "*SLO ${zp}");
        instructions.put(0x0F, "*SLO ${abs}");
        instructions.put(0x13, "*SLO ${izy}");
        instructions.put(0x17, "*SLO ${zpx}");
        instructions.put(0x1B, "*SLO ${aby}");
        instructions.put(0x1F, "*SLO ${abx}");
        instructions.put(0x23, "*RLA ${izx}");
        instructions.put(0x27, "*RLA ${zp}");
        instructions.put(0x2F, "*RLA ${abs}");
        instructions.put(0x33, "*RLA ${izy}");
        instructions.put(0x37, "*RLA ${zpx}");
        instructions.put(0x3B, "*RLA ${aby}");
        instructions.put(0x3F, "*RLA ${abx}");
        instructions.put(0x43, "*SRE ${izx}");
        instructions.put(0x47, "*SRE ${zp}");
        instructions.put(0x4F, "*SRE ${abs}");
        instructions.put(0x53, "*SRE ${izy}");
        instructions.put(0x57, "*SRE ${zpx}");
        instructions.put(0x5B, "*SRE ${aby}");
        instructions.put(0x5F, "*SRE ${abx}");
        instructions.put(0x63, "*RRA ${izx}");
        instructions.put(0x67, "*RRA ${zp}");
        instructions.put(0x6F, "*RRA ${abs}");
        instructions.put(0x73, "*RRA ${izy}");
        instructions.put(0x77, "*RRA ${zpx}");
        instructions.put(0x7B, "*RRA ${aby}");
        instructions.put(0x7F, "*RRA ${abx}");
    }

    private String twoBytesToHexString(byte v1, byte v2) {
        int value = ((v1 & 0xFF) | (v2 & 0xFF) << 8);
        String result = Utils.toHex(value, 2);
        return result;
    }

    public DecompileResult decompile(Cpu cpu, int offset) {
        int opcode = cpu.read(offset, true) & 0xFF;
        String command = instructions.get(opcode);
        if (command == null) {
            throw new IllegalArgumentException("Bad opcode [0x" + Integer.toHexString(opcode) + "] by [" + offset + "] offset");
        }

        int argIndex = command.indexOf('$');
        if (argIndex == -1) {
            return new DecompileResult(1, command);
        }

        int argumentEnd = command.indexOf('}');
        String memoryAddressType = command.substring(argIndex + 2, argumentEnd);
        String opcodeString = command.substring(0, argIndex);
        switch (memoryAddressType) {
            case "imm":
                return new DecompileResult(2, opcodeString + "#$" + Utils.toHex(cpu.read(offset + 1, true)));
            case "imm16":
                return new DecompileResult(3, opcodeString + "$" + twoBytesToHexString(cpu.read(offset + 1, true), cpu.read(offset + 2, true)));
            case "izx": {
                int immediatePointer = cpu.read(offset + 1, true) & 0xFF;
                int resultPointer = immediatePointer + (cpu.X & 0xFF);
                resultPointer = resultPointer % 0x100;
                int resultPointerValue = cpu.read16_d(resultPointer, (resultPointer + 1) % 0x100, true) & 0xFFFF;
                int actualData = cpu.read(resultPointerValue, true) & 0xFF;
                return new DecompileResult(2, opcodeString + "($" + Utils.toHex(immediatePointer, 1) + ",X) @ " + Utils.toHex(resultPointer, 1) + " = " + Utils.toHex(resultPointerValue, 2) + " = " + Utils.toHex(actualData, 1));
            }
            case "izy": {
                int immediatePointer = cpu.read(offset + 1, true) & 0xFF;
                int basePointer = cpu.read16_d(immediatePointer, (immediatePointer + 1) % 0x100, true) & 0xFFFF;

                int resultPointer = (basePointer + (cpu.Y & 0xFF)) & 0xFFFF;

                int actualData = cpu.read(resultPointer, true) & 0xFF;
                return new DecompileResult(2, opcodeString + "($" + Utils.toHex(immediatePointer, 1) + "),Y = " + Utils.toHex(basePointer, 2) + " @ " + Utils.toHex(resultPointer, 2) + " = " + Utils.toHex(actualData, 1));
            }
            case "zp": {
                int address = cpu.read(offset + 1, true) & 0xFF;
                byte value = cpu.read(address, true);
                return new DecompileResult(2, opcodeString + "$" + Utils.toHex(address, 1) + " = " + Utils.toHex(value));
            }
            case "abs": {
                int value = cpu.read16(offset + 1, true);
                int inspectedValue = cpu.read(value, true);
                return new DecompileResult(3, opcodeString + "$" + Utils.toHex(value, 2) + " = " + Utils.toHex(inspectedValue, 1));
            }

            case "zpx": {
                int address = cpu.read(offset + 1, true) & 0xFF;
                int resultAddress = (address + (cpu.X & 0xFF)) & 0xFF;
                byte actualValue = cpu.read(resultAddress, true);
                return new DecompileResult(2, opcodeString + "$" + Utils.toHex(address, 1) + ",X @ " + Utils.toHex(resultAddress, 1) + " = " + Utils.toHex(actualValue, 1));
            }
            case "zpy": {
                int address = cpu.read(offset + 1, true) & 0xFF;
                int resultAddress = (address + (cpu.Y & 0xFF)) & 0xFF;
                byte actualValue = cpu.read(resultAddress, true);
                return new DecompileResult(2, opcodeString + "$" + Utils.toHex(address, 1) + ",Y @ " + Utils.toHex(resultAddress, 1) + " = " + Utils.toHex(actualValue, 1));
            }
            case "abx": {
                int address = cpu.read16(offset + 1, true);
                int resultAddress = (address + (cpu.X & 0xFF)) & 0xFFFF;
                byte actualValue = cpu.read(resultAddress, true);
                return new DecompileResult(3, opcodeString + "$" + Utils.toHex(address, 2) + ",X @ " + Utils.toHex(resultAddress, 2) + " = " + Utils.toHex(actualValue, 1));
            }
            case "aby": {
                int address = cpu.read16(offset + 1, true);
                int resultAddress = (address + (cpu.Y & 0xFF)) & 0xFFFF;
                byte actualValue = cpu.read(resultAddress, true);
                return new DecompileResult(3, opcodeString + "$" + Utils.toHex(address, 2) + ",Y @ " + Utils.toHex(resultAddress, 2) + " = " + Utils.toHex(actualValue, 1));
            }
            case "rel": {
                int newOffset = cpu.read(offset + 1, true);
                int currentPC = cpu.PC;
                int newAddress = currentPC + newOffset + 2;
                return new DecompileResult(2, opcodeString + "$" + Utils.toHex(newAddress, 2));
            }
            case "absjump": {
                int address = cpu.read16(offset + 1, true);

                int inspectedValue = cpu.read16_d(address, (address & 0xFF00) | ((address + 1) % 0x100), true);
                return new DecompileResult(3, opcodeString + "($" + Utils.toHex(address, 2) + ") = " + Utils.toHex(inspectedValue, 2));
            }
            case "rel16":
                return new DecompileResult(3, opcodeString + "$" + twoBytesToHexString(cpu.read(offset + 1, true), cpu.read(offset + 2, true)));
            default:
                throw new IllegalStateException("Unknown memory address type [" + memoryAddressType + "]");
        }
    }

    public static class DecompileResult {

        public DecompileResult(int bytesCount, String decompiledString) {
            this.bytesCount = bytesCount;
            this.decompiledString = decompiledString;
        }

        public int bytesCount;
        public String decompiledString;

        @Override
        public String toString() {
            return decompiledString;
        }

    }

}
