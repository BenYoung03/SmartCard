package com.example.smartcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


enum class FlashCardsScreen(){
    Start,
    Cards,
    Quiz,
    Profile
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp(){

    val decks = remember {
        mutableStateListOf(
            FlashDeck(
                "Programming Languages",
                "This is the flash deck for my programming languages class"
            ),
            FlashDeck("Data Structures", "This is the flash deck for my data structures class"),
            FlashDeck("Algorithms", "This is the flash deck for my algorithms class"),
        )
    }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

        // Use Column to stack the button and LazyColumn
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

            // LazyColumn for displaying flash decks
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(decks) { deck ->
                    DeckView(deck)
                }
            }
        }

    }

}



//Easier way to visualize UI elements of the app through the DESIGN tab
@Preview(showBackground = true, name = "New Deck Preview")
@Composable
fun NewDeckPreview() {
    NewDeck(
        onAddDeck = { name, description ->
            // Sample placeholder action for preview
            println("New deck added with name: $name and description: $description")
        }
    )
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
            .fillMaxWidth(), // Ensure the Column fills the width of the screen
        horizontalAlignment = Alignment.CenterHorizontally // Center-align the button horizontally
    ) {
        Button(onClick = { inputDeck = true /*TODO*/ }) {
            Text(text = "Add new deck")
        }

        if(inputDeck){

            Spacer(modifier = Modifier.padding(8.dp))

            Text(
                text = "Deck Name",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displaySmall
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
                style = MaterialTheme.typography.displaySmall
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
                deckName = "" // Clear deck name
                deckDescription = "" // Clear deck description
            }) {
                Text(text = "Confirm")
            }
        }
    }


}

//Easier way to visualize UI elements of the app through the DESIGN tab
@Preview(showBackground = true, name = "Deck View Preview")
@Composable
fun DeckViewPreview(){
    val sampleDeck = FlashDeck(
        name = "Sample Deck",
        description = "This is the description of the sample deck"

    )

    DeckView(deck = sampleDeck)

}

@Composable
fun DeckView(deck: FlashDeck) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
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





