/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData

data class SortingRule(private val seed: Long) {
    val id: String
        get() = "comdagen-${Math.abs(seed + "ruleId".hashCode())}"

    val description: String
        get() = RandomData.getRandomSentence(seed + "ruleDescription".hashCode())
}

data class SortingRuleAssignment(val categoryId: String, val ruleId: String)
