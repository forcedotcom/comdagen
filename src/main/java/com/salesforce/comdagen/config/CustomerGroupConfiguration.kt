/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.Configuration
import com.salesforce.comdagen.ExtendableObjectConfig
import com.salesforce.comdagen.RenderConfig

@JsonRootName("customer-groups")
data class CustomerGroupConfiguration(
    val minCustomers: Int = 5,

    val maxCustomers: Int = 100,


    override val customAttributes: Map<String, AttributeConfig>? = null,

    override val generatedAttributes: GeneratedAttributeConfig? = null,

    override val elementCount: Int = 5,
    override val initialSeed: Long,

    val rules: CustomerGroupConditions? = null,

    override val outputFilePattern: String = "generated.xml",
    override val outputDir: String = "",
    override val templateName: String = "customer-groups.ftlx"

) : RenderConfig, ExtendableObjectConfig {
    init {
        require(maxCustomers >= minCustomers, { "maxCustomers needs to be greater equal minCustomers" })
    }

}

class CustomerGroupConditions(override val initialSeed: Long) : Configuration {
    val includeConditions: List<CustomerGroupCondition> = listOf()
    val excludeConditions: List<CustomerGroupCondition> = listOf()
    override val elementCount: Int
        get() = 2

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as CustomerGroupConditions

        if (includeConditions != other.includeConditions) return false
        if (excludeConditions != other.excludeConditions) return false

        return true
    }


    override fun hashCode(): Int {
        return includeConditions.hashCode() + 31 * excludeConditions.hashCode()
    }

}
class CustomerGroupCondition(
        val attributePath: String,
        val operator: String) {

    val value: String? = null

}
