package venus.vfs

import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.window

val VENUS_AUTH_TOKEN = "1yJPLzMSwOYPYqsLegJ8NpvJXdIC7PcrWLtPxPpZ6DzI9BsFv3iGwIpilpgVy0M7TmmEA063VUkBYIHezoes4vHF6m0mZA8DuTh"
val COMPATABLE_VERSION = "1.0.0"
data class VFSMountedDriveHandler(var url: String) {
    init {
//        login()
//        ping()
//        ping()
        if (url.endsWith("/")) {
            url = url.removeSuffix("/")
        }
        connect()
    }

    fun connect() {
        if (!ping()) {
            throw IllegalStateException("Could not connect to $url")
        }
        compatable()
    }

    fun save(): String {
        return url
    }

    fun make_request(type: String, endPoint: String, data: String = ""): String? {
        val xhttp = XMLHttpRequest()
        xhttp.open(type, "$url/$endPoint", async = false)
        xhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
        if (js("""window.DEBUG""") == true) {
            console.log("Sending request to $endPoint!")
        }
        try {
            xhttp.send(data)
        } catch (e: Throwable) {
            return null
        }
        if (js("""window.DEBUG""") == true) {
            console.log("Got response!")
        }
//        if (xhttp.status == 401.toShort()) {
//            console.log("Failed to make request to $endPoint due to an auth error, logging back in...")
//            login()
//            return make_request(type, endPoint, data)
//        }
        if (xhttp.status == 0.toShort()) {
            throw IllegalStateException("Server responded with ${xhttp.status} - ${xhttp.statusText}! This may be due to strict enforcement of HTTPS.")
        }
        if (xhttp.status != 200.toShort()) {
            throw IllegalStateException("Server responded with ${xhttp.status} - ${xhttp.statusText} for request $endPoint. Data: $data")
        }
        return xhttp.responseText
    }

//    fun login() {
//        val res = make_request("POST", "login", VENUS_AUTH_TOKEN)
//        this.token = JSON.parse<LoginToken>(res)
//    }
//
//    fun logout() {
//        if (this.token != null) {
//            make_request("POST", "logout", this.token!!.token)
//            this.token = null
//        }
//    }

    fun compatable() {
        val res = this.getVersion()
        if (res == null) {
            throw IllegalStateException("Failed to get version from the file server ($url)!")
        } else if (res != COMPATABLE_VERSION) {
            throw IllegalStateException("This version of Venus's drive handler ($COMPATABLE_VERSION) is not compatible with the mounted file server ($res)!")
        }
    }

    fun getVersion(): String? {
        val rsp = make_request("GET", "version")
        val res = rsp?.let { JSON.parse<pingRes>(it) }
        return res?.data
    }

    data class pingRes(val data: String)
    fun ping(): Boolean {
        console.log("Sending ping...")
        val rsp = make_request("GET", "ping")
        val res = rsp?.let { JSON.parse<pingRes>(it) } ?: pingRes("Server failed to respond!")
        console.log("Server responded with: ${res.data}!")
        return rsp != null
    }

    data class CMDlsReq(val data: String)
    data class CMDlsRes(val success: Boolean, val data: Array<String> = arrayOf<String>())
    fun CMDls(dir: String = ""): Array<String>? {
        val rsp = make_request("POST", "api/fs/ls/names", data = JSON.stringify(CMDlsReq(dir)))
        val res = rsp?.let { JSON.parse<CMDlsRes>(it) } ?: CMDlsRes(success = false)
        if (!res.success) {
            return null
        }
        return res.data
    }

    data class CMDfileinfoReq(val name: String, val path: String)
    data class CMDfileinfoRes(val success: Boolean, val name: String = "", val type: String = "", val data: String = "")
    fun CMDfileinfo(path: String, name: String): Map<String, String> {
        val rsp = make_request("POST", "api/fs/file/info", data = JSON.stringify(CMDfileinfoReq(name = name, path = path)))
        val res = rsp?.let { JSON.parse<CMDfileinfoRes>(it) } ?: CMDfileinfoRes(success = false)
        return mapOf(Pair("name", res.name), Pair("type", res.type))
    }

    data class CMDfilereadReq(val path: String)
    data class CMDfilereadRes(val success: Boolean, val data: String = "FAILED TO READ FILE")
    fun CMDfileread(path: String): String {
        val rsp = make_request("POST", "api/fs/file/read", data = JSON.stringify(CMDfilereadReq(path = path)))
        val res = rsp?.let { JSON.parse<CMDfilereadRes>(it) } ?: CMDfilereadRes(success = false)
        return res.data
    }

    data class CMDfilewriteReq(val path: String, val data: String)
    data class CMDfilewriteRes(val success: Boolean, val data: String = "FAILED TO WRITE TO FILE")
    fun CMDfilewrite(path: String, data: String): String {
        val rsp = make_request("POST", "api/fs/file/write", data = JSON.stringify(CMDfilewriteReq(path = path, data = data)))
        val res = rsp?.let { JSON.parse<CMDfilewriteRes>(it) } ?: CMDfilewriteRes(success = false)
        if (!res.success) {
            window.alert("Failed to write to file: ${res.data}")
        }
        return res.data
    }

    data class CMDmkdirReq(val path: String)
    data class CMDmkdirRes(val success: Boolean, val data: String = "FAILED TO GET RESPONSE")
    fun CMDmkdir(path: String): String {
        val rsp = make_request("POST", "api/fs/mkdir", data = JSON.stringify(CMDmkdirReq(path = path)))
        val res = rsp?.let { JSON.parse<CMDmkdirRes>(it) } ?: CMDmkdirRes(success = false)
        if (!res.success) {
            window.alert("Failed to write make folder: ${res.data}")
        }
        return res.data
    }

    data class CMDtouchReq(val path: String)
    data class CMDtouchRes(val success: Boolean, val data: String = "FAILED TO GET RESPONSE")
    fun CMDtouch(path: String): String {
        val rsp = make_request("POST", "api/fs/touch", data = JSON.stringify(CMDtouchReq(path = path)))
        val res = rsp?.let { JSON.parse<CMDtouchRes>(it) } ?: CMDtouchRes(success = false)
        if (!res.success) {
            window.alert("Failed to write make file: ${res.data}")
        }
        return res.data
    }

    data class CMDrmReq(val path: String)
    data class CMDrmRes(val success: Boolean, val data: String = "FAILED TO GET RESPONSE")
    fun CMDrm(path: String): String {
        val rsp = make_request("POST", "api/fs/rm", data = JSON.stringify(CMDrmReq(path = path)))
        val res = rsp?.let { JSON.parse<CMDrmRes>(it) } ?: CMDrmRes(success = false)
        if (!res.success) {
            window.alert("Failed to remove file or directory: ${res.data}")
        }
        return res.data
    }

    data class CMDnameRes(val success: Boolean, val data: String)
    fun CMDname(): String {
        val rsp = make_request("GET", "api/fs/name")
        val res = rsp?.let { JSON.parse<CMDnameRes>(it) } ?: CMDnameRes(false, "Could not get a response from the server!")
        return res.data
    }
}

data class LoginToken(var token: String, var expiration: String)