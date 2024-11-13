package com.example.smartcard

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

val db = Firebase.firestore

fun addDeck(deckName: String, deckDescription: String){
    val newDeck = FlashDeck(deckName, deckDescription)

    db.collection("flashdecks")
        .add(newDeck)
        .addOnSuccessListener { documentReference ->
            Log.d("Firestore", "Flashcard added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error adding flashcard", e)
        }
}

fun addCard(front: String, back: String, deckId: String){
    val newCard = FlashCard(front, back, deckId)

    db.collection("flashcards")
        .add(newCard)
        .addOnSuccessListener { documentReference ->
            Log.d("Firestore", "Flashcard added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error adding flashcard", e)
        }
}

fun getDecks(onSuccess: (List<FlashDeck>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection("flashdecks")
        .get()
        .addOnSuccessListener { result ->
            val decks = result.map { document ->
                FlashDeck(
                    name = document.getString("name") ?: "",
                    description = document.getString("description") ?: ""
                )
            }
            onSuccess(decks)
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

fun getCardsForDeck(deckId: String, onSuccess: (List<FlashCard>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection("flashcards")
        .whereEqualTo("deckId", deckId)
        .addSnapshotListener() { snapshot, e ->
            if (e != null) {
                onFailure(e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val cards = snapshot.documents.map { document ->
                    FlashCard(
                        question = document.getString("question") ?: "",
                        answer = document.getString("answer") ?: "",
                        deckId = document.getString("deckId") ?: ""
                    )
                }
                onSuccess(cards)
            } else {
                Log.d("Firestore", "Current data: null")
            }
        }
}

fun getDeckIdByName(deckName: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection("flashdecks")
        .whereEqualTo("name", deckName)
        .get()
        .addOnSuccessListener { result ->
            if (!result.isEmpty) {
                val document = result.documents[0]
                val deckId = document.id
                onSuccess(deckId)
            } else {
                onFailure(Exception("Deck not found"))
            }
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}
