package com.smalljnes;

import com.smalljnes.mappers.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Dmitry
 */
public class Cartridge {

    private AbstractMapper mapper;
    private Cpu cpu;
    private byte[] romData;
    private String romName;

    public Cartridge() {
    }

    public String getRomName() {
        return romName;
    }

    void cartridgeInserted(Cpu cpu) {
        this.cpu = cpu;
        mapper.cartridgeInserted(cpu);
    }

    public byte[] getRomData() {
        return romData;
    }

    public void write(int address, byte value) {
        mapper.write(address,value);
    }

    public byte read(int address, boolean debugInspect) {
        return mapper.read(address, false);
    }

    public byte chrRead(int address) {
        return mapper.chrRead(address);
    }

    public void chrWrite(int address, byte value) {
        mapper.chrWrite(address, value);
    }

    void signalScanline() {
        mapper.signalScanline();
    }

    private AbstractMapper createMapper(int mapperNumber, byte[] romData) {
        switch (mapperNumber) {
            case 0:
                return new Mapper0(romData);
            case 1:
                return new Mapper1(romData);
            case 2:
                return new Mapper2(romData);
            case 3:
                return new Mapper3(romData);
            case 4:
                return new Mapper4(romData);
            default:
                throw new IllegalArgumentException("Mapper [" + mapperNumber + "] does not supported");
        }
    }

    public void load(byte[] romData, String romName) {
        this.romData = romData;
        this.romName = romName;
        int mapperNumber = (romData[7] & 0xF0) | (romData[6] >> 4);
        mapper = createMapper(mapperNumber, romData);
    }

    public void load(InputStream stream, String romName) throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("Cannot load from null input stream");
        }
        load(readInputStreamFully(stream), romName);
    }

    public void load(File file) throws IOException {
        if (!file.exists()) {
            throw new IllegalArgumentException("Rom [" + file + "] does not exists");
        }
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        InputStream stream=new FileInputStream(file);
        if(file.getName().endsWith("zip")){
            ZipInputStream zipStream=new ZipInputStream(stream);
            ZipEntry entry=zipStream.getNextEntry();
            System.out.println("Open file ["+entry.getName()+"] from zip archive");
            stream=zipStream;
        }
        load(new BufferedInputStream(stream), fileName);
    }

    private byte[] readInputStreamFully(InputStream stream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(100000);
        while (true) {
            int value = stream.read();
            if (value == -1) {
                return outputStream.toByteArray();
            }
            outputStream.write(value);
        }
    }
}
