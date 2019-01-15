package com.salesforce.comdagen

import com.salesforce.comdagen.config.SortingRuleConfiguration
import com.salesforce.comdagen.generator.SortingRuleGenerator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortingRuleTest {

    companion object {
        val seed: Long = 1234
    }

    @Test
    fun testSortingRuleElementCount() {
        val elementCount = 5

        val config = SortingRuleConfiguration(elementCount = elementCount, initialSeed = seed)
        val generator = SortingRuleGenerator(config)

        assertEquals(elementCount, generator.objects.count())
        assertTrue(
            generator.assignments.count() >= 1,
            "At least the root category is per default assigned in the template"
        )
    }
}
