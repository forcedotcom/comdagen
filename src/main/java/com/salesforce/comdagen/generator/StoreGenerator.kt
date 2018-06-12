/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.StoreConfiguration
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.Store

data class StoreGenerator(override val configuration: StoreConfiguration)
    : Generator<StoreConfiguration, Store> {

    override val creatorFunc = { _: Int, seed: Long -> Store(seed, configuration) }

    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = mapOf("Store" to configuration.attributeDefinitions("Store"))
}
