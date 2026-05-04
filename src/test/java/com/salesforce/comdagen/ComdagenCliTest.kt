package com.salesforce.comdagen

import org.junit.jupiter.api.Test
import org.kohsuke.args4j.CmdLineParser
import kotlin.test.assertEquals

class ComdagenCliTest {

    @Test
    fun testMaxProductsPerFile_defaultsToZero() {
        val comdagen = Comdagen()
        CmdLineParser(comdagen).parseArgument()
        assertEquals(0, comdagen.maxProductsPerFile)
    }

    @Test
    fun testMaxProductsPerFile_parsesExplicitValue() {
        val comdagen = Comdagen()
        CmdLineParser(comdagen).parseArgument("--max-products-per-file", "500")
        assertEquals(500, comdagen.maxProductsPerFile)
    }
}
