/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Configure generation of variation products
 *
 * @property elementCount how many variation master products for the configured attributes
 *
 * @property localVariationAttributes define local variation attributes and its values
 *
 * @property sharedVariationAttributes which shared variation attributes should get used?
 * @property attributes all attributes, both local and shared (only the ones applicable to this config), , initialized from [CatalogListConfiguration]
 */
data class VariationProductConfiguration(
    @JsonProperty("elementCount")
    val elementCount: Int = 100,

    @JsonProperty("localVariationAttributes")
    val localVariationAttributes: List<VariationAttributeConfiguration> = emptyList(),

    @JsonProperty("sharedVariationAttributes")
    val sharedVariationAttributes: List<String> = emptyList()

//    @JsonProperty("customAttributes")
//    val customAttributes: Map<String, AttributeConfig>? = null,
//
//    val generatedAttributes: GeneratedAttributeConfig? = null
) {
    lateinit var attributes: List<VariationAttributeConfiguration>
}
