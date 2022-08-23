#include "ZEPPP_icsp.h"

/*############################################################################
 *##                                                                        ##
 *##                                  C O N F I G                           ##
 *##                                                                        ##
 *############################################################################*/
#define ZEPPP_NAME_STRING       "ZEPPP"
/* If you add commands or change anything that also requires the CLI to change 
 * please update the version number. That way you can keep the CLI and firmware
 * in sync. Make sure to also update the date string for your releases. */
#define ZEPPP_VERSION_STRING    "1.0.1"
#define ZEPPP_RELDATE_STRING    "20200715"

#define MAX_SERIAL_IN_BUFFER     PIC_PGM_ROW*5 + 10 

/* Serial Commands ******************************/
typedef enum {
  ZEPPP_CMD_UNKNOWN,
  ZEPPP_CMD_FIRMWARE_INFO,
  ZEPPP_CMD_ENTER_LVP_MODE,
  ZEPPP_CMD_EXIT_LVP_MODE,
  ZEPPP_CMD_CHIP_ERASE,
  ZEPPP_CMD_PGM_MEM_ERASE,
  ZEPPP_CMD_DATA_MEM_ERASE,
  ZEPPP_CMD_SELECT_CFG_MEM,
  ZEPPP_CMD_INCREASE_ADDRESS,
  ZEPPP_CMD_DATA_MEM_READ,
  ZEPPP_CMD_PGM_MEM_READ,
  ZEPPP_CMD_PGM_MEM_BLOCK_WRITE,
  ZEPPP_CMD_PGM_MEM_WRITE,
  ZEPPP_CMD_DATA_MEM_WRITE
} ZEPPPCommand;

/* Serial command return codes ******************/
typedef enum {
  RET_ERR_VERIFICATION_FAILED = ICSP_ERR_VERIFICATION_FAILED,
  RET_ERR_OUT_OF_RANGE = ICSP_ERR_OUT_OF_RANGE,
  RET_OK = ICSP_OK,
  RET_ERR_SPACE_EXPECTED,
  RET_ERR_HEX_BYTE_EXPECTED,
  RET_ERR_HEX_WORD_EXPECTED,
  RET_ERR_UNKNOWN_COMMAND,
  RET_ERR_NO_MEMORY_AREA_SELECTED,
} ZEPPP_RET_ENUM;

#define ZEPPP_RET char

/* Serial Buffer Handling ***********************/
char serialBuffer[MAX_SERIAL_IN_BUFFER];
byte inBufferPos = 0;
byte bufferParsingPos = 0;

/* Serial command return strings ****************/
#define RET_MSG_ERROR       Serial.print(F("ER: "))
#define RET_MSG_OK          Serial.print(F("OK: "))


/*############################################################################
 *##                                                                        ##
 *##             S E R I A L   A U X   F U N C T I O N S                    ##
 *##                                                                        ##
 *############################################################################*/
void serial_write_byte (byte b) {
  if (b < 16) Serial.write ('0');
  Serial.print(b, HEX);
}

void serial_write_word (word w) {
  serial_write_byte (w >> 8);
  serial_write_byte (w & 0xff);
}

/*############################################################################
 *##                                                                        ##
 *##                                 S E T U P                              ##
 *##                                                                        ##
 *############################################################################*/
void setup() {
  Serial.begin(115200);   
  pinMode (MCLR_PIN, OUTPUT);
  pinMode (PGM_PIN, OUTPUT);
  pinMode (PGC_PIN, OUTPUT);

  lvp_exit_pgm_mode();
  serial_reset_buffer();
}

/*############################################################################
 *##                                                                        ##
 *##             H E X   U T I L I T Y   F U N C T I O N S                  ##
 *##                                                                        ##
 *############################################################################*/
 char hex_digit_val (char d){
  if (d >= '0'&& d <= '9')  return d - '0';
  if (d >= 'A'&& d <= 'F') return d - 'A' + 10;
  if (d >= 'a'&& d <= 'f') return d - 'a' + 10;
  return -1;
}

/*############################################################################
 *##                                                                        ##
 *##        S E R I A L   C O M M A N D   P A R S E   H E L P E R S         ##
 *##                                                                        ##
 *############################################################################*/
void serial_reset_buffer() {
  memset(serialBuffer, 0, MAX_SERIAL_IN_BUFFER);
  inBufferPos = 0;
}

bool serial_parse_match (char c){
  if (serialBuffer[bufferParsingPos] != c) return false;
  bufferParsingPos++;
  return true;
}

// maxDigits can be from 1 to 4
bool serial_parse_getword (word *dest, byte maxDigits) {
  byte digCount = 0;
  char digit = hex_digit_val(serialBuffer[bufferParsingPos]);
  if (digit == -1) return false; // At least one digit is required to have a number
  if (maxDigits > 4) maxDigits = 4;
  if (maxDigits < 1) maxDigits = 1;

  *dest = 0;
  while (digit != -1 && digCount < maxDigits) {
    *dest <<= 4;
    *dest |= (byte)digit;

    digCount++;
    // Get ready to parse next digit
    bufferParsingPos += 1;
    digit = hex_digit_val(serialBuffer[bufferParsingPos]);
  }
  return true;
}

bool serial_parse_getbyte (byte *dest) {
  word temp;
  bool r = serial_parse_getword (&temp, 2);
  if (r) {
    *dest = (byte)(temp & 0xff);
    return true;
  }
  return false;
}

void read_and_print_pgm_mem_words (byte sze){
  for (byte n = 0; n < sze; n++) {
    word d = read_pgm_mem();
    increment_addr();
    serial_write_word (d);
    Serial.write(' ');
  }
  Serial.write('\n');
}

void read_and_print_data_mem_words (byte sze){
  for (byte n = 0; n < sze; n++) {
    word d = read_data_mem() & 0xff;
    increment_addr();
    serial_write_word (d);
    Serial.write(' ');
  }
  Serial.write('\n');
}

ZEPPP_RET read_console_into_word_buffer (byte *count_dest) { 
  byte count = 0; 
  word w; 
  byte wordbuffer_size = pgm_buffer_size(); 
 
  while (serial_parse_match(' ')){ 
    if (count > wordbuffer_size) return RET_ERR_OUT_OF_RANGE; 
    if (!serial_parse_getword(&w, 4)) return RET_ERR_HEX_WORD_EXPECTED; 
    set_pgm_buffer(count, w); 
    count++; 
  }
  *count_dest = count;
  return RET_OK; 
}
/*############################################################################
 *##                                                                        ##
 *##             S E R I A L   C O M M A N D S  P A R S I N G               ##
 *##                                                                        ##
 *############################################################################*/
ZEPPPCommand getCommand (char *buffer){
  /* They all must be 3 letters! */

  if (strncmp(buffer, "LVP", 3) == 0) return ZEPPP_CMD_ENTER_LVP_MODE;
  if (strncmp(buffer, "EXT", 3) == 0) return ZEPPP_CMD_EXIT_LVP_MODE;
  if (strncmp(buffer, "SCM", 3) == 0) return ZEPPP_CMD_SELECT_CFG_MEM;
  if (strncmp(buffer, "PMR", 3) == 0) return ZEPPP_CMD_PGM_MEM_READ;
  if (strncmp(buffer, "PMW", 3) == 0) return ZEPPP_CMD_PGM_MEM_WRITE;
  if (strncmp(buffer, "PMB", 3) == 0) return ZEPPP_CMD_PGM_MEM_BLOCK_WRITE;
  if (strncmp(buffer, "DMR", 3) == 0) return ZEPPP_CMD_DATA_MEM_READ;
  if (strncmp(buffer, "IAD", 3) == 0) return ZEPPP_CMD_INCREASE_ADDRESS;
  if (strncmp(buffer, "DMW", 3) == 0) return ZEPPP_CMD_DATA_MEM_WRITE;
  if (strncmp(buffer, "DME", 3) == 0) return ZEPPP_CMD_DATA_MEM_ERASE;
  if (strncmp(buffer, "PME", 3) == 0) return ZEPPP_CMD_PGM_MEM_ERASE;
  if (strncmp(buffer, "CHE", 3) == 0) return ZEPPP_CMD_CHIP_ERASE;
  if (strncmp(buffer, "FWI", 3) == 0) return ZEPPP_CMD_FIRMWARE_INFO;
  return ZEPPP_CMD_UNKNOWN;
}

ZEPPP_RET execute_serial_cmd() {
  char ret;
  byte count;
  byte writeSize, eraseMode, writeMode;
  ZEPPPCommand cmdCode = getCommand(serialBuffer);

  // WARNING ABOUT COMMANDS:
  // In older devices (without an address reset command) the only way to reset the internal counter
  // to either read/write to another memory area from the start, is to exit and re-enter programming
  // mode, so dynamically switching between them is not possible. The only "Select" command that will
  // take you right to the beginning of the corresponding memory block is "select config", so it's perhaps
  // a good idea to perform "config" programming as the last step.

  // Not a really elegant way of parsing commands and arguments, but hey! it works.
  // (And still way better than using Strings, performance and portability-wise).
  // Skip the 3 characters we just read in getCommand().
  bufferParsingPos = 3;
  switch (cmdCode) {
    // Enter LVP Programming Mode --------
    case ZEPPP_CMD_ENTER_LVP_MODE:
      lvp_enter_pgm_mode ();
      RET_MSG_OK;
      Serial.println(F("Entering LVP Programming Mode (Legacy method)"));
    break;

    // Exit LVP Programming Mode --------
    case ZEPPP_CMD_EXIT_LVP_MODE:
      lvp_exit_pgm_mode ();
      RET_MSG_OK;
      Serial.println(F("Exiting LVP Programming Mode"));
    break;

    // Chip Erase --------
    case ZEPPP_CMD_CHIP_ERASE:
      if (!serial_parse_match(' ')) return RET_ERR_SPACE_EXPECTED; 
      if (!serial_parse_getbyte(&eraseMode)) return RET_ERR_HEX_BYTE_EXPECTED; 
      ret = oper_chip_erase(eraseMode);
      if (ret != RET_OK) {
        return ret;
      } else {
        RET_MSG_OK;
      }
      Serial.print("CHIP Erase ");
      Serial.println(eraseMode, 10);
    break;

    // PGM Memory Erase --------
    case ZEPPP_CMD_PGM_MEM_ERASE:
      if (!serial_parse_match(' ')) return RET_ERR_SPACE_EXPECTED; 
      if (!serial_parse_getbyte(&eraseMode)) return RET_ERR_HEX_BYTE_EXPECTED; 
      ret = oper_erase_pgm_mem(eraseMode);
      if (ret != RET_OK) {
        return ret;
      } else {
        RET_MSG_OK;
      }
      Serial.println(F("PROGRAM Memory Erased"));
    break;

    // DATA Memory Erase --------
    case ZEPPP_CMD_DATA_MEM_ERASE:
      if (!serial_parse_match(' ')) return RET_ERR_SPACE_EXPECTED; 
      if (!serial_parse_getbyte(&eraseMode)) return RET_ERR_HEX_BYTE_EXPECTED;
      ret = oper_erase_data_mem(eraseMode);
      if (ret != RET_OK) {
        return ret;
      } else {
        RET_MSG_OK;
      }
      Serial.println(F("DATA (EEPROM) Memory Erased"));
    break;

    // Select Config Memory --------
    case ZEPPP_CMD_SELECT_CFG_MEM:
      load_config_mem(0x3fff);
      RET_MSG_OK;
      Serial.println(F("CONFIG Memory Selected"));
    break;

    // Increment Address --------
    case ZEPPP_CMD_INCREASE_ADDRESS:
      if (!serial_parse_match(' ')) return RET_ERR_SPACE_EXPECTED;
      if (!serial_parse_getbyte(&count)) return RET_ERR_HEX_BYTE_EXPECTED;
      increase_addr_n (count);
      RET_MSG_OK;
      Serial.print(F("Address Pointer increased "));
      Serial.print(count, DEC);
      Serial.println(F(" positions"));
    break;

    // DATA Memory Read --------
    case ZEPPP_CMD_DATA_MEM_READ:
      if (!serial_parse_match(' ')) return RET_ERR_SPACE_EXPECTED;
      if (!serial_parse_getbyte(&count)) return RET_ERR_HEX_BYTE_EXPECTED;

      load_data_mem (0xff);
      RET_MSG_OK;
      read_and_print_data_mem_words (count);
    break;

    // PGM Memory Read --------
    case ZEPPP_CMD_PGM_MEM_READ:
      if (!serial_parse_match(' ')) return RET_ERR_SPACE_EXPECTED;
      if (!serial_parse_getbyte(&count)) return RET_ERR_HEX_BYTE_EXPECTED;

      load_pgm_mem (0x3fff);
      RET_MSG_OK;
      read_and_print_pgm_mem_words (count);
    break;

    // PGM Memory Write --------
    case ZEPPP_CMD_PGM_MEM_WRITE:
      if (!serial_parse_match(' ')) return RET_ERR_SPACE_EXPECTED;
      if (!serial_parse_getbyte(&writeMode)) return RET_ERR_HEX_BYTE_EXPECTED;
      // So far only two modes are supported for word-based PGM writes: 0 (Use Erase/Pgm cycle) and 1: (Use Program-only cycle)
      if (writeMode > 1) return RET_ERR_OUT_OF_RANGE; 

      ret = read_console_into_word_buffer(&count);
      if (ret != RET_OK) return ret;

      ret = oper_write_pgm_mem_from_buffer(count, writeMode);
      if (ret != RET_OK) {
        return ret;
      } else {
        RET_MSG_OK;
      }
      Serial.println(F("PGM block written"));
    break;

    // PGM Memory Block Write --------
    case ZEPPP_CMD_PGM_MEM_BLOCK_WRITE:
      if (!serial_parse_match(' ')) return RET_ERR_SPACE_EXPECTED;
      if (!serial_parse_getbyte(&writeSize)) return RET_ERR_HEX_BYTE_EXPECTED;
      if (writeSize < 2 || writeSize > pgm_buffer_size()) return RET_ERR_OUT_OF_RANGE;

      // Negative values are error codes.
      ret = read_console_into_word_buffer(&count);
      if (ret != RET_OK) return ret;

      ret = oper_write_pgm_block_from_buffer (count, writeSize);
      if (ret != RET_OK) {
        return ret;
      } else {
        RET_MSG_OK;
      }
      Serial.println(F("PGM block written"));
    break;

    // DATA Memory Write --------
    case ZEPPP_CMD_DATA_MEM_WRITE:
      if (!serial_parse_match(' ')) return RET_ERR_SPACE_EXPECTED;
      if (!serial_parse_getbyte(&writeMode)) return RET_ERR_HEX_BYTE_EXPECTED;
      // So far only two modes are supported for EEPROM  writes: 0 (Use Erase/Pgm cycle) and 1: (Use Program-only cycle with Begin Erase)
      if (writeMode > 1) return RET_ERR_OUT_OF_RANGE; 

      ret = read_console_into_word_buffer(&count);
      if (ret != RET_OK) return ret;

      ret = oper_write_data_from_buffer(count, writeMode);
      if (ret != RET_OK) {
        return ret;
      } else {
        RET_MSG_OK;
      }
      Serial.println(F("DATA block written"));
    break;

    case ZEPPP_CMD_FIRMWARE_INFO:
      RET_MSG_OK;
      Serial.print(F(ZEPPP_NAME_STRING));
      Serial.print(' ');
      Serial.print(F(ZEPPP_VERSION_STRING));
      Serial.print(' ');
      Serial.println(F(ZEPPP_RELDATE_STRING));
    break;

    default:
      return RET_ERR_UNKNOWN_COMMAND;
  }

  return RET_OK;
}

void parse_serial_buff() {
  ZEPPP_RET ret = execute_serial_cmd();

  if (ret != RET_OK) {
    RET_MSG_ERROR;

    switch (ret){
      case RET_ERR_SPACE_EXPECTED:
        Serial.println(F("space (32) char expected before argument"));
        break;

      case RET_ERR_HEX_BYTE_EXPECTED:
        Serial.println(F("HEX byte value expected"));
        break;

      case RET_ERR_HEX_WORD_EXPECTED:
        Serial.println(F("HEX word value expected"));
        break;

      case RET_ERR_NO_MEMORY_AREA_SELECTED:
        Serial.println(F("No device memory area selected"));
        break;

      case RET_ERR_UNKNOWN_COMMAND:
        Serial.print(F("Unknown command: "));
        Serial.println(serialBuffer);
        break;

      case RET_ERR_OUT_OF_RANGE:
        Serial.println(F("Value out of range"));
        break;

      case RET_ERR_VERIFICATION_FAILED:
        Serial.println(F("Verification failed!"));
        break;

      default:
        Serial.print(F("Unknown Error #"));
        Serial.println(ret, DEC);
    }
  }
  serial_reset_buffer();
}

/*############################################################################
 *##                                                                        ##
 *##                                  L O O P                               ##
 *##                                                                        ##
 *############################################################################*/
void loop() {
  byte r;

  if (Serial.available()) {
    r = Serial.read();
    if (r == '\r') {
      parse_serial_buff();
    } else if (inBufferPos < MAX_SERIAL_IN_BUFFER) {
      serialBuffer[inBufferPos] = r;
      inBufferPos++;
      serialBuffer[inBufferPos] = 0;
    }
  }
}
