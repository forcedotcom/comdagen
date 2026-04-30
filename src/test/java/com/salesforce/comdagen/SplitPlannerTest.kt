package com.salesforce.comdagen

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class SplitPlannerTest {

    @Test
    fun testComputeItemsPerFileDividesEvenlyForCount() {
        assertEquals(20, SplitPlanner.computeItemsPerFile(totalItems = 100, spec = SplitSpec.Count(5)))
    }

    @Test
    fun testComputeItemsPerFileCeilsForUnevenDivision() {
        assertEquals(21, SplitPlanner.computeItemsPerFile(totalItems = 103, spec = SplitSpec.Count(5)))
    }

    @Test
    fun testComputeItemsPerFileFloorsAtOneWhenCountExceedsTotalItems() {
        assertEquals(1, SplitPlanner.computeItemsPerFile(totalItems = 0, spec = SplitSpec.Count(10)))
    }

    @Test
    fun testComputeItemsPerFileDividesBytesBudgetByBytesPerItemForSizeSpec() {
        assertEquals(
            4285,
            SplitPlanner.computeItemsPerFile(
                totalItems = 10_000,
                spec = SplitSpec.Size(3_000_000),
                bytesPerItem = 700L
            )
        )
    }

    @Test
    fun testComputeItemsPerFileThrowsWhenBytesPerItemIsZeroForSizeSpec() {
        assertThrows<IllegalArgumentException> {
            SplitPlanner.computeItemsPerFile(
                totalItems = 10_000,
                spec = SplitSpec.Size(3_000_000),
                bytesPerItem = 0L
            )
        }
    }
}
