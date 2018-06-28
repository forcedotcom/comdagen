/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.ExtendableObjectConfig
import com.salesforce.comdagen.RenderConfig

@JsonRootName("shipping")
data class ShippingConfiguration(
    val minPrice: Float = 0.01F,

    val maxPrice: Float = 50.0F,

    override val customAttributes: Map<String, AttributeConfig>? = null,

    override val generatedAttributes: GeneratedAttributeConfig? = null,

    override val elementCount: Int = 5,
    override val initialSeed: Long,
    override val outputFilePattern: String = "shipping.xml",
    override val outputDir: String = "",
    override val templateName: String = "shipping.ftlx"
) : RenderConfig, ExtendableObjectConfig