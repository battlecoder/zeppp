package com.ezv.zeppp.intelhex;

// ################################################################################################################
// ## IntelHexRecord                                                                                             ##
// ##                                                                                                            ##
// ## Reasonably complete implementation of an Intel Hex file record.                                            ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
public class IntelHexRecord {
    private byte   size;
    private byte   type;
    private int    address;
    private byte   checksum;
    private byte[] data;

    public static final int INTELHEX_RECORDTYPE_DATA          = 0;
    public static final int INTELHEX_RECORDTYPE_ENDOFFILE     = 1;
    public static final int INTELHEX_RECORDTYPE_EXTENDEDADDR  = 4;

    // All constants expressed in bytes
    private static final int INTELHEX_RECORDFIED_SIZE_OFFS     = 0;
    private static final int INTELHEX_RECORDFIELD_ADDRESS_OFFS = 1;
    private static final int INTELHEX_RECORDFIELD_TYPE_OFFS    = 3;
    private static final int INTELHEX_RECORDFIELD_DATA_OFFS    = 4;

    public IntelHexRecord (int address, byte size, byte type, byte[] data, byte checksum) {
        this.address  = address;
        this.size     = size;
        this.type     = type;
        this.data     = data.clone();
        this.checksum = checksum;
    }

    public IntelHexRecord (byte type, int address, HexBuffer buffer, int from, byte len) {
        this.address  = address;
        this.size     = len;
        this.type     = type;

        if (buffer != null) this.data     = buffer.getBytes(from, len);
        int headerSum = (short)this.size + (short)this.type + (short)(this.address & 0xff)  + (short)(this.address >> 8);
        this.checksum = computeChecksumWithStartingValue(buffer, from, len, headerSum);
    }

    public static IntelHexRecord createEndOfFileRecord () {
        return new IntelHexRecord((byte) INTELHEX_RECORDTYPE_ENDOFFILE, 0, null, 0, (byte) 0);
    }

    public static IntelHexRecord createExtendedAddressRecord (int addressH) throws IntelHexParsingException{
        HexBuffer buffer = HexBuffer.fromString(HexFileParseUtils.hexWordString(addressH));
        return new IntelHexRecord((byte) INTELHEX_RECORDTYPE_EXTENDEDADDR, 0, buffer, 0, (byte) 2);
    }

    public static IntelHexRecord fromString(String line) throws IntelHexParsingException{
        if (!line.startsWith(":")) throw new IntelHexParsingException ("IntelHexRecord definition expected to start with :");

        int recordLen = (line.length()-1) / 2;
        HexBuffer recBytes = HexBuffer.fromString(line.substring(1));

        int  recAddress     = recBytes.getWordHSBFirst(INTELHEX_RECORDFIELD_ADDRESS_OFFS);
        byte recType        = (byte) recBytes.getByte(INTELHEX_RECORDFIELD_TYPE_OFFS);
        byte dataSize       = (byte) recBytes.getByte(INTELHEX_RECORDFIED_SIZE_OFFS);
        int  offsetFieldPos = INTELHEX_RECORDFIELD_DATA_OFFS + (int)dataSize;
        int  expectedSize   = offsetFieldPos + 1;

        if (recordLen !=  expectedSize) {
            throw new IntelHexParsingException(
                    String.format("IntelHexRecord size mismatch. Complete record should be %d bytes in size", expectedSize)
            );
        }

        if (recType == INTELHEX_RECORDTYPE_EXTENDEDADDR && dataSize !=  2) {
            throw new IntelHexParsingException("Size of data expected to be 2 for 'Extended Linear Address' records.");
        }

        byte checksum = (byte) recBytes.getByte(offsetFieldPos);
        byte []data   = recBytes.getBytes(INTELHEX_RECORDFIELD_DATA_OFFS, dataSize);

        byte computedChecksum = computeChecksum (recBytes,0, recordLen-1);
        if (checksum  != computedChecksum) {
            throw new IntelHexParsingException(
                    String.format("Checksum mismatch. Computed: %02x, Declared: %02x", computedChecksum, checksum)
            );
        }
        return new IntelHexRecord(recAddress, dataSize, recType, data, checksum);
    }

    public String toString() {
        HexBuffer dataBuffer = (this.data != null ? new HexBuffer(this.data) : null);

        return ":" + HexFileParseUtils.hexByteString(this.size) +  HexFileParseUtils.hexWordString(this.address) +
                     HexFileParseUtils.hexByteString(this.type) +  (dataBuffer != null ? dataBuffer.toString() : "") +
                     HexFileParseUtils.hexByteString(this.checksum);
    }

    private static byte computeChecksum (HexBuffer data, int startPos, int len) {
        return computeChecksumWithStartingValue(data, startPos, len, 0);
    }

    private static byte computeChecksumWithStartingValue (HexBuffer data, int startPos, int len, int startVal) {
        int sum = startVal;

        if (data == null) return (byte)0xff;
        for (int b = startPos; b < len; b++) {
            sum += (short) data.getByte(b);
        }
        return (byte)(-sum);
    }

    public byte getSize() {
        return size;
    }

    public byte getType() {
        return type;
    }

    public int getAddress() {
        return address;
    }

    public byte getChecksum() {
        return checksum;
    }

    public byte getByteAt (int offset) {
        return data[offset];
    }

    public int getWordAt (int offset) {
        return (short)(data[offset+1] << 8) | (short) data[offset];
    }

    public byte[] getData() {
        return data;
    }
}
