package com.salesforce.comdagen

import com.salesforce.comdagen.config.AttributeId
import com.salesforce.comdagen.config.ContentConfiguration
import com.salesforce.comdagen.config.FolderConfiguration
import com.salesforce.comdagen.config.LibraryConfiguration
import com.salesforce.comdagen.generator.LibraryGenerator
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LibraryTest {

    @Rule
    @JvmField
    val tmpFolder = TemporaryFolder()

    @Test
    fun `default configuration tests`() {
        val defaultFolderConfig = FolderConfiguration(
            3456,
            null,
            null,
            "Testdescription",
            true,
            "root",
            false,
            1
        )
        val defaultContentConfig = ContentConfiguration(
            2345,
            null,
            "testfolder",
            true,
            AttributeId.BODY,
            1
        )

        val defaultLibraryConfig = LibraryConfiguration(
            1234,
            10,
            emptyList(),
            defaultFolderConfig,
            10,
            true,

            defaultContentConfig,
            1,
            "",
            "libraries",
            "library.ftlx"
        )


        val libraryGenerator = LibraryGenerator(defaultLibraryConfig, tmpFolder.root)
        val libraryObjects = libraryGenerator.objects


        assertTrue(
            1 <= libraryObjects.map { it.seed }.filter { it == 1234L }.count(),
            "Testing if initial seed is present"
        )

        assertEquals(1, libraryObjects.count(), "Testing library count")

        assertEquals(10, libraryObjects.sumBy { it.folders.count() }, "Test folder count")

        assertEquals(
            listOf("SiteGenesisSharedLibrary"),
            libraryObjects.map { it.libraryId }.toList(),
            "Test libraryIds"
        )

        assertEquals(
            10,
            libraryObjects.flatMap { lib ->
                lib.folders.asSequence().map { it.parent }
            }.filter { it == "root" }.count(),
            "Test folder parent folder"
        )

        assertEquals(
            10,
            libraryObjects.flatMap { lib ->
                lib.folders.asSequence().map { it.onlineFlag }
            }.filter { it }.count(),
            "Test folder onlineFlag"
        )

        assertEquals(
            10,
            libraryObjects.flatMap { lib ->
                lib.folders.asSequence().map { it.description }
            }.filter { it == "Testdescription" }.count(),
            "Test folder description"
        )

        assertEquals(9, libraryObjects.sumBy { it.contentAssets.count() }, "Test content asset count")


        assertEquals(9,
            libraryObjects.flatMap { lib ->
                lib.contentAssets.asSequence().map { it.classificationFolder }
            }.filter { it == "testfolder" }.count(),
            "Test content asset classificationfolder"
        )

        assertEquals(
            9,
            libraryObjects.flatMap { lib ->
                lib.contentAssets.asSequence().map { it.attributeId }
            }.filter { it == AttributeId.BODY }.count(),
            "Test content asset attributeid"
        )

    }

    @Test
    fun `testing default+custom configurations`() {
        val defaultFolderConfig = FolderConfiguration(
            328479,
            null,
            null,
            "Testdescription",
            true,
            "testparent",
            false,
            1
        )
        val customFolderConfig1 = FolderConfiguration(
            323,
            "folderIdTest1",
            "Testfolder",
            "Testdescription1",
            false,
            "testparent1",
            false,
            1
        )
        val customFolderConfig2 = FolderConfiguration(
            479,
            "folder2",
            "2. Folder",
            "Testdescription2",
            true,
            "testparent2",
            false,
            1
        )

        val defaultContentConfig = ContentConfiguration(
            223345,
            null,
            "classificationtest",
            true,
            AttributeId.BODY,
            1
        )

        val defaultLibraryConfig = LibraryConfiguration(
            122334,
            10,
            listOf(customFolderConfig1, customFolderConfig2),
            defaultFolderConfig,
            5,
            true,
            defaultContentConfig,
            1,
            "",
            "libraries",
            "library.ftlx"
        )


        val libraryGenerator = LibraryGenerator(defaultLibraryConfig, tmpFolder.root)
        val libraryObjects = libraryGenerator.objects

        assertTrue(
            1 <= libraryObjects.map { it.seed }.filter { it == 122334L }.count(),
            "Testing if initial seed is present"
        )

        assertEquals(1, libraryObjects.count(), "Testing library count")

        assertEquals(5, libraryObjects.sumBy { it.folders.count() }, "Test folder count")

        assertEquals(
            listOf("SiteGenesisSharedLibrary"),
            libraryObjects.map { it.libraryId }.toList(),
            "Test libraryIds"
        )

        assertEquals(
            mapOf(
                "testparent" to 3,
                "testparent1" to 1,
                "testparent2" to 1

            )
            , libraryObjects.flatMap { lib ->
                lib.folders.map { it.parent }.asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, Int>
            ,
            "Test folder parent folder"
        )

        assertEquals(
            4,
            libraryObjects.flatMap { lib ->
                lib.folders.asSequence().map { it.onlineFlag }
            }.filter { it }.count(),
            "Test folder onlineFlag"
        )

        assertEquals(
            mapOf(
                "Testdescription" to 3,
                "Testdescription1" to 1,
                "Testdescription2" to 1
            )
            , libraryObjects.flatMap { lib ->
                lib.folders.map { it.description }.asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, Int>
            ,
            "Test folder parent folder"
        )

        assertEquals(9, libraryObjects.sumBy { it.contentAssets.count() }, "Test content asset count")

        assertEquals(
            mapOf(
                "classificationtest" to 9
            )
            , libraryObjects.flatMap { lib ->
                lib.contentAssets.map { if (it.classificationFolder == null) "null" else it.classificationFolder }
                    .asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, Int>
            ,
            "Test folder parent folder"
        )

        assertEquals(
            mapOf(
                AttributeId.BODY to 9
            ) as Map<String, AttributeId>
            , libraryObjects.flatMap { lib ->
                lib.contentAssets.map { if (it.attributeId == null) "null" else it.attributeId }
                    .asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, AttributeId>
            ,
            "Test folder parent folder"
        )

    }

    @Test
    fun `disabled comdagen summary`() {
        val defaultFolderConfig = FolderConfiguration(
            328479,
            null,
            null,
            "Testdescription",
            true,
            "testparent",
            false,
            1
        )
        val customFolderConfig1 = FolderConfiguration(
            323,
            "folderIdTest1",
            "Testfolder",
            "Testdescription1",
            false,
            "testparent1",
            false,
            1
        )
        val customFolderConfig2 = FolderConfiguration(
            479,
            "folder2",
            "2. Folder",
            "Testdescription2",
            true,
            "testparent2",
            false,
            1
        )

        val defaultContentConfig = ContentConfiguration(
            223345,
            null,
            "classificationtest",
            true,
            AttributeId.BODY,
            1
        )

        val defaultLibraryConfig = LibraryConfiguration(
            122334,
            0,
            listOf(customFolderConfig1, customFolderConfig2),
            defaultFolderConfig,
            1,
            true,
            defaultContentConfig,
            1,
            "",
            "libraries",
            "library.ftlx"
        )

        val defaultLibraryConfig2 = LibraryConfiguration(
            1221334,
            1,
            listOf(customFolderConfig1, customFolderConfig2),
            defaultFolderConfig,
            2,
            false,
            defaultContentConfig,
            1,
            "",
            "libraries",
            "library.ftlx"
        )


        val libraryGenerator = LibraryGenerator(defaultLibraryConfig, tmpFolder.root)
        val libraryObjects = libraryGenerator.objects

        assertTrue(
            1 <= libraryObjects.map { it.seed }.filter { it == 122334L }.count(),
            "Testing if initial seed is present"
        )

        assertEquals(1, libraryObjects.count(), "Testing library count")

        assertEquals(1, libraryObjects.sumBy { it.folders.count() }, "Test folder count")

        assertEquals(
            listOf("SiteGenesisSharedLibrary"),
            libraryObjects.map { it.libraryId }.toList(),
            "Test libraryIds"
        )

        assertEquals(
            mapOf(
                "testparent1" to 1
            )
            , libraryObjects.flatMap { lib ->
                lib.folders.map { it.parent }.asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, Int>
            ,
            "Test folder parent folder"
        )

        assertEquals(
            0,
            libraryObjects.flatMap { lib ->
                lib.folders.asSequence().map { it.onlineFlag }
            }.filter { it }.count(),
            "Test folder onlineFlag"
        )

        assertEquals(
            mapOf(
                "Testdescription1" to 1
            )
            , libraryObjects.flatMap { lib ->
                lib.folders.map { it.description }.asSequence()
            }.asSequence().groupingBy { it }.eachCount()
            ,
            "Test folder parent folder"
        )

        assertEquals(0, libraryObjects.sumBy { it.contentAssets.count() }, "Test content asset count")

        assertEquals(0
            , libraryObjects.flatMap { lib ->
                lib.contentAssets.asSequence()
            }.count()
            ,
            "Test folder parent folder"
        )


        val libraryGenerator2 = LibraryGenerator(defaultLibraryConfig2, tmpFolder.root)
        val libraryObjects2 = libraryGenerator2.objects

        assertTrue(
            1 <= libraryObjects2.map { it.seed }.filter { it == 1221334L }.count(),
            "Testing if initial seed is present"
        )


        assertEquals(1, libraryObjects2.count(), "Testing library count")

        assertEquals(2, libraryObjects2.sumBy { it.folders.count() }, "Test folder count")

        assertEquals(
            listOf("SiteGenesisSharedLibrary"),
            libraryObjects2.map { it.libraryId }.toList(),
            "Test libraryIds"
        )

        assertEquals(
            mapOf(
                "testparent1" to 1,
                "testparent2" to 1
            )
            , libraryObjects2.flatMap { lib ->
                lib.folders.map { it.parent }.asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, Int>
            ,
            "Test folder parent folder"
        )

        assertEquals(
            1,
            libraryObjects2.flatMap { lib ->
                lib.folders.asSequence().map { it.onlineFlag }
            }.filter { it }.count(),
            "Test folder onlineFlag"
        )

        assertEquals(
            mapOf(
                "Testdescription1" to 1,
                "Testdescription2" to 1
            )
            , libraryObjects2.flatMap { lib ->
                lib.folders.map { it.description }.asSequence()
            }.asSequence().groupingBy { it }.eachCount()
            ,
            "Test folder parent folder"
        )
        assertEquals(1, libraryObjects2.sumBy { it.contentAssets.count() }, "Test generated content asset count")
    }
}
