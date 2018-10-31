package venus.glue.js

import org.w3c.dom.get
import venus.glue.Driver
import venus.glue.Renderer
import kotlin.browser.document
import kotlin.browser.window

/**
* This is used to add and remove external packages.
*/

@JsName("ScriptManager") object ScriptManager {
    init {
        js("""
            window.VenusScriptManager = this;
                """)
    }

    var packages = HashMap<String, venuspackage>()

    fun loadDefaults() {
        addPackage("packages/disassembler.js", enabled = true, removable = false)
    }

    fun loadScript(url: String, onfail: String, onload: String) {
        val urlelm = document.getElementById(url)
        urlelm?.parentNode?.removeChild(urlelm)
        val script = document.createElement("script")
        script.setAttribute("onerror", onfail)
        script.setAttribute("onload", onload)
        script.setAttribute("src", url)
        script.setAttribute("id", url)
        document.getElementsByTagName("head")[0]?.appendChild(script)
    }

    fun addPackage(url: String, enabled: Boolean = true, removable: Boolean = true) {
        val onfail = "window.VenusScriptManager.addPackageFailure('$url');"
        val onload = "window.VenusScriptManager.addPackageSuccess('$url', $enabled, $removable);"
        loadScript(url, onfail, onload)
    }

    fun verifyPackage(p: venuspackage): Boolean {
        return jsTypeOf(venuspackage.id) == "string" && jsTypeOf(venuspackage.load) == "function" && jsTypeOf(venuspackage.unload) == "function"
    }

    @JsName("addPackageSuccess") fun addPackageSuccess(url: String, enabled: Boolean = true, removable: Boolean = true) {
        if (!verifyPackage(venuspackage)) {
            window.alert("Could not load package '${venuspackage.id}' because it does not contain all of the required fields!")
            js("window.venuspackage = undefined")
            return
        }
        if (packages.containsKey(venuspackage.id)) {
            val orig = packages.get(venuspackage.id)
            if (!venuspackage.removable) {
                js("window.venuspackage = undefined")
                throw Throwable("Cannot update a default script!")
            }
            removePackage(venuspackage.id)
        }
        if (venuspackage == undefined) {
            addPackageFailure(url)
            return
        }
        venuspackage.url = url
        var worked = true
        if (enabled) {
            js("""
            try {
                window.venuspackage.load();
            } catch (e) {
                worked = false
                window.VenusScriptManager.addPackageFailure();
            }
        """)
        }
        if (worked) {
            venuspackage.enabled = enabled
            venuspackage.removable = removable
            Renderer.rendererAddPackage(venuspackage.id, enabled, removable)
            packages.put(venuspackage.id, venuspackage)
            updateLS()
            console.log("Loaded script ($url)!")
            js("window.venuspackage = undefined")
        }
    }

    @JsName("addPackageFailure") fun addPackageFailure(url: String) {
        console.log("Could not load the script ($url)!")
        js("window.venuspackage = undefined")
    }

    fun removePackage(id: String) {
        console.log("Removing package '$id'!")
        if (!packages.containsKey(id)) {
            console.log("Could not find package '$id'")
            return
        }
        disablePackage(id)
        val p = packages.remove(id)
        if (p != null) {
            val s = document.getElementById(venuspackage.url)
            if (s != null) {
                s.parentElement?.removeChild(s)
            }
        }
        updateLS()
        Renderer.rendererRemovePackage(id)
        console.log("Package '$id' uninstalled successfully!")
    }

    fun disablePackage(id: String) {
        console.log("Disabling package '$id'!")
        if (!packages.containsKey(id)) {
            console.log("Could not find package '$id'")
            return
        }
        val p = packages.get(id)
        if (venuspackage.enabled == false) {
            console.log("Package '$id' is already disabled!")
            return
        }
        var worked = true
        js("""
            try {
                p.unload();
            } catch (e) {
                worked = false;
                console.log("Could not disable package '" + id + "'!");
            }
            """)
        if (worked) {
            venuspackage.enabled = false
            Renderer.rendererUpdatePackage(id, false)
            updateLS()
            console.log("Successfully disable package '$id'!")
        }
    }

    fun enablePackage(id: String) {
        console.log("Enabling package '$id'!")
        if (!packages.containsKey(id)) {
            console.log("Could not find package '$id'")
            return
        }
        val p = packages.get(id)
        if (venuspackage.enabled == true) {
            console.log("Package '$id' is already enabled!")
            return
        }
        var worked = true
        js("""
            try {
                p.load();
            } catch (e) {
                worked = false;
                console.log("Could not enable package '" + id + "'!");
            }
            """)
        if (worked) {
            venuspackage.enabled = true
            Renderer.rendererUpdatePackage(id, true)
            updateLS()
            console.log("Successfully enabled package '$id'!")
        }
    }

    fun togglePackage(id: String) {
        console.log("Toggling package '$id'!")
        if (!packages.containsKey(id)) {
            console.log("Could not find package '$id'")
            return
        }
        val p = packages.get(id)
        if (venuspackage.enabled == true) {
            disablePackage(venuspackage.id)
        } else {
            enablePackage(venuspackage.id ?: "")
        }
    }

    fun updateLS() {
        val l = ArrayList<pkg>()
        for (p in packages.values) {
            val n = pkg(venuspackage.id, venuspackage.url, venuspackage.enabled, venuspackage.removable)
            l.add(n)
        }
        Driver.LS.set("script_manager", JSON.stringify(l))
    }

    fun loadPackages() {
        val pstr = Driver.LS.safeget("script_manager", "[]")
        val pkgs = JSON.parse<ArrayList<pkg>>(pstr)
        var i = 0
        while (js("i < pkgs.length")) {
            val p = js("pkgs[i]")
            if (p.removable) {
                addPackage(p.url, p.enabled, p.removable)
            } else {
                loadPKGTimeout(p.id, p.enabled)
            }
            i++
        }
    }

    fun loadPKGTimeout(id: String, enabled: Boolean) {
        if (packages.containsKey(id)) {
            if (enabled) {
                enablePackage(id)
            } else {
                disablePackage(id)
            }
        } else {
            window.setTimeout(ScriptManager::loadPKGTimeout, 100, id, enabled)
        }
    }
}

class pkg(
    var id: String = "",
    var url: String = "",
    var enabled: Boolean = false,
    var removable: Boolean = true
)

external object venuspackage {
    val id: String
    var url: String
    var enabled: Boolean
    var removable: Boolean
    val load: Function<Unit>
    val unload: Function<Unit>
}