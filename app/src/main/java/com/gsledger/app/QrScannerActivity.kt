package com.gsledger.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrScannerActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val previewView = PreviewView(this)
        setContentView(previewView)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            startCamera(previewView)
        }
    }

    private fun startCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, analyzer)

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue ?: continue

                    runOnUiThread {
                        abrirTelaLancamento(rawValue)
                    }
                    break
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun abrirTelaLancamento(codigoQr: String) {
        val intent = Intent(this, AddTransactionActivity::class.java)

        // 🔵 PIX
        val valorPix = extrairValorPix(codigoQr)
        if (valorPix != null) {
            intent.putExtra("qrValue", valorPix)
            intent.putExtra("tipoAuto", "entrada")
            startActivity(intent)
            finish()
            return
        }

        // 🧾 NFC-e (nota fiscal)
        if (codigoQr.contains("nfce") || codigoQr.contains("fazenda") || codigoQr.contains("nfe")) {
            buscarValorNfce(codigoQr)
            return
        }

        // ❓ Outro QR
        intent.putExtra("qrValue", "")
        startActivity(intent)
        finish()
    }

    /**
     * 🔍 Extrai valor do QR Pix padrão EMV
     * Campo 54 = valor da transação
     */
    private fun extrairValorPix(codigo: String): String? {
        return try {
            var i = 0
            while (i < codigo.length - 4) {
                val id = codigo.substring(i, i + 2)
                val tamanho = codigo.substring(i + 2, i + 4).toIntOrNull() ?: return null
                val valor = codigo.substring(i + 4, i + 4 + tamanho)

                if (id == "54") {
                    return valor.replace(".", ",")
                }

                i += 4 + tamanho
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 🌐 Busca valor da NFC-e direto no site da SEFAZ
     */
    private fun buscarValorNfce(url: String) {
        Thread {
            try {
                val html = java.net.URL(url).readText()

                val regex = Regex("""Valor\s+Total.*?R\$\s?([0-9\.,]+)""", RegexOption.IGNORE_CASE)
                val match = regex.find(html)
                val valor = match?.groups?.get(1)?.value ?: ""

                runOnUiThread {
                    val intent = Intent(this, AddTransactionActivity::class.java)
                    intent.putExtra("qrValue", valor)
                    intent.putExtra("descricaoAuto", "Compra via NFC-e")
                    intent.putExtra("tipoAuto", "saida")
                    startActivity(intent)
                    finish()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    val intent = Intent(this, AddTransactionActivity::class.java)
                    intent.putExtra("qrValue", "")
                    intent.putExtra("descricaoAuto", "Compra via NFC-e")
                    intent.putExtra("tipoAuto", "saida")
                    startActivity(intent)
                    finish()
                }
            }
        }.start()
    }
}
