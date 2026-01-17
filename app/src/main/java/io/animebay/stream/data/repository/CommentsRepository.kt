package io.animebay.stream.data.repository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.animebay.stream.data.model.Comment
import kotlinx.coroutines.tasks.await

class CommentsRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    suspend fun getComments(animeId: String): List<Comment> {
        return try {
            val snapshot = db.collection("comments")
                .whereEqualTo("animeId", animeId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(Comment::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addComment(animeId: String, text: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        var userNameFromDb = "مستخدم أنمي باي"
        var userProfilePicFromDb: String? = null

        try {
            val userDocument = db.collection("users").document(currentUser.uid).get().await()
            if (userDocument.exists()) {
                userNameFromDb = userDocument.getString("name") ?: userNameFromDb
                
                // ✅ --- تم التصحيح هنا لاستخدام الاسم الصحيح --- ✅
                userProfilePicFromDb = userDocument.getString("profileImageUrl") ?: userProfilePicFromDb

            } else {
                userNameFromDb = currentUser.displayName ?: userNameFromDb
                userProfilePicFromDb = currentUser.photoUrl?.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            userNameFromDb = currentUser.displayName ?: userNameFromDb
            userProfilePicFromDb = currentUser.photoUrl?.toString()
        }

        val newComment = Comment(
            animeId = animeId,
            text = text,
            userId = currentUser.uid,
            userName = userNameFromDb,
            userProfilePic = userProfilePicFromDb
        )

        return try {
            db.collection("comments").add(newComment).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteComment(commentId: String): Boolean {
        return try {
            db.collection("comments").document(commentId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
