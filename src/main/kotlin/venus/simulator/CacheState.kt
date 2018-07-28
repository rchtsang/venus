package venus.simulator

/**
 * This is a class representing the state of a cache.
 *
 * If default is true, then the address is ignored since it will be the centinel node.
 */
class CacheState(address: Int, cache: Cache, default: Boolean = false) {
    private lateinit var prevCacheState: CacheState
    private lateinit var currentInternalCache: InternalCache
    private val cache = cache

    private var hitcount = 0

    init {
        if (default) {
            prevCacheState = this
            /*Since this is the first block, we must set it up properly*/
            currentInternalCache = InternalCache(cache)
            currentInternalCache.setup()
        } else {
            /*Since this is not the default state, we can use the data made in the previous cache to set up this cache.*/
            prevCacheState = cache.currentState()
            this.hitcount = this.prevCacheState.getHitCount()
            currentInternalCache = prevCacheState.currentInternalCache.copy()
            hitcount += if (this.currentInternalCache.push(address)) 1 else 0
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
}

private class InternalCache(cache: Cache) {
    val cache = cache

    fun setup () {
        /*@todo sets up this (for the default state)*/
    }

    fun push (address: Int): Boolean {
        /*@todo will update the current state and return if it was successful*/
        return false
    }

    fun copy(): InternalCache {
        val inCache = InternalCache(this.cache)
        /*@todo will copy the elements in here so that we can keep each state.*/
        return inCache
    }
}