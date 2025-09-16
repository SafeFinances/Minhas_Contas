package com.example.meusboletos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.meusboletos.databinding.ActivityAddBoletoBinding

class AddBoletoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBoletoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBoletoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ação do botão Salvar
        binding.buttonSalvar.setOnClickListener {
            salvarBoleto()
        }
    }

    private fun salvarBoleto() {
        val nome = binding.editTextNome.text.toString()
        val valor = binding.editTextValor.text.toString()
        val data = binding.editTextData.text.toString()
        // Nova linha: lê o estado do switch
        val recorrente = binding.switchRecorrente.isChecked

        if (nome.isBlank() || valor.isBlank() || data.isBlank()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val resultIntent = Intent()
        resultIntent.putExtra("EXTRA_NOME", nome)
        resultIntent.putExtra("EXTRA_VALOR", valor)
        resultIntent.putExtra("EXTRA_DATA", data)
        // Nova linha: adiciona a informação ao "pacote de resposta"
        resultIntent.putExtra("EXTRA_RECORRENTE", recorrente)

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}