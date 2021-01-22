package venus.fernet

import java.security.GeneralSecurityException

open class FernetException : GeneralSecurityException {
    constructor(message: String?) : super(message) {}
    constructor(exception: Exception?) : super(exception) {}

    companion object {
        private const val serialVersionUID = 1L
    }
}