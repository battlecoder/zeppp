package com.ezv.zeppp.hardware;

// ################################################################################################################
// ## CommPort                                                                                                   ##
// ##                                                                                                            ##
// ## Helper class to initialize and manage a serial port using jSerialComm.                                     ##
// ## Part of the ZEPPP: Zero External Parts PIC Programmer project.                                             ##
// ##                                                                                                            ##
// ## Author: Elias Zacarias                                                                                     ##
// ##                                                                                                            ##
// ################################################################################################################
import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;

public class CommPort {
    public static final int READ_TIMEOUT = 50;
    public static final int COMMAND_FIRST_READ_TIMEOUT = 500;

    private SerialPort port;

    public CommPort (String descriptor, int baudRate) {
        port = SerialPort.getCommPort(descriptor);
        port.setBaudRate(baudRate);
        port.setNumDataBits(8);
        port.setNumStopBits(1);
        port.setParity(SerialPort.NO_PARITY);
        port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
    }

    public boolean open() {
        port.openPort();
        return port.isOpen();
    }

    public void close() {
        port.closePort();
    }

    public String sendAndWaitResponse (String str) {
        StringBuilder responseBuilder = new StringBuilder();

        port.writeBytes(str.getBytes(), str.length());

        // Using the input stream is faster than using "readBytes" with a buffer and the timeout.
        InputStream stream = port.getInputStream();

        // It also allows us to have a different timeout for the first byte (that should take longer
        // since the other end has to first parse and execute the command we just sent before sending data)
        // than the bytes that follow.
        int timeOut = COMMAND_FIRST_READ_TIMEOUT;
        try {
            long lastTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - lastTime < timeOut) {
                if (stream.available() != 0) {
                    responseBuilder.append((char)stream.read());
                    lastTime = System.currentTimeMillis();
                    timeOut = READ_TIMEOUT;
                }
            }
        } catch (Exception e) {
            return "";
        }
        return responseBuilder.toString();
    }
}
