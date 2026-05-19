package com.example.dacs3_nguyencongduc.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * AI Scanner utility using Google ML Kit
 * Quét văn bản trên ảnh để tìm số tiền (được coi là con số lớn nhất trong hóa đơn)
 */
object BillScanner {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun scanBill(bitmap: Bitmap, onResult: (Double?) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        processImage(image, onResult)
    }

    fun scanBill(context: Context, uri: Uri, onResult: (Double?) -> Unit) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            processImage(image, onResult)
        } catch (e: Exception) {
            onResult(null)
        }
    }

    private fun processImage(image: InputImage, onResult: (Double?) -> Unit) {
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val amounts = mutableListOf<Double>()
                
                // Regex tìm các chuỗi số có dạng 100.000 hoặc 100,000
                val regex = Regex("""\d{1,3}([,.]\d{3})+""")
                
                // Thử tìm các số đơn lẻ nếu không thấy dạng chấm/phẩy
                val simpleRegex = Regex("""\d{4,9}""")

                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        val text = line.text.replace(" ", "")
                            .replace("đ", "")
                            .replace("VND", "", ignoreCase = true)
                            .replace("VNĐ", "", ignoreCase = true)

                        // 1. Thử tìm số có định dạng chấm/phẩy
                        val match = regex.find(text)
                        if (match != null) {
                            val cleanValue = match.value.replace(".", "").replace(",", "")
                            cleanValue.toDoubleOrNull()?.let { amounts.add(it) }
                        } else {
                            // 2. Thử tìm số đơn thuần (từ 4 chữ số trở lên - ví dụ 50000)
                            val simpleMatch = simpleRegex.find(text)
                            simpleMatch?.value?.toDoubleOrNull()?.let { amounts.add(it) }
                        }
                    }
                }
                
                // Thường thì số tiền tổng là con số lớn nhất được tìm thấy trên hóa đơn
                // Loại bỏ các con số quá lớn (ví dụ mã số thuế, số điện thoại)
                val filteredAmounts = amounts.filter { it in 1000.0..10000000.0 }
                val totalAmount = filteredAmounts.maxOrNull()
                
                onResult(totalAmount)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}
