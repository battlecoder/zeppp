package com.ezv.zeppp.config;

// ################################################################################################################
// ## PICDeviceConfigEntry                                                                                       ##
// ##                                                                                                            ##
// ## Single PIC Device configuration abstraction.                                                               ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
public class PICDeviceConfigEntry {
    private String deviceName;
    private int pgmMemSize = 0;
    private int dataSize = 0;
    private int deviceId = 0;
    private int confMemAddress = 0;
    private int dataHexFileLogicalAddress = 0;
    private int confWords = 0;
    private int deviceIdRevisionBits = 0;
    private byte chipErase = 0;
    private byte pgmEraseMode = 0;
    private byte pgmWriteSize = 1;

    public PICDeviceConfigEntry (String deviceName) {
        this.deviceName = deviceName;
    }

    public int getPgmMemSize () {
        return pgmMemSize;
    }

    public int getDataSize () {
        return dataSize;
    }

    public int getDeviceId () {
        return deviceId;
    }

    public int getConfMemAddress () {
        return confMemAddress;
    }

    public int getDataHexFileLogicalAddress () {
        return dataHexFileLogicalAddress;
    }

    public int getConfWords () {
        return confWords;
    }

    public int getDeviceIdRevisionBits () {
        return deviceIdRevisionBits;
    }

    public byte getChipErase () {
        return chipErase;
    }

    public byte getPgmEraseMode () {
        return pgmEraseMode;
    }

    public byte getPgmWriteSize () {
        return pgmWriteSize;
    }

    public String getDeviceName () {
        return deviceName;
    }

    public PICDeviceConfigEntry withPgmMemSize (int pgmMemSize) {
        this.pgmMemSize = pgmMemSize;
        return this;
    }

    public PICDeviceConfigEntry withDataSize (int dataSize) {
        this.dataSize = dataSize;
        return this;
    }

    public PICDeviceConfigEntry withDeviceId (int deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public PICDeviceConfigEntry withConfMemAddress (int confMemAddress) {
        this.confMemAddress = confMemAddress;
        return this;
    }

    public PICDeviceConfigEntry withDataHexFileLogicalAddress (int dataHexFileLogicalAddress) {
        this.dataHexFileLogicalAddress = dataHexFileLogicalAddress;
        return this;
    }

    public PICDeviceConfigEntry withConfWords (int confWords) {
        this.confWords = confWords;
        return this;
    }

    public PICDeviceConfigEntry withDeviceIdRevisionBits (int deviceIdRevisionBits) {
        this.deviceIdRevisionBits = deviceIdRevisionBits;
        return this;
    }

    public PICDeviceConfigEntry withChipErase (byte chipErase) {
        this.chipErase = chipErase;
        return this;
    }

    public PICDeviceConfigEntry withPgmEraseMode (byte pgmEraseMode) {
        this.pgmEraseMode = pgmEraseMode;
        return this;
    }

    public PICDeviceConfigEntry withPgmWriteSize (byte pgmWriteSize) {
        this.pgmWriteSize = pgmWriteSize;
        return this;
    }
}