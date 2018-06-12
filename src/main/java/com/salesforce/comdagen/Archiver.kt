/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.io.File
import java.io.FileInputStream

/**
 * Helper object to zip files and directories
 */
object Archiver {
    /**
     * Zip a file or directory
     * Directories get zipped recursively
     *
     * @param file Directory or file that should get zipped
     * @param outputZip File to which the zip archive should get written
     */
    fun zip(file: File, outputZip: File) {
        val os = ZipArchiveOutputStream(outputZip)

        addEntry(file, file.name, os)
        os.close()
    }

    private fun addEntry(entryFile: File, entryName: String, os: ZipArchiveOutputStream) {
        if (entryFile.isHidden) {
            return
        }

        if (entryFile.isDirectory) {
            val children: Array<File> = entryFile.listFiles()
            children.forEach { child ->
                addEntry(child, "$entryName/${child.name}", os)
            }
            return
        }

        val entry = ZipArchiveEntry(entryFile, entryName)
        val fis = FileInputStream(entryFile)

        // add entry header to zip archive
        os.putArchiveEntry(entry)

        // write entry data to zip archive
        val bytes: ByteArray = kotlin.ByteArray(1024)
        var length = fis.read(bytes)
        while (length >= 0) {
            os.write(bytes, 0, length)
            length = fis.read(bytes)
        }
        fis.close()
        os.closeArchiveEntry()
    }
}