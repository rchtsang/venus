CodeMirror.defineMode("riscv", function(config, parserConfig) {
    function regexFromWords(words, ins) {
        return new RegExp("^(?:" + words.join("|") + ")$", ins);
    }

    var instructions = regexFromWords([
        "addw?",
        "addiw?",
        "andi?",
        "auipc",
        "beq",
        "bgeu?",
        "bltu?",
        "bne",
        "csrrci?",
        "csrrsi?",
        "csrrwi?",
        "ebreak",
        "ecall",
        "fence(\.i)?",
        "jalr?",
        "lbu?",
        "lhu?",
        "lui",
        "lwu?",
        "ld",
        "or",
        "ori",
        "sb",
        "sh",
        "sllw?",
        "slliw?",
        "slti?",
        "sltiu",
        "sltu",
        "sraw?",
        "sraiw?",
        "srlw?",
        "srliw?",
        "subw?",
        "sw",
        "sd",
        "xori?",
        /* Extensions */
        /* Atomic */
        "amoadd.[wd](\.aq\.rl|\.rl\.aq|\.aq|\.rl)?",
        "amoand.[wd](\.aq\.rl|\.rl\.aq|\.aq|\.rl)?",
        "amomax.[wd](\.aq\.rl|\.rl\.aq|\.aq|\.rl)?",
        "amomaxu.[wd](\.aq\.rl|\.rl\.aq|\.aq|\.rl)?",
        "amomin.[wd](\.aq\.rl|\.rl\.aq|\.aq|\.rl)?",
        "amominu.[wd](\.aq\.rl|\.rl\.aq|\.aq|\.rl)?",
        "amoor.[wd](\.aq\.rl|\.rl\.aq|\.aq|\.rl)?",
        "amoswap.[wd](\.aq\.rl|\.rl\.aq|\.aq|\.rl)?",
        "amoxor.[wd](\.aq\.rl|\.rl\.aq|\.aq|\.rl)?",
        "lr.[wd](\.aq\.rl|\.rl\.aq|\.aq|\.rl)?",
        "sc.[wd](\.aq\.rl|\.rl\.aq|\.aq|\.rl)?",
        /* Multiply */
        "mul[wh]?",
        "mulhsu",
        "mulhu",
        "div[wu]?",
        "rem[wu]?",
        "remuw",
        /* Floating */
        "fcvt.d.s",
        "fcvt.s.d",
        "fl[w|d]",
        "fs[w|d]",
        "fadd.[s|d]",
        "fclass.[s|d]",
        "fcvt.l.[s|d]",
        "fcvt.lu.[s|d]",
        "fcvt.[s|d].l",
        "fcvt.[s|d].lu",
        "fcvt.[s|d].w",
        "fcvt.[s|d].wu",
        "fcvt.w.[s|d]",
        "fcvt.wu.[s|d]",
        "fdiv.[s|d]",
        "feq.[s|d]",
        "fle.[s|d]",
        "fmadd.[s|d]",
        "fmax.[s|d]",
        "fmin.[s|d]",
        "fmsub.[s|d]",
        "fmul.[s|d]",
        "fmv.[s|d].x",
        "fmv.x.[s|d]",
        "fnmadd.[s|d]",
        "fnmsub.[s|d]",
        "fsgnj.[s|d]",
        "fsgnjn.[s|d]",
        "fsgnjx.[s|d]",
        "fsqrt.[s|d]",
        "fsub.[s|d]",
        /* Double */
        /* pseudoinstructions */
        "beqz",
        "bgez",
        "bgt",
        "bgtu",
        "bgtz",
        "ble",
        "bleu",
        "blez",
        "bltz",
        "bnez",
        "call",
        "jal",
        "jalr",
        "j",
        "jr",
        "la",
        "lb",
        "lbu",
        "lh",
        "lhu",
        "li",
        "lw",
        "mv",
        "neg",
        "nop",
        "ret",
        "not",
        "ret",
        "sb",
        "seqz",
        "sgtz",
        "sh",
        "sltz",
        "snez",
        "sw",
        "tail",
        /* nonstandard pseudoinstructions */
        "seq",
        "sge",
        "sgeu",
        "sgt",
        "sgtu",
        "sle",
        "sleu",
        "sne"
    ], "i");

    var registers = regexFromWords([
        "x0", "x1", "x2", "x3", "x4", "x5", "x6", "x7", "x8", "x9", "x10", "x11", "x12", "x13", "x14", "x15",
        "x16", "x17", "x18", "x19", "x20", "x21", "x22", "x23", "x24", "x25", "x26", "x27", "x28", "x29", "x30", "x31",
        "zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2", "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
        "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
    ], "");

    var keywords = regexFromWords([
        ".data", ".text", ".globl", ".float", ".double",
        ".asciiz", ".word", ".byte"
    ], "i");

    function normal(stream, state) {
        var ch = stream.next();

        if (ch == "#") {
            stream.skipToEnd();
            return "comment";
        }
        
        if (ch == "\"" || ch == "'") {
            state.cur = string(ch)
            return state.cur(stream, state);
        }
        
        if (/\d/.test(ch)) {
            stream.eatWhile(/[\w.%]/);
            return "number";
        }

        if (/[.\w_]/.test(ch)) {
            stream.eatWhile(/[\w\\\-_.]/);
            return "variable";
        }

        return null;
    }

    function string(quote) {
        return function(stream, state) {
            var escaped = false, ch;
            while ((ch = stream.next()) != null) {
                if (ch == quote && !escaped) break;
                escaped = !escaped && ch == "\\";
            }
            if (!escaped) state.cur = normal;
            return "string";
        };
    }

    return {
        startState: function(basecol) {
            return { basecol: basecol || 0, indentDepth: 0, cur: normal };
        },

        token: function(stream, state) {
            if (stream.eatSpace()) return null;
            var style = state.cur(stream, state);
            var word = stream.current();
            if (style == "variable") {
                if (keywords.test(word)) style = "keyword";
                else if (instructions.test(word)) style = "builtin";
                else if (registers.test(word)) style = "variable-2";
            }
            return style;
        }
    };
});

CodeMirror.registerHelper("lint", "riscv", function (text) {
    var errors = [];
    var parseError = function(err) {
        var line = err.lineNumber;
        errors.push({from: CodeMirror.Pos(line - 1, 0),
                     to: CodeMirror.Pos(line, 0),
                     severity: "error",
                     message: err.message});
    };

    var res = window.venus_main.venus.assembler.Linter.lint(text);
    for (var i = 0; i < res.length; i++) {
        parseError(res[i]);
    }
    return errors;
});
