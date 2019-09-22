#define c_print_int 1
#define c_print_str 4
#define c_sbrk 9
#define c_exit 10
#define c_print_char 11
#define c_openFile 13
#define c_readFile 14
#define c_writeFile 15
#define c_closeFile 16
#define c_exit2 17
#define c_fflush 18
#define c_feof 19
#define c_ferror 20
#define c_printHex 34
.data
newline: .string "\n"
.text
main: #a0 = argc a1 = argv
	mv s0 a0
    mv s1 a1
    la s2 newline
loop:
	beqz s0 end
    lw a1 0(s1)
    jal print_str
    mv a1 s2
    jal print_str
    addi s0 s0 -1
    addi s1 s1 4
    j loop
end:
    jal exit
# End main

# void print_int(int a1)
# Prints the integer in a1.
# args:
# 	a1 = integer to print
# return:
#	void
.globl print_int
print_int:
    li a0 c_print_int
    ecall
    jr ra

# void print_str(char *a1)
# Prints the null-terminated string at address a1.
# args:
# 	a1 = address of the string you want printed.
# return:
#	void
.globl print_str
print_str:
	li a0 c_print_str
    ecall
    jr ra
    
# void *sbrk(int a1)
# Allocates a1 bytes onto the heap.
# args:
# 	a1 = Number of bytes you want to allocate.
# return:
#	a0 = Pointer to the start of the allocated memory
.globl sbrk
sbrk:
	li a0 c_sbrk
    ecall
    jr ra
    
# void noreturn exit()
# Exits the program with a zero exit code.
# args:
# 	None
# return:
#	No Return
.globl exit
exit:
	li a0 c_exit
    ecall
    
# void print_char(char a1)
# Prints the ASCII character in a1 to the console.
# args:
# 	a1 = character to print
# return:
#	void
.globl print_char
print_char:
	li a0 c_print_char
    ecall
    jr ra
    
# int fopen(char *a1, int a2)
# Opens file with name a1 with permissions a2.
# args:
# 	a1 = filepath
#	a2 = permissions (0, 1, 2, 3, 4, 5 = r, w, a, r+, w+, a+)
# return:
#	a0 = file descriptor
.globl fopen
fopen:
	li a0 c_openFile
    ecall
    jr ra
    
# int fread(int a1, void *a2, size_t a3)
# Reads a3 bytes of the file into the buffer a2.
# args:
# 	a1 = file descriptor
#	a2 = pointer to the buffer you want to write the read bytes to.
#	a3 = Number of bytes to be read.
# return:
#	a0 = Number of bytes actually read.
.globl fread
fread:
	li a0 c_readFile
    ecall
    jr ra
    
# int fwrite(int a1, void *a2, size_t a3, size_t a4)
# Writes a3 * a4 bytes from the buffer in a2 to the file descriptor a1.
# args:
# 	a1 = file descriptor
#	a2 = Buffer to read from
#   a3 = Number of items to read from the buffer.
#   a4 = Size of each item in the buffer.
# return:
#	a0 = Number of bytes writen. If this is less than a3, it is either an error or EOF. You will also need to still flush the fd.
.globl fwrite
fwrite:
	li a0 c_writeFile
    ecall
    jr ra
    
# int fclose(int a1)
# Closes the file descriptor a1.
# args:
# 	a1 = file descriptor
# return:
#	a0 = 0 on success, and EOF (-1) otherwise.
.globl fclose
fclose:
	li a0 c_closeFile
    ecall
    jr ra
    
# void noreturn exit2(int a1)
# Exits the program with error code a1.
# args:
# 	a1 = Exit code.
# return:
#	This program does not return.
.globl exit2
exit2:
	li a0 c_exit2
    ecall
    jr ra
    
# int fflush(int a1)
# Flushes the data to the filesystem.
# args:
# 	a1 = file descriptor
# return:
#	a0 = 0 on success, and EOF (-1) otherwise.
.globl fflush
fflush:
	li a0 c_fflush
    ecall
    jr ra
    
# int ferror(int a1)
# Returns a nonzero value if the file stream has errors, otherwise it returns 0.
# args:
# 	a1 = file descriptor
# return:
#	a0 = Nonzero falue if the end of file is reached. 0 Otherwise.
.globl ferror
ferror:
	li a0 c_ferror
    ecall
    jr ra
    
# void print_hex(int a1)
# 
# args:
# 	a1 = The word which will be printed as a hex value.
# return:
#	void
.globl print_hex
print_hex:
	li a0 c_printHex
    ecall
    jr ra

