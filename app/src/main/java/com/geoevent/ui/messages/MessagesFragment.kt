package com.geoevent.ui.messages

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.geoevent.data.auth.SessionManager
import com.geoevent.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MessagesViewModel
    private lateinit var adapter: MessagesAdapter
    private lateinit var sessionManager: SessionManager

    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            viewModel.refreshMessages()
            handler.postDelayed(this, 5000) // Refresh every 5 seconds
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[MessagesViewModel::class.java]
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        return binding.root
    }

    private fun setupRecyclerView() {
        val currentUserId = sessionManager.getUserId() ?: ""
        adapter = MessagesAdapter(currentUserId)

        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true // Start from bottom
        }
        binding.recyclerMessages.adapter = adapter
    }

    private fun setupObservers() {
        // Observe available events for spinner
        viewModel.availableEvents.observe(viewLifecycleOwner) { events ->
            if (events.isEmpty()) {
                binding.textEmpty.visibility = View.VISIBLE
                binding.textEmpty.text = "No GeoEvents available.\nCreate one in the GeoEvents tab!"
                return@observe
            }

            val eventNames = events.map { "Event: ${it.id.take(8)}..." }
            val spinnerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                eventNames
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerEvents.adapter = spinnerAdapter

            binding.spinnerEvents.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.selectEvent(events[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        // Observe selected event
        viewModel.selectedEvent.observe(viewLifecycleOwner) { event ->
            if (event != null) {
                binding.textEmpty.visibility = View.GONE
                startAutoRefresh()
            } else {
                binding.textEmpty.visibility = View.VISIBLE
                stopAutoRefresh()
            }
        }

        // Observe messages
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages) {
                // Scroll to bottom after list update
                if (messages.isNotEmpty()) {
                    binding.recyclerMessages.scrollToPosition(messages.size - 1)
                }
            }
        }

        // Observe operation state
        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MessagesViewModel.OperationState.Loading -> {
                    binding.buttonSend.isEnabled = false
                }
                is MessagesViewModel.OperationState.Success -> {
                    binding.buttonSend.isEnabled = true
                    binding.editMessage.text.clear()
                }
                is MessagesViewModel.OperationState.Error -> {
                    binding.buttonSend.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.buttonSend.isEnabled = true
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonSend.setOnClickListener {
            val content = binding.editMessage.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.sendMessage(content)
            }
        }
    }

    private fun startAutoRefresh() {
        stopAutoRefresh()
        handler.postDelayed(refreshRunnable, 5000)
    }

    private fun stopAutoRefresh() {
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAvailableEvents()
    }

    override fun onPause() {
        super.onPause()
        stopAutoRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoRefresh()
        _binding = null
    }
}