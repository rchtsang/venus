main:
    addi t0 x0 1
    jal t0 func2
func1:
    xori s0 x0 0x01
func2:
    addi s1 x0 0x02