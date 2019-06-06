package com.smalljnes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Dmitry
 */
public class CpuTest {

    @Test
    public void testNesTester_CPU_only_without_timings() throws IOException {
        List<String> nesTestExpectedResult = readStreamToStringList(CpuTest.class.getResourceAsStream("/com/smalljnes/resources/nestest.log"));
        Mos6502Decompiler decompiler = new Mos6502Decompiler();

        Nes nes = new Nes(false, false,false);
        Cartridge nesTestCartridge = new Cartridge();
        nesTestCartridge.load(CpuTest.class.getResourceAsStream("/com/smalljnes/resources/nestest.nes"), "NesTest");
        nes.insertCartridge(nesTestCartridge);
        nes.powerUp();

        Cpu cpu = nes.getCpu();
        //manually set address to run auto test
        cpu.PC = 0xC000;
        cpu.S = (byte) 0xFD;

        for (int i = 0; i < nesTestExpectedResult.size(); i++) {
            String expectedCpuStatus = nesTestExpectedResult.get(i).substring(0, 73);//remove info about the cycles and ppu
            String actualCpuStatus = getCpuStatusString(cpu, decompiler, false);
            cpu.executeOneInstruction();
            Assert.assertEquals("Not equals results on line #" + (i + 1), expectedCpuStatus, actualCpuStatus);
        }
    }

    @Test
    public void testNesTester_CPU_with_Cpu_timings() throws IOException {
        List<String> nesTestExpectedResult = readStreamToStringList(CpuTest.class.getResourceAsStream("/com/smalljnes/resources/nestest.log"));
        Mos6502Decompiler decompiler = new Mos6502Decompiler();

        Nes nes = new Nes(false, false,false);
        Cartridge nesTestCartridge = new Cartridge();
        nesTestCartridge.load(CpuTest.class.getResourceAsStream("/com/smalljnes/resources/nestest.nes"), "NesTest");
        nes.insertCartridge(nesTestCartridge);
        nes.powerUp();

        Cpu cpu = nes.getCpu();
        //manually set address to run auto test
        cpu.PC = 0xC000;
        cpu.S = (byte) 0xFD;
        cpu.remainingCycles = cpu.TOTAL_CYCLES - 7;
        for (int i = 0; i < nesTestExpectedResult.size(); i++) {
            String expectedCpuStatus = nesTestExpectedResult.get(i).substring(0, 73) + nesTestExpectedResult.get(i).substring(85);//remove info about ppu
            String actualCpuStatus = getCpuStatusString(cpu, decompiler, true);
            cpu.executeOneInstruction();
            Assert.assertEquals("Not equals results on line #" + (i + 1), expectedCpuStatus, actualCpuStatus);
        }
    }

    private String getCpuStatusString(Cpu cpu, Mos6502Decompiler decompiler, boolean includeCpuCycles) {
        Mos6502Decompiler.DecompileResult decompileResult = decompiler.decompile(cpu, cpu.PC);
        boolean illegalInstruction = decompileResult.decompiledString.startsWith("*");
        StringBuilder sb = new StringBuilder();

        //address
        sb.append(Utils.toHex((short) cpu.PC));
        sb.append("  ");

        //bytes of instruction
        int columnStartPosition = sb.length();
        for (int i = 0; i < decompileResult.bytesCount; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(Utils.toHex(cpu.read(cpu.PC + i, true)));
        }

        alignToWidth(sb, columnStartPosition, illegalInstruction ? 9 : 10);

        //instruction mnemonic
        columnStartPosition = sb.length();
        sb.append(decompileResult.decompiledString);
        alignToWidth(sb, columnStartPosition, illegalInstruction ? 33 : 32);

        //registers
        sb.append("A:").append(Utils.toHex(cpu.A)).append(" ");
        sb.append("X:").append(Utils.toHex(cpu.X)).append(" ");
        sb.append("Y:").append(Utils.toHex(cpu.Y)).append(" ");
        sb.append("P:").append(Utils.toHex(cpu.getFlagByteValue())).append(" ");
        sb.append("SP:").append(Utils.toHex(cpu.S));

        if (includeCpuCycles) {
            sb.append(" CYC:").append(cpu.elapsed());
        }

        return sb.toString();
    }

    private List<String> readStreamToStringList(InputStream stream) {
        Scanner scanner = new Scanner(stream);
        List<String> result = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            result.add(line);
        }
        return result;
    }

    private void alignToWidth(StringBuilder sb, int columnStartPosition, int columnWidth) {
        int currentLength = sb.length();

        //make alignment by 10
        for (int i = 0; i < (columnWidth - (currentLength - columnStartPosition)); i++) {
            sb.append(" ");
        }
    }
}
