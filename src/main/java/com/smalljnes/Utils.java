package com.smalljnes;

/**
 * @author Dmitry
 */
public class Utils {

    public static String toHex(byte value) {
        String hexString = Integer.toHexString(value & 0xFF).toUpperCase();
        int necessaryZeroes = 2 - hexString.length();
        for (int i = 0; i < necessaryZeroes; i++) {
            hexString = "0" + hexString;
        }
        return hexString;
    }

    public static String toHex(short value) {
        String hexString = Integer.toHexString(value & 0xFFFF).toUpperCase();
        if (hexString.length() % 2 == 1) {
            hexString = "0" + hexString;
        }
        return hexString;
    }

    public static String toHex(int shortValue, int bytesCount) {
        int mask = 0;
        switch (bytesCount) {
            case 1:
                mask = 0xFF;
                break;
            case 2:
                mask = 0xFFFF;
                break;
            case 4:
                mask = 0xFFFFFFFF;
                break;
            default:
                throw new IllegalArgumentException("Cannot make toHex with bytesCount = " + bytesCount);
        }

        int value = shortValue & mask;
        String hexString = Integer.toHexString(value).toUpperCase();
        int necessaryZeroes = (bytesCount * 2) - hexString.length();
        for (int i = 0; i < necessaryZeroes; i++) {
            hexString = "0" + hexString;
        }
        return hexString;
    }
}
