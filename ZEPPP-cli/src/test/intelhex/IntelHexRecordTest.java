package intelhex;

import com.ezv.zeppp.intelhex.HexBuffer;
import com.ezv.zeppp.intelhex.IntelHexParsingException;
import com.ezv.zeppp.intelhex.IntelHexRecord;
import org.junit.Assert;
import org.junit.Test;

public class IntelHexRecordTest {
    @Test
    public void testHexRecordParser () throws IntelHexParsingException {
        String hexRecordType4 = ":020001040000F9";
        IntelHexRecord r = IntelHexRecord.fromString(hexRecordType4);

        Assert.assertTrue("Address should be 1", r.getAddress() == 1);
        Assert.assertTrue("IntelHexRecord type should be 4", r.getType() == 0x04);
        Assert.assertTrue("IntelHexRecord size should be 2", r.getSize() == 0x02);
        Assert.assertTrue("Checksum should be F9", r.getChecksum() == (byte) 0xf9);
    }

    @Test
    public void testRecordStringConversion () throws IntelHexParsingException {
        String hexRecordType4 = ":020001040000F9";
        IntelHexRecord r = IntelHexRecord.fromString(hexRecordType4);

        Assert.assertEquals("String version of both buffers should be equal", hexRecordType4, r.toString());
    }

    @Test
    public void testHexRecordBufferConstructor () throws IntelHexParsingException {
        HexBuffer buffer = HexBuffer.fromString("0102");
        IntelHexRecord r = new IntelHexRecord((byte) 0x04, 8, buffer, 0, (byte) 2);

        Assert.assertTrue("Address should be 08", r.getAddress() == 8);
        Assert.assertTrue("IntelHexRecord type should be 4", r.getType() == 0x04);
        Assert.assertTrue("IntelHexRecord size should be 2", r.getSize() == 0x02);
        Assert.assertTrue("Checksum should be EF", r.getChecksum() == (byte) 0xef);
    }

    @Test
    public void testHexRecordParserDataSizeMismatch () {
        String hexRecordType4 = ":020000040000888888FA";

        try {
            IntelHexRecord r = IntelHexRecord.fromString(hexRecordType4);
        } catch (IntelHexParsingException ihpex) {
            Assert.assertTrue(ihpex.getMessage().contains("IntelHexRecord size mismatch"));
        }
    }

    @Test
    public void testHexRecordParserChecksumMismatch () {
        String hexRecordType4 = ":020000040000FD";

        try {
            IntelHexRecord r = IntelHexRecord.fromString(hexRecordType4);
        } catch (IntelHexParsingException ihpex) {
            Assert.assertTrue(ihpex.getMessage().contains("Checksum mismatch"));
        }
    }
}
