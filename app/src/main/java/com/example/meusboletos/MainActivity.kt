package com.example.meusboletos

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.meusboletos.databinding.ActivityMainBinding
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import org.threeten.bp.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), BoletoAdapterListener {

    private lateinit var binding: ActivityMainBinding
    private val listaDeBoletos = mutableListOf<Boleto>()
    private lateinit var adapter: BoletoAdapter
    private var positionParaEditar: Int = -1

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permissão de notificação concedida!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissão de notificação negada.", Toast.LENGTH_SHORT).show()
            }
        }

    private val addBoletoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            val nome = data?.getStringExtra("EXTRA_NOME") ?: ""
            val dataVencimento = data?.getStringExtra("EXTRA_DATA") ?: ""
            val recorrente = data?.getBooleanExtra("EXTRA_RECORRENTE", false) ?: false
            val antecedencia = data?.getIntExtra("EXTRA_ANTECEDENCIA", 1) ?: 1

            val novoBoleto = Boleto(
                nome = nome,
                dataVencimento = dataVencimento,
                recorrente = recorrente,
                diasAntecedenciaNotificacao = antecedencia
            )
            listaDeBoletos.add(novoBoleto)
            adapter.notifyItemInserted(listaDeBoletos.size - 1)
            agendarNotificacao(novoBoleto)
        }
    }

    private val editBoletoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val boletoEditado = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getSerializableExtra("EXTRA_BOLETO_EDITADO", Boleto::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getSerializableExtra("EXTRA_BOLETO_EDITADO") as Boleto?
            }

            if (boletoEditado != null && positionParaEditar != -1) {
                cancelarNotificacao(listaDeBoletos[positionParaEditar])
                listaDeBoletos[positionParaEditar] = boletoEditado
                adapter.notifyItemChanged(positionParaEditar)
                agendarNotificacao(boletoEditado)
                Toast.makeText(this, "Boleto atualizado!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- NOVO: Inicializa o SDK de Anúncios ---
        com.google.android.gms.ads.MobileAds.initialize(this) {}

        // --- NOVO: Carrega o banner ---
        val adView = binding.adView // Acessa o AdView do layout
        val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // O resto do seu código onCreate continua igual...
        setSupportActionBar(binding.toolbar)

        adapter = BoletoAdapter(listaDeBoletos, this)
        binding.contentMain.recyclerViewBoletos.adapter = adapter
        binding.contentMain.recyclerViewBoletos.layoutManager = LinearLayoutManager(this)

        adicionarDadosIniciais()
        pedirPermissaoDeNotificacao()

        binding.fab.setOnClickListener {
            val intent = Intent(this, AddBoletoActivity::class.java) // Corrigido para AddBoletoActivity
            addBoletoLauncher.launch(intent)
        }

    }

    private fun adicionarDadosIniciais() {
        val boleto1 = Boleto(nome = "Conta de Luz", dataVencimento = "18/10/2025", recorrente = true, diasAntecedenciaNotificacao = 2)
        val boleto2 = Boleto(nome = "Assinatura Streaming", dataVencimento = "25/10/2025", recorrente = false)
        listaDeBoletos.add(boleto1)
        listaDeBoletos.add(boleto2)
        adapter.notifyDataSetChanged()
        listaDeBoletos.forEach { agendarNotificacao(it) }
    }

    private fun pedirPermissaoDeNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun agendarNotificacao(boleto: Boleto, isDebug: Boolean = false) {
        if (boleto.pago) return

        val workManager = WorkManager.getInstance(this)

        val dataVencimento = try {
            org.threeten.bp.LocalDate.parse(boleto.dataVencimento, org.threeten.bp.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: Exception) {
            return
        }

        val dataNotificacao = dataVencimento.minusDays(boleto.diasAntecedenciaNotificacao.toLong())
        val hoje = org.threeten.bp.LocalDate.now()

        if (dataNotificacao.isBefore(hoje) && !isDebug) return

        val delay = if (isDebug) {
            10
        } else {
            org.threeten.bp.temporal.ChronoUnit.SECONDS.between(hoje.atStartOfDay(), dataNotificacao.atStartOfDay())
        }

        if (delay < 0 && !isDebug) return

        val data = Data.Builder()
            .putString("NOME_BOLETO", boleto.nome)
            .putString("DATA_VENCIMENTO", boleto.dataVencimento)
            .build()

        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.SECONDS)
            .setInputData(data)
            .addTag(boleto.id.toString())
            .build()

        workManager.enqueue(notificationWorkRequest)
    }

    private fun cancelarNotificacao(boleto: Boleto) {
        WorkManager.getInstance(this).cancelAllWorkByTag(boleto.id.toString())
    }

    override fun onItemClick(position: Int) {
        positionParaEditar = position
        val boletoParaEditar = listaDeBoletos[position]
        val intent = Intent(this, EditBoletoActivity::class.java)
        intent.putExtra("EXTRA_BOLETO", boletoParaEditar)
        editBoletoLauncher.launch(intent)
    }

    override fun onExcluirClick(position: Int) {
        if (position >= listaDeBoletos.size) return
        val boletoParaExcluir = listaDeBoletos[position]

        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir o boleto '${boletoParaExcluir.nome}'?")
            .setPositiveButton("Sim") { _, _ ->
                cancelarNotificacao(boletoParaExcluir)
                listaDeBoletos.removeAt(position)
                adapter.notifyItemRemoved(position)
                Toast.makeText(this, "Boleto excluído!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    override fun onPagoClick(position: Int, isChecked: Boolean) {
        if (position >= listaDeBoletos.size) return

        if (!isChecked) {
            listaDeBoletos[position].pago = false
            agendarNotificacao(listaDeBoletos[position])
            return
        }

        val boleto = listaDeBoletos[position]
        boleto.pago = true
        cancelarNotificacao(boleto)

        if (boleto.recorrente) {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val dataAtual = LocalDate.parse(boleto.dataVencimento, formatter)
                val novaData = dataAtual.plusMonths(1)

                boleto.dataVencimento = novaData.format(formatter)
                boleto.pago = false
                adapter.notifyItemChanged(position)
                agendarNotificacao(boleto)
                Toast.makeText(this, "'${boleto.nome}' pago! Próximo vencimento em ${boleto.dataVencimento}.", Toast.LENGTH_LONG).show()

            } catch (e: DateTimeParseException) {
                Toast.makeText(this, "Formato de data inválido. Use dd/MM/yyyy.", Toast.LENGTH_SHORT).show()
                boleto.pago = false
                adapter.notifyItemChanged(position)
            }
        } else {
            AlertDialog.Builder(this)
                .setTitle("Boleto Pago")
                .setMessage("Este boleto não é recorrente. Deseja removê-lo da lista?")
                .setPositiveButton("Sim, remover") { _, _ ->
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