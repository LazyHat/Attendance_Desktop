package repo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.lazyhat.models.*
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
    suspend fun getLessonAttendance(lessonId: UInt): Flow<LessonAttendance>
    suspend fun updateAttendance(update: RegistryRecordUpdate): Boolean
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
    override suspend fun createLesson(lesson: LessonCreate): Boolean = networkSource.createLesson(userToken, lesson)
    override suspend fun getStudentsByLesson(lessonId: UInt): Map<String, Set<Student>> =
        networkSource.getStudentsWithLesson(lessonId, userToken)

    override suspend fun createToken(lessonId: UInt): LessonToken? = networkSource.createToken(lessonId, userToken)
    override suspend fun getLessonAttendance(lessonId: UInt): LessonAttendance? =
        networkSource.getLessonAttendance(lessonId, userToken)

    override suspend fun updateAttendance(update: RegistryRecordUpdate): Boolean =
        networkSource.upsertListAttendance(
            update,
            userToken
        )
}
