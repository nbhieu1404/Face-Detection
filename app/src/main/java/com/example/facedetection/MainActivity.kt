package com.example.facedetection

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.facedetection.databinding.ActivityMainBinding
import com.google.mlkit.vision.barcode.common.Barcode

class MainActivity : AppCompatActivity() {
    // Quyền sử dụng camera
    private val cameraPermission = android.Manifest.permission.CAMERA

    // ViewBinding
    private lateinit var binding: ActivityMainBinding


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startScanner()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button.setOnClickListener {
            requestCameraAndStartScanner()
        }
    }

    // Kiểm tra và yêu cầu quyền sử dụng camera
    private fun requestCameraAndStartScanner() {
        if (isPermissionGranted(cameraPermission)) {
            startScanner()
        } else {
            requestCameraPermission()
        }
    }

    // Bắt đầu sử dụng camera
    private fun startScanner() {
        ScannerActivity.startScanner(this) { barcodes ->
            barcodes.forEach { barcode ->
                when (barcode.valueType) {
                    Barcode.TYPE_URL -> {
                        binding.txtURL.text = "URL"
                        binding.txtContent.text = barcode.url?.url
                    }

                    Barcode.TYPE_CONTACT_INFO -> {
                        binding.txtURL.text = "Contact"
                        binding.txtContent.text = barcode.contactInfo?.name?.formattedName
                    }

                    else -> {
                        binding.txtURL.text = "Other"
                        binding.txtContent.text = barcode.rawValue
                    }
                }
            }
        }
    }

    // Yêu cầu quyền sử dụng camera
    private fun requestCameraPermission() {
        when {
            // Nếu người dùng đã từ chối thì hiển thị giải thích và điều hướng sang cài đặt
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                // Giải thích
                cameraPermissionRequest {
                    // Điều hướng sang cài đặt
                    openPermissionSetting()
                }
            }

            else -> {
                // Yêu cầu quyền
                requestPermissionLauncher.launch((cameraPermission))
            }
        }
    }
}