package com.example.meusboletos

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meusboletos.databinding.ActivityMainBinding
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

class MainActivity : AppCompatActivity(), BoletoAdapterListener {

    private lateinit var binding: ActivityMainBinding
    private val listaDeBoletos = mutableListOf<Boleto>()
    private lateinit var adapter: BoletoAdapter
    private var positionParaEditar: Int = -1 // NOVO: Guarda a posição do item sendo editado

    // Launcher para Adicionar Boleto (código que já tínhamos)
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

    // NOVO LAUNCHER: Para receber o resultado da tela de Edição
    private val editBoletoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val boletoEditado = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getSerializableExtra("EXTRA_BOLEto_EDITADO", Boleto::class.java)
            } else {
                result.data?.getSerializableExtra("EXTRA_BOLETO_EDITADO") as Boleto?
            }

            if (boletoEditado != null && positionParaEditar != -1) {
                listaDeBoletos[positionParaEditar] = boletoEditado
                adapter.notifyItemChanged(positionParaEditar)
                Toast.makeText(this, "Boleto atualizado!", Toast.LENGTH_SHORT).show()
            }
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
        listaDeBoletos.add(
            Boleto(
                nome = "Conta de Luz",
                valor = "150,00",
                dataVencimento = "18/10/2025",
                recorrente = true
            )
        )
        listaDeBoletos.add(
            Boleto(
                nome = "Compra Online",
                valor = "350,00",
                dataVencimento = "25/10/2025",
                recorrente = false
            )
        )
        adapter.notifyDataSetChanged()
    }

    // --- FUNÇÕES DO CONTRATO (BoletoAdapterListener) ---

    // NOVA FUNÇÃO: Chamada quando o usuário clica no item da lista
    override fun onItemClick(position: Int) {
        positionParaEditar = position // Guarda a posição do item clicado
        val boletoParaEditar = listaDeBoletos[position]

        // Cria a intenção de abrir a tela de edição
        val intent = Intent(this, EditBoletoActivity::class.java)
        // Coloca o boleto inteiro dentro da intenção para a outra tela receber
        intent.putExtra("EXTRA_BOLETO", boletoParaEditar)

        // Lança a tela de edição esperando um resultado de volta
        editBoletoLauncher.launch(intent)
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

    override fun onPagoClick(position: Int, isChecked: Boolean) {
        if (!isChecked) {
            listaDeBoletos[position].pago = false
            return
        }

        val boleto = listaDeBoletos[position]
        boleto.pago = true

        if (boleto.recorrente) {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val dataAtual = LocalDate.parse(boleto.dataVencimento, formatter)
                val novaData = dataAtual.plusMonths(1)

                boleto.dataVencimento = novaData.format(formatter)
                boleto.pago = false
                adapter.notifyItemChanged(position)
                Toast.makeText(
                    this,
                    "'${boleto.nome}' pago! Próximo vencimento em ${boleto.dataVencimento}.",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: DateTimeParseException) {
                Toast.makeText(
                    this,
                    "Formato de data inválido. Use dd/MM/yyyy.",
                    Toast.LENGTH_SHORT
                ).show()
                boleto.pago = false
                adapter.notifyItemChanged(position)
            }
        } else {
            AlertDialog.Builder(this)
                .setTitle("Boleto Pago")
                .setMessage("Este boleto não é recorrente. Deseja removê-lo da lista?")
                .setPositiveButton("Sim, remover") { _, _ ->
                    // CORREÇÃO APLICADA AQUI
                    listaDeBoletos.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    Toast.makeText(this, "Boleto removido!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Não, manter na lista") { _, _ ->
                    boleto.pago = false
                    adapter.notifyItemChanged(position)
                }
                .setCancelable(false)
                .show()
        }
    }
}