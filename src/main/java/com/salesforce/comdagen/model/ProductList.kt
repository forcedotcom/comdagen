package com.salesforce.comdagen.model

import com.google.common.collect.ImmutableMap
import com.salesforce.comdagen.config.ProductlistConfiguration
import kotlin.random.Random

class ProductList(
    private val seed: Long,
    private val productIds: Sequence<String>,
    private val customerId: String,
    private val config: ProductlistConfiguration
) {
    //TODO: Chose random type
    val type: String
    get() {
        val rng= Random(seed + "productlists".hashCode())
        return "wish_list" //config.types[rng.nextInt(config.types.size-1)]
    }

    //TODO: Draw random products and amounts from productIds
    val items: Map<String, Int>
    get() = emptyMap()//mapOf("asd" to 2)

    val id: String
        get() = "comdagen-${Math.abs(seed + "productlist".hashCode())}"

    val customer: String
        get() = customerId
}
