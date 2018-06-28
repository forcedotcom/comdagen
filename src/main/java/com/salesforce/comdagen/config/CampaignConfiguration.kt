/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.salesforce.comdagen.ExtendableObjectConfig

data class CampaignConfiguration(
    val minPromotions: Int = 1,

    val maxPromotions: Int = 5,

    val minCustomerGroups: Int = 1,

    val maxCustomerGroups: Int = 5,

    val minCoupons: Int = 1,

    val maxCoupons: Int = 5,

    val minSourceCodes: Int = 1,

    val maxSourceCodes: Int = 5,

    override val customAttributes: Map<String, AttributeConfig>? = null,

    override val generatedAttributes: GeneratedAttributeConfig? = null,

    /**
     * This parameter is ignored, campaigns get auto-created based on the number of available promotions and
     * the [minPromotions], [maxPromotions] bracket.
     */
    override val elementCount: Int = 0,
    override val initialSeed: Long
) : ExtendableObjectConfig
