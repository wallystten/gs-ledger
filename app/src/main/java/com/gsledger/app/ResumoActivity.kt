package com.gsledger.app

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import org.json.JSONArray
import java.text.NumberFormat
import java.util.*

class ResumoActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var tvTotal: TextView
    private lateinit var tvDicas: TextView
    private lateinit var pieChart: PieChart
    private lateinit var transacoes: JSONArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumo)

        tvTotal = findViewById(R.id.tvTotal)
        tvDicas = findViewById(R.id.tvDicas)
        listView = findViewById(R.id.listViewTransacoes)
        pieChart = findViewById(R.id.pieChart)

        carregarLista()

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

        val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        for (i in 0 until transacoes.length()) {
            val item = transacoes.getJSONObject(i)

            val descricao = item.optString("descricao", "Sem descrição")
            val valor = item.optString("valor", "0").replace(",", ".").toDoubleOrNull() ?: 0.0
            val data = item.optString("data", "")
            val tipo = item.optString("tipo", "saida")
            val origem = item.optString("origem", "Manual")

            val valorFormatado = formatadorMoeda.format(valor)

            val linha = if (tipo == "entrada") {
                totalEntradas += valor
                "📈 ENTRADA • $valorFormatado\nOrigem: $origem\nDescrição: $descricao\n$data"
            } else {
                totalSaidas += valor
                "📉 SAÍDA • $valorFormatado\nOrigem: $origem\nDescrição: $descricao\n$data"
            }

            lista.add(linha)
        }

        val saldo = totalEntradas - totalSaidas

        tvTotal.text = "Entradas: ${formatadorMoeda.format(totalEntradas)}\n" +
                "Saídas: ${formatadorMoeda.format(totalSaidas)}\n" +
                "Saldo: ${formatadorMoeda.format(saldo)}"

        val dicas = FinancialAdvisor.gerarDicas(transacoes)
        tvDicas.text = dicas.joinToString("\n\n")

        mostrarGrafico(totalEntradas, totalSaidas)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lista)
        listView.adapter = adapter
    }

    private fun mostrarGrafico(entradas: Double, saidas: Double) {
        val entries = ArrayList<PieEntry>()

        if (entradas > 0) entries.add(PieEntry(entradas.toFloat(), "Entradas"))
        if (saidas > 0) entries.add(PieEntry(saidas.toFloat(), "Saídas"))

        val dataSet = PieDataSet(entries, "Resumo Financeiro")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Fluxo de Caixa"
        pieChart.animateY(1000)
        pieChart.invalidate()
    }
}
