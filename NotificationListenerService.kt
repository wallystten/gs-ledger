package com.gsledger.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("GSLedger", "Serviço de notificações conectado")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val packageName = sbn.packageName
        val extras = sbn.notification.extras

        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        // Log básico (por enquanto)
        Log.d(
            "GSLedger",
            "Notificação recebida de $packageName | Título: $title | Texto: $text"
        )

        /*
         * FUTURO:
         * Aqui vamos:
         * - Identificar se é banco
         * - Detectar entrada ou saída
         * - Extrair valor
         * - Salvar no banco local
         */
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        // Não precisamos tratar remoção por enquanto
    }
}
