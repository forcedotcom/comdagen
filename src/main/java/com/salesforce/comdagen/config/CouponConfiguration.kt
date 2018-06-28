/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.Configuration
import com.salesforce.comdagen.RenderConfig

@JsonRootName("coupons")
data class CouponConfiguration(
    val singleCodeCoupons: Int = 100,

    val systemCodeCoupons: Int = 100,

    val minCodes: Int = 1,

    val maxCodes: Int = 100,

    val systemCodes: SystemCodeConfig? = null,

    /**
     * The number of code list coupons.
     */
    override val elementCount: Int = 100,
    override val initialSeed: Long,

    override val outputFilePattern: String = "generated.xml",

    override val outputDir: String = "site",

    override val templateName: String = "coupons.ftlx"
) : RenderConfig, Configuration
