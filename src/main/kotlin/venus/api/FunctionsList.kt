package venus.api

class FunctionsList(val id: String) {
    private val fns = HashMap<String, Function<Any?>>()

    fun add(id: String, f: Function<Any?>) {
        fns[id] = f
    }

    fun remove(id: String) {
        fns.remove(id)
    }

    fun evalFunctions(args: Any?): Boolean {
        js("""var a = [];""")
        if (js("args.constructor === Array") as Boolean) {
            js("a = args;")
        } else {
            if (args is Collection<Any?>) {
                for (arg in args) {
                    js("a.push(arg);")
                }
            }
        }
        var rv = true
        for (f in fns.values) {
            val v = js("f(a)")
            if (jsTypeOf(v) !== "undefined") {
                rv = rv && v
            }
        }
        return rv
    }
}