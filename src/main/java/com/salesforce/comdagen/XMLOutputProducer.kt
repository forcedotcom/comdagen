/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen

import com.salesforce.comdagen.generator.*
import com.salesforce.comdagen.model.AttributeDefinition

import com.salesforce.comdagen.model.NavigationCatalog
import freemarker.template.*
import freemarker.template.Configuration
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*

/**
 * A thin wrapper around the freemarker engine, centralizing it's configuration.
 *
 *
 * Since we pass all required information at render time, we can re-use a single producer for all templates.
 */
class XMLOutputProducer
/**
 * Initialize the freemarker wrapper.

 * @param templateDir this is where freemarker will load all templates from
 * *
 * @param outputDir this is where the generated xml files will end up
 * *
 * @throws IOException if the template dir can't be resolved an IOException is thrown
 */
@Throws(IOException::class)
constructor(
    private val templateDir: File = File("./templates"),
    private val outputDir: File = File("./output/generated")
) {

    private val freemarkerConfig: freemarker.template.Configuration =
        Configuration(freemarker.template.Configuration.VERSION_2_3_25)
    private val staticImagesNameWithPath: List<String> = listOf(
        "/images/baseImage.jpg",
        "/images/baseLarge.jpg",
        "/images/baseMedium.jpg",
        "/images/baseSmall.jpg",
        "/images/baseSwatch.jpg",
        "/images/baseThumb.jpg"
    )

    private val comdagenStatistics = ComdagenStatistics()

    init {
        /* configure template engine */
        // encoding for templates
        freemarkerConfig.defaultEncoding = "UTF-8"
        // use *.ftlx file extension for automatic xml encoding
        freemarkerConfig.recognizeStandardFileExtensions = true
        // set locale
        freemarkerConfig.locale = Locale.US
        // no formatting for numbers
        freemarkerConfig.numberFormat = "computer"
        // configure error handling
        freemarkerConfig.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        // where to find the templates
        freemarkerConfig.setDirectoryForTemplateLoading(templateDir)
        // this allows us to use [Sequence] like any list or iterator
        freemarkerConfig.objectWrapper = SequenceWrapper(freemarkerConfig.incompatibleImprovements)

        if (!outputDir.isDirectory || !outputDir.canWrite()) {
            throw IllegalStateException("Need to be able to write to $outputDir")
        }
    }

    class SequenceWrapper(incompatibleImprovements: Version) : DefaultObjectWrapper(incompatibleImprovements) {
        override fun handleUnknownType(obj: Any): TemplateModel =
            if (obj is Sequence<*>) {
                DefaultIteratorAdapter.adapt(obj.iterator(), this)
            } else {
                super.handleUnknownType(obj)
            }
    }

    /**
     * Render the provided template using information from the passed configuration.
     *
     * @param generator used to provide context for the generated objects, including the output file name
     * *
     * @param currentGeneratorIndex index of generator if multiple instances of the same generator exist
     * *
     * @throws IOException in case writing the output file hits an error
     */
    fun render(generator: Generator<*, *>, currentGeneratorIndex: Int = 0) {
        // each catalog has to be stored in a separate file
        when (generator) {
            is CatalogGenerator -> render(generator.generatorTemplate, generator)
            is SiteGenerator -> render(generator.generatorTemplate, generator.preferencesTemplate, generator)
            is LibraryGenerator -> render(generator.generatorTemplate, generator)
            else -> render(generator.generatorTemplate, generator, currentGeneratorIndex)
        }
    }

    @Throws(IOException::class)
    private fun render(templateName: String, generator: Generator<*, *>, currentGeneratorIndex: Int = 0) {
        val modelData = mapOf("gen" to generator)
        File("$outputDir/${generator.configuration.outputDir}").apply { mkdirs() }
        produce(
            templateName,
            "${generator.configuration.outputDir}/${generator.configuration.getFileName(currentGeneratorIndex)}",
            modelData
        )
    }

    @Throws(IOException::class)
    private fun render(templateName: String, generator: CatalogGenerator) {
        val catalogs = generator.objects
        catalogs.forEachIndexed { index, catalog ->
            val modelData = mapOf(
                "catalog" to catalog, "index" to index,
                "configuration" to generator.configuration
            )
            File("$outputDir/${generator.configuration.outputDir}/${catalog.id}").apply { mkdirs() }
            copyResources(staticImagesNameWithPath, File(outputDir, "/catalogs/${catalog.id}/static/default/"))
            produce(
                templateName,
                "${generator.configuration.outputDir}/${catalog.id}/${generator.configuration.getFileName()}", modelData
            )
        }
    }

    @Throws(IOException::class)
    private fun renderNavigationCatalog(templateName: String, navigationCatalog: NavigationCatalog) {
        val modelData = mapOf("catalog" to navigationCatalog, "index" to 0)
        File("$outputDir/catalogs/${navigationCatalog.id}").apply { mkdirs() }
        produce(
            templateName,
            "catalogs/${navigationCatalog.id}/catalog.xml", modelData
        )
    }

    @Throws(IOException::class)
    fun renderMeta(
        templateName: String = "system-objecttype-extensions.ftlx",
        customAttributes: Map<String, Set<AttributeDefinition>>,
        regions: Set<SupportedZone>
    ) {
        File("$outputDir/meta").apply { mkdirs() }
        produce(templateName, "meta/system-objecttype-extensions.xml", mapOf("customAttributes" to customAttributes))
        produce(
            "search2.ftlx", "search2.xml",
            mapOf("searchableAttributes" to customAttributes.flatMap { it.value.filter { it.searchable } },
                "locales" to regions.map { it.locale })
        )
    }

    @Throws(IOException::class)
    fun render(templateName: String, generator: LibraryGenerator) {
        val libraries = generator.objects

        // Gather statistics for the ComdagenSummary
        libraries.forEach {
            val gatheredLibraryStatistics: Map<String, String> = mapOf(
                "Library id" to it.libraryId,
                "Library seed" to it.seed.toString(),
                "Content asset count" to generator.configuration.contentAssetCount.toString()
            )
            comdagenStatistics.mergeIntoStatisticsMap(
                it.libraryId,
                gatheredLibraryStatistics,
                comdagenStatistics.libraryStatistics
            )
        }
        comdagenStatistics.generalStatistics["Libraries top level seed"] =
                generator.configuration.initialSeed.toString()

        // For each library
        libraries.forEach { library ->
            val modelData = mapOf(
                "library" to library,
                "contentAssets" to library.contentAssets,
                "folders" to library.folders,
                "comdagensitestats" to comdagenStatistics.siteStatistics,
                "comdagenlibrarystats" to comdagenStatistics.libraryStatistics,
                "generalstatistics" to comdagenStatistics.generalStatistics
            )
            // Generate the specified output folder and a folder named by the libraryId containing the library xml
            File("$outputDir/${generator.configuration.outputDir}/${library.libraryId}").apply { mkdirs() }
            LOGGER.info("Start rendering library ${library.libraryId} with template $templateName")
            produce(
                templateName,
                "${generator.configuration.outputDir}/${library.libraryId}/${library.libraryId}.xml",
                modelData
            )
        }
    }

    @Throws(IOException::class)
    private fun render(siteTemplateName: String, preferencesTemplateName: String, generator: SiteGenerator) {
        val catalogGenerators: MutableSet<CatalogGenerator> = mutableSetOf()
        val pricebookGenerators: MutableSet<PricebookGenerator> = mutableSetOf()
        val customerGenerators: MutableSet<CustomerGenerator> = mutableSetOf()
        val inventoryGenerators: MutableSet<InventoryGenerator> = mutableSetOf()
        val promotionGenerators: MutableSet<PromotionGenerator> = mutableSetOf()
        val sourceCodeGenerators: MutableSet<SourceCodeGenerator> = mutableSetOf()
        val customerGroupGenerators: MutableSet<CustomerGroupGenerator> = mutableSetOf()
        val productlistGenerators: MutableSet<ProductlistGenerator> = mutableSetOf()
        val shippingGenerators: MutableSet<ShippingGenerator> = mutableSetOf()
        val storeGenerators: MutableSet<StoreGenerator> = mutableSetOf()

        // Adding top level seed for statistics
        comdagenStatistics.generalStatistics["Sites top level seed"] = generator.configuration.initialSeed.toString()

        generator.objects.forEach {
            // render site.xml
            LOGGER.info("Start rendering site ${it.id} with template $siteTemplateName")
            val siteModelData = mapOf("site" to it)
            File("$outputDir/${generator.configuration.outputDir}/${it.id}").apply { mkdirs() }
            produce(
                siteTemplateName,
                "${generator.configuration.outputDir}/${it.id}/site.xml", siteModelData
            )

            // Gather site statistics
            val gatheredSiteStatistics: Map<String, String> = mapOf(
                "Site id" to it.id,
                "Site seed" to it.seed.toString(),
                "Product count" to (it.catalogConfig?.totalProductCount()?.toString() ?: "???")
            )
            comdagenStatistics.mergeIntoStatisticsMap(it.id, gatheredSiteStatistics, comdagenStatistics.siteStatistics)

            // render preferences.xml
            LOGGER.info("Start rendering preferences for site ${it.id} with template $preferencesTemplateName")

            val preferencesModelData = mutableMapOf<String, Any>("currencies" to it.currencies)
            if (it.pricebookGenerator != null) {
                preferencesModelData["pricebooks"] = it.pricebookGenerator.objects.map { it.id }.toList()
            }

            preferencesModelData["navigationCatalog"] = it.navigationCatalog.id
            preferencesModelData["locales"] = it.regions.map { it.locale }

            if (it.customerGenerator != null) {
                preferencesModelData["customerListId"] = it.customerGenerator.listId
            }
            if (it.inventoryGenerator != null) {
                preferencesModelData["inventoryLists"] = it.inventoryGenerator.objects.map { it.listId }.toList()
            }
            produce(
                preferencesTemplateName,
                "${generator.configuration.outputDir}/${it.id}/preferences.xml", preferencesModelData
            )

            // copy site specific static files to site output directory
            if (it.staticFiles.isNotEmpty()) {
                copyStaticFiles(it.staticFiles, File(outputDir, "sites/${it.id}/"))
            }

            // render coupons
            if (it.couponGenerator != null) {
                LOGGER.info("Start rendering coupons for site ${it.id} with template ${it.couponGenerator.generatorTemplate}")
                val couponModelData = mapOf("gen" to it.couponGenerator)
                produce(it.couponGenerator.generatorTemplate, "sites/${it.id}/coupons.xml", couponModelData)
            }

            // render customer groups
            if (it.customerGroupGenerator != null) {
                LOGGER.info("Start rendering customer-groups for site ${it.id} with template ${it.customerGroupGenerator.generatorTemplate}")
                val customerGroupData = mapOf("gen" to it.customerGroupGenerator)
                produce(
                    it.customerGroupGenerator.generatorTemplate,
                    "sites/${it.id}/customer-groups.xml",
                    customerGroupData
                )

                customerGroupGenerators.add(it.customerGroupGenerator)
            }

            // render product-lists methods
            if (it.productlistGenerator != null) {
                LOGGER.info("Start rendering productlists methods for site ${it.id} with template ${it.productlistGenerator.generatorTemplate}")
                val productlistData = mapOf("gen" to it.productlistGenerator)
                produce(it.productlistGenerator.generatorTemplate, "sites/${it.id}/productlist.xml", productlistData)

                productlistGenerators.add(it.productlistGenerator)
            }

            // render shipping methods
            if (it.shippingGenerator != null) {
                LOGGER.info("Start rendering shipping methods for site ${it.id} with template ${it.shippingGenerator.generatorTemplate}")
                val shippingData = mapOf("gen" to it.shippingGenerator)
                produce(it.shippingGenerator.generatorTemplate, "sites/${it.id}/shipping.xml", shippingData)

                shippingGenerators.add(it.shippingGenerator)
            }

            // render source codes
            if (it.sourceCodeGenerator != null) {
                LOGGER.info("Start rendering source codes for site ${it.id} with template ${it.sourceCodeGenerator.generatorTemplate}")
                val sourceCodeData = mapOf("gen" to it.sourceCodeGenerator)
                produce(it.sourceCodeGenerator.generatorTemplate, "sites/${it.id}/sourcecodes.xml", sourceCodeData)

                sourceCodeGenerators.add(it.sourceCodeGenerator)
            }

            // render promotions
            if (it.promotionGenerator != null) {
                LOGGER.info("Start rendering promotions for site ${it.id} with template ${it.promotionGenerator.generatorTemplate}")
                val promotionData = mapOf("gen" to it.promotionGenerator)
                produce(it.promotionGenerator.generatorTemplate, "sites/${it.id}/promotions.xml", promotionData)

                promotionGenerators.add(it.promotionGenerator)
            }

            // render stores
            if (it.storeGenerator != null) {
                LOGGER.info("Start rendering stores for site ${it.id} with template ${it.storeGenerator.generatorTemplate}")
                val storeData = mapOf("gen" to it.storeGenerator)
                produce(it.storeGenerator.generatorTemplate, "sites/${it.id}/stores.xml", storeData)

                storeGenerators.add(it.storeGenerator)
            }

            // render sorting rules
            if (it.sortingRuleGenerator != null) {
                LOGGER.info("Start rendering sorting rules for site ${it.id} with template ${it.sortingRuleGenerator.generatorTemplate}")
                val sortingRuleData = mapOf("gen" to it.sortingRuleGenerator)
                produce(it.sortingRuleGenerator.generatorTemplate, "sites/${it.id}/sort.xml", sortingRuleData)
            }

            // render redirect urls
            if (it.redirectUrlGenerator != null) {
                LOGGER.info("Start rendering redirect urls for site ${it.id} with template ${it.redirectUrlGenerator.generatorTemplate}")
                val redirectUrlData = mapOf("gen" to it.redirectUrlGenerator)
                produce(it.redirectUrlGenerator.generatorTemplate, "sites/${it.id}/redirect-urls.xml", redirectUrlData)
            }

            // render navigation catalog
            LOGGER.info("Start rendering navigation catalog for site ${it.id} with template ${it.navigationCatalogTemplateName}")
            renderNavigationCatalog(it.navigationCatalogTemplateName, it.navigationCatalog)


            // add non site specific generators to generator sets
            if (it.catalogGenerator != null)
                catalogGenerators.add(it.catalogGenerator)

            if (it.pricebookGenerator != null)
                pricebookGenerators.add(it.pricebookGenerator)

            if (it.customerGenerator != null)
                customerGenerators.add(it.customerGenerator)

            if (it.inventoryGenerator != null)
                inventoryGenerators.add(it.inventoryGenerator)
        }

        // copy global static files
        if (generator.configuration.staticFiles != null && generator.configuration.staticFiles.isNotEmpty()) {
            LOGGER.info("Start copying static files.")
            copyStaticFiles(generator.configuration.staticFiles, outputDir)
        }

        val customAttributes: MutableMap<String, Set<AttributeDefinition>> = mutableMapOf()

        // render catalogs
        catalogGenerators.forEach {
            it.metadata.forEach { extendingObject, attributeDefs ->
                customAttributes.merge(extendingObject, attributeDefs, { u, v -> u + v })
            }

            LOGGER.info("Start rendering catalogs with template ${it.generatorTemplate}")
            render(it)
        }

        // render pricebooks
        pricebookGenerators.forEachIndexed { index, pricebookGenerator ->
            pricebookGenerator.metadata.forEach { extendingObject, attributeDefs ->
                customAttributes.merge(extendingObject, attributeDefs, { u, v -> u + v })
            }


            LOGGER.info("Start rendering pricebooks with template ${pricebookGenerator.generatorTemplate}")
            render(pricebookGenerator, index)
        }

        // render customer lists
        customerGenerators.forEachIndexed { index, customerGenerator ->
            customerGenerator.metadata.forEach { extendingObject, attributeDefs ->
                customAttributes.merge(extendingObject, attributeDefs, { u, v -> u + v })
            }

            LOGGER.info("Start rendering customer-list with template ${customerGenerator.generatorTemplate}")
            render(customerGenerator, index)
        }

        // render inventory lists
        inventoryGenerators.forEachIndexed { index, inventoryGenerator ->
            inventoryGenerator.metadata.forEach { extendingObject, attributeDefs ->
                customAttributes.merge(extendingObject, attributeDefs, { u, v -> u + v })
            }

            LOGGER.info("Start rendering inventory-list with template ${inventoryGenerator.generatorTemplate}")
            render(inventoryGenerator, index)
        }

        // get meta data of site specific generators
        (sourceCodeGenerators
                + promotionGenerators
                + customerGroupGenerators
                + shippingGenerators
                + storeGenerators)
            .forEach {
                it.metadata.forEach { extendingObject, attributeDefs ->
                    customAttributes.merge(extendingObject, attributeDefs, { u, v -> u + v })
                }
            }

        // render meta
        LOGGER.info("Start rendering meta")
        renderMeta(
            customAttributes = customAttributes,
            regions = generator.configuration.sites.flatMap { it.regions }.toSet()
        )
    }

    /**
     * Copies static files to the output directory
     *
     * @param filePaths paths to the static files relative to the template directory
     * *
     * @param outputDir copy files to this directory
     */
    private fun copyStaticFiles(filePaths: List<String>, outputDir: File) {
        filePaths.forEach { path ->
            val file = File(templateDir, "static/$path")

            if (file.exists()) {
                if (file.isDirectory) {
                    file.copyRecursively(File(outputDir, file.name), overwrite = true)
                } else {
                    file.copyTo(File(outputDir, file.name), overwrite = true)
                }
            } else {
                LOGGER.info("Invalid Path -> " + file.toString())
            }
        }
    }


    /**
     * Copy all artifacts given from the jar file to a destination directory.
     */
    private fun copyResources(from: List<String>, target: File) {
        from.forEach { fileName ->
            copyResource(fileName, File(target, fileName))
        }
    }


    /**
     * Copies a single file  with the supplied to the output directory it will check for the file in path that is sent
     * @param fileName name of the file that needs to be copied
     * @param outputDir copies file to this directory
     */
    private fun copyResource(fileName: String, outputDir: File) {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        javaClass.getResourceAsStream(fileName).use { input ->
            if (input != null) {
                Files.copy(input, outputDir.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } else {
                LOGGER.warn("File not found at : $fileName")
            }
        }
    }

    @Throws(IOException::class)
    fun produce(templateName: String, outputFileName: String, modelData: Map<String, Any>) {
        val template = freemarkerConfig.getTemplate(templateName)
        try {
            FileWriter(File(outputDir, outputFileName)).use { writer ->
                if (LOGGER.isDebugEnabled) {
                    // write output to the console for testing
                    val consoleWriter = OutputStreamWriter(System.out)
                    template.process(modelData, consoleWriter)
                }
                template.process(modelData, writer)
            }
        } catch (e: IOException) {
            LOGGER.error("Unable to produce {} from template {}", outputFileName, templateName, e)
            // TODO rethrow?
        } catch (e: TemplateException) {
            LOGGER.error("Unable to produce {} from template {}", outputFileName, templateName, e)
        }
    }

    /**
     * Inner class that holds the comdagen statistics. Since random nouns can be chosen as libraryId and site
     * names be specifically set, different maps are being used to avoid conflicts with duplicate keys.
     */
    private class ComdagenStatistics {
        /**
         * Gathers all site specific statistics needed for the ComdagenSummary content asset.
         * Map<siteId<stat, value>>
         */
        val siteStatistics: MutableMap<String, Map<String, String>> = mutableMapOf()

        /**
         * Gathers all library specific statistics for needed for the ComdagenSummary content asset.
         * Map<libraryId<stat, value>>
         */
        val libraryStatistics: MutableMap<String, Map<String, String>> = mutableMapOf()

        /**
         * This map gathers general statistics data about the generated data
         * Map<stat, value>
         */
        val generalStatistics: MutableMap<String, String> = mutableMapOf()

        /**
         * Merges new data into the statistics map for the right key (to library or site).
         */
        fun mergeIntoStatisticsMap(
            id: String,
            data: Map<String, String>,
            dest: MutableMap<String, Map<String, String>>
        ) {
            dest.merge(id, data) { old, new -> old + new }

        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(XMLOutputProducer::class.java)

    }
}

