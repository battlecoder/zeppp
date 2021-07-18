package com.ezv.zeppp.intelhex;

// ################################################################################################################
// ## HexBuffer                                                                                                  ##
// ##                                                                                                            ##
// ## Abstraction of a Byte buffer that can be converted from/to HEX strings.                                    ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
import java.nio.ByteBuffer;
import java.util.Arrays;

public class HexBuffer {
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    ByteBuffer data;

    public HexBuffer () {
        this.data = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    }

    public HexBuffer (int capacity) {
        this.data = ByteBuffer.allocate(capacity);
    }

    public HexBuffer (byte[] data) {
        this.data = ByteBuffer.wrap(data);
    }

    public static HexBuffer fromString (String hexString) throws IntelHexParsingException {
        int recordLen = (hexString.length()) / 2;
        HexBuffer ret = new HexBuffer(recordLen);

        // Convert the whole data to hex
        for (int b = 0; b < recordLen; b++) {
            ret.setByte(b, (byte) HexFileParseUtils.parseHexString(hexString.substring(b * 2, b * 2 + 2)));
        }
        return ret;
    }

    public int getByte (int offset) {
        return (short) (this.data.get(offset) & 0xff);
    }

    public void setByte (int offset, byte b) {
        this.data.put(offset, b);
    }

    public void setWord (int offset, short w) {
        setByte(offset, (byte) (w & 0xff));
        setByte(offset + 1, (byte) ((w >> 8) & 0xff));
    }

    public int getWordHSBFirst (int offset) {
        return ((getByte(offset) << 8) | getByte(offset + 1));
    }

    public int getWord (int offset) {
        return ((getByte(offset + 1) << 8) | getByte(offset));
    }

    public byte[] getBytes (int offset, int len) {
        return Arrays.copyOfRange(this.data.array(), offset, offset + len);
    }

    public int getBufferSize () {
        return data.capacity();
    }

    public String toString () {
        StringBuilder strBuilder = new StringBuilder();

        for (int i = 0; i < this.data.capacity(); i++) {
            strBuilder.append(HexFileParseUtils.hexByteString((byte) getByte(i)));
        }
        return strBuilder.toString();
    }

    public String toStringAsWords () {
        StringBuilder strBuilder = new StringBuilder();

        for (int i = 0; i < this.data.capacity(); i+= 2) {
            strBuilder.append(HexFileParseUtils.hexWordString((short)getWord(i)));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    public HexBuffer getHexBufferSubsetInLSB (int start, int sizeInWords) {
        int remainingData =  getBufferSize() - start;
        int realSizeInBytes = Math.min(sizeInWords*2, remainingData);

        HexBuffer newBuffer = new HexBuffer(realSizeInBytes);
        for (int i = 0; i < realSizeInBytes; i+=2) newBuffer.setWord(i, (short)getWord(start + i));
        return newBuffer;
    }
    public boolean isMemAreaBlockEmpty (int wordStart, int wordCount, int emptyValue) {
        int max = getBufferSize();
        int start = wordStart*2;
        int end = start + wordCount*2;
        int limit = end < max ? end : max;

        for (int offset = start; offset < limit; offset += 2) {
            if (getWord(offset) != emptyValue) return false;
        }
        return true;
    }
}