package com.example.uberrider.presentation.registerscreen

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uberrider.R
import com.example.uberrider.databinding.RegisterFragmentLayoutBinding
import com.example.uberrider.presentation.mainactivity.MainActivity
import com.example.uberrider.util.Status
import com.example.uberrider.util.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment: Fragment(R.layout.register_fragment_layout){

    private var _binding: RegisterFragmentLayoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = RegisterFragmentLayoutBinding.bind(view)

        binding.registerButton.setOnClickListener {

            requireContext().hideKeyboard(view)

            val email = binding.emailEditText.text.toString().trim()
            val fullName = binding.fullNameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
            val phoneNumber = binding.phoneNumberEditText.text.toString().trim()

            viewModel.register(fullName, email, phoneNumber, password, confirmPassword)
        }

        binding.singIn.setOnClickListener {
            lifecycleScope.launchWhenStarted {

                val result = viewModel.getRegisterStatus()

                if(result) {
                    findNavController().popBackStack()
                    return@launchWhenStarted
                } else {
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
            }
        }

        viewModel.registerResult.observe(viewLifecycleOwner,{ result ->
            when(result.status) {
                Status.SUCCESS -> {
                    binding.registerProgress.visibility = View.INVISIBLE

                    lifecycleScope.launchWhenStarted {
                        viewModel.validateRegisterStatus()
                    }

                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                }
                Status.ERROR -> {
                    binding.registerProgress.visibility = View.INVISIBLE
                    binding.registerButton.visibility = View.VISIBLE
                    Snackbar.make(view, "${result.errorMessage}", Snackbar.LENGTH_LONG)
                        .show()
                }

                Status.LOADING -> {
                    binding.registerProgress.visibility = View.VISIBLE
                    binding.registerButton.visibility = View.INVISIBLE
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}