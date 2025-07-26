package com.martinez.gananciaalvolante.data.repository

import android.util.Log
import com.martinez.gananciaalvolante.data.local.dao.GananciaDao
import com.martinez.gananciaalvolante.data.local.dao.GastoDao
import com.martinez.gananciaalvolante.data.local.dao.RecorridoDao
import com.martinez.gananciaalvolante.data.local.dao.TurnoDao
import com.martinez.gananciaalvolante.data.local.dao.VehiculoDao
import com.martinez.gananciaalvolante.data.local.entity.EstadisticasDiarias
import com.martinez.gananciaalvolante.data.local.entity.Ganancia
import com.martinez.gananciaalvolante.data.local.entity.Gasto
import com.martinez.gananciaalvolante.data.local.entity.Recorrido
import com.martinez.gananciaalvolante.data.local.entity.ResumenTurno
import com.martinez.gananciaalvolante.data.local.entity.Turno
import com.martinez.gananciaalvolante.data.local.entity.Vehiculo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "Repo_DEBUG"
@Singleton
class VehiculoRepository @Inject constructor(
    private val vehiculoDao: VehiculoDao,
    private val recorridoDao: RecorridoDao,
    private val turnoDao: TurnoDao,
    private val gananciaDao: GananciaDao,
    private val gastoDao: GastoDao
// En el futuro, también podría recibir un 'FirebaseApi' o 'FirestoreSource'
) {

    fun getEstadisticasDelDia(): Flow<EstadisticasDiarias> {
        val (inicioDelDia, finDelDia) = getHoy()
        val recorridosDelDiaFlow: Flow<List<Recorrido>> =
            recorridoDao.getRecorridosEnRangoFlow(inicioDelDia, finDelDia)
        return recorridosDelDiaFlow.map { listaRecorridos ->
            val distanciaTotalKm = listaRecorridos.sumOf { it.distanciaKm }
            val tiempoTotalMs = listaRecorridos.sumOf { it.fechaFin - it.fechaInicio }

            EstadisticasDiarias(distanciaTotalKm = distanciaTotalKm, tiempoTotalMs = tiempoTotalMs)
        }
    }
    suspend fun getFirstVehiculo(): Vehiculo? {
        return vehiculoDao.getFirstVehiculo()
    }
    suspend fun saveGasto(gasto: Gasto) {
        Log.d(TAG, "Función saveGasto() llamada en Repositorio con: $gasto") // <-- LOG 9
        try {
            gastoDao.insert(gasto)
            Log.d(TAG, "¡ÉXITO! gastoDao.insert() ejecutado sin errores.") // <-- LOG 10
        } catch (e: Exception) {
            Log.e(TAG, "ERROR al insertar en GastoDao: ", e) // <-- LOG 11 (Captura cualquier crash de Room)
        }
    }
    suspend fun saveRecorridoAndUpdateOdometro(recorrido: Recorrido) {
        // Obtenemos el vehículo al que pertenece este recorrido
        val vehiculo = vehiculoDao.getVehiculoById(recorrido.vehiculoId)

        if (vehiculo != null) {
            // Calculamos el nuevo valor del odómetro
            val nuevoOdometro = vehiculo.odometro + recorrido.distanciaKm

            // Creamos una copia del objeto vehículo con el odómetro actualizado
            val vehiculoActualizado = vehiculo.copy(odometro = nuevoOdometro)
            recorridoDao.insert(recorrido)
            vehiculoDao.updateVehiculo(vehiculoActualizado) // Usaremos un método de actualización
        } else {
            recorridoDao.insert(recorrido)
        }
    }
    fun getAllTurnos(): Flow<List<Turno>> {
        return turnoDao.getAllTurnos()
    }
    fun getAllGastos(): Flow<List<Gasto>> {
        return gastoDao.getAllGastos()
    }
    // (Haz lo mismo para Recorridos y Ganancias cuando los necesites)
    fun getAllRecorridos(): Flow<List<Recorrido>> {
        return recorridoDao.getAllRecorridos() // Necesitarás crear este método en RecorridoDao
    }

    fun getAllGanancias(): Flow<List<Ganancia>> {
        return gananciaDao.getAllGanancias() // Necesitarás crear este método en GananciaDao
    }
    suspend fun getDatosParaResumenTurno(timestampInicio: Long): ResumenTurno {
        val recorridos = recorridoDao.getRecorridosDesde(timestampInicio)
        val gastos = gastoDao.getGastosDesde(timestampInicio)

        val distanciaKm = recorridos.sumOf { it.distanciaKm }
        val tiempoMs = recorridos.sumOf { it.fechaFin - it.fechaInicio }
        val gastoTotal = gastos.sumOf { it.monto }

        return ResumenTurno(distanciaKm, tiempoMs, gastoTotal)
    }

    suspend fun finalizarYGuardarTurno(fechaInicio: Long, fechaFin: Long, gananciaBruta: Double) {
        // 1. Obtener los recorridos y gastos del período que no tienen turno asignado
        val recorridosDelTurno = recorridoDao.getRecorridosSinTurno(fechaInicio, fechaFin)
        val gastosDelTurno = gastoDao.getGastosSinTurno(fechaInicio, fechaFin)

        // 2. Calcular las métricas
        val totalKm = recorridosDelTurno.sumOf { it.distanciaKm }
        val gastoTotal = gastosDelTurno.sumOf { it.monto }
        val gananciaNeta = gananciaBruta - gastoTotal
        val tiempoTotalTrabajadoMs = recorridosDelTurno.sumOf { it.fechaFin - it.fechaInicio }

        val horasTrabajadas = tiempoTotalTrabajadoMs / (1000.0 * 60 * 60)
        val gananciaPorHora = if (horasTrabajadas > 0) gananciaNeta / horasTrabajadas else 0.0
        val costoPorKm = if (totalKm > 0) gastoTotal / totalKm else 0.0

        // 3. Crear el objeto Turno
        val nuevoTurno = Turno(
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            gananciaBruta = gananciaBruta,
            gananciaNeta = gananciaNeta,
            totalKm = totalKm,
            tiempoTotalTrabajadoMs = tiempoTotalTrabajadoMs,
            costoPorKm = costoPorKm,
            gananciaPorHora = gananciaPorHora,
            gananciaPorKm = if (totalKm > 0) gananciaNeta / totalKm else 0.0
        )

        // 4. Guardar el turno y obtener su ID
        val turnoId = turnoDao.insert(nuevoTurno)

        // 5. Asociar los recorridos y gastos al ID del nuevo turno
        recorridoDao.asociarRecorridosAlTurno(turnoId, fechaInicio, fechaFin)
        gastoDao.asociarGastosAlTurno(turnoId, fechaInicio, fechaFin)
    }


    suspend fun insertVehiculo(vehiculo: Vehiculo) = vehiculoDao.insertVehiculo(vehiculo)
    fun getVehiculos(): Flow<List<Vehiculo>> = vehiculoDao.getAllVehiculos()
    suspend fun saveRecorrido(recorrido: Recorrido) = recorridoDao.insert(recorrido)
    suspend fun saveGanancia(ganancia: Ganancia) = gananciaDao.insert(ganancia)
    private fun getHoy(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        setTiempoACero(cal)
        val inicio = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val fin = cal.timeInMillis - 1
        return Pair(inicio, fin)
    }

    private fun setTiempoACero(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }
    fun getTurnoById(turnoId: Long): Flow<Turno?> {
        return turnoDao.getTurnoById(turnoId)
    }
    fun getRecorridosByTurnoId(turnoId: Long): Flow<List<Recorrido>> {
        return recorridoDao.getRecorridosByTurnoId(turnoId)
    }
    fun getGastosByTurnoId(turnoId: Long): Flow<List<Gasto>> {
        return gastoDao.getGastosByTurnoId(turnoId)
    }
//    fun getIngresosByTurnoId(turnoId: Long): Flow<List<IngresoManual>> {
//        return ingresoManualDao.getIngresosByTurnoId(turnoId)
//    }
}