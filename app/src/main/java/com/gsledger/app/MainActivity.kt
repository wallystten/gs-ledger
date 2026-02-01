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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔥 INICIALIZA FIREBASE
        FirebaseApp.initializeApp(this)

        val analytics = FirebaseAnalytics.getInstance(this)
        analytics.logEvent("app_aberto", null)

        Log.d("FIREBASE_TESTE", "Firebase conectado com sucesso!")

        val btnAdicionar = findViewById<Button>(R.id.btnAdicionar)
        val btnVerResumo = findViewById<Button>(R.id.btnVerResumo)
        val btnEscanearQR = findViewById<Button>(R.id.btnEscanearQR)
        val btnAtivarNotif = findViewById<Button>(R.id.btnAtivarNotif)
        val btnSobre = findViewById<Button>(R.id.btnSobre) // 🆕 NOVO BOTÃO

        // ➕ Adicionar lançamento manual
        btnAdicionar.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        // 📊 Ver resumo financeiro
        btnVerResumo.setOnClickListener {
            startActivity(Intent(this, ResumoActivity::class.java))
        }

        // 📷 Abrir leitor de QR Code
        btnEscanearQR.setOnClickListener {
            startActivity(Intent(this, QrScannerActivity::class.java))
        }

        // 🔔 Ativar leitura de notificações (COM AVISO EXPLICATIVO)
        btnAtivarNotif.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Permissão de Notificações")
                .setMessage(
                    "Para registrar automaticamente movimentações bancárias, o GS Ledger precisa de acesso às notificações.\n\n" +
                    "Esses dados são usados apenas no seu aparelho para organizar suas entradas e saídas financeiras.\n\n" +
                    "O aplicativo não compartilha suas informações com terceiros."
                )
                .setPositiveButton("Continuar") { _, _ ->
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // ℹ️ SOBRE E PRIVACIDADE
        btnSobre.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sobre o GS Ledger")
                .setMessage(
                    "O GS Ledger é um aplicativo de controle financeiro pessoal.\n\n" +
                    "🔹 Seus dados ficam somente no seu aparelho.\n" +
                    "🔹 Não enviamos suas informações bancárias para servidores.\n" +
                    "🔹 A leitura de notificações serve apenas para registrar automaticamente suas movimentações.\n\n" +
                    "Versão 1.0"
                )
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
