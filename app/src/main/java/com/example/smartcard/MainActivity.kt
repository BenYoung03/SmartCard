package com.example.smartcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcard.ui.theme.SmartCardTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val decks = mutableStateListOf(
            FlashDeck("Programming Languages", "This is the flash deck for my programming languages class"),
            FlashDeck("Data Structures", "This is the flash deck for my data structures class"),
        )

        setContent {
            SmartCardTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(decks, navController)
                    }
                    composable("flashcards/{deckName}") { backStackEntry ->
                        val deckName = backStackEntry.arguments?.getString("deckName") ?: ""
                        FlashcardScreen(deckName)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(decks: SnapshotStateList<FlashDeck>, navController: NavHostController) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NewDeck(
                modifier = Modifier.padding(top = 25.dp),
                onAddDeck = { name, description ->
                    decks.add(FlashDeck(name, description))
                }
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(decks) { deck ->
                    DeckView(deck) {
                        navController.navigate("flashcards/${deck.name}")
                    }
                }
            }
        }
    }
}

@Composable
fun NewDeck(
    modifier: Modifier = Modifier,
    onAddDeck: (String, String) -> Unit
) {
    var inputDeck by remember { mutableStateOf(false) }
    var deckDescription by remember { mutableStateOf("") }
    var deckName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { inputDeck = true }) {
            Text(text = "Add new deck")
        }

        if(inputDeck){
            Spacer(modifier = Modifier.padding(8.dp))

            Text(
                text = "Deck Name",
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.padding(8.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = deckName,
                onValueChange = { deckName = it },
                placeholder = { Text(text = "e.g. Data Structures") },
            )

            Text(
                text = "Deck Description",
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.padding(8.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = deckDescription,
                onValueChange = { deckDescription = it },
                placeholder = { Text(text = "e.g. This is what the deck is about") },
            )

            Button(onClick = {
                onAddDeck(deckName, deckDescription)
                inputDeck = false
                deckName = ""
                deckDescription = ""
            }) {
                Text(text = "Confirm")
            }
        }
    }
}

@Composable
fun DeckView(deck: FlashDeck, onDeckClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onDeckClick() }
    ) {
        Column {
            Text(
                text = deck.name,
                modifier = Modifier.padding(12.dp),
                fontSize = 20.sp
            )
            Text(
                text = deck.description,
                modifier = Modifier.padding(12.dp),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun FlashcardScreen(deckName: String) {
    // This composable represents the screen where you can add flashcards to a deck.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(
            text = "Flashcards for $deckName",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp)
                .border(width = 4.dp, color = Gray, shape = RoundedCornerShape(16.dp))
                .background(color = DarkGray, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ){ Text(
            text = "Question 1 : What type of language is Kotlin?",
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center,
            fontSize = 25.sp,
            color = Color.White
            )
        }
    }
}



