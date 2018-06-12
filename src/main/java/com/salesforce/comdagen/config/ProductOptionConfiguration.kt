/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Configure generation of product options
 *
 * @author ojauch
 */
data class ProductOptionConfiguration(
        /**
         * Number of options to generate
         */
        @JsonProperty("elementCount")
        val elementCount: Int = 10,

        /**
         * minimum number of values per option
         */
        @JsonProperty("minValues")
        val minValues: Int = 1,

        /**
         * maximum number of values per option
         */
        @JsonProperty("maxValues")
        val maxValues: Int = 5,

        /**
         * minimum price of one value in USD
         */
        @JsonProperty("minPrice")
        val minPrice: Double = 0.01,

        /**
         * maximum price of one value in USD
         */
        @JsonProperty("maxPrice")
        val maxPrice: Double = 500.0,

        /**
         * percentage of products with options
         */
        @JsonProperty("probability")
        val probability: Float = 0.1F
)
