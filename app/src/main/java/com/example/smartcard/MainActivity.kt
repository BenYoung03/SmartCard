package com.example.smartcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartcard.ui.theme.SmartCardTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val decks = listOf(
            FlashDeck("Programming Languages", "This is the flash deck for my programming languages class"),
            FlashDeck("Data Structures", "This is the flash deck for my data structures class"),
            FlashDeck("Algorithms", "This is the flash deck for my algorithms class"),
        )

        setContent {
            SmartCardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Use Column to stack the button and LazyColumn
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NewDeck(modifier = Modifier.padding(top = 50.dp))

                        // LazyColumn for displaying flash decks
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(decks) { deck ->
                                DeckView(deck)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewDeck(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth(), // Ensure the Column fills the width of the screen
        horizontalAlignment = Alignment.CenterHorizontally // Center-align the button horizontally
    ) {
        Button(onClick = { println("I've been clicked") }) {
            Text(text = "Add new deck")
        }
    }
}


@Composable
fun DeckView(deck: FlashDeck) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row {
            Text(
                text = deck.name,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun NewDeckPreview() {
    SmartCardTheme {
        NewDeck()
    }
}



