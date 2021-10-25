package com.example.uberrider.presentation.profilescreen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.RequestManager
import com.example.uberrider.R
import com.example.uberrider.databinding.ProfileFragmentLayoutBinding
import com.example.uberrider.presentation.StartingActivity
import com.example.uberrider.presentation.re_authenticate_bottom_sheet_dialog.ReAuthenticateBottomSheetDialog
import com.example.uberrider.presentation.util.CropActivityResultContract
import com.example.uberrider.util.Status
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment: Fragment(R.layout.profile_fragment_layout){

    private var _binding: ProfileFragmentLayoutBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var glide: RequestManager
    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>
    private lateinit var currentEmail: String
    private lateinit var currentPhoneNumber: String
    private var currentUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ProfileFragmentLayoutBinding.bind(view)
        viewModel.getRiderProfile()

        cropActivityResultLauncher = registerForActivityResult(CropActivityResultContract(1, 1)) {
            it?.let {
                binding.accountImage.setImageURI(it)

                currentUri = it
            }
        }

        binding.accountImage.setOnClickListener {
            cropActivityResultLauncher.launch(null)
        }

        viewModel.profileData.observe(viewLifecycleOwner, { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    binding.profileProgressBar.visibility = View.INVISIBLE
                    binding.apply {
                        accountName.visibility = View.VISIBLE
                        emailTextInput.visibility = View.VISIBLE
                        phoneNumberTextInput.visibility = View.VISIBLE
                        updateButton.visibility = View.VISIBLE
                        logoutButton.visibility = View.VISIBLE
                        accountImage.visibility = View.VISIBLE
                    }

                    binding.apply {
                        glide.load(result.data?.profilePhoto).into(accountImage)
                        accountName.text = result.data?.fullName
                        emailEditText.setText(result.data?.email)
                        phoneNumberEditText.setText(result.data?.phoneNumber)
                    }

                    currentEmail = binding.emailEditText.text.toString()
                    currentPhoneNumber = binding.phoneNumberEditText.text.toString()
                }
                Status.ERROR -> {
                    binding.profileProgressBar.visibility = View.INVISIBLE

                    Snackbar.make(
                        view,
                        "We have a technical issue, please try later.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                Status.LOADING -> {
                    binding.apply {
                        accountName.visibility = View.INVISIBLE
                        emailTextInput.visibility = View.INVISIBLE
                        phoneNumberTextInput.visibility = View.INVISIBLE
                        updateButton.visibility = View.INVISIBLE
                        logoutButton.visibility = View.INVISIBLE
                        accountImage.visibility = View.INVISIBLE
                    }
                    binding.profileProgressBar.visibility = View.VISIBLE
                }
            }
        })

        binding.logoutButton.setOnClickListener {
            viewModel.signOut()

            startActivity(Intent(requireActivity(), StartingActivity::class.java))

            requireActivity().finish()
        }

        binding.updateButton.setOnClickListener {
            val lastEmail = binding.emailEditText.text.toString()
            val lastPhoneNumber = binding.phoneNumberEditText.text.toString()

            if (lastEmail == currentEmail && lastPhoneNumber == currentPhoneNumber && currentUri == null) {
                Snackbar.make(view, "There is nothing to update.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bottomSheet = ReAuthenticateBottomSheetDialog()
            bottomSheet.show(childFragmentManager, "Bottom sheet dialog")

            childFragmentManager.setFragmentResultListener(
                "userCredentials",
                viewLifecycleOwner
            ) { key,bundle ->
                val credentialEmail = bundle.getString("currentEmail")!!
                val credentialPassword = bundle.getString("password")!!

                if (currentUri == null) {
                    viewModel.editProfile(credentialEmail, credentialPassword, lastEmail, lastPhoneNumber, null)
                } else {
                    viewModel.editProfile(credentialEmail, credentialPassword, lastEmail, lastPhoneNumber, currentUri.toString())
                }
            }

        }

        viewModel.editProfileResult.observe(viewLifecycleOwner, { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    binding.updateProgressBar.visibility = View.INVISIBLE
                    binding.updateButton.visibility = View.VISIBLE

                    currentUri = null
                    currentEmail = binding.emailEditText.text.toString()
                    currentPhoneNumber = binding.phoneNumberEditText.text.toString()

                    Snackbar.make(
                        view,
                        "Your account has been updated successfully",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                Status.ERROR -> {
                    binding.updateProgressBar.visibility = View.INVISIBLE
                    binding.updateButton.visibility = View.VISIBLE

                    Snackbar.make(view, result.errorMessage!!, Snackbar.LENGTH_SHORT).show()
                }

                Status.LOADING -> {
                    binding.updateButton.visibility = View.INVISIBLE
                    binding.updateProgressBar.visibility = View.VISIBLE
                }
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}