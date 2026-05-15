package com.example.dacs3_nguyencongduc.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Cloudinary Repository - Upload ảnh lên Cloudinary
 *
 * ╔══════════════════════════════════════════════════════╗
 * ║  HƯỚNG DẪN THIẾT LẬP CLOUDINARY:                   ║
 * ║                                                      ║
 * ║  1. Vào https://cloudinary.com → Đăng ký miễn phí   ║
 * ║  2. Vào Dashboard → copy "Cloud Name"               ║
 * ║  3. Thay CLOUD_NAME bên dưới = cloud name của bạn   ║
 * ║  4. Settings → Upload → Add Upload Preset:          ║
 * ║     - Tên: "capmoney_unsigned"                      ║
 * ║     - Signing Mode: "Unsigned"                      ║
 * ║     - Folder: "capmoney"                            ║
 * ║     - Bấm Save                                      ║
 * ╚══════════════════════════════════════════════════════╝
 */
object CloudinaryConfig {
    // ⚠️ THAY BẰNG CLOUD NAME CỦA BẠN
    const val CLOUD_NAME = "dn9zubibf"

    // Upload preset (unsigned) - tạo trong Cloudinary Dashboard
    const val UPLOAD_PRESET = "SPocket"

    // URL upload
    val UPLOAD_URL get() = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
}

/**
 * Repository xử lý upload ảnh lên Cloudinary
 * Dùng unsigned upload preset → không cần API secret trong app
 */
class CloudinaryRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Upload ảnh từ Uri lên Cloudinary
     * @return URL ảnh đã upload thành công
     */
    suspend fun uploadImage(context: Context, imageUri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // Đọc bytes từ Uri
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw Exception("Không đọc được ảnh")
                val imageBytes = inputStream.readBytes()
                inputStream.close()

                // Tạo multipart request
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        "spocket_${System.currentTimeMillis()}.jpg",
                        imageBytes.toRequestBody("image/jpeg".toMediaType())
                    )
                    .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                    .addFormDataPart("folder", "Spocket")
                    .build()

                val request = Request.Builder()
                    .url(CloudinaryConfig.UPLOAD_URL)
                    .post(requestBody)
                    .build()

                // Gửi request
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("Upload thất bại: ${response.code}")
                }

                val responseBody = response.body?.string()
                    ?: throw Exception("Response rỗng")

                // Parse JSON lấy URL
                val json = JSONObject(responseBody)
                val secureUrl = json.getString("secure_url")

                Result.success(secureUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Upload ảnh từ byte array (dùng cho Bitmap đã compress)
     */
    suspend fun uploadImageBytes(imageBytes: ByteArray): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        "spocket_${System.currentTimeMillis()}.jpg",
                        imageBytes.toRequestBody("image/jpeg".toMediaType())
                    )
                    .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                    .addFormDataPart("folder", "Spocket")
                    .build()

                val request = Request.Builder()
                    .url(CloudinaryConfig.UPLOAD_URL)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("Upload thất bại: ${response.code}")
                }

                val json = JSONObject(response.body?.string() ?: "{}")
                Result.success(json.getString("secure_url"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
