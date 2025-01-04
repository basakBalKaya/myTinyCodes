package com.example.mytinycodes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mytinycodes.viewmodel.WaterViewModel
import com.example.mytinycodes.data.WaterRecord
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun RecordsScreen(
    modifier: Modifier = Modifier,
    waterViewModel: WaterViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val weeklyRecords by waterViewModel.weeklyRecords.collectAsState()
    val monthlyRecords by waterViewModel.monthlyRecords.collectAsState()
    val yearlyRecords by waterViewModel.yearlyRecords.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Su İçme Kayıtları",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        TabRow(
            selectedTabIndex = selectedTab
        ) {
            listOf("Haftalık", "Aylık", "Yıllık").forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> WeeklyRecordsList(weeklyRecords)
            1 -> MonthlyRecordsList(monthlyRecords)
            2 -> YearlyRecordsList(yearlyRecords)
        }
    }
}
//didn't try yet but i guess its working
@Composable
private fun WeeklyRecordsList(records: List<WaterRecord>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(records.sortedByDescending { it.date }) { record ->
            RecordCard(record)
        }
    }
}

@Composable
private fun MonthlyRecordsList(records: List<WaterRecord>) {
    val groupedRecords = records
        .groupBy { it.date.dayOfMonth }
        .toSortedMap(compareByDescending { it })

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groupedRecords.entries.toList()) { (day, dayRecords) ->
            DailySummaryCard(
                day = day,
                records = dayRecords,
                total = dayRecords.sumOf { it.amount }
            )
        }
    }
}

@Composable
private fun YearlyRecordsList(records: List<WaterRecord>) {
    val groupedRecords = records
        .groupBy { it.date.month }
        .toSortedMap(compareByDescending { it })

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groupedRecords.entries.toList()) { (month, monthRecords) ->
            MonthlySummaryCard(
                month = month,
                records = monthRecords,
                total = monthRecords.sumOf { it.amount },
                average = monthRecords.sumOf { it.amount } / monthRecords.size
            )
        }
    }
}

@Composable
private fun RecordCard(record: WaterRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = record.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${record.amount}ml",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DailySummaryCard(day: Int, records: List<WaterRecord>, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "$day. Gün",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Toplam: ${total}ml",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    month: java.time.Month,
    records: List<WaterRecord>,
    total: Int,
    average: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = month.toString(),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Toplam: ${total}ml",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Günlük Ortalama: ${average}ml",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 