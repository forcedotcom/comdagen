/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.config.SortingRuleConfiguration
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.SortingRule
import com.salesforce.comdagen.model.SortingRuleAssignment

data class SortingRuleGenerator(override val configuration: SortingRuleConfiguration) :
    Generator<SortingRuleConfiguration, SortingRule> {

    override val creatorFunc = { _: Int, seed: Long -> SortingRule(seed) }

    val assignments: Sequence<SortingRuleAssignment>
        get() = objects.map { SortingRuleAssignment("root", SortingRule.DefaultRule.BEST_MATCH.id) }

    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = emptyMap()
}
