package models

import kotlinx.datetime.*
import ru.lazyhat.models.LessonCreate
import ru.lazyhat.models.LessonUpdate
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

operator fun LocalTime.plus(duration: Duration): LocalTime =
    (this.toMillisecondOfDay() + duration.inWholeMilliseconds).let {
        LocalTime.fromMillisecondOfDay(it.toInt())
    }

operator fun LocalDateTime.minus(other: LocalDateTime): Duration =
    this.toInstant(TimeZone.currentSystemDefault()) - other.toInstant(TimeZone.currentSystemDefault())

fun LessonCreate.toLessonUpdate(username: String) = LessonUpdate(
    username, title, dayOfWeek, start, duration, groupsList
)

fun LocalDateTime.Companion.now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun Duration.roundTo(unit: DurationUnit) = this.toLong(unit).toDuration(unit)