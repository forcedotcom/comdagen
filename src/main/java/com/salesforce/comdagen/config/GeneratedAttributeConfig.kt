/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Description of a randomly generated set of attributes.
 *
 * @property elementCount number of custom attributes to generate
 * @property thereofSearchable the first element(s) of [elementCount] will be indexed (and thus searchable)
 */
data class GeneratedAttributeConfig(
        @JsonProperty
        val elementCount: Int = 10,

        @JsonProperty
        val thereofSearchable: Int = 0
) {
    init {
        require(thereofSearchable <= elementCount, { "searchable attributes count must be lower/equal than total number of attributes" })
    }
}
