/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.RenderConfig

@JsonRootName("sortingrules")
@JsonIgnoreProperties(ignoreUnknown = true)
data class SortingRuleConfiguration(
    override val elementCount: Int = 1,
    override val initialSeed: Long,
    override val outputFilePattern: String = "sort.xml",
    override val outputDir: String = "",
    override val templateName: String = "sortingrules.ftlx"
) : RenderConfig
