package com.ezv.zeppp;

// ################################################################################################################
// ## ZEPPPCommandException                                                                                      ##
// ##                                                                                                            ##
// ## Exception subclass for errors during ZEPPP Commands.                                                       ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
public class ZEPPPCommandException extends Exception {
    public ZEPPPCommandException (String msg, String action) {
        super ("Failed to " + action + ". " + msg);
    }
}
