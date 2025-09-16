package com.example.meusboletos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meusboletos.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// MainActivity agora "assina o contrato" (implementa a interface)
class MainActivity : AppCompatActivity(), BoletoAdapterListener {

    private lateinit var binding: ActivityMainBinding
    private val listaDeBoletos = mutableListOf<Boleto>()
    private lateinit var adapter: BoletoAdapter

    private val addBoletoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            val nome = data?.getStringExtra("EXTRA_NOME") ?: ""
            val valor = data?.getStringExtra("EXTRA_VALOR") ?: ""
            val dataVencimento = data?.getStringExtra("EXTRA_DATA") ?: ""
            val recorrente = data?.getBooleanExtra("EXTRA_RECORRENTE", false) ?: false

            val novoBoleto = Boleto(
                nome = nome,
                valor = valor,
                dataVencimento = dataVencimento,
                recorrente = recorrente
            )
            listaDeBoletos.add(novoBoleto)
            adapter.notifyItemInserted(listaDeBoletos.size - 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = BoletoAdapter(listaDeBoletos, this)
        binding.contentMain.recyclerViewBoletos.adapter = adapter
        binding.contentMain.recyclerViewBoletos.layoutManager = LinearLayoutManager(this)

        adicionarDadosIniciais()

        binding.fab.setOnClickListener {
            val intent = Intent(this, AddBoletoActivity::class.java)
            addBoletoLauncher.launch(intent)
        }
    }

    private fun adicionarDadosIniciais() {
        // Boleto recorrente
        listaDeBoletos.add(Boleto(nome = "Conta de Luz", valor = "R$ 150,00", dataVencimento = "18/10/2025", recorrente = true))
        // Boleto não recorrente
        listaDeBoletos.add(Boleto(nome = "Compra Online", valor = "R$ 350,00", dataVencimento = "25/10/2025", recorrente = false))
        adapter.notifyDataSetChanged()
    }

    override fun onExcluirClick(position: Int) {
        val boletoParaExcluir = listaDeBoletos[position]

        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir o boleto '${boletoParaExcluir.nome}'?")
            .setPositiveButton("Sim") { _, _ ->
                listaDeBoletos.removeAt(position)
                adapter.notifyItemRemoved(position)
                Toast.makeText(this, "Boleto excluído!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    // A MÁGICA ACONTECE AQUI!
    override fun onPagoClick(position: Int, isChecked: Boolean) {
        // Só executa a lógica se o usuário MARCOU como pago.
        if (!isChecked) {
            listaDeBoletos[position].pago = false
            return
        }

        val boleto = listaDeBoletos[position]
        boleto.pago = true

        if (boleto.recorrente) {
            // LÓGICA PARA BOLETO RECORRENTE
            try {
                // Converte o texto da data para um objeto de data real
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val dataAtual = LocalDate.parse(boleto.dataVencimento, formatter)

                // Adiciona um mês
                val novaData = dataAtual.plusMonths(1)

                // Atualiza o boleto com a nova data e desmarca como pago
                boleto.dataVencimento = novaData.format(formatter)
                boleto.pago = false // Prepara para o próximo mês

                // Notifica o adapter que este item mudou, para redesenhar na tela
                adapter.notifyItemChanged(position)
                Toast.makeText(this, "'${boleto.nome}' pago! Próximo vencimento em ${boleto.dataVencimento}.", Toast.LENGTH_LONG).show()

            } catch (e: DateTimeParseException) {
                // Se o formato da data estiver errado, mostra um erro
                Toast.makeText(this, "Formato de data inválido. Use dd/MM/yyyy.", Toast.LENGTH_SHORT).show()
                boleto.pago = false // Desfaz a marcação
                adapter.notifyItemChanged(position)
            }

        } else {
            // LÓGICA PARA BOLETO NÃO RECORRENTE
            // Mostra um pop-up perguntando se quer apagar o boleto pago.
            AlertDialog.Builder(this)
                .setTitle("Boleto Pago")
                .setMessage("Este boleto não é recorrente. Deseja removê-lo da lista?")
                .setPositiveButton("Sim, remover") { _, _ ->
                    // Reutiliza a função de excluir que já criamos!
                    onExcluirClick(position)
                }
                .setNegativeButton("Não, manter na lista") { _, _ ->
                    // Não faz nada, apenas deixa o boleto marcado como pago.
                }
                .show()
        }
    }
}