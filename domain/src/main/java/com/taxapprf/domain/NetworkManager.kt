package com.taxapprf.domain

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import javax.inject.Inject


class NetworkManager @Inject constructor(
    connectivityManager: ConnectivityManager,
) {
    private var _available = false
    val available
        get() = _available

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _available = true
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            _available = false
        }
    }

    init {
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
}