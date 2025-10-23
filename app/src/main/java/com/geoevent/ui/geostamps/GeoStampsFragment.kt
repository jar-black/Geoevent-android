package com.geoevent.ui.geostamps

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.geoevent.databinding.FragmentGeostampsBinding

class GeoStampsFragment : Fragment() {

    private var _binding: FragmentGeostampsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GeoStampsViewModel
    private lateinit var adapter: GeoStampsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeostampsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[GeoStampsViewModel::class.java]

        setupRecyclerView()
        setupObservers()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = GeoStampsAdapter(
            onDeleteClick = { geoStamp ->
                showDeleteConfirmation(geoStamp.id)
            }
        )
        binding.recyclerStamps.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStamps.adapter = adapter
    }

    private fun setupObservers() {
        // Observe geostamps list
        viewModel.geoStamps.observe(viewLifecycleOwner) { stamps ->
            adapter.submitList(stamps)

            // Show empty state if no stamps
            if (stamps.isEmpty()) {
                binding.recyclerStamps.visibility = View.GONE
                binding.textEmpty.visibility = View.VISIBLE
            } else {
                binding.recyclerStamps.visibility = View.VISIBLE
                binding.textEmpty.visibility = View.GONE
            }
        }

        // Observe operation state
        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is GeoStampsViewModel.OperationState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is GeoStampsViewModel.OperationState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is GeoStampsViewModel.OperationState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun showDeleteConfirmation(stampId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete GeoStamp")
            .setMessage("Are you sure you want to delete this geostamp?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteGeoStamp(stampId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}