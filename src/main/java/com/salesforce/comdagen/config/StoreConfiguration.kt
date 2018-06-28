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

@JsonRootName("stores")
data class StoreConfiguration(
    val centerLongitude: Double? = null,

    val centerLatitude: Double? = null,

    /**
     * maximum distance of the stores to the center point in kilometers
     */
    val distance: Double? = null,

    override val customAttributes: Map<String, AttributeConfig>? = null,

    override val generatedAttributes: GeneratedAttributeConfig? = null,

    override val elementCount: Int = 10,
    override val initialSeed: Long,
    override val outputFilePattern: String = "stores.xml",
    override val outputDir: String = "",
    override val templateName: String = "stores.ftlx"
) : RenderConfig, ExtendableObjectConfig