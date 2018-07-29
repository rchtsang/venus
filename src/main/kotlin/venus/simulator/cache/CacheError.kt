package venus.simulator.cache

/**
 * Thrown when errors occur during cache op.
 */
class CacheError : Throwable {
    /**
     * @param msg the message to error with
     */
    constructor(msg: String? = null) : super(msg)
}
