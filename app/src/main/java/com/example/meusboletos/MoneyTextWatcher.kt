package com.example.meusboletos

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class MoneyTextWatcher(editText: EditText) : TextWatcher {
    private val editTextWeakReference: WeakReference<EditText> = WeakReference(editText)

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(editable: Editable) {
        val editText = editTextWeakReference.get() ?: return
        editText.removeTextChangedListener(this)

        val parsed = parseToBigDecimal(editable.toString())
        val formatted = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(parsed)

        // Remove o símbolo "R$" e espaços
        val cleanFormatted = formatted.replace(Regex("[R$\\s]"), "")

        editText.setText(cleanFormatted)
        editText.setSelection(cleanFormatted.length)
        editText.addTextChangedListener(this)
    }

    private fun parseToBigDecimal(text: String): BigDecimal {
        val cleanString = text.replace(Regex("[^\\d]"), "")
        return if (cleanString.isEmpty()) {
            BigDecimal.ZERO
        } else {
            BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
        }
    }
}