package ui.screens.pages.lessons

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.compose.koinInject
import repo.MainRepository
import ru.lazyhat.models.*
import kotlin.time.Duration.Companion.hours


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LessonCard(id: UInt, onBack: () -> Unit, openQrCode: (UInt) -> Unit) {
    val mainRepository = koinInject<MainRepository>()
    var lesson by remember { mutableStateOf<Lesson?>(null) }
    var attendance by remember { mutableStateOf<LessonAttendance?>(null) }
    val scope = rememberCoroutineScope { Dispatchers.IO }
    var refreshing by remember { mutableStateOf(false) }
    val selectedCells = remember { mutableStateListOf<Pair<LocalDate, Student>>() }

    fun refreshInfo() {
        refreshing = true
        scope.launch {
            lesson = mainRepository.getLessonById(id)
            attendance = mainRepository.getLessonAttendance(id)
            refreshing = false
        }
    }

    fun patchAttendance(status: AttendanceStatus) {
        scope.launch {
            mainRepository.updateAttendance(
                RegistryRecordUpdate(
                    id,
                    selectedCells.map {
                        RegistryRecordUpdate.Parameters(
                            it.second.username,
                            it.first
                        )
                    },
                    status
                )
            ).let {
                if (it) {
                    refreshInfo()
                    selectedCells.clear()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lesson: ${lesson?.title}") },
                actions = {
                    if (selectedCells.isNotEmpty()) {
                        Text("selected: ${selectedCells.count()}")
                        Spacer(Modifier.width(20.dp))
                        Text("Mark as:")
                        AttendanceStatus.entries.forEach {
                            Button({ patchAttendance(it) }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(it.name)
                                    Icon(painterResource("circle.svg"), contentDescription = null, tint = it.color)
                                }
                            }
                        }
                    }
                    Button(onBack) {
                        Text("Back")
                    }
                    Button(enabled = !refreshing, onClick = { refreshInfo() }) {
                        Text(if (refreshing) "Refreshing..." else "Refresh")
                    }
                })
        }
    ) {
        lesson?.let {
            Row(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Column {
                    Card {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(it.title)
                            Text("DOW: ${it.dayOfWeek.name}")
                            Text("Start: ${it.startTime}")
                            Text("End: ${it.startTime.plus(it.durationHours.toInt().hours)}")
                            Text("Groups: ${it.groups}")
                            if (LocalDateTime.now().time in it.startTime.rangeTo(it.startTime.plus(it.durationHours.toInt().hours)) && it.dayOfWeek == LocalDateTime.now().dayOfWeek)
                                Button({ openQrCode(id) }) {
                                    Text("Generate qr code")
                                }
                            else
                                Text("Unavailable for registration")
                        }
                    }
                }
                Spacer(Modifier.width(20.dp).fillMaxHeight())
                attendance?.let {
                    AttendanceTable(it, selectedCells) { pair ->
                        if (selectedCells.contains(pair))
                            selectedCells.remove(pair)
                        else
                            selectedCells.add(pair)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttendanceTable(
    attendance: LessonAttendance,
    selectedCells: List<Pair<LocalDate, Student>>,
    onClick: (Pair<LocalDate, Student>) -> Unit
) {
    val fullnameWeight = 12f
    val cellWeight = 1f
    var selectedRow by remember { mutableStateOf<Int?>(null) }
    val state = rememberLazyListState()
    Box {
        LazyColumn(
            state = state,
            modifier = Modifier.padding(end = 10.dp)
        ) {
            attendance.groups.forEach { groupAttendance ->
                stickyHeader {
                    Column(Modifier.background(Color.White)) {
                        Row {
                            Text(
                                groupAttendance.group,
                                Modifier.weight(fullnameWeight),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            groupAttendance.attendance.firstOrNull()?.attendance?.forEach {
                                Text(
                                    it.key.dayOfMonth.toString()
                                        .let { "${if (it.length == 1) "0" else ""}$it" } +
                                            "/" +
                                            it.key.monthNumber.toString()
                                                .let { "${if (it.length == 1) "0" else ""}$it" },
                                    Modifier.weight(cellWeight),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Divider(Modifier.padding(top = 2.dp), Color.Black, thickness = 1.dp)
                    }
                }
                itemsIndexed(groupAttendance.attendance) { index: Int, studentAttendance: LessonAttendance.GroupAttendance.StudentAttendance ->
                    val selected = index == selectedRow
                    Column {
                        Divider(
                            Modifier.padding(bottom = 2.dp),
                            Color.Black,
                            thickness = if (selected) 3.dp else 1.dp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            var height by remember { mutableStateOf(Dp.Hairline) }
                            val density = LocalDensity.current
                            Text(
                                studentAttendance.student.fullName,
                                modifier = Modifier
                                    .weight(fullnameWeight)
                                    .onGloballyPositioned {
                                        height = with(density) { it.size.height.toDp() }
                                    }.clickable { selectedRow = if (selected) null else index },
                                fontSize = 18.sp
                            )
                            studentAttendance.attendance.forEach { status ->
                                Box(
                                    Modifier.weight(cellWeight).height(height + 4.dp)
                                        .clickable { onClick(status.key to studentAttendance.student) }
                                        .let {
                                            if (selectedCells.contains(status.key to studentAttendance.student))
                                                it.background(Color.Black)
                                            else
                                                it
                                        }
                                ) {
                                    Box(
                                        Modifier.padding(3.dp).fillMaxSize().clip(RoundedCornerShape(4.dp))
                                            .background(status.value.color)
                                    )
                                }
                            }
                        }
                        Divider(Modifier.padding(top = 2.dp), Color.Black, thickness = if (selected) 3.dp else 1.dp)
                    }
                }
            }
        }
        VerticalScrollbar(
            rememberScrollbarAdapter(state),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )
    }
}