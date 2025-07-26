package com.martinez.gananciaalvolante.di.util

import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

object Formatters {

    /**
     * Convierte un tiempo en milisegundos a un formato HH:MM:SS.
     * Si las horas son 0, devuelve un formato MM:SS.
     *
     * @param ms El tiempo en milisegundos.
     * @return Una cadena de texto formateada.
     */
    fun getFormattedTimeWithUnit(ms: Long): Pair<String, String> {
        if (ms < 0) {
            return "0:00" to "min"
        }
        var milliseconds = ms

        // Calcular horas
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)

        // Calcular minutos
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)

        // Calcular segundos
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        // Decidir el formato y la unidad basándose en si hay horas o no
        return if (hours > 0) {
            // Formato H:MM:SS (sin cero a la izquierda para la hora)
            val timeString = "${hours}:" +
                    "${String.format("%02d", minutes)}:" +
                    "${String.format("%02d", seconds)}"
            timeString to "hs" // Devuelve el tiempo y la unidad "hs"
        } else {
            // Formato M:SS (sin cero a la izquierda para los minutos)
            val timeString = "${minutes}:" +
                    "${String.format("%02d", seconds)}"
            timeString to "min" // Devuelve el tiempo y la unidad "min"
        }
    }
    /**
     * Función original para cronómetros que siempre necesitan HH:MM:SS.
     * La mantenemos por si la necesitamos en otro lugar.
     */
    fun getFormattedStopWatchTime(ms: Long): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return "${String.format("%02d", hours)}:" +
                "${String.format("%02d", minutes)}:" +
                "${String.format("%02d", seconds)}"
    }
    /**
     * Formatea un valor numérico como moneda en pesos colombianos (COP).
     * Muestra el símbolo '$', usa puntos como separadores de miles y no muestra decimales.
     *
     * @param amount El monto a formatear.
     * @return Una cadena de texto formateada, ej. "$ 20.000".
     */
    fun toColombianPesos(amount: Double): String {
        // 1. Obtenemos una instancia de formato de moneda para la localización de Colombia.
        // Locale("es", "CO") especifica español de Colombia.
        val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        // 2. Por defecto, COP usa 2 decimales. Los eliminamos.
        format.maximumFractionDigits = 0

        // 3. Formateamos el número.
        // El formato ya incluye el símbolo "$" y el espaciado correcto.
        return format.format(amount)
    }

}
