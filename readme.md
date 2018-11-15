# ZEPPP : Zero External Parts PIC Programmer

**ZEPPP** is a PIC programmer that requires only an Arduino-compatible board and a small command-line PC utility to read, write, erase and verify several LVP-capable PIC microcontrollers via ICSP (In-Circuit Serial Programming).

The name of this project is a homage to the first PIC programmer I used: **James Padfield**'s "Enhanced NOPPP", a modified version of the classic **NOPPP** (No-Parts PIC Programmer) originally designed by **Michael Covington**. I built mine in the early 2000's and was the tool I used to program PICs for quite a while.

Currently ZEPPP supports the following PIC devices:
* 16F87, 16F88
* 16F627A, 16F628A, 16F648A
* 16F873A, 16F874A, 16F876A, 16F877A.

And can work with all the memory areas from the supported PICs (Program memory, EEPROM, Config words and User IDs).

## LICENSE
Both the firmware and the Command-Line utility are licensed under the MIT License.
Check **LICENSE.txt** for details.

## FIRMWARE
The firmware should work on **Arduino Nano**, **Pro Mini**, and **Uno** boards, including compatible designs using the **Atmega328P** processor.

You'll find the Arduino sketch in the **ZEPPP** folder.

Strictly speaking you can use the Arduino on its own to program your PICs without the command line utility, provided you don't mind sending the programming commands one by one by hand through a serial terminal (quite unpractical but can be done). A short document describing the serial commands implemented on the firmware can be found in **fw_commands.txt**.

## CONNECTING A PIC
Since this is a ICSP programmer you need to connect your Arduino (with the ZEPPP firmware) to your PIC using the ICSP pins (PGM, PGC, PGD, MCLR). On the Arduino side, those signals are mapped to digital pins 6 to 9. The exact mapping can be found at the top of the ZEPPP sketch in a section called "Pin assignment". You'll also need to connect the GND pin of your PIC to your Arduino's GND.

Unless you are targetting a PIC board with an already mounted ICSP header, you'll also need to check the pinout of your target PIC to know the pin number associated to each signal.

For example, to program a PIC 16F628A with the current firmware, the following pins should be connected:

| Arduino | ICSP Signal | PIC 16F628A  |
| ------- | ----------- | ------------ |
| D9      | PGM         | RB4 (Pin 10) |
| D8      | PGC         | RB6 (Pin 12) |
| D7      | PGD         | RB7 (Pin 13) |
| D6      | MCLR        | RA5 (Pin 4)  |
| GND     | Ground      | Vss (Pin 5)  |



## COMMAND-LINE UTILITY
The Command Line Interface (CLI) for this project was written in Java so it **should** work on Linux, Windows, OSX, and Raspberry Pi boards, although I've only tested it on Windows. Of course this means you'll need to [download and install Java](https://www.java.com/en/download/help/download_options.xml) on your machine first.

You'll find a ready-to-run **JAR** (**zeppp-cli.jar**) in the **root** folder of this repo, but you can also build the CLI yourself (the source code is inside the **ZEPPP-cli** folder and to build the project you'll need Apache Maven).

To run the compiled CLI jar you'll need to type the following in a cmd terminal:

    > java -jar zeppp-cli.jar <PARAMS>

For the sake of convenience I've also included a small batch file for Windows (**zeppp-cli.bat** ) that you can invoke from the cmd shell, and will check whether you have Java installed before running the jar with the parameters provided. Assuming both the jar and the bat file are in your current folder you should be able to run:

    > zeppp-cli <PARAMS>

Nothing too exciting. It basically does the "java -jar zeppp-cli.jar" part for you.


## USING THE CLI
You can run the CLI without parameters to see the available options, but here are some examples of what you can do with it:

> zeppp-cli -c COM3 -i blink.hex -p

This tells the CLI that the Arduino with the ZEPPP firmware is on COM3, then loads blink.hex, autodetects the connected PIC (this is implicit), and proceeds to "program" all the contents of the hex file on the detected PIC (-p is a shortcut for "Erase and Program All", since it's the most common operation).

> zeppp-cli -c COM3 -w 2000 -i blink.hex -p

Same as before but we are using an Arduino that resets itself when a serial connection is established (most Arduino variants do, apparently, the only one i've not seen doing this is Arduino Nano), so we need to wait 2 seconds (2000 ms) before trying to send any command.

> zeppp-cli -c COM3 -ra -o full_pic_dump.hex

Again, ZEPPP interface is assumed to be at COM3, the CLI autodetects the connected PIC, and proceeds to "read" the contents of all memory areas (-ra = Read All), saving all the read data to an HEX file.

> zeppp-cli -c COM3 -i program_already_burned_in_the_pic.hex -va

ZEPPP at COM3, The CLI will autodetect the connected PIC device and will read the contents from an hex file (that we presumably burned into the PIC beforehand). Then it will read and verify all the memory areas from the physical PIC, checking that they match the contents of the HEX file (-va = Verify All).

> zeppp-cli -c COM3 -w 2000 -d 16f628a -i file_with_only_eeprom_data.hex -pe

A more complicated example: ZEPPP firmware is in COM3 and we need to wait the 2 seconds before sending commands because we are using an Arduino Pro mini that resets when you connect to it. After the pause, we tell the CLI that we expect a 16F628A (this will check the connected PIC and will refuse to continue if a different PIC is found), and then we read an hex file that only contains eeprom data, which we will proceed to program into the PIC without touching other memory areas (-pe = Program EEPROM only).


## CLOSING WORDS
Feedback is always appreciated and if you decide to give this "programmer" a try let me know!

I originally posted about this project on my [blog](http://blog.damnsoft.org), so there's a chance that you'll find more related projects there in a future.

