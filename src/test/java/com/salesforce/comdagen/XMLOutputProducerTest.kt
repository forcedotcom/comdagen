package com.salesforce.comdagen

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

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
}
