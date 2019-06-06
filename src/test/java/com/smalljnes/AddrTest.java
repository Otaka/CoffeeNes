package com.smalljnes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Dmitry
 */
public class AddrTest {

    public AddrTest() {
    }

    @Test
    public void testAddr() {
        Addr addr = new Addr();
        addr.setCX(25);
        addr.setCY(20);
        addr.setNt(3);
        addr.setFY(6);

        assertEquals(25, addr.getCX());
        assertEquals(20, addr.getCY());
        assertEquals(3, addr.getNT());
        assertEquals(6, addr.getFY());

        addr.setR(0);
        assertEquals(0, addr.getCX());
        assertEquals(0, addr.getCY());
        assertEquals(0, addr.getNT());
        assertEquals(0, addr.getFY());
        
        addr.setH(112);
        addr.setL(211);
        assertEquals(112, addr.getH());
        assertEquals(211, addr.getL());
    }

}
