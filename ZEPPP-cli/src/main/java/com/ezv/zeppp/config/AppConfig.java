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
import java.util.*;
import java.util.stream.Collectors;

public class AppConfig {
    PICDeviceConfigEntry selectedDevice;
    private List<PICDeviceConfigEntry> supportedPICs;

    public AppConfig () {
        this.supportedPICs = new ArrayList<>();
        this.selectedDevice = null;

        add16F6xxDevices();
        add16F87xDevices();
        add16F87xADevices();
        add16F8xDevices();
        add16F88xDevices();
    }

    private void add16F6xxDevices () {
        addSupportedPIC (
                new PICDeviceConfigEntry("16f627")
                        .withDataSize(128)
                        .withPgmMemSize(1024)
                        .withDeviceId(0b000111101)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)0)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)0)
                        .withPgmWriteSize((byte)1)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f628")
                        .withDataSize(128)
                        .withPgmMemSize(2048)
                        .withDeviceId(0b000111110)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)0)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)0)
                        .withPgmWriteSize((byte)1)
        );

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
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)0)
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
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)0)
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
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)0)
                        .withPgmWriteSize((byte)1)
        );
    }

    private void add16F87xDevices () {
        addSupportedPIC (
                new PICDeviceConfigEntry("16f870")
                        .withDataSize(64)
                        .withPgmMemSize(2048)
                        .withDeviceId(0b001101000)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)2)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)2)
                        .withPgmWriteSize((byte)1)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f871")
                        .withDataSize(64)
                        .withPgmMemSize(2048)
                        .withDeviceId(0b001101001)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)2)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)2)
                        .withPgmWriteSize((byte)1)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f872")
                        .withDataSize(64)
                        .withPgmMemSize(2048)
                        .withDeviceId(0b001000111)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)2)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)2)
                        .withPgmWriteSize((byte)1)
        );

        addSupportedPIC (
                new PICDeviceConfigEntry("16f873")
                        .withDataSize(128)
                        .withPgmMemSize(4096)
                        .withDeviceId(0b001001011)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)2)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)2)
                        .withPgmWriteSize((byte)1)
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
                        .withChipErase((byte)2)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)2)
                        .withPgmWriteSize((byte)1)
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
                        .withChipErase((byte)2)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)2)
                        .withPgmWriteSize((byte)1)
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
                        .withChipErase((byte)2)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)2)
                        .withPgmWriteSize((byte)1)
        );
    }

    private void add16F87xADevices () {
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
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)1)
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
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)1)
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
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)1)
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
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)1)
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
                        .withPgmWriteMode((byte)1)
                        .withMemEraseMode((byte)1)
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
                        .withPgmWriteMode((byte)1)
                        .withMemEraseMode((byte)1)
                        .withPgmWriteSize((byte)4)
        );
    }

    private void add16F88xDevices () {
        // Pending for this family of devices:
        // - working pgm block writes
        // - calibration word
        addSupportedPIC (
                new PICDeviceConfigEntry("16f882")
                        .withDataSize(128)
                        .withPgmMemSize(2048)
                        .withDeviceId(0b100000000)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)0)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)0)
                        .withPgmWriteSize((byte)1)
        );
        addSupportedPIC (
                new PICDeviceConfigEntry("16f883")
                        .withDataSize(256)
                        .withPgmMemSize(4096)
                        .withDeviceId(0b100000001)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)0)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)0)
                        .withPgmWriteSize((byte)1)
        );
        addSupportedPIC (
                new PICDeviceConfigEntry("16f884")
                        .withDataSize(256)
                        .withPgmMemSize(4096)
                        .withDeviceId(0b100000010)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)0)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)0)
                        .withPgmWriteSize((byte)1)
        );
        addSupportedPIC (
                new PICDeviceConfigEntry("16f886")
                        .withDataSize(256)
                        .withPgmMemSize(8192)
                        .withDeviceId(0b100000011)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)0)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)0)
                        .withPgmWriteSize((byte)1)
        );
        addSupportedPIC (
                new PICDeviceConfigEntry("16f887")
                        .withDataSize(256)
                        .withPgmMemSize(8192)
                        .withDeviceId(0b100000100)
                        .withConfMemAddress(0x2000)
                        .withConfWords(1)
                        .withDataHexFileLogicalAddress(0x2100)
                        .withDeviceIdRevisionBits(5)
                        .withChipErase((byte)0)
                        .withPgmWriteMode((byte)0)
                        .withMemEraseMode((byte)0)
                        .withPgmWriteSize((byte)1)
        );
    }

    private PICDeviceConfigEntry findConfigByDeviceName(String deviceName) {
        return this.supportedPICs.stream().filter(p -> p.getDeviceName().equalsIgnoreCase(deviceName)).findFirst().orElse(null);
    }

    public boolean setSelectedDevice (String name) {
        this.selectedDevice = findConfigByDeviceName(name);
        return this.selectedDevice != null;
    }

    public PICDeviceConfigEntry getSelectedDevice () {
        return selectedDevice;
    }

    public void addSupportedPIC (PICDeviceConfigEntry config) {
        this.supportedPICs.add(config);
    }

    public List<String> getSupportedPICDevices () {
        return this.supportedPICs.stream().map(p -> p.getDeviceName()).collect(Collectors.toList());
    }

    public PICDeviceConfigEntry getDeviceByFullIdResponse (int id) {
        for (PICDeviceConfigEntry picDevice : this.supportedPICs) {
            if (id >> picDevice.getDeviceIdRevisionBits() == picDevice.getDeviceId() ) return picDevice;
        }
        return null;
    }
}
