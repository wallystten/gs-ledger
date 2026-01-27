package com.gsledger.app

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class ResumoActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var tvTotal: TextView
    private lateinit var tvDicas: TextView
    private lateinit var transacoes: JSONArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumo)

        tvTotal = findViewById(R.id.tvTotal)
        tvDicas = findViewById(R.id.tvDicas)
        listView = findViewById(R.id.listViewTransacoes)

        carregarLista()

        // 🗑️ Excluir ao segurar o item
        listView.setOnItemLongClickListener { _, _, position, _ ->
            AlertDialog.Builder(this)
                .setTitle("Excluir lançamento")
                .setMessage("Deseja remover este lançamento?")
                .setPositiveButton("Excluir") { _, _ ->
                    Storage.deleteTransaction(this, position)
                    carregarLista()
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }
    }

    private fun carregarLista() {
        transacoes = Storage.getTransactions(this)
        val lista = mutableListOf<String>()

        var totalEntradas = 0.0
        var totalSaidas = 0.0

        for (i in 0 until transacoes.length()) {
            val item = transacoes.getJSONObject(i)
            val descricao = item.getString("descricao")
            val valor = item.getString("valor").replace(",", ".").toDoubleOrNull() ?: 0.0
            val data = item.getString("data")
            val tipo = item.optString("tipo", "saida")

            if (tipo == "entrada") {
                totalEntradas += valor
                lista.add("📈 +R$ %.2f - %s\n%s".format(valor, descricao, data))
            } else {
                totalSaidas += valor
                lista.add("📉 -R$ %.2f - %s\n%s".format(valor, descricao, data))
            }
        }

        val saldo = totalEntradas - totalSaidas

        tvTotal.text = "Entradas: R$ %.2f\nSaídas: R$ %.2f\nSaldo: R$ %.2f"
            .format(totalEntradas, totalSaidas, saldo)

        // 💡 Gerar dicas inteligentes
        val dicas = FinancialAdvisor.gerarDicas(transacoes)
        tvDicas.text = dicas.joinToString("\n\n")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lista)
        listView.adapter = adapter
    }
}
