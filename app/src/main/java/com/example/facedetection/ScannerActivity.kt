package com.example.facedetection

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.facedetection.databinding.ActivityScannerBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannerBinding

    // Chỉ định camera trước hoặc sau
    private lateinit var cameraSelector: CameraSelector

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    // Lấy ProcessCameraProvider một cách không đồng bộ
    private lateinit var processCameraProvider: ProcessCameraProvider

    // Quản lý và hiển thị bản xem trước của camera.
    private lateinit var cameraPreview: Preview

    // Xử lý các hình ảnh từ camera theo thời gian thực
    private lateinit var imageAnalysis: ImageAnalysis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Chuyển sang chế độ fullscreen
        cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

//        binding.bottom.setOnClickListener {
//            cameraSelector = if (cameraSelector.lensFacing == CameraSelector.LENS_FACING_BACK) {
//                // Nếu đang sử dụng camera sau, chuyển sang camera trước
//                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
//            } else {
//                // Nếu đang sử dụng camera trước, chuyển sang camera sau
//                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
//            }
//
//            // Ràng buộc lại camera với lifecycle
//            processCameraProvider.unbindAll()
//            bindCameraPreview()
//            bindInputAnalyser()
//
//        }
        // Khởi tạo cameraProviderFuture để lấy ProcessCameraProvider
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        // Đăng ký sự kiện để xử lý khi ProcessCameraProvider sẵn sàng
        cameraProviderFuture.addListener(
            {
                // Lấy ProcessCameraProvider khi nó đã sẵn sàng
                processCameraProvider = cameraProviderFuture.get()

                // Liên kết và hiển thị bản xem trước của camera.
                bindCameraPreview()
                // Liên kết và xử lý các hình ảnh từ camera
                bindInputAnalyser()

            }, ContextCompat.getMainExecutor(this)
        )
    }

    // Xử lý hình ảnh từ camera
    private fun bindInputAnalyser() {

        // tùy chọn để quét mã QR.
        val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        // xử lý các khung hình từ camera
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()

        // quản lý một hàng đợi các tác vụ và đảm bảo rằng các tác vụ được thực thi tuần tự trong một luồng duy nhất.
        val cameraExecutor = Executors.newSingleThreadExecutor()

        // thiết lập một analyzer để xử lý các khung hình camera.
        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(barcodeScanner, imageProxy)
        }
        processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        // Lấy góc xoay của hình ảnh để đảm bảo rằng hình ảnh được quét với hướng đúng.
        val inputImage =
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    onScan?.invoke(barcodes)
                    onScan = null
                    finish()
                }
            }
            // Được gọi khi có lỗi xảy ra trong quá trình xử lý.
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed to scan barcode", Toast.LENGTH_SHORT).show()
            }

            // Được gọi khi quá trình xử lý hoàn tất, bất kể thành công hay thất bại.
            .addOnCompleteListener {
                //  Đóng ImageProxy để giải phóng tài nguyên
                imageProxy.close()
            }
    }

    private fun bindCameraPreview() {
        // Đặt TargetRotation theo độ xoay của previewView
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()
        // Thiết lập nhà cung cấp bề mặt (surface provider) cho cameraPreview
        cameraPreview.setSurfaceProvider(binding.previewView.surfaceProvider)

        // Ràng buộc vòng đời của cameraPreview với vòng đời của ScannerActivity
        processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
    }

    companion object {
        private var onScan: ((barcodes: List<Barcode>) -> Unit)? = null
        fun startScanner(context: Context, onScan: (barcodes: List<Barcode>) -> Unit) {
            this.onScan = onScan
            Intent(context, ScannerActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }
}