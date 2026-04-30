package com.salesforce.comdagen

sealed class SplitSpec {
    data class Count(val n: Int) : SplitSpec()
    data class Size(val bytes: Long) : SplitSpec()

    companion object {
        private val UNITS = listOf(
            "GB" to 1024L * 1024 * 1024,
            "MB" to 1024L * 1024,
            "KB" to 1024L,
            "B" to 1L,
        )

        fun parse(input: String?): SplitSpec? {
            if (input.isNullOrBlank()) return null
            val trimmed = input.trim()
            if (trimmed.all { it.isDigit() }) {
                val count = trimmed.toInt()
                if (count <= 0) throw IllegalArgumentException("Unsupported split spec: $input")
                return Count(count)
            }
            for ((suffix, multiplier) in UNITS) {
                if (trimmed.endsWith(suffix, ignoreCase = true)) {
                    val prefix = trimmed.dropLast(suffix.length).trim()
                    if (prefix.isNotEmpty() && prefix.all { it.isDigit() }) {
                        return Size(prefix.toLong() * multiplier)
                    }
                }
            }
            throw IllegalArgumentException("Unsupported split spec: $input")
        }
    }
}
