/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonProperty

data class CategoryConfiguration(
    /**
     * number of categories per catalog
     */
    @JsonProperty
    val elementCount: Int = 50,

    /**
     * depth of the category tree
     */
    @JsonProperty
    val categoryTreeDepth: Int = 5,

    /**
     * breadth of the category tree
     */
    @JsonProperty
    val categoryTreeBreadth: Int = 10,

    /**
     * category landing page template
     */
    @JsonProperty
    val categoryTemplate: String? = null
)
