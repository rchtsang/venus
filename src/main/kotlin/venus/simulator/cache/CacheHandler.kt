package venus.simulator.cache

import venus.riscv.Address
import venus.riscv.MemSize

class CacheHandler {
    private var numberOfBlocks: Int = 1
    /*This is in bytes*/
    private var cacheBlockSize: Int = 4
    private var placementPol: PlacementPolicy = PlacementPolicy.DIRECT_MAPPING
    private var BlockRepPolicy: BlockReplacementPolicy = BlockReplacementPolicy.LRU
    /*This is the set size of blocks*/
    private var associativity: Int = 1

    private var cacheList = ArrayList<CacheState>()
    private var addresses = ArrayList<Address>()
    private var RorW = ArrayList<RW>()
    private var initialCache = CacheState(Address(0, MemSize.WORD), this, RW.READ, true)

    init {
        this.reset()
    }

    /*@TODO Read and write do nothing special at the moment. Make it so that we can detect read and write hit/miss rate separately.*/
    fun read(a: Address) {
        val c = CacheState(a, this, RW.READ)
        addresses.add(a)
        cacheList.add(c)
        RorW.add(RW.READ)
    }

    fun write(a: Address) {
        val c = CacheState(a, this, RW.WRITE)
        addresses.add(a)
        cacheList.add(c)
        RorW.add(RW.WRITE)
    }

    fun access(a: Address) {
        val c = CacheState(a, this, RW.READ)
        addresses.add(a)
        cacheList.add(c)
        RorW.add(RW.READ)
    }

    fun undoAccess(addr: Address) {
        if (this.memoryAccessCount() > 0) {
            this.addresses.removeAt(this.addresses.size - 1)
            this.cacheList.removeAt(this.cacheList.size - 1)
            this.RorW.removeAt(this.RorW.size - 1)
        }
    }

    fun update() {
        val adrs = this.addresses
        val row = this.RorW
        this.reset()
        for (i in adrs.indices) {
            if (row[i] == RW.READ) {
                this.read(adrs[i])
            } else {
                this.write(adrs[i])
            }
        }
    }

    fun reset() {
        cacheList = ArrayList()
        cacheList.add(initialCache)
        addresses = ArrayList()
        RorW = ArrayList()
    }

    fun getBlocksState(): ArrayList<String> {
        return this.currentState().getBlocksState()
    }

    fun getHitCount(): Int {
        return this.currentState().getHitCount()
    }

    fun getMissCount(): Int {
        return this.currentState().getMissCount()
    }

    fun getHitRate(): Double {
        return this.currentState().getHitRate()
    }

    fun getMissRate(): Double {
        return this.currentState().getMissRate()
    }

    fun wasHit(): Boolean {
        return this.currentState().wasHit()
    }

    fun memoryAccessCount(): Int {
        return this.addresses.size
    }

    fun currentState(): CacheState {
        val clsize = this.cacheList.size
        return this.cacheList[clsize - 1]
    }

    /*This is in bytes*/
    fun cacheSize(): Int {
        return this.numberOfBlocks * this.cacheBlockSize
    }

    fun setNumberOfBlocks(i: Int) {
        val d = Math.log2(i.toDouble())
        if (!isInt(d)) {
            throw CacheError("Number of Blocks must be a power of 2!")
        }
        this.numberOfBlocks = i
        if (this.placementPol == PlacementPolicy.FULLY_ASSOCIATIVE) {
            this.setAssociativity(i, true)
        }
        this.update()
    }

    fun numberOfBlocks(): Int {
        return this.numberOfBlocks
    }

    fun setCacheBlockSize(i: Int) {
        val d = Math.log2(i.toDouble())
        if (!isInt(d)) {
            throw CacheError("CacheHandler Block Size must be a power of 2!")
        }
        this.cacheBlockSize = i
        this.update()
    }

    fun cacheBlockSize(): Int {
        return this.cacheBlockSize
    }

    fun setPlacementPol(p: PlacementPolicy) {
        this.placementPol = p
        if (p.equals(PlacementPolicy.DIRECT_MAPPING)) {
            this.associativity = 1
        }
        if (p.equals(PlacementPolicy.FULLY_ASSOCIATIVE)) {
            this.associativity = this.numberOfBlocks
        }
        this.update()
    }

    fun placementPol (): PlacementPolicy {
        return this.placementPol
    }

    fun setBlockRepPolicy (brp: BlockReplacementPolicy) {
        this.BlockRepPolicy = brp
        this.update()
    }

    fun blockRepPolicy (): BlockReplacementPolicy {
        return this.BlockRepPolicy
    }

    fun setAssociativity(i: Int, override: Boolean = false) {
        if (this.placementPol == PlacementPolicy.NWAY_SET_ASSOCIATIVE || override) {
            val d = Math.log2(i.toDouble())
            if (!isInt(d)) {
                throw CacheError("Associativity must be a power of 2!")
            }
            this.associativity = i
            this.update()
        }
    }

    fun associativity(): Int {
        return this.associativity
    }

    internal fun isInt(d: Double): Boolean {
        return !d.isNaN() && !d.isInfinite() && d == Math.floor(d).toDouble()
    }
}

external class Math {
    companion object {
        fun log2(d: Double): Double
        fun log2(d: Int): Double
        fun floor(d: Double): Int
    }
}

enum class PlacementPolicy {
    DIRECT_MAPPING,
    FULLY_ASSOCIATIVE,
    NWAY_SET_ASSOCIATIVE;

    fun toMyString(): String {
        if (this.equals(PlacementPolicy.FULLY_ASSOCIATIVE)) {
            return "Fully Associative"
        }
        if (this.equals(PlacementPolicy.NWAY_SET_ASSOCIATIVE)) {
            return "N-Way Set Associative"
        }
        return "Direct Mapped"
    }
}
enum class BlockReplacementPolicy {
    LRU,
    RANDOM;

    fun toMyString(): String {
        if (this.equals(BlockReplacementPolicy.LRU)) {
            return "LRU"
        }
        return "Random"
    }
}

enum class BlockState {
    HIT,
    MISS,
    EMPTY
}