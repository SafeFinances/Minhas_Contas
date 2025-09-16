package com.example.meusboletos

import java.util.UUID

data class Boleto(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val valor: String,
    var dataVencimento: String, // MUDANÇA FEITA AQUI
    var pago: Boolean = false,
    val recorrente: Boolean = false
): java.io.Serializable