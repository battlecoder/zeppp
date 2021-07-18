package com.ezv.zeppp;

// ################################################################################################################
// ## IntelHexPicHelper                                                                                          ##
// ##                                                                                                            ##
// ## Helper class to load/save HEX Files from PIC memory contents.                                              ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
import com.ezv.zeppp.config.PICDeviceConfigEntry;
import com.ezv.zeppp.intelhex.HexBuffer;
import com.ezv.zeppp.intelhex.IntelHexFile;
import com.ezv.zeppp.intelhex.IntelHexParsingException;
import com.ezv.zeppp.pic.PicDevice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class IntelHexPicHelper {
    public static void loadFromHexFile (PicDevice pic, String filePath, boolean ignoreOutOfBounds) throws IntelHexParsingException, IOException {
        Map<Integer, HexBuffer> loadedData;

        loadedData = IntelHexFile.load(filePath);
        for (Map.Entry<Integer, HexBuffer> entry : loadedData.entrySet()) {
            int baseAddress = entry.getKey();
            HexBuffer dataBlock = entry.getValue();
            for (int byteNdx = 0; byteNdx < dataBlock.getBufferSize(); byteNdx += 2) {
                int addr = (baseAddress + byteNdx);
                boolean success = pic.setWordAt(addr, (short) dataBlock.getWord(byteNdx));
                if (!success) {
                    String msg = String.format(
                            "Memory address 0x%04x is not mapped to any memory space in the selected device (%s)",
                            addr, pic.getDeviceCfg().getDeviceName());

                    if (ignoreOutOfBounds) {
                        ZEPPPConsole.warning(msg);
                    } else {
                        throw new IntelHexParsingException(msg);
                    }
                }
            }
        }
    }

    public static void saveToHexFile (PicDevice pic, String filePath) throws IntelHexParsingException {
        PICDeviceConfigEntry picConfig = pic.getDeviceCfg();
        Map<Integer, HexBuffer> buffers = new HashMap<>();

        // Separate program memory in chunks ----
        pic.addNonEmptyBlocks (buffers, pic.getProgramMem(), 0, PicDevice.DEFAULT_MEM_CONTENT);

        // Separate data memory in chunks, converted to word entries instead of bytes ----
        pic.addNonEmptyBlocks (buffers, pic.getDataMem(), picConfig.getDataHexFileLogicalAddress()*2, PicDevice.DEFAULT_DATA_MEM_CONTENT);

        buffers.put(2*picConfig.getConfMemAddress(), pic.getUserIds().getHexBufferSubsetInLSB(0, PicDevice.USER_IDS_COUNT));
        buffers.put(2*(picConfig.getConfMemAddress() + PicDevice.CONF_WORD_OFFSET), pic.getConfWords().getHexBufferSubsetInLSB(0, picConfig.getConfWords()));
        IntelHexFile.save(filePath, buffers);
    }



}
