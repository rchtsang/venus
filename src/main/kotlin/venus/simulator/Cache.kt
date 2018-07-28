package venus.simulator

class Cache {
    private var numberOfBlocks: Int = 1
    /*This is in words*/
    private var cacheBlockSize: Int = 1
    private var placementPol: PlacementPolicy = PlacementPolicy.DIRECT_MAPPING
    private var BlockRepPolicy: BlockReplacementPolicy = BlockReplacementPolicy.LRU
    /*This is the set size of blocks*/
    private var ppSize: Int = 1

    private var cacheList = ArrayList<CacheState>()
    private var addresses = ArrayList<Int>()
    private var initialCache = CacheState(0, this, true)

    init {
        this.reset()
    }

    fun access(address: Int) {
        addresses.add(address)
        var c = CacheState(address, this)
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
        this.numberOfBlocks = i
        if (this.placementPol == PlacementPolicy.NWAY_SET_ASSOCIATIVE) {
            this.ppSize = i
        }
        this.update()
    }

    fun numberOfBlocks(): Int {
        return this.numberOfBlocks
    }

    fun setCacheBlockSize(i: Int) {
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
            this.ppSize = 1
        }
        if (brp.equals(PlacementPolicy.FULLY_ASSOCIATIVE)) {
            this.ppSize = this.numberOfBlocks
        }
        this.update()
    }

    fun blockRepPolicy (): BlockReplacementPolicy {
        return this.BlockRepPolicy
    }

    fun setppSize(i: Int, override: Boolean = false) {
        if (this.placementPol == PlacementPolicy.NWAY_SET_ASSOCIATIVE || override) {
            this.ppSize = i
        }
        this.update()
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
