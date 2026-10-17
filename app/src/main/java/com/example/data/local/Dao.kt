package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE isArchived = 0 ORDER BY targetEpochMillis ASC")
    fun getAllEvents(): Flow<List<EventItem>>

    @Query("SELECT * FROM events WHERE isDefaultNewYear = 1 LIMIT 1")
    suspend fun getDefaultNewYearEvent(): EventItem?

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): EventItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventItem)

    @Update
    suspend fun updateEvent(event: EventItem)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEvent(id: String)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY priority DESC, targetEpochMillis ASC")
    fun getAllGoals(): Flow<List<GoalItem>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: String): GoalItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalItem)

    @Update
    suspend fun updateGoal(goal: GoalItem)

    @Query("UPDATE goals SET currentValue = :newValue, isCompleted = :isCompleted, completedAtEpochMillis = :completedAt WHERE id = :goalId")
    suspend fun updateGoalValue(goalId: String, newValue: Double, isCompleted: Boolean, completedAt: Long?)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: String)
}

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones WHERE goalId = :goalId ORDER BY dueEpochMillis ASC")
    fun getMilestonesForGoal(goalId: String): Flow<List<MilestoneItem>>

    @Query("SELECT * FROM milestones ORDER BY dueEpochMillis ASC")
    fun getAllMilestones(): Flow<List<MilestoneItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneItem)

    @Update
    suspend fun updateMilestone(milestone: MilestoneItem)

    @Query("DELETE FROM milestones WHERE id = :id")
    suspend fun deleteMilestone(id: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE scheduledDate = :date ORDER BY status ASC, priority DESC")
    fun getTasksForDate(date: String): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks ORDER BY scheduledDate DESC, priority DESC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE scheduledDate = :date AND status != 'COMPLETED' LIMIT 3")
    suspend fun getTopIncompleteTasksSync(date: String): List<TaskItem>

    @Query("SELECT * FROM tasks WHERE scheduledDate = :date")
    suspend fun getTasksForDateSync(date: String): List<TaskItem>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY currentStreak DESC")
    fun getAllHabits(): Flow<List<HabitItem>>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsSync(): List<HabitItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitItem)

    @Update
    suspend fun updateHabit(habit: HabitItem)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: String)
}

@Dao
interface AIPlanDao {
    @Query("SELECT * FROM ai_plans WHERE planDate = :date LIMIT 1")
    fun getPlanForDate(date: String): Flow<AIPlanItem?>

    @Query("SELECT * FROM ai_plans WHERE planDate = :date LIMIT 1")
    suspend fun getPlanForDateSync(date: String): AIPlanItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: AIPlanItem)
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM learning_resources ORDER BY isBookmarked DESC, id ASC")
    fun getAllResources(): Flow<List<LearningResource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(resources: List<LearningResource>)

    @Update
    suspend fun updateResource(resource: LearningResource)
}
