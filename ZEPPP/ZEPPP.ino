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

/* Pin assignment *******************************/
const int PGM_PIN  = 9;
const int PGC_PIN  = 8;
const int PGD_PIN  = 7;
const int MCLR_PIN = 6;

/* Timing and constants *************************/
#define DELAY_HALFCLOCK_IN_US    2
#define DELAY_SHORT_IN_US        5
#define DELAY_LONG_IN_US         10
#define DELAY_PGM_IN_MS          5
#define DELAY_ERASE_IN_MS        6 // Based on the 16F6XX datasheet
#define DELAY_ERASESETUP_IN_MS   8 // Based on the 16F87X (non-A) datasheet
#define PIC_PGM_ROW              32 
#define MAX_SERIAL_IN_BUFFER     PIC_PGM_ROW*5 + 10 

/* ICSP Commands ********************************/
#define CMD_LOAD_CONFIG          0b000000
#define CMD_LOAD_PGM_MEM_DATA    0b000010
#define CMD_LOAD_DAT_MEM_DATA    0b000011
#define CMD_INCREMENT_ADDRESS    0b000110
#define CMD_READ_PGM_MEM_DATA    0b000100
#define CMD_READ_DAT_MEM_DATA    0b000101
#define CMD_BEGIN_PGMERASE_CYCLE 0b001000
#define CMD_BEGIN_PGMONLY_CYCLE  0b011000
#define CMD_BULK_ERASE_PGM_MEM   0b001001
#define CMD_BULK_ERASE_DAT_MEM   0b001011

/* PIC16F87x Commands */
#define CMD_BULK_ERASE_SETUP_1   0b000001
#define CMD_BULK_ERASE_SETUP_2   0b000111

/* PIC16F88/PIC16F87xA Commands */
#define CMD_CHIP_ERASE           0b011111
#define CMD_BEGIN_ERASE          0b001000 
#define CMD_END_PROGRAMMING      0b010111 

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
  RET_ERR_SPACE_EXPECTED,
  RET_ERR_HEX_BYTE_EXPECTED,
  RET_ERR_HEX_WORD_EXPECTED,
  RET_ERR_VERIFICATION_FAILED,
  RET_ERR_OUT_OF_RANGE,
  RET_ERR_UNKNOWN_COMMAND,
  RET_ERR_NO_MEMORY_AREA_SELECTED,
  RET_OK
} ReturnCode;

/* Serial Buffer Handling ***********************/
char serialBuffer[MAX_SERIAL_IN_BUFFER];
byte inBufferPos = 0;
byte bufferParsingPos = 0;
int  wordBuffer[PIC_PGM_ROW]; 

/* Serial command return strings ****************/
#define RET_MSG_ERROR       Serial.print(F("ER: "))
#define RET_MSG_OK          Serial.print(F("OK: "))

/*############################################################################
 *##                                                                        ##
 *##                 L V P - S P E C I F I C   R O U T I N E S              ##
 *##                                                                        ##
 *############################################################################*/
void lvp_enter_pgm_mode () {
  /* Reset relevant signals */
  pinMode (PGD_PIN, OUTPUT);
  digitalWrite (PGD_PIN, LOW);
  digitalWrite (PGC_PIN, LOW);

  digitalWrite (PGM_PIN, LOW);
  digitalWrite (MCLR_PIN, LOW);

  /* Now we bring them to their default values again */
  digitalWrite (PGM_PIN, HIGH);
  delayMicroseconds(DELAY_SHORT_IN_US);
  digitalWrite (MCLR_PIN, HIGH);
  delayMicroseconds(DELAY_SHORT_IN_US);
}

void lvp_exit_pgm_mode() {
  digitalWrite (MCLR_PIN, LOW);
  pinMode (PGD_PIN, INPUT);
  delayMicroseconds(DELAY_LONG_IN_US);
  digitalWrite (PGM_PIN, LOW);
  digitalWrite (MCLR_PIN, HIGH);
}

void reset_lvp(){
  lvp_enter_pgm_mode();
}
/*############################################################################
 *##                                                                        ##
 *##           I C S P   B I T   B A N G I N G   R O U T I N E S            ##
 *##                                                                        ##
 *############################################################################*/
inline void clock_high() {
  delayMicroseconds(DELAY_HALFCLOCK_IN_US);
  digitalWrite (PGC_PIN, HIGH);
  delayMicroseconds(DELAY_HALFCLOCK_IN_US);
}

inline void clock_low() {
  delayMicroseconds(DELAY_HALFCLOCK_IN_US);
  digitalWrite (PGC_PIN, LOW);
  delayMicroseconds(DELAY_HALFCLOCK_IN_US);
}

void icsp_send_byte (byte data, byte bits) {
  byte d = data;

  pinMode (PGD_PIN, OUTPUT);
  for (byte b = 0; b < bits; b++){
    clock_high();
    digitalWrite (PGD_PIN, d & 1);
    clock_low();
    d >>= 1;
  }
}

word icsp_recv_word () {
  word w = 0;
  word mask = 1;
  pinMode (PGD_PIN, INPUT);
  delayMicroseconds(DELAY_SHORT_IN_US);

  // Data is 14 bits. With a start and stop bit (16 bit total). LSB first
  for (byte b = 0; b < 16; b++){
    clock_high();
    if (digitalRead(PGD_PIN)) w |= mask;
    mask <<= 1;
    clock_low();
  }
  return (w>>1) & 0b11111111111111;
}

void icsp_send_cmd (byte cmd) {
  // Command is 6 bits
  icsp_send_byte (cmd, 6);
  delayMicroseconds(DELAY_SHORT_IN_US);
}

void icsp_load_data (word data) {
  // Data is 14 bits. With a start and stop bit. LSB first
  icsp_send_byte ((byte)((data<<1) & 0xfe), 8);
  icsp_send_byte ((byte)((data>>7) & 0x7f), 8);
  delayMicroseconds(DELAY_LONG_IN_US);
}

/*############################################################################
 *##                                                                        ##
 *##                         I C S P   C O M M A N D S                      ##
 *##                                                                        ##
 *############################################################################*/
word read_pgm_mem () {
  icsp_send_cmd (CMD_READ_PGM_MEM_DATA);
  delayMicroseconds(DELAY_SHORT_IN_US);
  return icsp_recv_word();
}

word read_data_mem () {
  icsp_send_cmd (CMD_READ_DAT_MEM_DATA);
  delayMicroseconds(DELAY_SHORT_IN_US);
  return icsp_recv_word ();
}

void load_pgm_mem (word w) {
  icsp_send_cmd (CMD_LOAD_PGM_MEM_DATA);
  icsp_load_data (w);
}

void load_data_mem (word w) {
  icsp_send_cmd (CMD_LOAD_DAT_MEM_DATA);
  icsp_load_data (w);
}

void load_config_mem (word w) {
  icsp_send_cmd (CMD_LOAD_CONFIG);
  icsp_load_data (w);
}

void increment_addr () {
  icsp_send_cmd (CMD_INCREMENT_ADDRESS);
  delayMicroseconds(DELAY_SHORT_IN_US);
}

void bulk_erase_pgm_mem () {
  icsp_send_cmd (CMD_BULK_ERASE_PGM_MEM);
  delayMicroseconds(DELAY_LONG_IN_US);
}

void bulk_erase_data_mem () {
  icsp_send_cmd (CMD_BULK_ERASE_DAT_MEM);
  delayMicroseconds(DELAY_LONG_IN_US);
}

void chip_erase () {
  icsp_send_cmd (CMD_CHIP_ERASE);
  delay(DELAY_PGM_IN_MS);
}

void program_only_cycle () { 
  icsp_send_cmd (CMD_BEGIN_PGMONLY_CYCLE); 
  delay(DELAY_PGM_IN_MS); 
} 

void erase_and_program_cycle () {
  icsp_send_cmd (CMD_BEGIN_PGMERASE_CYCLE);
  delay(DELAY_PGM_IN_MS);
}

void end_programming(){ 
  icsp_send_cmd (CMD_END_PROGRAMMING); 
} 
 
void begin_erase(){ 
  icsp_send_cmd (CMD_BEGIN_ERASE); 
  delay(DELAY_PGM_IN_MS); 
} 

void bulk_erase_setup_1(){ 
  icsp_send_cmd (CMD_BULK_ERASE_SETUP_1); 
  delay(DELAY_PGM_IN_MS); 
} 

void bulk_erase_setup_2(){ 
  icsp_send_cmd (CMD_BULK_ERASE_SETUP_2);
  delay(DELAY_PGM_IN_MS); 
} 

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

ReturnCode read_console_into_word_buffer (byte *count_dest) { 
  byte count = 0; 
  word w; 
 
  while (serial_parse_match(' ')){ 
    if (count > PIC_PGM_ROW) return RET_ERR_OUT_OF_RANGE; 
    if (!serial_parse_getword(&w, 4)) return RET_ERR_HEX_WORD_EXPECTED; 
    wordBuffer[count] = w; 
    count++; 
  }
  *count_dest = count;
  return RET_OK; 
} 

/*############################################################################
 *##                                                                        ##
 *##                      HIGHER-LEVEL OPERATIONS                           ##
 *##                                                                        ##
 *############################################################################*/
void increase_addr_n(byte n){
  byte x;
  for (x = 0; x < n; x++) increment_addr();
}


ReturnCode oper_erase_pgm_mem (byte eraseMode){
  // So far only two modes are supported for Pgm Erase: 
  // 0: Bulk erase suffices
  // 1: Begin Erase is required after Bulk Erase
  // 2: Load PGM Data + Bulk Setup 1 + 2 sequence
  if (eraseMode > 2) return RET_ERR_OUT_OF_RANGE; 

  reset_lvp();
  if (eraseMode == 0) {
    load_pgm_mem (0x3fff);
    bulk_erase_pgm_mem();
    delay(DELAY_ERASE_IN_MS);
  }else if (eraseMode == 1){
    load_pgm_mem (0x3fff);
    bulk_erase_pgm_mem();
    begin_erase();
  } else {
    load_pgm_mem(0x3fff);
    bulk_erase_setup_1();
    bulk_erase_setup_2();
    begin_erase();
    delay(DELAY_ERASESETUP_IN_MS);
    bulk_erase_setup_1();
    bulk_erase_setup_2();
  }
  reset_lvp();
  return RET_OK;
}

ReturnCode oper_erase_data_mem (byte eraseMode){
  // So far only two modes are supported for Data Erase:
  // 0: (Bulk erase suffices)
  // 1: Begin Erase is required after Bulk Erase
  // 2: Load Data + Bulk Setup 1 + 2 sequence
  if (eraseMode > 2) return RET_ERR_OUT_OF_RANGE; 

  reset_lvp();
  if (eraseMode == 0){
    bulk_erase_data_mem();
    delay(DELAY_ERASE_IN_MS);
  }else if (eraseMode == 1){
    bulk_erase_data_mem();
    begin_erase();
  }else {
    load_data_mem(0x3fff);
    bulk_erase_setup_1();
    bulk_erase_setup_2();
    begin_erase();
    delay(DELAY_ERASESETUP_IN_MS);
    bulk_erase_setup_1();
    bulk_erase_setup_2();
  }
  reset_lvp();
  return RET_OK;
}


ReturnCode oper_chip_erase (byte eraseMode){
  // So far only three modes are supported for Chip Erase:
  // 0: Erase PGM and Data Memory manually using Bulk Erase commands (used by devices like the 6x8A. They don't have a proper CHIP Erase command
  // 1: Use Chip Erase command (11111)
  // 2: Use Load Config + Bulk Erase Setup 1 and 2 sequence
  if (eraseMode > 2) return RET_ERR_OUT_OF_RANGE; 

  reset_lvp();
  if (eraseMode == 0){
    load_config_mem(0x3fff);
    bulk_erase_pgm_mem();
    delay(DELAY_ERASE_IN_MS);
    bulk_erase_data_mem();
    delay(DELAY_ERASE_IN_MS);
  } else if (eraseMode == 1){
    load_config_mem(0x3fff);
    chip_erase();
  } else {
    load_config_mem(0x3fff);
    increase_addr_n(7);
    bulk_erase_setup_1();
    bulk_erase_setup_2();
    begin_erase();
    delay(DELAY_ERASESETUP_IN_MS);
    bulk_erase_setup_1();
    bulk_erase_setup_2();

    // This "chip erase"algorithm will only clear the EEPROM DATA if CP was
    // enabled. If it wasn't, it will just perform a bulk erase of PGM + CONFIG.
    // So to make it fully "chip erase" we will also perform a bulk data erase
    // separately.
    oper_erase_data_mem (2);
  }
  reset_lvp();
  return RET_OK;
}

ReturnCode oper_write_pgm_mem_from_buffer(byte count, byte writeMode){
  byte b;
  word w;
  if (writeMode == 0) {
    for (b = 0; b < count; b++) {
      w = wordBuffer[b];
      load_pgm_mem(w);
      erase_and_program_cycle();
      if (read_pgm_mem() != w) return RET_ERR_VERIFICATION_FAILED;
      increment_addr();
    }
  }else {
    for (b = 0; b < count; b++) {
      w = wordBuffer[b];
      load_pgm_mem(w);
      program_only_cycle();
      end_programming();
      if (read_pgm_mem() != w) return RET_ERR_VERIFICATION_FAILED;
      increment_addr();
    }
  }
  return RET_OK;
}

ReturnCode oper_write_data_from_buffer(byte count, byte writeMode){
  byte b;
  word w;
  if (writeMode == 0){
    for (b = 0; b < count; b++) {
      w = wordBuffer[b];
      load_data_mem(w);
      erase_and_program_cycle();
      if ((read_data_mem() & 0xff) != (w & 0xff)) return RET_ERR_VERIFICATION_FAILED;
      increment_addr();
    }
  } else {
    load_data_mem(0xff);
    for (b = 0; b < count; b ++) { 
      begin_erase(); 
      end_programming();
      w = wordBuffer[b]; 
      load_data_mem(w);
      program_only_cycle();
      end_programming();
      if ((read_data_mem() & 0xff) != (w & 0xff)) return RET_ERR_VERIFICATION_FAILED;
      increment_addr(); 
    }
  }
  return RET_OK;
}

ReturnCode oper_write_pgm_block_from_buffer(byte count, byte writeSize){
  byte b, n;
  word w;

  load_pgm_mem(0x3fff);
  begin_erase(); 
  end_programming(); 
  for (b = 0; b < count; b += writeSize) { 
    for (n = 0; n < writeSize; n++){ 
      if (b + n < count) { 
        w = wordBuffer[b + n]; 
      }else { 
        w = 0x3fff; 
      }
      load_pgm_mem(w); 
      if (n < writeSize - 1) increment_addr(); 
    }
    program_only_cycle(); 
    end_programming(); 
    increment_addr(); 
  }
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

ReturnCode execute_serial_cmd() {
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
      if (writeSize < 2 || writeSize > PIC_PGM_ROW) return RET_ERR_OUT_OF_RANGE;

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
  ReturnCode ret = execute_serial_cmd();

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
