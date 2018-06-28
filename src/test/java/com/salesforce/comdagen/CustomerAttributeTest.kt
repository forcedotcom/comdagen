package com.salesforce.comdagen

import com.salesforce.comdagen.config.GeneratedAttributeConfig
import com.salesforce.comdagen.model.RandomAttributeDefinition
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class CustomerAttributeTest {
    @Test
    fun `issue76 large amounts of generated attributes do not clash`() {
        val configSeed = 1234L
        val largeAmount = 200
        val rng = Random(configSeed)
        val attributes = (1..3).map {
            RandomAttributeDefinition.fromConfig(
                "Customer",
                GeneratedAttributeConfig(largeAmount),
                rng.nextLong()
            )
        }

        assertEquals(
            3 * largeAmount, attributes.flatten().size,
            "Total amount of attributes generated does not match expectation"
        )
        assertEquals(
            3 * largeAmount, attributes.flatMap { it.map { it.id } }.toSet().size,
            "Total amount of attribute ids does not match expectation"
        )
    }

    @Test
    fun `issue101 same seed generates same attributes`() {
        val configSeed = 1234L
        val largeAmount = 200

        val attributes = (1..10).map {
            RandomAttributeDefinition.fromConfig(
                "Customer",
                GeneratedAttributeConfig(largeAmount),
                configSeed
            )
        }
        assertEquals(largeAmount, attributes.flatten().toSet().size)
        assertEquals(largeAmount, attributes.flatMap { it.map { it.id } }.toSet().size)
    }
}
