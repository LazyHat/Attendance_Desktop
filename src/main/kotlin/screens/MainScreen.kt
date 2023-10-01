package screens

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import models.Lesson
import models.LessonCreate
import org.koin.compose.koinInject
import repo.MainRepository

@Composable
@Preview
fun MainScreen(toLesson: (id: UInt) -> Unit) {
    val mainRepository = koinInject<MainRepository>()
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val lessons = remember { mutableStateListOf<Lesson>() }
    var createLessonDialog by remember { mutableStateOf(false) }
    fun updateLessons() {
        scope.launch {
            lessons.clear()
            lessons.addAll(mainRepository.getLessons())
        }
    }

    LaunchedEffect(Unit) {
        updateLessons()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lessons") },
                actions = {
                    Button(onClick = {
                        updateLessons()
                    }) {
                        Text("Refresh")
                    }
                    Button(onClick = {
                        createLessonDialog = true
                    }) {
                        Text("create lesson")
                    }
                }
            )
        }
    ) {
        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(lessons) {
                LessonCard(it) {
                    toLesson(it.id)
                }
            }
        }
    }
    if (createLessonDialog) {
        CreateLessonDialog({ createLessonDialog = false }) {
            scope.launch {
                mainRepository.createLesson(it)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LessonCard(state: Lesson, onClick: () -> Unit) {
    Card(
        onClick = onClick
    ) {
        Column {
            Text("id: " + state.id)
            Text("Title: " + state.title)
            Text("Teacher: " + state.username)
            Text("start: " + state.start)
            Text("end: " + state.end)
            Text("groups: " + state.groupsList)
        }
    }
}

@Composable
fun CreateLessonDialog(onClose: () -> Unit, onCreate: (LessonCreate) -> Unit) {
    AlertDialog(onDismissRequest = onClose, {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            var title by remember { mutableStateOf("") }
            val start by remember {
                mutableStateOf(
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                )
            }
            val end by remember { mutableStateOf(start.toJavaLocalDateTime().plusHours(2).toKotlinLocalDateTime()) }
            val groups = remember { mutableStateListOf<String>() }
            Text("Create lesson")
            TextField(value = title, onValueChange = { title = it }, label = { Text("title") })
            Text(text = start.toString())
            Text(text = end.toString())
            LazyColumn {
                items(groups) {
                    Text(it)
                }
                item {
                    var new by remember { mutableStateOf("") }

                    fun addGroup() {
                        if (new.isNotEmpty()) {
                            groups.add(new)
                            new = ""
                        }
                    }

                    TextField(
                        value = new,
                        onValueChange = { new = it },
                        label = { Text("add group") },
                        trailingIcon = {
                            Button({
                                addGroup()
                            }) {
                                Text("add")
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            addGroup()
                        })
                    )
                }
            }
            Button({
                onCreate(LessonCreate(title, start, end, groups.toSet()))
                onClose()
            }) {
                Text("create lesson")
            }
        }
    })
}