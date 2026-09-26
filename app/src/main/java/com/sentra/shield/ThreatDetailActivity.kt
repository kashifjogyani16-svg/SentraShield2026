package com.sentra.shield

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ThreatDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_threat_detail)

        val appLabel = intent.getStringExtra("appLabel") ?: "Unknown App"
        val packageName = intent.getStringExtra("packageName") ?: "-"
        val riskScore = intent.getIntExtra("riskScore", 0)
        val reason = intent.getStringExtra("reason") ?: "No reason provided"
        val rxBytes = intent.getLongExtra("rxBytes", 0)
        val txBytes = intent.getLongExtra("txBytes", 0)

        findViewById<TextView>(R.id.tvAppName).text = appLabel
        findViewById<TextView>(R.id.tvPackage).text = "Package: $packageName"
        findViewById<TextView>(R.id.tvRisk).text = "Risk Score: $riskScore / 100"
        findViewById<TextView>(R.id.tvReason).text = "Reason: $reason"
        findViewById<TextView>(R.id.tvData).text =
            "Data Sent (↑): ${txBytes / 1024} KB\nData Received (↓): ${rxBytes / 1024} KB"

        // Risk color
        val riskView = findViewById<TextView>(R.id.tvRisk)
        when {
            riskScore >= 70 -> riskView.setTextColor(0xFFFF5252.toInt())
            riskScore >= 50 -> riskView.setTextColor(0xFFFFB300.toInt())
            riskScore >= 30 -> riskView.setTextColor(0xFF00B4D8.toInt())
            else -> riskView.setTextColor(0xFF00E676.toInt())
        }
    }
}
