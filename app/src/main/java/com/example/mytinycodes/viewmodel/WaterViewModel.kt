package com.example.mytinycodes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytinycodes.data.WaterRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

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
            
            sharedPreferences.edit()
                .putInt("water_amount", newAmount)
                .apply()
            
            saveRecord(newAmount)
        }
    }

    fun removeWater(amount: Int) {
        viewModelScope.launch {
            val newAmount = (_waterAmount.value - amount).coerceAtLeast(0)
            _waterAmount.value = newAmount
            
            sharedPreferences.edit()
                .putInt("water_amount", newAmount)
                .apply()
            
            saveRecord(newAmount)
        }
    }

    fun resetWater() {
        viewModelScope.launch {
            _waterAmount.value = 0
            
            sharedPreferences.edit()
                .putInt("water_amount", 0)
                .apply()
            
            saveRecord(0)
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

    private fun saveRecord(amount: Int) {
        val today = LocalDate.now()
        val record = WaterRecord(today, amount)
        
        // Günlük kayıt
        sharedPreferences.edit()
            .putInt("water_amount_${today}", amount)
            .apply()
        
        updateRecords(record)
    }

    private fun loadRecords() {
        viewModelScope.launch {
            val today = LocalDate.now()
            
            // Haftalık kayıtlar
            val weekStart = today.minusDays(6)
            _weeklyRecords.value = (0..6).map { days ->
                val date = weekStart.plusDays(days.toLong())
                WaterRecord(date, getWaterAmountForDate(date))
            }
            
            // Aylık kayıtlar
            val monthStart = today.minusDays(29)
            _monthlyRecords.value = (0..29).map { days ->
                val date = monthStart.plusDays(days.toLong())
                WaterRecord(date, getWaterAmountForDate(date))
            }
            
            // Yıllık kayıtlar (aylık toplamlar)
            val yearStart = today.minusMonths(11)
            _yearlyRecords.value = (0..11).map { months ->
                val date = yearStart.plusMonths(months.toLong())
                WaterRecord(date, getWaterAmountForMonth(date))
            }
        }
    }

    private fun updateRecords(record: WaterRecord) {
        loadRecords() // Tüm kayıtları yenile
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