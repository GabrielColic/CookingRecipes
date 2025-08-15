package org.unizd.rma.colic.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream
import java.util.Date

class Converters {
    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        val out = ByteArrayOutputStream()
        // Use a compact format; JPEG/WEBP both fine for photos
        if (Build.VERSION.SDK_INT >= 30) {
            bmp.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, out)
        } else {
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return out.toByteArray()
    }

    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap {
        // 1) Bounds decode
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

        val maxDim = 1024
        val inSample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDim, maxDim)

        // 2) Sampled decode to reduce memory
        val opts = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = inSample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            ?: error("Bitmap decode failed")
    }



    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        var halfW = width / 2
        var halfH = height / 2
        while (halfW / inSampleSize >= reqWidth && halfH / inSampleSize >= reqHeight) {
            inSampleSize *= 2
        }
        return maxOf(1, inSampleSize)
    }

    @TypeConverter fun fromDate(date: Date): Long = date.time
    @TypeConverter fun toDate(millis: Long): Date = Date(millis)
}
