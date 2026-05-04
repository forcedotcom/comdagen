package com.salesforce.comdagen

import com.salesforce.comdagen.model.BundleProduct
import com.salesforce.comdagen.model.CategoryAssignment
import com.salesforce.comdagen.model.MasterCatalog
import com.salesforce.comdagen.model.MasterProduct
import com.salesforce.comdagen.model.ProductSet
import com.salesforce.comdagen.model.StandardProduct

data class CatalogChunk(
    val fileName: String,
    val isFirstFile: Boolean,
    val products: List<StandardProduct>,
    val masterProducts: List<MasterProduct>,
    val bundles: List<BundleProduct>,
    val productSets: List<ProductSet>,
    val categoryAssignments: List<CategoryAssignment>
)

class CatalogChunker(private val maxProductsPerFile: Int) {
    fun chunk(catalog: MasterCatalog, baseFileName: String): List<CatalogChunk> {
        val allProducts = catalog.products.toList()
        val allMasterProducts = catalog.masterProducts.toList()
        val total = allProducts.size + allMasterProducts.size +
                allMasterProducts.sumOf { it.variants.size } +
                catalog.bundles.size + catalog.productSets.size

        if (maxProductsPerFile <= 0 || total <= maxProductsPerFile) {
            return listOf(
                CatalogChunk(
                    fileName = baseFileName,
                    isFirstFile = true,
                    products = allProducts,
                    masterProducts = allMasterProducts,
                    bundles = catalog.bundles,
                    productSets = catalog.productSets,
                    categoryAssignments = catalog.categoryAssignments.toList()
                )
            )
        }

        val productChunks = allProducts.chunked(maxProductsPerFile)
        val masterProductChunks = allMasterProducts.chunked(maxProductsPerFile)
        val bundleChunks = catalog.bundles.chunked(maxProductsPerFile)
        val productSetChunks = catalog.productSets.chunked(maxProductsPerFile)
        val numChunks = maxOf(productChunks.size, masterProductChunks.size, bundleChunks.size, productSetChunks.size)
        val base = baseFileName.removeSuffix(".xml")
        val allAssignments = catalog.categoryAssignments.toList()

        return (0 until numChunks).map { idx ->
            val productSlice = productChunks.getOrElse(idx) { emptyList() }
            val masterSlice = masterProductChunks.getOrElse(idx) { emptyList() }
            val bundleSlice = bundleChunks.getOrElse(idx) { emptyList() }
            val productSetSlice = productSetChunks.getOrElse(idx) { emptyList() }
            val chunkProductIds = (productSlice.map { it.id } +
                    masterSlice.map { it.id } +
                    masterSlice.flatMap { it.variants.map { v -> v.id } } +
                    bundleSlice.map { it.id } +
                    productSetSlice.map { it.id }).toSet()
            val chunkAssignments = allAssignments.filter { it.product.id in chunkProductIds }
            CatalogChunk(
                fileName = if (idx == 0) baseFileName else "$base-${idx + 1}.xml",
                isFirstFile = (idx == 0),
                products = productSlice,
                masterProducts = masterSlice,
                bundles = bundleSlice,
                productSets = productSetSlice,
                categoryAssignments = chunkAssignments
            )
        }
    }
}
