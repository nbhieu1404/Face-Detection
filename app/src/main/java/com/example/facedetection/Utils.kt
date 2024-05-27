package com.example.facedetection

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

// Kiểm tra quyền truy cập camera
fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

// Giải thích quyền truy cập camera
inline fun Context.cameraPermissionRequest(crossinline positive: () -> Unit) {
    AlertDialog.Builder(this)
        .setTitle("Camera permission required")
        .setMessage("Without accessing the camera it is not possible to SCAN QR codes...")
        .setPositiveButton("Allow Camera"){dialog, which ->
            positive.invoke()
        }
        .setNegativeButton("Cancel"){dialog, which ->

        }.show()
}
// Điều hướng sang cài đặt để cấp quyền
fun Context.openPermissionSetting(){
    Intent(ACTION_APPLICATION_DETAILS_SETTINGS).also {
        val uri: Uri = Uri.fromParts("package", packageName, null)
        it.data = uri
        startActivity(it)
    }
}