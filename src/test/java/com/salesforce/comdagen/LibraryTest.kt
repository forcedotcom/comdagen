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
            null,
            10,
            emptyList(),
            defaultFolderConfig,
            10,
            true,
            emptyList(),

            defaultContentConfig,
            3,
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

        assertEquals(3 + 1, libraryObjects.count(), "Testing library count")

        assertEquals(10 * 3 + 1, libraryObjects.sumBy { it.folders.count() }, "Test folder count")

        assertEquals(
            listOf("ComdagenSummaryLibrary", "Library_0", "Library_1", "Library_2"),
            libraryObjects.map { it.libraryId }.toList(),
            "Test libraryIds"
        )

        assertEquals(
            30 + 1,
            libraryObjects.flatMap { lib ->
                lib.folders.asSequence().map { it.parent }
            }.filter { it == "root" }.count(),
            "Test folder parent folder"
        )

        assertEquals(
            30 + 1,
            libraryObjects.flatMap { lib ->
                lib.folders.asSequence().map { it.onlineFlag }
            }.filter { it }.count(),
            "Test folder onlineFlag"
        )

        assertEquals(
            30,
            libraryObjects.flatMap { lib ->
                lib.folders.asSequence().map { it.description }
            }.filter { it == "Testdescription" }.count(),
            "Test folder description"
        )


        // The 'ComdagenSummary' content assets is being created in the template and therefore does not count as content
        // asset in the Library class.
        assertEquals(10 * 3, libraryObjects.sumBy { it.contentAssets.count() }, "Test content asset count")


        assertEquals(10 * 3,
            libraryObjects.flatMap { lib ->
                lib.contentAssets.asSequence().map { it.classificationFolder }
            }.filter { it == "testfolder" }.count(),
            "Test content asset classificationfolder"
        )

        assertEquals(
            10 * 3,
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

        val customFolderConfig3 = FolderConfiguration(
            479,
            "folder3",
            "3. Folder",
            "Testdescription3",
            false,
            "testparent3",
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


        val customContentConfig = ContentConfiguration(
            6375,
            null,
            null,
            false,
            AttributeId.BODY,
            1
        )


        val customLibraryConfig = LibraryConfiguration(
            1223345,
            null,
            20,
            listOf(customFolderConfig3),
            customFolderConfig2,
            6,
            false,
            emptyList(),
            customContentConfig,
            1,
            "",
            "libraries",
            "library.ftlx"
        )

        val defaultLibraryConfig = LibraryConfiguration(
            122334,
            "Testing library",
            10,
            listOf(customFolderConfig1, customFolderConfig2),
            defaultFolderConfig,
            5,
            true,
            listOf(customLibraryConfig),
            defaultContentConfig,
            7,
            "",
            "libraries",
            "library.ftlx"
        )


        val libraryGenerator = LibraryGenerator(defaultLibraryConfig, tmpFolder.root)
        val libraryObjects = libraryGenerator.objects

        assertTrue(
            1 <= libraryObjects.map { it.seed }.filter { it == 122334L }.count() &&
                    1 <= libraryObjects.map { it.seed }.filter { it == 1223345L }.count(),
            "Testing if initial seed is present"
        )

        assertEquals(7+1, libraryObjects.count(), "Testing library count")

        assertEquals(6 * 5 + 6 + 1, libraryObjects.sumBy { it.folders.count() }, "Test folder count")

        assertEquals(
            listOf(
                "ComdagenSummaryLibrary",
                "Testing library",
                "Library_1",
                "Library_2",
                "Library_3",
                "Library_4",
                "Library_5",
                "Library_6"
            ),
            libraryObjects.map { it.libraryId }.toList(),
            "Test libraryIds"
        )

        assertEquals(
            mapOf(
                "testparent" to 3 * 6,
                "testparent1" to 6,
                "testparent2" to 11,
                "testparent3" to 1,
                "root" to 1
            )
            , libraryObjects.flatMap { lib ->
                lib.folders.map { it.parent }.asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, Int>
            ,
            "Test folder parent folder"
        )

        assertEquals(
            29 + 1,
            libraryObjects.flatMap { lib ->
                lib.folders.asSequence().map { it.onlineFlag }
            }.filter { it }.count(),
            "Test folder onlineFlag"
        )

        assertEquals(
            mapOf(
                "Testdescription" to 3 * 6,
                "Testdescription1" to 6,
                "Testdescription2" to 11,
                "Testdescription3" to 1,
                "Root folder containing the comdagen summary content asset." to 1
            )
            , libraryObjects.flatMap { lib ->
                lib.folders.map { it.description }.asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, Int>
            ,
            "Test folder parent folder"
        )

        assertEquals(6 * 10 + 20, libraryObjects.sumBy { it.contentAssets.count() }, "Test content asset count")

        assertEquals(
            mapOf(
                "classificationtest" to 60,
                "null" to 20
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
                AttributeId.BODY to 80
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
    fun `testing cornercase configurations`() {
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

        val customFolderConfig3 = FolderConfiguration(
            479,
            "folder3",
            "3. Folder",
            "Testdescription3",
            false,
            "testparent3",
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


        val customContentConfig = ContentConfiguration(
            6375,
            null,
            null,
            false,
            AttributeId.BODY,
            1
        )


        val customLibraryConfig = LibraryConfiguration(
            1223345,
            null,
            20,
            listOf(customFolderConfig3),
            customFolderConfig2,
            6,
            false,
            emptyList(),
            customContentConfig,
            1,
            "",
            "libraries",
            "library.ftlx"
        )

        val defaultLibraryConfig = LibraryConfiguration(
            122334,
            null,
            0,
            listOf(customFolderConfig1, customFolderConfig2),
            defaultFolderConfig,
            1,
            true,
            emptyList(),
            defaultContentConfig,
            1,
            "",
            "libraries",
            "library.ftlx"
        )

        val defaultLibraryConfig2 = LibraryConfiguration(
            1221334,
            "Testing library",
            1,
            listOf(customFolderConfig1, customFolderConfig2),
            defaultFolderConfig,
            2,
            true,
            listOf(customLibraryConfig, customLibraryConfig),
            defaultContentConfig,
            2,
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

        assertEquals(1+1, libraryObjects.count(), "Testing library count")

        assertEquals(1+1, libraryObjects.sumBy { it.folders.count() }, "Test folder count")

        assertEquals(
            listOf("ComdagenSummaryLibrary", "Library_0"),
            libraryObjects.map { it.libraryId }.toList(),
            "Test libraryIds"
        )

        assertEquals(
            mapOf(
                "testparent1" to 1,
                "root" to 1
            )
            , libraryObjects.flatMap { lib ->
                lib.folders.map { it.parent }.asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, Int>
            ,
            "Test folder parent folder"
        )

        assertEquals(
            1,
            libraryObjects.flatMap { lib ->
                lib.folders.asSequence().map { it.onlineFlag }
            }.filter { it }.count(),
            "Test folder onlineFlag"
        )

        assertEquals(
            mapOf(
                "Testdescription1" to 1,
                "Root folder containing the comdagen summary content asset." to 1
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
            }
                .count()
            ,
            "Test folder parent folder"
        )


        val libraryGenerator2 = LibraryGenerator(defaultLibraryConfig2, tmpFolder.root)
        val libraryObjects2 = libraryGenerator2.objects

        assertTrue(
            1 <= libraryObjects2.map { it.seed }.filter { it == 1221334L }.count(),
            "Testing if initial seed is present"
        )

        // Additional 'ComdagenSummaryLibrary' is created (=3) but not configured in library configuration (=2).
        assertEquals(2 + 1, libraryObjects2.count(), "Testing library count")

        assertEquals(2 + 6 + 1, libraryObjects2.sumBy { it.folders.count() }, "Test folder count")

        assertEquals(
            listOf("ComdagenSummaryLibrary", "Testing library", "Library_1"),
            libraryObjects2.map { it.libraryId }.toList(),
            "Test libraryIds"
        )

        assertEquals(
            mapOf(
                "root" to 1,
                "testparent1" to 1,
                "testparent2" to 6,
                "testparent3" to 1
            )
            , libraryObjects2.flatMap { lib ->
                lib.folders.map { it.parent }.asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, Int>
            ,
            "Test folder parent folder"
        )

        assertEquals(
            6 + 1,
            libraryObjects2.flatMap { lib ->
                lib.folders.asSequence().map { it.onlineFlag }
            }.filter { it }.count(),
            "Test folder onlineFlag"
        )

        assertEquals(
            mapOf(
                "Testdescription1" to 1,
                "Testdescription2" to 6,
                "Testdescription3" to 1,
                "Root folder containing the comdagen summary content asset." to 1
            )
            , libraryObjects2.flatMap { lib ->
                lib.folders.map { it.description }.asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, Int>
            ,
            "Test folder parent folder"
        )

        assertEquals(20 + 1, libraryObjects2.sumBy { it.contentAssets.count() }, "Test content asset count")


        assertEquals(
            mapOf(
                "null" to 20,
                "classificationtest" to 1
            )
            , libraryObjects2.flatMap { lib ->
                lib.contentAssets.map { if (it.classificationFolder == null) "null" else it.classificationFolder }
                    .asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, Int>
            ,
            "Test folder parent folder"
        )

        assertEquals(
            mapOf(
                AttributeId.BODY to 21
            ) as Map<String, AttributeId>
            , libraryObjects2.flatMap { lib ->
                lib.contentAssets.map { if (it.attributeId == null) "null" else it.attributeId }
                    .asSequence()
            }.asSequence().groupingBy { it }.eachCount() as Map<String, AttributeId>
            ,
            "Test folder parent folder"
        )
    }
}
