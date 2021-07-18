# ZEPPP CHANGELOG


## CLI-VERSION v1.0.3, FIRMWARE v1.0.1 
Released 2021-07-17

### NEW
The following things were added:
* Support for the **16F87x** (non-A) family of devices: 16F870, 16F871, 16F872, 16F873, 16F874, 16F876, 16F877
* **-ignore-bounds-error** flag to force ZEPPP to work with HEX files that contain data outside the selected PIC's memory range.

The "-ignore-bounds-error" was added because of reports of one specific compiler generating HEX files that were mostly fine, except for a few bytes of automatically added data outside of PIC memory range.
The default behavior of ZEPPP is still to stop execution with an error on such cases, but this flag can be used to turn the error in just a warning, allowing ZEPPP to continue the process, discarding whatever data was outside range.

### CHANGED
Big changes were done to how verification is performed.
Most Write operations previously attempted "automatic" ("read back") verification immediately after writing, even if verification wasn't requested from the command line. This was either partially, or not supported at all by a handful of microcontrollers, so the behavior of "write" commands was not consistent across families. This also affected the "-p" command, which did not perform explicit verification apart from whatever was done implicitly by the write operations.

This behaviour also resulted in a rather confusing console output where ZEPPP would warn the user of the areas that couldn't be verified automatically, and thus required manual verification.


To fix this, the following was changed: 

* Write operations (-wp, -wc, -we, -wa) no longer attempt read-back verification. They just write to the corresponding memory areas. Users can still add -v flags if verification after a write is desired.
* The "-p" command now writes **and** verifies all memory areas. This is done using explicit verification, so it works the same across devices. If absolutely no verification is desired when programming, users can use "-wa" instead of "-p".

Other changes:
* "-v" (Show version) now shows the list of supported PIC devices.

### FIXED
* Trying to open or save to HEX file before connecting to the interface resulted in a Null Pointer Exception. This has been fixed.
