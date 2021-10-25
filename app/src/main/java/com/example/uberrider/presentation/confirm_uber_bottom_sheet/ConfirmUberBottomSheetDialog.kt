package com.example.uberrider.presentation.confirm_uber_bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.example.uberrider.databinding.ConfirmUberBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfirmUberBottomSheetDialog: BottomSheetDialogFragment(){

    private var _binding: ConfirmUberBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ConfirmUberBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancelUber.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                "uber_confirmation_state",
                bundleOf(
                    "state" to "Cancel",
                )
            )

            dismiss()
        }

        binding.confirmUber.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                "uber_confirmation_state",
                bundleOf(
                    "state" to "Confirm",
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