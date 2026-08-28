package ing.ipcheck.netscope

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsEthernet
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.VpnLock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.net.URLEncoder
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private enum class ThemePreference { SYSTEM, LIGHT, DARK }

private data class AppPalette(
    val background: Color,
    val surface: Color,
    val ink: Color,
    val mutedInk: Color,
    val blue: Color,
    val green: Color,
    val amber: Color,
    val red: Color,
    val softBlue: Color,
    val softGreen: Color,
    val softAmber: Color,
    val border: Color
)

private val LightPalette = AppPalette(
    background = Color(0xFFF4F6F8), surface = Color(0xFFFFFFFF), ink = Color(0xFF15232D), mutedInk = Color(0xFF66757F),
    blue = Color(0xFF1E88E5), green = Color(0xFF16A36A), amber = Color(0xFFF2A51A), red = Color(0xFFE05252),
    softBlue = Color(0xFFEAF4FF), softGreen = Color(0xFFE8F8F0), softAmber = Color(0xFFFFF5DC), border = Color(0xFFE3E9ED)
)

private val DarkPalette = AppPalette(
    background = Color(0xFF101417), surface = Color(0xFF192227), ink = Color(0xFFE7EEF2), mutedInk = Color(0xFFB5C3CB),
    blue = Color(0xFF8CC7FF), green = Color(0xFF84DBAE), amber = Color(0xFFFFD16E), red = Color(0xFFFFA3A3),
    softBlue = Color(0xFF16354B), softGreen = Color(0xFF173C2C), softAmber = Color(0xFF473818), border = Color(0xFF34434C)
)

private val LocalAppPalette = staticCompositionLocalOf { LightPalette }

private val AppBackground: Color @Composable get() = LocalAppPalette.current.background
private val CardSurface: Color @Composable get() = LocalAppPalette.current.surface
private val Ink: Color @Composable get() = LocalAppPalette.current.ink
private val MutedInk: Color @Composable get() = LocalAppPalette.current.mutedInk
private val Blue: Color @Composable get() = LocalAppPalette.current.blue
private val Green: Color @Composable get() = LocalAppPalette.current.green
private val Amber: Color @Composable get() = LocalAppPalette.current.amber
private val Red: Color @Composable get() = LocalAppPalette.current.red
private val SoftBlue: Color @Composable get() = LocalAppPalette.current.softBlue
private val SoftGreen: Color @Composable get() = LocalAppPalette.current.softGreen
private val SoftAmber: Color @Composable get() = LocalAppPalette.current.softAmber
private val Border: Color @Composable get() = LocalAppPalette.current.border

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NetScopeRoot() }
    }
}

@Composable
private fun NetScopeRoot() {
    val context = LocalContext.current
    var themePreference by remember {
        mutableStateOf(runCatching { SecureApiKeyStore.loadThemePreference(context) }.getOrDefault(ThemePreference.SYSTEM))
    }
    NetScopeTheme(themePreference) {
        NetScopeApp(
            themePreference = themePreference,
            onThemePreferenceChange = { selected ->
                runCatching { SecureApiKeyStore.saveThemePreference(context, selected) }
                    .onSuccess { themePreference = selected }
            }
        )
    }
}

@Composable
private fun NetScopeTheme(themePreference: ThemePreference, content: @Composable () -> Unit) {
    val useDarkPalette = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    val palette = if (useDarkPalette) DarkPalette else LightPalette
    val colorScheme = if (useDarkPalette) {
        darkColorScheme(
            primary = palette.blue, secondary = palette.green, surface = palette.surface, background = palette.background,
            onBackground = palette.ink, onSurface = palette.ink, outline = palette.border
        )
    } else {
        lightColorScheme(
            primary = palette.blue, secondary = palette.green, surface = palette.surface, background = palette.background,
            onBackground = palette.ink, onSurface = palette.ink, outline = palette.border
        )
    }
    MaterialTheme(colorScheme = colorScheme) {
        CompositionLocalProvider(LocalAppPalette provides palette, content = content)
    }
}

private data class IpSnapshot(
    val ipv4: String,
    val ipv6: String?,
    val country: String,
    val region: String,
    val city: String,
    val timezone: String,
    val isp: String,
    val asn: String,
    val networkType: String,
    val refreshedAt: String
)

private data class IpHistoryEntry(
    val ip: String,
    val country: String,
    val city: String,
    val asn: String,
    val networkType: String,
    val seenAt: String,
    val source: String
)

private enum class CheckStatus { IDLE, RUNNING, SUCCESS, FAILURE }

private data class EndpointResult(
    val name: String,
    val host: String,
    val url: String,
    val status: CheckStatus = CheckStatus.IDLE,
    val latencyMs: Long? = null,
    val detail: String = "等待检测"
)

private data class PrivacySnapshot(
    val vpnActive: Boolean,
    val privateDnsMode: String,
    val dnsServers: List<String>,
    val transport: String,
    val validated: Boolean,
    val metered: Boolean,
    val interfaceName: String,
    val privateDnsHostname: String,
    val downstreamKbps: Int,
    val upstreamKbps: Int
)

private data class SecurityChecklistItem(
    val id: String,
    val category: String,
    val title: String,
    val guidance: String
)

private data class DnsResolverResult(
    val resolver: String,
    val addresses: List<String>,
    val status: String,
    val error: String? = null
)

private data class DnsLookupResult(
    val host: String,
    val recordType: String = "A",
    val addresses: List<String>,
    val resolverResults: List<DnsResolverResult> = emptyList(),
    val error: String? = null
)

private enum class DnsConsensusTone { NEUTRAL, POSITIVE, NOTICE }

private data class DnsConsensus(
    val label: String,
    val detail: String,
    val tone: DnsConsensusTone
)

private data class WhoisLookupResult(
    val query: String,
    val registry: String,
    val lines: List<String>,
    val error: String? = null
)

private data class AsnNeighbour(
    val asn: String,
    val position: String,
    val pathCount: Int?
)

private data class AsnLookupResult(
    val asn: String,
    val name: String,
    val description: String,
    val countryCode: String,
    val website: String,
    val allocatedAt: String,
    val neighbours: List<AsnNeighbour> = emptyList(),
    val error: String? = null
)

private data class MacLookupResult(
    val mac: String,
    val vendor: String,
    val isLocallyAdministered: Boolean,
    val error: String? = null
)

private data class GlobalPingProbeResult(
    val location: String,
    val network: String,
    val averageMs: Double?,
    val packetLoss: Double?,
    val status: String
)

private data class GlobalPingResult(
    val target: String,
    val measurementId: String,
    val probes: List<GlobalPingProbeResult>,
    val status: String,
    val error: String? = null
)

private data class MtrHop(
    val number: Int,
    val host: String,
    val ip: String,
    val asn: String,
    val lossPct: Double?,
    val lastMs: Double?,
    val avgMs: Double?,
    val worstMs: Double?
)

private data class MtrProbeResult(
    val location: String,
    val network: String,
    val asn: String,
    val status: String,
    val hops: List<MtrHop>
)

private data class MtrResult(
    val target: String,
    val measurementId: String,
    val status: String,
    val probes: List<MtrProbeResult>,
    val error: String? = null
)

private data class OoniMeasurementResult(
    val input: String,
    val country: String,
    val asn: String,
    val measuredAt: String,
    val anomaly: Boolean,
    val confirmed: Boolean,
    val failure: Boolean,
    val verification: String
)

private data class OoniQueryResult(
    val domain: String,
    val country: String,
    val count: Int,
    val measurements: List<OoniMeasurementResult>,
    val error: String? = null
)

private data class PortProbeResult(
    val host: String,
    val port: Int,
    val status: CheckStatus,
    val latencyMs: Long? = null,
    val detail: String = "等待检测"
)

private data class OfficialStatusResult(
    val name: String,
    val endpoint: String,
    val statusPage: String,
    val indicator: String = "unknown",
    val description: String = "尚未查询",
    val updatedAt: String = "",
    val error: String? = null
)

private data class NetworkSpeedResult(
    val latencyMs: Long,
    val jitterMs: Double,
    val downloadMbps: Double,
    val downloadedBytes: Long
)

private enum class PurityTone { CONSISTENT, NOTICE, NEUTRAL }

private data class PuritySignal(
    val title: String,
    val value: String,
    val detail: String,
    val tone: PurityTone
)

private data class PurityRiskBucket(
    val title: String,
    val value: String,
    val detail: String,
    val tone: PurityTone
)

private data class PurityReport(
    val score: Double,
    val risk: Double,
    val abuseRisk: Double,
    val transparencyRisk: Double,
    val coverage: Double,
    val coverageLabel: String,
    val coverageDetail: String,
    val label: String,
    val summary: String,
    val buckets: List<PurityRiskBucket>,
    val signals: List<PuritySignal>,
    val checkedAt: String
)

private fun formatRisk(value: Double): String = String.format(java.util.Locale.US, "%.1f", value)

private fun isHttpsEndpoint(value: String): Boolean {
    if (value.isBlank()) return true
    val uri = runCatching { Uri.parse(value.trim()) }.getOrNull() ?: return false
    return uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
}

private fun isIpLiteral(value: String): Boolean {
    val normalized = value.trim().removePrefix("[").removeSuffix("]")
    if (normalized.isBlank() || normalized.any { !(it.isDigit() || it in ".:") }) return false
    return runCatching {
        val address = InetAddress.getByName(normalized)
        !address.hostAddress.isNullOrBlank()
    }.getOrDefault(false)
}

private fun isValidProbeHost(value: String): Boolean {
    val host = value.trim().removePrefix("[").removeSuffix("]")
    if (host.isBlank() || host.length > 253 || host.any { it.isWhitespace() || it == '/' || it == '@' }) return false
    return isIpLiteral(host) || host.matches(Regex("^[A-Za-z0-9](?:[A-Za-z0-9.-]{0,251}[A-Za-z0-9])?$"))
}

private data class PublicGeoProbe(
    val source: String,
    val ip: String,
    val countryCode: String,
    val country: String,
    val asn: String,
    val organization: String
)

private data class RiskIntelligence(
    val source: String,
    val proxy: Boolean?,
    val vpn: Boolean?,
    val tor: Boolean?,
    val hosting: Boolean?,
    val compromised: Boolean?,
    val scraper: Boolean?,
    val anonymous: Boolean?,
    val risk: Int?,
    val confidence: Int?,
    val detectionFirstSeen: String,
    val detectionLastSeen: String,
    val attackEventCount: Int,
    val attackHistoryPresent: Boolean,
    val attackSummary: String
)

private data class ApiKeyConfig(
    val abuseIpDbKey: String = "",
    val ipApiKey: String = "",
    val maxMindAccountId: String = "",
    val maxMindLicenseKey: String = "",
    val ipHubKey: String = "",
    val customKey: String = "",
    val customEndpoint: String = ""
)

private data class AbuseIpDbRisk(
    val confidenceScore: Int?,
    val totalReports: Int?,
    val distinctUsers: Int?,
    val isTor: Boolean?,
    val usageType: String,
    val lastReportedAt: String
)

private data class IpApiIsSecurity(
    val isDatacenter: Boolean?,
    val isProxy: Boolean?,
    val isVpn: Boolean?,
    val isTor: Boolean?,
    val isAbuser: Boolean?,
    val isCrawler: Boolean?,
    val hasManagedEgress: Boolean,
    val egressSummary: String,
    val companyName: String,
    val asnOrganization: String
)

private data class MaxMindInsights(
    val isAnonymous: Boolean?,
    val isAnonymousVpn: Boolean?,
    val isHostingProvider: Boolean?,
    val isPublicProxy: Boolean?,
    val isResidentialProxy: Boolean?,
    val isTorExitNode: Boolean?,
    val anonymizerConfidence: Int?,
    val network: String,
    val asn: String,
    val organization: String,
    val isp: String,
    val connectionType: String
)

private data class IpHubRisk(
    val block: Int?,
    val blockReason: String,
    val isProxy: Boolean?,
    val isTor: Boolean?,
    val isHosting: Boolean?,
    val isRelay: Boolean?,
    val isResidentialProxy: Boolean?,
    val asn: String,
    val isp: String,
    val countryCode: String
)

private object SecureApiKeyStore {
    private const val PreferencesName = "secure_api_keys"
    private const val KeyAlias = "netscope_api_key_encryption"
    private const val AbuseKey = "abuseipdb"
    private const val IpApiIsKey = "ipapi_is"
    private const val MaxMindAccountIdKey = "maxmind_account_id"
    private const val MaxMindLicenseKey = "maxmind_license_key"
    private const val IpHubKey = "iphub"
    private const val LegacyIpApiComKey = "ipapi"
    private const val CustomKey = "custom"
    private const val CustomEndpoint = "custom_endpoint"
    private const val IpHistoryKey = "ip_history"
    private const val ConnectivityEndpointsKey = "connectivity_endpoints"
    private const val PortProbeTargetsKey = "port_probe_targets"
    private const val SecurityChecklistProgressKey = "security_checklist_progress"
    private const val ThemePreferenceKey = "theme_preference"

    fun loadThemePreference(context: Context): ThemePreference = runCatching {
        val value = decrypt(context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).getString(ThemePreferenceKey, null)).orEmpty()
        ThemePreference.values().firstOrNull { it.name == value } ?: ThemePreference.SYSTEM
    }.getOrDefault(ThemePreference.SYSTEM)

    fun saveThemePreference(context: Context, preference: ThemePreference) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit()
            .putString(ThemePreferenceKey, encrypt(preference.name))
            .apply()
    }

    fun load(context: Context): ApiKeyConfig {
        val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
        return ApiKeyConfig(
            abuseIpDbKey = decrypt(preferences.getString(AbuseKey, null)).orEmpty(),
            ipApiKey = decrypt(preferences.getString(IpApiIsKey, null)).orEmpty(),
            maxMindAccountId = decrypt(preferences.getString(MaxMindAccountIdKey, null)).orEmpty(),
            maxMindLicenseKey = decrypt(preferences.getString(MaxMindLicenseKey, null)).orEmpty(),
            ipHubKey = decrypt(preferences.getString(IpHubKey, null)).orEmpty(),
            customKey = decrypt(preferences.getString(CustomKey, null)).orEmpty(),
            customEndpoint = decrypt(preferences.getString(CustomEndpoint, null)).orEmpty()
        )
    }

    fun save(context: Context, config: ApiKeyConfig) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit()
            .putString(AbuseKey, config.abuseIpDbKey.takeIf { it.isNotBlank() }?.let(::encrypt))
            .putString(IpApiIsKey, config.ipApiKey.takeIf { it.isNotBlank() }?.let(::encrypt))
            .putString(MaxMindAccountIdKey, config.maxMindAccountId.takeIf { it.isNotBlank() }?.let(::encrypt))
            .putString(MaxMindLicenseKey, config.maxMindLicenseKey.takeIf { it.isNotBlank() }?.let(::encrypt))
            .putString(IpHubKey, config.ipHubKey.takeIf { it.isNotBlank() }?.let(::encrypt))
            .remove(LegacyIpApiComKey)
            .putString(CustomKey, config.customKey.takeIf { it.isNotBlank() }?.let(::encrypt))
            .putString(CustomEndpoint, config.customEndpoint.takeIf { it.isNotBlank() }?.let(::encrypt))
            .apply()
    }

    fun loadIpHistory(context: Context): List<IpHistoryEntry> = runCatching {
        val encrypted = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).getString(IpHistoryKey, null)
        val array = JSONArray(decrypt(encrypted).orEmpty())
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val ip = item.stringOrBlank("ip")
                if (ip.isBlank()) continue
                add(
                    IpHistoryEntry(
                        ip = ip,
                        country = item.stringOrBlank("country"),
                        city = item.stringOrBlank("city"),
                        asn = item.stringOrBlank("asn"),
                        networkType = item.stringOrBlank("networkType"),
                        seenAt = item.stringOrBlank("seenAt"),
                        source = item.stringOrBlank("source")
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    fun recordIpHistory(context: Context, snapshot: IpSnapshot, source: String): List<IpHistoryEntry> {
        val entry = IpHistoryEntry(
            ip = snapshot.ipv4,
            country = snapshot.country,
            city = snapshot.city,
            asn = snapshot.asn,
            networkType = snapshot.networkType,
            seenAt = snapshot.refreshedAt,
            source = source
        )
        val history = (listOf(entry) + loadIpHistory(context).filter { it.ip != entry.ip || it.source != entry.source })
            .take(30)
        val serialized = JSONArray().apply {
            history.forEach { item ->
                put(JSONObject().apply {
                    put("ip", item.ip)
                    put("country", item.country)
                    put("city", item.city)
                    put("asn", item.asn)
                    put("networkType", item.networkType)
                    put("seenAt", item.seenAt)
                    put("source", item.source)
                })
            }
        }
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit()
            .putString(IpHistoryKey, encrypt(serialized.toString()))
            .apply()
        return history
    }

    fun clearIpHistory(context: Context) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit().remove(IpHistoryKey).apply()
    }

    fun loadConnectivityEndpoints(context: Context): List<EndpointResult> = runCatching {
        val encrypted = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).getString(ConnectivityEndpointsKey, null)
        val array = JSONArray(decrypt(encrypted).orEmpty())
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val name = item.stringOrBlank("name")
                val url = item.stringOrBlank("url")
                val host = item.stringOrBlank("host")
                if (name.isNotBlank() && url.isNotBlank() && host.isNotBlank() && isHttpsEndpoint(url)) {
                    add(EndpointResult(name = name, host = host, url = url))
                }
            }
        }.take(12)
    }.getOrDefault(emptyList())

    fun saveConnectivityEndpoints(context: Context, endpoints: List<EndpointResult>) {
        val sanitized = endpoints.take(12).map { it.copy(status = CheckStatus.IDLE, latencyMs = null, detail = "等待检测") }
        val serialized = JSONArray().apply {
            sanitized.forEach { endpoint ->
                put(JSONObject().apply {
                    put("name", endpoint.name)
                    put("host", endpoint.host)
                    put("url", endpoint.url)
                })
            }
        }
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit()
            .putString(ConnectivityEndpointsKey, encrypt(serialized.toString()))
            .apply()
    }

    fun clearConnectivityEndpoints(context: Context) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit().remove(ConnectivityEndpointsKey).apply()
    }

    fun loadPortProbeTargets(context: Context): List<PortProbeResult> = runCatching {
        val encrypted = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).getString(PortProbeTargetsKey, null)
        val array = JSONArray(decrypt(encrypted).orEmpty())
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val host = item.stringOrBlank("host")
                val port = item.intOrNull("port") ?: -1
                if (isValidProbeHost(host) && port in 1..65535) add(PortProbeResult(host, port, CheckStatus.IDLE))
            }
        }.distinctBy { "${it.host.lowercase()}:${it.port}" }.take(12)
    }.getOrDefault(emptyList())

    fun savePortProbeTargets(context: Context, probes: List<PortProbeResult>) {
        val sanitized = probes.take(12).map { it.copy(status = CheckStatus.IDLE, latencyMs = null, detail = "等待探测") }
        val array = JSONArray().apply {
            sanitized.forEach { probe -> put(JSONObject().put("host", probe.host).put("port", probe.port)) }
        }
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit()
            .putString(PortProbeTargetsKey, encrypt(array.toString()))
            .apply()
    }

    fun clearPortProbeTargets(context: Context) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit().remove(PortProbeTargetsKey).apply()
    }

    fun loadSecurityChecklistProgress(context: Context): Set<String> = runCatching {
        val encrypted = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).getString(SecurityChecklistProgressKey, null)
        val array = JSONArray(decrypt(encrypted).orEmpty())
        buildSet {
            for (index in 0 until array.length()) {
                array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }.getOrDefault(emptySet())

    fun saveSecurityChecklistProgress(context: Context, completedIds: Set<String>) {
        val serialized = JSONArray().apply { completedIds.sorted().forEach(::put) }
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit()
            .putString(SecurityChecklistProgressKey, encrypt(serialized.toString()))
            .apply()
    }

    fun clearSecurityChecklistProgress(context: Context) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit().remove(SecurityChecklistProgressKey).apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit()
            .remove(AbuseKey)
            .remove(IpApiIsKey)
            .remove(MaxMindAccountIdKey)
            .remove(MaxMindLicenseKey)
            .remove(IpHubKey)
            .remove(LegacyIpApiComKey)
            .remove(CustomKey)
            .remove(CustomEndpoint)
            .apply()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(value: String?): String? = runCatching {
        if (value.isNullOrBlank()) return@runCatching null
        val parts = value.split(":", limit = 2)
        if (parts.size != 2) return@runCatching null
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP))
        )
        String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), Charsets.UTF_8)
    }.getOrNull()

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KeyAlias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    KeyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setKeySize(256)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
        }.generateKey()
    }
}

private val DefaultEndpoints = listOf(
    EndpointResult("Google", "google.com", "https://www.google.com/generate_204"),
    EndpointResult("GitHub", "github.com", "https://github.com"),
    EndpointResult("Cloudflare", "cloudflare.com", "https://www.cloudflare.com/cdn-cgi/trace"),
    EndpointResult("ChatGPT", "chatgpt.com", "https://chatgpt.com"),
    EndpointResult("YouTube", "youtube.com", "https://www.youtube.com/generate_204"),
    EndpointResult("Wikipedia", "wikipedia.org", "https://www.wikipedia.org")
)

private val ProductivityEndpointPreset = listOf(
    EndpointResult("Microsoft 365", "microsoft.com", "https://www.microsoft.com"),
    EndpointResult("Slack", "slack.com", "https://slack.com"),
    EndpointResult("Notion", "notion.so", "https://www.notion.so"),
    EndpointResult("Atlassian", "atlassian.com", "https://www.atlassian.com"),
    EndpointResult("Dropbox", "dropbox.com", "https://www.dropbox.com"),
    EndpointResult("Zoom", "zoom.us", "https://zoom.us"),
    EndpointResult("Figma", "figma.com", "https://www.figma.com"),
    EndpointResult("GitLab", "gitlab.com", "https://gitlab.com"),
    EndpointResult("npm", "npmjs.com", "https://www.npmjs.com"),
    EndpointResult("PyPI", "pypi.org", "https://pypi.org"),
    EndpointResult("AWS", "aws.amazon.com", "https://aws.amazon.com"),
    EndpointResult("Docker Hub", "docker.com", "https://hub.docker.com")
)

private val DefaultPortProbes = listOf(
    PortProbeResult("github.com", 443, CheckStatus.IDLE),
    PortProbeResult("api.openai.com", 443, CheckStatus.IDLE),
    PortProbeResult("cloudflare.com", 443, CheckStatus.IDLE),
    PortProbeResult("www.wikipedia.org", 443, CheckStatus.IDLE)
)

private val DefaultOfficialStatuses = listOf(
    OfficialStatusResult("GitHub", "https://www.githubstatus.com/api/v2/status.json", "https://www.githubstatus.com"),
    OfficialStatusResult("Cloudflare", "https://www.cloudflarestatus.com/api/v2/status.json", "https://www.cloudflarestatus.com"),
    OfficialStatusResult("OpenAI", "https://status.openai.com/api/v2/status.json", "https://status.openai.com"),
    OfficialStatusResult("Discord", "https://discordstatus.com/api/v2/status.json", "https://discordstatus.com")
)

private val LocalSecurityChecklist = listOf(
    SecurityChecklistItem("updates", "基础维护", "及时安装系统与应用更新", "开启 Android 系统更新提示，并优先从可信应用商店更新常用应用。"),
    SecurityChecklistItem("screen_lock", "基础维护", "使用强屏幕锁定", "使用不少于 6 位 PIN、密码或生物识别，不使用容易猜测的图案。"),
    SecurityChecklistItem("auto_lock", "基础维护", "设置较短的自动锁屏时间", "降低设备无人看管时仍可直接访问数据的风险。"),
    SecurityChecklistItem("backups", "基础维护", "检查备份与恢复范围", "确认备份账号、加密方式和需要排除的敏感数据符合你的预期。"),
    SecurityChecklistItem("app_sources", "应用与权限", "仅从可信来源安装应用", "避免安装来历不明的 APK；如临时安装，完成后关闭未知来源安装权限。"),
    SecurityChecklistItem("permissions", "应用与权限", "复核高敏感权限", "定期检查定位、麦克风、相机、通讯录、辅助功能和通知读取权限。"),
    SecurityChecklistItem("accessibility", "应用与权限", "检查辅助功能服务", "只保留你明确需要且可信的辅助功能服务。"),
    SecurityChecklistItem("admin_apps", "应用与权限", "检查设备管理权限", "确认没有不认识的应用拥有设备管理员或工作资料管理权限。"),
    SecurityChecklistItem("wifi", "网络连接", "避免自动连接未知 Wi-Fi", "关闭自动加入开放或来源不明的无线网络。"),
    SecurityChecklistItem("bluetooth", "网络连接", "不使用时关闭蓝牙可发现性", "仅在需要配对时开启，并移除不再使用的已配对设备。"),
    SecurityChecklistItem("vpn", "网络连接", "核验 VPN / Private DNS 配置", "确认服务商、连接状态、Private DNS 主机名和失效时的实际行为符合预期。"),
    SecurityChecklistItem("public_wifi", "网络连接", "在公共网络减少敏感操作", "优先使用 HTTPS；避免在不受信任网络中处理高价值账户或敏感传输。"),
    SecurityChecklistItem("password_manager", "账户安全", "使用独立且强的密码", "优先使用可信密码管理器，不在多个重要站点复用密码。"),
    SecurityChecklistItem("mfa", "账户安全", "为重要账户开启多因素认证", "优先考虑验证器、通行密钥或硬件安全密钥，并妥善保存恢复方式。"),
    SecurityChecklistItem("recovery", "账户安全", "复核恢复邮箱和手机号", "移除不再使用的恢复方式，防止账户恢复链被接管。"),
    SecurityChecklistItem("sessions", "账户安全", "定期检查登录会话", "在邮件、云盘、社交与金融服务中退出不认识的设备和会话。"),
    SecurityChecklistItem("encryption", "数据与隐私", "使用设备加密与可信账户", "确认设备锁屏已启用，并了解云同步所使用的账户和加密边界。"),
    SecurityChecklistItem("sharing", "数据与隐私", "限制敏感文件分享链接", "为共享设置最小权限、到期时间，并定期撤销不再需要的链接。"),
    SecurityChecklistItem("notifications", "数据与隐私", "降低锁屏通知泄露", "对验证码、消息内容和敏感应用关闭锁屏完整预览。"),
    SecurityChecklistItem("photos", "数据与隐私", "清理照片位置与元数据", "在公开分享前检查地点、时间、联系人和文档元数据。"),
    SecurityChecklistItem("phishing", "识别与应急", "警惕钓鱼链接和假冒客服", "不要因短信、电话或弹窗催促而输入密码、验证码或恢复码。"),
    SecurityChecklistItem("downloads", "识别与应急", "扫描可疑下载与附件", "不打开来源不明的安装包、文档宏或压缩文件；有疑问时先核验发送方。"),
    SecurityChecklistItem("lost_device", "识别与应急", "准备设备遗失应对方案", "确认查找设备、远程锁定和远程擦除等功能可用，并保存恢复信息。"),
    SecurityChecklistItem("incident", "识别与应急", "准备账户异常处置顺序", "发生异常时先隔离设备、修改关键密码、撤销会话，再联系服务商。"),
    SecurityChecklistItem("payments", "高风险操作", "为支付和高价值账户设置额外保护", "开启交易提醒、强认证与独立恢复方式，警惕屏幕共享或远程控制请求。"),
    SecurityChecklistItem("developer_options", "高风险操作", "不需要时关闭开发者选项", "完成调试后关闭 USB 调试，避免在不可信电脑上授权调试。"),
    SecurityChecklistItem("root", "高风险操作", "理解 root / 解锁 Bootloader 的影响", "若设备已修改系统，请确认补丁、完整性、密钥与金融应用兼容性风险。"),
    SecurityChecklistItem("family", "高风险操作", "为共享设备设立独立空间", "为家人、儿童或工作用途使用独立账户、用户或工作资料，避免混用凭据。"),
    SecurityChecklistItem("review", "持续复核", "设定定期安全复核", "每 3–6 个月重新检查权限、会话、恢复方式、应用和网络配置。"),
    SecurityChecklistItem("evidence", "持续复核", "记录关键配置变更", "在变更 VPN、DNS、账户或设备安全设置前后保留可理解的本地记录。")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetScopeApp(
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var snapshot by remember { mutableStateOf<IpSnapshot?>(null) }
    var ipLoading by remember { mutableStateOf(true) }
    var ipError by remember { mutableStateOf<String?>(null) }
    var privacy by remember { mutableStateOf(NetworkRepository.inspectPrivacy(context)) }
    var endpoints by remember { mutableStateOf(SecureApiKeyStore.loadConnectivityEndpoints(context).ifEmpty { DefaultEndpoints }) }
    var testingAll by remember { mutableStateOf(false) }
    var showConnectivitySettings by remember { mutableStateOf(false) }
    var speedResult by remember { mutableStateOf<NetworkSpeedResult?>(null) }
    var speedTesting by remember { mutableStateOf(false) }
    var dnsHost by remember { mutableStateOf("example.com") }
    var dnsRecordType by remember { mutableStateOf("A") }
    var dnsResult by remember { mutableStateOf<DnsLookupResult?>(null) }
    var dnsLoading by remember { mutableStateOf(false) }
    var whoisQuery by remember { mutableStateOf("") }
    var whoisResult by remember { mutableStateOf<WhoisLookupResult?>(null) }
    var whoisLoading by remember { mutableStateOf(false) }
    var asnQuery by remember { mutableStateOf("") }
    var asnResult by remember { mutableStateOf<AsnLookupResult?>(null) }
    var asnLoading by remember { mutableStateOf(false) }
    var macQuery by remember { mutableStateOf("") }
    var macResult by remember { mutableStateOf<MacLookupResult?>(null) }
    var macLoading by remember { mutableStateOf(false) }
    var globalPingTarget by remember { mutableStateOf("example.com") }
    var globalPingResult by remember { mutableStateOf<GlobalPingResult?>(null) }
    var globalPingLoading by remember { mutableStateOf(false) }
    var mtrTarget by remember { mutableStateOf("") }
    var mtrResult by remember { mutableStateOf<MtrResult?>(null) }
    var mtrLoading by remember { mutableStateOf(false) }
    var ooniDomain by remember { mutableStateOf("example.com") }
    var ooniCountry by remember { mutableStateOf("US") }
    var ooniResult by remember { mutableStateOf<OoniQueryResult?>(null) }
    var ooniLoading by remember { mutableStateOf(false) }
    var portProbes by remember { mutableStateOf(SecureApiKeyStore.loadPortProbeTargets(context).ifEmpty { DefaultPortProbes }) }
    var portsLoading by remember { mutableStateOf(false) }
    var showPortProbeSettings by remember { mutableStateOf(false) }
    var officialStatuses by remember { mutableStateOf(DefaultOfficialStatuses) }
    var officialStatusLoading by remember { mutableStateOf(false) }
    var purityReport by remember { mutableStateOf<PurityReport?>(null) }
    var purityLoading by remember { mutableStateOf(false) }
    var purityError by remember { mutableStateOf<String?>(null) }
    var apiKeyConfig by remember { mutableStateOf(runCatching { SecureApiKeyStore.load(context) }.getOrDefault(ApiKeyConfig())) }
    var ipHistory by remember { mutableStateOf(runCatching { SecureApiKeyStore.loadIpHistory(context) }.getOrDefault(emptyList())) }
    var ipQuery by remember { mutableStateOf("") }
    var queriedSnapshot by remember { mutableStateOf<IpSnapshot?>(null) }
    var queryLoading by remember { mutableStateOf(false) }
    var queryError by remember { mutableStateOf<String?>(null) }
    var showApiKeySettings by remember { mutableStateOf(false) }
    var showDisplaySettings by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var completedChecklistIds by remember { mutableStateOf(runCatching { SecureApiKeyStore.loadSecurityChecklistProgress(context) }.getOrDefault(emptySet())) }

    fun refreshIpInfo() {
        scope.launch {
            ipLoading = true
            ipError = null
            runCatching { NetworkRepository.loadIpSnapshot() }
                .onSuccess {
                    snapshot = it
                    privacy = NetworkRepository.inspectPrivacy(context)
                    ipHistory = runCatching { SecureApiKeyStore.recordIpHistory(context, it, "当前出口") }.getOrDefault(ipHistory)
                }
                .onFailure { ipError = it.asUserMessage() }
            ipLoading = false
        }
    }

    fun runAllConnectivity() {
        scope.launch {
            testingAll = true
            endpoints = endpoints.map { it.copy(status = CheckStatus.RUNNING, latencyMs = null, detail = "正在连接…") }
            endpoints = coroutineScope {
                endpoints.map { endpoint ->
                    async(Dispatchers.IO) { NetworkRepository.testEndpoint(endpoint) }
                }.awaitAll()
            }
            testingAll = false
        }
    }

    fun runSingleConnectivity(target: EndpointResult) {
        scope.launch {
            endpoints = endpoints.map {
                if (it.name == target.name) it.copy(status = CheckStatus.RUNNING, latencyMs = null, detail = "正在连接…") else it
            }
            val checked = withContext(Dispatchers.IO) { NetworkRepository.testEndpoint(target) }
            endpoints = endpoints.map { if (it.name == target.name) checked else it }
        }
    }

    fun saveConnectivityEndpoints(updated: List<EndpointResult>) {
        val cleaned = updated.take(12).map { it.copy(status = CheckStatus.IDLE, latencyMs = null, detail = "等待检测") }
        runCatching { SecureApiKeyStore.saveConnectivityEndpoints(context, cleaned) }
            .onSuccess { endpoints = cleaned; showConnectivitySettings = false }
    }

    fun resetConnectivityEndpoints() {
        runCatching { SecureApiKeyStore.clearConnectivityEndpoints(context) }
            .onSuccess { endpoints = DefaultEndpoints; showConnectivitySettings = false }
    }

    fun measureCloudflare() {
        scope.launch {
            speedTesting = true
            speedResult = null
            speedResult = runCatching { withContext(Dispatchers.IO) { NetworkRepository.measureCloudflareSpeed() } }.getOrNull()
            speedTesting = false
        }
    }

    fun shareTextReport(title: String, report: String) {
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, report)
            putExtra(Intent.EXTRA_TITLE, title)
        }, "导出或分享"))
    }

    fun buildMarkdownReport(): String = buildString {
        appendLine("# NetScope 网络诊断摘要")
        appendLine()
        appendLine("生成时间：${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        snapshot?.let {
            appendLine("当前 IPv4：${it.ipv4}")
            appendLine("位置：${listOf(it.country, it.region, it.city).filter(String::isNotBlank).joinToString(" · ")}")
            appendLine("ASN / 网络：${it.asn} · ${it.isp}")
        }
        appendLine("Android VPN：${if (privacy.vpnActive) "已连接" else "未检测到"}")
        appendLine("网络传输：${privacy.transport}；系统验证：${if (privacy.validated) "已验证" else "未验证"}；计费：${if (privacy.metered) "按流量计费" else "非按流量计费"}")
        appendLine("Private DNS：${privacy.privateDnsMode}${privacy.privateDnsHostname.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty()}")
        purityReport?.let { appendLine("公开风险主分：${formatRisk(it.score)} / 100；覆盖度：${formatRisk(it.coverage)}") }
        speedResult?.let { appendLine("Cloudflare：延迟 ${it.latencyMs}ms；抖动 ${formatRisk(it.jitterMs)}ms；下载 ${formatRisk(it.downloadMbps)} Mbps") }
        appendLine("安全清单：已自行完成 ${completedChecklistIds.size.coerceAtMost(LocalSecurityChecklist.size)} / ${LocalSecurityChecklist.size} 项（本地自评）")
        mtrResult?.takeIf { it.error == null }?.let { mtr ->
            appendLine("Globalping MTR：目标 ${mtr.target}；返回 ${mtr.probes.size} 个远端探针")
            mtr.probes.forEach { probe -> appendLine("- ${probe.location}: ${probe.hops.size} 跳，状态 ${probe.status}") }
        }
        appendLine()
        appendLine("说明：这是当前设备和当前网络的快照，不是欺诈概率、账号信誉、安全保证、浏览器指纹或 DNS 泄漏结论。")
    }

    fun buildJsonReport(): String = JSONObject().apply {
        put("schema", "netscope.network-diagnostic.v1")
        put("generatedAt", Instant.now().toString())
        put("ip", snapshot?.let {
            JSONObject().apply {
                put("ipv4", it.ipv4); put("ipv6", it.ipv6); put("country", it.country); put("region", it.region)
                put("city", it.city); put("timezone", it.timezone); put("isp", it.isp); put("asn", it.asn); put("networkType", it.networkType)
            }
        } ?: JSONObject.NULL)
        put("androidNetwork", JSONObject().apply {
            put("vpnActive", privacy.vpnActive); put("privateDnsMode", privacy.privateDnsMode); put("privateDnsHostname", privacy.privateDnsHostname)
            put("dnsServers", JSONArray().apply { privacy.dnsServers.forEach(::put) }); put("transport", privacy.transport)
            put("validated", privacy.validated); put("metered", privacy.metered); put("interfaceName", privacy.interfaceName)
            put("downstreamKbpsEstimate", privacy.downstreamKbps); put("upstreamKbpsEstimate", privacy.upstreamKbps)
        })
        put("purity", purityReport?.let {
            JSONObject().apply { put("score", it.score); put("coverage", it.coverage); put("label", it.label); put("checkedAt", it.checkedAt) }
        } ?: JSONObject.NULL)
        put("speed", speedResult?.let {
            JSONObject().apply { put("latencyMs", it.latencyMs); put("jitterMs", it.jitterMs); put("downloadMbps", it.downloadMbps); put("downloadedBytes", it.downloadedBytes) }
        } ?: JSONObject.NULL)
        put("connectivity", JSONArray().apply {
            endpoints.forEach { endpoint -> put(JSONObject().apply { put("name", endpoint.name); put("host", endpoint.host); put("status", endpoint.status.name); put("latencyMs", endpoint.latencyMs); put("detail", endpoint.detail) }) }
        })
        put("portProbes", JSONArray().apply {
            portProbes.forEach { probe -> put(JSONObject().apply { put("host", probe.host); put("port", probe.port); put("status", probe.status.name); put("latencyMs", probe.latencyMs); put("detail", probe.detail) }) }
        })
        put("officialStatus", JSONArray().apply {
            officialStatuses.forEach { status -> put(JSONObject().apply { put("name", status.name); put("indicator", status.indicator); put("description", status.description); put("updatedAt", status.updatedAt); put("statusPage", status.statusPage) }) }
        })
        put("dns", dnsResult?.let { result ->
            JSONObject().apply {
                put("host", result.host); put("recordType", result.recordType); put("systemAnswers", JSONArray().apply { result.addresses.forEach(::put) })
                put("resolverResults", JSONArray().apply { result.resolverResults.forEach { resolver -> put(JSONObject().apply { put("resolver", resolver.resolver); put("answers", JSONArray().apply { resolver.addresses.forEach(::put) }); put("status", resolver.status); put("error", resolver.error) }) } })
            }
        } ?: JSONObject.NULL)
        put("mtr", mtrResult?.takeIf { it.error == null }?.let { mtr ->
            JSONObject().apply {
                put("target", mtr.target); put("measurementId", mtr.measurementId); put("status", mtr.status)
                put("probes", JSONArray().apply { mtr.probes.forEach { probe -> put(JSONObject().apply {
                    put("location", probe.location); put("network", probe.network); put("asn", probe.asn); put("status", probe.status)
                    put("hops", JSONArray().apply { probe.hops.forEach { hop -> put(JSONObject().apply { put("number", hop.number); put("host", hop.host); put("ip", hop.ip); put("asn", hop.asn); put("lossPct", hop.lossPct); put("lastMs", hop.lastMs); put("avgMs", hop.avgMs); put("worstMs", hop.worstMs) }) } })
                }) } })
            }
        } ?: JSONObject.NULL)
        put("securityChecklist", JSONObject().apply { put("completed", completedChecklistIds.size.coerceAtMost(LocalSecurityChecklist.size)); put("total", LocalSecurityChecklist.size); put("scope", "local-self-assessment") })
        put("notice", "User-initiated local export. No authorization credentials, IP history, or browser fingerprint data are included.")
    }.toString(2)

    fun lookupIp() {
        val target = ipQuery.trim()
        if (!isIpLiteral(target)) {
            queryError = "请输入有效的 IPv4 或 IPv6 地址"
            queriedSnapshot = null
            return
        }
        scope.launch {
            queryLoading = true
            queryError = null
            queriedSnapshot = null
            runCatching { withContext(Dispatchers.IO) { NetworkRepository.lookupIpSnapshot(target) } }
                .onSuccess {
                    queriedSnapshot = it
                    ipHistory = runCatching { SecureApiKeyStore.recordIpHistory(context, it, "手动查询") }.getOrDefault(ipHistory)
                }
                .onFailure { queryError = it.asUserMessage() }
            queryLoading = false
        }
    }

    fun clearIpHistory() {
        runCatching { SecureApiKeyStore.clearIpHistory(context) }
            .onSuccess { ipHistory = emptyList() }
    }

    fun toggleChecklistItem(itemId: String) {
        val updated = completedChecklistIds.toMutableSet().apply {
            if (!add(itemId)) remove(itemId)
        }.toSet()
        runCatching { SecureApiKeyStore.saveSecurityChecklistProgress(context, updated) }
            .onSuccess { completedChecklistIds = updated }
    }

    fun clearChecklistProgress() {
        runCatching { SecureApiKeyStore.clearSecurityChecklistProgress(context) }
            .onSuccess { completedChecklistIds = emptySet() }
    }

    fun runPurityDiagnosis() {
        scope.launch {
            purityLoading = true
            purityError = null
            purityReport = null
            runCatching { withContext(Dispatchers.IO) { NetworkRepository.runPurityDiagnosis(context, apiKeyConfig) } }
                .onSuccess { purityReport = it }
                .onFailure { purityError = it.asUserMessage() }
            purityLoading = false
        }
    }

    fun saveApiKeys(config: ApiKeyConfig) {
        val normalized = config.copy(customEndpoint = config.customEndpoint.trim())
        if (!isHttpsEndpoint(normalized.customEndpoint)) {
            purityError = "自定义请求地址必须是完整的 HTTPS 地址"
            return
        }
        if (normalized.customKey.isBlank() != normalized.customEndpoint.isBlank()) {
            purityError = "自定义 API Key 与 HTTPS 请求地址需要同时填写或同时清空"
            return
        }
        runCatching { SecureApiKeyStore.save(context, normalized) }
            .onSuccess {
                apiKeyConfig = normalized
                showApiKeySettings = false
                runPurityDiagnosis()
            }
            .onFailure { purityError = "无法保存本地 Key：${it.asUserMessage()}" }
    }

    fun clearApiKeys() {
        runCatching { SecureApiKeyStore.clear(context) }
            .onSuccess {
                apiKeyConfig = ApiKeyConfig()
                runPurityDiagnosis()
            }
            .onFailure { purityError = "无法清除本地 Key：${it.asUserMessage()}" }
    }

    fun resolveDns() {
        val target = dnsHost.trim().removePrefix("https://").removePrefix("http://").substringBefore('/').trim()
        val recordType = dnsRecordType.trim().uppercase()
        if (target.isBlank()) {
            dnsResult = DnsLookupResult(host = "", addresses = emptyList(), error = "请输入域名或主机名")
            return
        }
        if (recordType !in setOf("A", "AAAA", "TXT", "MX", "NS", "CNAME")) {
            dnsResult = DnsLookupResult(host = target, recordType = recordType, addresses = emptyList(), error = "仅支持 A、AAAA、TXT、MX、NS、CNAME")
            return
        }
        scope.launch {
            dnsLoading = true
            dnsResult = withContext(Dispatchers.IO) {
                runCatching { NetworkRepository.resolveDns(target, recordType) }
                    .getOrElse { DnsLookupResult(host = target, recordType = recordType, addresses = emptyList(), error = it.asUserMessage()) }
            }
            dnsLoading = false
        }
    }

    fun shareDnsResult(result: DnsLookupResult) {
        val report = buildString {
            appendLine("# NetScope DNS 查询")
            appendLine()
            appendLine("时间：${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
            appendLine("主机：${result.host}")
            appendLine("记录类型：${result.recordType}")
            val consensus = analyzeDnsConsensus(result)
            appendLine("对比结论：${consensus.label} — ${consensus.detail}")
            appendLine("系统解析：${result.addresses.joinToString(" · ").ifBlank { "未返回或该类型不适用" }}")
            result.resolverResults.forEach { resolver -> appendLine("${resolver.resolver}：${resolver.error ?: resolver.addresses.joinToString(" · ").ifBlank { "无记录" }}") }
            appendLine()
            appendLine("说明：DNS 差异可能来自 CDN、地域调度、缓存或安全策略，不能单独作为 DNS 泄漏结论。")
        }
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, report)
            putExtra(Intent.EXTRA_TITLE, "NetScope DNS 查询")
        }, "分享 DNS 查询"))
    }

    fun lookupWhois() {
        val target = whoisQuery.trim()
        if (target.isBlank()) {
            whoisResult = WhoisLookupResult("", "", emptyList(), "请输入域名或 IP 地址")
            return
        }
        scope.launch {
            whoisLoading = true
            whoisResult = withContext(Dispatchers.IO) {
                runCatching { NetworkRepository.lookupWhois(target) }
                    .getOrElse { WhoisLookupResult(target, "", emptyList(), it.asUserMessage()) }
            }
            whoisLoading = false
        }
    }

    fun lookupAsn() {
        val normalized = asnQuery.trim().removePrefix("AS").removePrefix("as")
        if (normalized.toLongOrNull()?.takeIf { it > 0 } == null) {
            asnResult = AsnLookupResult(asn = "", name = "", description = "", countryCode = "", website = "", allocatedAt = "", error = "请输入有效 ASN，例如 AS13335")
            return
        }
        scope.launch {
            asnLoading = true
            asnResult = withContext(Dispatchers.IO) {
                runCatching { NetworkRepository.lookupAsn(normalized) }
                    .getOrElse { AsnLookupResult(asn = "AS$normalized", name = "", description = "", countryCode = "", website = "", allocatedAt = "", error = it.asUserMessage()) }
            }
            asnLoading = false
        }
    }

    fun lookupMac() {
        val normalized = macQuery.uppercase().filter(Char::isLetterOrDigit)
        if (normalized.length != 12 || normalized.any { it !in '0'..'9' && it !in 'A'..'F' }) {
            macResult = MacLookupResult("", "", false, "请输入 12 位十六进制 MAC 地址")
            return
        }
        scope.launch {
            macLoading = true
            macResult = withContext(Dispatchers.IO) {
                runCatching { NetworkRepository.lookupMac(normalized) }
                    .getOrElse { MacLookupResult(normalized, "", false, it.asUserMessage()) }
            }
            macLoading = false
        }
    }

    fun runGlobalPing() {
        val target = globalPingTarget.trim().removePrefix("https://").removePrefix("http://").substringBefore('/').trim()
        if (target.isBlank() || target.any { it.isWhitespace() }) {
            globalPingResult = GlobalPingResult("", "", emptyList(), "错误", "请输入域名或 IP 地址")
            return
        }
        scope.launch {
            globalPingLoading = true
            globalPingResult = withContext(Dispatchers.IO) {
                runCatching { NetworkRepository.runGlobalPing(target) }
                    .getOrElse { GlobalPingResult(target, "", emptyList(), "错误", it.asUserMessage()) }
            }
            globalPingLoading = false
        }
    }

    fun runGlobalMtr() {
        val target = mtrTarget.trim().ifBlank { snapshot?.ipv4.orEmpty() }
            .removePrefix("https://").removePrefix("http://").substringBefore('/').trim()
        if (!isValidProbeHost(target)) {
            mtrResult = MtrResult(target, "", "错误", emptyList(), "请输入有效的域名或 IP 地址")
            return
        }
        mtrTarget = target
        scope.launch {
            mtrLoading = true
            mtrResult = withContext(Dispatchers.IO) {
                runCatching { NetworkRepository.runGlobalMtr(target) }
                    .getOrElse { MtrResult(target, "", "错误", emptyList(), it.asUserMessage()) }
            }
            mtrLoading = false
        }
    }

    fun queryOoni() {
        val domain = ooniDomain.trim().removePrefix("https://").removePrefix("http://").substringBefore('/').trim()
        val country = ooniCountry.trim().uppercase()
        if (domain.isBlank() || country.length != 2 || country.any { !it.isLetter() }) {
            ooniResult = OoniQueryResult(domain, country, 0, emptyList(), "请输入域名和两位国家代码，例如 US")
            return
        }
        scope.launch {
            ooniLoading = true
            ooniResult = withContext(Dispatchers.IO) {
                runCatching { NetworkRepository.queryOoni(domain, country) }
                    .getOrElse { OoniQueryResult(domain, country, 0, emptyList(), it.asUserMessage()) }
            }
            ooniLoading = false
        }
    }

    fun refreshOfficialStatuses() {
        scope.launch {
            officialStatusLoading = true
            officialStatuses = coroutineScope {
                officialStatuses.map { status ->
                    async(Dispatchers.IO) {
                        runCatching { NetworkRepository.fetchOfficialStatus(status) }
                            .getOrElse { error -> status.copy(error = error.asUserMessage(), description = "状态接口请求失败") }
                    }
                }.awaitAll()
            }
            officialStatusLoading = false
        }
    }

    fun savePortProbeTargets(updated: List<PortProbeResult>) {
        val cleaned = updated.take(12).map { it.copy(status = CheckStatus.IDLE, latencyMs = null, detail = "等待探测") }
        runCatching { SecureApiKeyStore.savePortProbeTargets(context, cleaned) }
            .onSuccess { portProbes = cleaned; showPortProbeSettings = false }
    }

    fun resetPortProbeTargets() {
        runCatching { SecureApiKeyStore.clearPortProbeTargets(context) }
            .onSuccess { portProbes = DefaultPortProbes; showPortProbeSettings = false }
    }

    fun runPortProbes() {
        scope.launch {
            portsLoading = true
            portProbes = portProbes.map { it.copy(status = CheckStatus.RUNNING, latencyMs = null, detail = "正在探测…") }
            portProbes = coroutineScope {
                portProbes.map { probe ->
                    async(Dispatchers.IO) { NetworkRepository.probePort(probe) }
                }.awaitAll()
            }
            portsLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshIpInfo()
            runAllConnectivity()
            runPurityDiagnosis()
            refreshOfficialStatuses()
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NetScope", fontWeight = FontWeight.Bold, color = Ink)
                        Text("IP 工具箱", fontSize = 11.sp, color = MutedInk)
                    }
                },
                navigationIcon = {
                    Row(
                        modifier = Modifier.padding(start = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Public, contentDescription = null, tint = Blue)
                        }
                        IconButton(onClick = { showApiKeySettings = true }) {
                            Icon(Icons.Outlined.VpnKey, contentDescription = "授权数据源 Key 设置", tint = Blue)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showDisplaySettings = true }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "显示设置", tint = Ink)
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Outlined.Share, contentDescription = "导出或分享诊断摘要", tint = Ink)
                    }
                    IconButton(onClick = { refreshIpInfo() }, enabled = !ipLoading) {
                        if (ipLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Blue)
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "刷新 IP 信息", tint = Ink)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppBackground)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 4.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HeroHeader()
            }
            item {
                SectionHeader(
                    icon = Icons.Outlined.Public,
                    title = "IP 信息",
                    subtitle = "通过多源查询确认你的当前公网出口"
                )
            }
            item {
                IpInfoCard(snapshot = snapshot, loading = ipLoading, error = ipError, onRetry = { refreshIpInfo() })
            }
            item {
                Ipv6Card(snapshot?.ipv6, loading = ipLoading)
            }
            item {
                SectionHeader(
                    icon = Icons.Outlined.Search,
                    title = "查询 IP",
                    subtitle = "输入 IPv4 或 IPv6，查看地理、ASN 与网络信息"
                )
            }
            item {
                IpQueryCard(
                    query = ipQuery,
                    onQueryChange = { ipQuery = it },
                    result = queriedSnapshot,
                    loading = queryLoading,
                    error = queryError,
                    onLookup = { lookupIp() }
                )
            }
            item {
                SectionHeader(
                    icon = Icons.Outlined.History,
                    title = "IP 历史",
                    subtitle = "仅加密保存在本机，最多保留最近 30 条",
                    actionLabel = if (ipHistory.isEmpty()) null else "清除",
                    onAction = if (ipHistory.isEmpty()) null else ({ clearIpHistory() })
                )
            }
            item {
                IpHistoryCard(history = ipHistory)
            }
            item {
                SectionHeader(
                    icon = Icons.Outlined.Security,
                    title = "增强纯净度诊断",
                    subtitle = "公开风险源与网络信号的可解释独立评分",
                    actionLabel = if (purityLoading) "检测中" else "重新检测",
                    onAction = if (purityLoading) null else ({ runPurityDiagnosis() })
                )
            }
            item {
                PurityDiagnosisCard(
                    report = purityReport,
                    loading = purityLoading,
                    error = purityError,
                    onRetry = { runPurityDiagnosis() }
                )
            }
            item {
                SectionHeader(
                    icon = Icons.Outlined.Security,
                    title = "离线安全清单",
                    subtitle = "30 项本地自评与加密进度；不是自动安全扫描或合规认证",
                    actionLabel = if (completedChecklistIds.isEmpty()) null else "清除进度",
                    onAction = if (completedChecklistIds.isEmpty()) null else ({ clearChecklistProgress() })
                )
            }
            item {
                SecurityChecklistCard(
                    items = LocalSecurityChecklist,
                    completedIds = completedChecklistIds,
                    onToggle = { toggleChecklistItem(it) }
                )
            }
            item {
                SectionHeader(
                    icon = Icons.Outlined.NetworkCheck,
                    title = "网络连通性",
                    subtitle = "检测 HTTPS 目标是否可访问；列表可自定义并加密保存在本机",
                    actionLabel = "管理",
                    onAction = { showConnectivitySettings = true }
                )
            }
            item {
                ConnectivityGrid(endpoints = endpoints, onRetryOne = { runSingleConnectivity(it) })
            }
            item {
                SectionHeader(
                    icon = Icons.Outlined.Security,
                    title = "隐私与 DNS",
                    subtitle = "以 Android 系统可可靠提供的网络信息进行诊断"
                )
            }
            item {
                PrivacyCard(privacy = privacy, onRefresh = { privacy = NetworkRepository.inspectPrivacy(context) })
            }
            item {
                SectionHeader(
                    icon = Icons.Outlined.Speed,
                    title = "快速网络测量",
                    subtitle = "手动触发的限量测速：延迟、抖动与最多 1 MB 下载"
                )
            }
            item {
                SpeedCard(result = speedResult, loading = speedTesting, onMeasure = { measureCloudflare() })
            }
            item {
                SectionHeader(
                    icon = Icons.Outlined.Hub,
                    title = "原生网络诊断",
                    subtitle = "常用查询与状态检测直接在 APP 内完成"
                )
            }
            item {
                DnsLookupCard(
                    host = dnsHost,
                result = dnsResult,
                loading = dnsLoading,
                recordType = dnsRecordType,
                onHostChange = { dnsHost = it },
                onRecordTypeChange = { dnsRecordType = it },
                onLookup = { resolveDns() },
                onShare = { dnsResult?.takeIf { it.error == null }?.let { shareDnsResult(it) } }
                )
            }
            item {
                WhoisLookupCard(
                    query = whoisQuery,
                    result = whoisResult,
                    loading = whoisLoading,
                    onQueryChange = { whoisQuery = it },
                    onLookup = { lookupWhois() }
                )
            }
            item {
                AsnLookupCard(
                    query = asnQuery,
                    result = asnResult,
                    loading = asnLoading,
                    onQueryChange = { asnQuery = it },
                    onLookup = { lookupAsn() }
                )
            }
            item {
                MacLookupCard(
                    query = macQuery,
                    result = macResult,
                    loading = macLoading,
                    onQueryChange = { macQuery = it },
                    onLookup = { lookupMac() }
                )
            }
            item {
                SectionHeader(
                    icon = Icons.Outlined.Language,
                    title = "全球与审查数据",
                    subtitle = "仅在手动执行时调用公共远端探针或历史测量 API"
                )
            }
            item {
                GlobalPingCard(
                    target = globalPingTarget,
                    result = globalPingResult,
                    loading = globalPingLoading,
                    onTargetChange = { globalPingTarget = it },
                    onRun = { runGlobalPing() }
                )
            }
            item {
                OoniCard(
                    domain = ooniDomain,
                    country = ooniCountry,
                    result = ooniResult,
                    loading = ooniLoading,
                    onDomainChange = { ooniDomain = it },
                    onCountryChange = { ooniCountry = it },
                    onRun = { queryOoni() }
                )
            }
            item {
                MtrCard(
                    target = mtrTarget,
                    result = mtrResult,
                    loading = mtrLoading,
                    onTargetChange = { mtrTarget = it },
                    onRun = { runGlobalMtr() }
                )
            }
            item {
                ServiceStatusCard(
                    probes = portProbes,
                    officialStatuses = officialStatuses,
                    loading = portsLoading,
                    officialLoading = officialStatusLoading,
                    onProbe = { runPortProbes() },
                    onRefreshOfficial = { refreshOfficialStatuses() },
                    onManageProbes = { showPortProbeSettings = true }
                )
            }
            item {
                DeviceEnvironmentCard(context)
            }
            item {
                BrowserFallbackCard(context)
            }
            item {
                Footer()
            }
        }
    }

    if (showExportDialog) {
        ExportFormatDialog(
            onDismiss = { showExportDialog = false },
            onShareMarkdown = {
                shareTextReport("NetScope 网络诊断摘要", buildMarkdownReport())
                showExportDialog = false
            },
            onShareJson = {
                shareTextReport("NetScope 网络诊断摘要 JSON", buildJsonReport())
                showExportDialog = false
            }
        )
    }

    if (showDisplaySettings) {
        DisplaySettingsDialog(
            selectedPreference = themePreference,
            onDismiss = { showDisplaySettings = false },
            onSave = {
                onThemePreferenceChange(it)
                showDisplaySettings = false
            }
        )
    }

    if (showPortProbeSettings) {
        PortProbeSettingsDialog(
            savedProbes = portProbes,
            onDismiss = { showPortProbeSettings = false },
            onSave = { savePortProbeTargets(it) },
            onReset = { resetPortProbeTargets() }
        )
    }

    if (showConnectivitySettings) {
        ConnectivitySettingsDialog(
            savedEndpoints = endpoints,
            onDismiss = { showConnectivitySettings = false },
            onSave = { saveConnectivityEndpoints(it) },
            onReset = { resetConnectivityEndpoints() }
        )
    }

    if (showApiKeySettings) {
        ApiKeySettingsDialog(
            savedConfig = apiKeyConfig,
            onDismiss = { showApiKeySettings = false },
            onSave = { saveApiKeys(it) },
            onClear = { clearApiKeys() }
        )
    }
}

@Composable
private fun ExportFormatDialog(
    onDismiss: () -> Unit,
    onShareMarkdown: () -> Unit,
    onShareJson: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导出诊断摘要") },
        text = {
            Column {
                Text("导出仅在你点击后通过 Android 系统分享面板执行，不会上传到 NetScope。内容不含授权 Key、加密 IP 历史或浏览器指纹信息。", color = MutedInk, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onShareMarkdown, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp)); Text("分享 Markdown 文本")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onShareJson, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp)); Text("分享 JSON 摘要")
                }
            }
        },
        confirmButton = {},
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun DisplaySettingsDialog(
    selectedPreference: ThemePreference,
    onDismiss: () -> Unit,
    onSave: (ThemePreference) -> Unit
) {
    var draft by remember(selectedPreference) { mutableStateOf(selectedPreference) }
    val choices = listOf(
        ThemePreference.SYSTEM to "跟随系统",
        ThemePreference.LIGHT to "浅色",
        ThemePreference.DARK to "深色"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("显示设置") },
        text = {
            Column {
                Text("主题选择仅加密保存在当前设备。深色模式会调整页面背景、卡片、文本和状态色；不会改变检测结果或任何网络请求。", color = MutedInk, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(12.dp))
                choices.forEach { (preference, label) ->
                    val isSelected = draft == preference
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { draft = preference }.padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(label, fontWeight = FontWeight.Medium, color = Ink, fontSize = 14.sp)
                            Text(
                                when (preference) {
                                    ThemePreference.SYSTEM -> "随 Android 的当前深浅色设置变化"
                                    ThemePreference.LIGHT -> "始终使用浅色界面"
                                    ThemePreference.DARK -> "始终使用深色界面"
                                },
                                color = MutedInk,
                                fontSize = 11.sp
                            )
                        }
                        StatusBadge(if (isSelected) "已选" else "选择", if (isSelected) Green else MutedInk, if (isSelected) SoftGreen else SoftBlue)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(draft) }) { Text("保存显示设置") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun PortProbeSettingsDialog(
    savedProbes: List<PortProbeResult>,
    onDismiss: () -> Unit,
    onSave: (List<PortProbeResult>) -> Unit,
    onReset: () -> Unit
) {
    var draft by remember(savedProbes) { mutableStateOf(savedProbes) }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("443") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("端口探测目标") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("每项仅探测一个主机和一个端口；最多 12 项。不会扫描端口范围，清单以加密形式仅保存在当前设备。", color = MutedInk, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(12.dp))
                draft.forEachIndexed { index, probe ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Text("${probe.host}:${probe.port}", modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Ink)
                        IconButton(onClick = { draft = draft.filterIndexed { current, _ -> current != index } }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "删除 ${probe.host}:${probe.port}", tint = Red)
                        }
                    }
                }
                HorizontalDivider(color = Border)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text("主机名或 IP 地址") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = port, onValueChange = { port = it.filter(Char::isDigit) }, label = { Text("端口（1–65535）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (error != null) Text(error.orEmpty(), color = Red, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    val normalizedHost = host.trim().removePrefix("[").removeSuffix("]")
                    val normalizedPort = port.toIntOrNull()
                    error = when {
                        draft.size >= 12 -> "最多保存 12 个探测目标"
                        !isValidProbeHost(normalizedHost) -> "请输入有效单个主机名或 IP 地址"
                        normalizedPort !in 1..65535 -> "端口必须在 1 到 65535 之间"
                        draft.any { it.host.equals(normalizedHost, true) && it.port == normalizedPort } -> "该主机和端口已存在"
                        else -> null
                    }
                    if (error == null) {
                        draft = draft + PortProbeResult(normalizedHost, normalizedPort!!, CheckStatus.IDLE)
                        host = ""
                        port = "443"
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp)); Text("添加单端口目标")
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(draft) }) { Text("加密保存") } },
        dismissButton = { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = onReset) { Text("恢复默认") }; OutlinedButton(onClick = onDismiss) { Text("取消") } } }
    )
}

@Composable
private fun ConnectivitySettingsDialog(
    savedEndpoints: List<EndpointResult>,
    onDismiss: () -> Unit,
    onSave: (List<EndpointResult>) -> Unit,
    onReset: () -> Unit
) {
    var draft by remember(savedEndpoints) { mutableStateOf(savedEndpoints) }
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("连通性目标") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("仅允许 HTTPS 地址。目标清单最多 12 项，以加密形式保存在当前设备；保存后需手动重新检测。", color = MutedInk, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(12.dp))
                draft.forEachIndexed { index, endpoint ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(endpoint.name, fontWeight = FontWeight.Medium, color = Ink, fontSize = 13.sp)
                            Text(endpoint.url, color = MutedInk, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { draft = draft.filterIndexed { current, _ -> current != index } }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "删除 ${endpoint.name}", tint = Red)
                        }
                    }
                }
                HorizontalDivider(color = Border)
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        val existing = draft.map { it.name.lowercase() }.toSet()
                        draft = (draft + ProductivityEndpointPreset.filter { it.name.lowercase() !in existing }).take(12)
                        error = if (draft.size >= 12) "已达到 12 项上限；其余预设未加入" else null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("加入生产力 HTTPS 预设（最多 12 项）") }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("显示名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("HTTPS 地址") }, placeholder = { Text("https://example.com/health") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (error != null) Text(error.orEmpty(), color = Red, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    val normalizedName = name.trim()
                    val normalizedUrl = url.trim()
                    val host = runCatching { Uri.parse(normalizedUrl).host.orEmpty() }.getOrDefault("")
                    error = when {
                        draft.size >= 12 -> "最多保存 12 个目标"
                        normalizedName.isBlank() -> "请输入显示名称"
                        !isHttpsEndpoint(normalizedUrl) -> "请输入完整 HTTPS 地址"
                        host.isBlank() -> "地址缺少有效主机名"
                        draft.any { it.name.equals(normalizedName, true) } -> "显示名称不能重复"
                        else -> null
                    }
                    if (error == null) {
                        draft = draft + EndpointResult(normalizedName, host, normalizedUrl)
                        name = ""
                        url = ""
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("添加 HTTPS 目标")
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(draft) }) { Text("加密保存") } },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onReset) { Text("恢复默认") }
                OutlinedButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}

@Composable
private fun HeroHeader() {
    Column {
        Text("你的网络，一眼看清", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Ink)
        Spacer(Modifier.height(5.dp))
        Text(
            "查看公网 IP、网络可达性与 Android 设备网络隐私设置。",
            color = MutedInk,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SoftBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Blue, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink)
            Text(subtitle, fontSize = 12.sp, color = MutedInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (actionLabel != null) {
            Text(
                actionLabel,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = onAction != null) { onAction?.invoke() }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (onAction == null) MutedInk else Blue
            )
        }
    }
}

@Composable
private fun IpInfoCard(snapshot: IpSnapshot?, loading: Boolean, error: String?, onRetry: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (loading) {
            LoadingCard("正在查询公网 IPv4 与地理位置…")
        } else if (snapshot != null) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge("IPv4", Blue, SoftBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("公网出口", fontSize = 13.sp, color = MutedInk)
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = { clipboard.setText(AnnotatedString(snapshot.ipv4)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "复制 IP 地址", tint = Blue, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(snapshot.ipv4, fontWeight = FontWeight.Bold, fontSize = 27.sp, color = Ink)
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Border)
                Spacer(Modifier.height(10.dp))
                InfoLine(Icons.Outlined.LocationOn, "位置", listOf(snapshot.country, snapshot.region, snapshot.city).filter { it.isNotBlank() && it != "—" }.joinToString(" · "))
                InfoLine(Icons.Outlined.Schedule, "时区", snapshot.timezone.ifBlank { "—" })
                InfoLine(Icons.Outlined.Business, "网络", snapshot.isp.ifBlank { "—" })
                InfoLine(Icons.Outlined.Router, "ASN", snapshot.asn.ifBlank { "—" })
                InfoLine(Icons.Outlined.SettingsEthernet, "类型", snapshot.networkType.ifBlank { "未知" })
                Spacer(Modifier.height(10.dp))
                Text("更新于 ${snapshot.refreshedAt}", color = MutedInk, fontSize = 11.sp)
            }
        } else {
            ErrorCard(error ?: "暂时无法读取公网 IP", onRetry)
        }
    }
}

@Composable
private fun IpQueryCard(
    query: String,
    onQueryChange: (String) -> Unit,
    result: IpSnapshot?,
    loading: Boolean,
    error: String?,
    onLookup: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("IPv4 或 IPv6 地址") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Button(onClick = onLookup, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(17.dp), color = CardSurface, strokeWidth = 2.dp)
                else Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (loading) "查询中…" else "查询此 IP")
            }
            if (error != null) {
                Spacer(Modifier.height(10.dp))
                Text(error, color = Red, fontSize = 12.sp)
            }
            if (result != null) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Border)
                Spacer(Modifier.height(10.dp))
                Text(result.ipv4, fontWeight = FontWeight.Bold, color = Ink, fontSize = 20.sp)
                InfoLine(Icons.Outlined.LocationOn, "位置", listOf(result.country, result.region, result.city).filter { it.isNotBlank() && it != "—" }.joinToString(" · ").ifBlank { "—" })
                InfoLine(Icons.Outlined.Business, "网络", result.isp.ifBlank { "—" })
                InfoLine(Icons.Outlined.Router, "ASN", result.asn.ifBlank { "—" })
                InfoLine(Icons.Outlined.Schedule, "时区", result.timezone.ifBlank { "—" })
            }
        }
    }
}

@Composable
private fun IpHistoryCard(history: List<IpHistoryEntry>) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (history.isEmpty()) {
            Text("尚无记录。刷新当前出口或完成一次 IP 查询后，结果会以加密形式仅保存在本机。", modifier = Modifier.padding(16.dp), color = MutedInk, fontSize = 12.sp, lineHeight = 18.sp)
        } else {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                history.take(8).forEachIndexed { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.ip, color = Ink, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(listOf(item.source, listOf(item.country, item.city).filter { it.isNotBlank() }.joinToString(" · "), item.asn).filter { it.isNotBlank() }.joinToString(" · "), color = MutedInk, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text(item.seenAt, color = MutedInk, fontSize = 10.sp)
                    }
                    if (index < history.take(8).lastIndex) HorizontalDivider(color = Border)
                }
                if (history.size > 8) Text("另有 ${history.size - 8} 条加密历史记录", color = MutedInk, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun Ipv6Card(ipv6: String?, loading: Boolean) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (loading) {
            LoadingCard("正在检测 IPv6…", compact = true)
        } else {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (ipv6 != null) SoftGreen else SoftAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (ipv6 != null) Icons.Outlined.CheckCircle else Icons.Outlined.Info,
                        contentDescription = null,
                        tint = if (ipv6 != null) Green else Amber
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("IPv6 出口", fontWeight = FontWeight.SemiBold, color = Ink)
                    Text(
                        ipv6 ?: "当前网络未检测到可用的 IPv6 公网地址",
                        fontSize = 12.sp,
                        color = MutedInk,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiKeySettingsDialog(
    savedConfig: ApiKeyConfig,
    onDismiss: () -> Unit,
    onSave: (ApiKeyConfig) -> Unit,
    onClear: () -> Unit
) {
    var abuseKey by remember(savedConfig) { mutableStateOf(savedConfig.abuseIpDbKey) }
    var ipApiKey by remember(savedConfig) { mutableStateOf(savedConfig.ipApiKey) }
    var maxMindAccountId by remember(savedConfig) { mutableStateOf(savedConfig.maxMindAccountId) }
    var maxMindLicenseKey by remember(savedConfig) { mutableStateOf(savedConfig.maxMindLicenseKey) }
    var ipHubKey by remember(savedConfig) { mutableStateOf(savedConfig.ipHubKey) }
    var customEndpoint by remember(savedConfig) { mutableStateOf(savedConfig.customEndpoint) }
    var customKey by remember(savedConfig) { mutableStateOf(savedConfig.customKey) }
    val endpointIsValid = isHttpsEndpoint(customEndpoint)
    val customConfigured = customEndpoint.isNotBlank() && customKey.isNotBlank()
    val customPairIsValid = customEndpoint.isBlank() == customKey.isBlank()
    val maxMindPairIsValid = maxMindAccountId.isBlank() == maxMindLicenseKey.isBlank()
    val maxMindConfigured = maxMindAccountId.isNotBlank() && maxMindLicenseKey.isNotBlank()
    val formIsValid = endpointIsValid && customPairIsValid && maxMindPairIsValid
    val configuredCount = listOf(abuseKey.isNotBlank(), ipApiKey.isNotBlank(), maxMindConfigured, ipHubKey.isNotBlank(), customConfigured).count { it }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.VpnKey, contentDescription = null, tint = Blue) },
        title = { Text("授权数据源 Key", fontWeight = FontWeight.Bold, color = Ink) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("不填写任何授权也可以检测：APP 会自动使用可公开访问的默认来源。填写下方服务商凭据后，仅在本机按需叠加相应结果。所有凭据以 Android Keystore AES-GCM 加密保存，不上传、不写日志、不放入 URL。", fontSize = 12.sp, color = MutedInk, lineHeight = 18.sp)
                Spacer(Modifier.height(12.dp))
                Text("默认公共检测（自动启用）", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                Spacer(Modifier.height(6.dp))
                ProviderSourceLine("默认公共源", "已启用", "ipify、ipapi.co、ipwho.is、ProxyCheck 与 Tor Project；无需 Key。")
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Border)
                Spacer(Modifier.height(10.dp))
                Text("可选授权服务商（未填写时仍回退公共源）", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                Text("没有填写 Key 时，AbuseIPDB、ipapi.is、MaxMind 与 IPHub 不会被请求；它们不能合法地以空 Key 调用。", fontSize = 11.sp, color = MutedInk, lineHeight = 16.sp)
                Spacer(Modifier.height(8.dp))
                SecretTextField(abuseKey, { abuseKey = it.trim() }, "AbuseIPDB API Key", "可选增强：公开滥用置信分、报告量、独立报告者与最近报告")
                Spacer(Modifier.height(8.dp))
                SecretTextField(ipApiKey, { ipApiKey = it.trim() }, "ipapi.is API Key", "VPN、代理、Tor、托管、滥用与爬虫字段")
                Spacer(Modifier.height(8.dp))
                SecretTextField(maxMindAccountId, { maxMindAccountId = it.trim() }, "MaxMind Account ID", "与 License Key 成对使用，调用 GeoIP Insights")
                Spacer(Modifier.height(8.dp))
                SecretTextField(maxMindLicenseKey, { maxMindLicenseKey = it.trim() }, "MaxMind License Key", if (!maxMindPairIsValid) "请同时填写 MaxMind Account ID 与 License Key" else "HTTPS Basic Auth；匿名化和网络上下文字段")
                Spacer(Modifier.height(8.dp))
                SecretTextField(ipHubKey, { ipHubKey = it.trim() }, "IPHub API Key", "可选增强：调用 IPHub v2.2；代理、Tor、托管与低置信上下文")
                Spacer(Modifier.height(12.dp))
                Text("网页 / 非 API 服务商", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                Spacer(Modifier.height(5.dp))
                ProviderSourceLine("BrowserLeaks", "网页入口", "浏览器内 WebRTC、JavaScript、TLS、DNS 与指纹自检；不需要 Key，也不伪装为原生 API。")
                Spacer(Modifier.height(5.dp))
                ProviderSourceLine("EdgeOne MyIP", "当前不可用", "myip.edgeone.ai 在本次核验中无法解析；不自动请求，等待官方可用 API。")
                Spacer(Modifier.height(5.dp))
                ProviderSourceLine("NSTool", "未提供 API", "未发现可审计的 IP 情报接口；不自动请求、评分或下载外部 APK。")
                Spacer(Modifier.height(12.dp))
                Text("其他自定义（仅本地保存）", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = customEndpoint,
                    onValueChange = { customEndpoint = it.trim() },
                    label = { Text("自定义请求地址（HTTPS，可选）") },
                    supportingText = {
                        Text(
                            if (!endpointIsValid) "请输入完整 HTTPS 地址，例如：https://api.example.com/v1/check" else if (!customPairIsValid) "自定义地址与 Key 需要同时填写" else "当前仅加密保存，不会自动请求或参与评分",
                            color = if (formIsValid) MutedInk else Red
                        )
                    },
                    isError = !endpointIsValid || !customPairIsValid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                SecretTextField(customKey, { customKey = it.trim() }, "自定义 API Key（可选）", if (!customPairIsValid) "请同时填写 HTTPS 请求地址与自定义 Key" else "仅与自定义地址配套本地保存")
                Spacer(Modifier.height(8.dp))
                Text(
                    if (configuredCount > 0) "已配置 $configuredCount / 5 个可选授权服务商；公共默认检测始终保持启用。保存后会重新执行诊断。" else "尚未配置授权服务商：将自动使用默认公共检测源。",
                    fontSize = 11.sp,
                    color = MutedInk
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(ApiKeyConfig(abuseKey, ipApiKey, maxMindAccountId, maxMindLicenseKey, ipHubKey, customKey, customEndpoint)) },
                enabled = formIsValid
            ) { Text("加密保存") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (configuredCount > 0 || customEndpoint.isNotBlank() || customKey.isNotBlank()) {
                    OutlinedButton(onClick = onClear) { Text("清除全部") }
                }
                OutlinedButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}

@Composable
private fun ProviderSourceLine(name: String, state: String, detail: String) {
    val isEnabled = state == "已启用"
    val stateColor = if (isEnabled) Green else Blue
    val stateBackground = if (isEnabled) SoftGreen else SoftBlue
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Ink)
            Text(detail, fontSize = 10.sp, color = MutedInk, lineHeight = 14.sp)
        }
        Spacer(Modifier.width(8.dp))
        StatusBadge(state, stateColor, stateBackground)
    }
}

@Composable
private fun SecretTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    supportingText: String
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = { Text(supportingText) },
        visualTransformation = if (visible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "隐藏 $label" else "显示 $label",
                    tint = MutedInk
                )
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SecurityChecklistCard(
    items: List<SecurityChecklistItem>,
    completedIds: Set<String>,
    onToggle: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    val completedCount = items.count { it.id in completedIds }
    val visibleItems = items.filter { item ->
        search.isBlank() || listOf(item.category, item.title, item.guidance).any { it.contains(search.trim(), ignoreCase = true) }
    }
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("已完成 $completedCount / ${items.size} 项", fontWeight = FontWeight.Bold, color = Ink, fontSize = 17.sp)
                    Text("进度仅加密保存在当前设备；由你自行判断是否完成。", color = MutedInk, fontSize = 11.sp, lineHeight = 16.sp)
                }
                StatusBadge("离线", Green, SoftGreen)
                Spacer(Modifier.width(6.dp))
                TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "收起" else "查看") }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "这是面向日常设备、账户、网络和隐私维护的本地自评清单。它不会读取系统设置、扫描应用、判断设备是否安全，也不是合规或专业安全审计。",
                color = MutedInk,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("搜索清单") },
                    placeholder = { Text("例如：权限、Wi-Fi、密码") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                visibleItems.groupBy { it.category }.forEach { (category, categoryItems) ->
                    Text(category, fontWeight = FontWeight.SemiBold, color = Blue, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    categoryItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggle(item.id) }
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Checkbox(
                                checked = item.id in completedIds,
                                onCheckedChange = { onToggle(item.id) },
                                colors = CheckboxDefaults.colors(checkedColor = Green)
                            )
                            Spacer(Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f).padding(top = 10.dp)) {
                                Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Ink)
                                Text(item.guidance, fontSize = 11.sp, color = MutedInk, lineHeight = 16.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                }
                if (visibleItems.isEmpty()) Text("没有匹配的清单项。", color = MutedInk, fontSize = 12.sp)
                Text("提示：可随时清除进度；清单没有联网评分，也不会上传勾选内容。", color = MutedInk, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun PurityDiagnosisCard(
    report: PurityReport?,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        when {
            loading -> LoadingCard("正在比对出口、地理和网络属性…")
            report != null -> {
                val scoreColor = when {
                    report.score >= 90 -> Green
                    report.score >= 70 -> Amber
                    else -> Red
                }
                val scoreBackground = when {
                    report.score >= 90 -> SoftGreen
                    report.score >= 70 -> SoftAmber
                    else -> Color(0xFFFFECEC)
                }
                Column(modifier = Modifier.padding(17.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(scoreBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(formatRisk(report.score), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = scoreColor)
                                Text("/ 100", fontSize = 9.sp, color = scoreColor)
                            }
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(report.label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                            Text(report.summary, fontSize = 12.sp, color = MutedInk, lineHeight = 18.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("公开滥用风险：${formatRisk(report.risk)} / 100；网络透明度：${formatRisk(report.transparencyRisk)} / 100；证据覆盖：${formatRisk(report.coverage)} / 100（${report.coverageLabel}）。", fontSize = 11.sp, color = MutedInk, lineHeight = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("主分只反映公开滥用风险的反向展示；Tor、代理、VPN、IDC 与 ASN 单列为透明度或网络上下文，不被当作历史恶意。", fontSize = 10.sp, color = MutedInk, lineHeight = 15.sp)
                    Spacer(Modifier.height(9.dp))
                    report.buckets.forEach { bucket ->
                        PurityRiskBucketLine(bucket)
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = Border)
                    Spacer(Modifier.height(5.dp))
                    Text("证据明细", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink)
                    report.signals.forEachIndexed { index, signal ->
                        PuritySignalLine(signal)
                        if (index < report.signals.lastIndex) HorizontalDivider(color = Border, modifier = Modifier.padding(start = 28.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("检测时间：${report.checkedAt}", fontSize = 11.sp, color = MutedInk)
                    Spacer(Modifier.height(7.dp))
                    Text("说明：每一分均由来源、字段、时间和连续公式可回放计算；未覆盖字段不会被当作无风险。主分是可解释的公开滥用风险信号指数，不是欺诈概率，也不是任何第三方服务的专有风控值。", fontSize = 11.sp, color = MutedInk, lineHeight = 16.sp)
                }
            }
            else -> {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Red)
                        Spacer(Modifier.width(10.dp))
                        Text("纯净度诊断暂不可用", fontWeight = FontWeight.SemiBold, color = Ink)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(error ?: "暂时无法获取公开网络属性", color = MutedInk, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onRetry) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("重新检测")
                    }
                }
            }
        }
    }
}

@Composable
private fun PurityRiskBucketLine(bucket: PurityRiskBucket) {
    val tone = bucket.tone
    val color = when (tone) {
        PurityTone.CONSISTENT -> Green
        PurityTone.NOTICE -> Amber
        PurityTone.NEUTRAL -> MutedInk
    }
    val background = when (tone) {
        PurityTone.CONSISTENT -> SoftGreen
        PurityTone.NOTICE -> SoftAmber
        PurityTone.NEUTRAL -> Color(0xFFF0F3F5)
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (tone == PurityTone.CONSISTENT) Icons.Outlined.CheckCircle else Icons.Outlined.Info,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(bucket.title, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Ink)
            Text(bucket.detail, fontSize = 10.sp, color = MutedInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Text(bucket.value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun PuritySignalLine(signal: PuritySignal) {
    val color = when (signal.tone) {
        PurityTone.CONSISTENT -> Green
        PurityTone.NOTICE -> Amber
        PurityTone.NEUTRAL -> MutedInk
    }
    val background = when (signal.tone) {
        PurityTone.CONSISTENT -> SoftGreen
        PurityTone.NOTICE -> SoftAmber
        PurityTone.NEUTRAL -> Color(0xFFF0F3F5)
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(22.dp).clip(RoundedCornerShape(7.dp)).background(background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (signal.tone) {
                    PurityTone.CONSISTENT -> Icons.Outlined.CheckCircle
                    PurityTone.NOTICE -> Icons.Outlined.Info
                    PurityTone.NEUTRAL -> Icons.Outlined.Pending
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(signal.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink)
            Text(signal.detail, fontSize = 10.sp, color = MutedInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Text(signal.value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun LoadingCard(message: String, compact: Boolean = false) {
    Row(
        modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Blue)
        Spacer(Modifier.width(14.dp))
        Text(message, color = MutedInk, fontSize = 14.sp)
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Red)
            Spacer(Modifier.width(10.dp))
            Text("IP 信息暂不可用", fontWeight = FontWeight.SemiBold, color = Ink)
        }
        Spacer(Modifier.height(8.dp))
        Text(message, color = MutedInk, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        OutlinedButton(onClick = onRetry) {
            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(6.dp))
            Text("重试")
        }
    }
}

@Composable
private fun StatusBadge(label: String, color: Color, background: Color) {
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(background)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp
    )
}

@Composable
private fun InfoLine(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MutedInk, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(9.dp))
        Text(label, modifier = Modifier.width(52.dp), fontSize = 13.sp, color = MutedInk)
        Text(value.ifBlank { "—" }, modifier = Modifier.weight(1f), fontSize = 13.sp, color = Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ConnectivityGrid(endpoints: List<EndpointResult>, onRetryOne: (EndpointResult) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        endpoints.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { endpoint ->
                    EndpointCard(endpoint, Modifier.weight(1f), onRetryOne)
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EndpointCard(endpoint: EndpointResult, modifier: Modifier, onRetry: (EndpointResult) -> Unit) {
    val (statusColor, statusBackground, statusIcon) = when (endpoint.status) {
        CheckStatus.SUCCESS -> Triple(Green, SoftGreen, Icons.Outlined.CheckCircle)
        CheckStatus.FAILURE -> Triple(Red, Color(0xFFFFECEC), Icons.Outlined.ErrorOutline)
        CheckStatus.RUNNING -> Triple(Blue, SoftBlue, Icons.Outlined.Pending)
        CheckStatus.IDLE -> Triple(MutedInk, Color(0xFFF0F3F5), Icons.Outlined.Pending)
    }
    Card(
        modifier = modifier.clickable(enabled = endpoint.status != CheckStatus.RUNNING) { onRetry(endpoint) },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(statusBackground),
                    contentAlignment = Alignment.Center
                ) {
                    if (endpoint.status == CheckStatus.RUNNING) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.8.dp, color = statusColor)
                    } else {
                        Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(endpoint.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(9.dp))
            Text(
                if (endpoint.status == CheckStatus.SUCCESS && endpoint.latencyMs != null) "可达 · ${endpoint.latencyMs}ms" else endpoint.detail,
                fontSize = 12.sp,
                color = statusColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(endpoint.host, fontSize = 10.sp, color = MutedInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PrivacyCard(privacy: PrivacySnapshot, onRefresh: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(SoftBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.VpnLock, contentDescription = null, tint = Blue)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Android 网络隐私诊断", fontWeight = FontWeight.SemiBold, color = Ink)
                    Text("VPN、Private DNS、传输方式与系统网络能力", fontSize = 12.sp, color = MutedInk)
                }
                Icon(Icons.Outlined.ExpandMore, contentDescription = if (expanded) "收起" else "展开", tint = MutedInk)
            }
            Spacer(Modifier.height(13.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                AssistChip(
                    onClick = { },
                    label = { Text(if (privacy.vpnActive) "VPN 已连接" else "未检测到 VPN", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (privacy.vpnActive) SoftGreen else SoftAmber,
                        labelColor = if (privacy.vpnActive) Green else Amber
                    )
                )
                AssistChip(
                    onClick = { },
                    label = { Text("Private DNS：${privacy.privateDnsMode}", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = SoftBlue, labelColor = Blue)
                )
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Border)
                Spacer(Modifier.height(10.dp))
                InfoLine(Icons.Outlined.Dns, "DNS", privacy.dnsServers.ifEmpty { listOf("系统未返回可读 DNS 地址") }.joinToString(" · "))
                InfoLine(Icons.Outlined.NetworkCheck, "传输", privacy.transport)
                InfoLine(Icons.Outlined.CheckCircle, "系统验证", if (privacy.validated) "已验证可访问互联网" else "未验证；可能是受限网络、登录页或当前检测未完成")
                InfoLine(Icons.Outlined.Info, "计费", if (privacy.metered) "按流量计费或系统未知" else "系统标记为非计费网络")
                InfoLine(Icons.Outlined.SettingsEthernet, "网络接口", privacy.interfaceName.ifBlank { "系统未提供" })
                if (privacy.privateDnsHostname.isNotBlank()) InfoLine(Icons.Outlined.Dns, "Private DNS 主机名", privacy.privateDnsHostname)
                if (privacy.downstreamKbps > 0 || privacy.upstreamKbps > 0) InfoLine(Icons.Outlined.Speed, "系统估算带宽", "下行 ${privacy.downstreamKbps.coerceAtLeast(0) / 1000} Mbps · 上行 ${privacy.upstreamKbps.coerceAtLeast(0) / 1000} Mbps")
                Text(
                    "说明：系统能力反映当前 Android 网络栈的即时报告，不等同于实测带宽、浏览器 WebRTC 候选地址或 JavaScript 指纹；应用不会伪造 WebRTC / DNS 泄漏结论。",
                    color = MutedInk,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text("刷新系统网络状态", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onRefresh() })
            }
        }
    }
}

@Composable
private fun SpeedCard(result: NetworkSpeedResult?, loading: Boolean, onMeasure: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(SoftGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Speed, contentDescription = null, tint = Green)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("快速网络测量", fontWeight = FontWeight.SemiBold, color = Ink)
                    Text("Cloudflare Edge · 延迟、抖动与下载吞吐", fontSize = 12.sp, color = MutedInk)
                }
            }
            Spacer(Modifier.height(14.dp))
            if (result == null) {
                Text("开始后会执行 5 次轻量延迟采样与最多 1 MB 下载测量；不会上传数据，不会自动运行。", fontSize = 12.sp, color = MutedInk, lineHeight = 18.sp)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    SpeedMetric("延迟", "${result.latencyMs}ms", Modifier.weight(1f))
                    SpeedMetric("抖动", "${formatRisk(result.jitterMs)}ms", Modifier.weight(1f))
                    SpeedMetric("下载", "${formatRisk(result.downloadMbps)} Mbps", Modifier.weight(1.3f))
                }
                Spacer(Modifier.height(8.dp))
                Text("本次下载 ${result.downloadedBytes / 1024} KB；结果受 Wi‑Fi/蜂窝、CDN 路由和后台流量影响，仅供参考。", fontSize = 10.sp, color = MutedInk, lineHeight = 15.sp)
            }
            Spacer(Modifier.height(13.dp))
            Button(
                onClick = onMeasure,
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("测量中")
                } else {
                    Icon(Icons.Outlined.Speed, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(if (result == null) "开始测量" else "重新测量")
                }
            }
        }
    }
}

@Composable
private fun SpeedMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(10.dp)).background(SoftBlue).padding(9.dp)) {
        Text(label, fontSize = 10.sp, color = MutedInk)
        Text(value, fontSize = 13.sp, color = Ink, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DnsLookupCard(
    host: String,
    result: DnsLookupResult?,
    loading: Boolean,
    recordType: String,
    onHostChange: (String) -> Unit,
    onRecordTypeChange: (String) -> Unit,
    onLookup: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            NativeToolHeader(Icons.Outlined.Dns, "DNS 解析", "系统解析 + Cloudflare、Google、Quad9、DNS.SB 的 DoH 交叉查询")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = host,
                onValueChange = onHostChange,
                label = { Text("域名或主机名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = recordType,
                onValueChange = onRecordTypeChange,
                label = { Text("记录类型：A、AAAA、TXT、MX、NS、CNAME") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Button(onClick = onLookup, enabled = !loading, colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("解析中")
                } else {
                    Icon(Icons.Outlined.Dns, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("开始解析")
                }
            }
            result?.let {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Border)
                Spacer(Modifier.height(10.dp))
                if (it.error != null) {
                    ResultMessage(it.error, Red)
                } else {
                    Text("${it.host} 的 ${it.recordType} 系统解析", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                    Spacer(Modifier.height(5.dp))
                    Text(it.addresses.joinToString("\n").ifBlank { if (it.recordType in setOf("A", "AAAA")) "系统未返回 ${it.recordType} 地址" else "该记录类型由公共 DoH 解析器提供交叉结果" }, fontSize = 12.sp, color = MutedInk, lineHeight = 18.sp)
                    val consensus = analyzeDnsConsensus(it)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("解析器对比", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                            Text(consensus.detail, fontSize = 10.sp, color = MutedInk, lineHeight = 14.sp)
                        }
                        val consensusColors = when (consensus.tone) {
                            DnsConsensusTone.NEUTRAL -> MutedInk to SoftBlue
                            DnsConsensusTone.POSITIVE -> Green to SoftGreen
                            DnsConsensusTone.NOTICE -> Amber to SoftAmber
                        }
                        StatusBadge(consensus.label, consensusColors.first, consensusColors.second)
                    }
                    if (it.resolverResults.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("公共 DNS 交叉核验（含 DNS.SB）", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                        Spacer(Modifier.height(6.dp))
                        it.resolverResults.forEachIndexed { index, resolver ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(resolver.resolver, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(resolver.error ?: resolver.addresses.joinToString(" · ").ifBlank { "无 A / AAAA 记录" }, color = if (resolver.error == null) MutedInk else Red, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                                StatusBadge(resolver.status, if (resolver.error == null) Green else Red, if (resolver.error == null) SoftGreen else Color(0xFFFFECEC))
                            }
                            if (index < it.resolverResults.lastIndex) Spacer(Modifier.height(7.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("提示：不同解析器返回的地址差异可能来自 CDN、地域策略或 DNS 安全策略；这不是浏览器 DNS 泄漏结论。", color = MutedInk, fontSize = 10.sp, lineHeight = 15.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = onShare, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(7.dp)); Text("分享 DNS 结果")
                    }
                }
            }
        }
    }
}

private fun analyzeDnsConsensus(result: DnsLookupResult): DnsConsensus {
    val successful = result.resolverResults.filter { it.error == null }
    val remoteSets = successful.map { resolver -> resolver.addresses.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet() }
    val populated = remoteSets.filter { it.isNotEmpty() }
    val systemSet = result.addresses.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
    return when {
        successful.isEmpty() -> DnsConsensus("未覆盖", "公共 DoH 解析器均未返回可用结果。", DnsConsensusTone.NEUTRAL)
        populated.isEmpty() -> DnsConsensus("无记录", "可访问的公共解析器均未返回该记录类型。", DnsConsensusTone.NEUTRAL)
        populated.distinct().size == 1 && (systemSet.isEmpty() || systemSet == populated.first()) -> DnsConsensus("一致", "可用解析器返回相同记录；不代表永久或全球一致。", DnsConsensusTone.POSITIVE)
        else -> DnsConsensus("有差异", "至少两个可用解析器或系统解析返回不同记录；可能是 CDN、地域、缓存或策略差异。", DnsConsensusTone.NOTICE)
    }
}

@Composable
private fun WhoisLookupCard(
    query: String,
    result: WhoisLookupResult?,
    loading: Boolean,
    onQueryChange: (String) -> Unit,
    onLookup: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            NativeToolHeader(Icons.Outlined.Storage, "Whois 查询", "直接连接公开 Whois 注册表，不保存查询记录")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("域名或 IP 地址") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Button(onClick = onLookup, enabled = !loading, colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("查询中")
                } else {
                    Icon(Icons.Outlined.Storage, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("查询注册信息")
                }
            }
            result?.let {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Border)
                Spacer(Modifier.height(10.dp))
                if (it.error != null) {
                    ResultMessage(it.error, Red)
                } else {
                    Text("注册表：${it.registry}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                    Spacer(Modifier.height(5.dp))
                    Text(it.lines.joinToString("\n"), fontSize = 12.sp, color = MutedInk, lineHeight = 18.sp, maxLines = 10, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun AsnLookupCard(
    query: String,
    result: AsnLookupResult?,
    loading: Boolean,
    onQueryChange: (String) -> Unit,
    onLookup: () -> Unit
) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CardSurface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(17.dp)) {
            NativeToolHeader(Icons.Outlined.Router, "ASN 信息", "通过 RIPE NCC RIPEstat 查询自治系统公开登记与路由概览")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = query, onValueChange = onQueryChange, label = { Text("ASN，例如 AS13335") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Button(onClick = onLookup, enabled = !loading, colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White) else Icon(Icons.Outlined.Router, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(7.dp)); Text(if (loading) "查询中" else "查询 ASN")
            }
            result?.let { item ->
                Spacer(Modifier.height(12.dp)); HorizontalDivider(color = Border); Spacer(Modifier.height(10.dp))
                if (item.error != null) ResultMessage(item.error, Red) else {
                    Text(item.asn, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Ink)
                    InfoLine(Icons.Outlined.Business, "名称", item.name.ifBlank { "—" })
                    InfoLine(Icons.Outlined.LocationOn, "国家/地区", item.countryCode.ifBlank { "—" })
                    InfoLine(Icons.Outlined.Schedule, "分配日期", item.allocatedAt.ifBlank { "—" })
                    if (item.description.isNotBlank()) Text(item.description, fontSize = 11.sp, color = MutedInk, lineHeight = 16.sp, modifier = Modifier.padding(top = 5.dp))
                    if (item.neighbours.isNotEmpty()) {
                        Spacer(Modifier.height(9.dp))
                        Text("观测到的 ASN 邻居（最多显示 12 个）", fontSize = 12.sp, color = Ink, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        item.neighbours.forEach { neighbour ->
                            Text("${neighbour.asn} · ${neighbour.position}${neighbour.pathCount?.let { " · 路径 $it" }.orEmpty()}", fontSize = 11.sp, color = MutedInk, lineHeight = 16.sp)
                        }
                    }
                    if (item.website.isNotBlank()) Text(item.website, fontSize = 11.sp, color = Blue, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
    }
}

@Composable
private fun MacLookupCard(
    query: String,
    result: MacLookupResult?,
    loading: Boolean,
    onQueryChange: (String) -> Unit,
    onLookup: () -> Unit
) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CardSurface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(17.dp)) {
            NativeToolHeader(Icons.Outlined.SettingsEthernet, "MAC Lookup", "查询 MAC 地址前缀的公开厂商登记，不读取设备 MAC")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = query, onValueChange = onQueryChange, label = { Text("MAC，例如 FC:FB:FB:01:FA:21") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Button(onClick = onLookup, enabled = !loading, colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White) else Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(7.dp)); Text(if (loading) "查询中" else "查询厂商")
            }
            result?.let { item ->
                Spacer(Modifier.height(12.dp)); HorizontalDivider(color = Border); Spacer(Modifier.height(10.dp))
                if (item.error != null) ResultMessage(item.error, Red) else {
                    Text(item.mac.chunked(2).joinToString(":"), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink)
                    InfoLine(Icons.Outlined.Business, "厂商", item.vendor)
                    if (item.isLocallyAdministered) Text("此地址设置了本地管理位，常见于随机化/虚拟 MAC；公开 OUI 厂商仅供参考。", color = Amber, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
    }
}

@Composable
private fun MtrCard(
    target: String,
    result: MtrResult?,
    loading: Boolean,
    onTargetChange: (String) -> Unit,
    onRun: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            NativeToolHeader(Icons.Outlined.NetworkCheck, "Globalping MTR", "从美国、德国和新加坡各选 1 个远端探针进行逐跳路径追踪")
            Spacer(Modifier.height(10.dp))
            Text("仅在点击后发起，单次最多使用 3 个 Globalping 测试。中间跳丢包可能是 ICMP 限速；只有从某一跳开始持续丢包才值得进一步调查。", color = MutedInk, fontSize = 11.sp, lineHeight = 16.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = target,
                onValueChange = onTargetChange,
                label = { Text("目标域名或 IP（留空使用当前 IPv4）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(9.dp))
            Button(onClick = onRun, enabled = !loading, colors = ButtonDefaults.buttonColors(containerColor = Blue), modifier = Modifier.fillMaxWidth()) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(17.dp), color = Color.White, strokeWidth = 2.dp)
                else Icon(Icons.Outlined.NetworkCheck, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(7.dp)); Text(if (loading) "MTR 测量中…" else "开始 Globalping MTR")
            }
            result?.error?.let {
                Spacer(Modifier.height(9.dp)); ResultMessage(it, Red)
            }
            if (result != null && result.error == null) {
                Spacer(Modifier.height(11.dp))
                Text("目标：${result.target} · 测量状态：${result.status}", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                result.probes.forEach { probe ->
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Border)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(probe.location.ifBlank { "位置未知" }, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(listOf(probe.network, probe.asn).filter(String::isNotBlank).joinToString(" · ").ifBlank { "探针网络未知" }, color = MutedInk, fontSize = 10.sp)
                        }
                        StatusBadge(probe.status.ifBlank { "未知" }, if (probe.status.equals("finished", true)) Green else Amber, if (probe.status.equals("finished", true)) SoftGreen else SoftAmber)
                    }
                    if (probe.hops.isEmpty()) {
                        Text("该探针未返回可解析的逐跳结果。", color = MutedInk, fontSize = 11.sp, modifier = Modifier.padding(top = 7.dp))
                    } else {
                        probe.hops.take(64).forEach { hop ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${hop.number}", color = MutedInk, fontSize = 10.sp, modifier = Modifier.width(24.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(listOf(hop.host, hop.ip).filter(String::isNotBlank).joinToString(" · ").ifBlank { "未知节点" }, color = Ink, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(hop.asn.takeIf { it.isNotBlank() } ?: "ASN 未知", color = MutedInk, fontSize = 9.sp)
                                }
                                Text(
                                    listOfNotNull(hop.avgMs?.let { "平均 ${formatRisk(it)}ms" }, hop.lossPct?.let { "丢包 ${formatRisk(it)}%" }).joinToString("\n").ifBlank { "—" },
                                    color = if ((hop.lossPct ?: 0.0) > 0) Amber else MutedInk,
                                    fontSize = 9.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                        }
                        if (probe.hops.size > 64) Text("仅显示前 64 跳。", color = MutedInk, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun GlobalPingCard(
    target: String,
    result: GlobalPingResult?,
    loading: Boolean,
    onTargetChange: (String) -> Unit,
    onRun: () -> Unit
) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CardSurface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(17.dp)) {
            NativeToolHeader(Icons.Outlined.Language, "Globalping 全球延迟", "从美国、德国和新加坡各选 1 个远端探针执行 ping")
            Spacer(Modifier.height(8.dp))
            Text("仅在点击后发起。单次消耗最多 3 个 Globalping 测试额度；结果是远端探针到目标的测量，不是本机网络延迟。", fontSize = 11.sp, color = MutedInk, lineHeight = 16.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = target, onValueChange = onTargetChange, label = { Text("域名或 IP 地址") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Button(onClick = onRun, enabled = !loading, colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White) else Icon(Icons.Outlined.Language, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(7.dp)); Text(if (loading) "全球探测中" else "运行全球 Ping")
            }
            result?.let { item ->
                Spacer(Modifier.height(12.dp)); HorizontalDivider(color = Border); Spacer(Modifier.height(10.dp))
                if (item.error != null) ResultMessage(item.error, Red) else {
                    Text("${item.target} · ${item.status}", fontWeight = FontWeight.SemiBold, color = Ink, fontSize = 13.sp)
                    item.probes.forEach { probe ->
                        Spacer(Modifier.height(6.dp))
                        Text("${probe.location} · ${probe.network.ifBlank { "网络未知" } }", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("${probe.status}${probe.averageMs?.let { " · 平均 ${formatRisk(it)}ms" }.orEmpty()}${probe.packetLoss?.let { " · 丢包 ${formatRisk(it)}%" }.orEmpty()}", color = MutedInk, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun OoniCard(
    domain: String,
    country: String,
    result: OoniQueryResult?,
    loading: Boolean,
    onDomainChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    onRun: () -> Unit
) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CardSurface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(17.dp)) {
            NativeToolHeader(Icons.Outlined.Security, "OONI 历史测量", "查询指定国家中公开的 Web Connectivity 历史观测，不执行本机审查测试")
            Spacer(Modifier.height(8.dp))
            Text("每次最多取回 5 条元数据。异常或已确认状态反映历史志愿者测量，不能直接证明你此刻的网络被封锁。", fontSize = 11.sp, color = MutedInk, lineHeight = 16.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = domain, onValueChange = onDomainChange, label = { Text("域名") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = country, onValueChange = onCountryChange, label = { Text("国家代码，例如 US") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Button(onClick = onRun, enabled = !loading, colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White) else Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(7.dp)); Text(if (loading) "查询中" else "查询公开历史")
            }
            result?.let { item ->
                Spacer(Modifier.height(12.dp)); HorizontalDivider(color = Border); Spacer(Modifier.height(10.dp))
                if (item.error != null) ResultMessage(item.error, Red) else {
                    Text("${item.country} · ${item.count} 条近期公开测量", fontWeight = FontWeight.SemiBold, color = Ink, fontSize = 13.sp)
                    item.measurements.forEach { measurement ->
                        Spacer(Modifier.height(7.dp))
                        val label = when { measurement.confirmed -> "已确认异常"; measurement.anomaly -> "异常"; measurement.failure -> "测量失败"; else -> "未见异常" }
                        Text("$label · ${measurement.country} · ${measurement.asn}", color = if (measurement.confirmed || measurement.anomaly) Amber else Green, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("${measurement.measuredAt.take(19).replace("T", " ")} · ${measurement.verification.ifBlank { "验证状态未知" }}", color = MutedInk, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceStatusCard(
    probes: List<PortProbeResult>,
    officialStatuses: List<OfficialStatusResult>,
    loading: Boolean,
    officialLoading: Boolean,
    onProbe: () -> Unit,
    onRefreshOfficial: () -> Unit,
    onManageProbes: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            NativeToolHeader(Icons.Outlined.NetworkCheck, "服务状态", "区分当前网络端口可达性与厂商官方状态页摘要")
            Spacer(Modifier.height(9.dp))
            Text("当前网络连通性", fontSize = 12.sp, color = MutedInk, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(5.dp))
            probes.forEachIndexed { index, probe ->
                ServiceProbeLine(probe)
                if (index < probes.lastIndex) HorizontalDivider(color = Border, modifier = Modifier.padding(vertical = 8.dp))
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(10.dp))
            Text("厂商官方状态摘要", fontSize = 12.sp, color = MutedInk, fontWeight = FontWeight.SemiBold)
            Text("来自各厂商公开状态页；不代表本机是否一定可访问。", fontSize = 10.sp, color = MutedInk, lineHeight = 15.sp, modifier = Modifier.padding(top = 3.dp, bottom = 7.dp))
            officialStatuses.forEachIndexed { index, status ->
                OfficialStatusLine(status)
                if (index < officialStatuses.lastIndex) HorizontalDivider(color = Border, modifier = Modifier.padding(vertical = 8.dp))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onProbe, enabled = !loading, modifier = Modifier.weight(1f)) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Blue) else Icon(Icons.Outlined.NetworkCheck, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp)); Text(if (loading) "探测中" else "端口探测")
                }
                OutlinedButton(onClick = onRefreshOfficial, enabled = !officialLoading, modifier = Modifier.weight(1f)) {
                    if (officialLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Blue) else Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp)); Text(if (officialLoading) "刷新中" else "刷新官方")
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onManageProbes, modifier = Modifier.align(Alignment.End)) { Text("管理端口目标") }
        }
    }
}

@Composable
private fun OfficialStatusLine(status: OfficialStatusResult) {
    val isOkay = status.indicator.equals("none", true)
    val isError = status.error != null
    val color = when { isError -> Red; isOkay -> Green; else -> Amber }
    val background = when { isError -> Color(0xFFFFECEC); isOkay -> SoftGreen; else -> SoftAmber }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { }) {
        Column(modifier = Modifier.weight(1f)) {
            Text(status.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Ink)
            Text(status.error ?: status.description, fontSize = 11.sp, color = if (isError) Red else MutedInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (status.updatedAt.isNotBlank()) Text("更新：${status.updatedAt.take(19).replace("T", " ")}", fontSize = 10.sp, color = MutedInk)
        }
        StatusBadge(if (isError) "错误" else if (isOkay) "正常" else "注意", color, background)
    }
}

@Composable
private fun DeviceEnvironmentCard(context: Context) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            NativeToolHeader(Icons.Outlined.Language, "设备环境", "显示原生 Android 能可靠读取的客户端环境信息")
            Spacer(Modifier.height(10.dp))
            InfoLine(Icons.Outlined.SettingsEthernet, "系统", "Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}")
            InfoLine(Icons.Outlined.Public, "语言", java.util.Locale.getDefault().toLanguageTag())
            InfoLine(Icons.Outlined.Info, "应用", context.packageName)
            Spacer(Modifier.height(6.dp))
            Text("原生 APP 没有网页 JavaScript 与 WebRTC 运行环境，因此不会伪造浏览器指纹或 WebRTC 泄漏结论。", fontSize = 12.sp, color = MutedInk, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun BrowserFallbackCard(context: Context) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFCFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            NativeToolHeader(Icons.Outlined.ArrowOutward, "网页专属诊断（外部入口）", "仅在需要浏览器 JavaScript、WebRTC、Canvas/WebGL、TLS 或 DNS 泄漏自检时使用")
            Spacer(Modifier.height(8.dp))
            Text("原生 Android 已完成 DNS、Whois、服务状态和设备网络信息；不会伪造浏览器指纹或 WebRTC 测试结论。", fontSize = 12.sp, color = MutedInk, lineHeight = 18.sp)
            Spacer(Modifier.height(10.dp))
            BrowserToolLink(context, "BrowserLeaks 自检", "浏览器指纹、WebRTC、Canvas/WebGL 与 TLS/DNS 相关自检", "https://browserleaks.com")
            Spacer(Modifier.height(7.dp))
            BrowserToolLink(context, "IPCheck WebRTC Leak", "网页 WebRTC 候选地址、NAT 和 SDP 日志", "https://ipcheck.ing/#webrtc")
            Spacer(Modifier.height(7.dp))
            BrowserToolLink(context, "IPCheck DNS Leak", "浏览器多端点 DNS 泄漏测试", "https://ipcheck.ing/#dns-leak")
            Spacer(Modifier.height(7.dp))
            BrowserToolLink(context, "IPCheck 高级工具", "全球延迟、MTR、代理规则、审查、Persona 等需要网页或远端探针的工具", "https://ipcheck.ing")
            Spacer(Modifier.height(8.dp))
            Text("这些页面在浏览器运行，不会把网页结果静默写回 APP 或混入纯净度评分。EdgeOne MyIP 当前无法解析，NSTool 未公开可审计 IP API，二者仍不会被自动请求或下载 APK。", fontSize = 11.sp, color = MutedInk, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun BrowserToolLink(context: Context, title: String, detail: String, url: String) {
    OutlinedButton(
        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(Icons.Outlined.ArrowOutward, contentDescription = null, modifier = Modifier.size(17.dp), tint = Blue)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(title, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = MutedInk, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun NativeToolHeader(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(SoftBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Blue, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, color = Ink)
            Text(subtitle, fontSize = 12.sp, color = MutedInk, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun ServiceProbeLine(probe: PortProbeResult) {
    val color = when (probe.status) {
        CheckStatus.SUCCESS -> Green
        CheckStatus.FAILURE -> Red
        CheckStatus.RUNNING -> Blue
        CheckStatus.IDLE -> MutedInk
    }
    val label = when (probe.status) {
        CheckStatus.SUCCESS -> "端口可达 · ${probe.latencyMs ?: 0}ms"
        else -> probe.detail
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = when (probe.status) {
                CheckStatus.SUCCESS -> Icons.Outlined.CheckCircle
                CheckStatus.FAILURE -> Icons.Outlined.ErrorOutline
                else -> Icons.Outlined.Pending
            },
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("${probe.host}:${probe.port}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
            Text(label, fontSize = 11.sp, color = color)
        }
    }
}

@Composable
private fun ResultMessage(message: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(7.dp))
        Text(message, fontSize = 12.sp, color = color)
    }
}

@Composable
private fun Footer() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = MutedInk, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text("NetScope 是独立的非官方 Android 客户端", fontSize = 11.sp, color = MutedInk)
        }
        Spacer(Modifier.height(5.dp))
        Text("界面信息架构参考 MyIP / IPCheck.ing 的公开功能说明", fontSize = 10.sp, color = MutedInk)
    }
}

private fun parseMtrOutput(rawOutput: String): List<MtrHop> {
    if (rawOutput.isBlank()) return emptyList()
    val columnFields = mapOf(
        "loss%" to "loss", "loss" to "loss", "last" to "last", "avg" to "avg",
        "wrst" to "worst", "worst" to "worst"
    )
    var columns: List<String>? = null
    val seen = mutableSetOf<Int>()
    val hops = mutableListOf<MtrHop>()
    val hopPattern = Regex("^\\\\s*(\\\\d{1,2})\\\\.(?:\\\\|--)?\\\\s+(.*)$")
    rawOutput.lineSequence().forEach { line ->
        if (columns == null && line.contains("Host", ignoreCase = true) && !hopPattern.matches(line)) {
            columns = line.trim().split(Regex("\\\\s+")).drop(1).map { columnFields[it.lowercase()] ?: "" }
            return@forEach
        }
        val match = hopPattern.matchEntire(line) ?: return@forEach
        val number = match.groupValues[1].toIntOrNull() ?: return@forEach
        if (number !in 1..64 || !seen.add(number)) return@forEach
        val tokens = match.groupValues[2].trim().split(Regex("\\\\s+")).toMutableList()
        val numbers = mutableListOf<Double>()
        while (tokens.isNotEmpty()) {
            val tail = tokens.last()
            val value = tail.removeSuffix("%").toDoubleOrNull()
            if (value == null) break
            numbers.add(0, value)
            tokens.removeAt(tokens.lastIndex)
        }
        var asn = ""
        tokens.firstOrNull()?.takeIf { it.startsWith("AS", true) }?.let {
            asn = it
            tokens.removeAt(0)
        }
        var ip = ""
        tokens.lastOrNull()?.takeIf { it.startsWith("(") && it.endsWith(")") }?.let {
            val candidate = it.removePrefix("(").removeSuffix(")")
            if (isIpLiteral(candidate)) { ip = candidate; tokens.removeAt(tokens.lastIndex) }
        }
        val hostText = tokens.joinToString(" ").takeIf { it.isNotBlank() && it != "???" && !it.startsWith("(waiting", true) }.orEmpty()
        val host = if (hostText.isNotBlank() && isIpLiteral(hostText)) { ip = ip.ifBlank { hostText }; "" } else hostText
        val values = mutableMapOf<String, Double>()
        columns?.let { header ->
            val offset = header.size - numbers.size
            numbers.forEachIndexed { index, value ->
                val field = header.getOrNull(offset + index).orEmpty()
                if (field.isNotBlank()) values[field] = value
            }
        }
        hops += MtrHop(number, host, ip, asn, values["loss"], values["last"], values["avg"], values["worst"])
    }
    return hops
}

private object NetworkRepository {
    private const val IPIFY_V4 = "https://api.ipify.org?format=json"
    private const val IPIFY_DUAL = "https://api64.ipify.org?format=json"

    suspend fun loadIpSnapshot(): IpSnapshot = withContext(Dispatchers.IO) {
        val ipv4 = fetchIp(IPIFY_V4)
        val dualStackIp = runCatching { fetchIp(IPIFY_DUAL) }.getOrNull()
        val ipv6 = dualStackIp?.takeIf { it.contains(":") }
        val geo = JSONObject(getText("https://ipapi.co/$ipv4/json/"))
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        IpSnapshot(
            ipv4 = ipv4,
            ipv6 = ipv6,
            country = geo.stringOrBlank("country_name"),
            region = geo.stringOrBlank("region"),
            city = geo.stringOrBlank("city"),
            timezone = geo.stringOrBlank("timezone"),
            isp = geo.stringOrBlank("org"),
            asn = geo.stringOrBlank("asn"),
            networkType = geo.stringOrBlank("version").ifBlank { "IPv4" },
            refreshedAt = now
        )
    }

    suspend fun lookupIpSnapshot(ip: String): IpSnapshot = withContext(Dispatchers.IO) {
        val normalized = ip.trim().removePrefix("[").removeSuffix("]")
        val geo = JSONObject(getText("https://ipapi.co/${URLEncoder.encode(normalized, Charsets.UTF_8.name())}/json/"))
        if (geo.stringOrBlank("error").isNotBlank()) throw IllegalStateException(geo.stringOrBlank("reason").ifBlank { "此 IP 暂无可用地理信息" })
        IpSnapshot(
            ipv4 = normalized,
            ipv6 = normalized.takeIf { it.contains(":") },
            country = geo.stringOrBlank("country_name"),
            region = geo.stringOrBlank("region"),
            city = geo.stringOrBlank("city"),
            timezone = geo.stringOrBlank("timezone"),
            isp = geo.stringOrBlank("org"),
            asn = geo.stringOrBlank("asn"),
            networkType = geo.stringOrBlank("version").ifBlank { if (normalized.contains(":")) "IPv6" else "IPv4" },
            refreshedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        )
    }

    suspend fun testEndpoint(endpoint: EndpointResult): EndpointResult = withContext(Dispatchers.IO) {
        val started = System.nanoTime()
        try {
            val connection = (URL(endpoint.url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7_000
                readTimeout = 7_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "NetScope Android/1.0")
            }
            val code = connection.responseCode
            val elapsed = (System.nanoTime() - started) / 1_000_000
            connection.disconnect()
            if (code in 200..499) {
                endpoint.copy(status = CheckStatus.SUCCESS, latencyMs = elapsed, detail = "可达")
            } else {
                endpoint.copy(status = CheckStatus.FAILURE, latencyMs = elapsed, detail = "HTTP $code")
            }
        } catch (error: Exception) {
            endpoint.copy(status = CheckStatus.FAILURE, detail = error.asUserMessage())
        }
    }

    fun measureCloudflareSpeed(): NetworkSpeedResult {
        val latencyUrl = "https://speed.cloudflare.com/__down?bytes=0"
        val latencySamples = (1..5).map {
            val started = System.nanoTime()
            getText(latencyUrl)
            (System.nanoTime() - started) / 1_000_000
        }
        val latency = latencySamples.sorted()[latencySamples.size / 2]
        val jitter = latencySamples.zipWithNext { first, second -> abs(first - second).toDouble() }.average().takeIf { !it.isNaN() } ?: 0.0
        val maxBytes = 1_000_000L
        val connection = (URL("https://speed.cloudflare.com/__down?bytes=$maxBytes&cb=${System.currentTimeMillis()}").openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 15_000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "NetScope Android/1.0")
        }
        val started = System.nanoTime()
        val bytes = try {
            if (connection.responseCode !in 200..299) throw IllegalStateException("测速服务返回 HTTP ${connection.responseCode}")
            connection.inputStream.use { input ->
                val buffer = ByteArray(16 * 1024)
                var total = 0L
                while (total < maxBytes) {
                    val read = input.read(buffer, 0, minOf(buffer.size.toLong(), maxBytes - total).toInt())
                    if (read < 0) break
                    total += read
                }
                total
            }
        } finally {
            connection.disconnect()
        }
        val elapsedSeconds = ((System.nanoTime() - started) / 1_000_000_000.0).coerceAtLeast(0.001)
        return NetworkSpeedResult(latency, jitter, (bytes * 8.0 / 1_000_000.0) / elapsedSeconds, bytes)
    }

    fun resolveDns(host: String, recordType: String = "A"): DnsLookupResult {
        val type = recordType.uppercase()
        val addresses = if (type in setOf("A", "AAAA")) {
            InetAddress.getAllByName(host)
                .mapNotNull { it.hostAddress }
                .filter { if (type == "A") !it.contains(":") else it.contains(":") }
                .distinct()
                .sorted()
        } else emptyList()
        val resolvers = listOf(
            "Cloudflare" to "https://cloudflare-dns.com/dns-query",
            "Google Public DNS" to "https://dns.google/resolve",
            "Quad9" to "https://dns.quad9.net/dns-query",
            "DNS.SB" to "https://doh.dns.sb/dns-query?"
        )
        val remoteResults = resolvers.map { (name, endpoint) ->
            runCatching { resolveDnsOverHttps(name, endpoint, host, type) }
                .getOrElse { error -> DnsResolverResult(name, emptyList(), "错误", error.asUserMessage()) }
        }
        if (addresses.isEmpty() && remoteResults.all { it.addresses.isEmpty() }) throw IllegalStateException("未获得可用 $type 记录")
        return DnsLookupResult(host = host, recordType = type, addresses = addresses, resolverResults = remoteResults)
    }

    private fun resolveDnsOverHttps(name: String, endpoint: String, host: String, recordType: String): DnsResolverResult {
        val encodedHost = URLEncoder.encode(host, Charsets.UTF_8.name()).replace("+", "%20")
        val separator = if (endpoint.contains("?")) "&" else "?"
        val json = JSONObject(getText("$endpoint${separator}name=$encodedHost&type=$recordType", mapOf("Accept" to "application/dns-json")))
        val statusCode = json.intOrNull("Status")
        val answers = json.optJSONArray("Answer")
        val addresses = buildList {
            if (answers != null) {
                for (index in 0 until answers.length()) {
                    val answer = answers.optJSONObject(index) ?: continue
                    answer.stringOrBlank("data").trimEnd('.').takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }.distinct()
        return DnsResolverResult(
            resolver = name,
            addresses = addresses,
            status = if (statusCode == 0) "正常" else "DNS $statusCode",
            error = if (statusCode == null) "未返回 DNS 状态" else null
        )
    }

    fun runGlobalMtr(target: String): MtrResult {
        val locations = JSONArray().apply {
            put(JSONObject().put("country", "US").put("limit", 1))
            put(JSONObject().put("country", "DE").put("limit", 1))
            put(JSONObject().put("country", "SG").put("limit", 1))
        }
        val request = JSONObject()
            .put("target", target)
            .put("type", "mtr")
            .put("locations", locations)
            .put("measurementOptions", JSONObject().put("port", 80).put("protocol", "ICMP"))
        var response = JSONObject(postJson("https://api.globalping.io/v1/measurements", request.toString()))
        val measurementId = response.stringOrBlank("id").ifBlank { throw IllegalStateException("Globalping 未返回 MTR 测量编号") }
        for (attempt in 0 until 4) {
            val state = response.stringOrBlank("status")
            if (state.isNotBlank() && !state.equals("in-progress", ignoreCase = true)) break
            Thread.sleep(1_200)
            response = JSONObject(getText("https://api.globalping.io/v1/measurements/${URLEncoder.encode(measurementId, Charsets.UTF_8.name())}"))
        }
        val results = response.optJSONArray("results")
        val probes = buildList {
            if (results != null) {
                for (index in 0 until results.length()) {
                    val item = results.optJSONObject(index) ?: continue
                    val probe = item.optJSONObject("probe")
                    val result = item.optJSONObject("result")
                    val location = listOf(probe?.stringOrBlank("city"), probe?.stringOrBlank("country")).filter { !it.isNullOrBlank() }.joinToString(", ").ifBlank { "位置未知" }
                    val rawOutput = result?.stringOrBlank("rawOutput").orEmpty()
                    add(MtrProbeResult(location, probe?.stringOrBlank("network").orEmpty(), probe?.stringOrBlank("asn").orEmpty(), result?.stringOrBlank("status").orEmpty().ifBlank { "未知" }, parseMtrOutput(rawOutput)))
                }
            }
        }
        return MtrResult(target, measurementId, response.stringOrBlank("status").ifBlank { "已创建" }, probes)
    }

    fun runGlobalPing(target: String): GlobalPingResult {
        val locations = JSONArray().apply {
            put(JSONObject().put("country", "US").put("limit", 1))
            put(JSONObject().put("country", "DE").put("limit", 1))
            put(JSONObject().put("country", "SG").put("limit", 1))
        }
        val request = JSONObject()
            .put("target", target)
            .put("type", "ping")
            .put("locations", locations)
            .put("measurementOptions", JSONObject().put("packets", 3))
        var response = JSONObject(postJson("https://api.globalping.io/v1/measurements", request.toString()))
        val measurementId = response.stringOrBlank("id").ifBlank { throw IllegalStateException("Globalping 未返回测量编号") }
        for (attempt in 0 until 4) {
            val state = response.stringOrBlank("status")
            if (state.isNotBlank() && !state.equals("in-progress", ignoreCase = true)) break
            Thread.sleep(1_200)
            response = JSONObject(getText("https://api.globalping.io/v1/measurements/${URLEncoder.encode(measurementId, Charsets.UTF_8.name())}"))
        }
        val results = response.optJSONArray("results")
        val probes = buildList {
            if (results != null) {
                for (index in 0 until results.length()) {
                    val item = results.optJSONObject(index) ?: continue
                    val probe = item.optJSONObject("probe")
                    val result = item.optJSONObject("result")
                    val stats = result?.optJSONObject("stats")
                    val location = listOf(probe?.stringOrBlank("city"), probe?.stringOrBlank("country")).filter { !it.isNullOrBlank() }.joinToString(", ").ifBlank { "位置未知" }
                    val network = probe?.stringOrBlank("network").orEmpty()
                    val average = stats?.opt("avg")?.toString()?.toDoubleOrNull()
                    val loss = stats?.opt("loss")?.toString()?.toDoubleOrNull()
                    add(GlobalPingProbeResult(location, network, average, loss, result?.stringOrBlank("status").orEmpty().ifBlank { "结果未知" }))
                }
            }
        }
        return GlobalPingResult(target, measurementId, probes, response.stringOrBlank("status").ifBlank { "已创建" })
    }

    fun queryOoni(domain: String, country: String): OoniQueryResult {
        val encodedDomain = URLEncoder.encode(domain, Charsets.UTF_8.name()).replace("+", "%20")
        val query = "https://api.ooni.io/api/v1/measurements?probe_cc=${URLEncoder.encode(country, Charsets.UTF_8.name())}&test_name=web_connectivity&domain=$encodedDomain&limit=5"
        val json = JSONObject(getText(query))
        val metadata = json.optJSONObject("metadata")
        val array = json.optJSONArray("results")
        val measurements = buildList {
            if (array != null) {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(OoniMeasurementResult(
                        input = item.stringOrBlank("input"),
                        country = item.stringOrBlank("probe_cc"),
                        asn = item.stringOrBlank("probe_asn"),
                        measuredAt = item.stringOrBlank("measurement_start_time"),
                        anomaly = item.booleanOrNull("anomaly") == true,
                        confirmed = item.booleanOrNull("confirmed") == true,
                        failure = item.booleanOrNull("failure") == true,
                        verification = item.stringOrBlank("verification_status")
                    ))
                }
            }
        }
        return OoniQueryResult(domain, country, metadata?.intOrNull("count") ?: measurements.size, measurements)
    }

    fun fetchOfficialStatus(source: OfficialStatusResult): OfficialStatusResult {
        val json = JSONObject(getText(source.endpoint))
        val status = json.optJSONObject("status") ?: throw IllegalStateException("官方状态页未返回状态摘要")
        val page = json.optJSONObject("page")
        return source.copy(
            indicator = status.stringOrBlank("indicator").ifBlank { "unknown" },
            description = status.stringOrBlank("description").ifBlank { "官方状态页未提供说明" },
            updatedAt = page?.stringOrBlank("updated_at").orEmpty(),
            error = null
        )
    }

    fun lookupAsn(rawAsn: String): AsnLookupResult {
        val value = rawAsn.removePrefix("AS").removePrefix("as").trim()
        val encoded = URLEncoder.encode("AS$value", Charsets.UTF_8.name())
        val json = JSONObject(getText("https://stat.ripe.net/data/as-overview/data.json?resource=$encoded"))
        if (!json.stringOrBlank("status").equals("ok", ignoreCase = true)) {
            throw IllegalStateException("RIPEstat 未返回可用 ASN 概览")
        }
        val data = json.optJSONObject("data") ?: throw IllegalStateException("RIPEstat ASN 概览缺少数据")
        val block = data.optJSONObject("block")
        val holder = data.stringOrBlank("holder")
        val routeState = when (data.booleanOrNull("announced")) {
            true -> "当前可见前缀：是（至少 10 个 RIS 全量对等体可见）"
            false -> "当前可见前缀：否或仅作转接"
            null -> "当前可见前缀：未知"
        }
        val allocation = listOfNotNull(
            block?.stringOrBlank("name")?.takeIf { it.isNotBlank() },
            block?.stringOrBlank("desc")?.takeIf { it.isNotBlank() }
        ).joinToString(" · ")
        val neighbourData = runCatching {
            JSONObject(getText("https://stat.ripe.net/data/asn-neighbours/data.json?resource=$encoded&lod=0"))
                .optJSONObject("data")
        }.getOrNull()
        val neighbourCounts = neighbourData?.optJSONObject("neighbour_counts")
        val neighbours = neighbourData?.optJSONArray("neighbours")?.let { array ->
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val neighbourAsn = item.opt("asn")?.toString().orEmpty().takeIf { it.isNotBlank() } ?: continue
                    add(AsnNeighbour("AS$neighbourAsn", item.stringOrBlank("type").ifBlank { "未知位置" }, item.intOrNull("power") ?: item.intOrNull("path_count")))
                }
            }.sortedByDescending { it.pathCount ?: 0 }.take(12)
        }.orEmpty()
        val neighbourSummary = neighbourCounts?.intOrNull("unique")?.let { "观测到的独立邻居：$it（展示前 12 个）" }.orEmpty()
        return AsnLookupResult(
            asn = "AS${data.stringOrBlank("resource").ifBlank { value }}",
            name = holder.ifBlank { allocation.ifBlank { "未提供登记持有人" } },
            description = listOf(routeState, allocation, neighbourSummary).filter { it.isNotBlank() }.joinToString("\n"),
            countryCode = "",
            website = "来源：RIPEstat ASN Overview / ASN Neighbours",
            allocatedAt = "",
            neighbours = neighbours
        )
    }

    fun lookupMac(rawMac: String): MacLookupResult {
        val mac = rawMac.uppercase().filter(Char::isLetterOrDigit)
        val firstByte = mac.take(2).toIntOrNull(16) ?: throw IllegalArgumentException("MAC 地址格式无效")
        val locallyAdministered = (firstByte and 0x02) != 0
        if (locallyAdministered) {
            return MacLookupResult(mac, "本地管理 / 随机化地址", true)
        }
        val vendor = getText("https://api.macvendors.com/${URLEncoder.encode(mac, Charsets.UTF_8.name())}").trim()
        if (vendor.isBlank()) throw IllegalStateException("未查到该 MAC 前缀的厂商登记")
        return MacLookupResult(mac, vendor, false)
    }

    fun lookupWhois(query: String): WhoisLookupResult {
        val normalized = query.removePrefix("https://").removePrefix("http://").substringBefore('/').trim().lowercase()
        if (normalized.isBlank()) throw IllegalArgumentException("请输入域名或 IP 地址")
        val registry = findWhoisRegistry(normalized)
        val raw = queryWhois(registry, normalized)
        val lines = raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("%") && !it.startsWith("#") }
            .take(12)
            .toList()
        if (lines.isEmpty()) throw IllegalStateException("注册表未返回可读结果")
        return WhoisLookupResult(query = normalized, registry = registry, lines = lines)
    }

    fun probePort(probe: PortProbeResult): PortProbeResult {
        val started = System.nanoTime()
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(probe.host, probe.port), 7_000)
            }
            val elapsed = (System.nanoTime() - started) / 1_000_000
            probe.copy(status = CheckStatus.SUCCESS, latencyMs = elapsed, detail = "端口可达")
        } catch (error: Exception) {
            probe.copy(status = CheckStatus.FAILURE, detail = error.asUserMessage())
        }
    }

    fun runPurityDiagnosis(context: Context, apiKeys: ApiKeyConfig = ApiKeyConfig()): PurityReport {
        val ipv4 = fetchIp(IPIFY_V4)
        val ipApi = runCatching { probeIpApi(ipv4) }.getOrNull()
        val ipWhoIs = runCatching { probeIpWhoIs() }.getOrNull()
        val proxyCheck = runCatching { probeProxyRisk(ipv4) }.getOrNull()
        val abuseRisk = apiKeys.abuseIpDbKey.takeIf { it.isNotBlank() }?.let { key ->
            runCatching { probeAbuseIpDb(ipv4, key) }.getOrNull()
        }
        val ipApiIsSecurity = apiKeys.ipApiKey.takeIf { it.isNotBlank() }?.let { key ->
            runCatching { probeIpApiIsSecurity(ipv4, key) }.getOrNull()
        }
        val maxMindInsights = apiKeys.maxMindAccountId.takeIf { it.isNotBlank() }
            ?.takeIf { apiKeys.maxMindLicenseKey.isNotBlank() }
            ?.let { accountId -> runCatching { probeMaxMindInsights(ipv4, accountId, apiKeys.maxMindLicenseKey) }.getOrNull() }
        val ipHubRisk = apiKeys.ipHubKey.takeIf { it.isNotBlank() }?.let { key ->
            runCatching { probeIpHub(ipv4, key) }.getOrNull()
        }
        val torProjectResult = runCatching { probeTorProject() }.getOrNull()
        val ipv6 = runCatching { fetchIp(IPIFY_DUAL).takeIf { it.contains(":") } }.getOrNull()
        val ipv6Geo = ipv6?.let { runCatching { probeIpApi(it) }.getOrNull() }
        if (ipApi == null && ipWhoIs == null) throw IllegalStateException("公开 IP 属性数据源暂不可用")

        fun ageDays(timestamp: String): Long? = runCatching {
            Duration.between(OffsetDateTime.parse(timestamp).toInstant(), Instant.now()).toDays().coerceAtLeast(0)
        }.getOrNull()

        fun volume(value: Int, tau: Double): Double = (1.0 - exp(-value.coerceAtLeast(0) / tau)).coerceIn(0.0, 1.0)
        fun decay(days: Long?, halfLife: Double, unknown: Double = 0.60): Double =
            days?.let { 2.0.pow(-it / halfLife) }?.coerceIn(0.0, 1.0) ?: unknown
        fun evidence(cap: Double, quality: Double, count: Int, tau: Double, days: Long?, halfLife: Double, resolution: Double = 1.0, unknownTime: Double = 0.60): Double =
            (cap * quality * volume(count, tau) * decay(days, halfLife, unknownTime) * resolution).coerceIn(0.0, 0.95)
        fun combinedEvidence(values: List<Double>): Double = values.filter { it > 0.0 }.maxOrNull()?.coerceAtMost(0.95) ?: 0.0
        fun sourceState(configured: Boolean, available: Boolean, title: String): Pair<String, PurityTone> = when {
            !configured -> "未配置" to PurityTone.NEUTRAL
            !available -> "未覆盖" to PurityTone.NEUTRAL
            else -> "已覆盖" to PurityTone.CONSISTENT
        }

        val signals = mutableListOf<PuritySignal>()
        val sameIp = ipWhoIs?.let { it.ip == ipv4 }
        signals += when (sameIp) {
            true -> PuritySignal("多源出口一致性", "一致", "api.ipify.org 与 ipwho.is 返回同一 IPv4；仅用于出口观测与覆盖度。", PurityTone.CONSISTENT)
            false -> PuritySignal("多源出口一致性", "不一致", "api.ipify.org：$ipv4；ipwho.is：${ipWhoIs?.ip}；差异只提示当前观测路径，不推断历史滥用。", PurityTone.NEUTRAL)
            null -> PuritySignal("多源出口一致性", "未覆盖", "ipwho.is 暂不可用；不会把未覆盖解释为低风险。", PurityTone.NEUTRAL)
        }
        if (ipApi != null && ipWhoIs != null && ipApi.countryCode.isNotBlank() && ipWhoIs.countryCode.isNotBlank()) {
            val sameCountry = ipApi.countryCode.equals(ipWhoIs.countryCode, ignoreCase = true)
            signals += PuritySignal("多源地理一致性", if (sameCountry) "一致" else "差异仅提示", "ipapi.co：${ipApi.countryCode}；ipwho.is：${ipWhoIs.countryCode}；地理差异不进入滥用风险。", if (sameCountry) PurityTone.CONSISTENT else PurityTone.NEUTRAL)
        } else {
            signals += PuritySignal("多源地理一致性", "未覆盖", "至少一个公开地理源未返回国家代码。", PurityTone.NEUTRAL)
        }
        if (ipv6 != null && ipv6Geo != null && ipApi != null && ipv6Geo.countryCode.isNotBlank() && ipApi.countryCode.isNotBlank()) {
            val sameCountry = ipApi.countryCode.equals(ipv6Geo.countryCode, ignoreCase = true)
            signals += PuritySignal("IPv4 / IPv6 位置", if (sameCountry) "一致" else "差异仅提示", "IPv4：${ipApi.countryCode}；IPv6：${ipv6Geo.countryCode}；双栈差异不进入滥用风险。", if (sameCountry) PurityTone.CONSISTENT else PurityTone.NEUTRAL)
        } else {
            signals += PuritySignal("IPv4 / IPv6 位置", "未覆盖", "未检测到双栈出口或 IPv6 地理属性。", PurityTone.NEUTRAL)
        }

        // 直接风险只接受行为证据。代理、VPN、Tor、IDC、ASN 和地理信息绝不单独进入本层。
        val proxyLastSeenDays = ageDays(proxyCheck?.detectionLastSeen.orEmpty())
        val abuseLastSeenDays = ageDays(abuseRisk?.lastReportedAt.orEmpty())
        val compromisedEvidence = if (proxyCheck?.compromised == true) evidence(0.95, 0.60, 1, 1.0, proxyLastSeenDays, 120.0) else 0.0
        val attackHistoryEvidence = if ((proxyCheck?.attackEventCount ?: 0) > 0) evidence(0.80, 0.45, proxyCheck?.attackEventCount ?: 0, 3.0, proxyLastSeenDays, 45.0) else 0.0
        val abuseSupport = maxOf(abuseRisk?.distinctUsers ?: 0, abuseRisk?.totalReports ?: 0)
        val abuseScoreFactor = (abuseRisk?.confidenceScore ?: 0).coerceIn(0, 100) / 100.0
        val abuseEvidence = if (abuseScoreFactor > 0.0) {
            (0.60 * 0.75 * (0.25 + 0.75 * abuseScoreFactor) * volume(maxOf(1, abuseSupport), 5.0) * decay(abuseLastSeenDays, 60.0, 0.60)).coerceIn(0.0, 0.95)
        } else 0.0
        val abuserEvidence = if (ipApiIsSecurity?.isAbuser == true) evidence(0.60, 0.65, 1, 1.0, null, 60.0) else 0.0
        val crawlerEvidence = combinedEvidence(listOf(
            if (proxyCheck?.scraper == true) evidence(0.20, 0.55, 1, 1.0, proxyLastSeenDays, 14.0) else 0.0,
            if (ipApiIsSecurity?.isCrawler == true) evidence(0.20, 0.60, 1, 1.0, null, 14.0) else 0.0
        ))
        val genericVendorEvidence = proxyCheck?.risk?.takeIf { it > 0 }?.let { risk ->
            (0.25 * 0.35 * (risk.coerceIn(0, 100) / 100.0).pow(1.2) * decay(proxyLastSeenDays, 30.0, 0.60)).coerceIn(0.0, 0.95)
        } ?: 0.0
        val familyEvidence = listOf(
            combinedEvidence(listOf(compromisedEvidence)),
            combinedEvidence(listOf(attackHistoryEvidence)),
            combinedEvidence(listOf(abuseEvidence, abuserEvidence)),
            crawlerEvidence,
            genericVendorEvidence
        )
        val directRisk = (85.0 * (1.0 - familyEvidence.fold(1.0) { product, value -> product * (1.0 - value.coerceIn(0.0, 0.95)) })).coerceIn(0.0, 85.0)
        val behaviorCoverage = listOf(
            proxyCheck?.let { it.risk != null || it.compromised != null || it.scraper != null || it.attackHistoryPresent } == true,
            abuseRisk?.let { it.confidenceScore != null || it.totalReports != null || it.distinctUsers != null } == true,
            ipApiIsSecurity?.let { it.isAbuser != null || it.isCrawler != null } == true
        ).count { it }.toDouble() / 3.0
        val behaviorFlags = buildList {
            if (proxyCheck?.compromised == true) add("受损")
            if ((proxyCheck?.attackEventCount ?: 0) > 0) add("攻击历史 ${proxyCheck?.attackEventCount}")
            abuseRisk?.confidenceScore?.takeIf { it > 0 }?.let { add("AbuseIPDB $it") }
            if (ipApiIsSecurity?.isAbuser == true) add("滥用")
            if (crawlerEvidence > 0.0) add("爬虫")
            proxyCheck?.risk?.takeIf { it > 0 }?.let { add("泛化风险 $it") }
        }
        signals += PuritySignal(
            "公开滥用风险（主分）",
            if (behaviorCoverage == 0.0) "未覆盖" else if (behaviorFlags.isEmpty()) "未检出" else behaviorFlags.joinToString("、"),
            if (behaviorCoverage == 0.0) "行为来源未覆盖；不把未知当作无风险。" else "按行为家族、来源质量、计数饱和和时间衰减计算：${formatRisk(directRisk)} / 85；网络属性不进入此分。",
            if (behaviorCoverage == 0.0) PurityTone.NEUTRAL else if (directRisk > 0.0) PurityTone.NOTICE else PurityTone.CONSISTENT
        )

        // 透明度只表达当前网络属性，不参与主风险 R。
        val maxMindTor = maxMindInsights?.isTorExitNode == true
        val torSources = listOf(torProjectResult == true, proxyCheck?.tor == true, abuseRisk?.isTor == true, ipApiIsSecurity?.isTor == true, maxMindTor, ipHubRisk?.isTor == true).count { it }
        val proxySources = listOf(proxyCheck?.proxy == true, ipApiIsSecurity?.isProxy == true, maxMindInsights?.isPublicProxy == true, ipHubRisk?.isProxy == true, ipHubRisk?.isResidentialProxy == true).count { it }
        val vpnSources = listOf(proxyCheck?.vpn == true, ipApiIsSecurity?.isVpn == true, maxMindInsights?.isAnonymousVpn == true).count { it }
        val relaySources = listOf(ipHubRisk?.isRelay == true).count { it }
        val genericAnonymous = maxMindInsights?.isAnonymous == true
        val ipHubNonResidential = ipHubRisk?.block == 1
        val transparencyRisk = when {
            torProjectResult == true -> 100.0
            torSources >= 2 -> 95.0
            torSources == 1 -> 85.0
            proxySources >= 2 -> 75.0
            proxySources == 1 -> 60.0
            vpnSources >= 2 -> 55.0
            vpnSources == 1 -> 45.0
            relaySources > 0 -> 35.0
            genericAnonymous || ipHubNonResidential -> 30.0
            else -> 0.0
        }
        val anonymityCoverage = listOf(
            torProjectResult != null,
            proxyCheck?.let { listOf(it.proxy, it.vpn, it.tor, it.anonymous).any { value -> value != null } } == true,
            ipApiIsSecurity?.let { listOf(it.isProxy, it.isVpn, it.isTor).any { value -> value != null } } == true,
            maxMindInsights?.let { listOf(it.isAnonymous, it.isAnonymousVpn, it.isHostingProvider, it.isPublicProxy, it.isResidentialProxy, it.isTorExitNode).any { value -> value != null } } == true,
            ipHubRisk?.let { it.block != null || listOf(it.isProxy, it.isTor, it.isHosting, it.isRelay, it.isResidentialProxy).any { value -> value != null } } == true
        ).count { it }.toDouble() / 5.0
        val anonymityFlags = buildList {
            if (torSources > 0) add(if (torProjectResult == true) "Tor 官方确认" else "Tor 标记")
            if (proxySources > 0) add("代理${if (proxySources >= 2) "（多源）" else ""}")
            if (vpnSources > 0) add("VPN${if (vpnSources >= 2) "（多源）" else ""}")
            if (relaySources > 0) add("中继")
            if (genericAnonymous) add("匿名网络")
            if (ipHubNonResidential) add("IPHub 非住宅网络")
            if (ipHubRisk?.block == 2) add("IPHub 低置信可疑")
        }
        signals += PuritySignal(
            "网络透明度（独立）",
            if (anonymityCoverage == 0.0) "未覆盖" else if (anonymityFlags.isEmpty()) "未检出" else anonymityFlags.joinToString("、"),
            if (anonymityCoverage == 0.0) "匿名化字段未覆盖；不影响主分。" else "透明度 ${formatRisk(transparencyRisk)} / 100；Tor、代理、VPN 和中继仅反映网络路径属性，不证明历史滥用。",
            if (anonymityCoverage == 0.0) PurityTone.NEUTRAL else if (transparencyRisk > 0.0) PurityTone.NOTICE else PurityTone.CONSISTENT
        )

        val contextFlags = buildList {
            if (proxyCheck?.hosting == true) add("ProxyCheck 托管")
            if (ipApiIsSecurity?.isDatacenter == true) add("ipapi.is 数据中心")
            if (maxMindInsights?.isHostingProvider == true) add("MaxMind 托管")
            if (ipHubRisk?.isHosting == true) add("IPHub 托管")
            if (ipHubRisk?.block == 1) add("IPHub block=1")
            maxMindInsights?.network?.takeIf { it.isNotBlank() }?.let { add("CIDR $it") }
            ipHubRisk?.blockReason?.takeIf { it.isNotBlank() }?.let { add("IPHub：$it") }
        }
        val contextCoverage = listOf(
            ipApi != null || ipWhoIs != null,
            proxyCheck?.hosting != null,
            ipApiIsSecurity?.isDatacenter != null,
            maxMindInsights != null,
            ipHubRisk?.block != null
        ).count { it }.toDouble() / 5.0
        signals += PuritySignal(
            "网络上下文（不计主分）",
            if (contextCoverage == 0.0) "未覆盖" else if (contextFlags.isEmpty()) "未标记" else contextFlags.take(3).joinToString("、"),
            if (contextCoverage == 0.0) "ASN、CIDR、托管和数据中心字段未覆盖。" else "IDC、云、托管、ASN、CIDR 和 ISP 仅作网络上下文，不单独判定历史滥用。",
            if (contextCoverage == 0.0) PurityTone.NEUTRAL else if (contextFlags.isNotEmpty()) PurityTone.NEUTRAL else PurityTone.CONSISTENT
        )

        fun addSourceSignal(title: String, configured: Boolean, available: Boolean, detail: String) {
            val (state, tone) = sourceState(configured, available, title)
            signals += PuritySignal(title, state, detail, tone)
        }
        addSourceSignal(
            "AbuseIPDB 授权来源",
            apiKeys.abuseIpDbKey.isNotBlank(), abuseRisk != null,
            abuseRisk?.let { "置信分：${it.confidenceScore ?: "未返回"}；报告：${it.totalReports ?: "未返回"}；独立报告者：${it.distinctUsers ?: "未返回"}；最近报告：${it.lastReportedAt.ifBlank { "未返回" }}。" }
                ?: "预置 APIv2 Check；仅在填写本地 Key 后查询。"
        )
        addSourceSignal(
            "ipapi.is 授权来源",
            apiKeys.ipApiKey.isNotBlank(), ipApiIsSecurity != null,
            ipApiIsSecurity?.let { "代理：${it.isProxy ?: "未返回"}；VPN：${it.isVpn ?: "未返回"}；Tor：${it.isTor ?: "未返回"}；滥用：${it.isAbuser ?: "未返回"}；爬虫：${it.isCrawler ?: "未返回"}。" }
                ?: "预置官方 JSON POST；仅在填写本地 Key 后查询。"
        )
        addSourceSignal(
            "MaxMind GeoIP Insights",
            apiKeys.maxMindAccountId.isNotBlank() || apiKeys.maxMindLicenseKey.isNotBlank(), maxMindInsights != null,
            maxMindInsights?.let { "网络：${it.network.ifBlank { "未返回" }}；ASN：${it.asn.ifBlank { "未返回" }}；匿名化置信：${it.anonymizerConfidence?.toString() ?: "未返回"}。仅进入透明度/上下文。" }
                ?: "预置 HTTPS Basic Auth Insights；需同时填写 Account ID 与 License Key。"
        )
        addSourceSignal(
            "IPHub v2.2",
            apiKeys.ipHubKey.isNotBlank(), ipHubRisk != null,
            ipHubRisk?.let { "block：${it.block?.toString() ?: "未返回"}；原因：${it.blockReason.ifBlank { "未返回" }}；托管/代理/Tor 仅进入透明度和上下文；block=2 只提示低置信。" }
                ?: "预置 X-Key 与 Accept-Version: 2.2；仅在填写本地 Key 后查询。"
        )
        signals += PuritySignal("网页专属检测", "外部入口", "BrowserLeaks 适合浏览器内 WebRTC、JavaScript、Canvas/WebGL、TLS 与 DNS 自检；原生 Android 不伪造这些结果。", PurityTone.NEUTRAL)
        signals += PuritySignal("EdgeOne / NSTool", "未纳入 API", "myip.edgeone.ai 本次无法解析；NSTool 未公开可审计风险 API。两者均不作为默认请求或评分来源。", PurityTone.NEUTRAL)

        val privacy = inspectPrivacy(context)
        signals += PuritySignal("Android 网络状态", if (privacy.vpnActive) "VPN 已连接" else "未检测到 VPN", "Private DNS：${privacy.privateDnsMode}${if (privacy.dnsServers.isNotEmpty()) "；DNS：${privacy.dnsServers.take(2).joinToString("、")}" else ""}；只展示，不参与主风险。", PurityTone.NEUTRAL)

        val coverage = (55.0 * behaviorCoverage + 25.0 * anonymityCoverage + 15.0 * contextCoverage + if (sameIp != null) 5.0 else 0.0).coerceIn(0.0, 100.0)
        val coverageLabel = when {
            coverage >= 85.0 -> "证据覆盖较完整"
            coverage >= 60.0 -> "部分覆盖"
            else -> "证据不足"
        }
        val coverageDetail = "公开滥用 ${formatRisk(behaviorCoverage * 100)}% · 匿名化 ${formatRisk(anonymityCoverage * 100)}% · 网络上下文 ${formatRisk(contextCoverage * 100)}% · 出口观测 ${if (sameIp != null) "100.0" else "0.0"}%。未覆盖不等于无风险。"
        signals += PuritySignal("证据覆盖度", "${formatRisk(coverage)} / 100 · $coverageLabel", coverageDetail, if (coverage >= 85.0) PurityTone.CONSISTENT else PurityTone.NEUTRAL)

        val score = 100.0 - directRisk
        val label = when {
            coverage < 60.0 -> "证据不足"
            directRisk <= 10.0 -> "低风险信号"
            directRisk <= 30.0 -> "轻度提示"
            directRisk <= 60.0 -> "需复核"
            else -> "高风险提示"
        }
        val summary = when {
            coverage < 60.0 -> "关键证据覆盖不足；未发现不等于无风险，主分只反映本次已覆盖来源。"
            directRisk <= 10.0 -> "已覆盖来源中未见明显公开滥用信号；这不是安全保证。"
            directRisk <= 30.0 -> "发现有限且可解释的公开滥用信号，建议查看事件来源、时间与网络配置。"
            directRisk <= 60.0 -> "发现较强的公开滥用信号，建议核对事件明细并采用低摩擦复核措施。"
            else -> "多个高权重公开滥用证据命中；仅作网络出口风险提示，不推断个人或账号行为。"
        }
        val buckets = listOf(
            PurityRiskBucket("公开滥用风险（主分）", "${formatRisk(directRisk)} / 85", "按行为家族、来源质量、饱和计数与时效组合；网络属性不计入。", if (behaviorCoverage == 0.0) PurityTone.NEUTRAL else if (directRisk > 0.0) PurityTone.NOTICE else PurityTone.CONSISTENT),
            PurityRiskBucket("网络透明度（独立）", "${formatRisk(transparencyRisk)} / 100", "Tor、代理、VPN 和中继是网络路径属性，独立显示且不污染主分。", if (anonymityCoverage == 0.0) PurityTone.NEUTRAL else if (transparencyRisk > 0.0) PurityTone.NOTICE else PurityTone.CONSISTENT),
            PurityRiskBucket("网络上下文", if (contextFlags.isEmpty()) "未标记" else "${contextFlags.size} 项", "IDC、托管、ASN、CIDR 和 ISP 仅供解释，不单独判定历史恶意。", if (contextCoverage == 0.0) PurityTone.NEUTRAL else if (contextFlags.isEmpty()) PurityTone.CONSISTENT else PurityTone.NEUTRAL),
            PurityRiskBucket("证据覆盖度", "${formatRisk(coverage)} / 100", coverageDetail, if (coverage >= 85.0) PurityTone.CONSISTENT else PurityTone.NEUTRAL)
        )
        return PurityReport(
            score = score,
            risk = directRisk,
            abuseRisk = directRisk,
            transparencyRisk = transparencyRisk,
            coverage = coverage,
            coverageLabel = coverageLabel,
            coverageDetail = coverageDetail,
            label = label,
            summary = summary,
            buckets = buckets,
            signals = signals,
            checkedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        )
    }

    private fun probeIpApi(ip: String): PublicGeoProbe {
        val json = JSONObject(getText("https://ipapi.co/$ip/json/"))
        if (json.has("error") && json.optBoolean("error")) throw IllegalStateException(json.stringOrBlank("reason").ifBlank { "ipapi.co 未返回数据" })
        return PublicGeoProbe(
            source = "ipapi.co",
            ip = json.stringOrBlank("ip").ifBlank { ip },
            countryCode = json.stringOrBlank("country_code"),
            country = json.stringOrBlank("country_name"),
            asn = json.stringOrBlank("asn"),
            organization = json.stringOrBlank("org")
        )
    }

    private fun probeIpWhoIs(): PublicGeoProbe {
        val json = JSONObject(getText("https://ipwho.is/"))
        if (!json.optBoolean("success", true)) throw IllegalStateException(json.stringOrBlank("message").ifBlank { "ipwho.is 未返回数据" })
        val connection = json.optJSONObject("connection")
        val rawAsn = connection?.opt("asn")?.toString().orEmpty()
        return PublicGeoProbe(
            source = "ipwho.is",
            ip = json.stringOrBlank("ip"),
            countryCode = json.stringOrBlank("country_code"),
            country = json.stringOrBlank("country"),
            asn = rawAsn.takeIf { it.isNotBlank() }?.let { if (it.startsWith("AS", ignoreCase = true)) it else "AS$it" }.orEmpty(),
            organization = connection?.optString("org", "").orEmpty()
        )
    }

    private fun probeAbuseIpDb(ip: String, apiKey: String): AbuseIpDbRisk {
        val encodedIp = URLEncoder.encode(ip, Charsets.UTF_8.name())
        val json = JSONObject(
            getText(
                "https://api.abuseipdb.com/api/v2/check?ipAddress=$encodedIp&maxAgeInDays=90",
                mapOf("Key" to apiKey)
            )
        )
        val data = json.optJSONObject("data") ?: throw IllegalStateException("AbuseIPDB 未返回 data 字段")
        return AbuseIpDbRisk(
            confidenceScore = data.intOrNull("abuseConfidenceScore")?.coerceIn(0, 100),
            totalReports = data.intOrNull("totalReports")?.coerceAtLeast(0),
            distinctUsers = data.intOrNull("numDistinctUsers")?.coerceAtLeast(0),
            isTor = data.booleanOrNull("isTor"),
            usageType = data.stringOrBlank("usageType"),
            lastReportedAt = data.stringOrBlank("lastReportedAt")
        )
    }

    private fun probeIpApiIsSecurity(ip: String, apiKey: String): IpApiIsSecurity {
        val requestBody = JSONObject()
            .put("q", ip)
            .put("key", apiKey)
            .toString()
        val json = JSONObject(postJson("https://api.ipapi.is", requestBody))
        json.stringOrBlank("error").takeIf { it.isNotBlank() }?.let { throw IllegalStateException(it) }
        val egressValue = json.opt("egress_service")
        val hasManagedEgress = egressValue != null && egressValue != JSONObject.NULL && egressValue != false
        val egressSummary = when (egressValue) {
            is JSONObject -> listOf(
                egressValue.stringOrBlank("name"),
                egressValue.stringOrBlank("type"),
                egressValue.stringOrBlank("service")
            ).filter { it.isNotBlank() }.distinct().joinToString(" · ")
            is String -> egressValue.takeUnless { it.equals("null", ignoreCase = true) }
            else -> ""
        }.orEmpty()
        val asn = json.optJSONObject("asn")
        return IpApiIsSecurity(
            isDatacenter = json.booleanOrNull("is_datacenter"),
            isProxy = json.booleanOrNull("is_proxy"),
            isVpn = json.booleanOrNull("is_vpn"),
            isTor = json.booleanOrNull("is_tor"),
            isAbuser = json.booleanOrNull("is_abuser"),
            isCrawler = json.booleanOrNull("is_crawler"),
            hasManagedEgress = hasManagedEgress,
            egressSummary = egressSummary,
            companyName = json.stringOrBlank("company_name").ifBlank { json.optJSONObject("company")?.stringOrBlank("name").orEmpty() },
            asnOrganization = json.stringOrBlank("asn_org").ifBlank { asn?.stringOrBlank("org").orEmpty() }
        )
    }

    private fun probeProxyRisk(ip: String): RiskIntelligence {
        val root = JSONObject(getText("https://proxycheck.io/v3/$ip?vpn=1&asn=1&risk=1"))
        if (!root.stringOrBlank("status").equals("ok", ignoreCase = true)) {
            throw IllegalStateException(root.stringOrBlank("message").ifBlank { "ProxyCheck 未返回可用结果" })
        }
        val result = root.optJSONObject(ip) ?: throw IllegalStateException("ProxyCheck 未返回当前 IP 的结果")
        val detections = result.optJSONObject("detections")
        val attackHistory = result.optJSONObject("attack_history")
        val attacks = mutableListOf<String>()
        var attackEventCount = 0
        if (attackHistory != null) {
            val keys = attackHistory.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val count = attackHistory.optInt(key, 0).coerceAtLeast(0)
                attackEventCount += count
                if (count > 0 && attacks.size < 2) attacks += "${key.replace('_', ' ')}：$count"
            }
        }
        fun intField(name: String): Int? = detections?.intOrNull(name)
        fun booleanField(name: String): Boolean? = detections?.booleanOrNull(name)
        return RiskIntelligence(
            source = "ProxyCheck",
            proxy = booleanField("proxy"),
            vpn = booleanField("vpn"),
            tor = booleanField("tor"),
            hosting = booleanField("hosting"),
            compromised = booleanField("compromised"),
            scraper = booleanField("scraper"),
            anonymous = booleanField("anonymous"),
            risk = intField("risk"),
            confidence = intField("confidence"),
            detectionFirstSeen = detections?.stringOrBlank("first_seen").orEmpty(),
            detectionLastSeen = detections?.stringOrBlank("last_seen").orEmpty(),
            attackEventCount = attackEventCount,
            attackHistoryPresent = attackHistory != null,
            attackSummary = attacks.joinToString("；")
        )
    }

    private fun probeMaxMindInsights(ip: String, accountId: String, licenseKey: String): MaxMindInsights {
        val token = Base64.encodeToString("$accountId:$licenseKey".toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val encodedIp = URLEncoder.encode(ip, Charsets.UTF_8.name()).replace("+", "%20")
        val json = JSONObject(
            getText(
                "https://geoip.maxmind.com/geoip/v2.1/insights/$encodedIp",
                mapOf("Authorization" to "Basic $token")
            )
        )
        val anonymizer = json.optJSONObject("anonymizer")
        val traits = json.optJSONObject("traits")
        val asnValue = traits?.opt("autonomous_system_number")?.toString().orEmpty()
        return MaxMindInsights(
            isAnonymous = anonymizer?.booleanOrNull("is_anonymous"),
            isAnonymousVpn = anonymizer?.booleanOrNull("is_anonymous_vpn"),
            isHostingProvider = anonymizer?.booleanOrNull("is_hosting_provider"),
            isPublicProxy = anonymizer?.booleanOrNull("is_public_proxy"),
            isResidentialProxy = anonymizer?.booleanOrNull("is_residential_proxy"),
            isTorExitNode = anonymizer?.booleanOrNull("is_tor_exit_node"),
            anonymizerConfidence = anonymizer?.intOrNull("confidence")?.coerceIn(0, 100),
            network = traits?.stringOrBlank("network").orEmpty(),
            asn = asnValue.takeIf { it.isNotBlank() }?.let { if (it.startsWith("AS", ignoreCase = true)) it else "AS$it" }.orEmpty(),
            organization = traits?.stringOrBlank("autonomous_system_organization").orEmpty().ifBlank { traits?.stringOrBlank("organization").orEmpty() },
            isp = traits?.stringOrBlank("isp").orEmpty(),
            connectionType = traits?.stringOrBlank("connection_type").orEmpty()
        )
    }

    private fun probeIpHub(ip: String, apiKey: String): IpHubRisk {
        val encodedIp = URLEncoder.encode(ip, Charsets.UTF_8.name()).replace("+", "%20")
        val json = JSONObject(
            getText(
                "https://v2.api.iphub.info/ip/$encodedIp",
                mapOf("X-Key" to apiKey, "Accept-Version" to "2.2")
            )
        )
        val proxyType = json.optJSONObject("proxyType")
        return IpHubRisk(
            block = json.intOrNull("block")?.takeIf { it in 0..2 },
            blockReason = json.stringOrBlank("blockReason"),
            isProxy = proxyType?.booleanOrNull("proxy"),
            isTor = proxyType?.booleanOrNull("tor"),
            isHosting = proxyType?.booleanOrNull("hosting"),
            isRelay = proxyType?.booleanOrNull("relay"),
            isResidentialProxy = proxyType?.booleanOrNull("residentialProxy"),
            asn = json.opt("asn")?.toString().orEmpty().takeIf { it.isNotBlank() }?.let { if (it.startsWith("AS", ignoreCase = true)) it else "AS$it" }.orEmpty(),
            isp = json.stringOrBlank("isp"),
            countryCode = json.stringOrBlank("countryCode")
        )
    }

    private fun probeTorProject(): Boolean? {
        val json = JSONObject(getText("https://check.torproject.org/api/ip"))
        return json.booleanOrNull("IsTor")
    }

    private fun isLikelyHostedNetwork(asn: String, organization: String): Boolean {
        val text = "$asn $organization".lowercase()
        val keywords = listOf(
            "amazon", "aws", "google cloud", "microsoft azure", "digitalocean", "ovh", "vultr", "linode", "hetzner",
            "alibaba cloud", "tencent cloud", "cloudflare", "datacenter", "data center", "hosting"
        )
        return keywords.any { text.contains(it) }
    }

    private fun findWhoisRegistry(query: String): String {
        val isIpAddress = query.matches(Regex("^[0-9a-fA-F:.]+$"))
        val ianaQuery = if (isIpAddress) query else query.substringAfterLast('.')
        val iana = queryWhois("whois.iana.org", ianaQuery)
        val match = Regex("(?im)^(?:refer|whois|referralserver):\\s*(?:whois://)?([^\\s/]+)").find(iana)
        return match?.groupValues?.getOrNull(1)?.trim()?.ifBlank { null } ?: "whois.iana.org"
    }

    private fun queryWhois(server: String, query: String): String {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(server, 43), 8_000)
            socket.soTimeout = 8_000
            val writer = socket.getOutputStream().bufferedWriter()
            writer.write(query)
            writer.write("\r\n")
            writer.flush()
            return socket.getInputStream().bufferedReader().use { reader -> reader.readText().take(12_000) }
        }
    }

    fun inspectPrivacy(context: Context): PrivacySnapshot {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork
        val capabilities = network?.let { manager.getNetworkCapabilities(it) }
        val properties = network?.let { manager.getLinkProperties(it) }
        val privateDns = runCatching {
            when (Settings.Global.getString(context.contentResolver, "private_dns_mode")) {
                "hostname" -> "指定主机名"
                "opportunistic" -> "自动"
                "off" -> "已关闭"
                else -> "系统默认"
            }
        }.getOrDefault("系统默认")
        val transports = buildList {
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) add("Wi‑Fi")
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) add("蜂窝数据")
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true) add("以太网")
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) add("VPN")
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) == true) add("蓝牙网络")
        }
        return PrivacySnapshot(
            vpnActive = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true,
            privateDnsMode = privateDns,
            dnsServers = properties?.dnsServers?.mapNotNull { it.hostAddress } ?: emptyList(),
            transport = transports.joinToString(" + ").ifBlank { "系统未识别活动网络" },
            validated = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true,
            metered = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) != true,
            interfaceName = properties?.interfaceName.orEmpty(),
            privateDnsHostname = properties?.privateDnsServerName.orEmpty(),
            downstreamKbps = capabilities?.linkDownstreamBandwidthKbps ?: 0,
            upstreamKbps = capabilities?.linkUpstreamBandwidthKbps ?: 0
        )
    }

    private fun fetchIp(url: String): String {
        val json = JSONObject(getText(url))
        return json.getString("ip")
    }

    private fun postJson(url: String, body: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", "NetScope Android/1.0")
        }
        return try {
            connection.outputStream.bufferedWriter().use { writer ->
                writer.write(body)
            }
            val code = connection.responseCode
            if (code !in 200..299) throw IllegalStateException("服务返回 HTTP $code")
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun getText(url: String, headers: Map<String, String> = emptyMap()): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "NetScope Android/1.0")
            headers.forEach { (name, value) -> setRequestProperty(name, value) }
        }
        return try {
            val code = connection.responseCode
            if (code !in 200..299) throw IllegalStateException("服务返回 HTTP $code")
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}

private fun JSONObject.stringOrBlank(name: String): String {
    return optString(name, "").takeUnless { it == "null" } ?: ""
}

private fun JSONObject.booleanOrNull(name: String): Boolean? {
    if (!has(name) || isNull(name)) return null
    return when (val value = opt(name)) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> when (value.trim().lowercase()) {
            "true", "1" -> true
            "false", "0" -> false
            else -> null
        }
        else -> null
    }
}

private fun JSONObject.intOrNull(name: String): Int? {
    if (!has(name) || isNull(name)) return null
    return when (val value = opt(name)) {
        is Number -> value.toInt()
        is String -> value.trim().toIntOrNull()
        else -> null
    }
}

private fun Throwable.asUserMessage(): String {
    val raw = message.orEmpty()
    return when {
        raw.contains("Unable to resolve host", ignoreCase = true) -> "无法连接网络，请检查 Wi‑Fi、移动数据或代理设置。"
        raw.contains("timeout", ignoreCase = true) -> "服务响应超时，请稍后重试。"
        raw.startsWith("服务返回") -> raw
        raw.isNotBlank() -> raw.take(100)
        else -> "发生未知网络错误，请稍后重试。"
    }
}
