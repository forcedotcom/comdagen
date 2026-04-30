package com.salesforce.comdagen

import com.salesforce.comdagen.config.*
import com.salesforce.comdagen.generator.CustomerGroupGenerator
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CustomerGroupTest {

    companion object {
        private val seed: Long = 1234
    }

    @Test
    fun testCustomerGroupCount() {
        val elementCount = 30
        val customerConfig = CustomerConfiguration(initialSeed = seed)
        val customerGroupConfig = CustomerGroupConfiguration(elementCount = elementCount, initialSeed = seed)
        val sourceCodeConfig = SourceCodeConfiguration(initialSeed = seed)
        val customerGroupGenerator =
            CustomerGroupGenerator(configuration = customerGroupConfig, customerConfig = customerConfig, sourceCodes = Collections.emptyList<String>())

        assertEquals(elementCount, customerGroupGenerator.objects.count())
    }

    @Test
    fun testCustomerGroupAssignmentCount() {
        val minCustomers = 5
        val maxCustomers = 15
        val elementCount = 10
        val customerConfig = CustomerConfiguration(initialSeed = seed)
        val customerGroupConfig = CustomerGroupConfiguration(
            minCustomers = minCustomers, maxCustomers = maxCustomers,
            elementCount = elementCount, initialSeed = seed
        )
        val sourceCodeConfig = SourceCodeConfiguration(initialSeed = seed)
        val customerGroupGenerator =
                CustomerGroupGenerator(configuration = customerGroupConfig, customerConfig = customerConfig, sourceCodes = Collections.emptyList<String>())

        customerGroupGenerator.objects.forEach { group ->
            val assignmentCount: Int = customerGroupGenerator.assignments.count { it.groupId == group.id }
            assertTrue(assignmentCount >= minCustomers)
            assertTrue(assignmentCount <= maxCustomers)
        }
    }

    @Test
    fun testCustomerGroupCustomAttributes() {
        val name = "foobar"
        val type = AttributeConfig.DataType.STRING
        val generationStrategy = AttributeConfig.GenerationStrategy.STATIC
        val dataStore = "foobar"

        val attributeConfig = AttributeConfig(
            type = type,
            generationStrategy = generationStrategy,
            dataStore = dataStore,
            searchable = false
        )
        val customAttribute: Map<String, AttributeConfig> = mapOf(name to attributeConfig)

        val customerGroupConfig = CustomerGroupConfiguration(customAttributes = customAttribute, initialSeed = seed)
        val sourceCodeConfig = SourceCodeConfiguration(initialSeed = seed)
        val customerGroupGenerator = CustomerGroupGenerator(
            configuration = customerGroupConfig,
            customerConfig = CustomerConfiguration(initialSeed = seed),
            sourceCodes = Collections.emptyList<String>()
        )

        assertEquals(customAttribute.values.size, customerGroupGenerator.metadata.values.sumBy { it.size })

        customerGroupGenerator.objects.forEach { group ->
            assertEquals(customAttribute.values.size, group.customAttributes.size)
            group.customAttributes.forEach { attribute ->
                assertEquals(name, attribute.definition.id)
                assertEquals(type, attribute.definition.type)
                assertEquals(dataStore, attribute.value)
            }
        }
    }

    @Test
    fun testCustomerGroupGeneratedAttributes() {
        val elementCount = 15
        val customerGroupConfig =
            CustomerGroupConfiguration(generatedAttributes = GeneratedAttributeConfig(elementCount), initialSeed = seed)
        val sourceCodeConfiguration = SourceCodeConfiguration(initialSeed = seed)
        val customerGroupGenerator = CustomerGroupGenerator(
            configuration = customerGroupConfig,
            customerConfig = CustomerConfiguration(initialSeed = seed),
            sourceCodes = Collections.emptyList<String>()
        )

        customerGroupGenerator.objects.forEach { group ->
            assertEquals(elementCount, group.customAttributes.size)
        }
    }

    @Test
    fun testCustomerGroupConfigEquals() {
        val configA = CustomerGroupConfiguration(elementCount = 10, initialSeed = seed)
        val configB = CustomerGroupConfiguration(elementCount = 10, initialSeed = seed)
        val configC = CustomerGroupConfiguration(elementCount = 15, initialSeed = seed)

        assertEquals(configA, configB)
        assertNotEquals(configA, configC)
    }
}
