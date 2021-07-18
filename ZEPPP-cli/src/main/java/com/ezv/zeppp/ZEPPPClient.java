package com.ezv.zeppp;

// ################################################################################################################
// ## ZEPPPClient                                                                                                ##
// ##                                                                                                            ##
// ## Intermediary layer between the ZEPPP Console functions and the ZEPPP Hardware.                             ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
import com.ezv.zeppp.config.AppConfig;
import com.ezv.zeppp.config.PICDeviceConfigEntry;
import com.ezv.zeppp.hardware.ZEPPP;
import com.ezv.zeppp.hardware.ZEPPPResponse;
import com.ezv.zeppp.hardware.CommPort;
import com.ezv.zeppp.intelhex.HexBuffer;
import com.ezv.zeppp.intelhex.HexFileParseUtils;
import com.ezv.zeppp.intelhex.IntelHexParsingException;
import com.ezv.zeppp.pic.PicDevice;

public class ZEPPPClient {
    public static final int DATA_UNITS_PER_READWRITE        = 32;
    public static final String ZEPPP_EXPECTED_VERSION      = "1.0.1";
    public static final int ZEPPP_BAUD_RATE                = 115200;

    CommPort comm = null;
    boolean currentlyInLVPMode;
    int currentlyVerifiedDeviceID = 0;
    boolean isConnected = false;

    public ZEPPPClient (String port) throws ZEPPPCommandException {
        this.currentlyInLVPMode = false;

        this.comm =  new CommPort(port, ZEPPP_BAUD_RATE);

        ZEPPPConsole.msg("Opening port " + port + "...");
        if (!this.comm.open()){
            throw new ZEPPPCommandException("Check that the interface is connected, and the port is not already opened by another program", "Open port " + port);
        }
    }

    public void connect() throws ZEPPPCommandException {
        if (isConnected) return;

        ZEPPPConsole.msg("Connecting to ZEPPP interface...");
        ZEPPPResponse response = ZEPPP.checkZEPPPInterface(this.comm, ZEPPP_EXPECTED_VERSION);
        throwExceptionOnFailure (response, "Connect to interface");
        ZEPPPConsole.msg("-- Interface detected: " + response.getMessage());

        isConnected = true;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public void verifyDeviceID (PICDeviceConfigEntry picCfg, AppConfig loadedConfig) throws ZEPPPCommandException, IntelHexParsingException {
        int expectedId = picCfg.getDeviceId();

        if (currentlyVerifiedDeviceID == expectedId) return;

        PICDeviceConfigEntry connectedDevice = getConnectedDevice(loadedConfig);
        if (connectedDevice.getDeviceId() != expectedId) {
            throw new ZEPPPCommandException(
                    String.format("ID mismatch! Expected 0x%04x (%s)", expectedId, picCfg.getDeviceName()), "Verify PIC Device ID"
            );
        }
        currentlyVerifiedDeviceID = expectedId;
    }

    public PICDeviceConfigEntry autodetectDevice (AppConfig loadedConfig) throws ZEPPPCommandException, IntelHexParsingException {
        PICDeviceConfigEntry connectedDevice = getConnectedDevice(loadedConfig);
        currentlyVerifiedDeviceID = connectedDevice.getDeviceId();
        return connectedDevice;
    }

    private PICDeviceConfigEntry getConnectedDevice (AppConfig loadedConfig) throws ZEPPPCommandException, IntelHexParsingException  {
        ZEPPPConsole.msg("Detecting connected device...");
        selectConfigMemStart(PicDevice.DEVICE_ID_OFFSET);
        ZEPPPResponse response = sendCommandWithByte(ZEPPP.ZEPPP_CMD_PGM_MEM_READ, (byte)1);
        throwExceptionOnFailure(response, "Read Device ID");

        int deviceIdFull = response.getMessageWord();

        PICDeviceConfigEntry detectedDevice = loadedConfig.getDeviceByFullIdResponse(deviceIdFull);

        if (detectedDevice == null) {
            throw new ZEPPPCommandException(String.format("Unrecognized device with ID 0x%04x", deviceIdFull), "Detect connected device");
        }

        String detectedDeviceName = detectedDevice.getDeviceName();

        int deviceId = deviceIdFull >> detectedDevice.getDeviceIdRevisionBits();
        int deviceRevision = detectedDevice.getDeviceIdRevisionBits() & (0xffff >> detectedDevice.getDeviceIdRevisionBits());

        ZEPPPConsole.msg(String.format("-- Device Name:     %s" , detectedDeviceName));
        ZEPPPConsole.msg(String.format("-- Device ID:       0x%04x" , deviceId));
        ZEPPPConsole.msg(String.format("-- Device Revision: 0x%04x" , deviceRevision));

        return detectedDevice;
    }

    private void enterLVPMode() throws ZEPPPCommandException {
        if (currentlyInLVPMode) return;
        throwExceptionOnFailure(sendCommand(ZEPPP.ZEPPP_CMD_ENTER_LVP_MODE), "Enter LVP Mode");
        currentlyInLVPMode = true;
    }

    // Let's make this one NOT throw an exception.
    public void exitLVPMode()  {
        if (sendCommand(ZEPPP.ZEPPP_CMD_EXIT_LVP_MODE).getCode() == ZEPPPResponse.StatusCode.STATUS_OK) currentlyInLVPMode = false;
    }

    public void resetLVP () throws ZEPPPCommandException {
        if (currentlyInLVPMode) {
            exitLVPMode();
        }
        enterLVPMode();
    }

    public void verifyUserIDs (PicDevice picDevice) throws ZEPPPCommandException, IntelHexParsingException {
        resetLVP();
        ZEPPPConsole.msg("Verifying User IDs...");
        throwExceptionOnFailure(sendCommand(ZEPPP.ZEPPP_CMD_SELECT_CFG_MEM), "Select CFG Memory Area");

        ZEPPPResponse response = sendCommandWithByte(ZEPPP.ZEPPP_CMD_PGM_MEM_READ, (byte)PicDevice.USER_IDS_COUNT);
        throwExceptionOnFailure(response, "Read User IDs");

        int[] uids = response.getMessageWordArray();
        if (uids.length != PicDevice.USER_IDS_COUNT) throw new ZEPPPCommandException("Data size mismatch", "Verify User ID Memory");
        verifyWordBuffer (picDevice.getUserIds(), 0, uids);
        ZEPPPConsole.msg("User ID Verification finished successfully.");
    }

    public void verifyConfigWords (PicDevice picDevice) throws ZEPPPCommandException, IntelHexParsingException {
        resetLVP();
        ZEPPPConsole.msg("Verifying Config Words...");

        selectConfigMemStart(PicDevice.CONF_WORD_OFFSET);
        ZEPPPResponse response = sendCommandWithByte(ZEPPP.ZEPPP_CMD_PGM_MEM_READ, (byte)picDevice.getDeviceCfg().getConfWords());
        throwExceptionOnFailure(response, "Read Config Words");

        int []dataReceived = response.getMessageWordArray();
        if (dataReceived.length != picDevice.getDeviceCfg().getConfWords()) throw new ZEPPPCommandException("Data size mismatch", "Verify Configuration Memory");
        verifyWordBuffer (picDevice.getConfWords(), 0, dataReceived);
        ZEPPPConsole.msg("Config Word Verification finished successfully.");
    }

    public void verifyPgmMem (PicDevice picDevice) throws IntelHexParsingException, ZEPPPCommandException {
        HexBuffer pgmMem = picDevice.getProgramMem();
        int pgmMemSizeInWords = getMaxWrittenWords(pgmMem);

        resetLVP();
        ZEPPPConsole.msg ("Verifying PGM Memory...");
        for (int i = 0; i < pgmMemSizeInWords; i += DATA_UNITS_PER_READWRITE) {
            if (picDevice.isPgmBlockEmpty(i, DATA_UNITS_PER_READWRITE)) {
                throwExceptionOnFailure(
                        sendCommandWithByte(ZEPPP.ZEPPP_CMD_INCREASE_ADDRESS, (byte) DATA_UNITS_PER_READWRITE),
                        String.format("Skip empty PGM Memory block 0x%04x", i)
                );
            }else {
                ZEPPPResponse readResponse = sendCommandWithByte(ZEPPP.ZEPPP_CMD_PGM_MEM_READ,(byte)DATA_UNITS_PER_READWRITE);

                throwExceptionOnFailure(
                        readResponse,
                        String.format("Read PGM Memory block 0x%04x", i)
                );
                int [] words = readResponse.getMessageWordArray();
                verifyWordBuffer(pgmMem, i*2, words);
            }
        }
        ZEPPPConsole.msg("PGM Memory Verification finished successfully.");
    }

    public void readPgmMem (PicDevice picDevice) throws IntelHexParsingException, ZEPPPCommandException {
        HexBuffer pgmMem = picDevice.getProgramMem();
        int pgmMemSizeInWords = pgmMem.getBufferSize() / 2;

        resetLVP();
        ZEPPPConsole.msg ("Reading PGM Memory...");
        for (int i = 0; i < pgmMemSizeInWords; i += DATA_UNITS_PER_READWRITE) {
            ZEPPPResponse readResponse = sendCommandWithByte(ZEPPP.ZEPPP_CMD_PGM_MEM_READ,(byte) DATA_UNITS_PER_READWRITE);
            throwExceptionOnFailure(
                    readResponse,
                    String.format("Read PGM Memory block 0x%04x", i)
            );
            int [] words = readResponse.getMessageWordArray();
            saveWordBuffer(pgmMem, i*2, words);
        }
    }

    public void readConfigWords (PicDevice picDevice) throws IntelHexParsingException, ZEPPPCommandException {
        HexBuffer confMem = picDevice.getConfWords();

        ZEPPPConsole.msg ("Reading Config Words...");
        selectConfigMemStart(PicDevice.CONF_WORD_OFFSET);

        ZEPPPResponse readResponse = sendCommandWithByte(ZEPPP.ZEPPP_CMD_PGM_MEM_READ,(byte)picDevice.getDeviceCfg().getConfWords());
        throwExceptionOnFailure(readResponse, "Read Config Words");
        int [] words = readResponse.getMessageWordArray();
        saveWordBuffer(confMem, 0, words);
        ZEPPPConsole.msg("-- :" + confMem.toStringAsWords());
    }

    public void readUserIDs (PicDevice picDevice) throws IntelHexParsingException, ZEPPPCommandException {
        HexBuffer uidMem = picDevice.getUserIds();

        ZEPPPConsole.msg ("Reading User IDs...");
        selectConfigMemStart(0);
        ZEPPPResponse readResponse = sendCommandWithByte(ZEPPP.ZEPPP_CMD_PGM_MEM_READ,(byte)PicDevice.USER_IDS_COUNT);
        throwExceptionOnFailure(readResponse, "Read User IDs");
        int [] words = readResponse.getMessageWordArray();
        saveWordBuffer(uidMem, 0, words);
        ZEPPPConsole.msg("-- :" + uidMem.toStringAsWords());
    }

    public void readDataMem (PicDevice picDevice) throws IntelHexParsingException, ZEPPPCommandException {
        HexBuffer dataMem = picDevice.getDataMem();
        int dataMemSizeInWords = dataMem.getBufferSize() / 2;

        resetLVP();
        ZEPPPConsole.msg ("Reading Data Memory...");
        for (int i = 0; i < dataMemSizeInWords; i += DATA_UNITS_PER_READWRITE) {
            ZEPPPResponse readResponse = sendCommandWithByte(ZEPPP.ZEPPP_CMD_DATA_MEM_READ,(byte)DATA_UNITS_PER_READWRITE);
            throwExceptionOnFailure(
                    readResponse,
                    String.format("Read Data Memory block 0x%04x", i)
            );
            int [] bytes = readResponse.getMessageWordArray();
            saveWordBuffer(dataMem, i*2, bytes);
        }
    }

    public void readAll(PicDevice picDevice) throws IntelHexParsingException, ZEPPPCommandException{
        readUserIDs (picDevice);
        readConfigWords (picDevice);
        readPgmMem (picDevice);
        readDataMem (picDevice);
    }


    public void verifyDataMem (PicDevice picDevice) throws IntelHexParsingException, ZEPPPCommandException {
        HexBuffer dataMem = picDevice.getDataMem();
        int dataMemSizeInWords = dataMem.getBufferSize() / 2;

        resetLVP();
        ZEPPPConsole.msg ("Verifying Data Memory...");

        for (int i = 0; i < dataMemSizeInWords; i += DATA_UNITS_PER_READWRITE) {
            if (picDevice.isDataBlockEmpty(i, DATA_UNITS_PER_READWRITE)) {
                throwExceptionOnFailure(
                        sendCommandWithByte(ZEPPP.ZEPPP_CMD_INCREASE_ADDRESS, (byte) DATA_UNITS_PER_READWRITE),
                        String.format("Skip empty Data Memory block 0x%04x", i)
                );
            }else {
                ZEPPPResponse readResponse = sendCommandWithByte(ZEPPP.ZEPPP_CMD_DATA_MEM_READ,(byte) DATA_UNITS_PER_READWRITE);
                throwExceptionOnFailure(
                        readResponse,
                        String.format("Read Data Memory block 0x%04x", i)
                );
                int [] bytes = readResponse.getMessageWordArray();
                verifyWordBuffer(dataMem, i*2, bytes);
            }
        }
        ZEPPPConsole.msg("EEPROM Verification finished successfully.");
    }

    public void verifyWordBuffer (HexBuffer picDeviceBuffer, int startOffset, int [] dataReceived) throws ZEPPPCommandException {
        for (int i = 0; i < dataReceived.length; i++) {
            int expected = picDeviceBuffer.getWord(startOffset + i*2);
            if (dataReceived[i] != expected) {
                throw new ZEPPPCommandException(
                        String.format("Expected 0x%04x. Got 0x%04x instead", expected, dataReceived[i]),
                        String.format("Verify data at offset 0x%04x!", (i + startOffset))
                );
            }
        }
    }

    public void saveWordBuffer (HexBuffer picDeviceBuffer, int startOffset, int [] dataReceived) {
        for (int i = 0; i < dataReceived.length; i++) {
            picDeviceBuffer.setWord(startOffset + i*2, (short)dataReceived[i]);
        }
    }

    public void saveByteBuffer (HexBuffer picDeviceBuffer, int startOffset, int [] dataReceived) {
        for (int i = 0; i < dataReceived.length; i++) {
            picDeviceBuffer.setByte(startOffset + i, (byte)dataReceived[i]);
        }
    }

    public void erasePgmMem(PicDevice picDevice)  throws ZEPPPCommandException {
        resetLVP();
        ZEPPPConsole.msg("Erasing PGM Memory...");
        ZEPPPConsole.info("In some devices this may also erase all Config Words");
        throwExceptionOnFailure(sendCommandWithByte(ZEPPP.ZEPPP_CMD_PGM_MEM_ERASE, picDevice.getDeviceCfg().getMemEraseMode()), "Erase PGM Memory");
    }

    public void eraseDataMem(PicDevice picDevice)  throws ZEPPPCommandException {
        resetLVP();
        ZEPPPConsole.msg("Erasing Data Memory...");
        throwExceptionOnFailure(sendCommandWithByte(ZEPPP.ZEPPP_CMD_DATA_MEM_ERASE, picDevice.getDeviceCfg().getMemEraseMode()), "Erase Data Memory");
    }

    public void chipErase(PicDevice picDevice)  throws ZEPPPCommandException {
        ZEPPPConsole.msg("Erasing CHIP Memory...");
        throwExceptionOnFailure(sendCommandWithByte(ZEPPP.ZEPPP_CMD_CHIP_ERASE, picDevice.getDeviceCfg().getChipErase()), "Erase CHIP");
    }

    public void writePgmMem (PicDevice picDevice) throws ZEPPPCommandException {
        HexBuffer pgmMem = picDevice.getProgramMem();
        int pgmMemSizeInWords = getMaxWrittenWords(pgmMem);
        byte writeSize = picDevice.getDeviceCfg().getPgmWriteSize();
        byte writeMode = picDevice.getDeviceCfg().getPgmWriteMode();

        resetLVP();
        ZEPPPConsole.msg("Writing PGM Memory...");
        for (int i = 0; i < pgmMemSizeInWords; i += DATA_UNITS_PER_READWRITE) {
            if (picDevice.isPgmBlockEmpty(i, DATA_UNITS_PER_READWRITE)) {
                throwExceptionOnFailure(
                        sendCommandWithByte(ZEPPP.ZEPPP_CMD_INCREASE_ADDRESS, (byte) DATA_UNITS_PER_READWRITE),
                        String.format("Skip empty PGM Memory block 0x%04x", i)
                );
            } else {
                throwExceptionOnFailure(
                        sendPgmWriteCommand(writeSize, writeMode, pgmMem, i, DATA_UNITS_PER_READWRITE),
                        String.format("Write PGM Memory block 0x%04x", i)
                );
            }
        }
    }

    private int getMaxWrittenWords (HexBuffer pgmMem) {
        int pgmMemSizeInWords = pgmMem.getBufferSize() / 2;

        for (int i = pgmMemSizeInWords-1; i >= 0; i--) {
            if (pgmMem.getWord(i*2) != PicDevice.DEFAULT_MEM_CONTENT) return i;
        }
        return 0;
    }

    public void writeDataMem (PicDevice picDevice) throws ZEPPPCommandException {
        HexBuffer dataMem = picDevice.getDataMem();
        int dataMemSizeInWords = dataMem.getBufferSize() / 2;
        byte dataWriteMode = picDevice.getDeviceCfg().getPgmWriteMode();

        resetLVP();
        ZEPPPConsole.msg("Writing Data Memory...");
        for (int i = 0; i < dataMemSizeInWords; i += DATA_UNITS_PER_READWRITE) {
            if (picDevice.isDataBlockEmpty(i, DATA_UNITS_PER_READWRITE)) {
                throwExceptionOnFailure(
                        sendCommandWithByte(ZEPPP.ZEPPP_CMD_INCREASE_ADDRESS, (byte) DATA_UNITS_PER_READWRITE),
                        String.format("Skip empty Data Memory block 0x%04x", i)
                );
            } else {
                throwExceptionOnFailure(
                        sendDataWriteCommand(dataWriteMode, dataMem, i, DATA_UNITS_PER_READWRITE),
                        String.format("Write Data Memory block 0x%04x", i)
                );
            }
        }
    }

    public void writeUserIDs (PicDevice picDevice) throws ZEPPPCommandException, IntelHexParsingException {
        byte writeSize = (byte)Math.min (picDevice.getDeviceCfg().getPgmWriteSize(), PicDevice.USER_IDS_COUNT );
        byte writeMode = picDevice.getDeviceCfg().getPgmWriteMode();

        ZEPPPConsole.msg("Writing User IDs...");
        selectConfigMemStart(0);
        sendPgmWriteCommand(writeSize, writeMode, picDevice.getUserIds(), 0, PicDevice.USER_IDS_COUNT);
    }

    public void writeConfigWords (PicDevice picDevice) throws ZEPPPCommandException {
        int confWordsCount = picDevice.getDeviceCfg().getConfWords();
        byte writeMode = picDevice.getDeviceCfg().getPgmWriteMode();

        ZEPPPConsole.msg("Writing Config Words...");
        selectConfigMemStart(PicDevice.CONF_WORD_OFFSET);

        sendPgmWriteCommand((byte)1, writeMode, picDevice.getConfWords(), 0, confWordsCount);
        if ((picDevice.getConfWords().getWord(0) & PicDevice.CONF_WORD_LVP_MASK) == 0) {
            ZEPPPConsole.info("Your code seems to disable Low-Voltage Programming. This won't be saved in PIC memory!");
        }
    }

    public void writeAll (PicDevice picDevice) throws ZEPPPCommandException, IntelHexParsingException {
        resetLVP();
        writeUserIDs(picDevice);
        writePgmMem(picDevice);
        writeDataMem(picDevice);
        // Write configuration last, because if code-protection is enabled we won't be able to
        // verify memory in the previous steps.
        writeConfigWords(picDevice);
    }

    public void programAll(PicDevice picDevice) throws ZEPPPCommandException, IntelHexParsingException {
        resetLVP();
        chipErase(picDevice);
        writeUserIDs(picDevice);
        verifyUserIDs(picDevice);
        writePgmMem(picDevice);
        verifyPgmMem(picDevice);
        writeDataMem(picDevice);
        verifyDataMem(picDevice);
        writeConfigWords(picDevice);
        try {
            verifyConfigWords(picDevice);
        } catch (ZEPPPCommandException ex) {
            ZEPPPConsole.info(ex.getMessage());
            ZEPPPConsole.info("Verification can fail if CP was enabled, or the Conf. Word tried to disable LVP.");
        }
    }

    public void verifyAll(PicDevice picDevice) throws ZEPPPCommandException, IntelHexParsingException{
        resetLVP();
        verifyUserIDs(picDevice);
        verifyConfigWords(picDevice);
        verifyPgmMem(picDevice);
        verifyDataMem(picDevice);
    }

    public void selectConfigMemStart (int withOffSet) throws ZEPPPCommandException {
        resetLVP();
        throwExceptionOnFailure(sendCommand(ZEPPP.ZEPPP_CMD_SELECT_CFG_MEM), "Select CFG Memory Area");
        if (withOffSet > 0) {
            throwExceptionOnFailure(sendCommandWithByte(ZEPPP.ZEPPP_CMD_INCREASE_ADDRESS, (byte)withOffSet), "Move to Address");
        }
    }

    private void throwExceptionOnFailure (ZEPPPResponse response, String action) throws ZEPPPCommandException {
        if (response.getCode() != ZEPPPResponse.StatusCode.STATUS_OK) {
            throw new ZEPPPCommandException(response.getMessage(), action);
        }
    }

    private ZEPPPResponse sendCommand (String cmd) {
        return ZEPPP.sendCommand(this.comm, cmd);
    }

    private ZEPPPResponse sendCommandWithByte (String cmd, byte byteParam) {
        return ZEPPP.sendCommand(this.comm, cmd + " " + HexFileParseUtils.hexByteString(byteParam));
    }

    private ZEPPPResponse sendPgmWriteCommand (byte writeSize, byte writeMode, HexBuffer wordBuffer, int startWordNdx, int numberOfWords)  {
        if (writeSize < 2) {
            return sendPgmCmndWithByteAndBuffer (ZEPPP.ZEPPP_CMD_PGM_MEM_WRITE, writeMode, wordBuffer, startWordNdx, numberOfWords);
        } else {
            return sendPgmCmndWithByteAndBuffer (ZEPPP.ZEPPP_CMD_PGM_MEM_BLOCKWRITE, writeSize, wordBuffer, startWordNdx, numberOfWords);
        }
    }

    private ZEPPPResponse sendPgmCmndWithByteAndBuffer (String cmd, byte byteParam, HexBuffer wordBuffer, int startWordNdx, int numberOfWords)  {
        StringBuilder cmdStrBuilder =  new StringBuilder();
        int bufferSizeInWords = wordBuffer.getBufferSize() / 2;

        cmdStrBuilder.append(cmd);
        cmdStrBuilder.append(' ');
        cmdStrBuilder.append(HexFileParseUtils.hexByteString(byteParam));

        int limit = startWordNdx + numberOfWords < bufferSizeInWords ? numberOfWords : bufferSizeInWords - startWordNdx;
        for (int w = 0; w < limit; w++) {
            cmdStrBuilder.append(' ');
            int wordNdx = startWordNdx + w;
            cmdStrBuilder.append(HexFileParseUtils.hexWordString(wordBuffer.getWord(wordNdx * 2)));
        }

        return ZEPPP.sendCommand(this.comm, cmdStrBuilder.toString());
    }

    private ZEPPPResponse sendDataWriteCommand (byte writeMode, HexBuffer byteBuffer, int start, int numberOfBytes)  {
        StringBuilder cmdStrBuilder =  new StringBuilder();
        int bufferSize = byteBuffer.getBufferSize();

        cmdStrBuilder.append(ZEPPP.ZEPPP_CMD_DATA_MEM_WRITE);
        cmdStrBuilder.append(' ');
        cmdStrBuilder.append(HexFileParseUtils.hexByteString(writeMode));

        int limit = start + numberOfBytes < bufferSize ? numberOfBytes : bufferSize - start;
        for (int w = 0; w < limit; w++) {
            cmdStrBuilder.append(' ');
            int byteNdx = start + w;
            cmdStrBuilder.append(HexFileParseUtils.hexByteString((byte)(byteBuffer.getWord(byteNdx*2) & 0xff)));
        }

        return ZEPPP.sendCommand(this.comm, cmdStrBuilder.toString());
    }
}
