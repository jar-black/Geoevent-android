package com.geoevent.ui.dashboard

import LocationHelper
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.geoevent.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel
    private lateinit var locationHelper: LocationHelper
    private lateinit var adapter: GeoStampAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupRecyclerView()
        setupLocationUpdates()
        setupObservers()
        setupClickListeners()

        // Load initial geostamps
        viewModel.loadGeoStamps()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = GeoStampAdapter()
        binding.recyclerStamps.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStamps.adapter = adapter
    }

    private fun setupLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationHelper = LocationHelper(requireContext())
            locationHelper.startLocationUpdates { location ->
                viewModel.updateLocation(location)
            }
        }
    }

    private fun setupObservers() {
        // Observe current location
        viewModel.currentLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                binding.textLocation.text =
                    "Lat: ${String.format("%.4f", location.latitude)}, " +
                    "Lon: ${String.format("%.4f", location.longitude)}\n" +
                    "Accuracy: ${String.format("%.1f", location.accuracy)}m"
            } else {
                binding.textLocation.text = "Waiting for location..."
            }
        }

        // Observe geostamps list
        viewModel.geoStamps.observe(viewLifecycleOwner) { stamps ->
            adapter.submitList(stamps)
        }

        // Observe create stamp state
        viewModel.createStampState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DashboardViewModel.StampState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.buttonCreateStamp.isEnabled = false
                    binding.textStatus.visibility = View.GONE
                }
                is DashboardViewModel.StampState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonCreateStamp.isEnabled = true
                    binding.textStatus.text = state.message
                    binding.textStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                    binding.textStatus.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is DashboardViewModel.StampState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonCreateStamp.isEnabled = true
                    binding.textStatus.text = state.message
                    binding.textStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                    binding.textStatus.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonCreateStamp.isEnabled = true
                }
            }
        }

        // Observe load stamps state
        viewModel.loadStampsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DashboardViewModel.StampState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is DashboardViewModel.StampState.Error -> {
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
        binding.buttonCreateStamp.setOnClickListener {
            viewModel.createGeoStamp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::locationHelper.isInitialized) {
            locationHelper.stopLocationUpdates()
        }
        _binding = null
    }
}