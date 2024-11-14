package com.example.smartcard

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

val db = Firebase.firestore

//Add deck function
fun addDeck(deckName: String, deckDescription: String){
    //Makes a new deck object given the name and description
    val newDeck = FlashDeck(deckName, deckDescription)

    //Passes the object created to the flashdeck collection in the firestore database
    db.collection("flashdecks")
        .add(newDeck)
        .addOnSuccessListener { documentReference ->
            //Logs the success of adding the deck
            Log.d("Firestore", "Flashcard added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            //Logs if there is an error adding a deck
            Log.w("Firestore", "Error adding flashcard", e)
        }
}

//Function to add cards
fun addCard(front: String, back: String, deckId: String){
    //Makes a new card object given the front and back of the card and the associated deckId
    val newCard = FlashCard(front, back, deckId)

    //Similar logic to adding decks. Adds the card object to the flashcards collection in the firestore database
    db.collection("flashcards")
        .add(newCard)
        .addOnSuccessListener { documentReference ->
            //Logs the success of adding the card
            Log.d("Firestore", "Flashcard added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            //Logs if there is an error adding a card
            Log.w("Firestore", "Error adding flashcard", e)
        }
}


//Function for reading the decks from the firestore database
fun getDecks(onSuccess: (List<FlashDeck>) -> Unit, onFailure: (Exception) -> Unit) {
    //Gets the flashdecks collection from the firestore database
    db.collection("flashdecks")
        //Adds a snapshot listener to the collection which provides realtime updates from the database to the app
    .addSnapshotListener() { snapshot, e ->
        if (e != null) {
            onFailure(e)
            return@addSnapshotListener
        }

        if (snapshot != null) {
            //If there is data in the snapshot, it takes that data and maps it to a list of flashdeck objects
            val decks = snapshot.documents.map { document ->
                FlashDeck(
                    name = document.getString("name") ?: "",
                    description = document.getString("description") ?: "",
                )
            }
            //passes the list back to the main activity if successful
            onSuccess(decks)
        } else {
            Log.d("Firestore", "Current data: null")
        }
    }
}

//Function for getting the cards for a given deck
fun getCardsForDeck(deckId: String, onSuccess: (List<FlashCard>) -> Unit, onFailure: (Exception) -> Unit) {
    //Gets the flashcards collection from the firestore database
    db.collection("flashcards")
        //Finds all flashcards that are associated with the current deck by comparing deckId
        .whereEqualTo("deckId", deckId)
        .addSnapshotListener() { snapshot, e ->
            if (e != null) {
                onFailure(e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                //Creates a list of flashcard objects which contains all of the flashcards associated with the current deck
                val cards = snapshot.documents.map { document ->
                    FlashCard(
                        question = document.getString("question") ?: "",
                        answer = document.getString("answer") ?: "",
                        deckId = document.getString("deckId") ?: ""
                    )
                }
                //Passes the cards back to the main activity if successful
                onSuccess(cards)
            } else {
                Log.d("Firestore", "Current data: null")
            }
        }
}

//Function for getting the deckId from the name of the deck
fun getDeckIdByName(deckName: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    //Gets the flashdecks collection from the firestore database
    db.collection("flashdecks")
        //Finds all of the flash decks where the deckName passed is equal to the deckName in the database
        .whereEqualTo("name", deckName)
        .get()
        .addOnSuccessListener { result ->
            if (!result.isEmpty) {
                //Takes the resulting list of documents and gets the id of the first document (should only be one)
                val document = result.documents[0]
                //Gets the deckId from the given document and returns it to the main activity
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
//Function for deleting decks
fun deleteDeck(deckName:String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    //Calls getDeckIdByName to get the deckId of the deck to be deleted given then name
    getDeckIdByName(deckName, onSuccess = { id ->
        //Gets the flashdeck collection
        db.collection("flashdecks")
            //Finds the document that matches the ID obtained from getDeckIdByName
            .document(id)
            //Deletes the document
            .delete()
            .addOnSuccessListener {
                onSuccess("Deck deleted")
                //When a deck is deleted, it is important we also delete all of the flashcards directly associated with it
                //So we get the flashcards collection
                db.collection("flashcards")
                    //Find all flashcards that have the deckId of the deck we just deleted
                    .whereEqualTo("deckId", id)
                    .get()
                    .addOnSuccessListener { result ->
                        //Deletes all of the cards found associated with the given deck
                        for (document in result) {
                            document.reference.delete()
                        }
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }, onFailure = { exception ->
        Log.e("Firestore", "Error retrieving deck ID", exception)
    })
}
