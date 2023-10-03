package screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import models.now
import models.plus
import org.koin.compose.koinInject
import repo.MainRepository
import ru.lazyhat.models.Lesson
import ru.lazyhat.models.Status
import ru.lazyhat.models.Student

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LessonScreen(id: UInt, generateCode: (UInt) -> Unit, onBack: () -> Unit) {
    var lesson by remember { mutableStateOf<Lesson?>(null) }
    var students by remember { mutableStateOf<Map<String, Set<Student>>>(mapOf()) }
    val mainRepository = koinInject<MainRepository>()
    val scope = rememberCoroutineScope { Dispatchers.IO }

    fun updateInfo() {
        scope.launch {
            lesson = mainRepository.getLessonById(id)
            students = mainRepository.getStudentsByLesson(id)
        }
    }

    LaunchedEffect(Unit) {
        updateInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lesson: ${lesson?.title}") }, actions = {
                Button(onBack) {
                    Text("Back")
                }
                Button({ updateInfo() }) {
                    Text("Refresh")
                }
            })
        }
    ) {
        lesson?.let {
            Row(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Card {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(it.title)
                            Text("DOW: ${it.dayOfWeek.name}")
                            Text("Start: ${it.start}")
                            Text("End: ${it.start + it.duration}")
                            Text("Groups: ${it.groupsList}")
                            if (it.start < LocalDateTime.now().time)
                                Button({
                                    generateCode(it.id)
                                }) {
                                    Text("Create QR Code")
                                }
                        }
                    }
                }
                LazyColumn(modifier = Modifier.weight(1f)) {
                    students.forEach {
                        stickyHeader { Text(it.key) }
                        items(it.value.toList()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(it.fullName)
                                Text(
                                    it.status.name, color = when (it.status) {
                                        Status.Idle -> Color.Gray
                                        Status.InLesson -> Color.Green
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
