package com.martinez.gananciaalvolante.di.util.vistas

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.martinez.gananciaalvolante.R
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
class VelocimetroView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var velocidadActual = 0f
    private var velocidadMaxima = 160 // Valor por defecto si no se define en XML
    private var anguloInicioDial = 135f
    private var anguloBarridoDial = 270f
    private var longitudAgujaFactor = 0.85f
    private var grosorAguja = 10f
    private var grosorDial = 20f
    private var textSizeNumerosDial = 30f
    private var textSizeVelocidadCentral = 120f // Se ajustará en onSizeChanged
    private var textSizeUnidadVelocidad = 40f  // Para "km/h"

    // Paints
    private val paintDial = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintMarcas = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintAguja = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintTextoNumerosDial = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintTextoVelocidadCentral = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintTextoUnidad = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintCirculoCentralAguja = Paint(Paint.ANTI_ALIAS_FLAG)


    private var animator: ValueAnimator? = null

    // Dimensiones
    private var radio = 0f
    private var centroX = 0f
    private var centroY = 0f

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.VelocimetroView, 0, 0)
            velocidadMaxima = typedArray.getInt(R.styleable.VelocimetroView_velocidadMaxima, 160)
            velocidadActual = typedArray.getFloat(R.styleable.VelocimetroView_velocidadActual, 0f)

            paintAguja.color = typedArray.getColor(R.styleable.VelocimetroView_colorAguja, Color.RED)
            paintDial.color = typedArray.getColor(R.styleable.VelocimetroView_colorDial, ContextCompat.getColor(context, R.color.md_theme_tertiaryFixedDim))
            paintMarcas.color = typedArray.getColor(R.styleable.VelocimetroView_colorMarcas, ContextCompat.getColor(context, R.color.md_theme_tertiary))
            paintTextoNumerosDial.color = typedArray.getColor(R.styleable.VelocimetroView_colorTextoNumeros, ContextCompat.getColor(context, R.color.md_theme_tertiaryFixed))
            paintTextoVelocidadCentral.color = typedArray.getColor(R.styleable.VelocimetroView_colorTextoCentral, ContextCompat.getColor(context, R.color.md_theme_errorContainer_mediumContrast))

            grosorDial = typedArray.getDimension(R.styleable.VelocimetroView_grosorDial, 20f)
            grosorAguja = typedArray.getDimension(R.styleable.VelocimetroView_grosorAguja, 10f)
            longitudAgujaFactor = typedArray.getFloat(R.styleable.VelocimetroView_longitudAgujaFactor, 0.85f)
            textSizeNumerosDial = typedArray.getDimension(R.styleable.VelocimetroView_textSizeNumerosDial, 30f)
            textSizeVelocidadCentral = typedArray.getDimension(R.styleable.VelocimetroView_textSizeVelocidadCentral, 150f) // Se ajusta en onSizeChanged

            anguloInicioDial = typedArray.getFloat(R.styleable.VelocimetroView_anguloInicioDial, 135f)
            anguloBarridoDial = typedArray.getFloat(R.styleable.VelocimetroView_anguloBarridoDial, 270f)

            typedArray.recycle()
        }

        paintDial.style = Paint.Style.STROKE
        paintDial.strokeWidth = grosorDial

        paintMarcas.strokeWidth = grosorDial * 0.25f // Hacerlo relativo al grosor del dial

        paintAguja.strokeWidth = grosorAguja
        paintAguja.strokeCap = Paint.Cap.ROUND

        paintCirculoCentralAguja.style = Paint.Style.FILL
        paintCirculoCentralAguja.color = paintAguja.color


        paintTextoNumerosDial.textSize = textSizeNumerosDial
        paintTextoNumerosDial.textAlign = Paint.Align.CENTER

        paintTextoVelocidadCentral.textAlign = Paint.Align.CENTER
        paintTextoVelocidadCentral.typeface = Typeface.create("sans-serif-light", Typeface.BOLD)
        // textSizeVelocidadCentral se ajusta en onSizeChanged

        paintTextoUnidad.color = paintTextoVelocidadCentral.color // Mismo color que el número por defecto
        paintTextoUnidad.textAlign = Paint.Align.CENTER
        // textSizeUnidadVelocidad se ajusta en onSizeChanged
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = grosorDial / 2f // Para que el dial no se corte en los bordes
        val diametroUtil = min(w, h) - 2 * padding
        radio = (diametroUtil / 2f) * 0.90f // Un poco más de espacio dentro del padding

        centroX = w / 2f
        centroY = h / 2f // Ajustar el centro si es necesario para el espacio del texto inferior

        // Ajustar tamaños de texto basados en el radio para escalabilidad
        paintTextoVelocidadCentral.textSize = radio * 0.45f // Reducido de 0.7f a 0.45f
        paintTextoNumerosDial.textSize = radio * 0.12f
        paintTextoUnidad.textSize = radio * 0.15f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Dibujar el dial
        val rectDial = RectF(centroX - radio, centroY - radio, centroX + radio, centroY + radio)
        canvas.drawArc(rectDial, anguloInicioDial, anguloBarridoDial, false, paintDial)

        // 2. Dibujar marcas y números alrededor del dial
        val incrementoMarcasVisuales = 10 // Queremos una marca visual cada 10 km/h
        val incrementoNumeros = 20     // Pero solo números cada 20 km/h

        for (i in 0..velocidadMaxima step 5) { // Iterar de 5 en 5 para marcas pequeñas
            if (i % incrementoMarcasVisuales != 0 && i != 0 && i != velocidadMaxima) continue // Solo dibujar marcas principales de 10

            val porcentajeDelMaximo = if (velocidadMaxima > 0) i.toFloat() / velocidadMaxima.toFloat() else 0f
            val anguloMarca = anguloInicioDial + (porcentajeDelMaximo * anguloBarridoDial)
            val anguloRadMarca = Math.toRadians(anguloMarca.toDouble())

            // Las marcas de 10, 30, 50, etc., serán más cortas que las de 0, 20, 40...
            val esMarcaPrincipalNumerada = (i % incrementoNumeros == 0 || i == 0 || i == velocidadMaxima)
            val factorLongitudMarca = if (esMarcaPrincipalNumerada) 0.88f else 0.92f // Marcas más cortas si no tienen número

            val xExterior = centroX + radio * cos(anguloRadMarca).toFloat()
            val yExterior = centroY + radio * sin(anguloRadMarca).toFloat()
            val xInterior = centroX + (radio * factorLongitudMarca) * cos(anguloRadMarca).toFloat()
            val yInterior = centroY + (radio * factorLongitudMarca) * sin(anguloRadMarca).toFloat()

            paintMarcas.strokeWidth = if (esMarcaPrincipalNumerada) grosorDial * 0.20f else grosorDial * 0.10f
            canvas.drawLine(xInterior, yInterior, xExterior, yExterior, paintMarcas)

            // Dibujar números
            if (esMarcaPrincipalNumerada) {
                val factorPosTexto = if (i > 99) 0.68f else 0.70f // Alejar un poco más los números
                val xTexto = centroX + (radio * factorPosTexto) * cos(anguloRadMarca).toFloat()
                val yTexto = centroY + (radio * factorPosTexto) * sin(anguloRadMarca).toFloat() + (paintTextoNumerosDial.textSize / 3f)
                canvas.drawText(i.toString(), xTexto, yTexto, paintTextoNumerosDial)
            }
        }

        // 3. Dibujar la aguja
        val velocidadParaAguja = velocidadActual.coerceIn(0f, velocidadMaxima.toFloat())
        val porcentajeVelocidad = if (velocidadMaxima > 0) velocidadParaAguja / velocidadMaxima.toFloat() else 0f
        val anguloAgujaEnDial = anguloInicioDial + (porcentajeVelocidad * anguloBarridoDial)

        canvas.save()
        canvas.rotate(anguloAgujaEnDial, centroX, centroY) // Rotar el canvas al ángulo absoluto del dial
        val longitudRealAguja = radio * longitudAgujaFactor
        // Dibujar la aguja apuntando a lo largo del eje X positivo del canvas rotado
        canvas.drawLine(centroX, centroY, centroX + longitudRealAguja, centroY, paintAguja)
        canvas.restore()

        paintCirculoCentralAguja.color = paintAguja.color
        canvas.drawCircle(centroX, centroY, grosorAguja * 0.85f, paintCirculoCentralAguja) // Círculo central más grande

        // 4. Dibujar el texto de velocidad grande y la unidad en la parte inferior del dial
        val textoVelocidadStr = String.format(Locale.getDefault(), "%.0f", velocidadActual)
        val rectBoundsVelocidad = Rect()
        paintTextoVelocidadCentral.getTextBounds(textoVelocidadStr, 0, textoVelocidadStr.length, rectBoundsVelocidad)

        // Posición Y para el número de velocidad (ajusta radio * 0.35f para subir/bajar)
        val yPosVelocidad = centroY + radio * 0.35f + (rectBoundsVelocidad.height() / 2f)
        canvas.drawText(textoVelocidadStr, centroX, yPosVelocidad, paintTextoVelocidadCentral)

        val yPosUnidad = yPosVelocidad + paintTextoUnidad.textSize * 0.7f // Más pegado al número
        canvas.drawText("km/h", centroX, yPosUnidad, paintTextoUnidad)
    }


    fun setVelocidad(velocidad: Float) { // Cambiado para llamar a la versión con animación
        setVelocidadConAnimacion(velocidad)
    }

    fun setVelocidadConAnimacion(nuevaVelocidad: Float) {
        val velocidadLimpia = nuevaVelocidad.coerceIn(0f, velocidadMaxima.toFloat())
        animator?.cancel()
        animator = ValueAnimator.ofFloat(this.velocidadActual, velocidadLimpia).apply {
            duration = 200 // Animación más rápida
            addUpdateListener { animation ->
                this@VelocimetroView.velocidadActual = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun setVelocidadMaxima(max: Int) {
        if (max > 0 && this.velocidadMaxima != max) {
            this.velocidadMaxima = max
            // Reajustar velocidad actual si excede el nuevo máximo
            this.velocidadActual = this.velocidadActual.coerceIn(0f, velocidadMaxima.toFloat())
            invalidate()
        }
    }
}

