package com.ezv.zeppp.pic;

// ################################################################################################################
// ## PicDevice                                                                                                  ##
// ##                                                                                                            ##
// ## Container for all the user-defined data on a PIC together with its hardware properties.                    ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
import com.ezv.zeppp.config.PICDeviceConfigEntry;
import com.ezv.zeppp.intelhex.HexBuffer;
import com.ezv.zeppp.intelhex.IntelHexFile;
import com.ezv.zeppp.intelhex.IntelHexParsingException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PicDevice {
    public static final int DEFAULT_MEM_CONTENT = 0x3FFF;
    public static final int DEFAULT_DATA_MEM_CONTENT = 0x00FF;
    public static final int USER_IDS_COUNT = 4;
    public static final int CONF_WORD_OFFSET = 7;
    public static final int DEVICE_ID_OFFSET = 6;
    public static final int ENTRIES_PER_HEX_FILE_WHEN_SAVING = 8;
    public static final int CONF_WORD_LVP_MASK = 0x80;

    private PICDeviceConfigEntry deviceCfg;
    private HexBuffer confWords;
    private HexBuffer userIds;
    private HexBuffer dataMem;
    private HexBuffer programMem;

    public PicDevice (PICDeviceConfigEntry cfg) {
        this.deviceCfg = cfg;

        // We Assume all PICs have 4 User IDs, which seem to be common across all devices.
        // If this changes a new configuration parameter should be added in PICDeviceConfigEntry and taken into
        // consideration here.
        this.userIds = createWordHexBuffer(USER_IDS_COUNT, DEFAULT_MEM_CONTENT);

        // Rest of the memory.
        this.confWords = createWordHexBuffer(cfg.getConfWords(), DEFAULT_MEM_CONTENT);
        // For data we will create a word array, as it's expected to be in " words" in Hex files, despite being bytes.
        this.dataMem = createWordHexBuffer(cfg.getDataSize(), DEFAULT_DATA_MEM_CONTENT);
        this.programMem = createWordHexBuffer(cfg.getPgmMemSize(), DEFAULT_MEM_CONTENT);
    }

    public void loadFromHexFile (String filePath) throws IntelHexParsingException, IOException {
        Map<Integer, HexBuffer> loadedData = null;

        loadedData = IntelHexFile.load(filePath);
        for (Map.Entry<Integer, HexBuffer> entry : loadedData.entrySet()) {
            int baseAddress = entry.getKey();
            HexBuffer dataBlock = entry.getValue();
            for (int byteNdx = 0; byteNdx < dataBlock.getBufferSize(); byteNdx += 2) {
                this.setWordAt((baseAddress + byteNdx), (short) dataBlock.getWord(byteNdx));
            }
        }
    }

    public void saveToHexFile (String filePath) throws IntelHexParsingException {
        Map<Integer, HexBuffer> buffers = new HashMap<>();

        // Separate program memory in chunks ----
        addNonEmptyBlocks (buffers, programMem, 0, DEFAULT_MEM_CONTENT);
        // Separate data memory in chunks, converted to word entries instead of bytes ----
        addNonEmptyBlocks (buffers, dataMem, deviceCfg.getDataHexFileLogicalAddress()*2, DEFAULT_DATA_MEM_CONTENT);

        buffers.put(2*deviceCfg.getConfMemAddress(), getHexBufferSubsetInLSB(userIds, 0, USER_IDS_COUNT));
        buffers.put(2*(deviceCfg.getConfMemAddress() + CONF_WORD_OFFSET), getHexBufferSubsetInLSB(confWords, 0, deviceCfg.getConfWords()));
        IntelHexFile.save(filePath, buffers);
    }

    private void addNonEmptyBlocks (Map<Integer, HexBuffer> destBufferMap, HexBuffer memArea, int baseAddress, int emptyValue) {
        int memSizeInWords = memArea.getBufferSize() / 2;

        for (int wordIndex = 0; wordIndex < memSizeInWords; wordIndex += ENTRIES_PER_HEX_FILE_WHEN_SAVING) {
            if (!isMemAreaBlockEmpty (memArea, wordIndex, ENTRIES_PER_HEX_FILE_WHEN_SAVING, emptyValue)) {
                HexBuffer subset = getHexBufferSubsetInLSB(memArea,wordIndex * 2, ENTRIES_PER_HEX_FILE_WHEN_SAVING );
                destBufferMap.put(baseAddress + wordIndex*2, subset);
            }
        }
    }

    private HexBuffer getHexBufferSubsetInLSB (HexBuffer inBuffer, int start, int sizeInWords) {
        int remainingData =  inBuffer.getBufferSize() - start;
        int realSizeInBytes = Math.min(sizeInWords*2, remainingData);

        HexBuffer newBuffer = new HexBuffer(realSizeInBytes);
        for (int i = 0; i < realSizeInBytes; i+=2) newBuffer.setWord(i, (short)inBuffer.getWord(start + i));
        return newBuffer;
    }

    private void setWordAt (int byteAddress, short word) throws IntelHexParsingException {
        int wordAddress = byteAddress / 2;
        PICDeviceConfigEntry cfg = this.deviceCfg;

        if (isWithinAddressSpace(wordAddress, cfg.getDataHexFileLogicalAddress(), cfg.getDataSize())) {
            this.dataMem.setWord(byteAddress - 2*cfg.getDataHexFileLogicalAddress(), (short)(word & 0xff));

        } else if (isWithinAddressSpace(wordAddress, cfg.getConfMemAddress() + CONF_WORD_OFFSET, cfg.getConfWords())) {
            this.confWords.setWord(byteAddress - 2*(cfg.getConfMemAddress() + CONF_WORD_OFFSET), word);

        } else if (isWithinAddressSpace(wordAddress, cfg.getConfMemAddress(), USER_IDS_COUNT)) {
            this.userIds.setWord(byteAddress - 2*cfg.getConfMemAddress(), word);

        } else if (isWithinAddressSpace(wordAddress, 0, cfg.getPgmMemSize())) {
            this.programMem.setWord(byteAddress, word);

        } else {
            throw new IntelHexParsingException(
                    String.format("Memory address 0x%04x is not mapped to any memory space in the selected device (%s)",
                            byteAddress, cfg.getDeviceName())
            );
        }
    }

    private boolean isWithinAddressSpace (int addressToTry, int startAddress, int size) {
        if (size == 0) return false;
        return (addressToTry >= startAddress && (addressToTry - startAddress) < size);
    }

    private HexBuffer createWordHexBuffer (int words, int defaultVal) {
        HexBuffer buff = new HexBuffer(words * 2);
        for (int w = 0; w < words; w++) {
            buff.setWord(w * 2, (short) defaultVal);
        }
        return buff;
    }

    public PICDeviceConfigEntry getDeviceCfg () {
        return deviceCfg;
    }

    public HexBuffer getConfWords () {
        return confWords;
    }

    public HexBuffer getUserIds () {
        return userIds;
    }

    public HexBuffer getDataMem () {
        return dataMem;
    }

    public HexBuffer getProgramMem () {
        return programMem;
    }

    public boolean isPgmBlockEmpty (int wordStart, int wordCount) {
        return isMemAreaBlockEmpty (programMem, wordStart, wordCount, DEFAULT_MEM_CONTENT);
    }

    public boolean isDataBlockEmpty (int wordStart, int wordCount) {
        return isMemAreaBlockEmpty (dataMem, wordStart, wordCount, DEFAULT_DATA_MEM_CONTENT);
    }

    private boolean isMemAreaBlockEmpty (HexBuffer memArea, int wordStart, int wordCount, int emptyValue) {
        int max = memArea.getBufferSize();
        int start = wordStart*2;
        int end = start + wordCount*2;
        int limit = end < max ? end : max;

        for (int offset = start; offset < limit; offset += 2) {
            if (memArea.getWord(offset) != emptyValue) return false;
        }
        return true;
    }

}
