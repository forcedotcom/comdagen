package com.salesforce.comdagen

import com.salesforce.comdagen.config.AttributeConfig
import com.salesforce.comdagen.config.GeneratedAttributeConfig
import com.salesforce.comdagen.config.StoreConfiguration
import com.salesforce.comdagen.generator.StoreGenerator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StoreTest {

    companion object {
        val seed: Long = 1234
    }

    @Test
    fun testStoreElementCount() {
        val elementCount = 30

        val storeConfig = StoreConfiguration(elementCount = elementCount, initialSeed = seed)
        val storeGenerator = StoreGenerator(storeConfig)

        assertEquals(elementCount, storeGenerator.objects.count())
    }

    @Test
    fun testStoreCoordinatesRange() {
        val storeGenerator = StoreGenerator(StoreConfiguration(initialSeed = seed))

        storeGenerator.objects.forEach { store ->
            assertTrue(
                store.coordinates.first >= -90.0 && store.coordinates.first <= 90.0,
                "Latitude is not in range of -90 to 90."
            )
            assertTrue(
                store.coordinates.second >= -180.0 && store.coordinates.second <= 180.0,
                "Longitude is not in range of -180 to 180."
            )
        }
    }

    @Test
    fun testStoreCustomAttributes() {
        val name = "foobar"
        val type = AttributeConfig.DataType.STRING
        val dataStore = "foobar"
        val generationStrategy = AttributeConfig.GenerationStrategy.STATIC

        val attributeConfig = AttributeConfig(
            type = type,
            dataStore = dataStore,
            generationStrategy = generationStrategy,
            searchable = false,
            probability = 1
        )
        val customAttributesConfig: Map<String, AttributeConfig> = mapOf(name to attributeConfig)

        val storeConfig = StoreConfiguration(customAttributes = customAttributesConfig, initialSeed = seed)
        val storeGenerator = StoreGenerator(storeConfig)

        storeGenerator.objects.forEach { store ->
            assertEquals(customAttributesConfig.values.size, store.customAttributes.size)
            store.customAttributes.forEach { attribute ->
                assertEquals(name, attribute.definition.id)
                assertEquals(dataStore, attribute.value)
                assertEquals(type, attribute.definition.type)
            }
        }
    }

    @Test
    fun testStoreGeneratedAttributes() {
        val elementCount = 15

        val storeConfig =
            StoreConfiguration(generatedAttributes = GeneratedAttributeConfig(elementCount), initialSeed = seed)
        val storeGenerator = StoreGenerator(storeConfig)

        storeGenerator.objects.forEach { store ->
            assertEquals(elementCount, store.customAttributes.size)
        }
    }
}
