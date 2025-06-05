package com.example.lifepaw.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.lifepaw.api.Animal
import com.example.lifepaw.api.RetrofitInstance
import com.example.lifepaw.databinding.ActivityMainBinding
import com.example.lifepaw.ui.notification.NotificationsActivity
import com.example.lifepaw.ui.report.ReportsActivity
import com.example.lifepaw.ui.sensor.SensorActivity

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(RetrofitInstance.api)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        setupUIListeners()
        setupObservers()
    }

    private fun setupUIListeners() {
        binding.animalList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedAnimal = parent?.getItemAtPosition(position) as? Animal
                selectedAnimal?.let {
                    viewModel.onAnimalSelected(it)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.onAnimalSelected(Animal(-1, "Не обрано", "", "", 0, com.example.lifepaw.api.Shelter(-1,"","",""), -1))
            }
        }

        binding.buttonSensors.setOnClickListener {
            val intent = Intent(this, SensorActivity::class.java)
            val animalId = viewModel.currentSelectedAnimalObject?.id

            intent.putExtra(ANIMAL_ID, animalId)
            startActivity(intent)
        }

        binding.buttonReports.setOnClickListener {
            val intent = Intent(this, ReportsActivity::class.java)
            val animalId = viewModel.currentSelectedAnimalObject?.shelterId

            intent.putExtra(SHELTER_ID, animalId)
            startActivity(intent)
        }

        binding.buttonNotifications.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            val animalId = viewModel.currentSelectedAnimalObject?.id
            val animalName = viewModel.currentSelectedAnimalObject?.name

            intent.putExtra(ANIMAL_ID, animalId)
            intent.putExtra(EXTRA_ANIMAL_NAME, animalName)
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        viewModel.animals.observe(this, Observer { animals ->
            val animalsToShow = animals ?: emptyList()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, animalsToShow)

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.animalList.adapter = adapter
        })

        viewModel.selectedAnimalDisplayInfo.observe(this, Observer { (cageText, animalInfoText) ->
            binding.textCageNumber.text = cageText
            binding.textAnimalInfo.text = animalInfoText
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.animalList.isEnabled = !isLoading
            if (isLoading) {
                binding.textCageNumber.text = "Завантаження..."
                binding.textAnimalInfo.text = ""
            }
        })

        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                Log.d("ABOBA", "setupObservers: $errorMessage")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object{
        const val ANIMAL_ID = "ANIMAL_ID"
        const val SHELTER_ID = "SHELTER_ID"
        const val EXTRA_ANIMAL_NAME = "ANIMAL_NAME"
    }
}