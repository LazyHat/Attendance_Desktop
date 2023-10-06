package ui.screens.pages.lessons

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.koin.compose.koinInject
import repo.MainRepository
import ru.lazyhat.models.Lesson
import ru.lazyhat.models.plus
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*
import kotlin.time.Duration.Companion.hours

@Composable
fun MainScreen(openLesson: (UInt) -> Unit) {
    val scope = rememberCoroutineScope()
    val mainRepository = koinInject<MainRepository>()
    val lessons = remember { mutableStateListOf<Lesson>() }
    var refreshing by remember { mutableStateOf(false) }

    fun refreshPage() {
        refreshing = true
        scope.launch {
            val result = mainRepository.getLessons()
            lessons.clear()
            lessons.addAll(result)
            refreshing = false
        }
    }

    LaunchedEffect(Unit) {
        refreshPage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lessons") },
                actions = {
                    Button(onClick = {
                        refreshPage()
                    }) {
                        Text(if (refreshing) "Refreshing..." else "Refresh")
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
                LessonListItem(it) {
                    openLesson(it.id)
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewLessonListItem() {
    val lesson =
        Lesson(1U, "lazy", "English", DayOfWeek.FRIDAY, LocalTime(15, 0), 2U, LocalDate(2023, 10, 5), 5U, setOf("2092"))
    Column {
        LessonListItem(lesson, {})
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LessonListItem(state: Lesson, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.fillMaxSize().clickable(onClick = onClick),
        icon = {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = state.id.toString(),
                fontSize = 30.sp
            )
        },
        text = {
            Text(
                state.title,
                fontSize = 19.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            )
        },
        secondaryText = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${
                        state.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    } ${state.startTime}-${state.startTime.plus(state.durationHours.toInt().hours)}\n${
                        state.groups.joinToString(
                            ", "
                        )
                    }",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        trailing = {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(state.teacher, fontSize = 18.sp)
            }
        }
    )
}