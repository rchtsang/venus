package venus.simulator.cache

import venus.riscv.Address

/**
 * This is a class representing the state of a cacheHandler.
 *
 * If default is true, then the address is ignored since it will be the centinel node.
 */
class CacheState(address: Address, cacheHandler: CacheHandler, rw: RW, default: Boolean = false) {
    private var prevCacheState: CacheState
    private var currentInternalCache: InternalCache
    private val cache = cacheHandler
    private var wasHit = false

    private var hitcount = 0

    init {
        if (default) {
            prevCacheState = this
            /*Since this is the first block, we must set it up properly*/
            currentInternalCache = InternalCache(cacheHandler)
            currentInternalCache.setup()
        } else {
            /*Since this is not the default state, we can use the data made in the previous cacheHandler to set up this cacheHandler.*/
            prevCacheState = cacheHandler.currentState()
            this.hitcount = this.prevCacheState.getHitCount()
            currentInternalCache = prevCacheState.currentInternalCache.copy()
            if (rw.equals(RW.READ)) {
                this.wasHit = this.currentInternalCache.read(address)
            } else {
                this.wasHit = this.currentInternalCache.write(address)
            }
            hitcount += if (this.wasHit) 1 else 0
        }
    }

    fun getHitCount(): Int {
        return this.hitcount
    }

    fun getMissCount(): Int {
        return this.cache.memoryAccessCount() - this.hitcount
    }

    fun getHitRate(): Double {
        return this.getHitCount().toDouble() / this.cache.memoryAccessCount().toDouble()
    }

    fun getMissRate(): Double {
        return this.getMissCount().toDouble() / this.cache.memoryAccessCount().toDouble()
    }

    fun wasHit(): Boolean {
        return this.wasHit
    }

    fun getBlocksState(): ArrayList<String> {
        return currentInternalCache.getBlocksState()
    }
}

enum class RW {
    READ,
    WRITE
}

private class InternalCache(cacheHandler: CacheHandler) {
    val cHandler = cacheHandler
//    var indexSize = 0
//    var offsetSize = 0
//    var tagSize = 0
    lateinit var cache: Cache

    fun setup () {
        /*@todo sets up this (for the default state)*/
//        indexSize = Math.log2(cHandler.cacheSize().toDouble() / (cHandler.cacheBlockSize().toDouble() * cHandler.associativity())).toInt()
//        offsetSize = Math.log2(this.cHandler.cacheBlockSize()).toInt()
//        tagSize = 32 - indexSize - offsetSize
        this.cache = Cache(cHandler)
    }

    fun read (address: Address): Boolean {
        /*@todo will update the current state and return if it was successful*/
        return cache.read(address)
    }

    fun write (address: Address): Boolean {
        /*@todo will update the current state and return if it was successful*/
        return cache.write(address)
    }

    fun getBlocksState(): ArrayList<String> {
        return cache.blockStates()
    }

    fun copy(): InternalCache {
        val inCache = InternalCache(this.cHandler)
        inCache.cache = this.cache.copy()
//        inCache.indexSize = this.indexSize
//        inCache.tagSize = this.tagSize
//        inCache.offsetSize = this.offsetSize
        /*@todo will copy the elements in here so that we can keep each state.*/
        return inCache
    }
}