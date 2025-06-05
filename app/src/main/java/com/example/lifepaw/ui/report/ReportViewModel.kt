package com.example.lifepaw.ui.report

import androidx.lifecycle.*
import com.example.lifepaw.api.AnimalApiService
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ReportViewModel(
    private val shelterId: Int,
    private val apiService: AnimalApiService
) : ViewModel() {

    private val _reports = MutableLiveData<List<Report>>()
    val reports: LiveData<List<Report>> = _reports

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        if (shelterId != -1 && shelterId != 0) {
            loadReports()
        } else {
            _errorMessage.value = "Помилка: ID притулку не вказано або невірний."
            _reports.value = emptyList()
        }
    }

    fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getMedicalReports(shelterId)
                if (response.isSuccessful) {
                    val reportDataList = response.body()
                    if (reportDataList != null) {
                        _reports.value = reportDataList.map { data ->
                            Report(
                                title = data.reportType,
                                date = formatDate(data.date),
                                description = data.description
                            )
                        }.sortedByDescending { formatDateForSort(it.date) }
                    } else {
                        _reports.value = emptyList()
                    }
                } else {
                    _errorMessage.value = "Помилка завантаження: ${response.code()} ${response.message()}"
                    _reports.value = emptyList()
                }
            } catch (e: IOException) {
                _errorMessage.value = "Мережева помилка: ${e.message}"
                _reports.value = emptyList()
            } catch (e: HttpException) {
                _errorMessage.value = "Помилка HTTP: ${e.message()}"
                _reports.value = emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Неочікувана помилка: ${e.message}"
                _reports.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatDate(dateTimeString: String?): String {
        if (dateTimeString.isNullOrEmpty()) return "Дата невідома"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            parser.parse(dateTimeString)?.let { formatter.format(it) } ?: dateTimeString.take(10)
        } catch (e: Exception) {
            try {
                val parserNoMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                parserNoMillis.timeZone = TimeZone.getTimeZone("UTC")
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                parserNoMillis.parse(dateTimeString)?.let { formatter.format(it) } ?: dateTimeString.take(10)
            } catch (e2: Exception) {
                try { dateTimeString.take(10) } catch (e3: Exception) { dateTimeString }
            }
        }
    }

    private fun formatDateForSort(dateString: String): String {
        return dateString
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

class ReportViewModelFactory(
    private val shelterId: Int,
    private val apiService: AnimalApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(shelterId, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for ReportViewModel")
    }
}