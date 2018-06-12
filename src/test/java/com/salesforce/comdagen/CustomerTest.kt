package com.salesforce.comdagen

import com.salesforce.comdagen.config.AttributeConfig
import com.salesforce.comdagen.config.CustomerConfiguration
import com.salesforce.comdagen.config.GeneratedAttributeConfig
import com.salesforce.comdagen.generator.CustomerGenerator
import com.salesforce.comdagen.model.Customer
import org.junit.Test
import kotlin.test.*

class CustomerTest {

    companion object {
        private val seed: Long = 1234
    }

    @Test
    fun testCustomerCount() {
        val elementCount = 300
        val customerConfig = CustomerConfiguration(elementCount = elementCount, initialSeed = seed)
        val regions = listOf(SupportedZone.Generic)
        val customerGenerator = CustomerGenerator(configuration = customerConfig, regions = regions)

        assertEquals(elementCount, customerGenerator.objects.count(),
                "Number of generated customers should equal configured element count")
    }

    @Test
    fun testCustomerAddressCount() {
        val minAddressCount = 3
        val maxAddressCount = 7
        val customerConfig = CustomerConfiguration(minAddressCount = minAddressCount, maxAddressCount = maxAddressCount, initialSeed = seed)
        val regions = listOf(SupportedZone.Generic)
        val customerGenerator = CustomerGenerator(configuration = customerConfig, regions = regions)

        customerGenerator.objects.forEach { customer ->
            assertTrue(customer.addresses.size >= minAddressCount)
            assertTrue(customer.addresses.size <= maxAddressCount)
        }
    }

    @Test
    fun testCustomerIdUniqueness() {
        val elementCount = 10000
        val regions = listOf(SupportedZone.Generic, SupportedZone.Chinese, SupportedZone.German)
        val customerConfig = CustomerConfiguration(elementCount = elementCount, initialSeed = seed)
        val customerGenerator = CustomerGenerator(configuration = customerConfig, regions = regions)

        val customers: List<Customer> = customerGenerator.objects.toList()

        assertEquals(customers.size, customers.distinctBy { it.id }.size, "Customer ids must be unique")
    }

    @Test
    fun testCustomerEmailUniqueness() {
        val elementCount = 10000
        val regions = listOf(SupportedZone.Generic)
        val customerConfig = CustomerConfiguration(elementCount = elementCount, initialSeed = seed)
        val customerGenerator = CustomerGenerator(configuration = customerConfig, regions = regions)

        val customers: List<Customer> = customerGenerator.objects.toList()

        assertEquals(customers.size, customers.distinctBy { it.profile.email }.size, "Customer emails must be unique")
    }

    @Test
    fun testCustomerCustomAttributes() {
        val name = "foobar"
        val type = AttributeConfig.DataType.STRING
        val generationStrategy = AttributeConfig.GenerationStrategy.STATIC
        val dataStore = "foobar"
        val attributeConfig = AttributeConfig(type = type, generationStrategy = generationStrategy, dataStore = dataStore, searchable = false)
        val customAttribute: Map<String, AttributeConfig> = mapOf(name to attributeConfig)
        val customerConfig = CustomerConfiguration(customAttributes = customAttribute, initialSeed = seed)
        val regions = listOf(SupportedZone.Generic)
        val customerGenerator = CustomerGenerator(configuration = customerConfig, regions = regions)

        customerGenerator.objects.forEach { customer ->
            assertEquals(customAttribute.values.size, customer.customAttributes.size)

            customer.customAttributes.forEach { attribute ->
                assertEquals(name, attribute.definition.id)
                assertEquals(type, attribute.definition.type)
                assertEquals(dataStore, attribute.value)
            }
        }
    }

    @Test
    fun testCustomerGeneratedAttributes() {
        val elementCount = 20
        val generatedAttributesConfig = GeneratedAttributeConfig(elementCount = elementCount)
        val customerConfig = CustomerConfiguration(generatedAttributes = generatedAttributesConfig, initialSeed = seed)
        val customerGenerator = CustomerGenerator(configuration = customerConfig, regions = listOf(SupportedZone.Generic))

        customerGenerator.objects.forEach { customer ->
            assertEquals(elementCount, customer.customAttributes.size)
        }
    }

    @Test
    fun testCustomerPasswordEncryption() {
        val passwd = "CloudIs4LetterWord!"

        val customerConfig = CustomerConfiguration(prehashPasswords = true, initialSeed = seed)
        val customerGenerator = CustomerGenerator(configuration = customerConfig, regions = listOf(SupportedZone.Generic))

        customerGenerator.objects.forEach { customer ->
            assertFalse(passwd == customer.password)
            assertNotNull(customer.encryptionScheme)
        }
    }

    @Test
    fun testCustomerPasswordPlain() {
        val passwd = "CloudIs4LetterWord!"

        val customerConfig = CustomerConfiguration(prehashPasswords = false, initialSeed = seed)
        val customerGenerator = CustomerGenerator(configuration = customerConfig, regions = listOf(SupportedZone.Generic))

        customerGenerator.objects.forEach { customer ->
            assertEquals(passwd, customer.password)
            assertNull(customer.encryptionScheme)
        }
    }

    @Test
    fun testCustomerMultipleRegions() {
        val regions = listOf(SupportedZone.Generic, SupportedZone.Chinese, SupportedZone.German)
        val elementCount = 2000

        val customerConfig = CustomerConfiguration(elementCount = elementCount, initialSeed = seed)
        val customerGenerator = CustomerGenerator(configuration = customerConfig, regions = regions)

        assertEquals(elementCount, customerGenerator.objects.count())

        val genericCustomerCount = customerGenerator.objects.count { it.region == SupportedZone.Generic }
        val chineseCustomerCount = customerGenerator.objects.count { it.region == SupportedZone.Chinese }
        val germanCustomerCount = customerGenerator.objects.count { it.region == SupportedZone.Generic }

        // test if all regions have approximately the same number of customers
        assertTrue(genericCustomerCount - chineseCustomerCount <= 1 && chineseCustomerCount - genericCustomerCount <= 1)
        assertTrue(genericCustomerCount - germanCustomerCount <= 1 && germanCustomerCount - genericCustomerCount <= 1)
        assertTrue(chineseCustomerCount - germanCustomerCount <= 1 && germanCustomerCount - chineseCustomerCount <= 1)
    }

    @Test
    fun testCustomerConfigEquals() {
        val configA = CustomerConfiguration(elementCount = 10, initialSeed = seed)
        val configB = CustomerConfiguration(elementCount = 10, initialSeed = seed)
        val configC = CustomerConfiguration(elementCount = 15, initialSeed = seed)

        assertEquals(configA, configB)
        assertNotEquals(configA, configC)
    }
}
