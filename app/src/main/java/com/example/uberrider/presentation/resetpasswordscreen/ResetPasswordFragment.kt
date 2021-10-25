package com.example.uberrider.presentation.resetpasswordscreen

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.uberrider.R
import com.example.uberrider.databinding.ResetPasswordFragmentLayoutBinding
import com.example.uberrider.util.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordFragment: Fragment(R.layout.reset_password_fragment_layout){

    private var _binding: ResetPasswordFragmentLayoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResetPasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ResetPasswordFragmentLayoutBinding.bind(view)

        binding.backArrowButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.confirmButton.setOnClickListener {
            viewModel.resetPassword(binding.emailEditText.text.toString().trim())
        }

        viewModel.resetPasswordResult.observe(viewLifecycleOwner,{ result ->
            when(result.status) {
                Status.SUCCESS -> {
                    binding.resetPasswordProgressBar.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), "Checkout your inbox.", Toast.LENGTH_SHORT)
                        .show()

                    findNavController().popBackStack()
                }
                Status.ERROR -> {
                    binding.resetPasswordProgressBar.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), "${result.errorMessage}", Toast.LENGTH_SHORT)
                        .show()
                }
                Status.LOADING -> {
                    binding.resetPasswordProgressBar.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}