/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.ExtendableObjectConfig
import com.salesforce.comdagen.RenderConfig

@JsonRootName("inventories")
@JsonIgnoreProperties(ignoreUnknown = true)
data class InventoryConfiguration(
    val inventoryRecords: InventoryRecordConfiguration,

    val coverage: Float = 1.0F,

    override val customAttributes: Map<String, AttributeConfig>? = null,

    override val generatedAttributes: GeneratedAttributeConfig? = null,

    override val elementCount: Int = 1,
    override val initialSeed: Long,
    override val outputFilePattern: String = "inventories\${i}.xml",
    override val outputDir: String = "inventory-lists",
    override val templateName: String = "inventories.ftlx"
) : RenderConfig, ExtendableObjectConfig