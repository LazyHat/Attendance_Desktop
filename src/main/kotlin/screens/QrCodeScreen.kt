package screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import models.LessonToken
import org.koin.compose.koinInject
import repo.MainRepository
import kotlin.math.abs
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun QrCodeScreen(lessonId: UInt) {
    val mainRepository = koinInject<MainRepository>()
    var tokenInfo by remember { mutableStateOf<LessonToken?>(null) }
    var expiresIn by remember { mutableStateOf(Duration.ZERO) }
    var job: Job? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope { Dispatchers.IO }

    fun updateToken() {
        scope.launch {
            tokenInfo = mainRepository.createToken(lessonId)
        }
    }

    LaunchedEffect(tokenInfo) {
        tokenInfo?.let {
            job?.cancel()
            job = scope.launch {
                while (true) {
                    expiresIn =
                        (it.expires.toInstant(TimeZone.currentSystemDefault()) - Clock.System.now()).toComponents { days, hours, minutes, seconds, nanoseconds ->
                            days.days + hours.hours + minutes.minutes + seconds.seconds
                        }
                    delay(1.seconds)
                    if (!isActive)
                        break
                }
            }
        }
    }

    LaunchedEffect(lessonId) {
        updateToken()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan") },
                actions = {
                    Button(
                        {
                            updateToken()
                        }
                    ) {
                        Text("Refresh QRCode")
                    }
                }
            )
        }
    ) {
        if (tokenInfo != null)
            tokenInfo?.let {
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(it.id, BarcodeFormat.QR_CODE, 0, 0)
                val width = bitMatrix.width
                val height = bitMatrix.height
                var size by remember { mutableStateOf(IntSize(50, 50)) }
                val scaleX = size.width / width.toFloat()
                val scaleY = size.height / height.toFloat()
                val scale = min(scaleX, scaleY)
                Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    Canvas(Modifier.padding(5.dp).fillMaxSize(0.9f).onGloballyPositioned {
                        val minimumDelta = 50
                        if (abs(size.height - it.size.height) >= minimumDelta || abs(size.width - it.size.width) >= minimumDelta)
                            size = it.size
                    }) {
                        for (x in 0 until width)
                            for (y in 0 until height) {
                                if (bitMatrix[x, y]) {
                                    drawRect(
                                        Color.Black,
                                        Offset(x.toFloat() * scale, y.toFloat() * scale),
                                        size = Size(scale, scale).times(1.0f)
                                    )
                                }
                            }
                    }
                    Text("token will expire in $expiresIn")
                }
            }
        else {
            Text("Loading...", modifier = Modifier.fillMaxSize())
        }
    }
}