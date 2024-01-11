#include "ZEPPP_icsp.h"

static int  wordBuffer[PIC_PGM_ROW]; 
/*############################################################################
 *##                                                                        ##
 *##                   P R O G R A M M I N G   B U F F E R                  ##
 *##                                                                        ##
 *############################################################################*/
void set_pgm_buffer(byte ndx, int w){
  wordBuffer[ndx % PIC_PGM_ROW] = w;
}

int get_pgm_buffer(byte ndx) {
  return wordBuffer[ndx % PIC_PGM_ROW];
}

byte pgm_buffer_size() {
  return PIC_PGM_ROW;
}

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
 *##                      HIGHER-LEVEL OPERATIONS                           ##
 *##                                                                        ##
 *############################################################################*/
void increase_addr_n(byte n){
  byte x;
  for (x = 0; x < n; x++) increment_addr();
}

ICSP_RET oper_erase_pgm_mem (byte eraseMode){
  // So far only two modes are supported for Pgm Erase: 
  // 0: Bulk erase suffices
  // 1: Begin Erase is required after Bulk Erase
  // 2: Load PGM Data + Bulk Setup 1 + 2 sequence
  if (eraseMode > 2) return ICSP_ERR_OUT_OF_RANGE; 

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
  return ICSP_OK;
}

ICSP_RET oper_erase_data_mem (byte eraseMode){
  // So far only two modes are supported for Data Erase:
  // 0: (Bulk erase suffices)
  // 1: Begin Erase is required after Bulk Erase
  // 2: Load Data + Bulk Setup 1 + 2 sequence
  if (eraseMode > 2) return ICSP_ERR_OUT_OF_RANGE; 

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
  return ICSP_OK;
}


ICSP_RET oper_chip_erase (byte eraseMode){
  // So far only three modes are supported for Chip Erase:
  // 0: Erase PGM and Data Memory manually using Bulk Erase commands (used by devices like the 6x8A. They don't have a proper CHIP Erase command
  // 1: Use Chip Erase command (11111)
  // 2: Use Load Config + Bulk Erase Setup 1 and 2 sequence
  if (eraseMode > 2) return ICSP_ERR_OUT_OF_RANGE; 

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
  return ICSP_OK;
}

ICSP_RET oper_write_pgm_mem_from_buffer(byte count, byte writeMode){
  byte b;
  word w;
  if (writeMode == 0) {
    for (b = 0; b < count; b++) {
      w = wordBuffer[b];
      load_pgm_mem(w);
      erase_and_program_cycle();
      if (read_pgm_mem() != w) return ICSP_ERR_VERIFICATION_FAILED;
      increment_addr();
    }
  }else {
    for (b = 0; b < count; b++) {
      w = wordBuffer[b];
      load_pgm_mem(w);
      program_only_cycle();
      end_programming();
      if (read_pgm_mem() != w) return ICSP_ERR_VERIFICATION_FAILED;
      increment_addr();
    }
  }
  return ICSP_OK;
}

ICSP_RET oper_write_data_from_buffer(byte count, byte writeMode){
  byte b;
  word w;
  if (writeMode == 0){
    for (b = 0; b < count; b++) {
      w = wordBuffer[b];
      load_data_mem(w);
      erase_and_program_cycle();
      if ((read_data_mem() & 0xff) != (w & 0xff)) return ICSP_ERR_VERIFICATION_FAILED;
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
      if ((read_data_mem() & 0xff) != (w & 0xff)) return ICSP_ERR_VERIFICATION_FAILED;
      increment_addr(); 
    }
  }
  return ICSP_OK;
}

ICSP_RET oper_write_pgm_block_from_buffer(byte count, byte writeSize){
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
  return ICSP_OK;
}
