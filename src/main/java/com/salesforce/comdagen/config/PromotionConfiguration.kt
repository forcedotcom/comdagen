/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.RenderConfig

/**
 * Promotion generator configuration.
 *
 * Notice how this splits into several promotion types, each with a different count.
 * TODO make [elementCount] the total and check against each?
 */
@JsonRootName("promotions")
data class PromotionConfiguration(

    // TODO why do we have three different classes for promotions instead of three instances of the same class?
    val productConfig: ProductPromotionConfiguration? = null,

    val orderConfig: OrderPromotionConfiguration? = null,

    val shippingConfig: ShippingPromotionConfiguration? = null,

    val campaigns: CampaignConfiguration? = null,

    /** Not used currently. Specify in the appropriate sub-config. */
    override val elementCount: Int = 0,
    override val initialSeed: Long,
    override val outputFilePattern: String = "promotions.xml",
    override val outputDir: String = "",
    override val templateName: String = "promotions.ftlx"
) : RenderConfig