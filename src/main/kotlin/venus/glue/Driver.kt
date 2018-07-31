package venus.glue

/* ktlint-disable no-wildcard-imports */

import org.w3c.dom.*
import venus.assembler.Assembler
import venus.assembler.AssemblerError
import venus.linker.LinkedProgram
import venus.linker.Linker
import venus.riscv.*
import venus.simulator.*
import venus.simulator.cache.BlockReplacementPolicy
import venus.simulator.cache.CacheError
import venus.simulator.cache.CacheHandler
import venus.simulator.cache.PlacementPolicy
import kotlin.browser.document
import kotlin.browser.window

/* ktlint-enable no-wildcard-imports */

/**
 * The "driver" singleton which can be called from Javascript for all functionality.
 */
@JsName("Driver") object Driver {
    var sim: Simulator = Simulator(LinkedProgram())
    var tr: Tracer = Tracer(sim)
    var cache: CacheHandler = CacheHandler()
    var alignedMemory = false
    var mutableText = true
    private var timer: Int? = null
    val LS = LocalStorage()
    internal var useLS = false
    private var saveInterval: Int? = null
    var p = ""
    private var ready = false

    init {
        console.log("Loading driver...")

        useLS = LS.get("venus") == "true"
        Renderer.renderButton(document.getElementById("sv") as HTMLButtonElement, useLS)

        window.setTimeout(Driver::initTimeout, 5)

        console.log("Finished loading driver!")
    }

    fun initTimeout() {
        loadAll(useLS)
        this.saveInterval = window.setInterval(Driver::saveIntervalFn, 5000)
        this.ready = true
    }

    /**
     * Run when the user clicks the "Simulator" tab.
     *
     * Assembles the text in the editor, and then renders the simulator.
     */
    @JsName("openSimulator") fun openSimulator() {
        if (this.ready) {
            try {
                val success = assemble(getText())
                if (success) {
                    Renderer.renderSimulator(sim)
                    setCacheSettings()
                    Renderer.updateCache(Address(0, MemSize.WORD))
                }
            } catch (e: Throwable) {
                Renderer.renderSimulator(Simulator(LinkedProgram()))
                handleError("Open Simulator", e)
            }
        } else {
            window.setTimeout(Driver::openSimulator, 100)
        }
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
            this.cache.reset()
            sim.state.cache = this.cache
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
            try{
                Renderer.setRunButtonSpinning(true)
                timer = window.setTimeout(Driver::runStart, TIMEOUT_TIME)
                sim.step() // walk past breakpoint
            } catch (e: Throwable) {
                runEnd()
                handleError("RunStart" , e, e is AlignmentError || e is StoreError)
            }
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
        try {
            var cycles = 0
            while (cycles < TIMEOUT_CYCLES) {
                if (sim.isDone() || sim.atBreakpoint()) {
                    runEnd()
                    Renderer.updateAll()
                    return
                }

                sim.step()
                Renderer.updateCache(Address(0, MemSize.WORD))
                cycles++
            }

            timer = window.setTimeout(Driver::runStart, TIMEOUT_TIME)
        } catch (e: Throwable) {
            runEnd()
            handleError("RunStart" , e, e is AlignmentError || e is StoreError)
        }
    }

    internal fun runEnd() {
        Renderer.updatePC(sim.getPC())
        Renderer.setRunButtonSpinning(false)
        timer?.let(window::clearTimeout)
        timer = null
    }

    /**
     * Runs the simulator for one step and renders any updates.
     */
    @JsName("step") fun step() {
        try {
            val diffs = sim.step()
            Renderer.updateFromDiffs(diffs)
            Renderer.updateCache(Address(0, MemSize.WORD))
            Renderer.updateControlButtons()
        } catch (e: Throwable) {
            handleError("step", e, e is AlignmentError || e is StoreError)
        }
    }

    /**
     * Undo the last executed instruction and render any updates.
     */
    @JsName("undo") fun undo() {
        try {
            val diffs = sim.undo()
            Renderer.updateFromDiffs(diffs)
            Renderer.updateControlButtons()
        } catch (e: Throwable) {
            handleError("undo", e, e is AlignmentError || e is StoreError)
        }
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

    @JsName("openCacheTab") fun openCacheTab() {
        Renderer.renderCacheTab()
    }

    @JsName("openSettingsTab") fun openSettingsTab() {
        Renderer.renderSettingsTab()
    }

    @JsName("openGeneralSettingsTab") fun openGeneralSettingsTab() {
        Renderer.renderGeneralSettingsTab()
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
        try {
            Renderer.clearConsole()
            Renderer.printConsole(getInstructionDump())
            val ta = document.getElementById("console-output") as HTMLTextAreaElement
            ta.select()
            val success = document.execCommand("copy")
            if (success) {
                //window.alert("Successfully copied machine code to clipboard")
                console.log("Successfully copied machine code to clipboard")
            }
        } catch (e: Throwable) {
            handleError("dump", e)
        }
    }

    @JsName("verifyText") fun verifyText(input: HTMLInputElement) {
        try {
            if (!currentlyRunning()) {
                try {
                    var i = userStringToInt(input.value)
                    try {
                        MemorySegments.setTextBegin(i)
                        val tabDisplay = document.getElementById("simulator-tab") as HTMLElement
                        if (tabDisplay.classList.contains("is-active")) {
                            this.openSimulator()
                        }
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
        } catch (e: Throwable) {
            handleError("Verify Text", e)
        }
    }

    @JsName("updateCacheBlockSize") fun updateCacheBlockSize(e: HTMLInputElement) {
        val v = e.value.toInt()
        try {
            cache.setCacheBlockSize(v)
        } catch (er: CacheError) {
            Renderer.clearConsole()
            Renderer.printConsole(er.toString())
        }
        e.value = cache.cacheBlockSize().toString()
        setCacheSettings()
    }

    @JsName("updateCacheNumberOfBlocks") fun updateCacheNumberOfBlocks(e: HTMLInputElement) {
        val v = e.value.toInt()
        try {
            cache.setNumberOfBlocks(v)
        } catch (er: CacheError) {
            Renderer.clearConsole()
            Renderer.printConsole(er.toString())
        }
        e.value = cache.numberOfBlocks().toString()
        setCacheSettings()
    }

    @JsName("updateCacheAssociativity") fun updateCacheAssociativity(e: HTMLInputElement) {
        val v = e.value.toInt()
        try {
            cache.setAssociativity(v)
        } catch (er: CacheError) {
            Renderer.clearConsole()
            Renderer.printConsole(er.toString())
        }
        e.value = cache.associativity().toString()
        setCacheSettings()
    }

    @JsName("updateCachePlacementPolicy") fun updateCachePlacementPolicy(e: HTMLSelectElement) {
        if (e.value == "N-Way Set Associative") {
            this.cache.setPlacementPol(PlacementPolicy.NWAY_SET_ASSOCIATIVE)
        } else if (e.value == "Fully Associative") {
            this.cache.setPlacementPol(PlacementPolicy.FULLY_ASSOCIATIVE)
        } else {
            this.cache.setPlacementPol(PlacementPolicy.DIRECT_MAPPING)
            e.value = "Direct Mapped"
        }
        setCacheSettings()
    }

    @JsName("updateCacheReplacementPolicy") fun updateCacheReplacementPolicy(e: HTMLSelectElement) {
        if (e.value == "Random") {
            this.cache.setBlockRepPolicy(BlockReplacementPolicy.RANDOM)
        } else {
            this.cache.setBlockRepPolicy(BlockReplacementPolicy.LRU)
            e.value = "LRU"
        }
        setCacheSettings()
    }

    fun setCacheSettings() {
        val bs = cache.cacheBlockSize().toString()
        val nb = cache.numberOfBlocks().toString()
        val av = cache.associativity().toString()
        val at = cache.placementPol().toMyString()
        val rp = cache.blockRepPolicy().toMyString()
        val cs = cache.cacheSize().toString()
        (document.getElementById("block-size-val") as HTMLInputElement).value = bs
        (document.getElementById("numblocks-val") as HTMLInputElement).value = nb
        (document.getElementById("associativity-val") as HTMLInputElement).value = av
        (document.getElementById("associativity-type") as HTMLSelectElement).value = at
        (document.getElementById("replacementPolicy") as HTMLSelectElement).value = rp
        (document.getElementById("cache-size-val") as HTMLInputElement).value = cs
        Renderer.makeCacheBlocks()
        Renderer.updateCache(Address(0, MemSize.WORD))
    }

    @JsName("trace") fun trace() {
        //@todo make it so trace is better
        Renderer.setNameButtonSpinning("simulator-trace", true)
        Renderer.clearConsole()
        this.loadTraceSettings()
        window.setTimeout(Driver::traceStart, TIMEOUT_TIME)
    }
    private fun loadTraceSettings() {
        tr.format = (document.getElementById("tregPattern") as HTMLTextAreaElement).value
        tr.base = (document.getElementById("tbase-val") as HTMLInputElement).value.toInt()
        tr.totCommands = (document.getElementById("ttot-cmds-val") as HTMLInputElement).value.toInt()
        tr.maxSteps = (document.getElementById("tmaxsteps-val") as HTMLInputElement).value.toInt()
        tr.instFirst = (document.getElementById("tinst-first") as HTMLButtonElement).value == "true"
        tr.twoStage = (document.getElementById("tTwoStage") as HTMLButtonElement).value == "true"
        wordAddressed = (document.getElementById("tPCWAddr") as HTMLButtonElement).value == "true"
    }
    internal fun traceStart() {
        try {
            tr.trace()
            window.setTimeout(Driver::traceString, TIMEOUT_TIME)
        } catch (e : Throwable) {
            handleError("Trace Start", e, e is AlignmentError || e is StoreError)
            Renderer.setNameButtonSpinning("simulator-trace", false)
        }
    }
    internal fun traceString() {
        try {
            tr.traceString()
            Renderer.clearConsole()
            Renderer.printConsole(tr.getString())
        } catch (e: Throwable) {
            handleError("Trace to String", e)
        }
        Renderer.setNameButtonSpinning("simulator-trace", false)
    }

    @JsName("persistentStorage") fun persistentStorage(b: Boolean) {
        this.useLS = b
        if (this.useLS) {
            console.log("Persistent storage has been enabled!")
            this.LS.set("venus", "true")
            this.saveAll()
        } else {
            console.log("Persistent storage has been disabled!")
            this.LS.set("venus", "false")
            //this.LS.reset()
        }
    }

    @JsName("psReset") fun psReset() {
        this.LS.reset()
        console.log("Persistent storage has been reset!")
    }

    fun saveIntervalFn() {
        if (this.useLS) {
            blinkSave(true)
            window.setTimeout(Driver::blinkSave, 500, false)
            saveAll()
        }
    }

    fun blinkSave(b: Boolean) {
        val e = document.getElementById("sv") as HTMLButtonElement
        if (b) {
            e.style.color = "yellow"
        } else {
            e.style.color = ""
        }
    }

    fun saveAll() {
        /*Trace settings*/
        loadTraceSettings()
        this.LS.set("trace_format", tr.format)
        this.LS.set("trace_base", tr.base.toString())
        this.LS.set("trace_totCommands", tr.totCommands.toString())
        this.LS.set("trace_maxSteps", tr.maxSteps.toString())
        this.LS.set("trace_instFirst", tr.instFirst.toString())
        this.LS.set("trace_wordAddressed", wordAddressed.toString())
        this.LS.set("trace_TwoStage", this.tr.twoStage.toString())

        /*Text Begin*/
        this.LS.set("text_begin", MemorySegments.TEXT_BEGIN.toString())

        /*Program*/
        js("codeMirror.save()")
        this.LS.set("prog", getText())

        /*Cache*/
        this.LS.set("cache_associativity", this.cache.associativity().toString())
        this.LS.set("cache_cacheBlockSize", this.cache.cacheBlockSize().toString())
        this.LS.set("cache_numberOfBlocks", this.cache.numberOfBlocks().toString())
        this.LS.set("cache_placementPol", this.cache.placementPol().toString())
        this.LS.set("cache_blockRepPolicy", this.cache.blockRepPolicy().toString())
    }

    /*If b is true, will load stored values else load default values.*/
    fun loadAll(b: Boolean) {
        val t = Tracer(sim)
        /*Trace Settings*/
        var fmt = t.format
        var bs = t.base.toString()
        var totC = t.totCommands.toString()
        var ms = t.maxSteps.toString()
        var instf = t.instFirst.toString()
        var tws = t.twoStage.toString()
        var wa = wordAddressed.toString()

        /*Text begin*/
        var txtStart = Renderer.intToString(MemorySegments.TEXT_BEGIN)

        /*Program*/
        js("codeMirror.save()")
        this.p = getText()
        if (useLS) {
            console.log("Using local storage!")
            /*Trace Settings*/
            fmt = LS.safeget("trace_format", fmt)
            bs = LS.safeget("trace_base", bs)
            totC = LS.safeget("trace_totCommands", totC)
            ms = LS.safeget("trace_maxSteps", ms)
            instf = LS.safeget("trace_instFirst", instf)
            tws = LS.safeget("trace_TwoStage", tws)
            wa = LS.safeget("trace_wordAddressed", wa)

            /*Text Begin*/
            txtStart = LS.safeget("text_begin", txtStart)

            /*Program*/
            this.p = LS.safeget("prog", this.p)

            /*Cache*/
            this.cache.setAssociativity(LS.safeget("cache_associativity", this.cache.associativity().toString()).toInt())
            this.cache.setCacheBlockSize(LS.safeget("cache_cacheBlockSize", this.cache.cacheBlockSize().toString()).toInt())
            this.cache.setNumberOfBlocks(LS.safeget("cache_numberOfBlocks", this.cache.numberOfBlocks().toString()).toInt())
            this.cache.setPlacementPol(PlacementPolicy.valueOf(LS.safeget("cache_placementPol", this.cache.placementPol().toString())))
            this.cache.setBlockRepPolicy(BlockReplacementPolicy.valueOf(LS.safeget("cache_blockRepPolicy", this.cache.blockRepPolicy().toString())))
        } else {
            console.log("Local Storage has been disabled!")
        }
        /*Trace Settings*/
        (document.getElementById("tregPattern") as HTMLTextAreaElement).value = fmt
        (document.getElementById("tbase-val") as HTMLInputElement).value = bs
        (document.getElementById("ttot-cmds-val") as HTMLInputElement).value = totC
        (document.getElementById("tmaxsteps-val") as HTMLInputElement).value = ms
        Renderer.renderButton(document.getElementById("tinst-first") as HTMLButtonElement, instf == "true")
        Renderer.renderButton(document.getElementById("tPCWAddr") as HTMLButtonElement, wa == "true")
        Renderer.renderButton(document.getElementById("tTwoStage") as HTMLButtonElement, tws == "true")

        /*Text Begin*/
        val ts = document.getElementById("text-start") as HTMLInputElement
        ts.value = txtStart
        verifyText(ts)

        /*Program*/
        js("codeMirror.setValue(driver.p)")
        p = ""

        cache.update()
        setCacheSettings()
    }
}
