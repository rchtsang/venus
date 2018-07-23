package venus.glue

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import venus.assembler.Assembler
import venus.assembler.AssemblerError
import venus.linker.Linker
import venus.riscv.InstructionField
import venus.riscv.MemorySegments
import venus.riscv.userStringToInt
import venus.simulator.Simulator
import venus.simulator.SimulatorError
import venus.simulator.Tracer
import venus.simulator.wordAddressed
import kotlin.browser.document
import kotlin.browser.window

/**
 * The "driver" singleton which can be called from Javascript for all functionality.
 */
@JsName("Driver") object Driver {
    lateinit var sim: Simulator
    lateinit var tr: Tracer
    private var timer: Int? = null

    init {
        console.log("Loading driver...")
        (document.getElementById("text-start") as HTMLInputElement).value = Renderer.toHex(MemorySegments.TEXT_BEGIN)

        val t = Tracer(sim)
        (document.getElementById("tregPattern") as HTMLTextAreaElement).value = t.format
        (document.getElementById("tmaxsteps-val") as HTMLInputElement).value = t.maxSteps.toString()
    }

    /**
     * Run when the user clicks the "Simulator" tab.
     *
     * Assembles the text in the editor, and then renders the simulator.
     */
    @JsName("openSimulator") fun openSimulator() {
        val success = assemble(getText())
        if (success) Renderer.renderSimulator(sim)
    }

    /**
     * Opens and renders the editor.
     */
    @JsName("openEditor") fun openEditor() {
        runEnd()
        Renderer.renderEditor()
    }

    /**
     * Gets the text from the textarea editor.
     */
    internal fun getText(): String {
        val editor = document.getElementById("asm-editor") as HTMLTextAreaElement
        return editor.value
    }

    /**
     * Assembles and links the program, sets the simulator
     *
     * @param text the assembly code.
     */
    internal fun assemble(text: String): Boolean {
        val (prog, errors) = Assembler.assemble(text)
        if (errors.isNotEmpty()) {
            Renderer.displayError(errors.first())
            return false
        }
        try {
            val linked = Linker.link(listOf(prog))
            sim = Simulator(linked)
            tr = Tracer(sim)
            return true
        } catch (e: AssemblerError) {
            Renderer.displayError(e)
            return false
        }
    }

    /**
     * Runs the simulator until it is done, or until the run button is pressed again.
     */
    @JsName("run") fun run() {
        if (currentlyRunning()) {
            runEnd()
        } else {
            Renderer.setRunButtonSpinning(true)
            timer = window.setTimeout(Driver::runStart, TIMEOUT_TIME)
            sim.step() // walk past breakpoint
        }
    }

    /**
     * Resets the simulator to its initial state
     */
    @JsName("reset") fun reset() {
        openSimulator()
    }

    @JsName("toggleBreakpoint") fun addBreakpoint(idx: Int) {
        val isBreakpoint = sim.toggleBreakpointAt(idx)
        Renderer.renderBreakpointAt(idx, isBreakpoint)
    }

    internal const val TIMEOUT_CYCLES = 100
    internal const val TIMEOUT_TIME = 10
    internal fun runStart() {
        var cycles = 0
        while (cycles < TIMEOUT_CYCLES) {
            if (sim.isDone() || sim.atBreakpoint()) {
                runEnd()
                Renderer.updateAll()
                return
            }

            sim.step()
            cycles++
        }

        timer = window.setTimeout(Driver::runStart, TIMEOUT_TIME)
    }

    internal fun runEnd() {
        Renderer.setRunButtonSpinning(false)
        timer?.let(window::clearTimeout)
        timer = null
    }

    /**
     * Runs the simulator for one step and renders any updates.
     */
    @JsName("step") fun step() {
        val diffs = sim.step()
        Renderer.updateFromDiffs(diffs)
        Renderer.updateControlButtons()
    }

    /**
     * Undo the last executed instruction and render any updates.
     */
    @JsName("undo") fun undo() {
        val diffs = sim.undo()
        Renderer.updateFromDiffs(diffs)
        Renderer.updateControlButtons()
    }

    /**
     * Change to memory tab.
     */
    @JsName("openMemoryTab") fun openMemoryTab() {
        Renderer.renderMemoryTab()
    }

    /**
     * Change to register tab.
     */
    @JsName("openRegisterTab") fun openRegisterTab() {
        Renderer.renderRegisterTab()
    }

    /**
     * Change to trace settings tab
     */
    @JsName("openTracerSettingsTab") fun openTracerSettingsTab() {
        Renderer.renderTracerSettingsTab()
    }

    internal fun currentlyRunning(): Boolean = timer != null

    /**
     * Save a register's value
     */
    @JsName("saveRegister") fun saveRegister(reg: HTMLInputElement, id: Int) {
        if (!currentlyRunning()) {
            try {
                val input = reg.value
                sim.setRegNoUndo(id, userStringToInt(input))
            } catch (e: NumberFormatException) {
                /* do nothing */
            }
        }
        Renderer.updateRegister(id, sim.getReg(id))
    }

    @JsName("updateRegMemDisplay") fun updateRegMemDisplay() {
        Renderer.updateRegMemDisplay()
    }

    @JsName("moveMemoryJump") fun moveMemoryJump() = Renderer.moveMemoryJump()

    @JsName("moveMemoryUp") fun moveMemoryUp() = Renderer.moveMemoryUp()

    @JsName("moveMemoryDown") fun moveMemoryDown() = Renderer.moveMemoryDown()

    fun getInstructionDump(): String {
        val sb = StringBuilder()
        for (i in 0 until sim.linkedProgram.prog.insts.size) {
            val mcode = sim.linkedProgram.prog.insts[i]
            val hexRepresentation = Renderer.toHex(mcode[InstructionField.ENTIRE])
            sb.append(hexRepresentation.removePrefix("0x"))
            sb.append("\n")
        }
        return sb.toString()
    }

    @JsName("dump") fun dump() {
        Renderer.clearConsole()
        Renderer.printConsole(getInstructionDump())
        val ta = document.getElementById("console-output") as HTMLTextAreaElement
        ta.select()
        val success = document.execCommand("copy")
        if (success) {
            window.alert("Successfully copied machine code to clipboard")
        }
    }

    @JsName("verifyText") fun verifyText(input: HTMLInputElement) {
        if (!currentlyRunning()) {
            try {
                var i = userStringToInt(input.value)
                try {
                    MemorySegments.setTextBegin(i)
                    this.openSimulator()
                } catch (e: SimulatorError) {
                    console.warn(e.toString())
                }
            } catch (e: NumberFormatException) {
                /* do nothing */
                console.warn("Unknown number format!")
            }
        } else {
            console.warn("Could not change text because the program is currently running!")
        }
        val ts = Renderer.intToString(MemorySegments.TEXT_BEGIN)
        input.value = ts
    }

    @JsName("trace") fun trace() {
        //@todo make it so trace is better
        Renderer.setNameButtonSpinning("simulator-trace", true)
        Renderer.clearConsole()
        tr.format = (document.getElementById("tregPattern") as HTMLTextAreaElement).value
        tr.base = (document.getElementById("tbase-val") as HTMLInputElement).value.toInt()
        tr.totCommands = (document.getElementById("ttot-cmds-val") as HTMLInputElement).value.toInt()
        tr.maxSteps = (document.getElementById("tmaxsteps-val") as HTMLInputElement).value.toInt()
        tr.instFirst = (document.getElementById("tinst-first") as HTMLButtonElement).value == "true"
        wordAddressed = (document.getElementById("tPCWAddr") as HTMLButtonElement).value == "true"
        window.setTimeout(Driver::traceStart, TIMEOUT_TIME)
    }
    internal fun traceStart() {
        try {
            tr.trace()
            window.setTimeout(Driver::traceString, TIMEOUT_TIME)
        } catch (e : SimulatorError) {
            Renderer.clearConsole()
            Renderer.printConsole(e.toString())
            Renderer.setNameButtonSpinning("simulator-trace", false)
        }
    }
    internal fun traceString() {
        tr.traceString()
        Renderer.clearConsole()
        Renderer.printConsole(tr.getString())
        Renderer.setNameButtonSpinning("simulator-trace", false)
    }
}
