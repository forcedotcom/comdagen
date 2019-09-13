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

    /**
     * A number of default sorting rules, that are hardcoded in the freemarker template.
     */
    enum class DefaultRule(val id: String, val description: String) {
        BEST_MATCH(
            "best-matches", "Applies static" +
                    " sortings (category position, search placement/rank), then text relevance, then" +
                    "            explicit sortings"
        ),
        BRAND("brand", "Sorts by product brand A-Z"),
        CUSTOMER_FAVORITE("customer-favorites", "Sorts by customer ratings"),
        MOST_POPULAR(
            "most-popular",
            "Sorts by combination of product views, sales velocity, look to book, and availability"
        ),
        PRICE_HIGHLOW("price-high-to-low", "Sorts by price descending"),
        PRICE_LOWHIGH("price-low-to-high", "Sorts by price ascending"),
        PRODUCTNAME_ASC("product-name-ascending", "Sorts by product name A-Z"),
        PRODUCTNAME_DESC("product-name-descending", "Sorts by product name Z-A"),
        TOPSELLER(
            "top-sellers", "Sorts by combination of revenue, units," +
                    " look to book, and availability"
        );

        override fun toString(): String {
            return id
        }
    }

}

data class SortingRuleAssignment(val categoryId: String, val ruleId: String)
