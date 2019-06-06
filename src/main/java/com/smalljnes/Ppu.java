package com.smalljnes;

import java.util.Arrays;

/**
 * @author Dmitry
 */
public class Ppu {

    private Nes nes;
    private Mirroring mirroring;
    byte ciRam[] = new byte[0x800];           // VRAM for nametables.
    byte cgRam[] = new byte[0x20];            // VRAM for palettes.
    byte oamMem[] = new byte[0x100];          // VRAM for sprite properties.
    Sprite oam[] = new Sprite[8];
    Sprite secOam[] = new Sprite[8];          // Sprite buffers.
    int pixels[] = new int[256 * 240];        // Video buffer.
    int fX;
    int oamAddr;
    int nt, at, bgL, bgH;
    int atShiftL, atShiftH;
    int bgShiftL, bgShiftH;
    int atLatchL, atLatchH;

    // Rendering counters:
    int scanline, dot;
    boolean frameOdd;

    int ctrlNt;  // Nametable ($2000 / $2400 / $2800 / $2C00).
    int ctrlIncrement;  // Address increment (1 / 32).
    int ctrlSpriteTable;  // Sprite pattern table ($0000 / $1000).
    int ctrlBackgroundTable;  // BG pattern table ($0000 / $1000).
    int ctrlSpriteSize;  // Sprite size (8x8 / 8x16).
    int ctrlSlave;  // PPU master/slave.
    int ctrlNmi;
    int ctrlRegister;

    boolean maskGray;  // Grayscale.
    boolean maskBackgroundLeft;  // Show background in leftmost 8 pixels.
    boolean maskSpriteLeft;  // Show sprite in leftmost 8 pixels.
    boolean maskShowBackground;  // Show background.
    boolean maskShowSprites;  // Show sprites.
    boolean maskRed;  // Intensify reds.
    boolean maskGreen;  // Intensify greens.
    boolean maskBlue; //Intensify blue
    int maskRegister;

    int statusSpriteOverflow;
    int statusSpriteHit;
    int statusVBlank;
    Addr vAddr = new Addr();
    Addr tAddr = new Addr();
    int frameCounter;

    int nesPalette[] = new int[]{0x7C7C7C, 0x0000FC, 0x0000BC, 0x4428BC, 0x940084, 0xA80020, 0xA81000, 0x881400,
        0x503000, 0x007800, 0x006800, 0x005800, 0x004058, 0x000000, 0x000000, 0x000000,
        0xBCBCBC, 0x0078F8, 0x0058F8, 0x6844FC, 0xD800CC, 0xE40058, 0xF83800, 0xE45C10,
        0xAC7C00, 0x00B800, 0x00A800, 0x00A844, 0x008888, 0x000000, 0x000000, 0x000000,
        0xF8F8F8, 0x3CBCFC, 0x6888FC, 0x9878F8, 0xF878F8, 0xF85898, 0xF87858, 0xFCA044,
        0xF8B800, 0xB8F818, 0x58D854, 0x58F898, 0x00E8D8, 0x787878, 0x000000, 0x000000,
        0xFCFCFC, 0xA4E4FC, 0xB8B8F8, 0xD8B8F8, 0xF8B8F8, 0xF8A4C0, 0xF0D0B0, 0xFCE0A8,
        0xF8D878, 0xD8F878, 0xB8F8B8, 0xB8F8D8, 0x00FCFC, 0xF8D8F8, 0x000000, 0x000000
    };

    public Ppu(Nes nes) {
        this.nes = nes;
        for (int i = 0; i < 8; i++) {
            oam[i] = new Sprite();
            secOam[i] = new Sprite();
        }
    }

    private boolean rendering() {
        return maskShowBackground || maskShowSprites;
    }

    private int getSpriteHeight() {
        return ctrlSpriteSize == 1 ? 16 : 8;
    }

    int ntAddress() {
        return 0x2000 | (vAddr.r & 0xFFF);
    }

    int atAddress() {
        return 0x23C0 | (vAddr.getNT() << 10) | ((vAddr.getCY() / 4) << 3) | (vAddr.getCX() / 4);
    }

    int bgAddress() {
        return (ctrlBackgroundTable * 0x1000) + (nt * 16) + vAddr.getFY();
    }

    void vUpdate() {
        if (!rendering()) {
            return;
        }
        vAddr.r = (vAddr.r & ~0x7BE0) | (tAddr.r & 0x7BE0);
    }

    void hUpdate() {
        if (!rendering()) {
            return;
        }
        vAddr.r = (vAddr.r & ~0x041F) | (tAddr.r & 0x041F);
    }

    void hScroll() {
        if (!rendering()) {
            return;
        }

        if (vAddr.getCX() == 31) {
            vAddr.r ^= 0x41F;
        } else {
            vAddr.setCX((vAddr.getCX() & 0xFF)+1);
        }
    }

    void vScroll() {
        if (!rendering()) {
            return;
        }
        if (vAddr.getFY() < 7) {
            vAddr.setFY(vAddr.getFY() + 1);;
        } else {
            vAddr.setFY(0);
            switch (vAddr.getCY()) {
                case 31:
                    vAddr.setCY(0);
                    break;
                case 29:
                    vAddr.setCY(0);
                    vAddr.setNt(vAddr.getNT() ^ 0b10);
                    break;
                default:
                    vAddr.setCY(vAddr.getCY() + 1);
                    break;
            }
        }
    }

    int ntMirror(int addr) {
        switch (mirroring) {
            case VERTICAL:
                return addr % 0x800;
            case HORIZONTAL:
                return ((addr / 2) & 0x400) + (addr % 0x400);
            default:
                return addr - 0x2000;
        }
    }

    void reloadShift() {
        bgShiftL = (bgShiftL & 0xFF00) | bgL;
        bgShiftH = (bgShiftH & 0xFF00) | bgH;

        atLatchL = (at & 1);
        atLatchH = (at & 0x2)==0?0:1;

      /*  print("RLD_SHFT bgShiftL:" + bgShiftL
                + " atLatchL:" + atLatchL
                + " atLatchH:" + atLatchH);*/
    }

    void clearOam() {
      //  print("PPU:CLR_OAM");
        for (int i = 0; i < 8; i++) {
            secOam[i].id = 64;
            secOam[i].y = 0xFF;
            secOam[i].tile = 0xFF;
            secOam[i].attr = 0xFF;
            secOam[i].x = 0xFF;
            secOam[i].dataL = 0;
            secOam[i].dataH = 0;
        }
    }

    public void setMirrorMode(Mirroring mirrorMode) {
        this.mirroring = mirrorMode;
    }

    public void setCtrlRegister(int ctrlRegister) {
        ctrlNt = ctrlRegister & 0b11;
        ctrlIncrement = (ctrlRegister >> 2) & 0x1;
        ctrlSpriteTable = (ctrlRegister >> 3) & 0x1;
        ctrlBackgroundTable = (ctrlRegister >> 4) & 0x1;
        ctrlSpriteSize = (ctrlRegister >> 5) & 0x1;
        ctrlSlave = (ctrlRegister >> 6) & 0x1;
        ctrlNmi = (ctrlRegister >> 7) & 0x1;
        this.ctrlRegister = ctrlRegister;
    }

    public void setMaskRegister(int maskRegister) {
        maskGray = ((maskRegister >> 0) & 0b1) == 1;
        maskBackgroundLeft = ((maskRegister >> 1) & 0x1) == 1;
        maskSpriteLeft = ((maskRegister >> 2) & 0x1) == 1;
        maskShowBackground = ((maskRegister >> 3) & 0x1) == 1;
        maskShowSprites = ((maskRegister >> 4) & 0x1) == 1;
        maskRed = ((maskRegister >> 5) & 0x1) == 1;
        maskGreen = ((maskRegister >> 6) & 0x1) == 1;
        maskBlue = ((maskRegister >> 7) & 0x1) == 1;
        this.maskRegister = maskRegister;
    }

    void clearStatus() {
        statusSpriteHit = 0;
        statusSpriteOverflow = 0;
        statusVBlank = 0;
    }

    public void reset() {
        frameCounter = 0;
        frameOdd = false;
        scanline = dot = 0;
        setCtrlRegister(0);
        setMaskRegister(0);
        clearStatus();
        Arrays.fill(pixels, 0);
        Arrays.fill(ciRam, (byte) 0x00);
        Arrays.fill(oamMem, (byte) 0x00);
    }

    int rd(int address) {
        if (address <= 0x1FFF) {
            int val = nes.getCartridge().chrRead(address) & 0xFF;
         //   print("PPU_READ_CHR:" + val);
            return val;
        } else if (address < 0x3EFF) {
            int val = ciRam[ntMirror(address)] & 0xFF;//Nametable
        //    print("PPU_READ_NT:" + val);
            return val;
        } else if (address <= 0x3FFF) {
            int newAddress=address;
            if ((newAddress & 0x13) == 0x10) {
                newAddress &= ~0x10;
            }
            int val = cgRam[newAddress & 0x1F] & (maskGray ? 0x30 : 0xFF);
        //    print("PPU_READ_PLT:" + newAddress + ":" + val);
            return val;
        }

        throw new IllegalArgumentException("Unimplemented read address [" + Utils.toHex(address, 2) + "]");
    }

    void wr(int addr, byte v) {
        int value=v&0xFF;
        if (addr <= 0x1FFF) {
        //    print("PPU_WRT_CHR:" + addr + ":" + value);
            nes.getCartridge().chrWrite(addr, (byte) value);
        } else if (addr <= 0x3EFF) {
        //    print("PPU_WRT_NT:" + addr + ":" + value);
            ciRam[ntMirror(addr)] = (byte) value;
        } else if (addr <= 0x3FFF) {
       //     print("PPU_WRT_PLT:" + addr + ":" + value);
            if ((addr & 0x13) == 0x10) {
                addr &= ~0x10;
            }
            cgRam[addr & 0x1F] = (byte) value;
        }
    }

    private void evalSprites() {
        int n = 0;
        for (int i = 0; i < 64; i++) {
            int line = ((scanline == 261) ? -1 : scanline) - (oamMem[i * 4 + 0] & 0xFF);
        //    print("PPU:EV_SPR:" + i + ":line=" + line);
            // If the sprite is in the scanline, copy its properties into secondary OAM:
            if (line >= 0 && line < getSpriteHeight()) {
         //       print("PPU:EV_SPR_added:" + i);
                secOam[n].id = i;
                secOam[n].y = oamMem[i * 4 + 0]&0xFF;
                secOam[n].tile = oamMem[i * 4 + 1]&0xFF;
                secOam[n].attr = oamMem[i * 4 + 2]&0xFF;
                secOam[n].x = oamMem[i * 4 + 3]&0xFF;
                n++;
                if (n >= 8) {
                    statusSpriteOverflow = 1;
                    break;
                }
            }
        }
    }

    private void loadSprites() {
        int addr = 0;
        for (int i = 0; i < 8; i++) {
            oam[i].set(secOam[i]);  // Copy secondary OAM into primary.

            // Different address modes depending on the sprite height:
            if (getSpriteHeight() == 16) {
                addr = ((oam[i].tile & 1) * 0x1000) + ((oam[i].tile & ~1) * 16);
            } else {
                addr = (ctrlSpriteTable * 0x1000) + (oam[i].tile * 16);
            }

            int sprY = (scanline - oam[i].y) % getSpriteHeight();  // Line inside the sprite.
            if ((oam[i].attr & 0x80) != 0) {
                sprY ^= getSpriteHeight() - 1;
            }      // Vertical flip.

            addr += sprY + (sprY & 8);  // Select the second tile if on 8x16.
            oam[i].dataL = rd(addr + 0) & 0xFF;
            oam[i].dataH = rd(addr + 8) & 0xFF;
        //    print("PPU_LD_SPR:" + i + " dataL:" + oam[i].dataL + " dataH:" + oam[i].dataH + " sprY:" + sprY + " addr" + addr);
        }
    }

    int getBit(int value, int bit) {
        return (value >> bit) & 0x1;
    }

    private void pixel() {
        int palette = 0;
        int objPalette = 0;
        boolean objPriority = false;
        int x = dot - 2;

        if (scanline < 240 && x >= 0 && x < 256) {
            if (maskShowBackground && !(!maskBackgroundLeft && x < 8)) {
                // Background:
                palette = (getBit(bgShiftH, 15 - fX) << 1)
                        | getBit(bgShiftL, 15 - fX);
                if (palette != 0) {
                    int b1=getBit(atShiftH, 7 - fX);
                    int b2=getBit(atShiftL, 7 - fX);
                    int atValue=(( b1<< 1) | b2) << 2;
                    //print("PPU:BG_PXL "+palette+" b1:"+b1+ "b2:"+b2+" atV:"+atValue);
                    palette |= atValue;
                }
            }
            // Sprites:
            if (maskShowSprites && !(!maskSpriteLeft && x < 8)) {
                for (int i = 7; i >= 0; i--) {
                    if (oam[i].id == 64) {
                        continue;  // Void entry.
                    }
                    int sprX = ((short) (x - oam[i].x))&0xFFFF;
                    
                    if (sprX >= 8) {
                        continue;            // Not in range.
                    }
                    if ((oam[i].attr & 0x40) != 0) {
                        sprX ^= 7;
                    }  // Horizontal flip.

                    int sprPalette = (getBit(oam[i].dataH, 7 - sprX) << 1)
                            | getBit(oam[i].dataL, 7 - sprX);
                    if (sprPalette == 0) {
                        continue;  // Transparent pixel.
                    }
                    if (oam[i].id == 0 && palette != -1 && x != 255) {
                        statusSpriteHit = 1;
                    }
                    sprPalette |= (oam[i].attr & 3) << 2;
                    //print("PPU:SPR_PAL:" + sprPalette + " i:" + i+" sprX:"+sprX);
                    objPalette = sprPalette + 16;
                    objPriority = (oam[i].attr & 0x20) != 0;
                }
            }
            // Evaluate priority:
            if ((objPalette != 0) && (palette == 0 || objPriority == false)) {
                palette = objPalette;
              //  print("PPU:PXL x:" + x + " y:" + scanline + "SPR");
            }
            int paletteAddress=rd(0x3F00 + (rendering() ? palette : 0));
            pixels[scanline * 256 + x] = nesPalette[paletteAddress];
        }
        // Perform background shifts:
        bgShiftL <<= 1;
        bgShiftH <<= 1;
        atShiftL = ((atShiftL << 1) | atLatchL)&0xFF;
        atShiftH = ((atShiftH << 1) | atLatchH)&0xFF;
    }

    int addr;

    void scanlineCycleVisible(boolean pre) {
        switch (dot) {
            case 1: {
                clearOam();
                if (pre) {
                    statusSpriteOverflow = 0;
                    statusSpriteHit = 0;
                }
                break;
            }
            case 257: {
                evalSprites();
                break;
            }
            case 321: {
                loadSprites();
                break;
            }
        }

        if ((dot >= 2 && dot <= 255) || (dot >= 322 && dot <= 337)) {
            pixel();
            switch (dot % 8) {
                case 1: {
                    addr = ntAddress();
              //      print("PPU D_1: NT_ADDR<=" + addr);
                    reloadShift();
                    break;
                }
                case 2: {
                    nt = rd(addr);
               //     print("PPU D_2: NT<=" + nt);
                    break;
                }
                case 3: {
                    addr = atAddress();
              //      print("PPU D_3: AT_ADDR<=" + addr);
                    break;
                }
                case 4: {
                    at = rd(addr);
                    int oldAt = at;
                    if ((vAddr.getCY() & 2) > 0) {
                        at >>= 4;
                    }
                    if ((vAddr.getCX() & 2) > 0) {
                        at >>= 2;
                    }
              //      print("PPU D_4:AT<=" + addr + " OLD_AT" + oldAt);
                    break;
                }
                case 5: {
                    addr = bgAddress();
            //        print("PPU D_5:BGL_A<=" + addr);
                    break;
                }
                case 6: {
                    bgL = rd(addr);
           //         print("PPU D_6:BGL<=" + bgL);
                    break;
                }
                case 7: {
                    addr += 8;
            //        print("PPU D_7:BGH_A<=" + addr);
                    break;
                }
                case 0: {
                    bgH = rd(addr);
                    hScroll();
            //        print("PPU D_0:BGH<=" + bgH);
                    break;
                }
            }
        } else if (dot == 256) {
            pixel();
            bgH = rd(addr);
            vScroll();
        } else if (dot == 257) {
            pixel();
            reloadShift();
            hUpdate();
        } else if (pre && (dot >= 280 && dot <= 304)) {
            vUpdate();
        } else if (dot == 1) {
            addr = ntAddress();
            if (pre) {
                statusVBlank = 0;
            }
        } else if (dot == 321 || dot == 339) {
            addr = ntAddress();
        } else if (dot == 338 ){
            nt =rd(addr);
        }else if(dot == 340) {
            nt = rd(addr);
            if (pre && rendering() && frameOdd) {
                dot++;
            }
        }

        if (dot == 260 && rendering()) {
            nes.getCartridge().signalScanline();
        }
    }

    void scanlineCyclePost() {
        if (dot == 0) {
          //  print("Frame finished " + frameCounter);
            frameCounter++;
            OnDrawFrame onDrawFrameEvent = nes.getOnDrawFrameEvent();
            if (onDrawFrameEvent != null) {
                onDrawFrameEvent.onDrawFrame(pixels);
            }
        }
    }

    void scanlineNmi() {
        if (dot == 1) {
            statusVBlank = 1;
            if (ctrlNmi == 1) {
                nes.getCpu().setNmi(true);
            }
        }
    }

    void step() {
      //  print("PPU:STEP "+scanline+":"+dot);
        if (scanline <= 239) {
            scanlineCycleVisible(false);
        } else if (scanline == 240) {
            scanlineCyclePost();
        } else if (scanline == 241) {
            scanlineNmi();
        } else if (scanline == 261) {
            scanlineCycleVisible(true);
        }

        // Update dot and scanline counters:
        if (++dot > 340) {
            dot %= 341;
            if (++scanline > 261) {
                scanline = 0;
                frameOdd = !frameOdd;
            }
        }
    }

    boolean latch;

    void writeRegister(int address, byte value) {
        address = address % 8;
        switch (address) {
            case 0:
             //   print("CTRL_SET:" + (value & 0xFF));
                setCtrlRegister(value & 0xFF);
                tAddr.setNt(ctrlNt);
                break;
            case 1:
              //  print("MASK_SET:" + (value & 0xFF));
                setMaskRegister(value & 0xFF);
                break;
            case 3:
                oamAddr = value & 0xFF;
              //  print("OAM_ADDR_SET:" + oamAddr);
                break;
            case 4:
              //  print("OAM_SET:" + oamAddr + ":" + (value & 0xFF));
                oamMem[oamAddr] = value;
                oamAddr++;
                oamAddr = oamAddr & 0xFF;
                break;
            case 5:
                if (!latch) {
                    fX = value & 7;
                    tAddr.setCX((value & 0xFF) >> 3);
                //    print("PPU_SCROLL_X:v:" + (value & 0xFF) + ":fx" + fX + ":cX" + tAddr.getCX());
                } else {
                    tAddr.setFY(value & 7);
                    tAddr.setCY((value & 0xFF) >> 3);
               //     print("PPU_SCROLL_Y:v:" + (value & 0xFF) + ":fY" + fX + ":cX" + ((value & 0xFF) >> 3));
                }
                latch = !latch;
                break;
            case 6:
                if (!latch) {
                    tAddr.setH(value & 0x3F);
               //     print("PPU_ADDR_h:v:" + (value & 0xFF));
                } else {
                    tAddr.setL(value);
                    vAddr.setR(tAddr.r);
               //     print("PPU_ADDR_l:v:" + (value & 0xFF));
                }

                latch = !latch;
                break;
            case 7:
                wr(vAddr.getAddr(), value);
              //  print("PPU_DATA:vaddr:" + vAddr.getAddr() + ":v:" + (value & 0xFF));
                vAddr.setAddr(vAddr.getAddr() + ((ctrlIncrement == 0) ? 1 : 32));
                break;
        }
    }

    int getStatusRegisterValue() {
        return statusVBlank << 7 | statusSpriteHit << 6 | statusSpriteOverflow << 5;
    }

    int res;
    int buffer;

    int readRegister(int address, boolean inspect) {
        address = address % 0x2000;
        switch (address) {
            // PPUSTATUS ($2002):
            case 2:
                res = (res & 0x1F) | getStatusRegisterValue();
                statusVBlank = 0;
                latch = false;
              //  print("PPU_RD_STATUS:" + res);
                break;
            case 4:
                res = oamMem[oamAddr];
              //  print("PPU_RD_OAMDATA:v" + res + ":addr:" + oamAddr);
                break;  // OAMDATA ($2004).
            case 7:                                 // PPUDATA ($2007).
                if (vAddr.getAddr() <= 0x3EFF) {
                    res = buffer;
                    buffer = rd(vAddr.getAddr());
                } else {
                    res = buffer = rd(vAddr.getAddr());
                }

             //   print("PPU_RD_PPUDATA:v" + res + ":addr:" + vAddr.getAddr());
                vAddr.setAddr(vAddr.getAddr() + ((ctrlIncrement == 0) ? 1 : 32));
        }

        return res;
    }

   /* boolean printStatusDisabled;

    void print(String str) {
        if (frameCounter < 0) {
            nes.getCpu().printStatus("X:" + dot + " scnl:" + scanline + " vadr.x:" + vAddr.r + " " + str);
        } else if (printStatusDisabled == false) {
            nes.getCpu().disableDebug();
            printStatusDisabled = true;
        }
    }*/
}
