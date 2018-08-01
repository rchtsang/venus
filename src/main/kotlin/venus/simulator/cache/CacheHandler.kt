package venus.simulator.cache

import venus.riscv.Address
import venus.riscv.MemSize

class CacheHandler (var cacheLevel: Int) {
    var seed: String = Math.random().toString()
        private set
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

    var nextLevelCacheHandler: CacheHandler? = null
    var attached = true
        private set

    init {
        this.reset()
    }

    /*@TODO Read and write do nothing special at the moment. Make it so that we can detect read and write hit/miss rate separately.*/
    fun read(a: Address) {
        addresses.add(a)
        RorW.add(RW.READ)
        if (attached) {
            val c = CacheState(a, this, RW.READ)
            cacheList.add(c)
        } else {
            nextLevelCacheHandler?.read(a)
        }
    }

    fun write(a: Address) {
        addresses.add(a)
        RorW.add(RW.WRITE)
        if (attached) {
            val c = CacheState(a, this, RW.WRITE)
            cacheList.add(c)
        } else {
            nextLevelCacheHandler?.write(a)
        }
    }

    fun access(a: Address) {
        addresses.add(a)
        RorW.add(RW.READ)
        if (attached) {
            val c = CacheState(a, this, RW.READ)
            cacheList.add(c)
        } else {
            nextLevelCacheHandler?.access(a)
        }
    }

    fun undoAccess(addr: Address) {
        if (this.memoryAccessCount() > 0) {
            this.addresses.removeAt(this.addresses.size - 1)
            this.cacheList.removeAt(this.cacheList.size - 1)
            this.RorW.removeAt(this.RorW.size - 1)
        }
    }

    fun setSeed(v: String) {
        this.seed = v
        this.update()
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

    fun attach(attach: Boolean) {
        this.attached = attach
        if (attach) {
            this.update()
        } else {
            this.reset()
        }
    }

    fun reset() {
        try {
            Math.seedrandom(seed)
        } catch (e: Throwable) {}
        cacheList = ArrayList()
        cacheList.add(CacheState(Address(0, MemSize.WORD), this, RW.READ, true))
        addresses = ArrayList()
        RorW = ArrayList()
        nextLevelCacheHandler?.reset()
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
        } else if (this.placementPol == PlacementPolicy.NWAY_SET_ASSOCIATIVE) {
            if (i < this.associativity()) {
                this.setAssociativity(i)
            }
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
        if ((this.placementPol == PlacementPolicy.NWAY_SET_ASSOCIATIVE || override)) {
            if (i !in 1..this.numberOfBlocks) {
                return
                //throw CacheError("Associativity must be greater than or equal to 1 but not greater than the number of blocks!")
            }
            val d = Math.log2(i.toDouble())
            if (!isInt(d)) {
                throw CacheError("Associativity must be a positive nonzero power of 2!")
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
        fun seedrandom(seed: String)
        fun random(): Double
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