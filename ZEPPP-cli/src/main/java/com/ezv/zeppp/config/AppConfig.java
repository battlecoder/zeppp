package com.ezv.zeppp.config;

// ################################################################################################################
// ## AppConfig                                                                                                  ##
// ##                                                                                                            ##
// ## Container Class for the Configuration parameters of the app.                                               ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AppConfig {
    PICDeviceConfigEntry selectedDevice;
    private HashMap<String, PICDeviceConfigEntry> supportedPICs;

    public AppConfig () {
        this.supportedPICs = new HashMap<>();
        this.selectedDevice = null;

        add16F6xxDevices();
        add16F87xDevices();
        add16F8xDevices();
    }

    private void add16F6xxDevices () {
        addSupportedPIC (
                new PICDeviceConfigEntry("16f627a")
                        .withDataSize(128)
                        .withPgmMemSize(1024)
                        .withDeviceId(0b010000010)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)0)
                        .withPgmEraseMode((byte)0)
                        .withPgmWriteSize((byte)1)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f628a")
                        .withDataSize(128)
                        .withPgmMemSize(2048)
                        .withDeviceId(0b010000011)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)0)
                        .withPgmEraseMode((byte)0)
                        .withPgmWriteSize((byte)1)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f648a")
                        .withDataSize(256)
                        .withPgmMemSize(4096)
                        .withDeviceId(0b010001000)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)0)
                        .withPgmEraseMode((byte)0)
                        .withPgmWriteSize((byte)1)
        );
    }

    private void add16F87xDevices () {
        addSupportedPIC (
                new PICDeviceConfigEntry("16f873")
                        .withDataSize(128)
                        .withPgmMemSize(4096)
                        .withDeviceId(0b001001011)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)1)
                        .withPgmEraseMode((byte)0)
                        .withPgmWriteSize((byte)8)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f873a")
                        .withDataSize(128)
                        .withPgmMemSize(4096)
                        .withDeviceId(0b0011100100)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(4)
                        .withChipErase((byte)1)
                        .withPgmEraseMode((byte)0)
                        .withPgmWriteSize((byte)8)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f874")
                        .withDataSize(128)
                        .withPgmMemSize(4096)
                        .withDeviceId(0b001001001)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)1)
                        .withPgmEraseMode((byte)0)
                        .withPgmWriteSize((byte)8)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f874a")
                        .withDataSize(128)
                        .withPgmMemSize(4096)
                        .withDeviceId(0b0011100110)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(4)
                        .withChipErase((byte)1)
                        .withPgmEraseMode((byte)0)
                        .withPgmWriteSize((byte)8)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f876")
                        .withDataSize(256)
                        .withPgmMemSize(8192)
                        .withDeviceId(0b001001111)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)1)
                        .withPgmEraseMode((byte)0)
                        .withPgmWriteSize((byte)8)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f876a")
                        .withDataSize(256)
                        .withPgmMemSize(8192)
                        .withDeviceId(0b0011100000)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(4)
                        .withChipErase((byte)1)
                        .withPgmEraseMode((byte)0)
                        .withPgmWriteSize((byte)8)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f877")
                        .withDataSize(256)
                        .withPgmMemSize(8192)
                        .withDeviceId(0b001001101)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)1)
                        .withPgmEraseMode((byte)0)
                        .withPgmWriteSize((byte)8)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f877a")
                        .withDataSize(256)
                        .withPgmMemSize(8192)
                        .withDeviceId(0b0011100010)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(4)
                        .withChipErase((byte)1)
                        .withPgmEraseMode((byte)0)
                        .withPgmWriteSize((byte)8)
        );
    }

    private void add16F8xDevices () {
        addSupportedPIC (
                new PICDeviceConfigEntry("16f87")
                        .withDataSize(128)
                        .withPgmMemSize(4096)
                        .withDeviceId(0b0001110010)
                        .withConfMemAddress(0x2000)
                        .withConfWords(2)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(4)
                        .withChipErase((byte)1)
                        .withPgmEraseMode((byte)1)
                        .withPgmWriteSize((byte)4)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f88")
                        .withDataSize(256)
                        .withPgmMemSize(4096)
                        .withDeviceId(0b0001110110)
                        .withConfMemAddress(0x2000)
                        .withConfWords(2)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(4)
                        .withChipErase((byte)1)
                        .withPgmEraseMode((byte)1)
                        .withPgmWriteSize((byte)4)
        );
    }

    public boolean setSelectedDevice (String name) {
        this.selectedDevice = this.supportedPICs.get(name.toLowerCase());
        return this.selectedDevice != null;
    }

    public PICDeviceConfigEntry getSelectedDevice () {
        return selectedDevice;
    }

    public void addSupportedPIC (PICDeviceConfigEntry config) {
        this.supportedPICs.put(config.getDeviceName(), config);
    }

    public Set<String> getSupportedPICDevices () {
        return this.supportedPICs.keySet();
    }

    public PICDeviceConfigEntry getDeviceByFullIdResponse (int id) {
        for (Map.Entry<String, PICDeviceConfigEntry> entry : this.supportedPICs.entrySet()) {
            PICDeviceConfigEntry picDevice = entry.getValue();
            if (id >> picDevice.getDeviceIdRevisionBits() == picDevice.getDeviceId() ) return picDevice;
        }
        return null;
    }
}
