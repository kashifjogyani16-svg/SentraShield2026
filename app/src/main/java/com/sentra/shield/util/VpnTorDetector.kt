package com.sentra.shield.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.InetSocketAddress
import java.net.Socket

object VpnTorDetector {

    val TOR_PACKAGES = listOf(
        "org.torproject.android",
        "com.torproject.torbrowser",
        "net.i2p.android.router"
    )

    val VPN_PACKAGES = listOf(
        "com.wireguard.android",
        "de.blinkt.openvpn",
        "com.trm.tunnel",
        "com.nordvpn.android",
        "com.expressvpn.vpn",
        "com.surfshark.vpnclient.android",
        "com.psiphon3",
        "org.proxydroid"
    )

    data class DetectionResult(
        val torAppsInstalled: List<String>,
        val vpnAppsInstalled: List<String>,
        val vpnActiveViaConnectivityManager: Boolean,
        val torProxyReachable: Boolean
    ) {
        val anyDetected: Boolean
            get() = torAppsInstalled.isNotEmpty() || vpnAppsInstalled.isNotEmpty() ||
                vpnActiveViaConnectivityManager || torProxyReachable
    }

    fun detect(context: Context): DetectionResult {
        val pm = context.packageManager
        val installedTor = TOR_PACKAGES.filter { isInstalled(pm, it) }
        val installedVpn = VPN_PACKAGES.filter { isInstalled(pm, it) }
        val activeVpn = isVpnActive(context)
        val torReachable = isTorProxyReachable()

        return DetectionResult(
            torAppsInstalled = installedTor,
            vpnAppsInstalled = installedVpn,
            vpnActiveViaConnectivityManager = activeVpn,
            torProxyReachable = torReachable
        )
    }

    private fun isInstalled(pm: PackageManager, packageName: String): Boolean = try {
        pm.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private fun isVpnActive(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    /** Attempts a short-timeout TCP connection to the default local TOR SOCKS port. */
    private fun isTorProxyReachable(): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("127.0.0.1", 9050), 300)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
