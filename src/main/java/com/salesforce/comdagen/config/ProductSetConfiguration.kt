/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

/**
 * Configuration for product sets
 *
 * @property elementCount how many product sets should get generated per catalog
 *
 * @property minSetProducts minimum number of set products per product set
 *
 * @property maxSetProducts maximum number of set products per product set
 */
data class ProductSetConfiguration(
    val elementCount: Int = 20,

    val minSetProducts: Int = 2,

    val maxSetProducts: Int = 10,

    val enforceMinMaxProducts: Boolean = false
)
