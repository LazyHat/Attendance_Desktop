package ru.lazyhat.models

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable


@Serializable
data class RegistryRecordUpdate(
    val lessonId: UInt,
    val recordsToUpdate: List<Parameters>,
    val newStatus: AttendanceStatus
) {
    @Serializable
    data class Parameters(
        val student: String,
        val date: LocalDate
    )
}

@Serializable
enum class AttendanceStatus(val color: Color) {
    Attended(Color(0xFF00C33C)),
    Missing(Color.Red),
    ValidReason(Color.Gray),
    Disease(Color.Blue)
}

@Serializable
data class LessonAttendance(
    val lessonId: UInt,
    val groups: List<GroupAttendance>
) {
    @Serializable
    data class GroupAttendance(
        val group: String,
        val attendance: List<StudentAttendance>
    ) {
        @Serializable
        data class StudentAttendance(
            val student: Student,
            val attendance: Map<LocalDate, AttendanceStatus>
        )
    }
}