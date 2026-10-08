package com.salesforce.comdagen

import com.salesforce.comdagen.config.AttributeId
import com.salesforce.comdagen.config.ContentConfiguration
import com.salesforce.comdagen.config.FolderConfiguration
import com.salesforce.comdagen.config.LibraryConfiguration
import com.salesforce.comdagen.generator.LibraryGenerator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LibraryTest {

    @TempDir
    lateinit var tmpFolder: File

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


        val libraryGenerator = LibraryGenerator(defaultLibraryConfig, tmpFolder)
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


        val libraryGenerator = LibraryGenerator(defaultLibraryConfig, tmpFolder)
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


        val libraryGenerator = LibraryGenerator(defaultLibraryConfig, tmpFolder)
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


        val libraryGenerator2 = LibraryGenerator(defaultLibraryConfig2, tmpFolder)
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

    /**
     * Test matrix whose axes come directly from real customer incidents — not made-up numbers.
     *
     * Primary axis: per-PO localized-AV volume = bodyCharsPerLocale × localeCount
     *   This is the actual quota unit (MChars) and was the root cause of the BBKT Sev1 and
     *   Kering performance degradation, NOT raw row count.
     *
     * Secondary axis: asset count (row count)
     *
     * Customer sources:
     *   BBKT (Fullbeauty) Sev1 2026 — 273k AV rows; 68k with CLOBs; 25% of those at 188KB–376KB
     *     drove 80% of storage; cold cache replication 40–80 min (vs 3–7 min baseline).
     *   Kering-class outliers — 500KB–1.2MB body per locale, 100+ locales.
     *   bjjz_prd fleet data — 47k rows at ~1000 KB/row caused heavier exhaustion than
     *     aahh_prd with 457k rows at ~100 KB/row. Proves row count alone is meaningless.
     *   Rick's ask (CJK) — ja + ko added because 1 CJK char = 3 bytes UTF-8; this is exactly
     *     where char count diverges from byte count, stressing the char-based MChars quota.
     *
     * Body sizes are scaled down for unit-test speed; full-scale values for load tests are in
     * config/libraries-bbkt.yaml and config/libraries-kering.yaml.
     */
    @Test
    fun `content asset AV-volume matrix - primary axis is localized body volume per PO`() {
        data class Case(
            val label: String,
            val locales: List<SupportedZone>,
            val bodyLengthChars: Int,
            val assetCount: Int
        )

        val cases = listOf(
            // CJK baseline — all three CJK locales, small body.
            // 1 Japanese/Korean/Chinese char = 3 bytes UTF-8 but counts as 1 char in the quota.
            Case("cjk-baseline",
                listOf(SupportedZone.Chinese, SupportedZone.Japanese, SupportedZone.Korean),
                500, 5),

            // BBKT typical — 67% of BBKT CLOB rows were under 63 KB.
            // Unit-test body is 1 500 chars (scaled); full load-test value: 63 000.
            Case("bbkt-typical",
                SupportedZone.values().toList(),
                1_500, 5),

            // BBKT outlier — 25% of rows were 188 KB–376 KB and monopolised 80% of storage.
            // Unit-test body is 3 000 chars (scaled); full load-test value: 375 000.
            Case("bbkt-outlier",
                SupportedZone.values().toList(),
                3_000, 5),

            // Kering-class — 500 KB–1.2 MB bodies, 100+ locales.
            // Unit-test body is 6 000 chars (scaled); full load-test value: 1 200 000.
            Case("kering-class",
                SupportedZone.values().toList(),
                6_000, 5)
        )

        for (case in cases) {
            val contentConfig = ContentConfiguration(
                initialSeed = 2345,
                contentId = null,
                classificationFolder = "testfolder",
                bodyLengthChars = case.bodyLengthChars
            )
            val libraryConfig = LibraryConfiguration(
                initialSeed = 1234,
                generatedContentAssetCount = case.assetCount + 1, // +1 for summary asset
                defaultContentAssetConfig = contentConfig,
                generatedFolderCount = 1,
                renderComdagenSummaryContentAsset = true,
                locales = case.locales
            )

            val assets = LibraryGenerator(libraryConfig, tmpFolder).objects
                .flatMap { it.contentAssets }
                .toList()

            // Secondary axis — row count must be correct
            assertEquals(case.assetCount, assets.size,
                "${case.label}: expected ${case.assetCount} assets")

            // Primary axis — localizedBodyVolumeChars = bodyCharsPerLocale × localeCount
            val expectedMinVolumePerAsset = case.locales.size.toLong() * case.bodyLengthChars
            assets.forEach { asset ->
                assertTrue(
                    asset.localizedBodyVolumeChars >= expectedMinVolumePerAsset,
                    "${case.label}: asset localizedBodyVolumeChars ${asset.localizedBodyVolumeChars} " +
                        "< expected minimum $expectedMinVolumePerAsset " +
                        "(${case.locales.size} locales × ${case.bodyLengthChars} chars)"
                )
            }
        }
    }
}
