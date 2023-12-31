
import androidx.compose.animation.Crossfade
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nav.NavController
import nav.NavControllerInstance
import nav.Page
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import repo.MainRepository
import repo.MainRepositoryImpl
import screens.LoginScreen
import screens.QrCodeScreen
import source.NetworkSource
import source.NetworkSourceImpl
import ui.screens.pages.lessons.LessonCard
import ui.screens.pages.lessons.MainScreen


fun main() = application {
    KoinContext(koinApplication { modules(module) }.koin) {
        var qrCodeWindowOpened by remember { mutableStateOf<UInt?>(null) }
        Window(onCloseRequest = ::exitApplication, title = "Attendance") {
            MaterialTheme {
                val navController: NavController = remember { NavControllerInstance(Page.Main) }
                val mainRepository: MainRepository = koinInject()
                val scope = rememberCoroutineScope { Dispatchers.IO }
                val currentPage by navController.collectCurrentPageAsState()

                LaunchedEffect(Unit) {
                    scope.launch {
                        if (!mainRepository.isLoggedIn())
                            navController.navigate(Page.LogIn)
                    }
                }

                Crossfade(currentPage) {
                    when (it) {
                        Page.LogIn -> LoginScreen { navController.navigateUp() }
                        Page.Main -> MainScreen {
                            navController.navigate(Page.Lesson(it))
                        }
                        is Page.Lesson -> LessonCard(it.id, {
                            navController.navigateUp()
                        }, { qrCodeWindowOpened = it })
                    }
                }
            }
            qrCodeWindowOpened?.let {
                Window(onCloseRequest = { qrCodeWindowOpened = null }, title = "QR Code") {
                    QrCodeScreen(it) { qrCodeWindowOpened = null }
                }
            }
        }
    }
}

val module = module {
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json()
            }
        }
    }
    single<NetworkSource> { NetworkSourceImpl(get()) }
    single<MainRepository> { MainRepositoryImpl(get()) }
}
