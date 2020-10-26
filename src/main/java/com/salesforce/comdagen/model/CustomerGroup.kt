/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import java.util.*

class CustomerGroup(private val idx: Int, private val seed: Long, private val attributeDefinitions: Set<AttributeDefinition>, includeConditions: (Int) -> List<GroupCondition>?) {
    val id: String
        get() = "comdagen-${Math.abs(seed)}"

    val description: String
        get() = RandomData.getRandomSentence(seed + "customerGroupDescription".hashCode())

    val conditions: List<GroupCondition>? = includeConditions(idx.toInt())

    val customAttributes: List<CustomAttribute>
        get() {
            val rng = Random(seed + "customAttributes".hashCode())
            return attributeDefinitions.map { CustomAttribute(it, rng.nextLong()) }
        }
}

data class GroupCondition(val attributePath: String, val operator: String, val value: String)
data class GroupAssignment(val groupId: String, val customerId: Int)
