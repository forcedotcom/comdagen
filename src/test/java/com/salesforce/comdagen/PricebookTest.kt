package com.salesforce.comdagen

import com.salesforce.comdagen.config.*
import com.salesforce.comdagen.generator.CatalogGenerator
import com.salesforce.comdagen.generator.PricebookGenerator
import com.salesforce.comdagen.model.Amount
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PricebookTest {

    companion object {
        private val seed: Long = 1234
    }

    @Test
    fun testPricebookGeneratesCorrectProductIds() {
        val catalogConfig = CatalogListConfiguration(
            products = ProductConfiguration(elementCount = 100, initialSeed = seed),
            initialSeed = seed
        )
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        val pricebookConfig = PricebookConfiguration(id = "pricebook", initialSeed = seed)
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = listOf(SupportedCurrency.USD)
        )

        val catalogProductIds: List<String> = catalogGenerator.objects.flatMap { it.products.map { it.id } }.toList()
        val pricebookProductIds: List<String> = pricebookGenerator.objects.flatMap {
            it.pricetables.map {
                it.productId
            }
        }.toList()

        catalogProductIds.forEach {
            assertTrue(pricebookProductIds.contains(it))
        }
    }

    @Test
    fun testPricebookEntriesMatchesWithProductCount() {
        val sharedVariationAttributes = listOf(
            VariationAttributeConfiguration("color", listOf("blue", "red", "green")),
            VariationAttributeConfiguration("size", listOf("1", "2", "3"))
        )
        val masterProductConfig = VariationProductConfiguration()
        val catalogConfig = CatalogListConfiguration(
            variationProducts = listOf(masterProductConfig),
            sharedVariationAttributes = sharedVariationAttributes, initialSeed = seed
        )
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        val pricebookConfig = PricebookConfiguration(id = "pricebook", initialSeed = seed)
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = listOf(SupportedCurrency.USD)
        )

        val productCount: Int = catalogGenerator.objects.map {
            it.products.count() + it.masterProducts.map { it.variants.size }
                .reduce { total, next -> total + next }
        }.reduce { total, next -> total + next }

        pricebookGenerator.objects.forEach { pricebook ->
            assertEquals(productCount, pricebook.pricetables.count())
        }
    }

    @Test
    fun testPricebookContainsProductVariations() {
        val sharedVariationAttributes = listOf(
            VariationAttributeConfiguration("color", listOf("blue", "red", "green")),
            VariationAttributeConfiguration("size", listOf("1", "2", "3"))
        )
        val masterProductConfig = VariationProductConfiguration()

        val catalogConfig = CatalogListConfiguration(
            variationProducts = listOf(masterProductConfig),
            sharedVariationAttributes = sharedVariationAttributes, initialSeed = seed
        )
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        val pricebookConfig = PricebookConfiguration(id = "pricebook", initialSeed = seed)
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = listOf(SupportedCurrency.USD)
        )

        val catalogVariationIds: List<String> = catalogGenerator.objects.flatMap {
            it.masterProducts.asSequence().flatMap {
                it.variants.asSequence().map { it.id }
            }
        }.toList()
        val catalogMasterIds: List<String> =
            catalogGenerator.objects.flatMap { it.masterProducts.asSequence().map { it.id } }.toList()

        val pricebookProductIds: List<String> = pricebookGenerator.objects.flatMap {
            it.pricetables.asSequence().map {
                it.productId
            }
        }.toList()

        // check if pricebook contains all product variations
        catalogVariationIds.forEach {
            assertTrue(
                pricebookProductIds.contains(it),
                "Pricebook should contain all product variations"
            )
        }

        // check that pricebook does not contain pricetables for variation master products
        catalogMasterIds.forEach {
            assertFalse(
                pricebookProductIds.contains(it),
                "Pricebook should not contain variation master products"
            )
        }
    }

    @Test
    fun testPricebookContainProductSets() {
        val productSetConfig = ProductSetConfiguration()
        val catalogConfig = CatalogListConfiguration(productSets = productSetConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        val pricebookConfig = PricebookConfiguration(id = "pricebook", initialSeed = seed)
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = listOf(SupportedCurrency.USD)
        )

        val productSetIds: List<String> = catalogGenerator.objects.toList().flatMap { catalog ->
            catalog.productSets.map { it.id }
        }

        val pricebookProductIds: Sequence<String> = pricebookGenerator.objects.flatMap { pricebook ->
            pricebook.pricetables.map { it.productId }
        }

        assertEquals(productSetIds.size, pricebookProductIds.toList().intersect(productSetIds).size)
    }

    @Test
    fun testPricebookMultipleCurrencies() {
        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val pricebookConfig = PricebookConfiguration(id = "pricebook", initialSeed = seed)
        val currencies = listOf(SupportedCurrency.USD, SupportedCurrency.EUR, SupportedCurrency.CNY)
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = currencies
        )

        val generatedCurrencies: List<String> = pricebookGenerator.objects.map { it.currency }.toList()

        currencies.forEach {
            assertTrue(
                generatedCurrencies.contains(it.toString()),
                "Pricebooks should get generated for all provided currencies"
            )
        }
    }

    @Test
    fun testShouldGenerateSalesPricebooks() {
        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val salesPricebookConfig = PricebookConfiguration(id = "sales", sales = true, initialSeed = seed)
        val pricebookConfig =
            PricebookConfiguration(id = "pricebook", children = listOf(salesPricebookConfig), initialSeed = seed)
        val currencies = listOf(SupportedCurrency.USD)
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = listOf(SupportedCurrency.USD)
        )

        assertEquals(currencies.size * 2, pricebookGenerator.objects.toList().size, "Should generate 2 pricebooks")

        assertTrue {
            pricebookGenerator.objects.forEach {
                if (it.id.contains("sale")) {
                    return@assertTrue true
                }
            }
            return@assertTrue false
        }
    }

    @Test
    fun testPricebookElementCount() {
        val elementCount = 20
        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val pricebookConfig = PricebookConfiguration(id = "pricebook", elementCount = elementCount, initialSeed = seed)
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = listOf(SupportedCurrency.USD)
        )

        assertEquals(elementCount, pricebookGenerator.objects.count())
    }

    @Test
    fun testPricebookIdUniqueness() {
        val elementCount = 20
        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val pricebookConfig = PricebookConfiguration(id = "pricebook", elementCount = elementCount, initialSeed = seed)
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig,
            currencies = listOf(SupportedCurrency.USD, SupportedCurrency.CNY, SupportedCurrency.EUR)
        )

/*        assertEquals(
            pricebookGenerator.objects.toList().size,
            pricebookGenerator.objects.toList().distinctBy { it.id }.size
        )*/
    }

    @Test
    fun testPricebookCoverage() {
        val coverage = 0.25f
        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val pricebookConfig = PricebookConfiguration(id = "pricebook", coverage = coverage, initialSeed = seed)
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = listOf(SupportedCurrency.USD)
        )

        val shouldCount = (coverage * GeneratorHelper.getProductIds(catalogConfig).count()).toInt()
        val isCount = (pricebookGenerator.objects.flatMap { it.pricetables }).count()

        assertEquals(shouldCount, isCount)
    }

    @Test
    fun testPricebookAmountCount() {
        val minAmountCount = 2
        val maxAmountCount = 10

        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val pricebookConfig = PricebookConfiguration(
            id = "pricebook",
            minAmountCount = minAmountCount,
            maxAmountCount = maxAmountCount,
            initialSeed = seed
        )
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = listOf(SupportedCurrency.USD)
        )

        val pricetables = pricebookGenerator.objects.flatMap { it.pricetables.asSequence() }

        pricetables.forEach { priceTable ->
            assert(priceTable.amounts.size >= minAmountCount)
            assert(priceTable.amounts.size <= maxAmountCount)
        }
    }

    @Test
    fun testPricebookAmountRange() {
        val minAmount = 100.0
        val maxAmount = 5000.0

        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val pricebookConfig =
            PricebookConfiguration(id = "pricebook", minAmount = minAmount, maxAmount = maxAmount, initialSeed = seed)
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = listOf(SupportedCurrency.USD)
        )

        // get all amounts with quantity = 1
        val amounts: Sequence<Amount> =
            pricebookGenerator.objects.flatMap { it.pricetables.asSequence().map { it.amounts[0] } }

        amounts.forEach {
            assert(it.amount >= minAmount)
            assert(it.amount <= maxAmount)
        }
    }

    @Test
    fun testPricebookGeneratedCustomAttributesCount() {
        val customAttributeElementCount = 30

        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val pricebookConfig = PricebookConfiguration(
            id = "pricebook",
            generatedAttributes = GeneratedAttributeConfig(customAttributeElementCount),
            initialSeed = seed
        )
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = listOf(SupportedCurrency.USD)
        )

        pricebookGenerator.objects.forEach { pricebook ->
            assertEquals(customAttributeElementCount, pricebook.customAttributes.size)
        }
    }

    @Test
    fun testPricebookCustomAttributes() {
        val name = "foobar"
        val type = AttributeConfig.DataType.STRING
        val dataStore = "foobar"
        val generationStrategy = AttributeConfig.GenerationStrategy.STATIC

        val attributeConfig = AttributeConfig(
            type = type,
            dataStore = dataStore,
            generationStrategy = generationStrategy,
            searchable = false
        )
        val customAttributesConfig: Map<String, AttributeConfig> = mapOf(name to attributeConfig)

        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val pricebookConfig =
            PricebookConfiguration(id = "pricebook", customAttributes = customAttributesConfig, initialSeed = seed)
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = listOf(SupportedCurrency.USD)
        )

        pricebookGenerator.objects.forEach { pricebook ->
            assertEquals(customAttributesConfig.values.size, pricebook.customAttributes.size)
            pricebook.customAttributes.forEach { attribute ->
                assertEquals(name, attribute.definition.id)
                assertEquals(dataStore, attribute.value)
                assertEquals(type, attribute.definition.type)
            }
        }
    }

    @Test
    fun testPricebookConfigEquals() {
        val configA = PricebookConfiguration(elementCount = 10, id = "pricebook", initialSeed = seed)
        val configB = PricebookConfiguration(elementCount = 10, id = "pricebook", initialSeed = seed)
        val configC = PricebookConfiguration(elementCount = 15, id = "pricebook", initialSeed = seed)

        assertEquals(configA, configB)
        assertNotEquals(configA, configC)
    }
}
