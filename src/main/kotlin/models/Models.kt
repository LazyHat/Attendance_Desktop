package models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

data class Group(
    val id: String,
    val lessonsList: Set<UInt>
)

@Serializable
data class Lesson(
    val id: UInt,
    val username: String,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val groupsList: Set<String>
)

@Serializable
data class LessonUpdate(
    val username: String,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val groupsList: Set<String>
)

@Serializable
data class LessonCreate(
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val groupsList: Set<String>
)

@Serializable
data class UserToken(
    val username: String,
    val access: Access,
    val expiresAt: LocalDateTime
)

@Serializable
data class LessonToken(
    val id: String,
    val lessonId: UInt,
    val expires: LocalDateTime
)

@Serializable
enum class Access {
    Student,
    Teacher
}

@Serializable
enum class Status {
    Idle,
    InLesson
}

@Serializable
data class StudentCreate(
    val username: String,
    val fullName: String,
    val password: String,
    val groupId: String
)

@Serializable
data class Student(
    val username: String,
    val fullName: String,
    val password: String,
    val status: Status,
    val groupId: String
)

@Serializable
data class Teacher(
    val username: String,
    val fullName: String,
    val password: String
)