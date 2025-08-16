package org.unizd.rma.colic.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream
import java.util.Date
import kotlin.math.max
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

class Converters {

    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        val maxDim = 2048
        val w = bmp.width
        val h = bmp.height
        val maxSide = max(w, h).toFloat()
        val scaled: Bitmap = if (maxSide > maxDim) {
            val scale = maxSide / maxDim
            val nw = (w / scale).toInt()
            val nh = (h / scale).toInt()
            bmp.scale(nw, nh)
        } else bmp

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)

        if (scaled !== bmp) scaled.recycle()
        return out.toByteArray()
    }

    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap {
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

            val maxDim = 1024
            val inSample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDim, maxDim)

            val opts = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = inSample
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                ?: throw IllegalStateException("Bitmap decode returned null")
        }.getOrElse {
            createBitmap(32, 32).apply {
                eraseColor(Color.LTGRAY)
            }
        }
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
