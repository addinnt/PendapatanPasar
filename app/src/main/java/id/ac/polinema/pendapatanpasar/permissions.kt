package id.ac.polinema.pendapatanpasar

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File

const val REQUEST_ACCESS_CAMERA_FOR_BUKTI_SETOR = 11
const val REQUEST_ACCESS_GALERY_FOR_BUKTI_SETOR = 21
const val REQUEST_ACCESS_GALERY_FOR_STS = 22
const val REQUEST_ACCESS_CAMERA_FOR_STS = 12

fun Context.requestStoragePermission(activity: Activity, callback: ()-> Unit) {
    Dexter.withContext(activity)
        .withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                // check if all permissions are granted
                if (report.areAllPermissionsGranted()) {
                    callback()
                }

                // check for permanent denial of any permission
                if (report.isAnyPermissionPermanentlyDenied) {
                    // show alert dialog navigating to Settings
                    showSetting()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: List<PermissionRequest>,
                token: PermissionToken
            ) {
                token.continuePermissionRequest()
            }
        })
        .withErrorListener {
            Toast.makeText(this, "Error occurred ! ", Toast.LENGTH_SHORT)
                .show()
        }
        .onSameThread()
        .check()
}

private fun Context.showSetting() {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Need Permissions")
    builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
    builder.setPositiveButton("GOTO SETTINGS") { dialog, _ ->
        dialog.cancel()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }

        (this as Activity).startActivityForResult(intent, 101)
    }

    builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
    builder.show()
}

fun Activity.accessCamera(requestCode: Int): Uri {
    val file = File(
        getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "${ System.currentTimeMillis() } _capturedImg.jpg"
    )

    val stringUri = "${BuildConfig.APPLICATION_ID}.fileprovider"
    val uri = FileProvider.getUriForFile(this, stringUri, file);
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        putExtra("return-data", true)
        putExtra(MediaStore.EXTRA_OUTPUT, uri)
    }

    startActivityForResult(intent, requestCode)

    return uri
}

fun Activity.accessGalery(requestCode: Int) {
    val intent = Intent().apply {
        type = "image/*"
        action = Intent.ACTION_GET_CONTENT
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    startActivityForResult(intent, requestCode)
}