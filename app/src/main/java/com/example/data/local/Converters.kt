package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.GoalType
import com.example.data.model.Priority
import com.example.data.model.TaskStatus

class Converters {
    @TypeConverter
    fun fromGoalType(value: GoalType): String = value.name

    @TypeConverter
    fun toGoalType(value: String): GoalType = try {
        GoalType.valueOf(value)
    } catch (_: Exception) {
        GoalType.MILESTONE_BASED
    }

    @TypeConverter
    fun fromPriority(value: Priority): String = value.name

    @TypeConverter
    fun toPriority(value: String): Priority = try {
        Priority.valueOf(value)
    } catch (_: Exception) {
        Priority.MEDIUM
    }

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = try {
        TaskStatus.valueOf(value)
    } catch (_: Exception) {
        TaskStatus.PENDING
    }
}
