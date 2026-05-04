package com.salesforce.comdagen

import com.salesforce.comdagen.config.CatalogListConfiguration
import com.salesforce.comdagen.config.ProductConfiguration
import com.salesforce.comdagen.generator.CatalogGenerator
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class XMLOutputProducerTest {
    @TempDir
    lateinit var inputFolder: File

    @TempDir
    lateinit var outputFolder: File

    @Test
    @Throws(IOException::class)
    fun testOutput() {
        // prepare input directory
        val templateFileName = "test.ftlx"
        val template = File(inputFolder, templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/" + templateFileName), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val outputProducer = XMLOutputProducer(inputFolder, outputFolder)

        val modelData = mapOf("title" to "myTITLE")
        // prepare output
        outputProducer.produce(templateFileName, "output.xml", modelData)

        val fileContent = File(outputFolder, "output.xml").readText()
        // check proper encoding
        assertThat(fileContent, containsString("<title>myTITLE</title>"))
    }

    @Test
    @Throws(IOException::class)
    fun testEncoding() {
        // prepare input directory
        val templateFileName = "test.ftlx"
        val template = File(inputFolder, templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/" + templateFileName), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val outputProducer = XMLOutputProducer(inputFolder, outputFolder)

        val modelData = mapOf("title" to "\\\"0\\\" && value<\\\"10\\\" ?\\\"valid\\\":\\\"error\\\"")

        // prepare output
        outputProducer.produce("test.ftlx", "output.xml", modelData)

        // check proper encoding
        val fileContent = File(outputFolder, "output.xml").readText()
        assertThat(
            fileContent, containsString(
                "\\&quot;0\\&quot; &amp;&amp; value&lt;\\&quot;10\\&quot; ?\\&quot;valid\\&quot;:\\&quot;error\\&quot;"
            )
        )
    }

    @Test
    @Throws(IOException::class)
    fun testRenderCatalogGenerator_thresholdExceeded_writesMultipleFiles() {
        // Copy catalogs.ftlx template into the input folder so freemarker can find it
        val templateFileName = "catalogs.ftlx"
        val template = File(inputFolder, templateFileName)
        Files.copy(
            File("templates", templateFileName).toPath(),
            template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val seed: Long = 1234
        val catalogConfig = CatalogListConfiguration(
            elementCount = 1,  // one catalog
            products = ProductConfiguration(elementCount = 10, initialSeed = seed),
            initialSeed = seed
        )
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        val outputProducer = XMLOutputProducer(inputFolder, outputFolder, maxProductsPerFile = 4)
        outputProducer.render(catalogGenerator)

        val catalog = catalogGenerator.objects.first()
        val catalogDir = File(outputFolder, "catalogs/${catalog.id}")
        assertTrue(File(catalogDir, "catalog.xml").exists(), "catalog.xml should exist")
        assertTrue(File(catalogDir, "catalog-2.xml").exists(), "catalog-2.xml should exist")
        assertTrue(File(catalogDir, "catalog-3.xml").exists(), "catalog-3.xml should exist")
        assertFalse(File(catalogDir, "catalog-4.xml").exists(), "catalog-4.xml should NOT exist")
    }

    @Test
    @Throws(IOException::class)
    fun testRenderCatalogGenerator_thresholdNotReached_producesByteIdenticalOutput() {
        val templateFileName = "catalogs.ftlx"
        Files.copy(
            File("templates", templateFileName).toPath(),
            File(inputFolder, templateFileName).toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val seed: Long = 1234
        val catalogConfig = CatalogListConfiguration(
            elementCount = 1,
            products = ProductConfiguration(elementCount = 5, initialSeed = seed),
            initialSeed = seed
        )
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)
        val catalog = catalogGenerator.objects.first()
        val catalogXml = File(outputFolder, "catalogs/${catalog.id}/catalog.xml")

        XMLOutputProducer(inputFolder, outputFolder, maxProductsPerFile = 0).render(catalogGenerator)
        val bytesFromZero = catalogXml.readBytes()

        catalogXml.delete()

        XMLOutputProducer(inputFolder, outputFolder, maxProductsPerFile = 10_000).render(catalogGenerator)
        val bytesFromHigh = catalogXml.readBytes()

        assertEquals(
            bytesFromZero.toList(), bytesFromHigh.toList(),
            "Unchunked output must be byte-identical regardless of threshold value"
        )
        assertFalse(File(outputFolder, "catalogs/${catalog.id}/catalog-2.xml").exists())
    }

    @Test
    @Throws(IOException::class)
    fun testRenderCatalogGenerator_firstChunkHasRootCategory_subsequentChunksDoNot() {
        val templateFileName = "catalogs.ftlx"
        Files.copy(
            File("templates", templateFileName).toPath(),
            File(inputFolder, templateFileName).toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val seed: Long = 1234
        val catalogConfig = CatalogListConfiguration(
            elementCount = 1,
            products = ProductConfiguration(elementCount = 10, initialSeed = seed),
            initialSeed = seed
        )
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        XMLOutputProducer(inputFolder, outputFolder, maxProductsPerFile = 4).render(catalogGenerator)

        val catalog = catalogGenerator.objects.first()
        val catalogDir = File(outputFolder, "catalogs/${catalog.id}")
        val first = File(catalogDir, "catalog.xml").readText()
        val second = File(catalogDir, "catalog-2.xml").readText()
        val third = File(catalogDir, "catalog-3.xml").readText()

        assertTrue(
            first.contains("<category category-id=\"root\""),
            "first chunk must contain root category"
        )
        assertFalse(
            second.contains("<category category-id=\"root\""),
            "second chunk must NOT contain root category"
        )
        assertFalse(
            third.contains("<category category-id=\"root\""),
            "third chunk must NOT contain root category"
        )
    }
}
