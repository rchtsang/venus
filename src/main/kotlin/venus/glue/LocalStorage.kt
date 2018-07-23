package venus.glue

class LocalStorage {
    //@todo convert this to js and use it externally instead.
    private var lsm: LocalStorageManager = LocalStorageManager("venus")

    init {

    }

    fun get(key: String): String {
        return lsm.get(key)
    }

    fun set(key: String, value: String) {
        lsm.set(key, value)
    }

    fun remove(key: String) {
        lsm.remove(key)
    }
}

external class LocalStorageManager (name: String) {
    fun set(key: String, value: String)
    fun get(key: String): String
    fun remove(key: String)
}