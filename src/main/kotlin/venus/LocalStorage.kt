package venus

class LocalStorage {
    // @todo convert this to js and use it externally instead.
    internal var lsm: LocalStorageManager = LocalStorageManager("venus")

    init {
    }

    @JsName("get") fun get(key: String): String {
        return lsm.get(key)
    }

    @JsName("set") fun set(key: String, value: String) {
        lsm.set(key, value)
    }

    @JsName("remove") fun remove(key: String) {
        lsm.remove(key)
    }

    @JsName("reset") fun reset() {
        lsm.reset()
    }

    @JsName("safeget") fun safeget(key: String, prevVal: String): String {
        val v = this.get(key)
        if (v == "undefined") {
            return prevVal
        }
        return v
    }
}

external class LocalStorageManager(name: String) {
    fun set(key: String, value: String)
    fun get(key: String): String
    fun remove(key: String)
    fun reset()
}