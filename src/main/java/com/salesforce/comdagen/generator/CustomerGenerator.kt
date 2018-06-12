/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.SupportedZone
import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.CustomerConfiguration
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.Customer

data class CustomerGenerator(override val configuration: CustomerConfiguration, private val regions: List<SupportedZone>)
    : Generator<CustomerConfiguration, Customer> {

    override val creatorFunc = { customerId: Int, seed: Long ->
        Customer(customerId, seed, configuration, regions[customerId % regions.size], metadata["Profile"].orEmpty())
    }

    val listId = configuration.id

    override val metadata: Map<String, Set<AttributeDefinition>> = mapOf(
            "Profile" to configuration.attributeDefinitions("Profile")
    )
}
