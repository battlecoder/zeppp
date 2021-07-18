package com.ezv.zeppp;

// ################################################################################################################
// ## ZEPPPConsole                                                                                               ##
// ##                                                                                                            ##
// ## Hub for the command-line parsing functions and console I/O.                                                ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
import com.ezv.zeppp.config.AppConfig;
import com.ezv.zeppp.config.PICDeviceConfigEntry;
import com.ezv.zeppp.intelhex.IntelHexParsingException;
import com.ezv.zeppp.pic.PicDevice;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;

import static com.ezv.zeppp.ZEPPPCLICommand.CLICommandCode.*;

public class ZEPPPConsole {
    public static final String ZEPPP_CLI_APP_NAME = "zeppp-cli";
    public static final String ZEPPP_CLI_VERSION = "1.0.3";

    private static AppConfig programConfig = new AppConfig();
    private static ZEPPPClient zepppBridge = null;
    private static PicDevice picDevice = null;
    private static boolean ignoreOutOfBounds = false;
    private static ArrayList<ZEPPPCLICommand> commandList;
    private static boolean initialized = false;

    private ZEPPPConsole () {
    }

    private static void init(){
        if (initialized) return;

        commandList = new ArrayList<>();

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_VERSION, "v", "version", null,
                "Shows current CLI version and supported PIC devices."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_COMM, "c", "comm-port", "<COMM PORT>",
                "Selects the COMM port where the interface is, and attempts to establish connection."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_DEVICE, "d", "device", "<pic device>",
                "Selects the PIC device. Must be set before most other operations. If not specified\n\t"+
                           "the interface will attempt to auto-detect the connected PIC Device."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_IGNORE_OOB_ERROR,null, "ignore-bounds-error", null,
                "Attempts to write to out of bound areas result in just a warning, and do not stop\n\texecution."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_INPUT,"i", "input", "<filename>",
                 "Reads an Intel HEX file into the PIC memory buffer."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_OUTPUT,"o", "output", "<filename>",
                 "Writes the PIC memory buffer to an Intel HEX file."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_READ_PGM_MEM,"rp", "read-pgm-mem", null,
                "Reads PROGRAM Memory from the connected PIC device into the PIC memory buffer."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_READ_CONF_WORDS,"rc", "read-conf-words", null,
                "Reads CONFIG Words from the connected PIC device into the PIC memory buffer."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_READ_USER_IDS,"ri", "read-user-ids", null,
                "Reads User-defined IDs from the connected PIC device into the PIC memory buffer."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_READ_EEPROM,"re", "read-eeprom", null,
                "Reads EEPROM Data from the connected PIC device into the PIC memory buffer."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_READ_ALL,"ra", "read-all", null,
                "Reads all memory areas from the connected PIC device into the PIC memory buffer."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_WRITE_PGM_MEM,"wp", "write-pgm-mem", null,
                "Writes PROGRAM Memory from the PIC memory buffer to the connected PIC device."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_WRITE_CONF_WORDS,"wc", "write-conf-words", null,
                "Writes CONFIG Words from the PIC memory buffer to the connected PIC device."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_WRITE_USER_IDS,"wi", "write-user-ids", null,
                "Writes User-defined IDs from the PIC memory buffer to the connected PIC device."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_WRITE_EEPROM,"we", "write-eeprom", null,
                "Writes EEPROM Data from the PIC memory buffer to the connected PIC device."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_WRITE_ALL,"wa", "write-all", null,
                "Writes all memory areas from the PIC memory buffer to the connected PIC device."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_VERIFY_PGM_MEM, "vp", "verify-pgm-mem", null,
                "Reads PROGRAM Memory from connected PIC device, and checks if it matches the PIC memory buffer."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_VERIFY_CONF_WORDS, "vc", "verify-conf-words", null,
                "Reads CONFIG Words from connected PIC device, and checks if it matches the PIC memory buffer."));

        commandList.add(new ZEPPPCLICommand(CLI_OOMMAND_VERIFY_USER_IDS,"vi", "verify-user-ids", null,
                "Reads User-defined IDs from connected PIC device, and checks if it matches the PIC memory buffer."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_VERIFY_EEPROM, "ve", "verify-eeprom", null,
                "Reads EEPROM Data from connected PIC device, and checks if it matches the PIC memory buffer."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_VERIFY_ALL, "va", "verify-all", null,
                "Reads all memory areas from the connected PIC device, and checks if they match the PIC memory buffer."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_ERASE_PGM_MEM, "ep", "erase-pgm-mem", null,
                "Erases PROGRAM Memory on the connected PIC device. May also wipe CONF words on some devices."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_ERASE_EEPROM, "ee", "erase-eeprom", null,
                "Erases EEPROM Data on the connected PIC device."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_CHIP_ERASE, "ce", "chip-erase", null,
                "Performs a chip erase on the connected PIC device. This clears all memory areas including User-defined\n\t" +
                           "IDs and CONFIG words, removing code protection if enabled."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_PROGRAM, "p", "program", null,
                "Shorthand for a 'chip erase' followed by the full set of write/verification operations"));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_WAIT, null, "wait", "<milliseconds>",
                "Waits a given amount of time before continuing. Useful when you need to wait before the next step.\n\t"+
                           "Several Arduinos for instance reset themselves when a serial connection is established, and the\n\t"+
                           "interface won't listen to commands for the 2 seconds that follow. If you want to use those boards\n\t" +
                           "for this project, you'll need to add a 2000ms delay after the port has been opened, and before\n\t"+
                           "any operation on the PIC device or the interface is attempted."));

        commandList.add(new ZEPPPCLICommand(CLI_COMMAND_HELP, "h", "help", "<command>",
                "Shows the description and parameters of a specific command or option."));

        initialized = true;
    }

    // ##############################################################################################################
    // ##                                                                                                          ##
    // ##                          C  O  M  M  A  N  D    L  I  N  E    P  A  R  S  I  N  G                        ##
    // ##                                                                                                          ##
    // ##############################################################################################################
    public static void parseCommandLine (String[] args) {
        int argumentsToSkip = 1;

        init();
        if (args.length < 1) {
            printHelp();
            return;
        }
        try {
            for (int i = 0; i < args.length; i += argumentsToSkip) {
                argumentsToSkip = 1;
                String key;
                String value = "";

                // Obtain a key-value pair, if possible
                if (args[i].startsWith("-")) {
                    key = args[i].substring(1);
                    if (i < args.length - 1 && !args[i + 1].startsWith("-")) {
                        value = args[i + 1];
                        argumentsToSkip = 2;
                    }
                    runCommand (key, value);
                } else {
                    ZEPPPConsole.warning(String.format("Invalid argument '%s'", args[i]));
                }
            }
        } catch (ZEPPPCommandException ace) {
            critical(ace.getMessage());

        } catch (IntelHexParsingException ihpe) {
            critical (ihpe.getMessage());

        } catch (NoSuchFileException nsfe) {
            critical (String.format("'%s' does not exist or could not be read", nsfe.getFile()));

        } catch (Exception e) {
            //e.printStackTrace();
            critical(e.toString());
        }
        closeInterface();
    }

    private static void closeInterface(){
        if (zepppBridge != null) zepppBridge.exitLVPMode();
    }

    private static void runCommand (String key, String value) throws Exception {
        long timeStart = System.currentTimeMillis();
        boolean isTimed = parseCommandReturnTrueIfTimed(key, value);
        if (isTimed) ZEPPPConsole.msg("That took " + (System.currentTimeMillis() - timeStart) + " ms");
    }

    private static ZEPPPCLICommand identifyCommand (String cmdName) {
        String cmdNameLowerCase = cmdName.toLowerCase();
        for (ZEPPPCLICommand cmd : commandList) {
            if (cmdNameLowerCase.equals(cmd.getShortName()) || cmdNameLowerCase.equals(cmd.getLongName())) return cmd;
        }
        return null;
    }

    private static boolean parseCommandReturnTrueIfTimed (String key, String val) throws Exception {
        String trimValue = val.trim();
        ZEPPPCLICommand command;

        command  = identifyCommand(key);
        if (command == null) {
            ZEPPPConsole.error(String.format("Unknown command '%s'", key));
            printHelp();
            return false;
        }

        switch (command.getCode()) {
            case CLI_COMMAND_COMM:
                zepppBridge = new ZEPPPClient(trimValue);
                return false;

            case CLI_COMMAND_IGNORE_OOB_ERROR:
                ignoreOutOfBounds = true;
                return false;

            case CLI_COMMAND_INPUT:
                requireInterfaceAndPIC();
                ZEPPPConsole.msg("Reading input Hex file: " + trimValue);
                IntelHexPicHelper.loadFromHexFile(picDevice, trimValue, ignoreOutOfBounds);
                return false;

            case CLI_COMMAND_OUTPUT:
                requireInterfaceAndPIC();
                ZEPPPConsole.msg("Saving buffers to Hex file: " + trimValue);
                IntelHexPicHelper.saveToHexFile(picDevice, trimValue);
                return false;

            case CLI_COMMAND_DEVICE:
                requireZEPPPInterface();
                if (!programConfig.setSelectedDevice(trimValue)) {
                    ZEPPPConsole.critical(String.format("Unsupported device '%s'. Valid devices are: %s", trimValue,
                            programConfig.getSupportedPICDevices().toString()));
                } else {
                    ZEPPPConsole.msg(String.format("Pic device '%s' selected", programConfig.getSelectedDevice().getDeviceName()));
                }
                picDevice = new PicDevice(programConfig.getSelectedDevice());
                zepppBridge.verifyDeviceID(picDevice.getDeviceCfg(), programConfig);
                return false;

            case CLI_COMMAND_VERIFY_ALL:
                requireInterfaceAndPIC();
                zepppBridge.verifyAll(picDevice);
                break;

            case CLI_COMMAND_VERIFY_EEPROM:
                requireInterfaceAndPIC();
                zepppBridge.verifyDataMem(picDevice);
                break;

            case CLI_COMMAND_VERIFY_CONF_WORDS:
                requireInterfaceAndPIC();
                zepppBridge.verifyConfigWords(picDevice);
                break;

            case CLI_OOMMAND_VERIFY_USER_IDS:
                requireInterfaceAndPIC();
                zepppBridge.verifyUserIDs (picDevice);
                break;

            case CLI_COMMAND_VERIFY_PGM_MEM:
                requireInterfaceAndPIC();
                zepppBridge.verifyPgmMem (picDevice);
                break;

            case CLI_COMMAND_WRITE_EEPROM:
                requireInterfaceAndPIC();
                zepppBridge.writeDataMem(picDevice);
                break;

            case CLI_COMMAND_WRITE_PGM_MEM:
                requireInterfaceAndPIC();
                zepppBridge.writePgmMem (picDevice);
                break;

            case CLI_COMMAND_WRITE_USER_IDS:
                requireInterfaceAndPIC();
                zepppBridge.writeUserIDs (picDevice);
                break;

            case CLI_COMMAND_WRITE_CONF_WORDS:
                requireInterfaceAndPIC();
                zepppBridge.writeConfigWords (picDevice);
                break;

            case CLI_COMMAND_WRITE_ALL:
                requireInterfaceAndPIC();
                zepppBridge.writeAll(picDevice);
                break;

            case CLI_COMMAND_READ_PGM_MEM:
                requireInterfaceAndPIC();
                zepppBridge.readPgmMem (picDevice);
                break;

            case CLI_COMMAND_READ_CONF_WORDS:
                requireInterfaceAndPIC();
                zepppBridge.readConfigWords (picDevice);
                break;

            case CLI_COMMAND_READ_USER_IDS:
                requireInterfaceAndPIC();
                zepppBridge.readUserIDs (picDevice);
                break;

            case CLI_COMMAND_READ_EEPROM:
                requireInterfaceAndPIC();
                zepppBridge.readDataMem (picDevice);
                break;

            case CLI_COMMAND_READ_ALL:
                requireInterfaceAndPIC();
                zepppBridge.readAll(picDevice);
                break;

            case CLI_COMMAND_ERASE_PGM_MEM:
                requireInterfaceAndPIC();
                zepppBridge.erasePgmMem(picDevice);
                break;

            case CLI_COMMAND_ERASE_EEPROM:
                requireInterfaceAndPIC();
                zepppBridge.eraseDataMem(picDevice);
                break;

            case CLI_COMMAND_CHIP_ERASE:
                requireInterfaceAndPIC();
                zepppBridge.chipErase(picDevice);
                break;

            case CLI_COMMAND_PROGRAM:
                requireInterfaceAndPIC();
                // Erase all, then write all.
                zepppBridge.programAll(picDevice);
                break;

            case CLI_COMMAND_WAIT:
                long ms = Long.parseLong(trimValue);
                ZEPPPConsole.msg(String.format("Waiting %d ms ...", ms));
                Thread.sleep (ms);
                return false;

            case CLI_COMMAND_HELP:
                ZEPPPCLICommand helpCmd = identifyCommand(trimValue);
                if (helpCmd == null) {
                    error (String.format("Can't find help for '%s'", trimValue));
                } else {
                    printHelpForCommand(helpCmd);
                }
                return false;

            case CLI_COMMAND_VERSION:
                ZEPPPConsole.msg ("ZEPPP CLI version: " + ZEPPP_CLI_VERSION);
                ZEPPPConsole.msg ("Supported ZEPPP Interface version: " + ZEPPPClient.ZEPPP_EXPECTED_VERSION);
                ZEPPPConsole.msg("Supported PIC devices: " + programConfig.getSupportedPICDevices().toString());
                return false;

        }

        return true;
    }
    // ##############################################################################################################
    // ##                                                                                                          ##
    // ##                           S  U  P  P  O  R  T     F  U  N  C  T  I  O  N  S                              ##
    // ##                                                                                                          ##
    // ##############################################################################################################
    private static void printHelpForCommand (ZEPPPCLICommand cmd) {
        if (cmd.getShortName() != null) msg ("-" + cmd.getShortName() + " " + (cmd.getParams() != null? cmd.getParams() : ""));
        if (cmd.getLongName() != null) msg ("-" + cmd.getLongName() + " " + (cmd.getParams() != null? cmd.getParams() : ""));
        msg ("\t" + cmd.getDescription() + "\n");
    }

    private static String cmdStr (ZEPPPCLICommand.CLICommandCode cmdCode) {
        for (ZEPPPCLICommand cmd : commandList) {
            if (cmd.getCode() == cmdCode) {
                if (cmd.getShortName() != null) return cmd.getShortName();
                return cmd.getLongName();
            }
        }
        return "??";
    }

    private static void printHelp () {
        msg ("USAGE:\n\t" + ZEPPP_CLI_APP_NAME + " <OPTIONS ....>\n");
        msg ("* Setting the COM port (where the interface is connected) is mandatory.");
        msg ("* Options are run one by one in the same order they were passed.");
        msg ("* This application has an internal PIC 'Memory' buffer that acts as a layer where data\n  can be read / written before it goes to a physical PIC or a file.\n");
        msg ("OPTIONS:");
        for (ZEPPPCLICommand cmd : commandList) {
            printHelpForCommand(cmd);
        }
        msg ("EXAMPLES:");
        msg ("\t" + String.format("%s -%s COM2 -%s blink.hex -%s", ZEPPP_CLI_APP_NAME, cmdStr(CLI_COMMAND_COMM), cmdStr(CLI_COMMAND_INPUT), cmdStr(CLI_COMMAND_PROGRAM)));
        msg ("\t" + String.format("%s -%s COM2 -%s -%s pic_full_mem_dump.hex", ZEPPP_CLI_APP_NAME, cmdStr(CLI_COMMAND_COMM),cmdStr(CLI_COMMAND_READ_ALL), cmdStr(CLI_COMMAND_OUTPUT)));
        msg ("\t" + String.format("%s -%s COM2 -%s 2000 -%s 16f877a -%s hex_file_with_eeprom_data.hex -%s", ZEPPP_CLI_APP_NAME, cmdStr(CLI_COMMAND_COMM), cmdStr(CLI_COMMAND_WAIT), cmdStr(CLI_COMMAND_DEVICE), cmdStr(CLI_COMMAND_INPUT), cmdStr(CLI_COMMAND_WRITE_EEPROM)));
    }

    private static void requireInterfaceAndPIC() throws ZEPPPCommandException, IntelHexParsingException {
        requireZEPPPInterface();
        if (picDevice == null) {
            PICDeviceConfigEntry picCfg = zepppBridge.autodetectDevice(programConfig);
            picDevice = new PicDevice(picCfg);
        }
    }

    private static void requireZEPPPInterface () throws ZEPPPCommandException {
        if (zepppBridge == null) ZEPPPConsole.critical("You need to connect to the interface first!");
        if (!zepppBridge.isConnected()) zepppBridge.connect();
    }

    public static void warning (String str) {
        msg("WARNING: " + str);
    }

    public static void info (String str) {
        msg("INFO: " + str);
    }

    public static void error (String str) {
        msg ("ERROR: " + str);
    }

    public static void critical (String str) {
        error(str);
        closeInterface();
        System.exit(1);
    }

    public static void msg (String str) {
        System.out.println(str);
    }
}
