package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DrawingRepository(private val context: Context, private val db: AppDatabase) {
    private val drawingDao = db.drawingDao()
    private val commentDao = db.commentDao()
    private val followDao = db.followDao()

    val allDrawingsFlow: Flow<List<DrawingEntity>> = drawingDao.getAllDrawingsFlow().flowOn(Dispatchers.IO)
    val followedArtistsFlow: Flow<List<FollowEntity>> = followDao.getAllFollowedFlow().flowOn(Dispatchers.IO)

    fun getDrawingByIdFlow(id: Int): Flow<DrawingEntity?> = drawingDao.getDrawingByIdFlow(id).flowOn(Dispatchers.IO)
    fun getCommentsForDrawingFlow(drawingId: Int): Flow<List<CommentEntity>> = commentDao.getCommentsForDrawingFlow(drawingId).flowOn(Dispatchers.IO)
    fun isArtistFollowedFlow(name: String): Flow<Boolean> = followDao.isArtistFollowedFlow(name).flowOn(Dispatchers.IO)

    fun searchDrawingsFlow(query: String): Flow<List<DrawingEntity>> {
        val searchQuery = "%$query%"
        return drawingDao.searchDrawingsFlow(code = query, query = searchQuery).flowOn(Dispatchers.IO)
    }

    suspend fun insertDrawing(drawing: DrawingEntity): Long = withContext(Dispatchers.IO) {
        drawingDao.insertDrawing(drawing)
    }

    suspend fun updateDrawing(drawing: DrawingEntity) = withContext(Dispatchers.IO) {
        drawingDao.updateDrawing(drawing)
    }

    suspend fun deleteDrawing(id: Int) = withContext(Dispatchers.IO) {
        val drawing = drawingDao.getDrawingById(id)
        if (drawing != null) {
            try {
                val file = File(drawing.imagePath)
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        commentDao.deleteCommentsForDrawing(id)
        drawingDao.deleteDrawingById(id)
    }

    suspend fun insertComment(comment: CommentEntity) = withContext(Dispatchers.IO) {
        commentDao.insertComment(comment)
    }

    suspend fun followArtist(artist: String) = withContext(Dispatchers.IO) {
        followDao.followArtist(FollowEntity(artist))
    }

    suspend fun unfollowArtist(artist: String) = withContext(Dispatchers.IO) {
        followDao.unfollowArtist(artist)
    }

    suspend fun checkAndSeedData() = withContext(Dispatchers.IO) {
        if (drawingDao.getDrawingCount() == 0) {
            val drawingsDir = File(context.filesDir, "drawings")
            if (!drawingsDir.exists()) drawingsDir.mkdirs()

            // 1. Starry Night
            val path1 = saveProceduralImage(drawingsDir, "starry_night") { canvas, paint ->
                val skyShader = LinearGradient(0f, 0f, 0f, canvas.height.toFloat(), Color.parseColor("#0a1128"), Color.parseColor("#1c2541"), Shader.TileMode.CLAMP)
                paint.shader = skyShader
                canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
                paint.shader = null

                paint.color = Color.parseColor("#ffea00")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 10f
                paint.alpha = 150
                canvas.drawCircle(300f, 250f, 80f, paint)
                canvas.drawCircle(500f, 350f, 110f, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#ffc300")
                paint.alpha = 255
                canvas.drawCircle(180f, 150f, 60f, paint)
                paint.color = Color.parseColor("#0a1128")
                canvas.drawCircle(210f, 140f, 55f, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#ffea00")
                val stars = listOf(Pair(100f, 200f), Pair(400f, 100f), Pair(700f, 150f), Pair(600f, 400f), Pair(350f, 500f), Pair(200f, 380f))
                for (s in stars) {
                    canvas.drawCircle(s.first, s.second, 8f, paint)
                }

                paint.color = Color.parseColor("#1b4332")
                paint.shader = null
                canvas.drawRect(0f, canvas.height.toFloat() - 100f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            }

            // 2. Sunset
            val path2 = saveProceduralImage(drawingsDir, "sunset") { canvas, paint ->
                val sunsetGradient = LinearGradient(0f, 0f, 0f, canvas.height.toFloat(), Color.parseColor("#7209b7"), Color.parseColor("#f72585"), Shader.TileMode.CLAMP)
                paint.shader = sunsetGradient
                canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
                paint.shader = null

                paint.color = Color.parseColor("#ffb703")
                canvas.drawCircle(canvas.width / 2f, canvas.height - 120f, 140f, paint)

                paint.color = Color.parseColor("#240046")
                paint.alpha = 230
                canvas.drawRect(0f, canvas.height - 150f, canvas.width.toFloat(), canvas.height.toFloat(), paint)

                paint.color = Color.parseColor("#10002b")
                paint.alpha = 255
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 14f
                canvas.drawLine(150f, canvas.height.toFloat(), 120f, canvas.height - 300f, paint)
                paint.strokeWidth = 8f
                canvas.drawLine(120f, canvas.height - 300f, 60f, canvas.height - 330f, paint)
                canvas.drawLine(120f, canvas.height - 300f, 180f, canvas.height - 310f, paint)
                canvas.drawLine(120f, canvas.height - 300f, 130f, canvas.height - 370f, paint)
            }

            // 3. Flower Garden
            val path3 = saveProceduralImage(drawingsDir, "garden") { canvas, paint ->
                canvas.drawColor(Color.parseColor("#e8f5e9"))
                val flowers = listOf(
                    Triple(200f, 400f, Color.parseColor("#e91e63")),
                    Triple(500f, 350f, Color.parseColor("#9c27b0")),
                    Triple(350f, 500f, Color.parseColor("#ff9800")),
                    Triple(650f, 450f, Color.parseColor("#00bcd4"))
                )
                for (f in flowers) {
                    paint.color = Color.parseColor("#4caf50")
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 6f
                    canvas.drawLine(f.first, f.second, f.first, f.second + 150f, paint)

                    paint.color = f.third
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(f.first - 25f, f.second, 20f, paint)
                    canvas.drawCircle(f.first + 25f, f.second, 20f, paint)
                    canvas.drawCircle(f.first, f.second - 25f, 20f, paint)
                    canvas.drawCircle(f.first, f.second + 25f, 20f, paint)
                    paint.color = Color.parseColor("#ffeb3b")
                    canvas.drawCircle(f.first, f.second, 18f, paint)
                }
            }

            val draw1 = DrawingEntity(
                title = "ليلة مرصعة بالنجوم",
                description = "محاكاة خيالية لمشهد النجوم الساحر والغيوم الحلزونية والقمر الذهبي الوهاب.",
                drawingIdString = "R-101",
                imagePath = path1,
                isPublic = true,
                authorName = "فان جوخ العرب",
                likesCount = 5,
                likedByUsersJson = ",أحمد,ليلى,فاطمة,",
                isLocalOriginal = false
            )
            val id1 = drawingDao.insertDrawing(draw1).toInt()

            val draw2 = DrawingEntity(
                title = "غروب ساحر",
                description = "تدرج ألوان الغروب الدافئة مع ظل شجرة النخيل والبحر المليء بالأسرار.",
                drawingIdString = "R-102",
                imagePath = path2,
                isPublic = true,
                authorName = "سارة أحمد",
                likesCount = 2,
                likedByUsersJson = ",سعيد,فاطمة,",
                isLocalOriginal = false
            )
            val id2 = drawingDao.insertDrawing(draw2).toInt()

            val draw3 = DrawingEntity(
                title = "حديقة الزهور",
                description = "بستان بديع من الزهور الملونة المتبرعمة في يوم ربيعي مشرق ودافئ.",
                drawingIdString = "R-103",
                imagePath = path3,
                isPublic = true,
                authorName = "سامي فنان",
                likesCount = 3,
                likedByUsersJson = ",أحمد,سعيد,يوسف,",
                isLocalOriginal = false
            )
            val id3 = drawingDao.insertDrawing(draw3).toInt()

            val comments = listOf(
                CommentEntity(drawingId = id1, authorName = "أحمد", content = "يا لها من رسمة ممتازة! التدرجات مدهشة للغاية!"),
                CommentEntity(drawingId = id1, authorName = "ليلى", content = "أجمل ما رأيت اليوم، مستوحاة من العصر الكلاسيكي."),
                CommentEntity(drawingId = id1, authorName = "جمال", content = "أبدعت يا صديقي! استمر في الهامنا."),
                CommentEntity(drawingId = id2, authorName = "فاطمة", content = "ألوان هادئة ومريحة للأعصاب. ذكرتني بغروب شاطئ جدة."),
                CommentEntity(drawingId = id3, authorName = "يوسف", content = "الألوان الربيعية تبهج القلب! استمر.")
            )
            for (c in comments) {
                commentDao.insertComment(c)
            }
        }
    }

    private fun saveProceduralImage(dir: File, name: String, drawAction: (Canvas, Paint) -> Unit): String {
        val file = File(dir, "$name.png")
        if (file.exists()) return file.absolutePath

        val bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        drawAction(canvas, paint)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return file.absolutePath
    }
}
