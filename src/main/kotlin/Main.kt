
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.Lesson
import net.Net
import net.Token
import kotlin.math.abs
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
@Preview
fun App(generateRequest: (lessonId: Int) -> Unit) {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        var lesson by remember {
            mutableStateOf<Lesson?>(null)
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Lessons") },
                    actions = {
                        Button(onClick = {
                            scope.launch(Dispatchers.IO) {
                                lesson = Net.getLessonInfo(1)
                            }
                        }) {
                            Text("Refresh")
                        }
                    }
                )
            }
        ) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = CenterHorizontally
            ) {
                Text(lesson?.title.orEmpty())
                Text(lesson?.start.toString())
                Text(lesson?.end.toString())
                Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                        generateRequest(1)
                    }
                }) {
                    Text("Generate QR-Code")
                }
            }
        }
    }
}

@Composable
fun QrCodeScreen(lessonId: Int) {
    var tokenInfo by remember { mutableStateOf<Token?>(null) }
    var expiresIn by remember { mutableStateOf(Duration.ZERO) }
    var job: Job? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    fun updateToken() {
        scope.launch(Dispatchers.IO) {
            tokenInfo = Net.getLessonToken(lessonId)
        }
    }

    LaunchedEffect(tokenInfo) {
        tokenInfo?.let {
            job?.cancel()
            job = scope.launch {
                while (true){
                    expiresIn =
                        (it.expires.toInstant(TimeZone.currentSystemDefault()) - Clock.System.now()).toComponents { days, hours, minutes, seconds, nanoseconds ->
                            days.days + hours.hours + minutes.minutes + seconds.seconds
                        }
                    delay(1.seconds)
                    if(!isActive)
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
                    Text("token expires will expire in $expiresIn")
                }
            }
        else {
            Text("Loading...", modifier = Modifier.fillMaxSize())
        }
    }
}

fun main() = application {
    var isQrQodeWindowShowed by remember { mutableStateOf<Int?>(null) }
    Window(onCloseRequest = ::exitApplication) {
        App {
            isQrQodeWindowShowed = it
        }
    }
    isQrQodeWindowShowed?.let {
        Window(onCloseRequest = {
            isQrQodeWindowShowed = null
        }) {
            QrCodeScreen(it)
        }
    }
}
