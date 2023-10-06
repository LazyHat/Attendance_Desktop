package ru.lazyhat.models

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


data class RegistryRecord(
    val id: ULong,
    val lessonId: UInt,
    val student: String,
    val createdAt: LocalDateTime
)

data class RegistryRecordCreate(
    val lessonId: UInt,
    val student: String,
    val createdAt: LocalDateTime
)

@Serializable
enum class AttendanceStatus(val color: Color) {
    Attended(Color(0xFF00C33C)),
    Missing(Color.Red)
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