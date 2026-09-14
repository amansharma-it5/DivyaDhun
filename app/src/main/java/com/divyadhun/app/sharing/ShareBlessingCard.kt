package com.divyadhun.app.sharing

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.divyadhun.app.R
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.Track
import java.io.File
import java.io.FileOutputStream

/** Generates an original, local share image and opens the system Sharesheet on explicit action. */
object ShareBlessingCard {
    fun message(track: Track, deityName: String, language: Language): String = if (language == Language.HINDI) {
        "आज $deityName के साथ भक्ति का एक पल — ${track.title.hindi}\nदिव्यधुन"
    } else {
        "A moment of Bhakti with $deityName — ${track.title.english}\nDivyaDhun"
    }

    fun share(context: Context, track: Track, language: Language, deityName: String) {
        val text = message(track, deityName, language)
        val uri = createImageUri(context, track, language, deityName)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (uri == null) "text/plain" else "image/png"
            putExtra(Intent.EXTRA_TEXT, text)
            uri?.let {
                putExtra(Intent.EXTRA_STREAM, it)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_blessing_chooser)))
    }

    fun share(context: Context, track: Track, language: Language) {
        share(context, track, language, context.getString(deityNameRes(track.deityId)))
    }

    private fun createImageUri(context: Context, track: Track, language: Language, deityName: String) = runCatching {
        val directory = File(context.cacheDir, "share-cards").apply { mkdirs() }
        val file = File(directory, "${track.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.png")
        FileOutputStream(file).use { output -> createBitmap(track, language, deityName).compress(Bitmap.CompressFormat.PNG, 100, output) }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }.getOrNull()

    private fun createBitmap(track: Track, language: Language, deityName: String): Bitmap {
        val bitmap = Bitmap.createBitmap(1200, 630, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.rgb(255, 249, 239))
        val maroon = Color.rgb(91, 24, 39)
        val saffron = Color.rgb(201, 116, 35)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = maroon }
        canvas.drawRoundRect(36f, 36f, 1164f, 594f, 34f, 34f, paint)
        paint.color = Color.rgb(255, 249, 239)
        canvas.drawCircle(190f, 315f, 112f, paint)
        paint.color = saffron
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 118f
        canvas.drawText(symbol(track.deityId), 190f, 355f, paint)
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(255, 249, 239)
        paint.textSize = 34f
        canvas.drawText("दिव्यधुन  ·  DivyaDhun", 370f, 125f, paint)
        paint.textSize = 52f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        drawWrapped(canvas, localizedTitle(track, language), 370f, 235f, 710f, paint, 52f)
        paint.textSize = 30f
        paint.typeface = Typeface.DEFAULT
        drawWrapped(canvas, message(track, deityName, language).substringBefore("\n"), 370f, 430f, 710f, paint, 30f)
        return bitmap
    }

    private fun localizedTitle(track: Track, language: Language) = if (language == Language.HINDI) track.title.hindi else track.title.english

    private fun drawWrapped(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, paint: Paint, lineHeight: Float) {
        var line = ""
        var baseline = y
        text.split(" ").forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line, x, baseline, paint)
                baseline += lineHeight
                line = word
            } else line = candidate
        }
        if (line.isNotEmpty()) canvas.drawText(line, x, baseline, paint)
    }

    private fun symbol(deityId: String) = when (deityId) {
        "hanuman" -> "ह"
        "shiv" -> "ॐ"
        "krishna" -> "क"
        "ram" -> "र"
        "ganesh" -> "ग"
        "durga" -> "द"
        "lakshmi" -> "ल"
        "sai" -> "स"
        else -> "ॐ"
    }

    private fun deityNameRes(id: String) = when (id) {
        "hanuman" -> R.string.deity_hanuman
        "shiv" -> R.string.deity_shiv
        "krishna" -> R.string.deity_krishna
        "ram" -> R.string.deity_ram
        "ganesh" -> R.string.deity_ganesh
        "durga" -> R.string.deity_durga
        "lakshmi" -> R.string.deity_lakshmi
        "sai" -> R.string.deity_sai
        else -> R.string.unknown_deity
    }
}
