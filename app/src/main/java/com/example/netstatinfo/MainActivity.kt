package com.example.netstatinfo

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import android.Manifest
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.netstatinfo.R

class MainActivity : ComponentActivity() {

    private lateinit var telephonyManager: TelephonyManager

    // listening to signal strength
    private val signalStrengthCallback =
        object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                val signalStrengthValue = signalStrength.level
                var signalStrengthStatus = "Unknown"
                when (signalStrengthValue) {
                    1 -> signalStrengthStatus = "poor"
                    2 -> signalStrengthStatus = "moderate"
                    3 -> signalStrengthStatus = "good"
                    4 -> signalStrengthStatus = "great"
                    else -> signalStrengthStatus = "Unknown"
                }

                Toast.makeText(
                    applicationContext,
                    "Signal Strength: $signalStrengthValue - $signalStrengthStatus",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            // Register the signal strength callback
            registerSignalStrengthCallback()
            setContent {
                Supported5GTypes(this)
            }
        }

    }


    // req perm
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_PHONE_STATE] == true &&
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

        if (granted) {
            registerSignalStrengthCallback()
            setContent { Supported5GTypes(this) }
        } else {
            Toast.makeText(
                this,
                "Permissions are required to access 5G information and signal strength.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // reg signal callback
    private fun registerSignalStrengthCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.registerTelephonyCallback(mainExecutor, signalStrengthCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.unregisterTelephonyCallback(signalStrengthCallback)
        }
    }


}


@Composable
fun Supported5GTypes(context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp, start = 50.dp, end = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(id = R.string.supported_5g_types),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        )
        val supported5GTypes = getSupported5GTypes(context)
        if (supported5GTypes.isNotEmpty()) {
            supported5GTypes.forEach { type ->
                Text(text = type)
            }
        } else {
            Text(text = stringResource(id = R.string.no_5g_support))
        }
    }
}

fun getSupported5GTypes(context: Context): List<String> {
    val supported5GTypes = mutableListOf<String>()

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val serviceState = telephonyManager.serviceState

            if (serviceState != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    when (telephonyManager.dataNetworkType) {
                        TelephonyManager.NETWORK_TYPE_1xRTT -> supported5GTypes.add("1xRTT (2G CDMA)")
                        TelephonyManager.NETWORK_TYPE_CDMA -> supported5GTypes.add("CDMA (2G)")
                        TelephonyManager.NETWORK_TYPE_EDGE -> supported5GTypes.add("EDGE (2.75G)")
                        TelephonyManager.NETWORK_TYPE_EHRPD -> supported5GTypes.add("eHRPD (3G)")
                        TelephonyManager.NETWORK_TYPE_EVDO_0 -> supported5GTypes.add("EVDO revision 0 (3G)")
                        TelephonyManager.NETWORK_TYPE_EVDO_A -> supported5GTypes.add("EVDO revision A (3G)")
                        TelephonyManager.NETWORK_TYPE_EVDO_B -> supported5GTypes.add("EVDO revision B (3G)")
                        TelephonyManager.NETWORK_TYPE_GPRS -> supported5GTypes.add("GPRS (2.5G)")
                        TelephonyManager.NETWORK_TYPE_GSM -> supported5GTypes.add("GSM (2G)")
                        TelephonyManager.NETWORK_TYPE_HSDPA -> supported5GTypes.add("HSDPA (3.5G)")
                        TelephonyManager.NETWORK_TYPE_HSPA -> supported5GTypes.add("HSPA (3G)")
                        TelephonyManager.NETWORK_TYPE_HSPAP -> supported5GTypes.add("HSPA+ (3.5G)")
                        TelephonyManager.NETWORK_TYPE_HSUPA -> supported5GTypes.add("HSUPA (3.5G)")
                        TelephonyManager.NETWORK_TYPE_IWLAN -> supported5GTypes.add("IWLAN")
                        TelephonyManager.NETWORK_TYPE_LTE -> supported5GTypes.add("LTE (4G or could be NSA-non-standalone)")
                        TelephonyManager.NETWORK_TYPE_NR -> supported5GTypes.add("5G NR (This will only be returned for 5G SA)")
                        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> supported5GTypes.add("TD-SCDMA (3G)")
                        TelephonyManager.NETWORK_TYPE_UMTS -> supported5GTypes.add("UMTS (3G)")
                        TelephonyManager.NETWORK_TYPE_UNKNOWN -> supported5GTypes.add("Network type is unknown")
                        else -> supported5GTypes.add("Unknown or unsupported network type")
                    }
                }
            }
        }
    }

    return supported5GTypes
}
