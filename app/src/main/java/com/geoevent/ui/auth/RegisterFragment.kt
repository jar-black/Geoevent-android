package com.geoevent.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.geoevent.MainActivity
import com.geoevent.databinding.FragmentRegisterBinding
import java.util.UUID

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.buttonRegister.isEnabled = false
                    binding.textError.visibility = View.GONE
                }
                is AuthViewModel.AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                    navigateToDashboard()
                }
                is AuthViewModel.AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonRegister.isEnabled = true
                    binding.textError.text = state.message
                    binding.textError.visibility = View.VISIBLE
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonRegister.isEnabled = true
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonRegister.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val phoneNumber = binding.editPhone.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (validateInputs(name, phoneNumber, password)) {
                val userId = UUID.randomUUID().toString()
                viewModel.register(userId, phoneNumber, name, password)
            }
        }

        binding.textLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun validateInputs(name: String, phoneNumber: String, password: String): Boolean {
        if (name.isEmpty()) {
            binding.tilName.error = "Name required"
            return false
        }
        binding.tilName.error = null

        if (phoneNumber.isEmpty()) {
            binding.tilPhone.error = "Phone number required"
            return false
        }
        binding.tilPhone.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password required"
            return false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return false
        }
        binding.tilPassword.error = null

        return true
    }

    private fun navigateToDashboard() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
