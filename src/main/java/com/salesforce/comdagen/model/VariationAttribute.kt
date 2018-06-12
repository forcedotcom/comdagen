/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.config.AttributeConfig

data class VariationAttribute(override val id: String, override val dataStore: List<String>) : AttributeDefinition {
    override val path: String
        get() = "product.$id"

    override val displayName: String
        get() = id.capitalize()

    override val type: AttributeConfig.DataType
        get() = AttributeConfig.DataType.STRING

    override val generationStrategy: AttributeConfig.GenerationStrategy
        get() = AttributeConfig.GenerationStrategy.LIST

    override val searchable: Boolean
        get() = false
}