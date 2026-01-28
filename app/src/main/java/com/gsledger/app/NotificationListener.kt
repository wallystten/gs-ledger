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

        // Junta tudo porque alguns bancos colocam o valor no bigText
        val mensagemCompleta = "$title $text $bigText"

        Log.d("GS_LEDGER_NOTIF", "PACOTE: $pacote | MSG: $mensagemCompleta")

        val valor = extrairValor(mensagemCompleta)
        val tipo = detectarTipo(mensagemCompleta)
        val banco = detectarBanco(pacote)

        if (valor != null) {
            Storage.saveTransaction(
                applicationContext,
                "Movimentação bancária",
                valor,
                tipo,
                banco // 🆕 AGORA SALVA A ORIGEM
            )
            Log.d("GS_LEDGER_NOTIF", "SALVO: R$ $valor | TIPO: $tipo | BANCO: $banco")
        }
    }

    private fun detectarBanco(pacote: String): String {
        return when {
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
    }

    private fun extrairValor(texto: String): String? {
        // Aceita formatos: R$ 900,00 | R$900,00 | R$ 1.234,56
        val regex = Pattern.compile("""R\$\s?([0-9\.,]+)""")
        val matcher = regex.matcher(texto)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun detectarTipo(texto: String): String {
        val t = texto.lowercase()

        // 🔴 SAÍDAS — checamos primeiro
        val palavrasSaida = listOf(
            "pix enviado",
            "seu pix foi enviado",
            "você enviou",
            "pagamento realizado",
            "pagamento de",
            "compra no valor",
            "débito realizado",
            "debito realizado",
            "transferência enviada",
            "ted enviada",
            "você pagou",
            "pagou um pix",
            "pix pago"
        )

        // 🟢 ENTRADAS
        val palavrasEntrada = listOf(
            "recebeu um pix",
            "pix recebido",
            "valor creditado",
            "creditado em sua conta",
            "transferência recebida",
            "ted recebida",
            "depósito recebido",
            "deposito recebido",
            "você recebeu"
        )

        if (palavrasSaida.any { t.contains(it) }) return "saida"
        if (palavrasEntrada.any { t.contains(it) }) return "entrada"

        // Se mencionar PIX mas não disser que foi enviado → assumimos entrada
        if (t.contains("pix") && !t.contains("enviado") && !t.contains("pagou"))
            return "entrada"

        return "saida"
    }
}
