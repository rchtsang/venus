package venus.api

@JsName("API") object API {
    // The key (String) is the ID of the FunctionsList
    val functionsLists: MutableMap<String, FunctionsList> = HashMap()

    @JsName("addList") fun addList(s: String) {
        functionsLists[s] = FunctionsList(s)
    }

    @JsName("evalList") fun evalList(s: String, args: Any?): Boolean {
        return functionsLists[s]?.evalFunctions(args) ?: false
    }

    /**
     * All functions should take in a single argument which is a javascript list of arguments.
     * This list of arguments will differ depending on which function it uses.
     */
    @JsName("addListener") fun addListener(locationID: String, pkgid: String, fnid: String, function: Function<Any?>) {
        if (functionsLists.containsKey(locationID)) {
            val f = functionsLists.get(locationID)
            f?.add("$pkgid.$fnid", function)
            console.log("Added function to '$locationID'")
        } else {
            console.error("Could not add function to '$locationID' because the location ID does not exist!")
        }
    }

    @JsName("removeListener") fun removeListener(locationID: String, pkgid: String, fnid: String) {
        if (functionsLists.containsKey(locationID)) {
            val f = functionsLists.get(locationID)
            f?.remove("$pkgid.$fnid")
            console.log("Remove function to '$locationID'")
        } else {
            console.error("Could not remove function to '$locationID' because the location ID does not exist!")
        }
    }
}