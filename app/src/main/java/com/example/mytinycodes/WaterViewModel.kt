package com.example.mytinycodes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

data class WaterRecord(
    val date: LocalDate,
    val amount: Int
)

class WaterViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("water_tracker", Application.MODE_PRIVATE)
    
    private val _waterAmount = MutableStateFlow(
        sharedPreferences.getInt("water_amount", 0)
    )
    val waterAmount: StateFlow<Int> = _waterAmount

    private val _dailyGoal = MutableStateFlow(
        sharedPreferences.getInt("daily_goal", 2000)
    )
    val dailyGoal: StateFlow<Int> = _dailyGoal

    private val _lastDrinkTime = MutableStateFlow<Date?>(
        sharedPreferences.getLong("last_drink_time", -1).let { if (it == -1L) null else Date(it) }
    )
    val lastDrinkTime: StateFlow<Date?> = _lastDrinkTime

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _weeklyRecords = MutableStateFlow<List<WaterRecord>>(emptyList())
    val weeklyRecords: StateFlow<List<WaterRecord>> = _weeklyRecords

    private val _monthlyRecords = MutableStateFlow<List<WaterRecord>>(emptyList())
    val monthlyRecords: StateFlow<List<WaterRecord>> = _monthlyRecords

    private val _yearlyRecords = MutableStateFlow<List<WaterRecord>>(emptyList())
    val yearlyRecords: StateFlow<List<WaterRecord>> = _yearlyRecords

    init {
        loadRecords()
    }

    fun addWater(amount: Int) {
        viewModelScope.launch {
            val newAmount = _waterAmount.value + amount
            _waterAmount.value = newAmount
            _lastDrinkTime.value = Date()
            
            sharedPreferences.edit()
                .putInt("water_amount", newAmount)
                .putLong("last_drink_time", Date().time)
                .apply()
            
            updateRecords()
        }
    }

    fun removeWater(amount: Int) {
        viewModelScope.launch {
            val newAmount = (_waterAmount.value - amount).coerceAtLeast(0)
            _waterAmount.value = newAmount
            
            sharedPreferences.edit()
                .putInt("water_amount", newAmount)
                .apply()
            
            updateRecords()
        }
    }

    fun resetWater() {
        viewModelScope.launch {
            _waterAmount.value = 0
            _lastDrinkTime.value = null
            
            sharedPreferences.edit()
                .putInt("water_amount", 0)
                .remove("last_drink_time")
                .apply()
            
            updateRecords()
        }
    }

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch {
            _dailyGoal.value = goal
            
            sharedPreferences.edit()
                .putInt("daily_goal", goal)
                .apply()
        }
    }

    fun setSelectedDate(date: LocalDate) {
        viewModelScope.launch {
            _selectedDate.value = date
            loadRecords()
        }
    }

    private fun loadRecords() {
        viewModelScope.launch {
            val today = LocalDate.now()
            
            // Weekly records
            val weekStart = today.minusDays(6)
            _weeklyRecords.value = (0..6).map { days ->
                val date = weekStart.plusDays(days.toLong())
                WaterRecord(date, getWaterAmountForDate(date))
            }
            
            // Monthly records
            val monthStart = today.minusDays(29)
            _monthlyRecords.value = (0..29).map { days ->
                val date = monthStart.plusDays(days.toLong())
                WaterRecord(date, getWaterAmountForDate(date))
            }
            
            // Yearly records
            val yearStart = today.minusMonths(11)
            _yearlyRecords.value = (0..11).map { months ->
                val date = yearStart.plusMonths(months.toLong())
                WaterRecord(date, getWaterAmountForMonth(date))
            }
        }
    }

    private fun updateRecords() {
        viewModelScope.launch {
            val today = LocalDate.now()
            sharedPreferences.edit()
                .putInt("water_amount_${today}", _waterAmount.value)
                .apply()
            loadRecords()
        }
    }

    private fun getWaterAmountForDate(date: LocalDate): Int {
        return sharedPreferences.getInt("water_amount_$date", 0)
    }

    private fun getWaterAmountForMonth(date: LocalDate): Int {
        var total = 0
        val daysInMonth = date.lengthOfMonth()
        for (day in 1..daysInMonth) {
            val currentDate = date.withDayOfMonth(day)
            total += getWaterAmountForDate(currentDate)
        }
        return total
    }
} 
} 