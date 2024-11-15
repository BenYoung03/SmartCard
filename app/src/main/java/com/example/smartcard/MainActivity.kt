package com.example.smartcard

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val decks = mutableStateListOf<FlashDeck>()

        //If decks are able to be retrieved, clear the current deck list and add all of the decks from the database to the list
        //This ensures that the list of decks shown to the user is up to date with the database
        getDecks(onSuccess = { fetchedDecks ->
            decks.clear()
            decks.addAll(fetchedDecks)
            }, onFailure = { exception ->
                Log.e("Firestore", "Error retrieving decks", exception)
            })

        setContent {
            SmartCardTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(decks, navController)
                    }
                    composable("flashcards/{deckName}") { backStackEntry ->
                        val deckName = backStackEntry.arguments?.getString("deckName") ?: ""
                        val selectedDeck = decks.find { it.name == deckName }
                        selectedDeck?.let {
                            DeckDetailView(
                                deck = it,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}


//material13 experimental for TopAppBar to work
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun HomeScreen(decks: SnapshotStateList<FlashDeck>, navController: NavHostController) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "SmartCard", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },

    )

    { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Text(
                text = "My Decks",
                fontSize = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )


            LazyColumn(modifier = Modifier.weight(1f)) {
                items(decks) { deck ->
                    DeckView(deck = deck,
                        onDeckClick = {navController.navigate("flashcards/${deck.name}")},
                        //Calls the delete deck function from Firebase.kt which deletes the deck from the database
                        onDeleteClick = { deleteDeck(
                            deck.name,
                            onSuccess = { Log.d("Firestore", "Deck deleted successfully") },
                            onFailure = { exception -> Log.e("Firestore", "Error deleting deck", exception) }
                        ) }
                    )
                }
            }

            NewDeck(
                modifier = Modifier.padding(top = 25.dp)
            )
        }
    }
}

@Composable
fun NewDeck(
    modifier: Modifier = Modifier,
) {
    var inputDeck by remember { mutableStateOf(false) }
    var deckDescription by remember { mutableStateOf("") }
    var deckName by remember { mutableStateOf("") }

    //Button to inputdeck
    Button(
        onClick = { inputDeck = true },
        modifier = modifier.padding(18.dp)
    ) {
        Text(text = "Add new deck", fontSize = 18.sp )
    }

    //Display Dialog if input deck is true
    if (inputDeck) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { inputDeck = false }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create New Deck",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = deckName,
                        onValueChange = { deckName = it },
                        placeholder = { Text(text = "Title of your deck") },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = deckDescription,
                        onValueChange = { deckDescription = it },
                        placeholder = { Text(text = "e.g. This is what the deck is about") },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            //Firestore storage of deck
                            addDeck(deckName, deckDescription)
                            inputDeck = false
                            deckName = ""
                            deckDescription = ""
                        }
                    ) {
                        Text(text = "Confirm")
                    }

                }

            }

        }
    }
}

@Composable
fun DeckView(deck: FlashDeck, onDeckClick: () -> Unit, onDeleteClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) } // State to control dropdown menu
    // For the edit functionality
    var editDeck by remember { mutableStateOf(false) }
    var deckName by remember { mutableStateOf(deck.name) }
    var deckDescription by remember { mutableStateOf(deck.description) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onDeckClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }

            Column(modifier = Modifier.weight(1f)) { // Left side with deck details
                Text(
                    text = deck.name,
                    fontSize = 20.sp
                )
                Text(
                    text = deck.description,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward, // Arrow indicating navigation
                contentDescription = "Go to Flashcards",
                modifier = Modifier.padding(end = 8.dp)
            )

            // Dropdown menu for edit/delete options
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    // Make editDeck true to show the edit dialog
                    onClick = {
                        expanded = false
                        editDeck = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        expanded = false
                        onDeleteClick() // Calls the delete function from Firebase.kt
                    }
                )
            }
        }
    }

    if(editDeck){
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { editDeck = false }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create New Deck",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = deckName,
                        onValueChange = { deckName = it },
                        placeholder = { Text(text = "Title of your deck") },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = deckDescription,
                        onValueChange = { deckDescription = it },
                        placeholder = { Text(text = "e.g. This is what the deck is about") },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            //Calls update deck with the current deck name and the new deckName and deckDescription
                            updateDeck(deck.name, deckDescription, deckName)
                            editDeck = false
                            deckName = ""
                            deckDescription = ""
                        }
                    ) {
                        Text(text = "Confirm")
                    }

                }

            }

        }
    }


}

//aka. FlashCardView screen (all flashcards from deck)
//material13 experimental for TopAppBar to work
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailView(deck: FlashDeck, onBack: () -> Unit) {
    var inputCard by remember { mutableStateOf(false) }
    var cardQuestion by remember { mutableStateOf("") }
    var cardAnswer by remember { mutableStateOf("") }
    var cards by remember { mutableStateOf(listOf<FlashCard>()) }
    var isInQuizMode by remember { mutableStateOf(false) }

    //calls getCardsForDeck to get all the cards in the deck
    LaunchedEffect(deck.name) {
        getCardsForDeck(deck.name, false, onSuccess = { fetchedCards ->
            cards = fetchedCards
        }, onFailure = { exception ->
            Log.e("Firestore", "Error retrieving cards", exception)
        })
    }

    if (isInQuizMode) {
        QuizModeView(deck, onExitQuiz = { isInQuizMode = false },
            onBack = { isInQuizMode = false })
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(text = deck.name, color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { onBack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
//                    Text(
//                        text = "My Cards",
//                        style = MaterialTheme.typography.headlineMedium,
//                        modifier = Modifier.padding(12.dp)
//                    )

                    // Dialog for adding a new card
                    if (inputCard) {
                        androidx.compose.ui.window.Dialog(
                            onDismissRequest = { inputCard = false }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Create New Card",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    TextField(
                                        value = cardQuestion,
                                        onValueChange = { cardQuestion = it },
                                        placeholder = { Text("Card Side A") }
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    TextField(
                                        value = cardAnswer,
                                        onValueChange = { cardAnswer = it },
                                        placeholder = { Text("Card Side B") }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(onClick = {
                                        //Gets the deckId of the current deck
                                        getDeckIdByName(deck.name, onSuccess = { deckId ->
                                            //Adds the card to firestore using the inputted values and deckId
                                            addCard(cardQuestion, cardAnswer, deckId)
                                            inputCard = false
                                            cardQuestion = ""
                                            cardAnswer = ""
                                        }, onFailure = { exception ->
                                            Log.e("Firestore", "Error retrieving deck ID", exception)
                                        })
                                    }) {
                                        Text(text = "Add Card")
                                    }
                                }
                            }
                        }
                    }

                    // Row for "Add New Card" and "Study" buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { inputCard = true },
                            modifier = Modifier.padding(end = 8.dp), // Add padding to separate buttons
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x803700B3) // Set the background color here
                            )
                        ) {
                            Text(text = "+ New Card")
                        }

                        Button(
                            onClick = { isInQuizMode = true },
                            modifier = Modifier.padding(end = 8.dp), // Add padding to separate buttons
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x803700B3) // Set the background color here
                            )
                        ) {
                            Text(text = "Start Quiz")
                        }
                    }

                    // Display list of cards
                    LazyColumn {
                        items(cards) { card ->
                            CardView(card)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun CardView(card: FlashCard) {
    var isQuestionVisible by remember { mutableStateOf(true) }
    var rotationAngle by remember { mutableStateOf(0f) }
    val animatedRotationAngle by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 500)
    )

    Card(
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                rotationAngle += 180f
                isQuestionVisible = !isQuestionVisible
            }
            .graphicsLayer {
                rotationY = animatedRotationAngle
                cameraDistance = 8 * density
                scaleX = if (animatedRotationAngle % 360 > 90 && animatedRotationAngle % 360 < 270) -1f else 1f // Flip the content on the Y-axis
            }
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(8.dp), // Elevation so the card has a shadow
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F8FF)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (animatedRotationAngle % 360 < 90 || animatedRotationAngle % 360 > 270) {
                Text(
                    text = card.question,
                    fontSize = 24.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = if (isQuestionVisible) 1f else 0f
                    }
                )
            } else {
                Text(
                    text = card.answer,
                    fontSize = 22.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = if (!isQuestionVisible) 1f else 0f
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizModeView(deck: FlashDeck, onExitQuiz: () -> Unit, onBack: () -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    var isQuestionVisible by remember { mutableStateOf(true) }
    var rotationAngle by remember { mutableStateOf(0f) }
    var deckId by remember { mutableStateOf("") }
    var cards by remember { mutableStateOf(listOf<FlashCard>()) }
    val currentCard = cards.getOrNull(currentIndex)

    val animatedRotationAngle by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 600)
    )

    //Gets the cards for the deck and shuffles them because its in quiz view
    LaunchedEffect(deck.name) {
        getCardsForDeck(deck.name, true, onSuccess = { fetchedCards -> cards = fetchedCards }, onFailure = { exception ->
            Log.e("Firestore", "Error retrieving cards", exception)
        })
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Quiz Mode", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (cards.isEmpty()) {
                    Text(
                        text = "No cards available in this deck yet!",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(onClick = { onExitQuiz() }) {
                        Text(text = "Exit Quiz Mode")
                    }
                } else {
                    if (currentCard != null) {
                        Card(
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable {
                                    rotationAngle += 180f
                                    isQuestionVisible = !isQuestionVisible
                                }
                                .graphicsLayer {
                                    rotationY = animatedRotationAngle
                                    cameraDistance = 8 * density
                                    scaleX = if (animatedRotationAngle % 360 > 90 && animatedRotationAngle % 360 < 270) -1f else 1f
                                }
                                .fillMaxWidth(0.95f)
                                .fillMaxHeight(0.5f)
                                .clip(MaterialTheme.shapes.medium)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFB0BEC5),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            elevation = CardDefaults.cardElevation(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF0F8FF)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (animatedRotationAngle % 360 < 90 || animatedRotationAngle % 360 > 270) {
                                    Text(
                                        text = currentCard.question,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.graphicsLayer {
                                            alpha = if (isQuestionVisible) 1f else 0f
                                        }
                                    )
                                } else {
                                    Text(
                                        text = currentCard.answer,
                                        fontSize = 22.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.graphicsLayer {
                                            alpha = if (!isQuestionVisible) 1f else 0f
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = {
                                        currentIndex = if (currentIndex > 0) currentIndex - 1 else cards.size - 1
                                        isQuestionVisible = true // Reset to question side
                                        rotationAngle = 0f // Reset rotation
                                    },
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Previous Card",
                                        modifier = Modifier.size(56.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text("Previous", fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = {
                                        currentIndex = if (currentIndex < cards.size - 1) currentIndex + 1 else 0
                                        isQuestionVisible = true // Reset to question side
                                        rotationAngle = 0f // Reset rotation
                                    },
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Next Card",
                                        modifier = Modifier.size(56.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text("Next", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    )
}




