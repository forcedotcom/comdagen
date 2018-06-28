/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.ShippingConfiguration
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.ShippingMethod

data class ShippingGenerator(override val configuration: ShippingConfiguration) :
    Generator<ShippingConfiguration, ShippingMethod> {

    override val creatorFunc = { idx: Int, seed: Long -> ShippingMethod(seed, configuration, default = (idx == 1)) }

    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = mapOf(
            "ShippingMethod" to configuration.attributeDefinitions()
        )
}
