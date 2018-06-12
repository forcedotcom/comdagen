package com.salesforce.comdagen

import com.salesforce.comdagen.config.CatalogListConfiguration
import com.salesforce.comdagen.config.RedirectUrlConfiguration
import com.salesforce.comdagen.generator.CatalogGenerator
import com.salesforce.comdagen.generator.RedirectUrlGenerator
import com.salesforce.comdagen.model.CategoryRedirectUrl
import com.salesforce.comdagen.model.ProductRedirectUrl
import com.salesforce.comdagen.model.StaticRedirectUrl
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RedirectUrlTest {

    companion object {
        val seed: Long = 12356
    }

    @Test
    fun testStaticRedirectUrlElementCount() {
        val elementCount = 15

        val config = RedirectUrlConfiguration(elementCount = elementCount, initialSeed = seed)
        val generator = RedirectUrlGenerator(config)

        assertEquals(elementCount, generator.objects.filter { it is StaticRedirectUrl }.count())
    }

    @Test
    fun testProductRedirectUrlElementCount() {
        val productRedirects = 15

        val catalogGenerator = CatalogGenerator(CatalogListConfiguration(initialSeed = seed))
        val categoryAssignments = catalogGenerator.objects.flatMap { it.categoryAssignments }

        val config = RedirectUrlConfiguration(productRedirects = productRedirects, initialSeed = seed)
        val generator = RedirectUrlGenerator(config, categoryAssignments = categoryAssignments)

        assertEquals(productRedirects, generator.objects.filter { it is ProductRedirectUrl }.count())
    }

    @Test
    fun testCategoryRedirectUrlElementCount() {
        val categoryRedirects = 15

        val catalogGenerator = CatalogGenerator(CatalogListConfiguration(initialSeed = seed))
        val categories = catalogGenerator.objects.toList().flatMap { it.categories }

        val config = RedirectUrlConfiguration(categoryRedirects = categoryRedirects, initialSeed = seed)
        val generator = RedirectUrlGenerator(config, categories = categories)

        assertEquals(categoryRedirects, generator.objects.filter { it is CategoryRedirectUrl }.count())
    }

    @Test
    fun testRedirectSequenceStability() {
        val generator = RedirectUrlGenerator(RedirectUrlConfiguration(initialSeed = seed))

        assertEquals(generator.objects.toList(), generator.objects.toList())
    }

    @Test
    fun testRedirectUrlDestinationId() {
        val catalogGenerator = CatalogGenerator(CatalogListConfiguration(initialSeed = seed))
        val products = catalogGenerator.objects.flatMap { it.getAllProducts().asSequence() }
        val categories = catalogGenerator.objects.toList().flatMap { it.categories }
        val categoryAssignments = catalogGenerator.objects.flatMap { it.categoryAssignments }

        val generator = RedirectUrlGenerator(RedirectUrlConfiguration(initialSeed = seed), categories = categories, categoryAssignments = categoryAssignments)

        generator.objects.forEach { redirectUrl ->
            when (redirectUrl) {
                is StaticRedirectUrl -> assertNull(redirectUrl.destinationId)
                is ProductRedirectUrl -> {
                    assertNotNull(redirectUrl.destinationId)

                    // check if destinationId is a product id from the products list
                    assertTrue(products.map { it.id }.contains(redirectUrl.destinationId))
                }
                is CategoryRedirectUrl -> {
                    assertNotNull(redirectUrl.destinationId)

                    // check if destinationId is a category id from the categories list
                    assertTrue(categories.map { it.id }.contains(redirectUrl.destinationId))
                }
            }
        }
    }

    @Test
    fun testRedirectUrlDestinationType() {
        val catalogGenerator = CatalogGenerator(CatalogListConfiguration(initialSeed = seed))
        val categories = catalogGenerator.objects.toList().flatMap { it.categories }
        val categoryAssignments = catalogGenerator.objects.flatMap { it.categoryAssignments }

        val generator = RedirectUrlGenerator(RedirectUrlConfiguration(initialSeed = seed), categories = categories, categoryAssignments = categoryAssignments)

        generator.objects.forEach { redirectUrl ->
            when (redirectUrl) {
                is StaticRedirectUrl -> assertNull(redirectUrl.destinationType)
                is ProductRedirectUrl -> assertEquals("product", redirectUrl.destinationType)
                is CategoryRedirectUrl -> assertEquals("category", redirectUrl.destinationType)
            }
        }
    }

    @Test
    fun testProductRedirectUrlProductCategoryId() {
        val catalogGenerator = CatalogGenerator(CatalogListConfiguration(elementCount = 1, initialSeed = seed))

        val categoryAssignments = catalogGenerator.objects.flatMap { it.categoryAssignments }

        val generator = RedirectUrlGenerator(RedirectUrlConfiguration(initialSeed = seed), categoryAssignments = categoryAssignments)

        // we make this a Set because we might have duplicate source ids and the test works on a set, too
        val sourceIds = generator.objects.map { (it as? ProductRedirectUrl)?.productCategoryId }.filterNotNull().toSet()

        assertEquals(sourceIds.size, sourceIds.intersect(categoryAssignments.map { it.category.id }.toList()).size)
    }

    @Test
    fun testRedirectUrlDestinationIds() {
        val catalogGenerator = CatalogGenerator(CatalogListConfiguration(initialSeed = seed))
        val products = catalogGenerator.objects.flatMap { it.getAllProducts().asSequence() }
        val categories = catalogGenerator.objects.toList().flatMap { it.categories }
        val categoryAssignments = catalogGenerator.objects.flatMap { it.categoryAssignments }

        val config = RedirectUrlConfiguration(initialSeed = seed)
        val generator = RedirectUrlGenerator(config, categories, categoryAssignments)

        generator.objects.forEach { redirect ->
            if (redirect is ProductRedirectUrl) {
                assertTrue(products.map { it.id }.contains(redirect.destinationId))
            } else if (redirect is CategoryRedirectUrl) {
                assertTrue(categories.map { it.id }.contains(redirect.destinationId))
            }
        }
    }
}
