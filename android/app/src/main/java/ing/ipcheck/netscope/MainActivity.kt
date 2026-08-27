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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandMore
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
import androidx.compose.material.icons.outlined.Security
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val AppBackground = Color(0xFFF4F6F8)
private val CardSurface = Color(0xFFFFFFFF)
private val Ink = Color(0xFF15232D)
private val MutedInk = Color(0xFF66757F)
private val Blue = Color(0xFF1E88E5)
private val Green = Color(0xFF16A36A)
private val Amber = Color(0xFFF2A51A)
private val Red = Color(0xFFE05252)
private val SoftBlue = Color(0xFFEAF4FF)
private val SoftGreen = Color(0xFFE8F8F0)
private val SoftAmber = Color(0xFFFFF5DC)
private val Border = Color(0xFFE3E9ED)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetScopeTheme {
                NetScopeApp()
            }
        }
    }
}

@Composable
private fun NetScopeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Blue,
            secondary = Green,
            surface = CardSurface,
            background = AppBackground,
            onBackground = Ink,
            onSurface = Ink,
            outline = Border
        ),
        content = content
    )
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
    val dnsServers: List<String>
)

private data class DnsLookupResult(
    val host: String,
    val addresses: List<String>,
    val error: String? = null
)

private data class WhoisLookupResult(
    val query: String,
    val registry: String,
    val lines: List<String>,
    val error: String? = null
)

private data class PortProbeResult(
    val host: String,
    val port: Int,
    val status: CheckStatus,
    val latencyMs: Long? = null,
    val detail: String = "等待检测"
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
    val risk: Double,
    val cap: Double,
    val detail: String
)

private data class PurityReport(
    val score: Double,
    val risk: Double,
    val abuseRisk: Double,
    val transparencyRisk: Double,
    val contextRisk: Double,
    val observabilityRisk: Double,
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
    val customKey: String = "",
    val customEndpoint: String = ""
)

private data class AbuseIpDbRisk(
    val confidenceScore: Int?,
    val totalReports: Int?,
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

private object SecureApiKeyStore {
    private const val PreferencesName = "secure_api_keys"
    private const val KeyAlias = "netscope_api_key_encryption"
    private const val AbuseKey = "abuseipdb"
    private const val IpApiIsKey = "ipapi_is"
    private const val LegacyIpApiComKey = "ipapi"
    private const val CustomKey = "custom"
    private const val CustomEndpoint = "custom_endpoint"

    fun load(context: Context): ApiKeyConfig {
        val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
        return ApiKeyConfig(
            abuseIpDbKey = decrypt(preferences.getString(AbuseKey, null)).orEmpty(),
            ipApiKey = decrypt(preferences.getString(IpApiIsKey, null)).orEmpty(),
            customKey = decrypt(preferences.getString(CustomKey, null)).orEmpty(),
            customEndpoint = decrypt(preferences.getString(CustomEndpoint, null)).orEmpty()
        )
    }

    fun save(context: Context, config: ApiKeyConfig) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit()
            .putString(AbuseKey, config.abuseIpDbKey.takeIf { it.isNotBlank() }?.let(::encrypt))
            .putString(IpApiIsKey, config.ipApiKey.takeIf { it.isNotBlank() }?.let(::encrypt))
            .remove(LegacyIpApiComKey)
            .putString(CustomKey, config.customKey.takeIf { it.isNotBlank() }?.let(::encrypt))
            .putString(CustomEndpoint, config.customEndpoint.takeIf { it.isNotBlank() }?.let(::encrypt))
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).edit().clear().apply()
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

private val DefaultPortProbes = listOf(
    PortProbeResult("github.com", 443, CheckStatus.IDLE),
    PortProbeResult("api.openai.com", 443, CheckStatus.IDLE),
    PortProbeResult("cloudflare.com", 443, CheckStatus.IDLE),
    PortProbeResult("www.wikipedia.org", 443, CheckStatus.IDLE)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetScopeApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var snapshot by remember { mutableStateOf<IpSnapshot?>(null) }
    var ipLoading by remember { mutableStateOf(true) }
    var ipError by remember { mutableStateOf<String?>(null) }
    var privacy by remember { mutableStateOf(NetworkRepository.inspectPrivacy(context)) }
    var endpoints by remember { mutableStateOf(DefaultEndpoints) }
    var testingAll by remember { mutableStateOf(false) }
    var cloudflareLatency by remember { mutableStateOf<Long?>(null) }
    var speedTesting by remember { mutableStateOf(false) }
    var dnsHost by remember { mutableStateOf("example.com") }
    var dnsResult by remember { mutableStateOf<DnsLookupResult?>(null) }
    var dnsLoading by remember { mutableStateOf(false) }
    var whoisQuery by remember { mutableStateOf("") }
    var whoisResult by remember { mutableStateOf<WhoisLookupResult?>(null) }
    var whoisLoading by remember { mutableStateOf(false) }
    var portProbes by remember { mutableStateOf(DefaultPortProbes) }
    var portsLoading by remember { mutableStateOf(false) }
    var purityReport by remember { mutableStateOf<PurityReport?>(null) }
    var purityLoading by remember { mutableStateOf(false) }
    var purityError by remember { mutableStateOf<String?>(null) }
    var apiKeyConfig by remember { mutableStateOf(runCatching { SecureApiKeyStore.load(context) }.getOrDefault(ApiKeyConfig())) }
    var showApiKeySettings by remember { mutableStateOf(false) }

    fun refreshIpInfo() {
        scope.launch {
            ipLoading = true
            ipError = null
            runCatching { NetworkRepository.loadIpSnapshot() }
                .onSuccess {
                    snapshot = it
                    privacy = NetworkRepository.inspectPrivacy(context)
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

    fun measureCloudflare() {
        scope.launch {
            speedTesting = true
            val result = withContext(Dispatchers.IO) {
                NetworkRepository.testEndpoint(DefaultEndpoints.first { it.name == "Cloudflare" })
            }
            cloudflareLatency = result.latencyMs
            speedTesting = false
        }
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
        if (target.isBlank()) {
            dnsResult = DnsLookupResult("", emptyList(), "请输入域名或主机名")
            return
        }
        scope.launch {
            dnsLoading = true
            dnsResult = withContext(Dispatchers.IO) {
                runCatching { NetworkRepository.resolveDns(target) }
                    .getOrElse { DnsLookupResult(target, emptyList(), it.asUserMessage()) }
            }
            dnsLoading = false
        }
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
                    icon = Icons.Outlined.Security,
                    title = "增强纯净度诊断",
                    subtitle = "公开风险源与网络信号的可解释独立评分",
                    actionLabel = if (purityLoading) "检测中" else "重新检测",
                    onAction = if (purityLoading) null else { { runPurityDiagnosis() } }
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
                    icon = Icons.Outlined.NetworkCheck,
                    title = "网络连通性",
                    subtitle = "检测常用服务是否可访问；延迟仅供参考",
                    actionLabel = if (testingAll) "检测中" else "全部重试",
                    onAction = if (testingAll) null else { { runAllConnectivity() } }
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
                    subtitle = "不执行大流量上传或下载，避免意外消耗移动数据"
                )
            }
            item {
                SpeedCard(latency = cloudflareLatency, loading = speedTesting, onMeasure = { measureCloudflare() })
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
                    onHostChange = { dnsHost = it },
                    onLookup = { resolveDns() }
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
                ServiceStatusCard(
                    probes = portProbes,
                    loading = portsLoading,
                    onProbe = { runPortProbes() }
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
    var customEndpoint by remember(savedConfig) { mutableStateOf(savedConfig.customEndpoint) }
    var customKey by remember(savedConfig) { mutableStateOf(savedConfig.customKey) }
    val endpointIsValid = isHttpsEndpoint(customEndpoint)
    val customConfigured = customEndpoint.isNotBlank() && customKey.isNotBlank()
    val customPairIsValid = customEndpoint.isBlank() == customKey.isBlank()
    val formIsValid = endpointIsValid && customPairIsValid
    val configuredCount = listOf(abuseKey.isNotBlank(), ipApiKey.isNotBlank(), customConfigured).count { it }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.VpnKey, contentDescription = null, tint = Blue) },
        title = { Text("授权数据源 Key", fontWeight = FontWeight.Bold, color = Ink) },
        text = {
            Column {
                Text("Key 与自定义请求地址均使用 Android Keystore 加密保存在本机，不上传到本项目服务器、不写入日志。", fontSize = 12.sp, color = MutedInk, lineHeight = 18.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = abuseKey,
                    onValueChange = { abuseKey = it.trim() },
                    label = { Text("AbuseIPDB API Key") },
                    supportingText = { Text("用于 abuseConfidenceScore、报告数、Tor 与使用类型提示") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ipApiKey,
                    onValueChange = { ipApiKey = it.trim() },
                    label = { Text("ipapi.is API Key") },
                    supportingText = { Text("用于 VPN、代理、Tor、托管、滥用与爬虫提示") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customEndpoint,
                    onValueChange = { customEndpoint = it.trim() },
                    label = { Text("自定义请求地址（HTTPS，可选）") },
                    supportingText = {
                        Text(
                            if (!endpointIsValid) "请输入完整 HTTPS 地址，例如：https://api.example.com/v1/check" else if (!customPairIsValid) "自定义地址与 Key 需要同时填写" else "例如：https://api.example.com/v1/check",
                            color = if (formIsValid) MutedInk else Red
                        )
                    },
                    isError = !endpointIsValid || !customPairIsValid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customKey,
                    onValueChange = { customKey = it.trim() },
                    label = { Text("自定义 API Key（可选）") },
                    supportingText = { Text(if (!customPairIsValid) "请同时填写 HTTPS 请求地址与自定义 Key" else "会与请求地址配套保存；当前版本不会自动请求或参与评分") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (configuredCount > 0) "已配置 $configuredCount / 3 个数据源；保存后会重新执行内置纯净度诊断。" else "未配置时，APP 仍只使用不需要 Key 的公开基础诊断。",
                    fontSize = 11.sp,
                    color = MutedInk
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(ApiKeyConfig(abuseKey, ipApiKey, customKey, customEndpoint)) },
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
                    Text("已识别公开风险：${formatRisk(report.risk)} / 100；证据覆盖：${formatRisk(report.coverage)} / 100（${report.coverageLabel}）。", fontSize = 11.sp, color = MutedInk)
                    Spacer(Modifier.height(4.dp))
                    Text("分层：直接行为 ${formatRisk(report.abuseRisk)} / 70 · 匿名化 ${formatRisk(report.transparencyRisk)} / 15 · 网络上下文 ${formatRisk(report.contextRisk)} / 10 · 出口观测 ${formatRisk(report.observabilityRisk)} / 5", fontSize = 10.sp, color = MutedInk, lineHeight = 15.sp)
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
                    Text("说明：本报告将公开滥用、匿名化/透明度、网络上下文和出口可观测性分层计算；未覆盖字段不会被当作无风险。分数是可解释的公开风险信号指数，不是欺诈概率，也不是任何第三方服务的专有风控值。", fontSize = 11.sp, color = MutedInk, lineHeight = 16.sp)
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
    val tone = when {
        bucket.risk == 0.0 -> PurityTone.CONSISTENT
        bucket.risk * 2 >= bucket.cap -> PurityTone.NOTICE
        else -> PurityTone.NEUTRAL
    }
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
                imageVector = if (bucket.risk == 0.0) Icons.Outlined.CheckCircle else Icons.Outlined.Info,
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
        Text("${formatRisk(bucket.risk)} / ${formatRisk(bucket.cap)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
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
                    Text("VPN、Private DNS 与当前 DNS 服务器", fontSize = 12.sp, color = MutedInk)
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
                Text(
                    "说明：原生应用无法复刻浏览器 WebRTC 候选地址或 JavaScript 指纹，因此不会伪造 WebRTC / DNS 泄漏结论。",
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
private fun SpeedCard(latency: Long?, loading: Boolean, onMeasure: () -> Unit) {
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
                    Text("边缘节点延迟", fontWeight = FontWeight.SemiBold, color = Ink)
                    Text("目标：Cloudflare", fontSize = 12.sp, color = MutedInk)
                }
                Text(
                    latency?.let { "${it}ms" } ?: "—",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (latency == null) MutedInk else Green
                )
            }
            Spacer(Modifier.height(14.dp))
            Text("为避免消耗移动数据，此操作只进行一次轻量 HTTP 测量，不会执行下载或上传测速。", fontSize = 12.sp, color = MutedInk, lineHeight = 18.sp)
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
                    Text("测量延迟")
                }
            }
        }
    }
}

@Composable
private fun DnsLookupCard(
    host: String,
    result: DnsLookupResult?,
    loading: Boolean,
    onHostChange: (String) -> Unit,
    onLookup: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            NativeToolHeader(Icons.Outlined.Dns, "DNS 解析", "使用 Android 系统解析器查询 A / AAAA 地址")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = host,
                onValueChange = onHostChange,
                label = { Text("域名或主机名") },
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
                    Text("${it.host} 的解析结果", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                    Spacer(Modifier.height(5.dp))
                    Text(it.addresses.joinToString("\n"), fontSize = 12.sp, color = MutedInk, lineHeight = 18.sp)
                }
            }
        }
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
private fun ServiceStatusCard(probes: List<PortProbeResult>, loading: Boolean, onProbe: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            NativeToolHeader(Icons.Outlined.NetworkCheck, "服务状态", "从当前网络探测 HTTPS 端口可达性，不代表服务全球状态")
            Spacer(Modifier.height(9.dp))
            probes.forEachIndexed { index, probe ->
                ServiceProbeLine(probe)
                if (index < probes.lastIndex) HorizontalDivider(color = Border, modifier = Modifier.padding(vertical = 8.dp))
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onProbe, enabled = !loading) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Blue)
                    Spacer(Modifier.width(8.dp))
                    Text("探测中")
                } else {
                    Icon(Icons.Outlined.NetworkCheck, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("重新探测")
                }
            }
        }
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
            NativeToolHeader(Icons.Outlined.ArrowOutward, "浏览器专属能力（备用）", "仅在需要 WebRTC、JavaScript 指纹或真实多地区探针时使用")
            Spacer(Modifier.height(8.dp))
            Text("以上 DNS、Whois、服务状态和设备环境均已在 APP 内完成。", fontSize = 12.sp, color = MutedInk)
            Spacer(Modifier.height(11.dp))
            OutlinedButton(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://ipcheck.ing/tools/browserinfo")))
            }) {
                Icon(Icons.Outlined.ArrowOutward, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(7.dp))
                Text("需要时打开浏览器诊断")
            }
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

    fun resolveDns(host: String): DnsLookupResult {
        val addresses = InetAddress.getAllByName(host)
            .mapNotNull { it.hostAddress }
            .distinct()
            .sortedWith(compareBy<String> { if (it.contains(":")) 1 else 0 }.thenBy { it })
        if (addresses.isEmpty()) throw IllegalStateException("未获得可用 DNS 地址")
        return DnsLookupResult(host = host, addresses = addresses)
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
        val externalRisk = runCatching { probeProxyRisk(ipv4) }.getOrNull()
        val abuseRisk = apiKeys.abuseIpDbKey.takeIf { it.isNotBlank() }?.let { key -> runCatching { probeAbuseIpDb(ipv4, key) }.getOrNull() }
        val ipApiIsSecurity = apiKeys.ipApiKey.takeIf { it.isNotBlank() }?.let { key -> runCatching { probeIpApiIsSecurity(ipv4, key) }.getOrNull() }
        val torProjectResult = runCatching { probeTorProject() }.getOrNull()
        val ipv6 = runCatching { fetchIp(IPIFY_DUAL).takeIf { it.contains(":") } }.getOrNull()
        val ipv6Geo = ipv6?.let { runCatching { probeIpApi(it) }.getOrNull() }
        if (ipApi == null && ipWhoIs == null) {
            throw IllegalStateException("公开 IP 属性数据源暂不可用")
        }

        fun ageDays(timestamp: String): Long? = runCatching {
            Duration.between(OffsetDateTime.parse(timestamp).toInstant(), Instant.now()).toDays().coerceAtLeast(0)
        }.getOrNull()

        fun freshnessFactor(timestamp: String, fallback: Double = 0.70): Double {
            val days = ageDays(timestamp) ?: return fallback
            return (0.45 + 0.55 * exp(-days / 21.0)).coerceIn(0.45, 1.0)
        }

        fun proxyCheckReliability(): Double {
            val confidence = externalRisk?.confidence?.coerceIn(0, 100)?.div(100.0) ?: 0.60
            return ((0.30 + 0.70 * confidence) * freshnessFactor(externalRisk?.detectionLastSeen.orEmpty())).coerceIn(0.25, 1.0)
        }

        fun abuseScoreRisk(score: Int): Double = 34.0 * (score.coerceIn(0, 100) / 100.0).pow(1.35)
        fun reportVolumeRisk(reports: Int): Double = 2.0 * ln(1.0 + reports.coerceAtLeast(0)) / ln(101.0)
        fun vendorRisk(score: Int?): Double {
            val raw = score?.coerceIn(0, 100) ?: return 0.0
            return 16.0 * (raw / 100.0).pow(1.20) * proxyCheckReliability()
        }
        fun historyRisk(events: Int): Double {
            if (events <= 0) return 0.0
            return 4.0 * ln(1.0 + events) / ln(101.0) * proxyCheckReliability()
        }

        val signals = mutableListOf<PuritySignal>()
        val sameIp = ipWhoIs?.let { it.ip == ipv4 }
        val observabilityRisk = if (sameIp == false) 5.0 else 0.0
        signals += when (sameIp) {
            true -> PuritySignal("多源出口一致性", "一致", "api.ipify.org 与 ipwho.is 返回同一 IPv4；不计风险", PurityTone.CONSISTENT)
            false -> PuritySignal("多源出口一致性", "不一致", "api.ipify.org：$ipv4；ipwho.is：${ipWhoIs?.ip}；出口可观测性计 ${formatRisk(observabilityRisk)} / 5", PurityTone.NOTICE)
            null -> PuritySignal("多源出口一致性", "未覆盖", "ipwho.is 暂不可用；不扣风险，但会降低证据覆盖度", PurityTone.NEUTRAL)
        }

        if (ipApi != null && ipWhoIs != null && ipApi.countryCode.isNotBlank() && ipWhoIs.countryCode.isNotBlank()) {
            val sameCountry = ipApi.countryCode.equals(ipWhoIs.countryCode, ignoreCase = true)
            signals += PuritySignal(
                "多源地理一致性",
                if (sameCountry) "一致" else "差异仅提示",
                "ipapi.co：${ipApi.countryCode}；ipwho.is：${ipWhoIs.countryCode}；地理库差异不参与风险评分",
                if (sameCountry) PurityTone.CONSISTENT else PurityTone.NEUTRAL
            )
        } else {
            signals += PuritySignal("多源地理一致性", "未覆盖", "至少一个公开地理源未返回国家代码；不因缺失扣风险", PurityTone.NEUTRAL)
        }

        if (ipv6 != null && ipv6Geo != null && ipApi != null && ipv6Geo.countryCode.isNotBlank() && ipApi.countryCode.isNotBlank()) {
            val sameCountry = ipApi.countryCode.equals(ipv6Geo.countryCode, ignoreCase = true)
            signals += PuritySignal(
                "IPv4 / IPv6 位置",
                if (sameCountry) "一致" else "差异仅提示",
                "IPv4：${ipApi.countryCode}；IPv6：${ipv6Geo.countryCode}；双栈出口差异不参与风险评分",
                if (sameCountry) PurityTone.CONSISTENT else PurityTone.NEUTRAL
            )
        } else {
            signals += PuritySignal("IPv4 / IPv6 位置", "未覆盖", "未检测到双栈出口或 IPv6 地理属性；不因缺失扣风险", PurityTone.NEUTRAL)
        }

        val proxyReliability = proxyCheckReliability()
        val torFromOfficial = torProjectResult == true
        val torFromOtherSources = externalRisk?.tor == true || abuseRisk?.isTor == true || ipApiIsSecurity?.isTor == true
        val proxySources = listOf(externalRisk?.proxy == true, ipApiIsSecurity?.isProxy == true).count { it }
        val vpnSources = listOf(externalRisk?.vpn == true, ipApiIsSecurity?.isVpn == true).count { it }
        val externalAnonymityCovered = externalRisk?.let { listOf(it.proxy, it.vpn, it.tor, it.anonymous).any { value -> value != null } } == true
        val ipApiAnonymityCovered = ipApiIsSecurity?.let { listOf(it.isProxy, it.isVpn, it.isTor).any { value -> value != null } } == true
        val anonymityCoverage = torProjectResult != null || externalAnonymityCovered || ipApiAnonymityCovered
        val proxyRisk = when {
            externalRisk?.proxy == true && ipApiIsSecurity?.isProxy == true -> maxOf(9.0, 10.0 * proxyReliability) + 2.0 * proxyReliability
            externalRisk?.proxy == true -> 10.0 * proxyReliability
            ipApiIsSecurity?.isProxy == true -> 9.0
            else -> 0.0
        }
        val vpnRisk = when {
            externalRisk?.vpn == true && ipApiIsSecurity?.isVpn == true -> maxOf(7.0, 8.0 * proxyReliability) + 1.5 * proxyReliability
            externalRisk?.vpn == true -> 8.0 * proxyReliability
            ipApiIsSecurity?.isVpn == true -> 7.0
            else -> 0.0
        }
        val transparencyRisk = when {
            torFromOfficial -> 15.0
            torFromOtherSources -> if (externalRisk?.tor == true) 13.0 + 1.5 * proxyReliability else 13.0
            else -> maxOf(proxyRisk, vpnRisk)
        }.coerceIn(0.0, 15.0)
        val anonymityFlags = buildList {
            if (torFromOfficial) add("Tor 官方确认")
            else if (torFromOtherSources) add("Tor 标记")
            if (!torFromOfficial && !torFromOtherSources && proxySources > 0) add("代理${if (proxySources >= 2) "（双源）" else ""}")
            if (!torFromOfficial && !torFromOtherSources && proxySources == 0 && vpnSources > 0) add("VPN${if (vpnSources >= 2) "（双源）" else ""}")
            if (externalRisk?.anonymous == true && isEmpty()) add("匿名标记（未单独计分）")
        }
        signals += PuritySignal(
            "匿名化 / 透明度",
            when {
                !anonymityCoverage -> "未覆盖"
                anonymityFlags.isEmpty() -> "未检出"
                else -> anonymityFlags.joinToString("、")
            },
            when {
                !anonymityCoverage -> "相关字段未覆盖；不扣风险，但会降低证据覆盖度"
                transparencyRisk == 0.0 -> "已覆盖的来源未返回 Tor、代理或 VPN 标记"
                torFromOfficial -> "Tor Project 是二元官方确认；透明度风险计 ${formatRisk(transparencyRisk)} / 15。该网络属性不等于历史滥用。"
                else -> "ProxyCheck 置信度 ${externalRisk?.confidence?.let { "$it%" } ?: "未返回，按保守回退"}、最近检出 ${externalRisk?.detectionLastSeen?.ifBlank { "未返回" } ?: "未返回"} 已连续缩放；同类只取最强结论；透明度风险计 ${formatRisk(transparencyRisk)} / 15"
            },
            when {
                !anonymityCoverage -> PurityTone.NEUTRAL
                transparencyRisk > 0.0 -> PurityTone.NOTICE
                else -> PurityTone.CONSISTENT
            }
        )

        val abuseBaseRisk = abuseRisk?.confidenceScore?.let(::abuseScoreRisk) ?: 0.0
        val abuseVolumeRisk = abuseRisk?.totalReports?.let(::reportVolumeRisk) ?: 0.0
        val abuseFreshnessRisk = abuseRisk?.confidenceScore?.takeIf { it > 0 }?.let { 2.0 * freshnessFactor(abuseRisk.lastReportedAt, 0.60) } ?: 0.0
        val abuseFromScore = minOf(36.0, abuseBaseRisk + abuseVolumeRisk + abuseFreshnessRisk)
        val proxyCheckRisk = vendorRisk(externalRisk?.risk)
        val compromised = externalRisk?.compromised == true
        val abuser = ipApiIsSecurity?.isAbuser == true
        val crawler = externalRisk?.scraper == true || ipApiIsSecurity?.isCrawler == true
        val compromisedRisk = if (compromised) 12.0 + 8.0 * proxyReliability else 0.0
        val abuserRisk = if (abuser) 16.0 else 0.0
        val crawlerRisk = when {
            externalRisk?.scraper == true -> 2.0 + 4.0 * proxyReliability
            ipApiIsSecurity?.isCrawler == true -> 4.0
            else -> 0.0
        }
        val directAttackRisk = minOf(
            28.0,
            maxOf(compromisedRisk, abuserRisk, crawlerRisk) +
                (if (compromised && abuser) 3.0 else 0.0) +
                historyRisk(externalRisk?.attackEventCount ?: 0)
        )
        val primaryEvidence = maxOf(abuseFromScore / 36.0, proxyCheckRisk / 16.0, directAttackRisk / 28.0).coerceIn(0.0, 1.0)
        val externalAttackStrength = maxOf(proxyCheckRisk / 16.0, compromisedRisk / 20.0)
        val independentStrengths = listOf(
            (abuseFromScore / 36.0).takeIf { abuseFromScore > 0.0 },
            externalAttackStrength.takeIf { externalAttackStrength > 0.0 },
            (abuserRisk / 16.0).takeIf { abuserRisk > 0.0 }
        ).filterNotNull()
        val corroborationStrength = if (independentStrengths.size >= 2) {
            (0.08 + 0.12 * independentStrengths.average()).coerceAtMost(0.20)
        } else {
            0.0
        }
        val crawlerSupport = if (crawlerRisk > 0.0 && primaryEvidence > crawlerRisk / 28.0) minOf(0.08, crawlerRisk / 50.0) else 0.0
        val abuseAndAttackRisk = (70.0 * (1.0 - (1.0 - primaryEvidence) * (1.0 - corroborationStrength) * (1.0 - crawlerSupport))).coerceIn(0.0, 70.0)
        val externalBehaviorCovered = externalRisk?.let { it.risk != null || it.compromised != null || it.scraper != null || it.attackHistoryPresent } == true
        val abuseBehaviorCovered = abuseRisk?.let { it.confidenceScore != null || it.totalReports != null } == true
        val ipApiBehaviorCovered = ipApiIsSecurity?.let { it.isAbuser != null || it.isCrawler != null } == true
        val behaviorCoverage = externalBehaviorCovered || abuseBehaviorCovered || ipApiBehaviorCovered
        val abuseFlags = buildList {
            abuseRisk?.confidenceScore?.let { add("AbuseIPDB $it") }
            externalRisk?.risk?.let { add("ProxyCheck $it") }
            if (compromised) add("受损")
            if (abuser) add("滥用")
            if (crawler) add("爬虫")
        }
        signals += PuritySignal(
            "公开滥用风险",
            when {
                !behaviorCoverage -> "未覆盖"
                abuseFlags.isEmpty() -> "未检出"
                else -> abuseFlags.joinToString("、")
            },
            when {
                !behaviorCoverage -> "滥用字段未覆盖；不扣风险，但会降低证据覆盖度"
                abuseAndAttackRisk == 0.0 -> "已覆盖来源未返回可计入的滥用或攻击信号"
                else -> "AbuseIPDB ${abuseRisk?.confidenceScore?.toString() ?: "未覆盖"}/100 → ${formatRisk(abuseFromScore)}；ProxyCheck ${externalRisk?.risk?.toString() ?: "未覆盖"}/100 → ${formatRisk(proxyCheckRisk)}；攻击历史 ${externalRisk?.attackEventCount ?: 0} 次；交叉支持 ${formatRisk(corroborationStrength * 100)}%；行为风险计 ${formatRisk(abuseAndAttackRisk)} / 70"
            },
            when {
                !behaviorCoverage -> PurityTone.NEUTRAL
                abuseAndAttackRisk > 0.0 -> PurityTone.NOTICE
                else -> PurityTone.CONSISTENT
            }
        )

        if (apiKeys.abuseIpDbKey.isBlank()) {
            signals += PuritySignal("AbuseIPDB 授权来源", "未配置", "填写本机 Key 后才查询置信分、报告数、最近报告和 Tor 字段；不扣风险，但会降低覆盖度", PurityTone.NEUTRAL)
        } else if (abuseRisk == null) {
            signals += PuritySignal("AbuseIPDB 授权来源", "未覆盖", "授权接口未返回可用结果；不扣风险，但会降低覆盖度", PurityTone.NEUTRAL)
        } else {
            val scoreText = abuseRisk.confidenceScore?.let { "$it / 100" } ?: "关键字段未覆盖"
            signals += PuritySignal(
                "AbuseIPDB 授权来源",
                scoreText,
                "报告：${abuseRisk.totalReports?.toString() ?: "未返回"}；类型：${abuseRisk.usageType.ifBlank { "未返回" }}；最近报告：${abuseRisk.lastReportedAt.ifBlank { "未返回" }}；同类供应商分数不线性叠加",
                if (abuseFromScore > 0.0 || abuseRisk.isTor == true) PurityTone.NOTICE else PurityTone.CONSISTENT
            )
        }

        val ipApiSecurityCovered = ipApiIsSecurity?.let { listOf(it.isDatacenter, it.isProxy, it.isVpn, it.isTor, it.isAbuser, it.isCrawler).any { value -> value != null } } == true
        if (apiKeys.ipApiKey.isBlank()) {
            signals += PuritySignal("ipapi.is 授权来源", "未配置", "填写本机 Key 后才查询 VPN、代理、Tor、托管、滥用与爬虫字段；不扣风险，但会降低覆盖度", PurityTone.NEUTRAL)
        } else if (ipApiIsSecurity == null || !ipApiSecurityCovered) {
            signals += PuritySignal("ipapi.is 授权来源", "字段未覆盖", "授权接口未返回完整安全字段；不把字段缺失解释为未检出", PurityTone.NEUTRAL)
        } else {
            val flags = buildList {
                if (ipApiIsSecurity.isDatacenter == true) add("数据中心")
                if (ipApiIsSecurity.isProxy == true) add("代理")
                if (ipApiIsSecurity.isVpn == true) add("VPN")
                if (ipApiIsSecurity.isCrawler == true) add("爬虫")
                if (ipApiIsSecurity.isTor == true) add("Tor")
                if (ipApiIsSecurity.isAbuser == true) add("滥用")
            }
            val identity = listOf(ipApiIsSecurity.companyName, ipApiIsSecurity.asnOrganization)
                .filter { it.isNotBlank() }.distinct().joinToString(" · ")
            signals += PuritySignal(
                "ipapi.is 授权来源",
                if (flags.isEmpty()) "未检出" else flags.joinToString("、"),
                "${if (identity.isBlank()) "未返回公司 / ASN" else identity}${if (ipApiIsSecurity.hasManagedEgress) "；受管理出口：${ipApiIsSecurity.egressSummary.ifBlank { "是" }}（仅说明）" else ""}；同类信号在风险桶内去重",
                if (flags.isEmpty()) PurityTone.CONSISTENT else PurityTone.NOTICE
            )
        }

        val metadata = ipApi ?: ipWhoIs
        val heuristicHosting = metadata?.let { isLikelyHostedNetwork(it.asn, it.organization) } == true
        val proxyCheckHostingRisk = if (externalRisk?.hosting == true) 2.0 + 2.0 * proxyReliability else 0.0
        val ipApiHostingRisk = if (ipApiIsSecurity?.isDatacenter == true) 4.5 else 0.0
        val hostingCorroboration = if (proxyCheckHostingRisk > 0.0 && ipApiHostingRisk > 0.0) 1.5 + 2.0 * proxyReliability else 0.0
        val contextRisk = minOf(
            10.0,
            maxOf(if (heuristicHosting) 2.0 else 0.0, proxyCheckHostingRisk, ipApiHostingRisk) + hostingCorroboration
        )
        val externalContextCovered = externalRisk?.hosting != null
        val ipApiContextCovered = ipApiIsSecurity?.isDatacenter != null
        val contextCoverage = metadata != null || externalContextCovered || ipApiContextCovered
        val contextFlags = buildList {
            if (proxyCheckHostingRisk > 0.0 && ipApiHostingRisk > 0.0) add("双源托管 / 数据中心")
            else if (proxyCheckHostingRisk > 0.0 || ipApiHostingRisk > 0.0) add("直接托管 / 数据中心")
            else if (heuristicHosting) add("ASN / 组织启发式")
            if (ipApiIsSecurity?.hasManagedEgress == true) add("受管理出口（仅说明）")
        }
        signals += PuritySignal(
            "网络上下文",
            when {
                !contextCoverage -> "未覆盖"
                contextFlags.isEmpty() -> "未标记"
                else -> contextFlags.joinToString("、")
            },
            when {
                !contextCoverage -> "ASN、组织或托管字段未覆盖；不扣风险，但会降低证据覆盖度"
                contextRisk == 0.0 -> "已覆盖来源未把当前出口标为托管或数据中心"
                else -> "云 / IDC / 托管是网络背景而非恶意证据；上下文风险计 ${formatRisk(contextRisk)} / 10"
            },
            when {
                !contextCoverage -> PurityTone.NEUTRAL
                contextRisk > 0.0 -> PurityTone.NOTICE
                else -> PurityTone.CONSISTENT
            }
        )

        val privacy = inspectPrivacy(context)
        signals += PuritySignal(
            "Android 网络状态",
            if (privacy.vpnActive) "VPN 已连接" else "未检测到 VPN",
            "Private DNS：${privacy.privateDnsMode}${if (privacy.dnsServers.isNotEmpty()) "；DNS：${privacy.dnsServers.take(2).joinToString("、")}" else ""}；系统状态只展示，不参与历史滥用评分",
            PurityTone.NEUTRAL
        )

        val coverageItems = listOf(
            "直接行为" to (70.0 to behaviorCoverage),
            "匿名化" to (15.0 to anonymityCoverage),
            "网络上下文" to (10.0 to contextCoverage),
            "出口观测" to (5.0 to (sameIp != null))
        )
        val coverage = coverageItems.sumOf { (_, item) -> if (item.second) item.first else 0.0 }.coerceIn(0.0, 100.0)
        val coverageLabel = when {
            coverage >= 80.0 -> "证据覆盖较完整"
            coverage >= 50.0 -> "部分覆盖"
            else -> "覆盖不足"
        }
        val missingCoverage = coverageItems.filter { (_, item) -> !item.second }.joinToString("、") { it.first }
        val coverageDetail = if (missingCoverage.isBlank()) {
            "直接行为、匿名化、网络上下文和出口观测四类关键证据均已覆盖。"
        } else {
            "未覆盖：$missingCoverage。未覆盖不会扣风险，也不应被解释为无风险。"
        }
        signals += PuritySignal(
            "证据覆盖度",
            "${formatRisk(coverage)} / 100 · $coverageLabel",
            coverageDetail,
            when {
                coverage >= 80.0 -> PurityTone.CONSISTENT
                coverage >= 50.0 -> PurityTone.NEUTRAL
                else -> PurityTone.NOTICE
            }
        )

        val buckets = listOf(
            PurityRiskBucket("直接恶意与滥用", abuseAndAttackRisk, 70.0, "近期、可溯源的滥用与攻击信号主导；同类来源去重并使用有限交叉支持"),
            PurityRiskBucket("匿名化 / 透明度", transparencyRisk, 15.0, "Tor、代理和 VPN 是网络属性，单列展示，不等同于历史恶意"),
            PurityRiskBucket("网络上下文", contextRisk, 10.0, "ASN、云/IDC、托管和网段背景仅作低上限先验"),
            PurityRiskBucket("出口可观测性", observabilityRisk, 5.0, "只反映同次出口地址冲突，不推断历史行为")
        )
        val totalRisk = buckets.sumOf { it.risk }.coerceIn(0.0, 100.0)
        val score = 100.0 - totalRisk
        val label = when {
            totalRisk <= 10.0 && coverage >= 50.0 -> "低风险信号"
            totalRisk <= 30.0 -> "轻度提示"
            totalRisk <= 60.0 -> "需复核"
            else -> "高风险提示"
        }
        val summary = when {
            coverage < 50.0 -> "关键证据覆盖不足；当前分数只反映已覆盖来源，不应据此判断出口安全性。"
            totalRisk <= 10.0 -> "本次已覆盖的公开来源中未见明显风险信号；这不是安全保证。"
            totalRisk <= 30.0 -> "发现有限、可解释的公开风险或透明度提示，建议结合网络配置复检。"
            totalRisk <= 60.0 -> "发现较强的公开风险或匿名化/出口观测提示，建议核对网络路径与风险明细。"
            else -> "多个高权重公开风险信号同时命中；仅作网络出口风险提示，不能单独推断个人或账号行为。"
        }
        return PurityReport(
            score = score,
            risk = totalRisk,
            abuseRisk = abuseAndAttackRisk,
            transparencyRisk = transparencyRisk,
            contextRisk = contextRisk,
            observabilityRisk = observabilityRisk,
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
        return PrivacySnapshot(
            vpnActive = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true,
            privateDnsMode = privateDns,
            dnsServers = properties?.dnsServers?.mapNotNull { it.hostAddress } ?: emptyList()
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
