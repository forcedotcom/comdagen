package com.salesforce.comdagen

import com.salesforce.comdagen.config.BundleProductConfiguration
import com.salesforce.comdagen.config.CatalogListConfiguration
import com.salesforce.comdagen.config.CategoryConfiguration
import com.salesforce.comdagen.config.ProductConfiguration
import com.salesforce.comdagen.config.ProductSetConfiguration
import com.salesforce.comdagen.config.VariationAttributeConfiguration
import com.salesforce.comdagen.config.VariationProductConfiguration
import com.salesforce.comdagen.generator.CatalogGenerator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogChunkerTest {

    private val seed: Long = 1234

    @Test
    fun testChunker_thresholdZero_returnsSingleChunkWithWholeCatalog() {
        val catalogConfig = CatalogListConfiguration(
            products = ProductConfiguration(elementCount = 10, initialSeed = seed),
            initialSeed = seed
        )
        val catalog = CatalogGenerator(configuration = catalogConfig).objects.first()

        val chunks = CatalogChunker(maxProductsPerFile = 0).chunk(catalog, "catalog.xml")

        assertEquals(1, chunks.size)
        val chunk = chunks.single()
        assertEquals("catalog.xml", chunk.fileName)
        assertTrue(chunk.isFirstFile)
        assertEquals(catalog.products.toList(), chunk.products)
        assertEquals(catalog.masterProducts.toList(), chunk.masterProducts)
        assertEquals(catalog.bundles, chunk.bundles)
        assertEquals(catalog.productSets, chunk.productSets)
        assertEquals(catalog.categoryAssignments.toList().size, chunk.categoryAssignments.size)
    }

    @Test
    fun testChunker_thresholdBelowTotal_producesExpectedNumberOfChunks() {
        val catalogConfig = CatalogListConfiguration(
            products = ProductConfiguration(elementCount = 15, initialSeed = seed),
            initialSeed = seed
        )
        val catalog = CatalogGenerator(configuration = catalogConfig).objects.first()

        val chunks = CatalogChunker(maxProductsPerFile = 6).chunk(catalog, "catalog.xml")

        assertEquals(3, chunks.size)
    }

    @Test
    fun testChunker_fileNames_firstIsBaseAndSubsequentAreSuffixed() {
        val catalogConfig = CatalogListConfiguration(
            products = ProductConfiguration(elementCount = 15, initialSeed = seed),
            initialSeed = seed
        )
        val catalog = CatalogGenerator(configuration = catalogConfig).objects.first()

        val chunks = CatalogChunker(maxProductsPerFile = 6).chunk(catalog, "catalog.xml")

        assertEquals(listOf("catalog.xml", "catalog-2.xml", "catalog-3.xml"), chunks.map { it.fileName })
    }

    @Test
    fun testChunker_categoryAssignments_followTheirProductIntoChunk() {
        val catalogConfig = CatalogListConfiguration(
            products = ProductConfiguration(elementCount = 15, initialSeed = seed),
            categoryConfig = CategoryConfiguration(elementCount = 5),
            initialSeed = seed
        )
        val catalog = CatalogGenerator(configuration = catalogConfig).objects.first()

        val chunks = CatalogChunker(maxProductsPerFile = 6).chunk(catalog, "catalog.xml")

        val totalAssignmentsInChunks = chunks.sumOf { it.categoryAssignments.size }
        val totalAssignmentsInCatalog = catalog.categoryAssignments.toList().size
        assertEquals(totalAssignmentsInCatalog, totalAssignmentsInChunks)

        chunks.forEach { chunk ->
            val productIds = chunk.products.map { it.id }.toSet()
            val offender = chunk.categoryAssignments.firstOrNull { it.product.id !in productIds }
            assertTrue(
                offender == null,
                "Chunk ${chunk.fileName} has assignment for product ${offender?.product?.id} " +
                        "but that product id is not in the chunk's product ids $productIds"
            )
        }
    }

    @Test
    fun testChunker_thresholdBelowTotal_chunksBundlesAcrossFiles() {
        val catalogConfig = CatalogListConfiguration(
            products = ProductConfiguration(elementCount = 0, initialSeed = seed),
            bundleConfig = BundleProductConfiguration(elementCount = 15),
            initialSeed = seed
        )
        val catalog = CatalogGenerator(configuration = catalogConfig).objects.first()

        val chunks = CatalogChunker(maxProductsPerFile = 6).chunk(catalog, "catalog.xml")

        assertEquals(3, chunks.size)
        assertEquals(catalog.bundles.size, chunks.sumOf { it.bundles.size })
        assertEquals(6, chunks[0].bundles.size)
        assertEquals(6, chunks[1].bundles.size)
        assertEquals(3, chunks[2].bundles.size)
    }

    @Test
    fun testChunker_thresholdBelowTotal_chunksProductSetsAcrossFiles() {
        val catalogConfig = CatalogListConfiguration(
            products = ProductConfiguration(elementCount = 5, initialSeed = seed),
            productSets = ProductSetConfiguration(
                elementCount = 15,
                minSetProducts = 2,
                maxSetProducts = 2
            ),
            initialSeed = seed
        )
        val catalog = CatalogGenerator(configuration = catalogConfig).objects.first()

        val chunks = CatalogChunker(maxProductsPerFile = 6).chunk(catalog, "catalog.xml")

        assertEquals(3, chunks.size)
        assertEquals(15, chunks.sumOf { it.productSets.size })
        assertEquals(6, chunks[0].productSets.size)
        assertEquals(6, chunks[1].productSets.size)
        assertEquals(3, chunks[2].productSets.size)
    }

    @Test
    fun testChunker_thresholdBelowTotal_chunksMasterProductsAcrossFiles() {
        val catalogConfig = CatalogListConfiguration(
            products = ProductConfiguration(elementCount = 0, initialSeed = seed),
            variationProducts = listOf(
                VariationProductConfiguration(
                    elementCount = 15,
                    localVariationAttributes = listOf(
                        VariationAttributeConfiguration(name = "size", values = listOf("M"))
                    )
                )
            ),
            initialSeed = seed
        )
        val catalog = CatalogGenerator(configuration = catalogConfig).objects.first()

        val chunks = CatalogChunker(maxProductsPerFile = 6).chunk(catalog, "catalog.xml")

        assertEquals(15, chunks.sumOf { it.masterProducts.size })
        assertTrue(chunks.size >= 3)
        assertTrue(
            chunks[0].masterProducts.size <= 6,
            "first chunk masterProducts=${chunks[0].masterProducts.size} should not exceed threshold 6"
        )
    }
}
