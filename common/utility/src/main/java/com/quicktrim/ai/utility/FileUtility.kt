package com.quicktrim.ai.utility

import android.content.Context
import java.io.File
import java.io.IOException

object FileUtility {

    fun getFileFromRaw(context: Context, resId: Int, fileName: String): File? {
        return try {
            // Open the raw resource as an InputStream and read all its bytes.
            val bytes = context.resources.openRawResource(resId).use { it.readBytes() }

            // Create the output file in the app's external files directory.
            val file = File(context.cacheDir, fileName)

            // Write the bytes to the file.
            file.outputStream().use { it.write(bytes) }

            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}