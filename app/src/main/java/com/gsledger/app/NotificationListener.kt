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
                "Movimentação bancária",
                valor,
                tipo
            )
        }
    }

    private fun extrairValor(texto: String): String? {
        // Captura valores tipo: R$ 1.234,56 ou R$12,34
        val regex = Pattern.compile("""R\$\s?([0-9\.,]+)""")
        val matcher = regex.matcher(texto)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun detectarTipo(texto: String): String {
        val t = texto.lowercase()

        return when {
            // 🟢 ENTRADAS
            t.contains("recebido") ||
            t.contains("pix recebido") ||
            t.contains("valor creditado") ||
            t.contains("creditado") ||
            t.contains("transferência recebida") ||
            t.contains("ted recebida") ||
            t.contains("deposito") ||
            t.contains("depósito") -> "entrada"

            // 🔴 SAÍDAS
            t.contains("enviado") ||
            t.contains("pix enviado") ||
            t.contains("pagamento") ||
            t.contains("compra") ||
            t.contains("débito") ||
            t.contains("debito") ||
            t.contains("transferência enviada") ||
            t.contains("ted enviada") -> "saida"

            else -> "saida" // padrão segurança
        }
    }
}
