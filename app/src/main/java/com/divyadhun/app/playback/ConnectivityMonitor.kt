package com.divyadhun.app.playback

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Lightweight process-local connectivity signal used to keep retry UX honest. */
class ConnectivityMonitor(context: Context) {
    private val manager = context.applicationContext.getSystemService(ConnectivityManager::class.java)
    private val mutableAvailable = MutableStateFlow(currentAvailability())
    val available: StateFlow<Boolean> = mutableAvailable.asStateFlow()
    private var registered = false

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { mutableAvailable.value = true }
        override fun onLost(network: Network) { mutableAvailable.value = currentAvailability() }
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            mutableAvailable.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }

    fun start() {
        if (registered) return
        runCatching { manager.registerDefaultNetworkCallback(callback); registered = true }
    }

    fun stop() {
        if (!registered) return
        runCatching { manager.unregisterNetworkCallback(callback) }
        registered = false
    }

    private fun currentAvailability(): Boolean {
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
