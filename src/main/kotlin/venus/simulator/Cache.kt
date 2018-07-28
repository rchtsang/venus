package venus.simulator

class Cache {
    private var numberOfBlocks: Int = 1
    /*This is in words*/
    private var cacheBlockSize: Int = 1
    private var placementPol: PlacementPolicy = PlacementPolicy.DIRECT_MAPPING
    private var BlockRepPolicy: BlockReplacementPolicy = BlockReplacementPolicy.LRU
    /*This is the set size of blocks*/
    private var associativity: Int = 1

    private var cacheList = ArrayList<CacheState>()
    private var addresses = ArrayList<Int>()
    private var initialCache = CacheState(0, this, true)

    init {
        this.reset()
    }

    fun access(address: Int) {
        val c = CacheState(address, this)
        addresses.add(address)
        cacheList.add(c)
    }

    fun undoAccess() {
        if (this.memoryAccessCount() > 0) {
            this.addresses.removeAt(this.addresses.size - 1)
            this.cacheList.removeAt(this.cacheList.size - 1)
        }
    }

    fun update() {
        var adrs = this.addresses
        this.reset()
        for (addr in adrs) {
            this.access(addr)
        }
    }

    fun reset() {
        cacheList = ArrayList()
        cacheList.add(initialCache)
        addresses = ArrayList()
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
        return 4 * this.numberOfBlocks * this.cacheBlockSize
    }

    fun setNumberOfBlocks(i: Int) {
        val d = Math.log2(i.toDouble())
        if (!isInt(d)) {
            throw CacheError("Number of Blocks must be a power of 2!")
        }
        this.numberOfBlocks = i
        this.setAssociativity(i)
        this.update()
    }

    fun numberOfBlocks(): Int {
        return this.numberOfBlocks
    }

    fun setCacheBlockSize(i: Int) {
        val d = Math.log2(i.toDouble())
        if (!isInt(d)) {
            throw CacheError("Cache Block Size must be a power of 2!")
        }
        this.cacheBlockSize = i
        this.update()
    }

    fun cacheBlockSize(): Int {
        return this.cacheBlockSize
    }

    fun setPlacementPol(p: PlacementPolicy) {
        this.placementPol = p
        this.update()
    }

    fun placementPol (): PlacementPolicy {
        return this.placementPol
    }

    fun setBlockRepPolicy (brp: BlockReplacementPolicy) {
        this.BlockRepPolicy = brp
        if (brp.equals(PlacementPolicy.DIRECT_MAPPING)) {
            this.associativity = 1
        }
        if (brp.equals(PlacementPolicy.FULLY_ASSOCIATIVE)) {
            this.associativity = this.numberOfBlocks
        }
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
        }
        this.update()
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
    NWAY_SET_ASSOCIATIVE
}
enum class BlockReplacementPolicy {
    LRU,
    RANDOM
}
