package com.salesforce.comdagen

import org.hamcrest.core.StringContains.containsString
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class XMLOutputProducerTest {
    @JvmField
    @Rule
    var inputFolder = TemporaryFolder()

    @JvmField
    @Rule
    var outputFolder = TemporaryFolder()

    @Test
    @Throws(IOException::class)
    fun testOutput() {
        // prepare input directory
        val templateFileName = "test.ftlx"
        val template = inputFolder.newFile(templateFileName)
        Files.copy(javaClass.getResourceAsStream("/templates/" + templateFileName), template.toPath(),
                StandardCopyOption.REPLACE_EXISTING)

        val outputProducer = XMLOutputProducer(inputFolder.root, outputFolder.root)

        val modelData = mapOf("title" to "myTITLE")
        // prepare output
        outputProducer.produce(templateFileName, "output.xml", modelData)

        val fileContent = File(outputFolder.root, "output.xml").readText()
        // check proper encoding
        assertThat(fileContent, containsString("<title>myTITLE</title>"))
    }

    @Test
    @Throws(IOException::class)
    fun testEncoding() {
        // prepare input directory
        val templateFileName = "test.ftlx"
        val template = inputFolder.newFile(templateFileName)
        Files.copy(javaClass.getResourceAsStream("/templates/" + templateFileName), template.toPath(),
                StandardCopyOption.REPLACE_EXISTING)

        val outputProducer = XMLOutputProducer(inputFolder.root, outputFolder.root)

        val modelData = mapOf("title" to "\\\"0\\\" && value<\\\"10\\\" ?\\\"valid\\\":\\\"error\\\"")

        // prepare output
        outputProducer.produce("test.ftlx", "output.xml", modelData)

        // check proper encoding
        val fileContent = File(outputFolder.root, "output.xml").readText()
        assertThat(fileContent, containsString(
                "\\&quot;0\\&quot; &amp;&amp; value&lt;\\&quot;10\\&quot; ?\\&quot;valid\\&quot;:\\&quot;error\\&quot;"))
    }
}
