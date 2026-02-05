package com.gsledger.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.util.regex.Pattern

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val pacote = sbn.packageName.lowercase()

        // 🔎 Só processa notificações de apps bancários conhecidos
        if (!pacote.contains("santander") &&
            !pacote.contains("itau") &&
            !pacote.contains("bradesco") &&
            !pacote.contains("bb") &&
            !pacote.contains("caixa") &&
            !pacote.contains("inter") &&
            !pacote.contains("nubank") &&
            !pacote.contains("sicredi")
        ) return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

        val mensagemCompleta = "$title $text $bigText"

        Log.d("GS_LEDGER_NOTIF", "PACOTE: $pacote | MSG: $mensagemCompleta")

        val valor = extrairValor(mensagemCompleta) ?: return
        val tipo = detectarTipo(mensagemCompleta)

        // 🏷️ Define a origem baseada no app
        val origem = when {
            pacote.contains("santander") -> "Santander"
            pacote.contains("itau") -> "Itaú"
            pacote.contains("bradesco") -> "Bradesco"
            pacote.contains("bb") -> "Banco do Brasil"
            pacote.contains("caixa") -> "Caixa"
            pacote.contains("inter") -> "Banco Inter"
            pacote.contains("nubank") -> "Nubank"
            pacote.contains("sicredi") -> "Sicredi"
            else -> "Banco"
        }

        Storage.saveTransaction(
            applicationContext,
            "Movimentação bancária",
            valor,
            tipo,
            origem
        )

        Log.d("GS_LEDGER_NOTIF", "SALVO: R$ $valor | TIPO: $tipo | ORIGEM: $origem")
    }

    private fun extrairValor(texto: String): String? {
        val regex = Pattern.compile("""R\$\s?([0-9\.,]+)""")
        val matcher = regex.matcher(texto)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun detectarTipo(texto: String): String {
        val t = texto.lowercase()

        val palavrasSaida = listOf(
            "pix enviado", "você enviou", "pagamento realizado",
            "pagamento de", "compra no valor", "débito realizado",
            "debito realizado", "transferência enviada", "ted enviada",
            "você pagou", "pix pago",

            // 🆕 SANTANDER (cartão débito/crédito)
            "compra aprovada",
            "compra no cartão",
            "cartão final"
        )

        val palavrasEntrada = listOf(
            "recebeu um pix", "pix recebido", "valor creditado",
            "creditado em sua conta", "transferência recebida",
            "ted recebida", "depósito recebido", "deposito recebido",
            "você recebeu"
        )

        if (palavrasSaida.any { t.contains(it) }) return "saida"
        if (palavrasEntrada.any { t.contains(it) }) return "entrada"

        if (t.contains("pix") && !t.contains("enviado") && !t.contains("pagou"))
            return "entrada"

        return "saida"
    }
}
