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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.compose.koinInject
import repo.MainRepository
import ru.lazyhat.models.LessonToken
import ru.lazyhat.models.minus
import ru.lazyhat.models.now
import ru.lazyhat.models.roundTo
import kotlin.math.abs
import kotlin.math.min
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun QrCodeScreen(lessonId: UInt, closeWindow: () -> Unit) {
    val mainRepository = koinInject<MainRepository>()
    var tokenInfo by remember { mutableStateOf<LessonToken?>(null) }
    var expiresIn by remember { mutableStateOf(ZERO) }
    val closeVia = expiresIn + 5.seconds
    val scope = rememberCoroutineScope { Dispatchers.IO }

    fun updateToken() {
        scope.launch {
            tokenInfo = mainRepository.createToken(lessonId)
        }
    }

    LaunchedEffect(tokenInfo) {
        tokenInfo?.let {
            while (true) {
                expiresIn = (it.expires - LocalDateTime.now()).roundTo(DurationUnit.SECONDS)
                delay(1.seconds)
                if (!isActive) break
            }
        }
    }

    if (closeVia < ZERO)
        closeWindow()

    LaunchedEffect(lessonId) {
        updateToken()
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Scan") }, actions = {
            Button({
                updateToken()
            }) {
                Text("Refresh QRCode")
            }
        })
    }) {
        if (tokenInfo != null) tokenInfo?.let {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(it.id, BarcodeFormat.QR_CODE, 0, 0)
            val width = bitMatrix.width
            val height = bitMatrix.height
            var size by remember { mutableStateOf(IntSize(50, 50)) }
            val scaleX = size.width / width.toFloat()
            val scaleY = size.height / height.toFloat()
            val scale = min(scaleX, scaleY)
            Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                if (expiresIn > ZERO)
                    Canvas(Modifier.padding(5.dp).fillMaxSize(0.9f).onGloballyPositioned {
                        val minimumDelta = 50
                        if (abs(size.height - it.size.height) >= minimumDelta || abs(size.width - it.size.width) >= minimumDelta) size =
                            it.size
                    }) {
                        for (x in 0 until width) for (y in 0 until height) {
                            if (bitMatrix[x, y]) {
                                drawRect(
                                    Color.Black,
                                    Offset(x.toFloat() * scale, y.toFloat() * scale),
                                    size = Size(scale, scale).times(1.0f)
                                )
                            }
                        }
                    }
                Text(
                    if (expiresIn > ZERO) "token will expire in $expiresIn"
                    else "Window will automatically closed after $closeVia"
                )
            }
        }
        else {
            Text("Loading...", modifier = Modifier.fillMaxSize())
        }
    }
}