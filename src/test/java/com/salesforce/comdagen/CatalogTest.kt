package com.salesforce.comdagen

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.hasSize
import com.salesforce.comdagen.config.*
import com.salesforce.comdagen.generator.CatalogGenerator
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.Category
import com.salesforce.comdagen.model.Product
import com.salesforce.comdagen.model.ProductOption
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CatalogTest {

    private val seed: Long = 1234

    @Test
    fun testCatalogProductCount() {

        val elementCount = 150

        val productConfig = ProductConfiguration(elementCount = elementCount, initialSeed = seed)

        val catalogConfig = CatalogListConfiguration(products = productConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            assertEquals(elementCount, catalog.products.count())
        }
    }

    @Test
    fun testCatalogCategoryCount() {
        val categoryDepth = 2
        val categoryBreadth = 2
        val categoryCount = 50

        val categoryConfig = CategoryConfiguration(elementCount = categoryCount, categoryTreeBreadth = categoryBreadth,
                categoryTreeDepth = categoryDepth)

        val catalogConfig = CatalogListConfiguration(categoryConfig = categoryConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            assertEquals(categoryCount, catalog.categories.size)
        }
    }

    @Test
    fun testCatalogZeroCategoryTreeSize() {
        val categoryDepth = 0
        val categoryBreadth = 0
        val categoryCount = 10

        val categoryConfig = CategoryConfiguration(elementCount = categoryCount, categoryTreeDepth = categoryDepth,
                categoryTreeBreadth = categoryBreadth)

        val catalogConfig = CatalogListConfiguration(categoryConfig = categoryConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            assertEquals(categoryCount, catalog.categories.size)
        }
    }

    @Test
    fun testCatalogProductIdDuplicates() {
        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        val products: List<Product> = catalogGenerator.objects.flatMap {
            it.products.asSequence().plus(it.masterProducts).plus(it.masterProducts.flatMap { it.variants.asSequence() })
                    .plus(it.bundles)
        }.toList()

        // Check if generated catalogs contain duplicate product ids
        assertEquals(products.size, products.distinctBy { it.id }.size)
    }

    @Test
    fun testCatalogCategoryIdDuplicates() {
        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        val categories: List<Category> = catalogGenerator.objects.flatMap {
            it.categories.asSequence()
        }.toList()

        assertEquals(categories.size, categories.distinctBy { it.id }.size)
    }

    @Test
    fun testCatalogCategoryAssignments() {
        val bundleConfig = BundleProductConfiguration()
        val variationConfig = VariationProductConfiguration()
        val sharedVariationAttributes = listOf(
                VariationAttributeConfiguration("color", listOf("blue", "red", "green")),
                VariationAttributeConfiguration("size", listOf("1", "2", "3"))
        )
        val catalogConfig = CatalogListConfiguration(bundleConfig = bundleConfig, variationProducts = listOf(variationConfig),
                sharedVariationAttributes = sharedVariationAttributes, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            val products = catalog.products.plus(catalog.masterProducts).plus(catalog.bundles)
            val assignedProducts: Sequence<Product> = catalog.categoryAssignments.map { it.product }

            products.forEach { product ->
                assertTrue(assignedProducts.contains(product), "Product ${product.id} is not assigned to any category.")
            }
        }
    }

    @Test
    fun testCatalogBundles() {
        val bundleConfig = BundleProductConfiguration()
        val catalogConfig = CatalogListConfiguration(bundleConfig = bundleConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            assertEquals(emptyList(), catalog.bundles.flatMap { it.bundledProducts.keys } - catalog.products, "Bundle products not in the catalog")
        }
    }

    @Test
    fun testCatalogBundleCount() {
        val bundleCount = 10
        val bundleConfig = BundleProductConfiguration(elementCount = bundleCount)
        val catalogConfig = CatalogListConfiguration(bundleConfig = bundleConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            assertEquals(bundleCount, catalog.bundles.size)
        }
    }

    @Test
    fun testCatalogBundledProductCount() {
        val minBundledProducts = 4
        val maxBundledProducts = 10
        val bundleConfig = BundleProductConfiguration(minBundledProducts = minBundledProducts, maxBundledProducts = maxBundledProducts)
        val catalogConfig = CatalogListConfiguration(bundleConfig = bundleConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            catalog.bundles.forEach { bundle ->
                assertTrue(bundle.bundledProducts.size >= minBundledProducts)
                assertTrue(bundle.bundledProducts.size <= maxBundledProducts)
            }
        }
    }

    @Test
    fun testCatalogBundledProductsQuantity() {
        val minQuantity = 3
        val maxQuantity = 15
        val bundleConfig = BundleProductConfiguration(minQuantity = minQuantity, maxQuantity = maxQuantity)
        val catalogConfig = CatalogListConfiguration(bundleConfig = bundleConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            catalog.bundles.forEach { bundle ->
                bundle.bundledProducts.forEach { bundledProduct ->
                    assertTrue(bundledProduct.value >= minQuantity)
                    assertTrue(bundledProduct.value <= maxQuantity)
                }
            }
        }
    }

    @Test
    fun testCatalogProductSetCount() {
        val productSetCount = 10
        val productSetConfig = ProductSetConfiguration(elementCount = productSetCount)
        val catalogConfig = CatalogListConfiguration(productSets = productSetConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            assertEquals(productSetCount, catalog.productSets.size)
        }
    }

    @Test
    fun testCatalogSetProductsCount() {
        val minSetProducts = 3
        val maxSetProducts = 10
        val productSetConfig = ProductSetConfiguration(minSetProducts = minSetProducts, maxSetProducts = maxSetProducts)
        val catalogConfig = CatalogListConfiguration(productSets = productSetConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            catalog.productSets.forEach { set ->
                assertTrue(set.products.size >= minSetProducts)
                assertTrue(set.products.size <= maxSetProducts)
            }
        }
    }

    @Test
    fun testCatalogVariationCount() {
        val variationCount = 20

        val sharedVariationAttributesConfig = listOf(
                VariationAttributeConfiguration("color", listOf("blue", "red", "green")),
                VariationAttributeConfiguration("size", listOf("1", "2", "3", "4"))
        )
        val variationConfig = VariationProductConfiguration(elementCount = variationCount)
        val catalogConfig = CatalogListConfiguration(variationProducts = listOf(variationConfig),
                sharedVariationAttributes = sharedVariationAttributesConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            assertEquals(variationCount, catalog.masterProducts.count())
        }
    }

    @Test
    fun testCatalogVariantsCount() {
        val sharedVariationAttributesConfig = listOf(
                VariationAttributeConfiguration("color", listOf("blue", "red", "green")),
                VariationAttributeConfiguration("size", listOf("1", "2", "3", "4"))
        )

        // how many different value combinations are possible
        val variationAttributeValueCount = sharedVariationAttributesConfig.map { it.values.size }.reduce { total, next ->
            total * next
        }

        val variationConfig = VariationProductConfiguration(sharedVariationAttributes = listOf("color", "size"))
        val catalogConfig = CatalogListConfiguration(variationProducts = listOf(variationConfig),
                sharedVariationAttributes = sharedVariationAttributesConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            catalog.masterProducts.forEach { variantMaster ->
                assertEquals(variationAttributeValueCount, variantMaster.variants.size)
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testVariantConfigProbabilityEnforced() {
        VariationAttributeConfiguration("test", listOf("1"), 2.0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testVariantConfigNegativeProbabilityRejected() {
        VariationAttributeConfiguration("test", listOf("1"), -1.4f)
    }

    @Test
    fun testVariantProbabilityOneGeneratesAllVariants() {

        val numberOfProductMasters = 10
        val variationValues = listOf("S", "M", "L")

        val variationConfig = VariationProductConfiguration(elementCount = numberOfProductMasters, localVariationAttributes = listOf(
                VariationAttributeConfiguration("size", variationValues, probability = 1.0f)))
        val catalogConfig = CatalogListConfiguration(
                elementCount = 1,
                products = ProductConfiguration(elementCount = 0 /* to make it easier to compute */, initialSeed = seed),
                variationProducts = listOf(variationConfig), initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            assertEquals(numberOfProductMasters * variationValues.size, catalog.masterProducts.sumBy { it.variants.size })
        }
    }

    @Test
    fun testVariantProbabilityZeroGeneratesNoVariants() {
        val numberOfProductMasters = 10
        val variationValues = listOf("S", "M", "L")

        val variationConfig = VariationProductConfiguration(elementCount = numberOfProductMasters, localVariationAttributes = listOf(
                VariationAttributeConfiguration("size", variationValues, probability = 0f)))
        val catalogConfig = CatalogListConfiguration(
                elementCount = 1,
                products = ProductConfiguration(elementCount = 0 /* to make it easier to compute */, initialSeed = seed),
                variationProducts = listOf(variationConfig), initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            assertEquals(0, catalog.masterProducts.sumBy { it.variants.size })
        }
    }

    @Test
    fun testCatalogElementCount() {
        val elementCount = 4

        val catalogConfig = CatalogListConfiguration(elementCount = elementCount, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        assertEquals(elementCount, catalogGenerator.objects.count())
    }

    @Test
    fun testCatalogSharedOptionsCount() {
        val optionCount = 20
        val sharedOptionConfig = ProductOptionConfiguration(elementCount = optionCount)
        val catalogConfig = CatalogListConfiguration(sharedOptions = sharedOptionConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            assertEquals(optionCount, catalog.sharedOptions.size)
        }
    }

    @Test
    fun testCatalogSharedOptionsValueCount() {
        val minValues = 3
        val maxValues = 15
        val sharedOptionConfig = ProductOptionConfiguration(minValues = minValues, maxValues = maxValues)
        val catalogConfig = CatalogListConfiguration(sharedOptions = sharedOptionConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            catalog.sharedOptions.forEach { option ->
                assertTrue(option.dataStore.size >= minValues)
                assertTrue(option.dataStore.size <= maxValues)
            }
        }
    }

    @Test
    fun testCatalogSharedOptionsPrice() {
        val minPrice = 5.0
        val maxPrice = 200.0

        val sharedOptionConfig = ProductOptionConfiguration(minPrice = minPrice, maxPrice = maxPrice)
        val catalogConfig = CatalogListConfiguration(sharedOptions = sharedOptionConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.forEach { catalog ->
            catalog.sharedOptions.forEach { option ->
                option.dataStore.forEach { value ->
                    assertTrue(value.prices["USD"]!! >= minPrice)
                    assertTrue(value.prices["USD"]!! <= maxPrice)
                }
            }
        }
    }

    @Test
    fun `local options are assigned to product`() {
        val localOptionConfig = ProductOptionConfiguration(elementCount = 2, probability = 1.0f)
        val productConfig = ProductConfiguration(initialSeed = seed, options = localOptionConfig)
        val catalogConfig = CatalogListConfiguration(products = productConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        catalogGenerator.objects.first().products.forEach {
            assertThat(it.localOptions, hasSize(equalTo(2)))
        }
    }

    @Test
    fun testCatalogSharedOptionsMeta() {
        val optionsCount = 20

        val sharedOptionsConfig = ProductOptionConfiguration(elementCount = optionsCount)
        val catalogConfig = CatalogListConfiguration(sharedOptions = sharedOptionsConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        val sharedOptions: List<ProductOption> = catalogGenerator.objects.toList().flatMap { it.sharedOptions }

        sharedOptions.forEach { option ->
            assertThat(catalogGenerator.productCustomAttributes, hasElement<AttributeDefinition>(option))
        }
    }

    @Test
    fun testCatalogLocalOptionsMeta() {
        val optionsCount = 5

        val localOptionsConfig = ProductOptionConfiguration(elementCount = optionsCount)
        val productConfig = ProductConfiguration(options = localOptionsConfig, initialSeed = seed)
        val catalogConfig = CatalogListConfiguration(products = productConfig, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        val localOptions = catalogGenerator.objects.flatMap { it.products.flatMap { it.localOptions.asSequence() } }

        localOptions.forEach { option ->
            assertThat(catalogGenerator.productCustomAttributes, hasElement<AttributeDefinition>(option))
        }
    }

    @Test
    fun testCatalogConfigEquals() {
        val catalogConfigA = CatalogListConfiguration(elementCount = 10, initialSeed = seed)
        val catalogConfigB = CatalogListConfiguration(elementCount = 10, initialSeed = seed)
        val catalogConfigC = CatalogListConfiguration(elementCount = 15, initialSeed = seed)

        assertEquals(catalogConfigA, catalogConfigB)
        assertNotEquals(catalogConfigA, catalogConfigC)
    }

    // #108
    @Test
    fun `products are identical under iteration`() {
        val catalogConfig = CatalogListConfiguration(elementCount = 1, initialSeed = seed,
                products = ProductConfiguration(initialSeed = seed, options = ProductOptionConfiguration()))
        val catalogGenerator = CatalogGenerator(catalogConfig)

        val seq = catalogGenerator.objects.first().products
        assertThat(seq.toList(), equalTo(seq.toList()))
    }
}
