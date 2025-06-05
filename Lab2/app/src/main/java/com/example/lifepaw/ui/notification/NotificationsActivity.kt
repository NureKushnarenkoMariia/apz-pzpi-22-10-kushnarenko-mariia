package com.example.lifepaw.ui.notification

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lifepaw.api.RetrofitInstance
import com.example.lifepaw.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {

    private var _binding: ActivityNotificationsBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val EXTRA_ANIMAL_ID = "ANIMAL_ID"
        const val EXTRA_ANIMAL_NAME = "ANIMAL_NAME"
    }

    private val animalId: Int by lazy {
        intent.getIntExtra(EXTRA_ANIMAL_ID, -1)
    }

    private val animalName: String by lazy {
        intent.getStringExtra(EXTRA_ANIMAL_NAME) ?: "Unknown"
    }

    private val viewModel: NotificationsViewModel by viewModels {
        NotificationsViewModelFactory(animalId, RetrofitInstance.api)
    }

    private lateinit var adapter: NotificationAdapter
    private val notificationsDisplayList: MutableList<NotificationItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        setupListView()
        observeViewModel()

    }

    private fun setupListView() {
        adapter = NotificationAdapter(this, notificationsDisplayList)
        binding.listNotifications.adapter = adapter

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.animalName.text = animalName
    }

    private fun observeViewModel() {
        viewModel.notificationItems.observe(this) { items ->
            notificationsDisplayList.clear()
            notificationsDisplayList.addAll(items)
            adapter.notifyDataSetChanged()

        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}