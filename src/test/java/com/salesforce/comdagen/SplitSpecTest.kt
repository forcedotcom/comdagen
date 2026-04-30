package com.salesforce.comdagen

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SplitSpecTest {

    @Test
    fun testParseReturnsNullForNullInput() {
        assertNull(SplitSpec.parse(null))
    }

    @Test
    fun testParseReturnsNullForEmptyString() {
        assertNull(SplitSpec.parse(""))
    }

    @Test
    fun testParseReturnsNullForWhitespaceOnlyString() {
        assertNull(SplitSpec.parse("\t\n "))
    }

    @Test
    fun testParseReturnsCountForSingleDigitInput() {
        assertEquals(SplitSpec.Count(5), SplitSpec.parse("5"))
    }

    @Test
    fun testParseReturnsCountForMinimalInput() {
        assertEquals(SplitSpec.Count(1), SplitSpec.parse("1"))
    }

    @Test
    fun testParseReturnsSizeForMegabyteSuffix() {
        assertEquals(SplitSpec.Size(bytes = 100L * 1024 * 1024), SplitSpec.parse("100MB"))
    }

    @Test
    fun testParseIsCaseInsensitiveForUnitSuffix() {
        assertEquals(SplitSpec.Size(bytes = 7L * 1024 * 1024), SplitSpec.parse("7mb"))
    }

    @Test
    fun testParseAllowsWhitespaceBetweenNumberAndUnit() {
        assertEquals(SplitSpec.Size(bytes = 42L * 1024 * 1024), SplitSpec.parse("42 MB"))
    }

    @Test
    fun testParseReturnsSizeForGigabyteSuffix() {
        assertEquals(SplitSpec.Size(bytes = 2L * 1024 * 1024 * 1024), SplitSpec.parse("2GB"))
    }

    @Test
    fun testParseReturnsSizeForKilobyteSuffix() {
        assertEquals(SplitSpec.Size(bytes = 512L * 1024), SplitSpec.parse("512KB"))
    }

    @Test
    fun testParseReturnsSizeForByteSuffix() {
        assertEquals(SplitSpec.Size(bytes = 2048L), SplitSpec.parse("2048B"))
    }

    @Test
    fun testParseRejectsZeroCount() {
        assertThrows<IllegalArgumentException> { SplitSpec.parse("0") }
    }
}
