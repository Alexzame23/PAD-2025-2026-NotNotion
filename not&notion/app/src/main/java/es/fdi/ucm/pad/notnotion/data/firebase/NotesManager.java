package es.fdi.ucm.pad.notnotion.data.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;

import es.fdi.ucm.pad.notnotion.data.model.Note;

public class NotesManager {

    private static final String TAG = "NotesManager";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public NotesManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Devuelve la ruta base de las notas de una carpeta específica
     * Ejemplo: users/{uid}/folders/{folderId}/notes
     */
    private String getNotesPath(@NonNull String folderId) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Log.e(TAG, "No hay usuario autenticado");
            return null;
        }
        return "users/" + uid + "/folders/" + folderId + "/notes";
    }

    /**
     * Inserta una nueva nota en una carpeta específica
     */
    public void addNote(@NonNull String title, @NonNull String content, @NonNull String folderId, boolean isFavorite) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        String noteId = UUID.randomUUID().toString();

        Note note = new Note(
                noteId,
                title,
                content,
                folderId,
                Timestamp.now(),
                Timestamp.now(),
                isFavorite
        );

        db.collection(path)
                .document(noteId)
                .set(note)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nota creada correctamente"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al crear nota", e));
    }

    /**
     * Actualiza una nota existente (en su carpeta)
     */
    public void updateNote(@NonNull Note note) {
        String path = getNotesPath(note.getFolderId());
        if (path == null) return;

        note.setUpdatedAt(Timestamp.now());

        db.collection(path)
                .document(note.getId())
                .set(note)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nota actualizada"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar nota", e));
    }

    /**
     * Elimina una nota concreta dentro de una carpeta
     */
    public void deleteNote(@NonNull String folderId, @NonNull String noteId) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        db.collection(path)
                .document(noteId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nota eliminada"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar nota", e));
    }

    /**
     * Obtiene todas las notas de una carpeta específica
     */
    public void getNotesByFolder(@NonNull String folderId, OnSuccessListener<QuerySnapshot> listener) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        db.collection(path)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener notas por carpeta", e));
    }

    /**
     * Obtiene las notas favoritas dentro de una carpeta
     */
    public void getFavoriteNotes(@NonNull String folderId, OnSuccessListener<QuerySnapshot> listener) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        db.collection(path)
                .whereEqualTo("isFavorite", true)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener notas favoritas", e));
    }

    /**
     * Obtiene todas las notas del usuario (de todas las carpetas)
     * 🔹 Requiere recorrer todas las subcolecciones "notes" bajo cada folder
     */
    public void getAllNotes(OnSuccessListener<QuerySnapshot> listener) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Log.e(TAG, "No hay usuario autenticado");
            return;
        }

        db.collectionGroup("notes") // 🔹 busca en todas las subcolecciones llamadas "notes"
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener todas las notas", e));
    }
}
