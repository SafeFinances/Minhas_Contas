package com.example.meusboletos

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meusboletos.databinding.ActivityEditBoletoBinding
import java.util.Calendar

class EditBoletoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBoletoBinding
    private var boletoOriginal: Boleto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBoletoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boletoOriginal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("EXTRA_BOLETO", Boleto::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("EXTRA_BOLETO") as Boleto?
        }

        boletoOriginal?.let { boleto ->
            binding.editTextNome.setText(boleto.nome)
            binding.editTextData.setText(boleto.dataVencimento)
            binding.editTextAntecedencia.setText(boleto.diasAntecedenciaNotificacao.toString())
        }

        binding.buttonSalvar.setOnClickListener {
            salvarAlteracoes()
        }

        setupDatePicker()
    }

    private fun setupDatePicker() {
        binding.editTextData.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = "${String.format("%02d", dayOfMonth)}/${String.format("%02d", monthOfYear + 1)}/$year"
                binding.editTextData.setText(selectedDate)
            }, year, month, day)

            dpd.show()
        }
    }

    private fun salvarAlteracoes() {
        val nomeEditado = binding.editTextNome.text.toString()
        val dataEditada = binding.editTextData.text.toString()
        val antecedenciaEditada = binding.editTextAntecedencia.text.toString().toIntOrNull() ?: 1

        val boletoEditado = boletoOriginal?.copy(
            nome = nomeEditado,
            dataVencimento = dataEditada,
            diasAntecedenciaNotificacao = antecedenciaEditada
        )

        val resultIntent = Intent()
        resultIntent.putExtra("EXTRA_BOLETO_EDITADO", boletoEditado)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}