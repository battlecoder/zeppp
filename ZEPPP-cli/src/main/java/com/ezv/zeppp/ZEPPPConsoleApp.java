package com.ezv.zeppp;

// ################################################################################################################
// ## ZEPPPConsoleApp                                                                                            ##
// ##                                                                                                            ##
// ## Main class of the ZEPPP Command Line Interface.                                                            ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
class ZEPPPConsoleApp {
    public static void main (String[] args) {
        ZEPPPConsole.parseCommandLine(args);
    }
}