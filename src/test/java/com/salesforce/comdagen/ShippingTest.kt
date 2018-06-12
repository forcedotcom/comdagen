package com.salesforce.comdagen

import com.salesforce.comdagen.config.AttributeConfig
import com.salesforce.comdagen.config.GeneratedAttributeConfig
import com.salesforce.comdagen.config.ShippingConfiguration
import com.salesforce.comdagen.generator.ShippingGenerator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShippingTest {
    private val seed: Long = 1234

    @Test
    fun testShippingMethodElementCount() {
        val elementCount = 7

        val shippingConfig = ShippingConfiguration(elementCount = elementCount, initialSeed = seed)
        val shippingGenerator = ShippingGenerator(shippingConfig)

        assertEquals(elementCount, shippingGenerator.objects.count())
    }

    @Test
    fun testShippingMethodPriceRange() {
        val minPrice = 2.0F
        val maxPrice = 15.0F

        val shippingConfig = ShippingConfiguration(minPrice = minPrice, maxPrice = maxPrice, initialSeed = seed)
        val shippingGenerator = ShippingGenerator(shippingConfig)

        shippingGenerator.objects.forEach { shippingMethod ->
            shippingMethod.priceTable.forEachIndexed { index, amountEntry ->
                assertTrue { amountEntry.amount >= minPrice * index }
                assertTrue { amountEntry.amount <= maxPrice * index }
            }
        }
    }

    @Test
    fun testShippingMethodCustomAttributes() {
        val name = "foobar"
        val type = AttributeConfig.DataType.STRING
        val dataStore = "foobar"
        val generationStrategy = AttributeConfig.GenerationStrategy.STATIC

        val attributeConfig = AttributeConfig(type = type, dataStore = dataStore, generationStrategy = generationStrategy, searchable = false)
        val customAttributesConfig: Map<String, AttributeConfig> = mapOf(name to attributeConfig)

        val shippingConfig = ShippingConfiguration(customAttributes = customAttributesConfig, initialSeed = seed)
        val shippingGenerator = ShippingGenerator(shippingConfig)

        shippingGenerator.objects.forEach { shippingMethod ->
            assertEquals(customAttributesConfig.values.size, shippingMethod.customAttributes.size)
            shippingMethod.customAttributes.forEach { attribute ->
                assertEquals(name, attribute.definition.id)
                assertEquals(dataStore, attribute.value)
                assertEquals(type, attribute.definition.type)
            }
        }
    }

    @Test
    fun testShippingMethodGeneratedAttributes() {
        val elementCount = 15

        val shippingConfig = ShippingConfiguration(generatedAttributes = GeneratedAttributeConfig(elementCount), initialSeed = seed)
        val shippingGenerator = ShippingGenerator(shippingConfig)

        shippingGenerator.objects.forEach { shippingMethod ->
            assertEquals(elementCount, shippingMethod.customAttributes.count())
        }
    }
}
