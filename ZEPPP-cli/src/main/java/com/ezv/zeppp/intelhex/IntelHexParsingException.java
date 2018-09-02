package com.ezv.zeppp.intelhex;

// ################################################################################################################
// ## IntelHexParsingException                                                                                   ##
// ##                                                                                                            ##
// ## Exception subclass for Hex Parsing routines.                                                               ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
public class IntelHexParsingException extends Exception {
    public IntelHexParsingException (String msg) {
        super(msg);
    }
}
