package com.app.deviceadminowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class MainActivity : AppCompatActivity() {
    private lateinit var generateQrCode: Button
    private lateinit var startProvisioningButton: Button
    private lateinit var bitMap: ImageView
    private lateinit var qrCodeBitmap: Bitmap

    companion object {
        const val PROVISION_REQUEST_CODE = 1
    }

    private val qrCodeData = """
        {
          "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": "com.app.v/.DeviceAdminReceiver",
          "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": "https://uce47f1bbd0562809e9bcb3345ae.dl.dropboxusercontent.com/cd/0/get/CT40wGkdDLNayGcLx62xvXwOp59pRlJLLtzfD7PCJrE9JdV3ta3uxeokVYUFR2dzskdqaM33ItnQuJ3kGGnh5TP3kNo8FaM18lyJWLt7Iod6jnsa3lpjCZ_WBXnklZQrYN4wXXimoAXzZCgrQjTCnYYNxbaalE4WhyWT3NQ_BxekCQ/file#",
          "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM": "kqHrojY35Mbueogl9VVNn5pIPlTOCcABEYLizmH65U0=",
          "android.app.extra.PROVISIONING_SKIP_ENCRYPTION": true,
          "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED": true,
          "android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE": {}
        }
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        generateQrCode = findViewById(R.id.generateQrCode)
        startProvisioningButton = findViewById(R.id.startProvisioningButton)
        bitMap = findViewById(R.id.qrCodeImageView)

        generateQrCode.setOnClickListener {
            qrCodeBitmap = generateQrCodeBitmap(qrCodeData)
            bitMap.setImageBitmap(qrCodeBitmap)
        }
        startProvisioningButton.setOnClickListener {
            startProvisioning(qrCodeData)
        }

    }

    private fun generateQrCodeBitmap(qrCodeData: String): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(qrCodeData, BarcodeFormat.QR_CODE, 500, 500)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    private fun startProvisioning(qrCodeData: String) {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE)
        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
            ComponentName(this, MyDeviceOwnerReceiver::class.java))
        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME, packageName)
        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE, Bundle().apply {
                putString("PROVISIONING_DATA", qrCodeData) })

        startActivityForResult(intent, PROVISION_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROVISION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Device provisioning successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Device provisioning failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}