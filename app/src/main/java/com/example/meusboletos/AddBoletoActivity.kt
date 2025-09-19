package com.example.meusboletos

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.meusboletos.databinding.ActivityAddBoletoBinding
import java.util.Calendar

class AddBoletoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBoletoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBoletoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSalvar.setOnClickListener {
            salvarBoleto()
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

    private fun salvarBoleto() {
        val nome = binding.editTextNome.text.toString()
        val data = binding.editTextData.text.toString()
        val recorrente = binding.switchRecorrente.isChecked
        val antecedencia = binding.editTextAntecedencia.text.toString().toIntOrNull() ?: 1

        if (nome.isBlank() || data.isBlank()) {
            Toast.makeText(this, "Preencha o nome e a data!", Toast.LENGTH_SHORT).show()
            return
        }

        val resultIntent = Intent()
        resultIntent.putExtra("EXTRA_NOME", nome)
        resultIntent.putExtra("EXTRA_DATA", data)
        resultIntent.putExtra("EXTRA_RECORRENTE", recorrente)
        resultIntent.putExtra("EXTRA_ANTECEDENCIA", antecedencia)

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}