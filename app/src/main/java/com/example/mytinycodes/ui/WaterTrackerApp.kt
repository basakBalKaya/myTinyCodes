package com.example.mytinycodes.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mytinycodes.R
import com.example.mytinycodes.data.WaterRecord
import com.example.mytinycodes.viewmodel.WaterViewModel
import com.google.accompanist.pager.*
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun WaterTrackerApp(viewModel: WaterViewModel = viewModel()) {
    val waterAmount by viewModel.waterAmount.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val lastDrinkTime by viewModel.lastDrinkTime.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val weeklyRecords by viewModel.weeklyRecords.collectAsState()
    val monthlyRecords by viewModel.monthlyRecords.collectAsState()
    val yearlyRecords by viewModel.yearlyRecords.collectAsState()
    
    var showGoalDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                val yesterday = LocalDate.now().minusDays(1)
                viewModel.setSelectedDate(yesterday)
                isRefreshing = false
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üst başlık ve ayarlar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Su İçme Takibi",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { showGoalDialog = true }) {
                Icon(Icons.Default.Settings, "Ayarlar")
            }
        }

        // Tab başlıkları
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            }
        ) {
            listOf("Günlük", "Haftalık", "Aylık", "Yıllık").forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title) }
                )
            }
        }

        // Sayfa içeriği
        HorizontalPager(
            count = 4,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> Box(Modifier.pullRefresh(pullRefreshState)) {
                    DailyView(
                        waterAmount = waterAmount,
                        dailyGoal = dailyGoal,
                        lastDrinkTime = lastDrinkTime,
                        selectedDate = selectedDate,
                        onAddWater = viewModel::addWater,
                        onRemoveWater = viewModel::removeWater,
                        onReset = { showResetDialog = true }
                    )
                    PullRefreshIndicator(
                        refreshing = isRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
                1 -> WeeklyView(weeklyRecords)
                2 -> MonthlyView(monthlyRecords)
                3 -> YearlyView(yearlyRecords)
            }
        }
    }

    // Diyaloglar
    if (showGoalDialog) {
        GoalDialog(
            currentGoal = dailyGoal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { goal ->
                viewModel.setDailyGoal(goal)
                showGoalDialog = false
            }
        )
    }

    if (showResetDialog) {
        ResetDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                viewModel.resetWater()
                showResetDialog = false
            }
        )
    }
}

@Composable
fun DailyView(
    waterAmount: Int,
    dailyGoal: Int,
    lastDrinkTime: Date?,
    selectedDate: LocalDate,
    onAddWater: (Int) -> Unit,
    onRemoveWater: (Int) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Su miktarı kartı
        WaterAmountCard(
            waterAmount = waterAmount,
            dailyGoal = dailyGoal,
            lastDrinkTime = lastDrinkTime,
            selectedDate = selectedDate
        )

        // Su ekleme/çıkarma butonları
        WaterControls(
            onAddWater = onAddWater,
            onRemoveWater = onRemoveWater,
            onReset = onReset
        )
    }
}

@Composable
fun WeeklyView(records: List<WaterRecord>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        records.forEach { record ->
            WaterRecordItem(
                date = record.date.format(DateTimeFormatter.ofPattern("dd MMMM")),
                amount = record.amount
            )
        }
    }
}

@Composable
fun MonthlyView(records: List<WaterRecord>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        records.forEach { record ->
            WaterRecordItem(
                date = record.date.format(DateTimeFormatter.ofPattern("dd MMMM")),
                amount = record.amount
            )
        }
    }
}

@Composable
fun YearlyView(records: List<WaterRecord>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        records.forEach { record ->
            WaterRecordItem(
                date = record.date.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                amount = record.amount
            )
        }
    }
}

@Composable
fun WaterRecordItem(date: String, amount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(date)
            Text("${amount}ml")
        }
    }
}

@Composable
fun WaterAmountCard(
    waterAmount: Int,
    dailyGoal: Int,
    lastDrinkTime: Date?,
    selectedDate: LocalDate
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "${waterAmount}ml / ${dailyGoal}ml",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "İçilen su miktarı",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            lastDrinkTime?.let { time ->
                Text(
                    text = "Son içilen: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(time)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
            LinearProgressIndicator(
                progress = (waterAmount.toFloat() / dailyGoal).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun WaterControls(
    onAddWater: (Int) -> Unit,
    onRemoveWater: (Int) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { onRemoveWater(200) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Remove, "Azalt")
                    Spacer(Modifier.width(4.dp))
                    Text("200ml")
                }
            }
            
            Button(
                onClick = { onAddWater(200) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, "Ekle")
                    Spacer(Modifier.width(4.dp))
                    Text("200ml")
                }
            }
        }
        
        Button(
            onClick = { onAddWater(500) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, "Ekle")
                Spacer(Modifier.width(4.dp))
                Text("500ml")
            }
        }
        
        TextButton(
            onClick = onReset,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Sıfırla")
        }
    }
}

@Composable
fun GoalDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var goalInput by remember { mutableStateOf(currentGoal.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Günlük Hedef Belirle") },
        text = {
            OutlinedTextField(
                value = goalInput,
                onValueChange = { goalInput = it.filter { char -> char.isDigit() } },
                label = { Text("Hedef (ml)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                goalInput.toIntOrNull()?.let { onConfirm(it) }
            }) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun ResetDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sıfırla") },
        text = { Text("Bugünkü su içme verilerinizi sıfırlamak istediğinize emin misiniz?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Evet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hayır")
            }
        }
    )
} 