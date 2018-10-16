package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.SupportedZone
import com.salesforce.comdagen.config.ContentConfiguration
import com.salesforce.comdagen.config.LibraryConfiguration
import java.io.File


data class Library(
    /**
     * This internId is necessary to set a unique libraryId when
     * none is defined in the configuration file. The internId
     * will be related to the order in which the libraries are
     * generated.
     */
    private val internId: Int,
    private val seed: Long,
                   private val config: LibraryConfiguration,
                   private val configDir: File) {
    val libraryId: String get() = config.libraryId ?: "Library_"+internId.toString()


    val contentAssets: List<Content>
        get() {
            val x = (1..config.contentAssetCount).map {
                Content(
                    it,
                    seed,
                    config.defaultContentAssetConfig,
                    SupportedZone.values().asList()
                )
            }
            return x
        }


}


data class Content(
    private val internId: Int,
    val initialSeed: Long,
                   val contentConfig: ContentConfiguration,
                   val regions: List<SupportedZone>
) {

    //TODO: contentId is always null?? dont use get() here for ftlx
    val contentId: String get() = contentConfig.contentId ?: "ContentId_"+internId.toString()

    val name: Map<SupportedZone, String> get() = regions.associateBy({ it }, { RandomData.getRandomNoun(initialSeed, it) })

    val description: Map<SupportedZone, String> get() = regions.associateBy({ it }, { RandomData.getRandomSentence(initialSeed, it) })

    val onlineFlag: Boolean get() = contentConfig.onlineFlag

    val searchable: Boolean get() = contentConfig.searchableFlag

    val folderId: String? get() = contentConfig.classificationFolder

    val classificationFolder: String? get() = contentConfig.classificationFolder


    /**
     * This implements customAttributes for libraries.
     */
//    val customAttributes: Map<String, AttributeConfig>?
//        get() = contentConfig.attributeDefinitions("Library").map{
//            CustomAttribute(it, initialSeed + "customAttributs".hashCode())
//        }
}

data class Folder(val initialSeed: Long) {
    //TODO: Implement
}