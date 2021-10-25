package com.example.uberrider.presentation.introscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.uberrider.R
import com.example.uberrider.databinding.IntroFragmentLayoutBinding

class IntroFragment: Fragment(R.layout.intro_fragment_layout) {

    private var _binding: IntroFragmentLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = IntroFragmentLayoutBinding.bind(view)

        binding.getStartedButton.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_registerFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}