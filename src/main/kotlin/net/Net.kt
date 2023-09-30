package net

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

val authority = "http://lazyhat.ru"

object Net {
    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getLessonInfo(lessonId: Int): Lesson = client.get("$authority/lessons/$lessonId").body()

    suspend fun getStudentsOnLesson(lessonId: Int): List<Student> = client.get("$authority/lessons/$lessonId/students").body()

    suspend fun getLessonToken(lessonId: Int): Token =
        client.get("$authority/lessons/$lessonId/token").body()
}

@Serializable
data class Token(
    val id: String,
    val lessonId: Int,
    val expires: LocalDateTime
)

@Serializable
data class Student(
    val fullName: String
)

@Serializable
data class Lesson(
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime
)