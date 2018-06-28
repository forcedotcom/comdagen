/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.RenderConfig

@JsonRootName("redirectUrls")
@JsonIgnoreProperties(ignoreUnknown = true)
data class RedirectUrlConfiguration(
    @JsonProperty("productRedirects")
    val productRedirects: Int = 10,

    @JsonProperty("categoryRedirects")
    val categoryRedirects: Int = 10,

    override val elementCount: Int = 10,
    override val initialSeed: Long,
    override val outputFilePattern: String = "redirect-urls.xml",
    override val outputDir: String = "",
    override val templateName: String = "redirect-urls.ftlx"
) : RenderConfig