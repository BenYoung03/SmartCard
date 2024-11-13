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

        getDecks(onSuccess = { fetchedDecks ->
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
                        onEditClick = { /* Handle edit action */ },
                        onDeleteClick = {  }
                    )
                }
            }

            NewDeck(
                modifier = Modifier.padding(top = 25.dp),
                onAddDeck = { name, description ->
                    decks.add(FlashDeck(name, description))
                }
            )
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
                            onAddDeck(deckName, deckDescription)
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
fun DeckView(deck: FlashDeck, onDeckClick: () -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) } // State to control dropdown menu

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
                    onClick = {
                        expanded = false
                        onEditClick() //add edit functionality
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        expanded = false
                        onDeleteClick() //add delete functionality
                    }
                )
            }
        }
    }
}


//    @Composable
//    fun FlashcardScreen(deck: FlashDeck, flashcards: SnapshotStateList<Flashcard>) {
//        val curFlashcards = flashcards.filter { it.curDeck == deck }
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//                .padding(top = 30.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//
//        ) {
//            Text(
//                text = "Flashcards for ${deck.name}",
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                textAlign = TextAlign.Center
//            )
//
//            LazyColumn {
//                items(curFlashcards) { flashcard ->
//                    var currentText by remember { mutableStateOf(flashcard.front) }
//                    Box(
//                        modifier = Modifier
//                            .size(300.dp)
//                            .padding(16.dp)
//                            .border(width = 4.dp, color = Gray, shape = RoundedCornerShape(16.dp))
//                            .background(color = DarkGray, shape = RoundedCornerShape(16.dp))
//                            .clickable {
//                                currentText =
//                                    if (currentText == flashcard.front) flashcard.back else flashcard.front
//                            },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = currentText,
//                            modifier = Modifier.padding(16.dp),
//                            textAlign = TextAlign.Center,
//                            fontSize = 25.sp,
//                            color = Color.White
//                        )
//                    }
//                }
//            }
//        }
//    }

@Composable
fun DeckDetailView(deck: FlashDeck, onBack: () -> Unit) {
    var inputCard by remember { mutableStateOf(false) }
    var cardQuestion by remember { mutableStateOf("") }
    var cardAnswer by remember { mutableStateOf("") }
    var cards by remember { mutableStateOf(listOf<FlashCard>()) }
    var deckId by remember { mutableStateOf("") }
    var isInQuizMode by remember { mutableStateOf(false) }

    LaunchedEffect(deck.name) {
        getDeckIdByName(deck.name, onSuccess = { id ->
            deckId = id
        }, onFailure = { exception ->
            Log.e("Firestore", "Error retrieving deck ID", exception)
        })
    }

    LaunchedEffect(deckId) {
        getCardsForDeck(deckId, onSuccess = { fetchedCards ->
            cards = fetchedCards
        }, onFailure = { exception ->
            Log.e("Firestore", "Error retrieving cards", exception)
        })
    }

    if(isInQuizMode) {
        QuizModeView(deck, onExitQuiz = { isInQuizMode = false })
    } else {

        Column(modifier = Modifier.fillMaxSize()) {

            Button(onClick = { onBack() }) {
                Text("Back to Decks")
            }

            Text(
                text = deck.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(12.dp)
            )

            Button(onClick = { isInQuizMode = true}) {
                Text("Start Quiz")
            }
            if (inputCard) {
                TextField(
                    value = cardQuestion,
                    onValueChange = { cardQuestion = it },
                    placeholder = { Text("Card Question") })
                TextField(
                    value = cardAnswer,
                    onValueChange = { cardAnswer = it },
                    placeholder = { Text("Card Answer") })
                Button(onClick = {
                    getDeckIdByName(deck.name, onSuccess = { deckId ->
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
            } else {
                Button(onClick = { inputCard = true }) {
                    Text(text = "Add New Card")
                }
            }

            LazyColumn {
                items(cards) { card ->
                    CardView(card)
                }
            }
        }
    }
}

@Composable
fun CardView(card: FlashCard) {
    var isQuestionVisible by remember { mutableStateOf(true) }
    var rotationAngle by remember { mutableStateOf(0f) }
    val animatedRotationAngle by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 600)
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
            .background(Color.White)
            .fillMaxWidth(0.8f)
            .height(200.dp)
            .clip(MaterialTheme.shapes.medium)
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
                    text = "${card.question}",
                    fontSize = 24.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = if (isQuestionVisible) 1f else 0f
                    }
                )
            } else {
                Text(
                    text = "${card.answer}",
                    fontSize = 22.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = if (!isQuestionVisible) 1f else 0f
                    }
                )
            }
        }
    }
}

@Composable
fun QuizModeView(deck: FlashDeck, onExitQuiz: () -> Unit) {
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

    LaunchedEffect(deck.name) {
        getDeckIdByName(deck.name, onSuccess = { id ->
            deckId = id
        }, onFailure = { exception ->
            Log.e("Firestore", "Error retrieving deck ID", exception)
        })
    }

    LaunchedEffect(deckId) {
        getCardsForDeck(deckId, onSuccess = { fetchedCards ->
            cards = fetchedCards
        }, onFailure = { exception ->
            Log.e("Firestore", "Error retrieving cards", exception)
        })
    }

    // Handling the scenario where no cards are added to the deck
    if (cards.isEmpty()) {
        // Show a message if the deck is empty
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No cards available in this deck.",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.error,  // Show an error message in red
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = { onExitQuiz() },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Exit Quiz Mode")
            }
        }
    } else {
        // Main Quiz Mode UI when cards are available
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Navigation buttons (Previous, Next) with consistent styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (currentIndex > 0) {
                            currentIndex--
                        } else {
                            currentIndex = cards.size - 1 // Loop back to the last card
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Previous Card")
                }
                Button(
                    onClick = {
                        if (currentIndex < cards.size - 1) {
                            currentIndex++
                        } else {
                            currentIndex = 0 // Loop back to the first card
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Next Card")
                }
            }

            // Ensure card aesthetic is consistent with CardView
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
                            scaleX = if (animatedRotationAngle % 360 > 90 && animatedRotationAngle % 360 < 270) -1f else 1f // Flip the content on the Y-axis
                        }
                        .background(MaterialTheme.colorScheme.surface)  // Use the same surface color as before
                        .fillMaxWidth(0.8f)
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)  // Consistent card shape
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Show question or answer based on rotation angle
                        if (animatedRotationAngle % 360 < 90 || animatedRotationAngle % 360 > 270) {
                            Text(
                                text = currentCard.question,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface,  // Text color consistent with theme
                                modifier = Modifier.graphicsLayer {
                                    alpha = if (isQuestionVisible) 1f else 0f
                                }
                            )
                        } else {
                            Text(
                                text = currentCard.answer,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface,  // Text color consistent with theme
                                modifier = Modifier.graphicsLayer {
                                    alpha = if (!isQuestionVisible) 1f else 0f
                                }
                            )
                        }
                    }
                }
            }

            // Exit Quiz button below card view and navigation buttons
            Button(
                onClick = { onExitQuiz() },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally) // Center align the button
            ) {
                Text(text = "Exit Quiz Mode")
            }
        }
    }
}



