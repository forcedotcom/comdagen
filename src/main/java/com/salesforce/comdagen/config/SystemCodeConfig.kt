/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonProperty

data class SystemCodeConfig(
    @JsonProperty("minCodes")
    val minCodes: Int = 1,

    @JsonProperty("maxCodes")
    val maxCodes: Int = 1000
)
