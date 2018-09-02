package com.ezv.zeppp.intelhex;

// ################################################################################################################
// ## HexFileParseUtils                                                                                          ##
// ##                                                                                                            ##
// ## Utility class for parsing Hex Strings.                                                                     ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
public class HexFileParseUtils {
    private static final String HEX_SET = "0123456789abcdef";
    private static final String BINARY_SET = "01";

    private HexFileParseUtils () {
    }

    public static int parseHexString (String hexStr) throws IntelHexParsingException {
        int value = 0;
        int multiplier = 1;

        for (int c = hexStr.length() - 1; c >= 0; c--) {
            byte digit = parseHexDigit(hexStr.charAt(c));
            value += digit * multiplier;
            multiplier *= 16;
        }
        return value;
    }

    public static byte parseHexDigit (char hc) throws IntelHexParsingException {
        char digit = Character.toLowerCase(hc);
        int index = HEX_SET.indexOf(digit);
        if (index < 0 || index > 15) {
            throw new IntelHexParsingException(String.format("%c is not a valid Hex digit!", hc));
        }
        return (byte) index;
    }

    public static String hexByteString (byte b) {
        return String.format("%02X", b);
    }

    public static String hexWordString (int w) {
        return String.format("%02X%02X", (w >> 8) & 0xff, (w & 0xff));
    }
}
