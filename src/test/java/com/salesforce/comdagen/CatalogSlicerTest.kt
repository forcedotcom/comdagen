package com.salesforce.comdagen

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CatalogSlicerTest {

    @Test
    fun testSliceSpansProductTypesWhenChunkBoundaryCrossesConcatenation() {
        val products = listOf("P0", "P1", "P2")
        val masters = listOf("M0", "M1", "M2")
        val bundles = emptyList<String>()
        val sets = emptyList<String>()

        val slices = CatalogSlicer.slice(products, masters, bundles, sets, itemsPerFile = 2)

        val expected = listOf(
            CatalogSlice(
                products = listOf("P0", "P1"),
                masters = emptyList(),
                bundles = emptyList<String>(),
                sets = emptyList<String>(),
            ),
            CatalogSlice(
                products = listOf("P2"),
                masters = listOf("M0"),
                bundles = emptyList<String>(),
                sets = emptyList<String>(),
            ),
            CatalogSlice(
                products = emptyList<String>(),
                masters = listOf("M1", "M2"),
                bundles = emptyList<String>(),
                sets = emptyList<String>(),
            ),
        )
        assertEquals(expected, slices)
    }

    @Test
    fun testSliceProducesContiguousItemsAcrossAllFourTypes() {
        val products = listOf("P0", "P1")
        val masters = listOf("M0", "M1")
        val bundles = listOf("B0", "B1")
        val sets = listOf("S0", "S1")

        val slices = CatalogSlicer.slice(products, masters, bundles, sets, itemsPerFile = 3)

        val expected = listOf(
            CatalogSlice(
                products = listOf("P0", "P1"),
                masters = listOf("M0"),
                bundles = emptyList<String>(),
                sets = emptyList<String>(),
            ),
            CatalogSlice(
                products = emptyList<String>(),
                masters = listOf("M1"),
                bundles = listOf("B0", "B1"),
                sets = emptyList<String>(),
            ),
            CatalogSlice(
                products = emptyList<String>(),
                masters = emptyList<String>(),
                bundles = emptyList<String>(),
                sets = listOf("S0", "S1"),
            ),
        )
        assertEquals(expected, slices)
    }
}
