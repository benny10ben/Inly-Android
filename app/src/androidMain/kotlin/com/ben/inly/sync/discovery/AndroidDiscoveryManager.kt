package com.ben.inly.sync.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class AndroidDiscoveryManager(context: Context) : SyncDiscoveryManager {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    override val discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())

    private val serviceType = "_inlysync._tcp."

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {}
        override fun onServiceFound(service: NsdServiceInfo) {
            nsdManager.resolveService(service, resolveListener)
        }
        override fun onServiceLost(service: NsdServiceInfo) {}
        override fun onDiscoveryStopped(serviceType: String) {}
        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            nsdManager.stopServiceDiscovery(this)
        }
        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            nsdManager.stopServiceDiscovery(this)
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            val ip = serviceInfo.host.hostAddress ?: return
            val newDevice = DiscoveredDevice(serviceInfo.serviceName, ip, serviceInfo.port)

            discoveredDevices.update { current ->
                if (current.none { it.ipAddress == ip }) current + newDevice else current
            }
        }
    }

    override fun startScanning() {
        discoveredDevices.value = emptyList()
        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    override fun stopScanning() {
        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
        } catch (e: Exception) {
            // Listener wasn't registered
        }
    }

    override fun startBroadcasting(port: Int, deviceName: String) { /* Not needed for Mobile */ }
    override fun stopBroadcasting() { /* Not needed for Mobile */ }
}