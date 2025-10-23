package com.geoevent.ui.geoevents

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.geoevent.R
import com.geoevent.databinding.FragmentGeoeventsBinding

class GeoEventsFragment : Fragment() {

    private var _binding: FragmentGeoeventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GeoEventsViewModel
    private lateinit var adapter: GeoEventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeoeventsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[GeoEventsViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = GeoEventsAdapter(
            onViewChatClick = { geoEvent ->
                // Navigate to messages tab - will be implemented in Phase 6
                Toast.makeText(requireContext(), "Chat for event ${geoEvent.id.take(8)}... (Coming in Phase 6)", Toast.LENGTH_SHORT).show()
                // Navigate to Messages tab
                findNavController().navigate(R.id.navigation_message)
            },
            onDeleteClick = { geoEvent ->
                showDeleteConfirmation(geoEvent.id)
            }
        )
        binding.recyclerEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEvents.adapter = adapter
    }

    private fun setupObservers() {
        // Observe events list
        viewModel.geoEvents.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events)

            // Show empty state if no events
            if (events.isEmpty()) {
                binding.recyclerEvents.visibility = View.GONE
                binding.textEmpty.visibility = View.VISIBLE
            } else {
                binding.recyclerEvents.visibility = View.VISIBLE
                binding.textEmpty.visibility = View.GONE
            }
        }

        // Observe operation state
        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is GeoEventsViewModel.OperationState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is GeoEventsViewModel.OperationState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is GeoEventsViewModel.OperationState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabCreateEvent.setOnClickListener {
            showCreateEventDialog()
        }
    }

    private fun showCreateEventDialog() {
        val stamps = viewModel.availableStamps.value

        if (stamps.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                "No available GeoStamps. Create one in Dashboard first!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val stampNames = stamps.map { "Stamp: ${it.id.take(8)}... (Lat: ${String.format("%.4f", it.latitude ?: 0.0)}, Lon: ${String.format("%.4f", it.longitude ?: 0.0)})" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, stampNames)

        AlertDialog.Builder(requireContext())
            .setTitle("Create GeoEvent")
            .setMessage("Select a GeoStamp to link to this event:")
            .setAdapter(adapter) { _, which ->
                val selectedStamp = stamps[which]
                viewModel.createGeoEvent(selectedStamp.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(eventId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete GeoEvent")
            .setMessage("Are you sure you want to delete this event?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteGeoEvent(eventId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}