package com.salesforce.comdagen

import com.salesforce.comdagen.config.AttributeConfig
import com.salesforce.comdagen.config.GeneratedAttributeConfig
import com.salesforce.comdagen.config.SourceCodeConfiguration
import com.salesforce.comdagen.generator.SourceCodeGenerator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SourceCodeTest {
    companion object {
        val seed: Long = 1234
    }

    @Test
    fun testSourceCodeElementCount() {
        val elementCount = 50

        val sourceCodeConfig = SourceCodeConfiguration(elementCount = elementCount, initialSeed = seed)
        val sourceCodeGenerator = SourceCodeGenerator(sourceCodeConfig)

        assertEquals(elementCount, sourceCodeGenerator.objects.count())
    }

    @Test
    fun testSourceCodeCount() {
        val minCodes = 5
        val maxCodes = 15

        val sourceCodeConfig = SourceCodeConfiguration(minCodes = minCodes, maxCodes = maxCodes, initialSeed = seed)
        val sourceCodeGenerator = SourceCodeGenerator(sourceCodeConfig)

        sourceCodeGenerator.objects.forEach { group ->
            assertTrue(group.sourceCodes.count() >= minCodes)
            assertTrue(group.sourceCodes.count() <= maxCodes)
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

        val sourceCodeConfig = SourceCodeConfiguration(customAttributes = customAttributesConfig, initialSeed = seed)
        val sourceCodeGenerator = SourceCodeGenerator(sourceCodeConfig)

        sourceCodeGenerator.objects.forEach { group ->
            assertEquals(customAttributesConfig.values.size, group.customAttributes.size)
            group.customAttributes.forEach { attribute ->
                assertEquals(name, attribute.definition.id)
                assertEquals(dataStore, attribute.value)
                assertEquals(type, attribute.definition.type)
            }
        }
    }

    @Test
    fun testShippingMethodGeneratedAttributes() {
        val elementCount = 15

        val sourceCodeConfig = SourceCodeConfiguration(generatedAttributes = GeneratedAttributeConfig(elementCount), initialSeed = seed)
        val sourceCodeGenerator = SourceCodeGenerator(sourceCodeConfig)

        sourceCodeGenerator.objects.forEach { group ->
            assertEquals(elementCount, group.customAttributes.count())
        }
    }
}
