package com.example.meusboletos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

interface BoletoAdapterListener {
    fun onExcluirClick(position: Int)
    fun onPagoClick(position: Int, isChecked: Boolean)
    fun onItemClick(position: Int)
}

class BoletoAdapter(
    private val boletos: List<Boleto>,
    private val listener: BoletoAdapterListener
) : RecyclerView.Adapter<BoletoAdapter.BoletoViewHolder>() {

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

        // Remove o listener antigo para evitar múltiplos cliques
        holder.checkBoxPago.setOnCheckedChangeListener(null)
        holder.checkBoxPago.isChecked = boleto.pago

        // --- MUDANÇA PRINCIPAL AQUI ---
        // Agora usamos holder.adapterPosition, que é sempre a posição atual.

        holder.buttonExcluir.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                listener.onExcluirClick(currentPosition)
            }
        }

        holder.checkBoxPago.setOnCheckedChangeListener { _, isChecked ->
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                listener.onPagoClick(currentPosition, isChecked)
            }
        }

        holder.itemView.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                listener.onItemClick(currentPosition)
            }
        }
    }

    override fun getItemCount() = boletos.size
}