package com.example.dacs3_nguyencongduc.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.example.dacs3_nguyencongduc.MainActivity
import com.example.dacs3_nguyencongduc.R
import com.example.dacs3_nguyencongduc.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Spocket Widget - Hiển thị ảnh giao dịch mới nhất ra màn hình chính giống Locket
 */
class SpocketWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Lặp qua tất cả các widget đang được người dùng thêm ra màn hình
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }


    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.dacs3_nguyencongduc.UPDATE_WIDGET") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, SpocketWidgetProvider::class.java)
            )
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_spocket)

            // Intent để mở app khi ấn vào widget
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            // Dùng Coroutine để lấy data từ Room và tải ảnh từ mạng
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Lấy giao dịch mới nhất từ DB
                    val dao = AppDatabase.getDatabase(context).transactionDao()
                    val latestTx = dao.getLatestTransaction()

                    if (latestTx != null && latestTx.imageUri != null) {
                        // Load ảnh bằng Coil
                        val imageLoader = ImageLoader(context)
                        val request = ImageRequest.Builder(context)
                            .data(latestTx.imageUri)
                            // Bo tròn ảnh để làm widget đẹp hơn
                            .transformations(RoundedCornersTransformation(60f))
                            .size(500, 500)
                            .build()

                        val result = imageLoader.execute(request)
                        val drawable = result.drawable

                        if (drawable != null) {
                            val bitmap = drawable.toBitmap()
                            views.setImageViewBitmap(R.id.widget_image, bitmap)
                            views.setTextViewText(R.id.widget_text, latestTx.title)
                        } else {
                            views.setTextViewText(R.id.widget_text, "Không tải được ảnh")
                        }
                    } else {
                        views.setTextViewText(R.id.widget_text, "Chưa có ảnh nào")
                    }
                } catch (e: Exception) {
                    views.setTextViewText(R.id.widget_text, "Lỗi kết nối")
                } finally {
                    // Cập nhật lại widget lên màn hình
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
