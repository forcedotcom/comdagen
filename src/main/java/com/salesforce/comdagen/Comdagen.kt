/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.salesforce.InvalidSpecificationException
import com.salesforce.comdagen.config.LibraryConfiguration
import com.salesforce.comdagen.config.SitesConfig
import com.salesforce.comdagen.generator.LibraryGenerator
import com.salesforce.comdagen.generator.SiteGenerator
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

class Comdagen {

    @Option(name = "--config", usage = "Use this config file to specify which sites to generate content for")
    private var sitesConfigFile: File? = null

    @Option(name = "--configDir", usage = "Generate xml files for all configs in this dir")
    private var configDir = File("./config")

    @Option(name = "--output")
    private var outputDir = File("./output/generated")

    @Option(name = "--templates")
    private var templateDir = File("./templates")

    @Option(name = "--zip-output", usage = "Specify zip file name")
    private var outputZip = File("./output/generated.zip")

    @Option(name = "--zip", usage = "Zip directory after data generation")
    private var zip: Boolean = false

    @Option(name = "--xlt", usage = "Export a txt file with generated product names")
    private var xltExport: Boolean = false

    @Option(
        name = "--names-output",
        usage = "Specify product names txt file pattern ('\${locale}' get's replaced by locale name)"
    )
    private var outputNames = "./output/productnames_\${site}_\${locale}.txt"

    @Throws(IOException::class)
    private fun doMain(args: Array<String>): Int {
        val parser = CmdLineParser(this)
        try {
            parser.parseArgument(*args)
        } catch (e: CmdLineException) {
            System.err.println(e.message)
            parser.printUsage(System.err)
            return -1
        }

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw IOException("Unable to create required output directory ${outputDir.name}")
        }

        if (!outputDir.isDirectory || !outputDir.canWrite()) {
            throw IOException("Unable to write to output directory ${outputDir.name}")
        }

        if (!templateDir.isDirectory || !templateDir.canRead()) {
            throw IOException("Unable to read from template directory ${templateDir.name}")
        }

        if (!configDir.isDirectory || !configDir.canRead()) {
            throw IOException("Unable to read from config directory ${configDir.name}")
        }

        val outputProducer = XMLOutputProducer(templateDir, outputDir)

        // load default sites config if none is specified as cli parameter
        if (sitesConfigFile == null) {
            sitesConfigFile = File(configDir, "sites.yaml")
        }

        if (!sitesConfigFile!!.isFile || !sitesConfigFile!!.canRead()) {
            throw InvalidSpecificationException(
                "Must provide a readable sites config file (given location: $sitesConfigFile)"
            )
        }


        /*
        * load library configuration
        * This is done before site generation because it is required to know if the comdagen summary content asset
        * will be created in order to determine the content asset to be loaded in the central content asset slot on
        * home page.
        */
        val librariesConfFile = File(configDir, "libraries.yaml")
        val librariesConf: LibraryConfiguration? =
            if (librariesConfFile.isFile && librariesConfFile.canRead()) OBJECT_MAPPER.readValue(
                librariesConfFile,
                LibraryConfiguration::class.java
            ) else null


        // load sites configuration
        val sitesConfig = OBJECT_MAPPER.readValue(sitesConfigFile, SitesConfig::class.java)


        // render generated data as xml files; if the library config couldn't be read, don't set the content slot to
        // the comdagen summary content asset
        outputProducer.render(
            SiteGenerator(
                sitesConfig,
                configDir,
                librariesConf?.renderComdagenSummaryContentAsset ?: false
            )
        )


        // render libraries
        if (librariesConf != null) outputProducer.render(
            LibraryGenerator(
                librariesConf,
                configDir
            )
        ) else LOGGER.warn("Could not read in library configuration \"library.yaml\".")

        // zip generated output directory if cmd option is set
        if (zip) {
            LOGGER.info("Start zipping generated XML files")
            Archiver.zip(outputDir, outputZip)
        }

        // export product names as txt file if xlt option is enabled
        if (xltExport) {
            LOGGER.info("Start exporting product names.")

            val siteGenerator =
                SiteGenerator(sitesConfig, configDir, librariesConf?.renderComdagenSummaryContentAsset ?: false)

            val allRegions = siteGenerator.configuration.sites.flatMap { it.regions.toList() }.toSet()

            // group sites by region
            val regionToSites =
                allRegions.associate { zone -> zone to siteGenerator.objects.filter { it.regions.contains(zone) } }

            // create one output file per site and region
            regionToSites.forEach { zone, sites ->
                sites.forEach { site ->
                    // remove '/' and '\' from file name
                    val siteName = site.name.replace("/", "").replace("\\", "")

                    val outputFile = File(
                        outputNames.replace("\${site}", siteName)
                            .replace("\${locale}", zone.countryCode)
                    )
                    outputFile.printWriter().use { out ->
                        site.navigationCatalog.assignedProducts.forEach { product ->
                            out.println(product.name[zone])
                        }
                    }
                }
            }
        }

        return 0
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(Comdagen::class.java)

        /** Used for parsing the generator config files.  */
        val OBJECT_MAPPER = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
            .addMixIn(Configuration::class.java, SeedInheritanceMixin::class.java)
            .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true)

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            System.exit(Comdagen().doMain(args))
        }
    }
}