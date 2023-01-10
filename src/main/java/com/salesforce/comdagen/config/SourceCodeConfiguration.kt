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

@JsonRootName("sourcecodes")
data class SourceCodeConfiguration(
    val minCodes: Int = 1,

    val maxCodes: Int = 10,

    override val customAttributes: Map<String, AttributeConfig> = emptyMap(),

    override val localizableCustomAttributes: Map<String, AttributeConfig>? = null,

    override val generatedAttributes: GeneratedAttributeConfig? = null,

    override val elementCount: Int = 10,
    override val initialSeed: Long,
    override val outputFilePattern: String = "sourcecodes.xml",
    override val outputDir: String = "",
    override val templateName: String = "sourcecodes.ftlx"
) : RenderConfig, ExtendableObjectConfig {
    init {
        require(maxCodes >= minCodes, { "maxCodes needs to be greater equal minCodes" })
    }
}