package intelhex;

import com.ezv.zeppp.intelhex.HexBuffer;
import org.junit.Assert;
import org.junit.Test;

public class HexBufferTest {
    @Test
    public void testHexBufferReadAndWrite () {
        HexBuffer buffer = new HexBuffer();

        buffer.setByte(100, (byte) 0xf0);
        buffer.setWord(101, (short) 0xf0c4);

        Assert.assertEquals("Reading single byte", 0xf0, buffer.getByte(100));
        Assert.assertEquals("Reading MSB of a 16-bit word", 0xf0, buffer.getByte(101));
        Assert.assertEquals("Reading LSB of a 16-bit word", 0xc4, buffer.getByte(102));
        Assert.assertEquals("Reading complete 16-bit word", 0xf0c4, buffer.getWord(101));
    }

    @Test(expected = java.lang.IndexOutOfBoundsException.class)
    public void testHexBufferOutOfBounds () {
        HexBuffer buffer = new HexBuffer(100);
        Assert.assertEquals("Reading buffer capacity", 100, buffer.getBufferSize());
        buffer.setByte(101, (byte) 0xa5);
    }

    @Test
    public void testHexBufferByteArray () {
        HexBuffer buffer = new HexBuffer(256);

        for (int i = 0; i < 256; i++) buffer.setByte(i, (byte) i);

        int subsetLen   = 100;
        int subsetStart = 80;
        byte[] subset = buffer.getBytes(subsetStart, subsetLen);
        Assert.assertEquals("Buffer subset size", subsetLen, subset.length);

        for (int i = 0; i < subsetLen; i++) {
            Assert.assertEquals("Buffer subset content for position " + i, (byte) (i + subsetStart), subset[i]);
        }
    }
}
