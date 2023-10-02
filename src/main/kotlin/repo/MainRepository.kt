package repo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import models.Lesson
import models.LessonCreate
import models.LessonToken
import models.Student
import source.NetworkSource
import kotlin.time.Duration.Companion.milliseconds

interface MainRepository {
    suspend fun isLoggedIn(): Boolean
    suspend fun logIn(username: String, password: String): Boolean
    suspend fun getLessons(): List<Lesson>
    suspend fun getLessonById(id: UInt): Lesson?
    suspend fun createLesson(lesson: LessonCreate): Boolean
    suspend fun getStudentsByLesson(lessonId: UInt): Map<String, Set<Student>>
    suspend fun createToken(lessonId: UInt): LessonToken?
}

data class Credentials(val username: String, val password: String)

class MainRepositoryImpl(private val networkSource: NetworkSource) : MainRepository {
    val scope = CoroutineScope(Dispatchers.IO)
    lateinit var userToken: String
    var savedCredentials: Credentials? = Credentials("lazy", "pass")

    init {
        scope.launch {
            isLoggedIn()
        }
    }

    override suspend fun isLoggedIn(): Boolean =
        savedCredentials?.let {
            logIn(it.username, it.password)
        } ?: false

    override suspend fun logIn(username: String, password: String): Boolean =
        networkSource.logIn(username, password)?.let {
            userToken = it
            true
        } ?: false

    override suspend fun getLessons(): List<Lesson> {
        while (!this::userToken.isInitialized) {
            delay(500.milliseconds)
        }
        return networkSource.getLessonsByToken(userToken)
    }

    override suspend fun getLessonById(id: UInt): Lesson? = networkSource.getLessonById(id, userToken)

    override suspend fun createLesson(lesson: LessonCreate): Boolean = networkSource.getTokenInfo(userToken).let {
        networkSource.createLesson(userToken, lesson)
    }

    override suspend fun getStudentsByLesson(lessonId: UInt): Map<String, Set<Student>> =
        networkSource.getStudentsWithLesson(lessonId, userToken)

    override suspend fun createToken(lessonId: UInt): LessonToken? = networkSource.createToken(lessonId, userToken)
}
