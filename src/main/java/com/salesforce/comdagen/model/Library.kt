package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.SupportedZone
import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.AttributeConfig
import com.salesforce.comdagen.config.ContentConfiguration
import com.salesforce.comdagen.config.GlobalLibraryConfiguration
import com.salesforce.comdagen.config.LibraryConfiguration
import java.io.File



//TODO: What do I really need here??
data class Library(private val seed: Long,
                   private val config: LibraryConfiguration,
                   private val configDir: File) {
val libraryId: String get() = config.libraryId ?: "hashID" + this.hashCode().toString()

}


// TODO: Implement IndexedLibrary


data class Content(val initialSeed: Long,
                   //val elementCount: Int = 1,
                   private val contentId: String,
                   val contentConfig: ContentConfiguration,
                   val regions: List<SupportedZone>
) {


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

data class Folder(val initialSeed: Long){
    //TODO: Implement
}