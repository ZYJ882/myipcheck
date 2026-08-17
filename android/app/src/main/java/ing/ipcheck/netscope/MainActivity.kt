package ing.ipcheck.netscope

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.material.icons.outlined.VpnLock
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

private data class PurityReport(
    val score: Int,
    val label: String,
    val summary: String,
    val signals: List<PuritySignal>,
    val checkedAt: String
)

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
    val proxy: Boolean,
    val vpn: Boolean,
    val tor: Boolean,
    val hosting: Boolean,
    val compromised: Boolean,
    val scraper: Boolean,
    val anonymous: Boolean,
    val risk: Int?,
    val confidence: Int?,
    val attackSummary: String
)

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
            runCatching { withContext(Dispatchers.IO) { NetworkRepository.runPurityDiagnosis(context) } }
                .onSuccess { purityReport = it }
                .onFailure { purityError = it.asUserMessage() }
            purityLoading = false
        }
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
                    Box(
                        modifier = Modifier
                            .padding(start = 14.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoftBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Public, contentDescription = null, tint = Blue)
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
                    title = "透明纯净度诊断",
                    subtitle = "参考公开网络信号评估当前出口一致性",
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
                            modifier = Modifier.size(58.dp).clip(RoundedCornerShape(18.dp)).background(scoreBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${report.score}", fontWeight = FontWeight.Bold, fontSize = 23.sp, color = scoreColor)
                                Text("/ 100", fontSize = 9.sp, color = scoreColor)
                            }
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(report.label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                            Text(report.summary, fontSize = 12.sp, color = MutedInk, lineHeight = 18.sp)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Border)
                    Spacer(Modifier.height(5.dp))
                    report.signals.forEachIndexed { index, signal ->
                        PuritySignalLine(signal)
                        if (index < report.signals.lastIndex) HorizontalDivider(color = Border, modifier = Modifier.padding(start = 28.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("检测时间：${report.checkedAt}", fontSize = 11.sp, color = MutedInk)
                    Spacer(Modifier.height(7.dp))
                    Text("说明：本报告仅查询当前公网出口的公开风险与一致性信号，不读取账号、Cookie、浏览记录或设备指纹。分数不是 Ping0 风控值，不包含专有 IP 段标注、共享人数或历史信誉数据。", fontSize = 11.sp, color = MutedInk, lineHeight = 16.sp)
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

    fun runPurityDiagnosis(context: Context): PurityReport {
        val ipv4 = fetchIp(IPIFY_V4)
        val ipApi = runCatching { probeIpApi(ipv4) }.getOrNull()
        val ipWhoIs = runCatching { probeIpWhoIs() }.getOrNull()
        val externalRisk = runCatching { probeProxyRisk(ipv4) }.getOrNull()
        val torProjectResult = runCatching { probeTorProject() }.getOrNull()
        val ipv6 = runCatching { fetchIp(IPIFY_DUAL).takeIf { it.contains(":") } }.getOrNull()
        val ipv6Geo = ipv6?.let { runCatching { probeIpApi(it) }.getOrNull() }
        if (ipApi == null && ipWhoIs == null) {
            throw IllegalStateException("公开 IP 属性数据源暂不可用")
        }

        val signals = mutableListOf<PuritySignal>()
        var score = 100

        if (ipWhoIs != null) {
            val sameIp = ipv4 == ipWhoIs.ip
            if (!sameIp) score -= 35
            signals += PuritySignal(
                title = "多源出口一致性",
                value = if (sameIp) "一致" else "不一致",
                detail = "api.ipify.org：$ipv4；ipwho.is：${ipWhoIs.ip}",
                tone = if (sameIp) PurityTone.CONSISTENT else PurityTone.NOTICE
            )
        } else {
            signals += PuritySignal("多源出口一致性", "未覆盖", "ipwho.is 暂不可用，本次不扣分", PurityTone.NEUTRAL)
        }

        if (ipApi != null && ipWhoIs != null && ipApi.countryCode.isNotBlank() && ipWhoIs.countryCode.isNotBlank()) {
            val sameCountry = ipApi.countryCode.equals(ipWhoIs.countryCode, ignoreCase = true)
            if (!sameCountry) score -= 15
            signals += PuritySignal(
                title = "多源地理一致性",
                value = if (sameCountry) "一致" else "提示",
                detail = "ipapi.co：${ipApi.countryCode}；ipwho.is：${ipWhoIs.countryCode}",
                tone = if (sameCountry) PurityTone.CONSISTENT else PurityTone.NOTICE
            )
        } else {
            signals += PuritySignal("多源地理一致性", "未覆盖", "至少一个公开数据源未返回国家代码，本次不扣分", PurityTone.NEUTRAL)
        }

        if (ipv6 != null && ipv6Geo != null && ipApi != null && ipv6Geo.countryCode.isNotBlank() && ipApi.countryCode.isNotBlank()) {
            val sameCountry = ipApi.countryCode.equals(ipv6Geo.countryCode, ignoreCase = true)
            if (!sameCountry) score -= 15
            signals += PuritySignal(
                title = "IPv4 / IPv6 位置",
                value = if (sameCountry) "一致" else "提示",
                detail = "IPv4：${ipApi.countryCode}；IPv6：${ipv6Geo.countryCode}",
                tone = if (sameCountry) PurityTone.CONSISTENT else PurityTone.NOTICE
            )
        } else {
            signals += PuritySignal("IPv4 / IPv6 位置", "未覆盖", "未检测到双栈出口或 IPv6 地理属性，本次不扣分", PurityTone.NEUTRAL)
        }

        val metadata = ipApi ?: ipWhoIs
        if (metadata != null && (metadata.asn.isNotBlank() || metadata.organization.isNotBlank())) {
            val hosted = isLikelyHostedNetwork(metadata.asn, metadata.organization)
            if (hosted) score -= 8
            signals += PuritySignal(
                title = "公开网络属性",
                value = if (hosted) "托管提示" else "已读取",
                detail = listOf(metadata.asn, metadata.organization).filter { it.isNotBlank() }.joinToString(" · "),
                tone = if (hosted) PurityTone.NOTICE else PurityTone.CONSISTENT
            )
        } else {
            signals += PuritySignal("公开网络属性", "未覆盖", "ASN / 组织字段不可用，本次不扣分", PurityTone.NEUTRAL)
        }

        var torPenaltyApplied = false
        if (externalRisk != null) {
            val riskPenalty = when (externalRisk.risk ?: -1) {
                in 75..100 -> 25
                in 50..74 -> 16
                in 25..49 -> 8
                else -> 0
            }
            score -= riskPenalty
            val riskText = externalRisk.risk?.let { "$it / 100" } ?: "未返回"
            signals += PuritySignal(
                title = "公开风险评分",
                value = riskText,
                detail = "${externalRisk.source} 置信度：${externalRisk.confidence?.let { "$it%" } ?: "未返回"}${if (riskPenalty > 0) "；已按公开规则扣 $riskPenalty 分" else "；本项不扣分"}",
                tone = if (riskPenalty > 0) PurityTone.NOTICE else PurityTone.CONSISTENT
            )

            val relayFlags = buildList {
                if (externalRisk.proxy) add("代理")
                if (externalRisk.vpn) add("VPN")
                if (externalRisk.tor) add("Tor")
                if (externalRisk.anonymous) add("匿名")
            }
            val relayPenalty = (if (externalRisk.proxy) 18 else 0) + (if (externalRisk.vpn) 12 else 0) + (if (externalRisk.tor) 30 else 0)
            score -= relayPenalty
            if (externalRisk.tor) torPenaltyApplied = true
            signals += PuritySignal(
                title = "代理 / VPN / Tor",
                value = if (relayFlags.isEmpty()) "未检出" else relayFlags.joinToString("、"),
                detail = if (relayFlags.isEmpty()) "${externalRisk.source} 未返回代理、VPN 或 Tor 标记" else "${externalRisk.source} 明确标记；已按公开规则扣 $relayPenalty 分",
                tone = if (relayFlags.isEmpty()) PurityTone.CONSISTENT else PurityTone.NOTICE
            )

            val attackPenalty = (if (externalRisk.compromised) 18 else 0) + (if (externalRisk.scraper) 10 else 0)
            score -= attackPenalty
            val attackFlags = buildList {
                if (externalRisk.compromised) add("受损")
                if (externalRisk.scraper) add("爬虫")
            }
            signals += PuritySignal(
                title = "公开攻击提示",
                value = if (attackFlags.isEmpty()) "未检出" else attackFlags.joinToString("、"),
                detail = when {
                    attackFlags.isNotEmpty() -> "${externalRisk.source}：${externalRisk.attackSummary.ifBlank { "无附加摘要" }}；已扣 $attackPenalty 分"
                    externalRisk.attackSummary.isNotBlank() -> externalRisk.attackSummary
                    else -> "${externalRisk.source} 未返回受损或爬虫标记"
                },
                tone = if (attackFlags.isEmpty()) PurityTone.CONSISTENT else PurityTone.NOTICE
            )

            if (externalRisk.hosting) score -= 6
            signals += PuritySignal(
                title = "风险源托管属性",
                value = if (externalRisk.hosting) "托管提示" else "未标记",
                detail = if (externalRisk.hosting) "${externalRisk.source} 将当前出口标记为托管网络；已扣 6 分" else "${externalRisk.source} 未标记托管网络",
                tone = if (externalRisk.hosting) PurityTone.NOTICE else PurityTone.CONSISTENT
            )
        } else {
            signals += PuritySignal("公开风险源", "未覆盖", "风险数据源暂不可用，本次不扣分", PurityTone.NEUTRAL)
        }

        if (torProjectResult == true) {
            if (!torPenaltyApplied) score -= 30
            signals += PuritySignal(
                title = "Tor 官方出口验证",
                value = "Tor 出口",
                detail = if (torPenaltyApplied) "Tor Project 官方接口确认；已由其他 Tor 证据计分，未重复扣分" else "Tor Project 官方接口确认；已扣 30 分",
                tone = PurityTone.NOTICE
            )
        } else if (torProjectResult == false) {
            signals += PuritySignal("Tor 官方出口验证", "未检出", "Tor Project 官方接口未将当前出口识别为 Tor", PurityTone.CONSISTENT)
        } else {
            signals += PuritySignal("Tor 官方出口验证", "未覆盖", "Tor Project 接口暂不可用，本次不扣分", PurityTone.NEUTRAL)
        }

        val privacy = inspectPrivacy(context)
        signals += PuritySignal(
            title = "Android 网络状态",
            value = if (privacy.vpnActive) "VPN 已连接" else "未检测到 VPN",
            detail = "Private DNS：${privacy.privateDnsMode}${if (privacy.dnsServers.isNotEmpty()) "；DNS：${privacy.dnsServers.take(2).joinToString("、")}" else ""}",
            tone = PurityTone.NEUTRAL
        )

        score = score.coerceIn(0, 100)
        val label = when {
            score >= 90 -> "出口一致"
            score >= 70 -> "轻度提示"
            score >= 40 -> "存在明显风险或不一致"
            else -> "高风险提示"
        }
        val summary = when {
            score >= 90 -> "公开数据源的当前出口信息基本一致。"
            score >= 70 -> "发现可解释的网络属性提示，建议结合实际网络配置复检。"
            else -> "发现多个公开风险或出口不一致信号，建议检查代理、VPN、双栈和分流配置。"
        }
        return PurityReport(
            score = score,
            label = label,
            summary = summary,
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

    private fun probeProxyRisk(ip: String): RiskIntelligence {
        val root = JSONObject(getText("https://proxycheck.io/v3/$ip?vpn=1&asn=1&risk=1"))
        if (!root.stringOrBlank("status").equals("ok", ignoreCase = true)) {
            throw IllegalStateException(root.stringOrBlank("message").ifBlank { "ProxyCheck 未返回可用结果" })
        }
        val result = root.optJSONObject(ip) ?: throw IllegalStateException("ProxyCheck 未返回当前 IP 的结果")
        val detections = result.optJSONObject("detections")
        val attackHistory = result.optJSONObject("attack_history")
        val attacks = mutableListOf<String>()
        if (attackHistory != null) {
            val keys = attackHistory.keys()
            while (keys.hasNext() && attacks.size < 2) {
                val key = keys.next()
                val count = attackHistory.optInt(key, 0)
                if (count > 0) attacks += "${key.replace('_', ' ')}：$count"
            }
        }
        fun intField(name: String): Int? = detections?.opt(name)?.toString()?.toIntOrNull()
        return RiskIntelligence(
            source = "ProxyCheck",
            proxy = detections?.optBoolean("proxy", false) ?: false,
            vpn = detections?.optBoolean("vpn", false) ?: false,
            tor = detections?.optBoolean("tor", false) ?: false,
            hosting = detections?.optBoolean("hosting", false) ?: false,
            compromised = detections?.optBoolean("compromised", false) ?: false,
            scraper = detections?.optBoolean("scraper", false) ?: false,
            anonymous = detections?.optBoolean("anonymous", false) ?: false,
            risk = intField("risk"),
            confidence = intField("confidence"),
            attackSummary = attacks.joinToString("；")
        )
    }

    private fun probeTorProject(): Boolean {
        val json = JSONObject(getText("https://check.torproject.org/api/ip"))
        return json.optBoolean("IsTor", false)
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

    private fun getText(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "NetScope Android/1.0")
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
