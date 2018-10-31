package venus.glue

/* ktlint-disable no-wildcard-imports */

import venus.assembler.Assembler
import venus.assembler.AssemblerError
import venus.glue.jvm.JVMInitInstructions
import venus.glue.vfs.VirtualFileSystem
import venus.linker.LinkedProgram
import venus.linker.Linker
import venus.riscv.*
import venus.simulator.*
import venus.simulator.cache.BlockReplacementPolicy
import venus.simulator.cache.CacheError
import venus.simulator.cache.CacheHandler
import venus.simulator.cache.PlacementPolicy
import java.io.File
import java.io.FileNotFoundException
import kotlin.system.exitProcess

/* ktlint-enable no-wildcard-imports */

/**
 * The "driver" singleton which can be called from Javascript for all functionality.
 */
object Driver {
    var VFS = VirtualFileSystem("v")

    var sim: Simulator = Simulator(LinkedProgram(), VFS)
    var tr: Tracer = Tracer(sim)
    val mainCache: CacheHandler = CacheHandler(1)

    var cache: CacheHandler = mainCache
    var cacheLevels: ArrayList<CacheHandler> = arrayListOf(mainCache)
    val simSettings = SimulatorSettings()

    var p = ""
    private var ready = false
    var FReginputAsFloat = true
    var debug = false


    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 1) {
            println("This takes in one argument: the file you want to run!")
            exitProcess(-1)
        }

        JVMInitInstructions()

        val filename = args[0]

        val assemblyProgramText = try {
            readFileDirectlyAsText(args[0])
        } catch (e: FileNotFoundException) {
            println("Could not find the file: " + filename)
            exitProcess(1)
        }

        if (!assemble(assemblyProgramText)) {
            exitProcess(-1)
        }
        try {
            sim.run()
            println() // This is to end on a new line regardless of the output.
        } catch (e: Exception) {
            println(e)
            exitProcess(-1)
        }
    }

    fun readFileDirectlyAsText(fileName: String): String
            = File(fileName).readText(Charsets.UTF_8)


    /**
     * Assembles and links the program, sets the simulator
     *
     * @param text the assembly code.
     */
    internal fun assemble(text: String): Boolean {
        val (prog, errors, warnings) = Assembler.assemble(text)
        if (warnings.isNotEmpty()) {
            for (warning in warnings) {
                Renderer.displayWarning(warning)
            }
        }
        if (errors.isNotEmpty()) {
            println("Could not assemble program!\nHere are the errors which were produced:")
            for (error in errors) {
                Renderer.displayError(error)
            }
            return false
        }
        try {
            val linked = Linker.link(listOf(prog))
            loadSim(linked)
            return true
        } catch (e: AssemblerError) {
            Renderer.displayError(e)
            return false
        }
    }

    fun loadSim(linked: LinkedProgram) {
        sim = Simulator(linked, VFS, simSettings)
        mainCache.reset()
        sim.state.cache = mainCache
        tr = Tracer(sim)
    }

    internal const val TIMEOUT_CYCLES = 100

    fun destrictiveGetSimOut(): String {
        val tmp = sim.stdout
        sim.stdout = ""
        return tmp
    }



    fun getInstructionDump(): String {
        val sb = StringBuilder()
        for (i in 0 until sim.linkedProgram.prog.insts.size) {
            val mcode = sim.linkedProgram.prog.insts[i]
            val hexRepresentation = Renderer.toHex(mcode[InstructionField.ENTIRE])
            sb.append(hexRepresentation/*.removePrefix("0x")*/)
            sb.append("\n")
        }
        return sb.toString()
    }

    fun dump() {
        try {
            Renderer.printConsole(getInstructionDump())
        } catch (e: Throwable) {
            handleError("dump", e)
        }
    }

    fun setOnlyEcallExit(b: Boolean) {
        simSettings.ecallOnlyExit = b
    }

    fun setSetRegsOnInit(b: Boolean) {
        simSettings.setRegesOnInit = b
    }

    fun setNumberOfCacheLevels(i: Int) {
        if (i == cacheLevels.size) {
            return
        }
        if (cacheLevels.size < i) {
            val lastCache = cacheLevels[cacheLevels.size - 1]
            while (cacheLevels.size < i) {
                val newCache = CacheHandler(cacheLevels.size + 1)
                cacheLevels[cacheLevels.size - 1].nextLevelCacheHandler = newCache
                cacheLevels.add(newCache)
            }
            lastCache.update()
        } else if (cacheLevels.size > i) {
            while (cacheLevels.size > i) {
                val prevCacheIndex = cacheLevels.size - 1
                val prevCache = cacheLevels[prevCacheIndex]
                cacheLevels.removeAt(prevCacheIndex)
                val lastCache = cacheLevels[cacheLevels.size - 1]
                lastCache.nextLevelCacheHandler = null
                if (cache.cacheLevel == prevCache.cacheLevel) {
                    cache = lastCache
                }
            }
        }
    }

    fun setCacheEnabled(enabled: Boolean) {
        cache.attach(enabled)
    }

    fun updateCacheLevel(value: String) {
        try {
            val level = value.removePrefix("L").toInt()
            updateCacheLvl(level)
        } catch (e: NumberFormatException) {
            handleError("Update Cache Level (NFE)", e, true)
        }
    }

    fun updateCacheLvl(level: Int) {
        if (level in 1..cacheLevels.size) {
            cache = cacheLevels[level - 1]
        } else {
            handleError("Update Cache Level (LVL)", CacheError("Cache level '" + level + "' does not exist in your current cache!"), true)
        }
    }

    fun updateCacheBlockSize(value: String) {
        val v = value.toInt()
        try {
            cache.setCacheBlockSize(v)
        } catch (er: CacheError) {
            Renderer.printConsole(er.toString())
        }
    }

    fun updateCacheNumberOfBlocks(value: String) {
        val v = value.toInt()
        try {
            cache.setNumberOfBlocks(v)
        } catch (er: CacheError) {
            Renderer.printConsole(er.toString())
        }
    }

    fun updateCacheAssociativity(value: String) {
        val v = value.toInt()
        try {
            cache.setAssociativity(v)
        } catch (er: CacheError) {
            Renderer.printConsole(er.toString())
        }
    }

    fun updateCachePlacementPolicy(value: String) {
        if (value == "N-Way Set Associative") {
            cache.setPlacementPol(PlacementPolicy.NWAY_SET_ASSOCIATIVE)
        } else if (value == "Fully Associative") {
            cache.setPlacementPol(PlacementPolicy.FULLY_ASSOCIATIVE)
        } else {
            cache.setPlacementPol(PlacementPolicy.DIRECT_MAPPING)
        }
    }

    fun updateCacheReplacementPolicy(value: String) {
        if (value == "Random") {
            cache.setBlockRepPolicy(BlockReplacementPolicy.RANDOM)
        } else {
            cache.setBlockRepPolicy(BlockReplacementPolicy.LRU)
        }
    }

    fun setCacheSeed(v: String) {
        cache.setCurrentSeed(v)
    }

    fun setAlignedAddressing(b: Boolean) {
        simSettings.alignedAddress = b
    }

    fun setMutableText(b: Boolean) {
        simSettings.mutableText = b
    }

    fun trace() {
        traceSt()
    }

    internal fun traceSt() {
        try {
            tr.traceStart()
            traceLoop()
        } catch (e: Throwable) {
            handleError("Trace tr Start", e, e is AlignmentError || e is StoreError)
        }
    }

    internal fun traceLoop() {
        try {
            var cycles = 0
            while (cycles < TIMEOUT_CYCLES) {
                if (sim.isDone()) {
                    runTrEnd()
                    return
                }
                tr.traceStep()
                cycles++
            }
            traceLoop()
        } catch (e: Throwable) {
            handleError("Trace tr Loop", e, e is AlignmentError || e is StoreError)
        }
    }
    internal fun runTrEnd() {
        try {
            tr.traceEnd()
            tr.traceStringStart()
            traceStringLoop()
        } catch (e: Throwable) {
            handleError("Trace Tr End", e, e is AlignmentError || e is StoreError)
        }
    }

    internal fun traceStringLoop() {
        try {
        var cycles = 0
        while (cycles < TIMEOUT_CYCLES) {
            if (!tr.traceStringStep()) {
                traceStringEnd()
                return
            }
        }
            traceStringLoop()
        } catch (e: Throwable) {
            handleError("Trace String Loop", e, e is AlignmentError || e is StoreError)
        }
    }

    internal fun traceStringEnd() {
        try {
            tr.traceStringEnd()
            Renderer.printConsole(tr.getString())
        } catch (e: Throwable) {
            handleError("Trace String End", e, e is AlignmentError || e is StoreError)
        }
    }

    /*@JsName("trace") fun trace() {
        //@todo make it so trace is better
        Renderer.setNameButtonSpinning("simulator-trace", true)
        Renderer.clearConsole()
        this.loadTraceSettings()
        window.setTimeout(Driver::traceStart, TIMEOUT_TIME)
    }*/
    internal fun traceStart() {
        try {
            tr.trace()
            traceString()
        } catch (e: Throwable) {
            handleError("Trace Start", e, e is AlignmentError || e is StoreError)
        }
    }
    internal fun traceString() {
        try {
            tr.traceString()
            Renderer.printConsole(tr.getString())
        } catch (e: Throwable) {
            handleError("Trace to String", e)
        }
    }
}