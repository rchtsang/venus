package venus.glue

import org.w3c.dom.get
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
        ScriptManager.addPackage("packages/disassembler.js", enabled = true, removable = false)
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

    @JsName("addPackageSuccess") fun addPackageSuccess(url: String, en: Boolean = true, removable: Boolean = true) {
        var enabled = en
        if (venuspackage == undefined) {
            addPackageFailure(url)
            return
        }
        if (jsTypeOf(venuspackage.requires) != "undefined") {
            if (js("venuspackage.requires.constructor === Array")) {
                var i = js("venuspackage.requires.length")
                while (i > 0) {
                    var k = js("venuspackage.requires[i - 1]")
                    if (!packages.containsKey(k)) {
                        console.warn("This package requires '$k' which was not found in the currently installed packages! Thus it cannot be loaded!")
                        addPackageFailure(url)
                        return
                    }
                    i--
                }
            } else {
                venuspackage.requires = js("[]")
            }
        } else {
            venuspackage.requires = js("[]")
        }
        venuspackage.dependent = js("[]")
        if (packages.containsKey(venuspackage.id)) {
            val orig = packages.get(venuspackage.id)
            if (!orig!!.removable) {
                js("window.venuspackage = undefined")
                throw Throwable("Cannot update a default script!")
            }
            removePackage(venuspackage.id)
        }
        venuspackage.url = url
        var i = js("window.venuspackage.requires.length") as Int
        while (i > 0) {
            val k = js("window.venuspackage.requires[i - 1]") as String
            if (!((packages.get(k))?.enabled ?: true)) {
                console.warn("Could not enable package '${venuspackage.id}' because it requires a package which has been disabled ($k)!")
                enabled = false
            }
            i--
        }
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
            var i = js("venuspackage.requires.length") as Int
            while (i > 0) {
                val k = js("venuspackage.requires[i - 1]")
                val p = packages[k]
                js("p.dependent.push(venuspackage.id)")
                i--
            }
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
            val s = document.getElementById(p.url)
            if (s != null) {
                s.parentElement?.removeChild(s)
            }
            var i = js("p.requires.length") as Int
            while (i > 0) {
                val k = js("p.requires[i - 1]")
                val pk = packages[k]
                js("""try {
                        pk.dependent.pop(k);
                    } catch (e) {}""")
                i--
            }
            i = js("p.dependent.length") as Int
            while (i > 0) {
                val k = js("p.dependent[i - 1]")
                removePackage(k)
                i--
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
        if (p?.enabled == false) {
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
            p?.enabled = false
            Renderer.rendererUpdatePackage(id, false)
            updateLS()
            var i = js("p.dependent.length") as Int
            while (i > 0) {
                val k = js("p.dependent[i - 1]")
                disablePackage(k)
                i--
            }
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
        if (p?.enabled == true) {
            console.log("Package '$id' is already enabled!")
            return
        }
        var i = js("p.requires.length") as Int
        while (i > 0) {
            val k = js("p.requires[i - 1]") as String
            if (!((packages.get(k))?.enabled ?: true)) {
                console.warn("Could not enable package '${p?.id}' because it requires a package which has been disabled ($k)!")
                Renderer.rendererUpdatePackage(id, false)
                return
            }
            i--
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
            p?.enabled = true
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
        if (p?.enabled == true) {
            disablePackage(p.id)
        } else {
            enablePackage(p?.id ?: "")
        }
    }

    fun updateLS() {
        val l = ArrayList<pkg>()
        for (p in packages.values) {
            val n = pkg(p.id, p.url, p.enabled, p.removable)
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
    var requires: List<String>
    var dependent: MutableList<String>
}