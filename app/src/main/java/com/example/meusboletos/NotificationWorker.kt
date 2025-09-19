package com.example.meusboletos

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log // IMPORTANTE: Adicionar esta linha
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

class NotificationWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        // ESCUTA 1: O robô começou a trabalhar?
        Log.d("NotificationWorker", ">>> O doWork() foi iniciado!")

        val nomeBoleto = inputData.getString("NOME_BOLETO") ?: "Boleto"
        val dataVencimentoStr = inputData.getString("DATA_VENCIMENTO") ?: return Result.failure()

        // ESCUTA 2: Ele conseguiu receber os dados corretamente?
        Log.d("NotificationWorker", "Dados recebidos: Nome=$nomeBoleto, Vencimento=$dataVencimentoStr")

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dataVencimento = LocalDate.parse(dataVencimentoStr, formatter)
        val hoje = LocalDate.now()
        val diasParaVencer = ChronoUnit.DAYS.between(hoje, dataVencimento)

        val mensagem = when {
            diasParaVencer < 0 -> "Sua fatura '$nomeBoleto' já venceu!"
            diasParaVencer == 0L -> "Sua fatura '$nomeBoleto' vence hoje!"
            diasParaVencer == 1L -> "Sua fatura '$nomeBoleto' vence amanhã!"
            else -> "Faltam $diasParaVencer dias para o vencimento da sua fatura '$nomeBoleto'."
        }

        // ESCUTA 3: Ele preparou a mensagem corretamente?
        Log.d("NotificationWorker", "Mensagem preparada: $mensagem")

        dispararNotificacao(nomeBoleto, mensagem)

        return Result.success()
    }

    private fun dispararNotificacao(titulo: String, conteudo: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, MyApplication.CHANNEL_ID)
            // ÍCONE TROCADO PARA UM MAIS SEGURO (Muitos crashes acontecem por ícones inválidos)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(conteudo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // ESCUTA 4: Ele tem permissão e vai tentar disparar a notificação?
            Log.d("NotificationWorker", "Permissão OK. Disparando a notificação agora...")
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
        } else {
            // ESCUTA 5: Ele não conseguiu por falta de permissão?
            Log.d("NotificationWorker", "ERRO: Falhou ao disparar. Permissão de notificação NEGADA.")
        }
    }
}