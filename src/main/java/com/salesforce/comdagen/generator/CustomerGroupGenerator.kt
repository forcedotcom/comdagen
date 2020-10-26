/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.CustomerConfiguration
import com.salesforce.comdagen.config.CustomerGroupConfiguration
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.CustomerGroup
import com.salesforce.comdagen.model.GroupAssignment
import com.salesforce.comdagen.model.GroupCondition
import java.util.*

data class CustomerGroupGenerator(
    override val configuration: CustomerGroupConfiguration,
    private val customerConfig: CustomerConfiguration,
    private val sourceCodes: List<String>
) : Generator<CustomerGroupConfiguration, CustomerGroup> {

    override val creatorFunc = { idx: Int, seed: Long -> CustomerGroup(idx, seed, metadata["CustomerGroup"].orEmpty(), includeConditions) }

    val assignments: Sequence<GroupAssignment>
        get() {
            if(configuration.rules != null)
                return emptySequence()
            val groups = objects
            val rng = Random(configuration.initialSeed)
            return groups.flatMap { group ->
                val customerCount = if (configuration.maxCustomers > configuration.minCustomers)
                    rng.nextInt(configuration.maxCustomers - configuration.minCustomers) +
                            configuration.minCustomers
                else
                    configuration.minCustomers
                (1..customerCount).asSequence().map { GroupAssignment(group.id, getRandomCustomer(rng.nextInt())) }
            }
        }
    private val includeConditions: (Int) -> List<GroupCondition>? = { idx ->
          configuration.rules?.includeConditions?.map { condition -> GroupCondition(condition.attributePath ,condition.operator, sourceCodes[idx]) }
        }
    val excludeConditions: List<GroupCondition>?
        get() {
            TODO()
        }

    override val metadata: Map<String, Set<AttributeDefinition>> = mapOf(
        "CustomerGroup" to configuration.attributeDefinitions("CustomerGroup")
    )

    private fun getRandomCustomer(seed: Int): Int {
        val customerIds = getCustomerIds()
        return customerIds[Math.abs(seed) % customerIds.size]
    }

    private fun getCustomerIds(): List<Int> {
        return customerIds.getOrPut(customerConfig) { (1..customerConfig.elementCount).toList() }
    }

    companion object {
        private val customerIds: MutableMap<CustomerConfiguration, List<Int>> = mutableMapOf()
    }
}
