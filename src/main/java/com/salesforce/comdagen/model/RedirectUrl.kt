/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import java.util.*

sealed class RedirectUrl {
    /**
     * Controls all random aspects of the redirect.
     */
    abstract val seed: Long

    /**
     * The target of the _business object_ targeted by a redirect.
     * Null if this is a static redirect (which targets another URL).
     */
    abstract val destinationId: String?

    /**
     * Describes the type of the business object.
     * Null if this is a static redirect (has no business object).
     */
    abstract val destinationType: String?

    val sourceUri: String
        get() = RandomData.getRandomUri(seed + "sourceUri".hashCode())

    val statusCode: Int
        get() = 301
}

/**
 * Redirect from [sourceUri] to a product.
 *
 * @property categoryAssignment defines connection between category (source) and product (target)
 */
data class ProductRedirectUrl(override val seed: Long, private val categoryAssignment: CategoryAssignment) :
    RedirectUrl() {
    val productCategoryId: String
        get() = categoryAssignment.category.id

    override val destinationId: String?
        get() = categoryAssignment.product.id

    override val destinationType: String
        get() = "product"
}

/**
 * Category redirect URL
 *
 * @property categories list of categories to choose one from for the redirect
 */
data class CategoryRedirectUrl(override val seed: Long, private val categories: List<Category>) : RedirectUrl() {
    override val destinationId: String?
        get() = categories[Math.abs(Random(seed + "destinationUrl".hashCode()).nextInt() % categories.size)].id

    override val destinationType: String
        get() = "category"
}

/**
 * Static redirect from one random string ([sourceUri]) to another ([destinationUrl]).
 */
data class StaticRedirectUrl(override val seed: Long) : RedirectUrl() {
    override val destinationId: String?
        get() = null

    val destinationUrl: String
        get() = RandomData.getRandomUri(seed + "destinationUrl".hashCode())

    override val destinationType: String?
        get() = null
}
