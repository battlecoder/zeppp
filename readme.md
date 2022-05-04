# ZEPPP : Zero External Parts PIC Programmer

**ZEPPP** is a PIC programmer that requires only an Arduino-compatible board and a small command-line PC utility (CLI) to read, write, erase and verify several LVP-capable PIC microcontrollers via ICSP (In-Circuit Serial Programming).

The name of this project is a homage to the first PIC programmer I used: **James Padfield**'s "Enhanced NOPPP", a modified version of the classic **NOPPP** (No-Parts PIC Programmer) originally designed by **Michael Covington**. I built mine in the early 2000's and was the tool I used to program PICs for quite a while.

Currently ZEPPP supports the following PIC devices:
* 16F87, 16F88
* 16F627A, 16F628A, 16F648A
* 16F873A, 16F874A, 16F876A, 16F877A
* 16F870, 16F871, 16F872, 16F873, 16F874, 16F876, 16F877
* 16F882, 16F883, 16F884, 16F886, 16F887 (*)

(*): Calibration word and PGM block writes not yet supported in this family of microcontrollers.

And can work with all the memory areas from the supported PICs (Program memory, EEPROM, Config words and User IDs).


## LICENSE
Both the firmware and the Command-Line utility are licensed under the MIT License.
Check **LICENSE.txt** for details.

## FIRMWARE
The firmware should work on **Arduino Nano**, **Pro Mini**, and **Uno** boards, including compatible designs using the **Atmega328P** processor.

You'll find the Arduino Sketch (*ZEPPP.ino*) in the **/ZEPPP** folder of this repository.

Strictly speaking you can use the Arduino on its own to program your PICs without the command line utility, provided you don't mind sending the programming commands one by one by hand through a serial terminal (quite unpractical but can be done). A short document describing the serial commands implemented on the firmware can be found in **fw_commands.txt**.

## CONNECTING A PIC
Since this is a ICSP programmer you need to connect your Arduino (with the ZEPPP firmware) to your PIC using the ICSP pins (PGM, PGC, PGD, MCLR). On the Arduino side, those signals are mapped to digital pins 6 to 9. The exact mapping can be found at the top of the ZEPPP sketch in a section called "Pin assignment". You'll also need to connect the GND pin of your PIC to your Arduino's GND.

Unless you are targetting a PIC board with an already mounted ICSP header, you'll also need to check the pinout of your target PIC to know the pin number associated to each signal. The following table shows the pins that should be connected depending on the PIC device family, with the current version of the firmware:

| Arduino | ICSP Signal | PIC 16F6xxA  | PIC 16F87/88 | PIC 16F87x(A)| PIC 16F88X   |
| ------- | ----------- | ------------ | ------------ | ------------ | ------------ |
| D9      | PGM (6)     | RB4 (Pin 10) | RB3 (Pin 9)  | RB3 (Pin 36) | RB3 (Pin 36) |
| D8      | PGC (5)     | RB6 (Pin 12) | RB6 (Pin 12) | RB6 (Pin 39) | RB6 (Pin 39) |
| D7      | PGD (4)     | RB7 (Pin 13) | RB7 (Pin 13) | RB7 (Pin 40) | RB7 (Pin 40) |
| D6      | MCLR (1)    | RA5 (Pin 4)  | RA5 (Pin 4)  | MCLR (Pin 1) | MCLR (Pin 1) |
| GND     | Ground (3)  | Vss (Pin 5)  | Vss (Pin 5)  | Vss (12, 31) | Vss (12, 31) |

---
> **_NOTE:_** 
Your target PIC also needs to be connected to power (Its VDD pin/s must be getting their operational voltage, which is most likely going to be 5V). You can get away with using your Arduino's 5V pin for that if the PIC you want to program is not connected to anything else.
---

As briefly mentioned before, if your target board has a proper ICSP connector, you would need connect the Arduino pins to the corresponding signals on the ICSP header, but bear in mind that if the ICSP connector on your board does not support Low-Voltage Programming (LVP), it will most likely lack the "PGM" signal, and you won't be able to use this programmer.


## COMMAND-LINE UTILITY
The Command Line Interface (CLI) for this project was written in Java so it **should** work on Linux, Windows, OSX, and Raspberry Pi boards, although I've only tested it on Windows. Of course this means you'll need to [download and install Java](https://www.java.com/en/download/help/download_options.xml) on your machine first.

You'll find a ready-to-run **JAR** (**zeppp-cli.jar**) in the **root** folder of this repo, but you can also build the CLI yourself (the source code is inside the **ZEPPP-cli** folder and to build the project you'll need Apache Maven).

To run the compiled CLI jar you'll need to type the following in a cmd terminal:

    > java -jar zeppp-cli.jar <PARAMS>

For the sake of convenience I've also included a small batch file for Windows (**zeppp-cli.bat** ) that you can invoke from the cmd shell, and will check whether you have Java installed before running the jar with the parameters provided. Assuming both the jar and the bat file are in your current folder you should be able to run:

    > zeppp-cli <PARAMS>

Nothing too exciting. It basically does the "java -jar zeppp-cli.jar" part for you.


## USING THE CLI

You can run the CLI without parameters to see the available options. All the operations that you perform will be done in the order they appear in the command line, so you can chain multiple operations together.
Here are some examples of what you can do with it:

---
> **_NOTE:_**  Most Arduino variants equipped with a USB-Serial driver IC for programming (Arduino Uno, Nano, etc) need the **-wait** parameter with at least a delay of 2 seconds after the COM port is selected. This is because they reset the microcontroller when a connection is eastablished, and ZEPPP-CLI will need to wait before attempting to communicate with the firmware. The [Arduino Pro Mini](https://www.arduino.cc/en/pmwiki.php?n=Main/ArduinoBoardProMini) (The one **without** an on-board USB Serial IC, that needs to be manually reset with push-button when programmed) is the only Arduino I've tested so far that doesn't need this, because it doesn't auto-reset on serial connections.
You may need to add or remove the **-wait** flag from the following examples depending on the Arduino board you are using.
---

**Example 1**
> zeppp-cli -c COM3 -i blink.hex -p

This tells the CLI that the Arduino with the ZEPPP firmware is on COM3, then loads blink.hex, autodetects the connected PIC (this is implicit), and proceeds to "program" all the contents of the hex file on the detected PIC (-p is a shortcut for "Erase and Program All", since it's the most common operation).

**Example 2**
> zeppp-cli -c COM3 -wait 2000 -i blink.hex -p

Same as before but we are using an Arduino that resets itself when a serial connection is established, so we need to wait 2 seconds (2000 ms) before trying to send any command.

**Example 3**
> zeppp-cli -c COM3 -ra -o full_pic_dump.hex

Again, ZEPPP interface is assumed to be at COM3, the CLI autodetects the connected PIC, and proceeds to "read" the contents of all memory areas (-ra = Read All), saving all the read data to an HEX file.

**Example 4**
> zeppp-cli -c COM3 -i program_already_burned_in_the_pic.hex -va

ZEPPP at COM3, The CLI will autodetect the connected PIC device and will read the contents from an hex file (that we presumably burned into the PIC beforehand). Then it will read and verify all the memory areas from the physical PIC, checking that they match the contents of the HEX file (-va = Verify All).

**Example 5**
> zeppp-cli -c COM3 -wait 2000 -d 16f628a -i file_with_only_eeprom_data.hex -we

A more complicated example: ZEPPP firmware is in COM3 and we need to wait the 2 seconds before sending commands because we are using an Arduino Uno. After the pause, we tell the CLI that we expect a 16F628A (this will check the connected PIC and will refuse to continue if a different PIC is found), and then we read an hex file that only contains eeprom data, which we will proceed to program into the PIC without touching other memory areas (-we = Write EEPROM only).


## BUILDING THE CLI FROM SOURCE CODE
You can skip this step if you use the [pre-compiled JAR](https://github.com/battlecoder/zeppp/blob/master/zeppp-cli.jar), which should work out of the box in most systems.

If you want to build the tool yourself from source code, you'll need [Apache Maven](https://maven.apache.org/) and the [Java JDK version 8 or better](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html).
Once you have both working in your enviroment, building the CLI program only requires you to run:

    > cd ZEPPP-cli
    > mvn clean package

The JAR will be copied to the root directory of the project automatically once it's built.



## CLOSING WORDS
Feedback is always appreciated and if you decide to give this "programmer" a try let me know!

I originally posted about this project on my [blog](http://blog.damnsoft.org), so there's a chance that you'll find more related projects there in a future.

