package com.example.lifepaw.ui.sensor

import androidx.lifecycle.*
import com.example.lifepaw.api.AnimalApiService
import com.example.lifepaw.api.SensorUIData
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class SensorViewModel(
    private val animalIdReceived: Int,
    private val apiService: AnimalApiService
) : ViewModel() {

    private val _sensorUIData = MutableLiveData<SensorUIData>()
    val sensorUIData: LiveData<SensorUIData> = _sensorUIData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        if (animalIdReceived != -1) {
            loadSensorData()
        } else {
            _errorMessage.value = "Помилка: Неправильний ID тварини."
            _sensorUIData.value = getErrorUIData("Помилка ID")
        }
    }

    fun loadSensorData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getSensorForAnimal(animalIdReceived)
                if (response.isSuccessful) {
                    val sensorDataList = response.body()
                    if (!sensorDataList.isNullOrEmpty()) {
                        val sensorData = sensorDataList.first()

                        val formattedInstallDate = formatDate(sensorData.installationDate)

                        _sensorUIData.value = SensorUIData(
                            type = "Тип сенсора: Клімат-контроль",
                            status = "Статус: Активний",
                            installationDate = "Дата встановлення: $formattedInstallDate",
                            linkedAnimalInfo = "Прив’язка до: ${sensorData.animal.name} (Клітка №${sensorData.animal.id})",
                            temperature = "Температура: %.1f°C".format(sensorData.temperature),
                            humidity = "Вологість: %.1f%%".format(sensorData.humidity)
                        )
                    } else {
                        _errorMessage.value = "Дані сенсорів для цієї тварини не знайдено."
                        _sensorUIData.value = getErrorUIData("Дані відсутні")
                    }
                } else {
                    _errorMessage.value = "Помилка ${response.code()}: ${response.message()}"
                    _sensorUIData.value = getErrorUIData("Помилка ${response.code()}")
                }
            } catch (e: IOException) {
                _errorMessage.value = "Мережева помилка: ${e.message}"
                _sensorUIData.value = getErrorUIData("Мережева помилка")
            } catch (e: HttpException) {
                _errorMessage.value = "Помилка HTTP: ${e.message()}"
                _sensorUIData.value = getErrorUIData("Помилка HTTP")
            } catch (e: Exception) {
                _errorMessage.value = "Неочікувана помилка: ${e.message}"
                _sensorUIData.value = getErrorUIData("Неочікувана помилка")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatDate(dateTimeString: String?): String {
        if (dateTimeString.isNullOrEmpty()) return "-"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            parser.parse(dateTimeString)?.let { formatter.format(it) } ?: dateTimeString.take(10) // Повертаємо перші 10 символів, якщо парсинг не вдався
        } catch (e: Exception) {
            dateTimeString.take(10) // Або просто dateTimeString
        }
    }

    private fun getErrorUIData(statusMessage: String): SensorUIData {
        return SensorUIData(
            type = "Тип сенсора: N/A",
            status = "Статус: $statusMessage",
            installationDate = "Дата встановлення: -",
            linkedAnimalInfo = "Прив’язка до: - (Клітка №-)",
            temperature = "Температура: -",
            humidity = "Вологість: -"
        )
    }

}

class SensorViewModelFactory(
    private val animalId: Int,
    private val apiService: AnimalApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SensorViewModel(animalId, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for SensorViewModel")
    }
}