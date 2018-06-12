package com.salesforce.comdagen

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.salesforce.comdagen.config.CatalogListConfiguration
import com.salesforce.comdagen.config.InventoryConfiguration
import com.salesforce.comdagen.config.InventoryRecordConfiguration
import com.salesforce.comdagen.config.ProductConfiguration
import com.salesforce.comdagen.generator.CatalogGenerator
import com.salesforce.comdagen.generator.InventoryGenerator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class InventoryTest {

    companion object {
        private val seed: Long = 1234
    }

    @Test
    fun testInventoryGeneratesCorrectProductIds() {
        val catalogConfig = CatalogListConfiguration(products = ProductConfiguration(elementCount = 100, initialSeed = seed), initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        val recordConfig = InventoryRecordConfiguration(initialSeed = seed)
        val inventoryConfig = InventoryConfiguration(inventoryRecords = recordConfig, initialSeed = seed)
        val inventoryGenerator = InventoryGenerator(configuration = inventoryConfig, catalogConfiguration = catalogConfig)

        val catalogProductIds = catalogGenerator.objects.flatMap { it.products.asSequence().map { it.id } }.toList()
        val inventoryProductIds = inventoryGenerator.objects.flatMap {
            it.inventoryRecords.asSequence().map {
                it.productId
            }
        }.toList()

        // test catalogProductIds totally contained in inventoryProductIds
        assertThat((catalogProductIds.toSet() + inventoryProductIds.toSet()).size, equalTo(inventoryProductIds.size))
    }

    @Test
    fun testInventoryCountRange() {
        val minCount = 15
        val maxCount = 1500

        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val recordConfig = InventoryRecordConfiguration(minCount = minCount, maxCount = maxCount, initialSeed = seed)
        val inventoryConfig = InventoryConfiguration(inventoryRecords = recordConfig, initialSeed = seed)
        val inventoryGenerator = InventoryGenerator(configuration = inventoryConfig, catalogConfiguration = catalogConfig)

        inventoryGenerator.objects.forEach { inventory ->
            inventory.inventoryRecords.forEach { record ->
                assertTrue(record.ats >= minCount)
                assertTrue(record.ats <= maxCount)
            }
        }
    }

    @Test
    fun testInventoryElementCount() {
        val elementCount = 25

        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val recordConfig = InventoryRecordConfiguration(initialSeed = seed)
        val inventoryConfig = InventoryConfiguration(elementCount = elementCount, inventoryRecords = recordConfig, initialSeed = seed)
        val inventoryGenerator = InventoryGenerator(configuration = inventoryConfig, catalogConfiguration = catalogConfig)

        assertEquals(elementCount, inventoryGenerator.objects.count())
    }

    @Test
    fun testInventoryCoverage() {
        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)
        val productElementCount = catalogGenerator.objects.toList().flatMap { it.getAllProducts() }.count()

        val coverage = 0.5F
        val inventoryConfig = InventoryConfiguration(coverage = coverage, inventoryRecords = InventoryRecordConfiguration(initialSeed = seed), initialSeed = seed)
        val inventoryGenerator = InventoryGenerator(configuration = inventoryConfig, catalogConfiguration = catalogConfig)

        val recordCount = (inventoryGenerator.objects.flatMap { it.inventoryRecords }).count()

        assertEquals((productElementCount * coverage).toInt(), recordCount)
    }

    @Test
    fun testInventoryConfigEquals() {
        val configA = InventoryConfiguration(elementCount = 10, inventoryRecords = InventoryRecordConfiguration(initialSeed = seed), initialSeed = seed)
        val configB = InventoryConfiguration(elementCount = 10, inventoryRecords = InventoryRecordConfiguration(initialSeed = seed), initialSeed = seed)
        val configC = InventoryConfiguration(elementCount = 15, inventoryRecords = InventoryRecordConfiguration(initialSeed = seed), initialSeed = seed)

        assertEquals(configA, configB)
        assertNotEquals(configA, configC)
    }
}
