#ifndef __ZEPPP_ICSP_H
#define __ZEPPP_ICSP_H
#include <Arduino.h>

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


typedef enum {
  ICSP_ERR_VERIFICATION_FAILED,
  ICSP_ERR_OUT_OF_RANGE,
  ICSP_OK
} ICSP_RET;

/* LVP Functions */
void lvp_enter_pgm_mode();
void lvp_exit_pgm_mode();
void reset_lvp();

/* programming buffer */
void set_pgm_buffer(byte ndx, int w);
int get_pgm_buffer(byte ndx);
byte pgm_buffer_size();

/* Low-level programming operations */
word read_pgm_mem ();
word read_data_mem();
void load_pgm_mem (word w);
void load_data_mem (word w);
void load_config_mem (word w);
void increment_addr();
void bulk_erase_pgm_mem();
void bulk_erase_data_mem();
void chip_erase();
void program_only_cycle();
void erase_and_program_cycle();
void end_programming();
void begin_erase();
void bulk_erase_setup_1();
void bulk_erase_setup_2();

/* High-level Programming operations */
void increase_addr_n(byte n);
ICSP_RET oper_erase_pgm_mem (byte eraseMode);
ICSP_RET oper_erase_data_mem (byte eraseMode);
ICSP_RET oper_chip_erase (byte eraseMode);
ICSP_RET oper_write_pgm_mem_from_buffer(byte count, byte writeMode);
ICSP_RET oper_write_data_from_buffer(byte count, byte writeMode);
ICSP_RET oper_write_pgm_block_from_buffer(byte count, byte writeSize);
#endif
