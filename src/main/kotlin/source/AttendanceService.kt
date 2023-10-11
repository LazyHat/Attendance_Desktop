package source

import kotlinx.coroutines.flow.Flow
import ru.lazyhat.models.LessonAttendance

interface AttendanceService {
    fun observeLessonAttendance(lessonId: String): Flow<LessonAttendance>
}

//class AttendanceServiceImpl(
//    private val client : HttpClient
//): AttendanceService{
//
//    private var socket : WebSocketSession? = null
//    private var attendance = emptyFlow<LessonAttendance>()
//    override fun observeLessonAttendance(lessonId: String): Flow<LessonAttendance> = try {
//        socket?.incoming?.receiveAsFlow()?.filter { it is Frame.Text }?.collect {
//
//        }
//    }
//}