package com.salesforce.comdagen

object SplitPlanner {
    fun computeItemsPerFile(totalItems: Int, spec: SplitSpec, bytesPerItem: Long = 0): Int {
        return when (spec) {
            is SplitSpec.Count -> ((totalItems + spec.n - 1) / spec.n).coerceAtLeast(1)
            is SplitSpec.Size -> {
                require(bytesPerItem > 0) { "bytesPerItem must be > 0 for SplitSpec.Size" }
                (spec.bytes / bytesPerItem).toInt()
            }
        }
    }
}
