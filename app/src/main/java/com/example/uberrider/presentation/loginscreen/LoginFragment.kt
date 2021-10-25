package com.example.uberrider.presentation.loginscreen

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uberrider.R
import com.example.uberrider.databinding.LoginFragmentLayoutBinding
import com.example.uberrider.presentation.mainactivity.MainActivity
import com.example.uberrider.util.Status
import com.example.uberrider.util.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.login_fragment_layout){

    private var _binding: LoginFragmentLayoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = LoginFragmentLayoutBinding.bind(view)

        binding.register.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                val result = viewModel.getRegisterStatus()

                if(result) {
                    findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
                }else {
                    findNavController().popBackStack()
                }
            }
        }

        binding.loginButton.setOnClickListener {

            requireContext().hideKeyboard(view)
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            viewModel.login(email, password)
        }

        binding.forgetPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_resetPasswordFragment)
        }

        viewModel.loginResult.observe(viewLifecycleOwner,{ result ->
            when(result.status) {
                Status.SUCCESS -> {
                    lifecycleScope.launchWhenStarted {
                        binding.loginProgressBar.visibility = View.INVISIBLE
                        viewModel.validateRegisterStatus()

                        startActivity(Intent(activity, MainActivity::class.java))
                        requireActivity().finish()
                    }
                }

                Status.ERROR -> {
                    binding.loginProgressBar.visibility = View.INVISIBLE
                    binding.loginButton.visibility = View.VISIBLE
                    Snackbar.make(view, result.errorMessage!!, Snackbar.LENGTH_LONG)
                        .show()
                }

                Status.LOADING -> {
                    binding.loginProgressBar.visibility = View.VISIBLE
                    binding.loginButton.visibility = View.INVISIBLE
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}