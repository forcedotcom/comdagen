package com.salesforce.comdagen.generator

import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.ProductlistConfiguration
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.Catalog
import com.salesforce.comdagen.model.ProductList

data class ProductlistGenerator(
    override val configuration: ProductlistConfiguration,
    private val catalog: Catalog,
    private val customerIds: List<String> = emptyList()
) :
    Generator<ProductlistConfiguration, ProductList> {

    //TODO: Mutate seeds to generate differnt productlists, use customers from generator
    override val creatorFunc =
        { _: Int, seed: Long -> ProductList(seed, sequence { "s" }, "Customerid", configuration) }

    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = mapOf("SourceCodeGroup" to configuration.attributeDefinitions())
}
