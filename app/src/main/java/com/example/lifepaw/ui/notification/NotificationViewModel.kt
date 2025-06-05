// File: app/src/main/java/com/example/lifepaw/ui/notification/NotificationsViewModel.kt
package com.example.lifepaw.ui.notification

import androidx.lifecycle.*
import com.example.lifepaw.api.AnimalApiService
import com.example.lifepaw.api.NotificationData // Переконайтеся, що шлях правильний
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationsViewModel(
    private val animalId: Int,
    private val apiService: AnimalApiService
) : ViewModel() {

    private val _notificationItems = MutableLiveData<List<NotificationItem>>()
    val notificationItems: LiveData<List<NotificationItem>> = _notificationItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        if (animalId != -1) {
            loadNotifications()
        } else {
            _errorMessage.value = "Помилка: ID тварини не вказано або невірний."
            _notificationItems.value = emptyList()
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getNotificationForAnimal(animalId)
                if (response.isSuccessful) {
                    val notificationDataList = response.body()
                    if (notificationDataList != null) {
                        _notificationItems.value = notificationDataList.map { data ->
                            NotificationItem(
                                title = data.type,
                                date = formatDate(data.dateTime),
                                description = data.message
                            )
                        }.sortedByDescending { formatDateForSort(it.date) }
                    } else {
                        _notificationItems.value = emptyList()
                    }
                } else {
                    _errorMessage.value = "Помилка завантаження: ${response.code()} ${response.message()}"
                    _notificationItems.value = emptyList()
                }
            } catch (e: IOException) {
                _errorMessage.value = "Мережева помилка: ${e.message}"
                _notificationItems.value = emptyList()
            } catch (e: HttpException) {
                _errorMessage.value = "Помилка HTTP: ${e.message()}"
                _notificationItems.value = emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Неочікувана помилка: ${e.message}"
                _notificationItems.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatDate(dateTimeString: String?): String {
        if (dateTimeString.isNullOrEmpty()) return "Дата невідома"
        return try {
            val parserWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            parserWithMillis.parse(dateTimeString)?.let { formatter.format(it) } ?: run {
                val parserWithoutMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                parserWithoutMillis.parse(dateTimeString)?.let { formatter.format(it) } ?: dateTimeString.take(10)
            }
        } catch (e: Exception) {
            try { dateTimeString.take(10) } catch (e2: Exception) { dateTimeString }
        }
    }

    private fun formatDateForSort(dateString: String): String {
        return dateString
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

class NotificationsViewModelFactory(
    private val animalId: Int,
    private val apiService: AnimalApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationsViewModel(animalId, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for NotificationsViewModel")
    }
}