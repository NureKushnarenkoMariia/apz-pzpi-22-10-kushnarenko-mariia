// File: app/src/main/java/com/example/lifepaw/ui/report/ReportsActivity.kt
package com.example.lifepaw.ui.report

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifepaw.api.RetrofitInstance
import com.example.lifepaw.databinding.ActivityReportsBinding

class ReportsActivity : AppCompatActivity() {

    private var _binding: ActivityReportsBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val EXTRA_SHELTER_ID = "SHELTER_ID"
    }

    private val shelterId: Int by lazy {
        intent.getIntExtra(EXTRA_SHELTER_ID, -1)
    }

    private val viewModel: ReportViewModel by viewModels {
        ReportViewModelFactory(shelterId, RetrofitInstance.api)
    }

    private lateinit var reportAdapter: ReportAdapter
    private val reportDisplayList: MutableList<Report> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        setupRecyclerView()
        setupUIListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        reportAdapter = ReportAdapter(reportDisplayList)
        binding.reportsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ReportsActivity)
            adapter = reportAdapter
        }
    }

    private fun setupUIListeners() {
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.reports.observe(this) { reports ->
            reportDisplayList.clear()
            reportDisplayList.addAll(reports)
            reportAdapter.notifyDataSetChanged()

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