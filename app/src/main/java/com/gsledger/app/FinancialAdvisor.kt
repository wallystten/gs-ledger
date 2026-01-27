package com.gsledger.app

import org.json.JSONArray
import java.util.*
import kotlin.math.roundToInt

object FinancialAdvisor {

    fun gerarDicas(transacoes: JSONArray): List<String> {
        val dicas = mutableListOf<String>()

        if (transacoes.length() == 0) {
            dicas.add("Adicione lançamentos para receber análises financeiras.")
            return dicas
        }

        var totalEntradas = 0.0
        var totalSaidas = 0.0

        val hoje = Calendar.getInstance()
        val diaAtual = hoje.get(Calendar.DAY_OF_MONTH)
        val diasNoMes = hoje.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 0 until transacoes.length()) {
            val item = transacoes.getJSONObject(i)
            val valor = item.getString("valor").replace(",", ".").toDoubleOrNull() ?: 0.0
            val tipo = item.optString("tipo", "saida")

            if (tipo == "entrada") totalEntradas += valor else totalSaidas += valor
        }

        val saldoAtual = totalEntradas - totalSaidas

        // 🔮 Previsão até o fim do mês
        val mediaGastoDiario = if (diaAtual > 0) totalSaidas / diaAtual else 0.0
        val previsaoGastos = mediaGastoDiario * diasNoMes
        val saldoPrevisto = totalEntradas - previsaoGastos

        if (saldoPrevisto < 0) {
            dicas.add("⚠️ No ritmo atual, você pode terminar o mês no negativo.")
        } else {
            dicas.add("👍 Mantendo o ritmo atual, seu saldo deve fechar positivo.")
        }

        // 📉 Alerta de despesas maiores que renda
        if (totalSaidas > totalEntradas) {
            dicas.add("🚨 Suas despesas já ultrapassaram suas entradas este mês.")
        }

        // 🎯 Meta de gasto diário
        val diasRestantes = diasNoMes - diaAtual
        if (diasRestantes > 0) {
            val limiteDiario = if (saldoAtual > 0) saldoAtual / diasRestantes else 0.0
            dicas.add("💡 Para manter o saldo positivo, tente gastar no máximo R$ %.2f por dia."
                .format(limiteDiario))
        }

        // 🔁 Muitos gastos pequenos
        var pequenosGastos = 0
        for (i in 0 until transacoes.length()) {
            val item = transacoes.getJSONObject(i)
            val valor = item.getString("valor").replace(",", ".").toDoubleOrNull() ?: 0.0
            if (valor < 20) pequenosGastos++
        }
        if (pequenosGastos >= 5) {
            dicas.add("🧾 Muitos gastos pequenos detectados. Eles podem somar um valor alto no fim do mês.")
        }

        return dicas
    }
}
