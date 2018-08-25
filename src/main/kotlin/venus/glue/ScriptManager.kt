package venus.glue

import org.w3c.dom.get
import kotlin.browser.document

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

    fun addPackage(url: String, enabled: Boolean = true) {
        val onfail = "window.VenusScriptManager.addPackageFailure('$url');"
        val onload = "window.VenusScriptManager.addPackageSuccess('$url', $enabled);"
        loadScript(url, onfail, onload)
    }

    @JsName("addPackageSuccess") fun addPackageSuccess(url: String, enabled: Boolean = true) {
        if (packages.containsKey(venuspackage.id)) {
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
            Renderer.rendererAddPackage(venuspackage.id, enabled)
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
            val n = pkg
            n.id = p.id
            n.url = p.url
            n.enabled = p.enabled
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
            addPackage(p.url, p.enabled)
            i++
        }
    }
}

object pkg {
    var id: String = ""
    var url: String = ""
    var enabled: Boolean = false
}

external object venuspackage {
    val id: String
    var url: String
    var enabled: Boolean
    val load: Function<Unit>
    val unload: Function<Unit>
}