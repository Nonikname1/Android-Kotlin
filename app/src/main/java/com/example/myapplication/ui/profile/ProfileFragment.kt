package com.example.myapplication.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.App
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentProfileBinding
import com.example.myapplication.ui.auth.AuthUiState
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.auth.AuthViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory((requireActivity().application as App).authRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenManager = (requireActivity().application as App).tokenManager

        viewLifecycleOwner.lifecycleScope.launch {
            combine(tokenManager.userName, tokenManager.userEmail) { name, email ->
                name to email
            }.collect { (name, email) ->
                binding.nameText.text = name ?: "—"
                binding.emailText.text = email ?: "—"
            }
        }

        binding.logoutButton.setOnClickListener {
            authViewModel.logout()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.uiState.collectLatest { state ->
                if (state is AuthUiState.LoggedOut) {
                    findNavController().navigate(
                        R.id.loginFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build()
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
