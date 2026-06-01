package com.example.vocabtrainer.data.remote

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseRealtimeDatabaseClient {

    private const val DATABASE_URL = "https://vocabtrainer-6bf8a-default-rtdb.firebaseio.com"

    val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance(DATABASE_URL) }

    val root: DatabaseReference get() = database.reference

    val wordsRef: DatabaseReference get() = root.child("words")

    val metaVersionRef: DatabaseReference get() = root.child("meta").child("version")
}