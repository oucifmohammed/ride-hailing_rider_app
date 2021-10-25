package com.example.uberrider.presentation.homescreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.example.uberrider.R
import com.example.uberrider.databinding.FragmentHomeBinding
import com.example.uberrider.presentation.confirm_uber_bottom_sheet.ConfirmUberBottomSheetDialog
import com.example.uberrider.presentation.model.DriverResponseEvent
import com.example.uberrider.presentation.util.Constants.ACCEPT_RIDER_REQUEST
import com.example.uberrider.presentation.util.Constants.DECLINE_RIDER_REQUEST
import com.example.uberrider.presentation.util.Constants.DRIVER_ARRIVED
import com.example.uberrider.presentation.util.Constants.PERMISSION_LOCATION_REQUEST_CODE
import com.example.uberrider.presentation.util.Constants.PERMISSION_PHONE_CALL_REQUEST_CODE
import com.example.uberrider.util.Status
import com.example.uberrider.util.hideKeyboard
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback,
    EasyPermissions.PermissionCallbacks {


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var myMap: GoogleMap
    private var mapFragment: SupportMapFragment? = null
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var currentLocation: Location
    private lateinit var geocoder: Geocoder
    private lateinit var addressName: String
    private val markerList = HashMap<String, Marker>()

    private lateinit var polyLine: Polyline
    private lateinit var currentPositionCoordinates: LatLng

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var glide: RequestManager

    private lateinit var driverPhoneNumber: String

    private var destinationLatitude = 0.0
    private var destinationLongitude = 0.0

    private var driverComingShown = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentHomeBinding.bind(view)
        mapFragment = childFragmentManager.findFragmentById(binding.map.id) as SupportMapFragment?

        requestPermissionLocation()

        mapFragment?.getMapAsync(this)

        viewModel.connectionState.observe(viewLifecycleOwner, { result ->

            result.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.SUCCESS -> {
                        Snackbar.make(
                            mapFragment?.requireView()!!,
                            "You are online.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    Status.ERROR -> {
                        Snackbar.make(
                            mapFragment?.requireView()!!,
                            it.errorMessage!!,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    Status.LOADING -> {
                        // Do nothing
                    }
                }
            }
        })

        viewModel.availableDrivers.observe(viewLifecycleOwner, { drivers ->
            for (driver in drivers) {
                if (!markerList.containsKey(driver.key)) {
                    markerList[driver.key] = myMap.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                driver.value.latitude,
                                driver.value.longitude
                            )
                        )
                            .flat(true)
                            .title(driver.value.fullName)
                            .snippet(driver.value.phoneNumber)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                    )!!
                } else {
                    markerList[driver.key]?.position = LatLng(
                        driver.value.latitude,
                        driver.value.longitude
                    )
                }
            }
        })

        viewModel.getDisconnectDriver().observe(viewLifecycleOwner, {
            markerList[it]?.remove()
            markerList.remove(it)
        })

        viewModel.searchForDriversResult.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> {
                    //binding.findingRiderFooter.root.visibility = View.INVISIBLE
                }

                Status.ERROR -> {
                    binding.findingRiderFooter.root.visibility = View.INVISIBLE
                    Snackbar.make(requireView(), it.errorMessage!!, Snackbar.LENGTH_LONG).show()
                }

                Status.LOADING -> {
                    //Do nothing
                }
            }
        })

        setHasOptionsMenu(true)
    }

    @SuppressLint("MissingPermission")
    fun init() {
        locationRequest = LocationRequest.create().apply {
            smallestDisplacement = 50f
            interval = 5000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                val newPosition = LatLng(
                    locationResult.lastLocation.latitude,
                    locationResult.lastLocation.longitude
                )
                myMap.animateCamera(CameraUpdateFactory.newLatLng(newPosition))

                viewModel.connectRider(
                    locationResult.lastLocation.latitude,
                    locationResult.lastLocation.longitude
                )

                currentLocation = locationResult.lastLocation
                loadAvailableDrivers()

            }
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        geocoder = Geocoder(requireContext(), Locale.getDefault())
    }


    override fun onStart() {
        super.onStart()
        init()

        EventBus.getDefault().register(this)
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.getMainLooper()
            )

            lifecycleScope.launchWhenResumed {
                while (true) {
                    viewModel.getAvailableDrivers()
                    delay(5000)
                }
            }
        }
    }

    private fun hasLocationPermission() =
        EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    private fun hasPhoneCallPermission() =
        EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.CALL_PHONE
        )

    private fun requestPermissionLocation() {
        if (hasLocationPermission()) {
            return
        }

        EasyPermissions.requestPermissions(
            this,
            "You need to accept location permission to use this app.",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private fun requestPhoneCallLocation() {
        EasyPermissions.requestPermissions(
            this,
            "You need to accept phone call permission so you can the driver.",
            PERMISSION_PHONE_CALL_REQUEST_CODE,
            Manifest.permission.CALL_PHONE
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

        when (requestCode) {
            PERMISSION_LOCATION_REQUEST_CODE -> {
                enableUserLocation()
            }

            PERMISSION_PHONE_CALL_REQUEST_CODE -> {
                phoneCall()
            }
        }

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {

            if (requestCode == PERMISSION_PHONE_CALL_REQUEST_CODE) {
                requestPhoneCallLocation()
            } else if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
                requestPermissionLocation()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )
    }

    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {

        myMap.isMyLocationEnabled = true

        lifecycleScope.launchWhenResumed {
            val location = fusedLocationProviderClient.lastLocation.await()
            val latLng = LatLng(location.latitude, location.longitude)

            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

            viewModel.updateToken()
        }

        myMap.setOnMyLocationButtonClickListener {

            lifecycleScope.launchWhenResumed {
                val location = fusedLocationProviderClient.lastLocation.await()
                val latLng = LatLng(location.latitude, location.longitude)

                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
            }

            true
        }


    }

    override fun onMapReady(map: GoogleMap) {
        myMap = map
        myMap.uiSettings.isZoomControlsEnabled = true

        if (hasLocationPermission()) {
            enableUserLocation()
        }

        myMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.uber_maps_style
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun loadAvailableDrivers() {
        lifecycleScope.launchWhenResumed {
            try {

                val location = fusedLocationProviderClient.lastLocation.await()

                val addressList = withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                }

                addressName = addressList[0].locality

                viewModel.queryDrivers(
                    addressName,
                    location.latitude,
                    location.longitude,
                    loadDrivers = { loadAvailableDrivers() })

            } catch (e: Exception) {
                e.printStackTrace()
                if (e != NullPointerException()) {
                    Snackbar.make(
                        requireView(),
                        "We have a technical issue, please open the app later.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main, menu)

        val menuItem = menu.findItem(R.id.search)

        val searchView = menuItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Type your destination"

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return if (query!!.isEmpty()) {
                    Snackbar.make(
                        requireView(),
                        "Please write the destination before you submit.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    false
                } else {
                    lifecycleScope.launchWhenResumed {
                        if (searchDestination(destination = query))
                            MenuItemCompat.collapseActionView(menuItem)
                    }

                    true
                }

            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }
        })
    }

    @SuppressLint("MissingPermission")
    suspend fun searchDestination(destination: String): Boolean {

        try {
            val addressesList = withContext(Dispatchers.IO) {
                geocoder.getFromLocationName(destination, 1)
            }

            if (addressesList.size > 0) {
                val destinationAddress = addressesList[0]
                val destinationCoordinates = LatLng(
                    destinationAddress.latitude,
                    destinationAddress.longitude
                )

                destinationLatitude = destinationAddress.latitude
                destinationLongitude = destinationAddress.longitude

                markerList["destinationMarker"] = myMap.addMarker(
                    MarkerOptions().position(
                        destinationCoordinates
                    )
                        .flat(true)
                        .title(destination)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination))

                )!!

                currentPositionCoordinates = LatLng(
                    currentLocation.latitude, currentLocation.longitude
                )

                val polylineOptions =
                    PolylineOptions().add(currentPositionCoordinates, destinationCoordinates)

                polyLine = myMap.addPolyline(polylineOptions)

                val builder = LatLngBounds.Builder()
                builder.include(currentPositionCoordinates)
                builder.include(destinationCoordinates)
                val bounds = builder.build()

                requireContext().hideKeyboard(requireView())
                val cu = CameraUpdateFactory.newLatLngBounds(bounds, 50)
                myMap.animateCamera(cu)
                delay(1000)

                listenToUberConfirmationStatus()
                return true
            } else {
                Snackbar.make(
                    requireView(),
                    "Couldn't find this destination, please you make sure you write it correctly",
                    Snackbar.LENGTH_LONG
                ).show()

                requireContext().hideKeyboard(requireView())

                return false
            }

        } catch (e: IOException) {
            e.printStackTrace()

            val TAG = "Home_FRAGMENT"
            Log.e(TAG, e.message!!)
            Snackbar.make(
                requireView(),
                "We have a technical issue please try later",
                Snackbar.LENGTH_SHORT
            ).show()

            requireContext().hideKeyboard(requireView())

            return false
        }
    }

    private fun listenToUberConfirmationStatus() {
        val bottomSheet = ConfirmUberBottomSheetDialog()
        bottomSheet.show(childFragmentManager, "Bottom sheet dialog")

        childFragmentManager.setFragmentResultListener(
            "uber_confirmation_state",
            viewLifecycleOwner
        ) { key, bundle ->

            val confirmationState = bundle.getString("state")

            if (confirmationState == "Cancel") {
                markerList["destinationMarker"]?.remove()
                markerList.remove("destinationMarker")
                polyLine.remove()

                myMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentPositionCoordinates,
                        18f
                    )
                )
            }

            if (confirmationState == "Confirm") {

                markerList["destinationMarker"]?.remove()
                markerList.remove("destinationMarker")
                polyLine.remove()

                myMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentPositionCoordinates,
                        18f
                    )
                )

                binding.findingRiderFooter.root.visibility = View.VISIBLE

                viewModel.searchForDrivers(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    destinationLatitude,
                    destinationLongitude
                )
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onDriverResponse(event: DriverResponseEvent) {

        if (event.response == DECLINE_RIDER_REQUEST) {
            viewModel.addToDeclineDrivers(event.driverId)
            viewModel.searchForDrivers(
                currentLocation.latitude,
                currentLocation.longitude,
                destinationLatitude,
                destinationLongitude
            )
        }

        if (event.response == ACCEPT_RIDER_REQUEST) {
            binding.findingRiderFooter.root.visibility = View.INVISIBLE

            driverPhoneNumber = event.phoneNumber
            binding.driverAcceptFooter.riderName.text = event.driverName
            glide.load(event.profilePhoto).into(binding.driverAcceptFooter.profileImage)


            binding.driverAcceptFooter.root.visibility = View.VISIBLE

            if(!driverComingShown) {
                Snackbar.make(
                    requireView(),
                    "We got a driver for you, he will be coming soon.",
                    Snackbar.LENGTH_LONG
                ).show()

                driverComingShown = true
            }

            binding.driverAcceptFooter.callIcon.setOnClickListener {
                if (hasPhoneCallPermission()) {
                    phoneCall()
                } else {
                    requestPhoneCallLocation()
                }
            }
        }

        if(event.response == DRIVER_ARRIVED) {
            binding.driverAcceptFooter.root.visibility = View.INVISIBLE
        }
    }

    private fun phoneCall() {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${driverPhoneNumber}"))
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        viewModel.disconnectRider()

        if (EventBus.getDefault().hasSubscriberForEvent(DriverResponseEvent::class.java)) {
            EventBus.getDefault().removeStickyEvent(DriverResponseEvent::class.java)
        }
    }
}