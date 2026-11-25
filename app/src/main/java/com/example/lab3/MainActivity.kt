package com.example.lab3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MaterialTheme { MainApp() } }
    }
}

enum class AppDestinations(val label: String) {
    HOME("Головна"),
    PROFILE("Профіль"),
}

// --- Пункт 2: Моделі даних, Репозиторій та ViewModel ---

sealed interface FeedItem {
    val id: Int
}

data class NoteItem(
    override val id: Int,
    val title: String,
    val description: String
) : FeedItem

data class TaskItem(
    override val id: Int,
    val taskName: String,
    val isDone: Boolean
) : FeedItem

// Модель для пункту 9 (горизонтальний список у вертикальному)
data class HorizontalRowItem(
    override val id: Int,
    val sectionTitle: String,
    val tags: List<String>
) : FeedItem

class FeedRepository {
    fun getItems(): List<FeedItem> = listOf(
        NoteItem(1, "Лабораторна №1", "Налаштовано базову навігацію та стани."),
        TaskItem(2, "Додати NavHost", isDone = true),
        HorizontalRowItem(
            id = 3,
            sectionTitle = "Використані технології (гортай вбік)",
            tags = listOf("Kotlin", "Jetpack Compose", "Material 3", "NavHost", "ViewModel", "LazyColumn")
        ),
        TaskItem(4, "Реалізувати LazyColumn", isDone = true),
        NoteItem(5, "Порада", "Modifier.weight(1f) змушує список зайняти увесь вільний простір."),
        TaskItem(6, "Завантажити проєкт на GitHub", isDone = false),
        NoteItem(7, "Тестовий запис", "Додатковий елемент для перевірки вертикальної прокрутки.")
    )
}

class FeedViewModel : ViewModel() {
    private val repository = FeedRepository()
    val items: List<FeedItem> = repository.getItems()
}

// --- Навігація застосунку ---

@Composable
fun MainApp() {
    var current by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {},
                    label = { Text(it.label) },
                    selected = it == current,
                    onClick = { current = it }
                )
            }
        }
    ) {
        Scaffold { padding ->
            val mod = Modifier.fillMaxSize().padding(padding)
            when (current) {
                AppDestinations.HOME -> HomeScreen(mod)
                AppDestinations.PROFILE -> ProfileScreen(mod)
            }
        }
    }
}

// Екран 1 (з попередньої лабораторної без змін)
@Composable
fun HomeScreen(modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home_main",
        modifier = modifier
    ) {
        composable("home_main") {
            HomeMainSubscreen(onOpenDetails = { navController.navigate("home_details") })
        }
        composable("home_details") {
            HomeDetailsSubscreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun HomeMainSubscreen(onOpenDetails: () -> Unit) {
    var count by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Натискань: $count", fontSize = 26.sp, color = Color(0xFF0D47A1))
        Spacer(Modifier.height(16.dp))
        Button(onClick = { count++ }) { Text("Додати +1") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenDetails) { Text("Перейти на другий підекран") }
    }
}

@Composable
fun HomeDetailsSubscreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Другий підекран", fontSize = 24.sp, color = Color(0xFFE65100))
        Spacer(Modifier.height(8.dp))
        Text("Повернись кнопкою або системним жестом «Назад».", fontSize = 15.sp, color = Color(0xFFBF360C))
        Spacer(Modifier.height(20.dp))
        Button(onClick = onBack) { Text("Назад") }
    }
}

// --- Пункти 1, 3, 4, 9: Екран 2 зі списком на увесь вільний простір ---

@Composable
fun ProfileScreen(
    modifier: Modifier,
    feedViewModel: FeedViewModel = viewModel()
) {
    var status by rememberSaveable { mutableStateOf("Статус: очікування") }

    Column(
        modifier = modifier
            .background(Color(0xFFE8F5E9))
            .padding(16.dp)
    ) {
        // Пункт 1: weight(1f) віддає списку весь вільний простір над нижньою панеллю
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Пункт 4: зв'язування розміток відповідно до типу даних
            items(feedViewModel.items, key = { it.id }) { item ->
                when (item) {
                    is NoteItem -> NoteItemCard(item)
                    is TaskItem -> TaskItemCard(item)
                    is HorizontalRowItem -> HorizontalSectionCard(item)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Елементи з попередніх лабораторних робіт
        Text(
            text = status,
            fontSize = 18.sp,
            color = Color(0xFF1B5E20),
            modifier = Modifier.align(Alignment.End)
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { status = "Статус: оновлено!" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Оновити")
        }
    }
}

// Пункт 3: Розмітка для NoteItem
@Composable
fun NoteItemCard(item: NoteItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(item.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
            Spacer(Modifier.height(4.dp))
            Text(item.description, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}

// Пункт 3: Розмітка для TaskItem
@Composable
fun TaskItemCard(item: TaskItem) {
    val bgColor = if (item.isDone) Color(0xFFC8E6C9) else Color(0xFFFFECB3)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.taskName, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                text = if (item.isDone) "Виконано" else "В процесі",
                fontSize = 13.sp,
                color = if (item.isDone) Color(0xFF2E7D32) else Color(0xFFE65100)
            )
        }
    }
}

// Пункт 9: Горизонтальний список (LazyRow) всередині вертикального (LazyColumn)
@Composable
fun HorizontalSectionCard(item: HorizontalRowItem) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = item.sectionTitle,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(item.tags) { tag ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D47A1))) {
                    Text(
                        text = tag,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}