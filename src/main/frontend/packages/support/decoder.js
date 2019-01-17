console.log("Loading decoder...");
decoder = {
  RTYPE_FORMAT : "%inst%\t%rd%, %rs1%, %rs2%",
  ITYPE_FORMAT : "%inst%\t%rd%, %rs1%, %imm%",
  MEM_FORMAT : "%inst%\t%rs2%, %imm%(%rs1%)",
  UTYPE_FORMAT : "%inst%\t%rd%, %imm%",
  BRANCH_FORMAT : "%inst%\t%rs1%, %rs2%, %imm%",
  INST_FORMAT : "%inst%",
  PRL_FORMAT : "%inst%\t%rs1%, %imm%", /*Pseudo Register Label format*/
  PR_FORMAT : "%inst%\t%rd%",
  PL_FORMAT : "%inst%\t%imm%",
  PRR_FORMAT : "%inst%\t%rd%, %rs1%",
  sudoRegs : ["zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2", "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"],
  useSudoRegs : true,
  pseudoDecode : false,
  nopAll : false,
  addLabels : false,

  getRegString : function(i) {
    i = Math.round(i);
    if (i < 0 || i > 31) {
      return null;
    }
    if (decoder.useSudoRegs) {
      return decoder.sudoRegs[i];
    }
    return "x" + i;
  },

  isRegZero : function(reg) {
    return reg == 0 || reg == decoder.getRegString(0);
  },

  handleUnknownInst : function (inst) {
    inst.notvalid = true;
    return "#" + decoder.decimalToHexString(inst.inst) + " #Unknown Instruction!";
  },

  opcode : function (inst) {
      return decoder.extractBits(inst.inst, 0, 6);
  },

  func3 : function (inst) {
    return decoder.extractBits(inst.inst, 12, 14);
  },

  func7 : function (inst) {
    return decoder.extractBits(inst.inst, 25, 31);
  },

  reg : function (r) {
    if (decoder.useSudoRegs) {
      return decoder.sudoRegs[r];
    }
    return "x" + r.toString();
  },

  rd : function (inst) {
    r = decoder.extractBits(inst.inst, 7, 11);
    return decoder.reg(r);
  },

  rs1 : function (inst) {
    r = decoder.extractBits(inst.inst, 15, 19);
    return decoder.reg(r);
  },

  rs2 : function (inst) {
    r = decoder.extractBits(inst.inst, 20, 24);
    return decoder.reg(r);
  },

  loadInst : function (inst) {
    rs2 = decoder.rd(inst);
    rs1 = decoder.rs1(inst);
    imm = decoder.Immediate(inst.inst, "I");
    func3 = decoder.func3(inst);
    switch(func3) {
      case 0:
        ins = "lb";
        break;
      case 1:
        ins = "lh";
        break;
      case 2:
        ins = "lw";
        break;
      case 3:
        ins = "ld";
        break;
      case 4:
        ins = "lbu";
        break;
      case 5:
        ins = "lhu";
        break;
      case 6:
        ins = "lwu";
        break;
      default:
        return decoder.handleUnknownInst(inst);
    }
    inst.name = ins;
    inst.rs1 = rs1;
    inst.rs2 = rs2;
    inst.imm = imm;
    inst.format = decoder.MEM_FORMAT;
    return decoder.MEM_FORMAT.replace("%inst%", ins).replace("%rs2%", rs2).replace("%rs1%", rs1).replace("%imm%", imm);
  },

  fenceInst : function (inst) {
    func3 = decoder.func3(inst);
    switch(func3) {
      case 0:
        ins = "fence";
      case 1:
        ins = "fence.i";
      default:
        return decoder.handleUnknownInst(inst);
    }
    inst.name = ins;
    inst.format = decoder.INST_FORMAT;
    return INST_FORMAT.replace("%inst%", ins);
  },

  itypeArithmeticInst : function (inst) {
    func3 = decoder.func3(inst);
    func7 = decoder.func7(inst);
    imm = decoder.Immediate(inst.inst, "I");
    format = decoder.ITYPE_FORMAT;
    rd = decoder.rd(inst);
    switch(func3) {
        case 0:
          if(decoder.pseudoDecode && decoder.isRegZero(rd)){
            format = decoder.INST_FORMAT;
            ins = "nop";
          } else if (decoder.pseudoDecode && imm == 0) {
            ins = "mv";
            format = decoder.PRR_FORMAT;
          } else {
            ins = "addi";
          }
          break;
        case 1:
          if (func7 == 0x00) {
              ins = "slli";
              imm = decoder.extractBits(imm, 0, 5);
          } else {
              return decoder.handleUnknownInst(inst);
          }
          break;
        case 2:
          ins = "slti";
          break;
        case 3:
          if (decoder.pseudoDecode && imm == 1) {
            ins = "seqz";
            format = decoder.PRR_FORMAT;
          } else {
            ins = "sltiu";
          }
          break;
        case 4:
          if (decoder.pseudoDecode && imm == -1) {
            ins = "not";
            format = decoder.PRR_FORMAT;
          } else {
            ins = "xori";
          }
          break;
        case 5:
          switch(func7) {
              case 0x00:
                  ins = "srli";
                  imm = decoder.extractBits(imm, 0, 5);
                  break;
              case 0x20:
                  ins = "srai";
                  imm = decoder.extractBits(imm, 0, 5);
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        case 6:
          ins = "ori";
          break;
        case 7:
          ins = "andi";
          break;
        default:
          return decoder.handleUnknownInst(inst);
    }
    rs1 = decoder.rs1(inst);
    if (decoder.pseudoDecode && decoder.nopAll && decoder.isRegZero(rd)) {
      format = decoder.INST_FORMAT;
      ins = "nop";
    }
    inst.name = ins;
    inst.rd = rd;
    inst.rs1 = rs1;
    inst.imm = imm;
    inst.format = format;
    return format.replace("%inst%", ins).replace("%rd%", rd).replace("%rs1%", rs1).replace("%imm%", imm);
  },

  uTypeInst : function (inst, mnemonic) {
    rd = decoder.rd(inst);
    imm = decoder.Immediate(inst.inst, "U");
    inst.rd = rd;
    inst.imm = imm;
    inst.name = mnemonic;
    inst.format = decoder.UTYPE_FORMAT;
    return decoder.UTYPE_FORMAT.replace("%inst%", mnemonic).replace("%rd%", rd).replace("%imm%", imm);
  },

  iWordsInst : function (inst) {
    func3 = decoder.func3(inst);
    func7 = decoder.func7(inst);
    imm = decoder.Immediate(inst.inst, "I");
    switch(func3) {
        case 0:
          ins = "addiw";
          break;
        case 1:
          if (func7 == 0x00) {
              ins = "slliw";
              imm = decoder.extractBits(imm, 0, 5);
          } else {
              return decoder.handleUnknownInst(inst);
          }
          break;
        case 5:
          switch(func7) {
              case 0x00:
                  ins = "srliw";
                  imm = decoder.extractBits(imm, 0, 5);
                  break;
              case 0x20:
                  ins = "sraiw";
                  imm = decoder.extractBits(imm, 0, 5);
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        default:
          return decoder.handleUnknownInst(inst);
    }
    rd = decoder.rd(inst);
    rs1 = decoder.rs1(inst);
    inst.name = ins;
    inst.rd = rd;
    inst.rs1 = rs1;
    inst.imm = imm;
    inst.format = decoder.ITYPE_FORMAT;
    return decoder.ITYPE_FORMAT.replace("%inst%", ins).replace("%rd%", rd).replace("%rs1%", rs1).replace("%imm%", imm);
  },

  sTypeInst : function (inst) {
    func3 = decoder.func3(inst);
    switch(func3) {
        case 0:
          ins = "sb";
          break;
        case 1:
          ins = "sh";
          break;
        case 2:
          ins = "sw";
          break;
        case 3:
          ins = "sd";
          break;
        default:
          return decoder.handleUnknownInst(inst);
    }
    imm = decoder.Immediate(inst.inst, "S");
    rs1 = decoder.rs1(inst);
    rs2 = decoder.rs2(inst);
    inst.name = ins;
    inst.rs1 = rs1;
    inst.rs2 = rs2;
    inst.imm = imm;
    inst.format = decoder.MEM_FORMAT;
    return decoder.MEM_FORMAT.replace("%inst%", ins).replace("%rs1%", rs1).replace("%rs2%", rs2).replace("%imm%", imm);
  },

  rTypeInst : function (inst) {
    func3 = decoder.func3(inst);
    func7 = decoder.func7(inst);
    rd = decoder.rd(inst);
    rs1 = decoder.rs1(inst);
    rs2 = decoder.rs2(inst);
    format = decoder.RTYPE_FORMAT;
    switch(func3) {
        case 0:
          switch(func7) {
              case 0x00:
                  if (decoder.pseudoDecode && (decoder.isRegZero(rs1) || decoder.isRegZero(rs2))){
                    if (!decoder.isRegZero(rs2)) {
                      rs1 = rs2;
                    }
                    format = decoder.PRR_FORMAT;
                    ins = "mv";
                  } else {
                    ins = "add";
                  }
                  break;
              case 0x01:
                  ins = "mul";
                  break;
              case 0x20:
                  if (decoder.pseudoDecode && decoder.isRegZero(rs1)) {
                    rs1 = rs2;
                    format = decoder.PRR_FORMAT;
                    ins = "neg";
                  } else {
                    ins = "sub";
                  }
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        case 1:
          switch(func7) {
              case 0x00:
                  ins = "sll";
                  break;
              case 0x01:
                  ins = "mulh";
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        case 2:
          switch(func7) {
              case 0x00:
                  ins = "slt";
                  break;
              case 0x01:
                  ins = "mulhsu";
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        case 3:
          switch(func7) {
              case 0x00:
                  if(decoder.pseudoDecode && decoder.isRegZero(rs1)){
                    ins = "snez";
                    format = decoder.PRR_FORMAT;
                    rs1 = rs2;
                  } else {
                    ins = "sltu";
                  }
                  break;
              case 0x01:
                  ins = "mulhu";
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        case 4:
          switch(func7) {
              case 0x00:
                  ins = "xor";
                  break;
              case 0x01:
                  ins = "div";
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        case 5:
          switch(func7) {
              case 0x00:
                  ins = "srl";
                  break;
              case 0x01:
                  ins = "divu";
                  break;
              case 0x20:
                  ins = "sra";
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        case 6:
          switch(func7) {
              case 0x00:
                  ins = "or";
                  break;
              case 0x01:
                  ins = "rem";
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        case 7:
          switch(func7) {
              case 0x00:
                  ins = "and";
                  break;
              case 0x01:
                  ins = "remu";
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        default:
          return decoder.handleUnknownInst(inst);
    }
    inst.name = ins;
    inst.rd = rd;
    inst.rs1 = rs1;
    inst.rs2 = rs2;
    inst.format = format;
    return format.replace("%inst%", ins).replace("%rs1%", rs1).replace("%rs2%", rs2).replace("%rd%", rd);
  },

  rWordsInst : function (inst) {
    func3 = decoder.func3(inst);
    func7 = decoder.func7(inst);
    switch(func3) {
        case 0:
          switch(func7) {
              case 0x00:
                  ins = "addw";
                  break;
              case 0x20:
                  ins = "subw";
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        case 1:
          switch(func7) {
              case 0x00:
                  ins = "sllw";
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        case 5:
          switch(func7) {
              case 0x00:
                  ins = "srlw";
                  break;
              case 0x20:
                  ins = "sraw";
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
        default:
          return decoder.handleUnknownInst(inst);
    }
    rd = decoder.rd(inst);
    rs1 = decoder.rs1(inst);
    rs2 = decoder.rs2(inst);
    inst.name = ins;
    inst.rd = rd;
    inst.rs1 = rs1;
    inst.rs2 = rs2;
    inst.format = decoder.RTYPE_FORMAT;
    return decoder.RTYPE_FORMAT.replace("%inst%", ins).replace("%rs1%", rs1).replace("%rs2%", rs2).replace("%rd%", rd);
  },

  branchInst : function (inst) {
    rs1 = decoder.rs1(inst);
    rs2 = decoder.rs2(inst);
    func3 = decoder.func3(inst);
    format = decoder.BRANCH_FORMAT;
    switch(func3) {
      case 0:
          if (decoder.pseudoDecode && (decoder.isRegZero(rs1)|| decoder.isRegZero(rs2))) {
            if (!decoder.isRegZero(rs2)) {
              rs1 = rs2;
            }
            ins = "beqz";
            format = decoder.PRL_FORMAT;
          } else {
            ins = "beq";
          }
          break;
      case 1:
          if (decoder.pseudoDecode && (decoder.isRegZero(rs1)|| decoder.isRegZero(rs2))) {
            if (!decoder.isRegZero(rs2)) {
              rs1 = rs2;
            }
            ins = "bnez";
            format = decoder.PRL_FORMAT;
          } else {
            ins = "bne";
          }
          break;
      case 4:
          ins = "blt";
          break;
      case 5:
          ins = "bge";
          break;
      case 6:
          ins = "bltu";
          break;
      case 7:
          ins = "bgeu";
          break;
      default:
          return decoder.handleUnknownInst(inst);
    }
    imm = decoder.Immediate(inst.inst, "SB");
    inst.name = ins;
    inst.rs1 = rs1;
    inst.rs2 = rs2;
    inst.imm = imm;
    inst.format = format;
    return format.replace("%inst%", ins).replace("%rs1%", rs1).replace("%rs2%", rs2).replace("%imm%", imm);
  },

  jalrInst : function (inst) {
    func3 = decoder.func3(inst);
    switch(func3) {
        case 0:
          ins = "jalr";
          break;
        default:
          return decoder.handleUnknownInst(inst);
    }
    imm = decoder.Immediate(inst.inst, "I");
    format = decoder.ITYPE_FORMAT;
    rd = decoder.rd(inst);
    rs1 = decoder.rs1(inst);
    if (decoder.pseudoDecode && imm == 0 && decoder.isRegZero(rd)) {
      if (["x1", "ra"].includes(rs1)) {
        format = decoder.INST_FORMAT;
        ins = "ret";
      } else {
        format = decoder.PR_FORMAT;
        ins = "jr";
        rd = rs1;
      }
    }
    inst.name = ins;
    inst.rd = rd;
    inst.rs1 = rs1;
    inst.imm = imm;
    inst.format = format;
    return format.replace("%inst%", ins).replace("%rd%", rd).replace("%rs1%", rs1).replace("%imm%", imm); 
  },

  ujTypeInst : function (inst) {
    ins = "jal";
    format = decoder.UTYPE_FORMAT;
    imm = decoder.Immediate(inst.inst, "UJ");
    rd = decoder.rd(inst);
    if (decoder.pseudoDecode && decoder.isRegZero(rd)) {
      format = decoder.PL_FORMAT;
      ins = "j";
    }
    inst.name = ins;
    inst.rd = rd;
    inst.imm = imm;
    inst.format = format;
    return format.replace("%inst%", ins).replace("%rd%", rd).replace("%imm%", imm);
  },

  systemInst : function (inst) {
    func3 = decoder.func3(inst);
    switch(func3) {
      case 0:
          imm = decoder.Immediate(inst.inst, "I");
          switch(imm) {
              case 0x000:
                  ins = "ecall";
                  break;
              case 0x001:
                  ins = "ebreak";
                  break;
              default:
                  return decoder.handleUnknownInst(inst);
          }
          break;
      case 1:
          ins = "CSRRW";
          break;
      case 2:
          ins = "CSRRS";
          break;
      case 3:
          ins = "CSRRC";
          break;
      case 5:
          ins = "CSRRWI";
          break;
      case 6:
          ins = "CSRRSI";
          break;
      case 7:
          ins = "CSRRCI";
          break;
      default:
          return decoder.handleUnknownInst(inst);
    }
    inst.name = ins;
    inst.format = decoder.INST_FORMAT;
    return decoder.INST_FORMAT.replace("%inst%", ins);
  },

  labeler : function(instList) {
    if (!decoder.addLabels) {
      return;
    }
    //return instList;
    //labelInsts = [];
    labelLocs = {};
    for (i in instList) {
      inst = instList[i];
      if (inst && !inst.notvalid && ["beq", "bge", "bgeu", "blt", "bltu", "bne", "jal", "beqz", "bnez", "j"].includes(inst.name)) {
        inst.addr = i * 4;
        //labelInsts.push(inst);
        inst.format = inst.format.replace("%imm%", "%ilabel% #offset=%imm%");
        var offset = inst.imm + inst.addr;
        inst.ilabel = "L" + offset;
        labelLocs[offset] = inst.ilabel;
      }
      /*Label format will just be L#. Maybe adj in future. Would have to rewrite and add to this to make that work.*/
    }
    for (adr of Object.keys(labelLocs)) {
      realAdr = adr / 4;
      var ins = instList[realAdr];
      ins.label = labelLocs[adr];
      ins.format = "%label%: " + ins.format
    }
  },

  multiPseudo : function(allinsts) {
    if (!decoder.pseudoDecode) {
      return allinsts;
    }
    var inst = allinsts.pop();
    var previnst = allinsts.pop();
    if (typeof previnst !== "undefined") {
       allinsts.push(previnst);
    }
    if (typeof inst !== "undefined") {
       allinsts.push(inst);
    }
    if (!previnst || !["auipc", "lui"].includes(previnst.name)) {
      return allinsts;
    }
    switch(previnst.name) {
      case "auipc":
        if (previnst.rd != inst.rd || inst.rs1 != inst.rd || inst.name != "addi") {
          return allinsts;
        }
        ins = "la";
        rd = inst.rd;
        imm = (previnst.imm << 12) | inst.imm;
        break;
      case "lui":
        if (previnst.rd != inst.rd || inst.rs1 != inst.rd || inst.name != "addi") {
          return allinsts;
        }
        ins = "li";
        rd = inst.rd;
        imm = (previnst.imm << 12) | inst.imm;
        break;
      default:
        return allinsts;
    }
    var i = new Instruction(0);
    i.rd = rd;
    i.name = ins;
    i.imm = imm;
    i.decoded = decoder.PRL_FORMAT.replace("%inst%", ins).replace("%rs1%", rd).replace("%imm%", imm);
    allinsts.pop();
    allinsts.pop();
    allinsts.push(i);
    return allinsts;
  },

  Immediate : function (inst, type) {
    switch(type.toUpperCase()) {
      case "I":
        imm = decoder.extractBits(inst, 20, 31);
        imm = decoder.parseTwos(imm, 12);
        return imm;
      case "S":
        imm40 = decoder.extractBits(inst, 7, 11).toString(2);
        imm115 = decoder.extractBits(inst, 25, 31).toString(2);
        imm = "0".repeat(7 - imm115.length) + imm115 + "0".repeat(5 - imm40.length) + imm40;
        imm = decoder.parseTwos(parseInt(imm, 2), imm.length);
        return imm;
      case "SB":
        imm11 = decoder.extractBits(inst, 7, 7).toString(2);
        imm41 = decoder.extractBits(inst, 8, 11).toString(2);
        imm105 = decoder.extractBits(inst, 25, 30).toString(2);
        imm12 = decoder.extractBits(inst, 31, 31).toString(2);
        imm = imm12 + imm11 + "0".repeat(6 - imm105.length) + imm105 + "0".repeat(4 - imm41.length) + imm41 + "0";
        imm = decoder.parseTwos(parseInt(imm, 2), imm.length);
        return imm;
      case "U":
        imm = decoder.extractBits(inst, 12, 31);
        //Utype is not twos complement!
        //imm = decoder.parseTwos(imm, 20);
        return imm;
      case "UJ":
        imm1912 = decoder.extractBits(inst, 12, 19).toString(2);
        imm11 = decoder.extractBits(inst, 20, 20).toString(2);
        imm101 = decoder.extractBits(inst, 21, 30).toString(2);
        imm20 = decoder.extractBits(inst, 31, 31).toString(2);
        imm = imm20 + "0".repeat(8 - imm1912.length) + imm1912 + imm11 + "0".repeat(10 - imm101.length) + imm101 + "0";
        imm = decoder.parseTwos(parseInt(imm, 2), imm.length);
        return imm;
    }
    console.log("Unknown inst type! Cannot parse immediate.");
    return null;
  },

  /*
    Takes in decimal number and number of bits and converts it into twos complement number.
  */
  parseTwos : function(number,  nbits) {
    nbits--;
    sign = decoder.extractBits(number, nbits, nbits);
    val = sign * (-(2**(nbits)));
    return val + decoder.extractBits(number, 0, nbits - 1);
  },

  /*
      This extracts bits inclusively. 
      Ex: extractBits(0b011101010, 1, 5) == 0b10101
  */
  extractBits : function (word, start, end) {
    return (word >> start) & ~(~0 << (end - start + 1));
  },

  extendZeros : function (s) {
    var z = 8-s.length;
    for (var k = 0; k < z; k++) {
      s = "0" + s;
    }
    return s;
  },

  getBaseLog : function (x, y) {
    return Math.log(y) / Math.log(x);
  },

  decimalToHexString : function (number)
  {
      if (number < 0)
      {
          number = 0xFFFFFFFF + number + 1;
      }
      n = number.toString(16).toUpperCase().substring(0,8);
      if (n.length < 8) {
        n = "0".repeat(8 - n.length) + n;
      }
      n = "0x" + n;
      return n;
  },
}

decoder.opcodeMap = {
    0x03 : decoder.loadInst,
    0x0F : decoder.fenceInst,
    0x13 : decoder.itypeArithmeticInst,
    0x17 : function(inst){
              return decoder.uTypeInst(inst, "auipc");
            },
    0x1B : decoder.iWordsInst,
    0x23 : decoder.sTypeInst,
    0x33 : decoder.rTypeInst,
    0x37 : function(inst){
              return decoder.uTypeInst(inst, "lui");
            },
    0x3B : decoder.rWordsInst,
    0x63 : decoder.branchInst,
    0x67 : decoder.jalrInst,
    0x6F : decoder.ujTypeInst,
    0x73 : decoder.systemInst,
}

var Instruction = class Instruction {
  constructor(hex) {
    this.opcode = NaN;
    this.rd = NaN;
    this.rs1 = NaN;
    this.rs2 = NaN;
    this.func3 = NaN;
    this.func7 = NaN;
    this.imm = NaN;
    this.label = "";
    this.format = "";
    this.name = "";
    this.decoded = "";
    this.inst = parseInt(hex);
    this.notvalid = this.inst != hex;
    if (this.notvalid) {
      this.inst = hex;
      if (this.inst == "") {
        this.decoded = "";
      } else {
        this.decoded = "#" + this.inst + " #Unknown Instruction!";
      }
      return;
    }
    //this part actually decodes inst
    this.opcode = decoder.opcode(this);
    var process = decoder.opcodeMap[this.opcode];
    if (!process) {
      this.decoded = decoder.handleUnknownInst(this);
      return;
    }
    this.decoded = process(this);
  }

  decode() {
    //Will add support for combining data here.
    if (!this.notvalid) {
      this.decoded = this.format.replace("%inst%", this.name).replace("%rd%", this.rd).replace("%rs1%", this.rs1).replace("%rs2%", this.rs2).replace("%imm%", this.imm).replace("%label%", this.label).replace("%ilabel%", this.ilabel);
    }
    return this.decoded;
  }
}


console.log("Decoder Loaded!");