package com.cars24.core.common.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

interface ConnectivityMonitor {
    val isOnline: Boolean
    fun observe(): Flow<Boolean>
}

class AndroidConnectivityMonitor(context: Context) : ConnectivityMonitor {

    private val connectivityManager = requireNotNull(context.getSystemService<ConnectivityManager>()) {
        "ConnectivityManager unavailable - cannot run without it"
    }

    override val isOnline: Boolean
        get() {
            val capabilities = connectivityManager.activeNetwork
                ?.let(connectivityManager::getNetworkCapabilities)
                ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

    override fun observe(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(isOnline)
            }

            override fun onLost(network: Network) {
                trySend(isOnline)
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(isOnline)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
        trySend(isOnline)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.conflate().distinctUntilChanged()
}
