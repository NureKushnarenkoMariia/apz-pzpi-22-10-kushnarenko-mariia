package com.example.lifepaw.ui.main

import androidx.lifecycle.*
import com.example.lifepaw.api.Animal
import com.example.lifepaw.api.AnimalApiService
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainViewModel(private val animalApiService: AnimalApiService) : ViewModel() {

    private val _animals = MutableLiveData<List<Animal>>()
    val animals: LiveData<List<Animal>> = _animals

    private val _selectedAnimalDisplayInfo = MutableLiveData<Pair<String, String>>()
    val selectedAnimalDisplayInfo: LiveData<Pair<String, String>> = _selectedAnimalDisplayInfo

    private var _currentSelectedAnimalObject: Animal? = null
    val currentSelectedAnimalObject: Animal?
        get() = _currentSelectedAnimalObject

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        fetchAnimals()
    }

    fun fetchAnimals() {
        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = animalApiService.getAnimals()
                if (response.isSuccessful) {
                    val fetchedAnimals = response.body()
                    _animals.value = fetchedAnimals ?: emptyList()
                    fetchedAnimals?.firstOrNull()?.let {
                        onAnimalSelected(it)
                    } ?: run {
                        _currentSelectedAnimalObject = null
                        _selectedAnimalDisplayInfo.value = Pair("Клітка не обрана", "Інформація про тварину відсутня")
                    }
                } else {
                    _errorMessage.value = "Помилка завантаження тварин: ${response.code()} ${response.message()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Мережева помилка: ${e.message}"
            } catch (e: HttpException) {
                _errorMessage.value = "Помилка HTTP: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Неочікувана помилка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onAnimalSelected(animal: Animal) {
        _currentSelectedAnimalObject = animal

        val cageText = "Клітка №${animal.id} — ${animal.name}"

        val ageSuffix = when {
            animal.age % 10 == 1 && animal.age % 100 != 11 -> "рік"
            animal.age % 10 in 2..4 && (animal.age % 100 < 10 || animal.age % 100 >= 20) -> "роки"
            else -> "років"
        }

        val animalDetailedInfoText = """
            ID: ${animal.id}
            Ім’я: ${animal.name}
            Вид: ${animal.species}
            Порода: ${animal.breed}
            Вік: ${animal.age} $ageSuffix
        """.trimIndent()

        _selectedAnimalDisplayInfo.value = Pair(cageText, animalDetailedInfoText)
    }
}

class MainViewModelFactory(private val animalApiService: AnimalApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(animalApiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}