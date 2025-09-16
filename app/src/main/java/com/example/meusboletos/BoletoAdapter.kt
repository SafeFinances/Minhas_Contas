package com.example.meusboletos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// O "contrato" para comunicar cliques para a MainActivity
interface BoletoAdapterListener {
    fun onExcluirClick(position: Int)
    fun onPagoClick(position: Int, isChecked: Boolean)
}

class BoletoAdapter(
    private val boletos: List<Boleto>,
    private val listener: BoletoAdapterListener // O Adapter agora precisa de um "ouvinte"
) : RecyclerView.Adapter<BoletoAdapter.BoletoViewHolder>() {

    // O ViewHolder agora conhece todos os botões e textos
    class BoletoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeBoleto: TextView = itemView.findViewById(R.id.textViewNomeBoleto)
        val dataVencimento: TextView = itemView.findViewById(R.id.textViewDataVencimento)
        val checkBoxPago: CheckBox = itemView.findViewById(R.id.checkBoxPago)
        val buttonExcluir: ImageButton = itemView.findViewById(R.id.buttonExcluir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoletoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_boleto, parent, false)
        return BoletoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoletoViewHolder, position: Int) {
        val boleto = boletos[position]
        holder.nomeBoleto.text = boleto.nome
        holder.dataVencimento.text = "Vence em: ${boleto.dataVencimento}"
        holder.checkBoxPago.isChecked = boleto.pago

        // Ação para o botão de excluir
        holder.buttonExcluir.setOnClickListener {
            listener.onExcluirClick(position)
        }

        // Ação para o checkbox de pago
        holder.checkBoxPago.setOnCheckedChangeListener { _, isChecked ->
            listener.onPagoClick(position, isChecked)
        }
    }

    override fun getItemCount() = boletos.size
}