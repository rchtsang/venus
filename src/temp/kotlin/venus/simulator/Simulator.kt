package venus.simulator

/* ktlint-disable no-wildcard-imports */

import venus.glue.Renderer
import venus.linker.LinkedProgram
import venus.riscv.*
import venus.riscv.insts.dsl.Instruction
import venus.simulator.diffs.*

/* ktlint-enable no-wildcard-imports */

/** Right now, this is a loose wrapper around SimulatorState
    Eventually, it will support debugging. */
class Simulator(val linkedProgram: LinkedProgram, var settings: SimulatorSettings = SimulatorSettings()) {
    val state = SimulatorState()
    var maxpc = MemorySegments.TEXT_BEGIN
    private var cycles = 0
    private val history = History()
    private val preInstruction = ArrayList<Diff>()
    private val postInstruction = ArrayList<Diff>()
    private val breakpoints: Array<Boolean>

    init {
        for (inst in linkedProgram.prog.insts) {
            /* TODO: abstract away instruction length */
            state.mem.storeWord(maxpc, inst[InstructionField.ENTIRE])
            maxpc += inst.length
        }

        var dataOffset = MemorySegments.STATIC_BEGIN
        for (datum in linkedProgram.prog.dataSegment) {
            state.mem.storeByte(dataOffset, datum.toInt())
            dataOffset++
        }

        state.pc = linkedProgram.startPC ?: MemorySegments.TEXT_BEGIN
        if (settings.setRegesOnInit) {
            state.setReg(2, MemorySegments.STACK_BEGIN)
            state.setReg(3, MemorySegments.STATIC_BEGIN)
        }

        breakpoints = Array<Boolean>(linkedProgram.prog.insts.size, { false })
    }

    fun isDone(): Boolean = getPC() >= if (settings.ecallOnlyExit) MemorySegments.STATIC_BEGIN else maxpc

    fun run() {
        while (!isDone()) {
            step()
            cycles++
        }
    }

    fun step(): List<Diff> {
        this.branched = false
        this.jumped = false
        this.ecallMsg = ""
        preInstruction.clear()
        postInstruction.clear()
        /* TODO: abstract away instruction length */
        val mcode: MachineCode = getNextInstruction()
        Instruction[mcode].impl32(mcode, this)
        history.add(preInstruction)
        return postInstruction.toList()
    }

    fun undo(): List<Diff> {
        if (!canUndo()) return emptyList() /* TODO: error here? */
        val diffs = history.pop()
        for (diff in diffs) {
            diff(state)
        }
        return diffs
    }

    var ecallMsg = ""
    var branched = false
    var jumped = false
    fun reset() {
        while (this.canUndo()) {
            this.undo()
        }
        this.branched = false
        this.jumped = false
        this.ecallMsg = ""
    }

    fun trace(): Tracer {
        return Tracer(this)
    }

    fun canUndo() = !history.isEmpty()

    fun getReg(id: Int) = state.getReg(id)

    fun setReg(id: Int, v: Int) {
        preInstruction.add(RegisterDiff(id, state.getReg(id)))
        state.setReg(id, v)
        postInstruction.add(RegisterDiff(id, state.getReg(id)))
    }

    fun setRegNoUndo(id: Int, v: Int) {
        state.setReg(id, v)
    }
    fun getFReg(id: Int) = state.getFReg(id)

    fun setFReg(id: Int, v: Float) {
        preInstruction.add(FRegisterDiff(id, state.getFReg(id)))
        state.setFReg(id, v)
        postInstruction.add(FRegisterDiff(id, state.getFReg(id)))
    }

    fun setFRegNoUndo(id: Int, v: Float) {
        state.setFReg(id, v)
    }

    fun toggleBreakpointAt(idx: Int): Boolean {
        breakpoints[idx] = !breakpoints[idx]
        return breakpoints[idx]
    }

    fun atBreakpoint() = breakpoints[(state.pc - MemorySegments.TEXT_BEGIN) / 4]

    fun getPC() = state.pc

    fun setPC(newPC: Int) {
        preInstruction.add(PCDiff(state.pc))
        state.pc = newPC
        postInstruction.add(PCDiff(state.pc))
    }

    fun incrementPC(inc: Int) {
        preInstruction.add(PCDiff(state.pc))
        state.pc += inc
        postInstruction.add(PCDiff(state.pc))
    }

    fun loadByte(addr: Int): Int = state.mem.loadByte(addr)
    fun loadBytewCache(addr: Int): Int {
        if (this.settings.alignedAddress && addr % MemSize.BYTE.size != 0) {
            throw AlignmentError("Address: '" + Renderer.toHex(addr) + "' is not BYTE aligned!")
        }
        preInstruction.add(CacheDiff(Address(addr, MemSize.BYTE)))
        state.cache.read(Address(addr, MemSize.BYTE))
        postInstruction.add(CacheDiff(Address(addr, MemSize.BYTE)))
        return this.loadByte(addr)
    }
    fun loadHalfWord(addr: Int): Int = state.mem.loadHalfWord(addr)
    fun loadHalfWordwCache(addr: Int): Int {
        if (this.settings.alignedAddress && addr % MemSize.HALF.size != 0) {
            throw AlignmentError("Address: '" + Renderer.toHex(addr) + "' is not HALF WORD aligned!")
        }
        preInstruction.add(CacheDiff(Address(addr, MemSize.HALF)))
        state.cache.read(Address(addr, MemSize.HALF))
        postInstruction.add(CacheDiff(Address(addr, MemSize.HALF)))
        return this.loadHalfWord(addr)
    }
    fun loadWord(addr: Int): Int = state.mem.loadWord(addr)
    fun loadWordwCache(addr: Int): Int {
        if (this.settings.alignedAddress && addr % MemSize.WORD.size != 0) {
            throw AlignmentError("Address: '" + Renderer.toHex(addr) + "' is not WORD aligned!")
        }
        preInstruction.add(CacheDiff(Address(addr, MemSize.WORD)))
        state.cache.read(Address(addr, MemSize.WORD))
        postInstruction.add(CacheDiff(Address(addr, MemSize.WORD)))
        return this.loadWord(addr)
    }

    fun storeByte(addr: Int, value: Int) {
        preInstruction.add(MemoryDiff(addr, loadWord(addr)))
        state.mem.storeByte(addr, value)
        postInstruction.add(MemoryDiff(addr, loadWord(addr)))
        this.storeTextOverrideCheck(addr, value, MemSize.BYTE)
    }
    fun storeBytewCache(addr: Int, value: Int) {
        if (this.settings.alignedAddress && addr % MemSize.BYTE.size != 0) {
            throw AlignmentError("Address: '" + Renderer.toHex(addr) + "' is not BYTE aligned!")
        }
        if (!this.settings.mutableText && addr in (MemorySegments.TEXT_BEGIN + 1 - MemSize.BYTE.size)..this.maxpc) {
            throw StoreError("You are attempting to edit the text of the program though the program is set to immutable at address " + Renderer.toHex(addr) + "!")
        }
        preInstruction.add(CacheDiff(Address(addr, MemSize.BYTE)))
        state.cache.write(Address(addr, MemSize.BYTE))
        this.storeByte(addr, value)
        postInstruction.add(CacheDiff(Address(addr, MemSize.BYTE)))
    }

    fun storeHalfWord(addr: Int, value: Int) {
        preInstruction.add(MemoryDiff(addr, loadWord(addr)))
        state.mem.storeHalfWord(addr, value)
        postInstruction.add(MemoryDiff(addr, loadWord(addr)))
        this.storeTextOverrideCheck(addr, value, MemSize.HALF)
    }
    fun storeHalfWordwCache(addr: Int, value: Int) {
        if (this.settings.alignedAddress && addr % MemSize.HALF.size != 0) {
            throw AlignmentError("Address: '" + Renderer.toHex(addr) + "' is not HALF WORD aligned!")
        }
        if (!this.settings.mutableText && addr in (MemorySegments.TEXT_BEGIN + 1 - MemSize.HALF.size)..this.maxpc) {
            throw StoreError("You are attempting to edit the text of the program though the program is set to immutable at address " + Renderer.toHex(addr) + "!")
        }
        preInstruction.add(CacheDiff(Address(addr, MemSize.HALF)))
        state.cache.write(Address(addr, MemSize.HALF))
        this.storeHalfWord(addr, value)
        postInstruction.add(CacheDiff(Address(addr, MemSize.HALF)))
    }

    fun storeWord(addr: Int, value: Int) {
        preInstruction.add(MemoryDiff(addr, loadWord(addr)))
        state.mem.storeWord(addr, value)
        postInstruction.add(MemoryDiff(addr, loadWord(addr)))
        this.storeTextOverrideCheck(addr, value, MemSize.WORD)
    }
    fun storeWordwCache(addr: Int, value: Int) {
        if (this.settings.alignedAddress && addr % MemSize.WORD.size != 0) {
            throw AlignmentError("Address: '" + Renderer.toHex(addr) + "' is not WORD aligned!")
        }
        if (!this.settings.mutableText && addr in (MemorySegments.TEXT_BEGIN + 1 - MemSize.WORD.size)..this.maxpc) {
            throw StoreError("You are attempting to edit the text of the program though the program is set to immutable at address " + Renderer.toHex(addr) + "!")
        }
        preInstruction.add(CacheDiff(Address(addr, MemSize.WORD)))
        state.cache.write(Address(addr, MemSize.WORD))
        this.storeWord(addr, value)
        postInstruction.add(CacheDiff(Address(addr, MemSize.WORD)))
    }

    fun storeTextOverrideCheck(addr: Int, value: Int, size: MemSize) {
        /*Here, we will check if we are writing to memory*/
        if (addr in (MemorySegments.TEXT_BEGIN until this.maxpc) || (addr + size.size - MemSize.BYTE.size) in (MemorySegments.TEXT_BEGIN until this.maxpc)) {
            try {
                val adjAddr = ((addr / MemSize.WORD.size) * MemSize.WORD.size)
                val lowerAddr = adjAddr - MemorySegments.TEXT_BEGIN
                var newInst = this.state.mem.loadWord(adjAddr)
                preInstruction.add(Renderer.updateProgramListing(lowerAddr / MemSize.WORD.size, newInst))
                if ((lowerAddr + MemorySegments.TEXT_BEGIN) != addr && (lowerAddr + MemSize.WORD.size - MemSize.BYTE.size) < this.maxpc) {
                    newInst = this.state.mem.loadWord(adjAddr + MemSize.WORD.size)
                    preInstruction.add(Renderer.updateProgramListing((lowerAddr / MemSize.WORD.size) + 1, newInst))
                }
            } catch (e: Throwable) { /*This is to not error the tests.*/ }
        }
    }

    fun getHeapEnd() = state.heapEnd

    fun addHeapSpace(bytes: Int) {
        preInstruction.add(HeapSpaceDiff(state.heapEnd))
        state.heapEnd += bytes
        postInstruction.add(HeapSpaceDiff(state.heapEnd))
    }

    private fun getInstructionLength(short0: Int): Int {
        if ((short0 and 0b11) != 0b11) {
            return 2
        } else if ((short0 and 0b11111) != 0b11111) {
            return 4
        } else if ((short0 and 0b111111) == 0b011111) {
            return 6
        } else if ((short0 and 0b1111111) == 0b111111) {
            return 8
        } else {
            throw SimulatorError("instruction lengths > 8 not supported")
        }
    }

    fun getNextInstruction(): MachineCode {
        val short0 = loadHalfWord(getPC())
        val length = getInstructionLength(short0)
        if (length != 4) {
            throw SimulatorError("Instruction length != 4 not supported! (This may be due to you overriding parts of the text causing an invalid instruction)")
        }

        val short1 = loadHalfWord(getPC() + 2)

        return MachineCode((short1 shl 16) or short0)
    }
}
