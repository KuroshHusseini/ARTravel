package com.example.artravel

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_take_image.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

private const val FILE_NAME = "photo.jpg"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File

class TakeImage : AppCompatActivity() {

    lateinit var currentPhotoPath: String

    private var bitmap: Bitmap? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 13
        private const val CAMERA_REQUEST_CODE = 12
        private const val STORAGE_PERMISSION_CODE = 3
        private const val REQUEST_IMAGE_CAPTURE = 82

        // SAVE THE GODDAMN IMAGE TO GALLERY !!!!
        private const val REQUEST_SAVE_AS = 22
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_image)

        if (imageView.drawable != null) {
            Log.d("DBG", "Is NOT null ${imageView.drawable.toString()}")
        } else {
            Log.d("DBG", "Is null")
        }

        takeImage_btn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }

        saveImage_btn.setOnClickListener {

            saveMediaToStorage(bitmap)


        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d("DBG", "OnRequestPermissionsResult")
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(this, "Permission denied for the camera", Toast.LENGTH_SHORT).show()

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("DBG", "requestCode $requestCode")

        when (requestCode) {
            CAMERA_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                    Log.d("DBG", "Save camera image to file")
                    bitmap = thumbnail
                    imageView.setImageBitmap(thumbnail)

//                    saveMediaToStorage(bitmap)
                }
        }
    }

    private fun saveMediaToStorage(bitmap: Bitmap?) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        Log.d("DBG", "saveMediaToStorage")
        Log.d("DBG", "filename $filename")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }

                Log.d("DBG", "imageUri $imageUri")
                Log.d("DBG", "fos $fos")
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

            val image = File(imagesDir, filename)

            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Saved to photos", Toast.LENGTH_SHORT).show()
        }
    }
}