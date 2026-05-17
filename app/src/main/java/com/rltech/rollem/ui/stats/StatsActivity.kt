package com.rltech.rollem.ui.stats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rltech.rollem.game.data.GameData
import com.rltech.rollem.game.data.GameDataHelper
import com.rltech.rollem.game.data.RollRecord
import com.rltech.rollem.ui.theme.RollEmTheme
import kotlin.math.roundToInt

class StatsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RollEmTheme {
                val previousGame = remember { GameDataHelper.getSavedGameData(this) }
                val currentGame = remember { GameDataHelper.getCurrentGameDataSnapshot() }

                StatsScreen(
                    previousGame = previousGame,
                    currentGame = currentGame,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun StatsScreen(
    previousGame: GameData,
    currentGame: GameData,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF202020))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Stats", color = Color.White, fontWeight = FontWeight.Bold)
            Button(onClick = onBack) { Text("Back") }
        }

        Spacer(modifier = Modifier.height(10.dp))
        GameSection(title = "Current Game", game = currentGame)
        Spacer(modifier = Modifier.height(16.dp))
        GameSection(title = "Previous Game", game = previousGame)
    }
}

@Composable
private fun GameSection(title: String, game: GameData) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow(game)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF5A5A5A))
            Spacer(modifier = Modifier.height(8.dp))
            StatsTable(records = game.rollRecords)
        }
    }
}

@Composable
private fun SummaryRow(game: GameData) {
    val rollValues = game.rollRecords.map { it.rollValue }
    val streaks = game.rollRecords.map { it.streakSize }
    val oddEvenPredictions = game.rollRecords.map { it.oddEvenPrediction }
    val valuePredictions = game.rollRecords.map { it.valuePrediction }

    val avgRoll = GameDataHelper.avgRoll(game.totalRolls, rollValues)
    val highestStreak = GameDataHelper.highestStreak(streaks)
    val oddEvenCorrect = GameDataHelper.totalCorrectOddEvenGuesses(oddEvenPredictions, rollValues)
    val nextValueCorrect = GameDataHelper.totalCorrectNextGuesses(valuePredictions, rollValues)

    val total = rollValues.size.coerceAtLeast(1)
    val oddEvenPct = ((oddEvenCorrect * 100.0) / total).roundToInt()
    val nextValuePct = ((nextValueCorrect * 100.0) / total).roundToInt()

    Column {
        Text("Final Score: ${game.finalScore}", color = Color(0xFF90EE90))
        Text("Total Rolls: ${game.totalRolls}", color = Color.White)
        Text("Avg Roll: ${"%.2f".format(avgRoll)}", color = Color.White)
        Text("Highest Streak: $highestStreak", color = Color.White)
        Text("Odd/Even Accuracy: $oddEvenCorrect/$total ($oddEvenPct%)", color = Color.White)
        Text("Value Guess Accuracy: $nextValueCorrect/$total ($nextValuePct%)", color = Color.White)
    }
}

@Composable
private fun StatsTable(records: List<RollRecord>) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
    ) {
        TableHeader()
        records.forEach { record ->
            TableRow(record)
        }

        if (records.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF666666))
                    .padding(10.dp)
            ) {
                Text("No rolls yet", color = Color.LightGray)
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF777777))
            .background(Color(0xFF3A3A3A))
            .padding(vertical = 6.dp)
    ) {
        HeaderCell("Roll #", 80.dp)
        HeaderCell("Value", 80.dp)
        HeaderCell("Streak", 80.dp)
        HeaderCell("Odd/Even Guess", 140.dp)
        HeaderCell("Next Guess", 120.dp)
    }
}

@Composable
private fun TableRow(record: RollRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF555555))
            .padding(vertical = 6.dp)
    ) {
        Cell(record.rollNumber.toString(), 80.dp)
        Cell(record.rollValue.toString(), 80.dp)
        Cell(record.streakSize.toString(), 80.dp)
        Cell(record.oddEvenPrediction, 140.dp)
        Cell(record.valuePrediction.toString(), 120.dp)
    }
}

@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.width(width).padding(horizontal = 8.dp)
    )
}

@Composable
private fun Cell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        color = Color.White,
        modifier = Modifier.width(width).padding(horizontal = 8.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF202020)
@Composable
fun StatsActivityPreview() {
    val sampleRecords = listOf(
        RollRecord(1, 4, 0, "n/a", 2),
        RollRecord(2, 6, 1, "EVEN", 6),
        RollRecord(3, 6, 2, "ODD", 3),
        RollRecord(4, 3, 0, "ODD", 3),
        RollRecord(5, 5, 1, "ODD", 5),
    )

    val previousGame = GameData(
        finalScore = 45L,
        totalRolls = 5,
        rollRecords = sampleRecords.toMutableList()
    )

    val currentGame = GameData(
        finalScore = 32L,
        totalRolls = 3,
        rollRecords = listOf(
            RollRecord(1, 2, 0, "EVEN", 1),
            RollRecord(2, 5, 1, "ODD", 5),
            RollRecord(3, 1, 0, "n/a", 0),
        ).toMutableList()
    )

    RollEmTheme {
        StatsScreen(
            previousGame = previousGame,
            currentGame = currentGame,
            onBack = {}
        )
    }
}
