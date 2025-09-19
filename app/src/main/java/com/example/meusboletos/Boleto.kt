package com.example.meusboletos

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tabela_de_boletos")
data class Boleto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var nome: String,
    var dataVencimento: String,
    var pago: Boolean = false,
    val recorrente: Boolean = false,
    var diasAntecedenciaNotificacao: Int = 1
) : Serializable