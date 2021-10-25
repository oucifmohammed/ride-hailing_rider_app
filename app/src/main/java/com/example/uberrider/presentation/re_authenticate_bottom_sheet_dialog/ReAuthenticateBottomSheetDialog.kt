package com.example.uberrider.presentation.re_authenticate_bottom_sheet_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.example.uberrider.databinding.ReAuthenticateBottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ReAuthenticateBottomSheetDialog: BottomSheetDialogFragment(){

    private var _binding: ReAuthenticateBottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ReAuthenticateBottomSheetLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.authenticateButton.setOnClickListener {

            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if(email.isEmpty() || password.isEmpty()) {

                binding.emailTextInput.error = "This field is required"
                binding.passwordTextInput.error = "This field is required"

                return@setOnClickListener
            }

            parentFragmentManager.setFragmentResult(
                "userCredentials",
                bundleOf(
                    "currentEmail" to binding.emailEditText.text.toString(),
                    "password" to binding.passwordEditText.text.toString()
                )
            )

            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}