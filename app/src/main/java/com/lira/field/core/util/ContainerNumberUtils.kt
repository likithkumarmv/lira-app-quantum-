package com.lira.field.core.util

/**
 * ISO 6346 Container Number Utilities
 * Validates check digit and formats container numbers as e.g. "MSCU 661920-0"
 */
object ContainerNumberUtils {

    // ISO 6346 letter values: multiples of 11 are omitted (11, 22, 33)
    private val LETTER_VALUES = mapOf(
        'A' to 10, 'B' to 12, 'C' to 13, 'D' to 14, 'E' to 15,
        'F' to 16, 'G' to 17, 'H' to 18, 'I' to 19, 'J' to 20,
        'K' to 21, 'L' to 23, 'M' to 24, 'N' to 25, 'O' to 26,
        'P' to 27, 'Q' to 28, 'R' to 29, 'S' to 30, 'T' to 31,
        'U' to 32, 'V' to 34, 'W' to 35, 'X' to 36, 'Y' to 37, 'Z' to 38
    )

    private val POWERS_OF_TWO = intArrayOf(1, 2, 4, 8, 16, 32, 64, 128, 256, 512)

    /**
     * Calculates the ISO 6346 check digit for the first 10 characters.
     * Returns -1 if input length < 10 or invalid characters.
     */
    fun calculateCheckDigit(first10: String): Int {
        val clean = first10.replace("[^A-Za-z0-9]".toRegex(), "").uppercase()
        if (clean.length < 10) return -1

        var sum = 0
        for (i in 0 until 10) {
            val c = clean[i]
            val value = when {
                c in '0'..'9' -> c - '0'
                c in 'A'..'Z' -> LETTER_VALUES[c] ?: return -1
                else -> return -1
            }
            sum += value * POWERS_OF_TWO[i]
        }

        val remainder = sum % 11
        return if (remainder == 10) 0 else remainder
    }

    /**
     * Validates an 11-character ISO 6346 container identifier.
     */
    fun validateIso6346(raw: String): Boolean {
        val clean = raw.replace("[^A-Za-z0-9]".toRegex(), "").uppercase()
        if (clean.length != 11) return false

        val first10 = clean.substring(0, 10)
        val expectedCheckDigit = calculateCheckDigit(first10)
        if (expectedCheckDigit < 0) return false

        val actualCheckDigit = clean[10] - '0'
        return expectedCheckDigit == actualCheckDigit
    }

    fun isValid(raw: String): Boolean = validateIso6346(raw)

    fun format(raw: String): String = formatIso6346(raw)

    /**
     * Formats container number into standard ISO 6346 representation:
     * e.g. "MSCU6619200" -> "MSCU 661920-0"
     */
    fun formatIso6346(raw: String): String {
        val clean = raw.replace("[^A-Za-z0-9]".toRegex(), "").uppercase()
        if (clean.length == 11) {
            val owner = clean.substring(0, 4)
            val serial = clean.substring(4, 10)
            val check = clean.substring(10, 11)
            return "$owner $serial-$check"
        }
        if (clean.length == 10) {
            val owner = clean.substring(0, 4)
            val serial = clean.substring(4, 10)
            val calculated = calculateCheckDigit(clean)
            return if (calculated >= 0) "$owner $serial-$calculated" else clean
        }
        return raw
    }
}
