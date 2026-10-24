package com.example.system

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * 🎨 Uygulama Simgeleri Bellek Önbelleği (AppIconCache)
 * 
 * Android PackageManager üzerinden yüklenen uygulama ikonlarını RAM'de (LruCache)
 * tutarak UI listelerinde hızlı ve takılmasız (jank-free) render edilmesini sağlar.
 */
object AppIconCache {
    /** Bellekte en fazla 120 uygulama simgesi tutan LRU (En az kullanılanı silen) önbellek */
    private val memoryCache = LruCache<String, ImageBitmap>(120)

    /**
     * Verilen paket adına ait uygulama simgesini döndürür.
     * Önce RAM önbelleğine bakar, yoksa PackageManager'dan çekip önbelleğe yazar.
     * 
     * @param context Android context
     * @param packageName Uygulama paket adı
     * @return Jetpack Compose ImageBitmap nesnesi veya null
     */
    fun getIcon(context: Context, packageName: String): ImageBitmap? {
        memoryCache.get(packageName)?.let { return it }

        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            val bitmap = drawableToBitmap(drawable)
            val imageBitmap = bitmap.asImageBitmap()
            memoryCache.put(packageName, imageBitmap)
            imageBitmap
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Android Drawable nesnesini Bitmap formatına dönüştürür.
     * 
     * @param drawable Dönüştürülecek Drawable
     * @return 72x72 - 96x96 piksel boyutlarında optimize edilmiş Bitmap
     */
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(96) else 72
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(96) else 72
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
