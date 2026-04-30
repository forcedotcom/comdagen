package com.salesforce.comdagen

data class CatalogSlice<P, M, B, S>(
    val products: List<P>,
    val masters: List<M>,
    val bundles: List<B>,
    val sets: List<S>,
)

object CatalogSlicer {
    fun <P, M, B, S> slice(
        products: List<P>,
        masters: List<M>,
        bundles: List<B>,
        sets: List<S>,
        itemsPerFile: Int,
    ): List<CatalogSlice<P, M, B, S>> {
        val productsEnd = products.size
        val mastersEnd = productsEnd + masters.size
        val bundlesEnd = mastersEnd + bundles.size
        val total = bundlesEnd + sets.size

        val slices = mutableListOf<CatalogSlice<P, M, B, S>>()
        var start = 0
        while (start < total) {
            val end = minOf(start + itemsPerFile, total)

            val productsFrom = minOf(start, productsEnd)
            val productsTo = minOf(end, productsEnd)
            val mastersFrom = minOf(maxOf(start, productsEnd), mastersEnd) - productsEnd
            val mastersTo = minOf(maxOf(end, productsEnd), mastersEnd) - productsEnd
            val bundlesFrom = minOf(maxOf(start, mastersEnd), bundlesEnd) - mastersEnd
            val bundlesTo = minOf(maxOf(end, mastersEnd), bundlesEnd) - mastersEnd
            val setsFrom = minOf(maxOf(start, bundlesEnd), total) - bundlesEnd
            val setsTo = minOf(maxOf(end, bundlesEnd), total) - bundlesEnd

            slices.add(
                CatalogSlice(
                    products = products.subList(productsFrom, productsTo),
                    masters = masters.subList(mastersFrom, mastersTo),
                    bundles = bundles.subList(bundlesFrom, bundlesTo),
                    sets = sets.subList(setsFrom, setsTo),
                )
            )
            start = end
        }
        return slices
    }
}
