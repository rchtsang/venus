package venus.fernet

class TokenExpiredException(message: String?) : FernetException(message) {
    companion object {
        private const val serialVersionUID = 1L
    }
}