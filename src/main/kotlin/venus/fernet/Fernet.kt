package venus.fernet

/* ktlint-disable no-wildcard-imports */
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
/* ktlint-enable no-wildcard-imports */

/**
 * Fernet (symmetric encryption)
 *
 * Fernet guarantees that a message encrypted using it cannot be manipulated or
 * read without the key. Fernet is an implementation of symmetric (also known as
 * “secret key”) authenticated cryptography.
 *
 * Fernet Spec https://github.com/fernet/spec/blob/master/Spec.md
 *
 * All encryption in this version is done with AES 128 in CBC mode. All base 64
 * encoding is done with the "URL and Filename Safe" variant, defined in RFC
 * 4648 as "base64url".
 *
 * @author Philipp Grosswiler <philipp.grosswiler></philipp.grosswiler>@gmail.com>
 */
class Fernet {
    private object Bytes {
        /**
         * Returns the values from each provided array combined into a single array. For
         * example, `concat(new byte[] {a, b}, new byte[] {}, new byte[] {c}`
         * returns the array `{a, b, c}`.
         *
         * @param arrays zero or more `byte` arrays
         * @return a single array containing all the values from the source arrays, in
         * order
         */
        fun concat(vararg arrays: ByteArray): ByteArray {
            var length = 0
            for (array in arrays) {
                length += array.size
            }
            val result = ByteArray(length)
            var pos = 0
            for (array in arrays) {
                System.arraycopy(array, 0, result, pos, array.size)
                pos += array.size
            }
            return result
        }
    }

    /**
     * Key Format
     *
     * A fernet key is the base64url encoding of the following fields: Signing-key ‖
     * Encryption-key
     *
     * Signing-key, 128 bits Encryption-key, 128 bits
     */
    class Key {
        val signingKey: ByteArray
        val encryptionKey: ByteArray

        constructor() {
            signingKey = generateKey()
            encryptionKey = generateKey()
        }

        constructor(key: String?) : this(base64UrlDecode(key)) {}
        constructor(key: ByteArray?) {
            if (key != null && key.size == 32) {
                signingKey = Arrays.copyOf(key, 16)
                encryptionKey = Arrays.copyOfRange(key, 16, 32)
            } else {
                throw FernetException("Incorrect key.")
            }
        }

        override fun toString(): String {
            return base64UrlEncode(Bytes.concat(signingKey, encryptionKey))
        }
    }

    /**
     * Token Format
     *
     * A fernet token is the base64url encoding of the concatenation of the
     * following fields: Version ‖ Timestamp ‖ IV ‖ Ciphertext ‖ HMAC
     *
     * Version, 8 bits Timestamp, 64 bits IV, 128 bits Ciphertext, variable length,
     * multiple of 128 bits HMAC, 256 bits
     *
     * Fernet tokens are not self-delimiting. It is assumed that the transport will
     * provide a means of finding the length of each complete fernet token.
     */
    class Token {
        // Token Fields
        /**
         * Version
         *
         * This field denotes which version of the format is being used by the token.
         * Currently there is only one version defined, with the value 128 (0x80).
         */
        private var version = VERSION

        /**
         * Timestamp
         *
         * This field is a 64-bit unsigned big-endian integer. It records the number of
         * seconds elapsed between January 1, 1970 UTC and the time the token was
         * created.
         */
        private var timestamp: Long = 0

        /**
         * IV
         *
         * The 128-bit Initialization Vector used in AES encryption and decryption of
         * the Ciphertext.
         *
         * When generating new fernet tokens, the IV must be chosen uniquely for every
         * token. With a high-quality source of entropy, random selection will do this
         * with high probability.
         */
        var iv = ByteArray(KEY_SIZE)

        /**
         * Ciphertext
         *
         * This field has variable size, but is always a multiple of 128 bits, the AES
         * block size. It contains the original input message, padded and encrypted.
         */
        lateinit var ciphertext: ByteArray

        /**
         * HMAC
         *
         * This field is the 256-bit SHA256 HMAC, under signing-key, of the
         * concatenation of the following fields: Version ‖ Timestamp ‖ IV ‖ Ciphertext
         *
         * Note that the HMAC input is the entire rest of the token verbatim, and that
         * this input is not base64url encoded.
         */
        private var signature = ByteArray(HMAC_SIZE)

        constructor() {
            timestamp = time
            iv = generateKey()
        }

        constructor(timestamp: Long, iv: ByteArray) {
            this.timestamp = timestamp
            this.iv = iv
        }

        constructor(token: ByteArray?) {
            val buffer = ByteBuffer.wrap(token)
            buffer!!.order(ByteOrder.BIG_ENDIAN)
            if (buffer != null && buffer.capacity() >= MIN_TOKEN_SIZE) {
                version = buffer.get()
                timestamp = buffer.long
                buffer[iv]
                ciphertext = ByteArray(buffer.remaining() - signature.size)
                buffer[ciphertext]
                buffer[signature]
            }
        }

        constructor(token: String?) : this(base64UrlDecode(token)) {
            // 1. base64url decode the token.
        }

        @Throws(FernetException::class)
        fun verify(ttl: Int, signingKey: ByteArray): Boolean {
            // 2. Ensure the first byte of the token is 0x80.
            if (version != VERSION) {
                throw FernetException("Invalid version.")
            }

            // 3. If the user has specified a maximum age (or "time-to-live") for the token,
            // ensure the recorded timestamp is not too far in the past.
            if (ttl > 0) {
                val currentTime = time
                if (timestamp + ttl < currentTime || currentTime + MAX_CLOCK_SKEW < timestamp) {
                    throw TokenExpiredException("Token has expired.")
                }
            }

            // 4. Recompute the HMAC from the other fields and the user-supplied
            // signing-key.
            val token = buildToken()

            // 5. Ensure the recomputed HMAC matches the HMAC field stored in the token,
            // using a constant-time comparison function.
            try {
                if (!Arrays.equals(signature, generateHash(token, signingKey))) {
                    throw FernetException("Invalid signature.")
                }
            } catch (e: Exception) {
                throw FernetException(e)
            }
            return true
        }

        @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
        fun sign(ciphertext: ByteArray, signingKey: ByteArray): ByteArray {
            this.ciphertext = ciphertext
            val token = buildToken()
            signature = generateHash(token, signingKey)
            return Bytes.concat(token, // This field is the 256-bit SHA256 HMAC, under signing-key.
                signature)
        }

        private fun buildToken(): ByteArray {
            return Bytes.concat(
                // This field denotes which version of the format is being used by the token.
                byteToByteArray(version), // This field is a 64-bit unsigned big-endian integer.
                longToByteArray(timestamp), // The 128-bit Initialization Vector used in AES encryption and decryption of
                // the Ciphertext.
                iv, // This field has variable size, but is always a multiple of 128 bits, the AES
                // block size.
                ciphertext)
        }

        @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
        private fun generateHash(data: ByteArray, signingKey: ByteArray): ByteArray {
            val mac: Mac
            val keySpec = SecretKeySpec(signingKey, "HmacSHA256")
            mac = Mac.getInstance("HmacSHA256")
            mac.init(keySpec)
            return mac.doFinal(data)
        }

        private fun byteToByteArray(value: Byte): ByteArray {
            return byteArrayOf(value)
        }

        /**
         * Returns a big-endian representation of `value` in an 8-element byte
         * array; equivalent to `ByteBuffer.allocate(8).putLong(value).array()`.
         * For example, the input value `0x1213141516171819L` would yield the byte array `{0x12, 0x13, 0x14,
         * 0x15, 0x16, 0x17, 0x18, 0x19}`.
         *
         *
         *
         * If you need to convert and concatenate several values (possibly even of
         * different types), use a shared [ByteBuffer] instance, or use
         * [com.google.common.io.ByteStreams.newDataOutput] to get a growable
         * buffer.
         */
        private fun longToByteArray(value: Long): ByteArray {
            // Note that this code needs to stay compatible with GWT, which has known
            // bugs when narrowing byte casts of long values occur.
            var value = value
            val result = ByteArray(8)
            for (i in 7 downTo 0) {
                result[i] = (value and 0xffL).toByte()
                value = value shr 8
            }
            return result
        }
    }

    private val key: Key

    constructor() {
        // Generate random keys.
        key = Key()
    }

    /**
     * A URL-safe base64-encoded 32-byte key. This must be kept secret. Anyone with
     * this key is able to create and read messages.
     *
     * @param key
     * @throws FernetException
     */
    constructor(key: String?) {
        this.key = Key(key)
    }

    constructor(key: ByteArray?) {
        this.key = Key(key)
    }

    constructor(key: Key) {
        this.key = key
    }

    /**
     * The encrypted message contains the current time when it was generated in
     * plaintext, the time a message was created will therefore be visible to a
     * possible attacker.
     *
     * @param data The message you would like to encrypt.
     * @return A secure message that cannot be read or altered without the key. It
     * is URL-safe base64-encoded. This is referred to as a “Fernet token”.
     * @throws FernetException
     */
    @Throws(FernetException::class)
    fun encrypt(data: ByteArray?): String {
        return base64UrlEncode(encryptRaw(data))
    }

    /**
     * The encrypted message contains the current time when it was generated in
     * plaintext, the time a message was created will therefore be visible to a
     * possible attacker.
     *
     * @param data The message you would like to encrypt.
     * @param token The Fernet token to use.
     * @return A secure message that cannot be read or altered without the key. It
     * is URL-safe base64-encoded. This is referred to as a “Fernet token”.
     * @throws FernetException
     */
    @Throws(FernetException::class)
    fun encrypt(data: ByteArray?, token: Token): String {
        return base64UrlEncode(encryptRaw(data, token))
    }
    /**
     * The encrypted message contains the current time when it was generated in
     * plaintext, the time a message was created will therefore be visible to a
     * possible attacker.
     *
     * @param data The message you would like to encrypt.
     * @return A secure message that cannot be read or altered without the key. This
     * is referred to as a “Fernet token”.
     * @throws FernetException
     */
    /**
     * The encrypted message contains the current time when it was generated in
     * plaintext, the time a message was created will therefore be visible to a
     * possible attacker.
     *
     * @param data The message you would like to encrypt.
     * @param token The Fernet token to use.
     * @return A secure message that cannot be read or altered without the key. This
     * is referred to as a “Fernet token”.
     * @throws FernetException
     */
    @JvmOverloads
    @Throws(FernetException::class)
    fun encryptRaw(data: ByteArray?, token: Token = Token()): ByteArray {
        val ivSpec = IvParameterSpec(token.iv)
        val keySpec = SecretKeySpec(key.encryptionKey, "AES")
        return try {
            // In Java, the standard padding name is PKCS5Padding, not PKCS7Padding.
            // Java is actually performing PKCS #7 padding, but in the JCA specification,
            // PKCS5Padding is the name given.
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val ciphertext = cipher.doFinal(data)
            token.sign(ciphertext, key.signingKey)
        } catch (e: Exception) {
            throw FernetException(e)
        }
    }

    /**
     * @param data The Fernet token. This is the result of calling encrypt().
     * @param ttl Optionally, the number of seconds old a message may be for it to
     * be valid. If the message is older than ttl seconds (from the time
     * it was originally created) an exception will be raised. If ttl is
     * not provided (or is None), the age of the message is not
     * considered.
     * @return The original plaintext.
     * @throws FernetException
     */
    @Throws(FernetException::class)
    fun decrypt(token: Token, ttl: Int): ByteArray {
        token.verify(ttl, key.signingKey)
        return try {
            // 6. Decrypt the ciphertext field using AES 128 in CBC mode with the recorded
            // IV and user-supplied encryption-key.
            val ivSpec = IvParameterSpec(token.iv)
            val keySpec = SecretKeySpec(key.encryptionKey, "AES")

            // In Java, the standard padding name is PKCS5Padding, not PKCS7Padding.
            // Java is actually performing PKCS #7 padding, but in the JCA specification,
            // PKCS5Padding is the name given.
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            cipher.doFinal(token.ciphertext)
        } catch (e: Exception) {
            throw FernetException(e)
        }
    }

    @Throws(FernetException::class)
    fun decrypt(data: String?, ttl: Int): ByteArray {
        val token = Token(data)
        return decrypt(token, ttl)
    }

    @Throws(FernetException::class)
    fun decrypt(data: ByteArray?, ttl: Int): ByteArray {
        val token = Token(data)
        return decrypt(token, ttl)
    }

    /**
     *
     * @param token The Fernet token. This is the result of calling encrypt().
     * @return The original plaintext.
     * @throws FernetException
     */
    @Throws(FernetException::class)
    fun decrypt(token: String?): ByteArray {
        return decrypt(token, 0)
    }

    @JvmOverloads
    @Throws(FernetException::class)
    fun decryptRaw(data: ByteArray?, ttl: Int = 0): ByteArray {
        return decrypt(data, ttl)
    }

    companion object {
        const val VERSION = 0x80.toByte() // 8 bits
        private const val MIN_TOKEN_SIZE = 8 + 64 + 128 + 0 + 256 shr 3
        private const val KEY_SIZE = 128 shr 3
        private const val HMAC_SIZE = 256 shr 3
        private const val MAX_CLOCK_SKEW = 60

        /**
         * Generates a fresh fernet key.
         *
         * Keep this some place safe! If you lose it you’ll no longer be able to decrypt
         * messages; if anyone else gains access to it, they’ll be able to decrypt all
         * of your messages, and they’ll also be able forge arbitrary messages that will
         * be authenticated and decrypted.
         *
         * @return key
         */
        private fun generateKey(): ByteArray {
            val random: SecureRandom
            val key = ByteArray(KEY_SIZE)
            random = try {
                SecureRandom.getInstance("SHA1PRNG")
            } catch (e: NoSuchAlgorithmException) {
                SecureRandom()
            }
            random.nextBytes(key)
            return key
        }

        val time: Long
            get() = System.currentTimeMillis() / 1000L

        fun base64UrlEncode(input: ByteArray?): String {
//            return Base64.encodeUrlSafe(input)
            return Base64.getUrlEncoder().encodeToString(input)
        }

        fun base64UrlDecode(input: String?): ByteArray {
//            return Base64.decodeUrlSafe(input)
            return Base64.getUrlDecoder().decode(input)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val fernet = Fernet()
            println("Key = " + fernet.key)
            try {
                val token = fernet.encrypt("The quick brown fox jumps over the lazy dog.".toByteArray())
                println("Token = $token")
                val message = fernet.decrypt(token)
                println("Message = " + String(message))
            } catch (e: FernetException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
    }
}