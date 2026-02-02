package com.gsledger.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {

    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔥 FIREBASE
        FirebaseApp.initializeApp(this)
        val analytics = FirebaseAnalytics.getInstance(this)
        analytics.logEvent("app_aberto", null)
        Log.d("FIREBASE_TESTE", "Firebase conectado com sucesso!")

        // 💰 INICIALIZA ADMOB
        MobileAds.initialize(this)

        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        val btnAdicionar = findViewById<Button>(R.id.btnAdicionar)
        val btnVerResumo = findViewById<Button>(R.id.btnVerResumo)
        val btnEscanearQR = findViewById<Button>(R.id.btnEscanearQR)
        val btnAtivarNotif = findViewById<Button>(R.id.btnAtivarNotif)
        val btnSobre = findViewById<Button>(R.id.btnSobre)

        btnAdicionar.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        btnVerResumo.setOnClickListener {
            startActivity(Intent(this, ResumoActivity::class.java))
        }

        btnEscanearQR.setOnClickListener {
            startActivity(Intent(this, QrScannerActivity::class.java))
        }

        btnAtivarNotif.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Permissão de Notificações")
                .setMessage(
                    "Para registrar automaticamente movimentações bancárias, o GS Ledger precisa de acesso às notificações.\n\n" +
                    "Esses dados são utilizados apenas no seu aparelho para organizar suas entradas e saídas financeiras.\n\n" +
                    "O aplicativo não compartilha suas informações com terceiros."
                )
                .setPositiveButton("Continuar") { _, _ ->
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnSobre.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sobre o GS Ledger")
                .setMessage(
                    "O GS Ledger é um aplicativo de controle financeiro simples, criado para ajudar você a organizar suas entradas e saídas de dinheiro.\n\n" +
                    "🔹 Seus dados ficam armazenados somente no seu aparelho.\n" +
                    "🔹 Não enviamos suas informações bancárias para servidores.\n" +
                    "🔹 A leitura de notificações é utilizada exclusivamente para registrar automaticamente suas movimentações financeiras.\n\n" +
                    "Versão 1.0\n" +
                    "Desenvolvido por Wallystten"
                )
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
