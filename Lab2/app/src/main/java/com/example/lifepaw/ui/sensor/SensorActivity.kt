// File: app/src/main/java/com/example/lifepaw/ui/sensor/SensorActivity.kt
package com.example.lifepaw.ui.sensor

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lifepaw.api.RetrofitInstance
import com.example.lifepaw.databinding.ActivitySensorBinding

class SensorActivity : AppCompatActivity() {

    private var _binding: ActivitySensorBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val EXTRA_ANIMAL_ID = "ANIMAL_ID"
    }

    private val animalId: Int by lazy {
        intent.getIntExtra(EXTRA_ANIMAL_ID, -1)
    }

    private val viewModel: SensorViewModel by viewModels {
        SensorViewModelFactory(animalId, RetrofitInstance.api)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySensorBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        if (animalId == -1) {
            Toast.makeText(this, "Помилка: ID тварини не було передано.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.sensorUIData.observe(this) { uiData ->
            binding.textSensorType.text = uiData.type
            binding.textSensorStatus.text = uiData.status
            binding.textSensorInstallDate.text = uiData.installationDate
            binding.textSensorLinkedAnimal.text = uiData.linkedAnimalInfo
            binding.textSensorTemperature.text = uiData.temperature
            binding.textSensorHumidity.text = uiData.humidity
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.textSensorStatus.text = "Статус: Завантаження..."
            }
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                Log.d("ABOBA", "setupObservers: $errorMessage")

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}