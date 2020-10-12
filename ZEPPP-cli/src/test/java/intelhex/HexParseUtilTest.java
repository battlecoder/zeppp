package intelhex;

import com.ezv.zeppp.intelhex.HexFileParseUtils;
import com.ezv.zeppp.intelhex.IntelHexParsingException;
import org.junit.Assert;
import org.junit.Test;

public class HexParseUtilTest {
    @Test
    public void testHexDigitParseOK () throws IntelHexParsingException {
        String testSet = "0123456789abcdefABCDEF";
        int[] testValues = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 10, 11, 12, 13, 14, 15};

        for (int i = 0; i < testSet.length(); i++) {
            char ch = testSet.charAt(i);
            int expectedValue = testValues[i];
            Assert.assertEquals("Parsing single hex digit " + Character.toString(ch), expectedValue, HexFileParseUtils.parseHexDigit(ch));
        }
    }

    @Test
    public void testHexStringParseOK () throws IntelHexParsingException {
        Assert.assertEquals("Hex String", 0xffaacc, HexFileParseUtils.parseHexString("FFAACC"));
    }

    @Test(expected = IntelHexParsingException.class)
    public void testHexDigitParseError () throws IntelHexParsingException {
        HexFileParseUtils.parseHexDigit("X".charAt(0));
    }

    @Test(expected = IntelHexParsingException.class)
    public void testHexStringParseError () throws IntelHexParsingException {
        HexFileParseUtils.parseHexString("0ABCDG");
    }
}
