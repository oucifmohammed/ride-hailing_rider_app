package com.example.uberrider.presentation.splashscreen

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uberrider.R
import com.example.uberrider.presentation.mainactivity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class SplashFragment: Fragment(R.layout.splash_fragment_layout) {

    private val viewModel: SplashViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            delay(3000)

            val result = viewModel.getRegisterStatus()

            if (result) {
                val userLoggedIn = viewModel.getUserConnectionStatus().data

                userLoggedIn?.let {
                    if (userLoggedIn) {
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                    }
                }

            } else {
                findNavController().navigate(R.id.action_splashFragment_to_introFragment)
            }
        }
    }
}