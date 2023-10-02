package source

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import models.*

private val authority = "http://localhost:8080"
//private val authority = "http://lazyhat.ru"

interface NetworkSource {
    suspend fun logIn(username: String, password: String): String?
    suspend fun validateUserToken(token: String): Boolean
    suspend fun getLessonsByToken(token: String): List<Lesson>
    suspend fun getTokenInfo(token: String): UserToken
    suspend fun createLesson(token: String, lesson: LessonCreate): Boolean
    suspend fun getLessonById(id: UInt, token: String): Lesson?
    suspend fun getStudentsWithLesson(lessonId: UInt, token: String): Map<String, Set<Student>>
    suspend fun createToken(lessonId: UInt, token: String): LessonToken?
}

class NetworkSourceImpl(private val client: HttpClient) : NetworkSource {
    override suspend fun logIn(username: String, password: String): String? =
        client.get("$authority/teacher/login?username=$username&password=$password").takeIf {
            it.status == HttpStatusCode.OK
        }?.bodyAsText()

    override suspend fun validateUserToken(token: String): Boolean = client.get("$authority/user/token-info").let {
        when (it.status) {
            HttpStatusCode.OK -> true
            else -> false
        }
    }

    override suspend fun getLessonsByToken(token: String): List<Lesson> = client.get("$authority/teacher/lessons") {
        headers["Authorization"] = "Bearer $token"
    }.body()

    override suspend fun getTokenInfo(token: String): UserToken = client.get("$authority/token-info") {
        headers["Authorization"] = "Bearer $token"
    }.body()

    override suspend fun createLesson(token: String, lesson: LessonCreate): Boolean =
        client.post("$authority/teacher/lessons") {
            headers["Authorization"] = "Bearer $token"
            contentType(ContentType.Application.Json)
            setBody(lesson)
        }.status == HttpStatusCode.OK

    override suspend fun getLessonById(id: UInt, token: String): Lesson? =
        client.get("$authority/teacher/lessons/$id") {
            headers["Authorization"] = "Bearer $token"
        }.let {
            if (it.status == HttpStatusCode.OK) it.body()
            else null
        }

    override suspend fun getStudentsWithLesson(lessonId: UInt, token: String): Map<String, Set<Student>> =
        client.get("$authority/teacher/students?lesson=$lessonId") {
            headers["Authorization"] = "Bearer $token"
        }.let {
            if (it.status == HttpStatusCode.OK) it.body()
            else mapOf()
        }

    override suspend fun createToken(lessonId: UInt, token: String): LessonToken? =
        client.get("$authority/teacher/lessons/$lessonId/token") {
            headers["Authorization"] = "Bearer $token"
        }.let {
            if (it.status == HttpStatusCode.OK)
                it.body()
            else null
        }
}