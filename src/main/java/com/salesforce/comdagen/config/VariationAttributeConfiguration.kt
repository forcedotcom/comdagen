/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents one variation attribute configuration.
 *
 * @property name key of the variation attribute
 * @property values list of available values
 * @property probability a float between `[0,1]` that describes the probability of each value being used for any given
 *                       master product (lower values mean less chance of master products carrying this attribute)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class VariationAttributeConfiguration(
        @JsonProperty("name")
        val name: String,

        @JsonProperty("values")
        val values: List<String>,

        @JsonProperty
        val probability: Float = 1.0f
) {
    init {
        require(probability in 0.0..1.0, { "Probability for $name must be in [0,1]" })
    }
}
