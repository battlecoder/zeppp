package com.ezv.zeppp;

// ################################################################################################################
// ## ZEPPPCLICommand                                                                                            ##
// ##                                                                                                            ##
// ## Abstraction of ZEPPP CLI Commands.                                                                         ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
public class ZEPPPCLICommand {
    public enum CLICommandCode{
        CLI_COMMAND_COMM,
        CLI_COMMAND_IGNORE_OOB_ERROR,
        CLI_COMMAND_INPUT,
        CLI_COMMAND_OUTPUT,
        CLI_COMMAND_DEVICE,
        CLI_COMMAND_READ_PGM_MEM,
        CLI_COMMAND_READ_CONF_WORDS,
        CLI_COMMAND_READ_USER_IDS,
        CLI_COMMAND_READ_EEPROM,
        CLI_COMMAND_READ_ALL,
        CLI_COMMAND_WRITE_EEPROM,
        CLI_COMMAND_WRITE_PGM_MEM,
        CLI_COMMAND_WRITE_USER_IDS,
        CLI_COMMAND_WRITE_CONF_WORDS,
        CLI_COMMAND_WRITE_ALL,
        CLI_COMMAND_VERIFY_ALL,
        CLI_COMMAND_VERIFY_EEPROM,
        CLI_COMMAND_VERIFY_CONF_WORDS,
        CLI_OOMMAND_VERIFY_USER_IDS,
        CLI_COMMAND_VERIFY_PGM_MEM,
        CLI_COMMAND_ERASE_PGM_MEM,
        CLI_COMMAND_ERASE_EEPROM,
        CLI_COMMAND_CHIP_ERASE,
        CLI_COMMAND_PROGRAM,
        CLI_COMMAND_WAIT,
        CLI_COMMAND_HELP,
        CLI_COMMAND_VERSION
    }

    private String shortName;
    private String longName;
    private String description;
    private String params;
    private CLICommandCode code;

    public ZEPPPCLICommand (CLICommandCode code, String shortName, String longName, String params, String description) {
        this.code = code;
        this.shortName = shortName;
        this.longName = longName;
        this.params = params;
        this.description = description;
    }

    public CLICommandCode getCode () {
        return code;
    }

    public String getShortName () {
        return shortName;
    }

    public String getLongName () {
        return longName;
    }

    public String getDescription () {
        return description;
    }

    public String getParams () {
        return params;
    }
}
