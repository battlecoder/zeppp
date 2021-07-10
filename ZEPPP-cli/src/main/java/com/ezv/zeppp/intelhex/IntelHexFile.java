package com.ezv.zeppp.intelhex;

// ################################################################################################################
// ## IntelHexFile                                                                                               ##
// ##                                                                                                            ##
// ## Basic implementation of an Intel Hex Format File Reader/Writer.                                            ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class IntelHexFile {

    private IntelHexFile() {
    }

    public static void save (String intelHexFilePath, Map<Integer, HexBuffer> entries) throws IntelHexParsingException {
        int addressH;
        int addressL;
        Path filePath = Paths.get(intelHexFilePath);

        if (Files.exists(filePath)) {
            throw new IntelHexParsingException(String.format("File '%s' already exists", intelHexFilePath));
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, Charset.forName("UTF-8"))){
            int currentAddressH = 0;

            saveIntelHexRecord (writer, IntelHexRecord.createExtendedAddressRecord(currentAddressH));
            // Create a sorted map.
            TreeMap<Integer, HexBuffer> sortedMap = new TreeMap<>(entries);
            for (Map.Entry<Integer, HexBuffer> entry : sortedMap.entrySet()) {
                HexBuffer buffer = entry.getValue();
                int absoluteAddress = entry.getKey();
                if (absoluteAddress > 0xffff) {
                    addressH = (absoluteAddress >> 16) & 0xffff;
                    addressL = absoluteAddress & 0xffff;
                } else {
                    addressH = 0;
                    addressL = absoluteAddress;
                }
                // Create and add an entry that switches to this address mask.
                if (addressH != currentAddressH) {
                    currentAddressH = addressH;
                    saveIntelHexRecord (writer, IntelHexRecord.createExtendedAddressRecord(addressH));
                }
                // Write the entry proper.
                saveIntelHexRecord (
                        writer,
                        new IntelHexRecord((byte)IntelHexRecord.INTELHEX_RECORDTYPE_DATA, addressL, buffer,
                                0, (byte)buffer.getBufferSize())
                );
            }
            saveIntelHexRecord(writer, IntelHexRecord.createEndOfFileRecord());
        } catch (Exception e) {
            throw new IntelHexParsingException ("Couldn't save buffers to file! " + e.toString());
        }
    }

    private static void saveIntelHexRecord (BufferedWriter writer, IntelHexRecord record) throws IOException {
        writer.write(record.toString());
        writer.write("\n");
    }

    public static Map<Integer, HexBuffer> load (String intelHexFilePath) throws IntelHexParsingException, IOException {
        int addressH = 0;
        HashMap<Integer, HexBuffer> dataBlocks = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(intelHexFilePath), StandardCharsets.UTF_8);

        for (int ln = 0; ln < lines.size(); ln++) {
            IntelHexRecord record = IntelHexRecord.fromString(lines.get(ln));

            switch (record.getType()) {
                case IntelHexRecord.INTELHEX_RECORDTYPE_DATA:
                    int realAddress  = (addressH << 16) | record.getAddress();
                    HexBuffer buffer =  new HexBuffer(record.getData());

                    dataBlocks.put(realAddress, buffer);
                break;

                case IntelHexRecord.INTELHEX_RECORDTYPE_ENDOFFILE:
                    return dataBlocks;

                case IntelHexRecord.INTELHEX_RECORDTYPE_EXTENDEDADDR:
                    addressH = record.getWordAt(0);
                    break;

                default:
                    throw new IntelHexParsingException(
                            String.format("Unsupported record type %d at line %s", record.getType(), ln)
                    );

            }
        }
        return dataBlocks;
    }
}
