/* ktlint-disable package-name */
package venusbackend
/* ktlint-enable package-name */

fun <T> assertArrayEquals(expected: List<T>, actual: List<T>, message: String = "") {
    if (expected.size != actual.size) {
        throw AssertionError("Expected <$expected>, actual <$actual>. $message")
    }
    for (i in 0 until expected.size) {
        if (expected[i] != actual[i]) {
            throw AssertionError("Expected <$expected>, actual <$actual>. $message")
        }
    }
}