/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.InvalidComdagenConfigurationValueException
import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.CustomerConfiguration
import com.salesforce.comdagen.config.CustomerGroupConfiguration
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.CustomerGroup
import com.salesforce.comdagen.model.GroupAssignment
import java.util.*

data class CustomerGroupGenerator(
    override val configuration: CustomerGroupConfiguration,
    private val customerConfig: CustomerConfiguration
) : Generator<CustomerGroupConfiguration, CustomerGroup> {

    override val creatorFunc = { _: Int, seed: Long -> CustomerGroup(seed, metadata["CustomerGroup"].orEmpty()) }

    val assignments: Sequence<GroupAssignment>
        get() {
            val groups = objects
            val rng = Random(configuration.initialSeed)
            return groups.flatMap { group ->
                val customerCount = when {
                    configuration.maxCustomers > configuration.minCustomers -> rng.nextInt(
                        configuration.maxCustomers - configuration.minCustomers
                    ) + configuration.minCustomers
                    configuration.maxCustomers == configuration.minCustomers -> configuration.minCustomers
                    else -> throw InvalidComdagenConfigurationValueException(
                        "maxCustomer value ${configuration.maxCustomers} needs to be bigger than minCustomers " +
                                "${configuration.minCustomers} in the customer-groups.yaml configuration file."
                    )
                }
                (1..customerCount).asSequence().map { GroupAssignment(group.id, getRandomCustomer(rng.nextInt())) }
            }
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
