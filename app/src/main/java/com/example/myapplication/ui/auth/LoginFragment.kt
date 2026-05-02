package com.example.myapplication.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory((requireActivity().application as App).authRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUiState()
        setupClickListeners()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUiState.Idle -> setLoading(false)
                        is AuthUiState.Loading -> setLoading(true)
                        is AuthUiState.Success -> {
                            setLoading(false)
                            findNavController().navigate(R.id.action_login_to_apartments)
                        }
                        is AuthUiState.Error -> {
                            setLoading(false)
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                        is AuthUiState.LoggedOut -> Unit
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text?.toString().orEmpty().trim()
            val password = binding.passwordInput.text?.toString().orEmpty()
            if (email.isBlank() || password.isBlank()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_fill_all_fields),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.login(email, password)
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !loading
        binding.registerButton.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
