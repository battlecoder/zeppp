package com.ezv.zeppp.hardware;

// ################################################################################################################
// ## ZEPPPResponse                                                                                              ##
// ##                                                                                                            ##
// ## Abstraction of ZEPPP Responses that makes parsing them easier.                                             ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
import com.ezv.zeppp.intelhex.HexFileParseUtils;
import com.ezv.zeppp.intelhex.IntelHexParsingException;

public class ZEPPPResponse {
    public enum StatusCode{
        STATUS_OK,
        STATUS_ERROR
    }

    private StatusCode code;
    private String message;

    ZEPPPResponse (StatusCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public StatusCode getCode () {
        return code;
    }

    public String getMessage () {
        return message;
    }

    public int[] getMessageWordArray () throws IntelHexParsingException{
        String[] hexWords = message.split(" ");
        int[] array = new int[hexWords.length];

        for (int w = 0; w < hexWords.length; w++) {
            array[w] = HexFileParseUtils.parseHexString(hexWords[w]);
        }
        return array;
    }

    public int getMessageWord () throws IntelHexParsingException{
        return HexFileParseUtils.parseHexString(message);
    }
}
