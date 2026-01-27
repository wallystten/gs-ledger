package com.gsledger.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.regex.Pattern

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        val mensagemCompleta = "$title $text"

        val valor = extrairValor(mensagemCompleta)
        val tipo = detectarTipo(mensagemCompleta)

        if (valor != null) {
            Storage.saveTransaction(
                applicationContext,
                "Lançamento automático",
                valor,
                tipo
            )
        }
    }

    private fun extrairValor(texto: String): String? {
        val regex = Pattern.compile("""R\$\s?([0-9]+[.,][0-9]{2})""")
        val matcher = regex.matcher(texto)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun detectarTipo(texto: String): String {
        val t = texto.lowercase()

        return when {
            t.contains("recebido") || t.contains("pix recebido") || t.contains("deposito") -> "entrada"
            t.contains("enviado") || t.contains("compra") || t.contains("pagamento") || t.contains("debito") -> "saida"
            else -> "saida" // padrão segurança
        }
    }
}
