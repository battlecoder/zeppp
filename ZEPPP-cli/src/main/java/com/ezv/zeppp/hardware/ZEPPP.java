package com.ezv.zeppp.hardware;

// ################################################################################################################
// ## ZEPPP                                                                                                      ##
// ##                                                                                                            ##
// ## Hub for methods and constants related to the ZEPPP interface.                                              ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
public class ZEPPP {
    public static final String OK_STR_PREFIX                 = "OK: ";
    public static final String ERR_STR_PREFIX                = "ER: ";

    public static final String ZEPPP_NAME_STRING             = "ZEPPP";

    public static final String ZEPPP_CMD_FIRMWARE_INFO       = "FWI";
    public static final String ZEPPP_CMD_ENTER_LVP_MODE      = "LVP";
    public static final String ZEPPP_CMD_EXIT_LVP_MODE       = "EXT";
    public static final String ZEPPP_CMD_CHIP_ERASE          = "CHE";
    public static final String ZEPPP_CMD_PGM_MEM_ERASE       = "PME";
    public static final String ZEPPP_CMD_DATA_MEM_ERASE      = "DME";
    public static final String ZEPPP_CMD_SELECT_CFG_MEM      = "SCM";
    public static final String ZEPPP_CMD_INCREASE_ADDRESS    = "IAD";
    public static final String ZEPPP_CMD_DATA_MEM_READ       = "DMR";
    public static final String ZEPPP_CMD_PGM_MEM_READ        = "PMR";
    public static final String ZEPPP_CMD_PGM_MEM_WRITE       = "PMW";
    public static final String ZEPPP_CMD_PGM_MEM_BLOCKWRITE  = "PMB";
    public static final String ZEPPP_CMD_DATA_MEM_WRITE      = "DMW";

    private ZEPPP () {

    }

    public static ZEPPPResponse checkZEPPPInterface (CommPort port, String expectedVersion){
        // The response to a "Firmware Info" command should be the project name, version, and release string
        ZEPPPResponse response = sendCommand(port, ZEPPP_CMD_FIRMWARE_INFO);

        if (response.getCode() == ZEPPPResponse.StatusCode.STATUS_OK) {
            String[] responseParts = response.getMessage().split(" ");

            if (responseParts.length < 3 || !responseParts[0].equals(ZEPPP_NAME_STRING)) {
                return new ZEPPPResponse(ZEPPPResponse.StatusCode.STATUS_ERROR, "Invalid version string received: " + response.getMessage());

            } else if (!responseParts[1].equals(expectedVersion)) {
                return new ZEPPPResponse(ZEPPPResponse.StatusCode.STATUS_ERROR, "Interface version mismatch! Expected: " + expectedVersion + ". Received: " + responseParts[1]);
            }
        }

        return response;
    }

    public static ZEPPPResponse sendCommand (CommPort port, String cmd) {
        String response = port.sendAndWaitResponse(cmd + "\r").trim();

        if (response.startsWith(OK_STR_PREFIX)) {
            return new ZEPPPResponse(ZEPPPResponse.StatusCode.STATUS_OK, response.substring(OK_STR_PREFIX.length()));

        }else if (response.startsWith(ERR_STR_PREFIX)) {
            return new ZEPPPResponse(ZEPPPResponse.StatusCode.STATUS_ERROR, response.substring(ERR_STR_PREFIX.length()));

        } else if (response.isEmpty()) {
            return new ZEPPPResponse(ZEPPPResponse.StatusCode.STATUS_ERROR, "No response from interface");
        }

        return new ZEPPPResponse(ZEPPPResponse.StatusCode.STATUS_ERROR, "Invalid response from interface: " + response);
    }
}
