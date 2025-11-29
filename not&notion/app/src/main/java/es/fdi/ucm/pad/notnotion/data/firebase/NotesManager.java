package es.fdi.ucm.pad.notnotion.data.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.data.model.Note;
import es.fdi.ucm.pad.notnotion.data.model.ContentBlock;

public class NotesManager {

    private static final String TAG = "NotesManager";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public NotesManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Devuelve la ruta base de las notas de una carpeta específica
    private String getNotesPath(@NonNull String folderId) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Log.e(TAG, "No hay usuario autenticado");
            return null;
        }
        return "users/" + uid + "/folders/" + folderId + "/notes";
    }

    // Inserta una nueva nota en una carpeta específica
    public void addNote(@NonNull String title, @NonNull String content,
                        @NonNull String folderId, boolean isFavorite) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        String noteId = UUID.randomUUID().toString();

        // Convertimos el texto simple en un bloque de contenido
        List<ContentBlock> contentBlocks = new ArrayList<>();

        // Si el contenido no está vacío, lo añadimos como un bloque de texto
        if (content != null && !content.trim().isEmpty()) {
            ContentBlock textBlock = ContentBlock.createTextBlock(
                    content,
                    ContentBlock.STYLE_NORMAL,  // estilo normal por defecto
                    16  // tamaño 16sp por defecto
            );
            contentBlocks.add(textBlock);
        }

        // Ahora creamos la nota con el nuevo constructor
        Note note = new Note(
                noteId,             // id
                title,              // título
                folderId,           // carpeta padre
                Timestamp.now(),    // fecha creación
                Timestamp.now(),    // fecha actualización
                isFavorite,         // es favorita
                null,               // coverImageUrl
                contentBlocks       // lista de bloques

        );

        db.collection(path)
                .document(noteId)
                .set(note)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nota creada correctamente"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al crear nota", e));
    }

    // Inserta una nueva nota en una carpeta específica con bloques de contenido
    public void addNoteWithBlocks(@NonNull String title,
                                  String coverImageUrl,
                                  @NonNull List<ContentBlock> contentBlocks,
                                  @NonNull String folderId,
                                  boolean isFavorite) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        String noteId = UUID.randomUUID().toString();

        Note note = new Note(
                noteId,
                title,
                folderId,
                Timestamp.now(),
                Timestamp.now(),
                isFavorite,
                coverImageUrl,
                contentBlocks
        );

        db.collection(path)
                .document(noteId)
                .set(note)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nota con bloques creada correctamente"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al crear nota", e));
    }

    // Actualiza una nota existente
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

    // Elimina una nota
    public void deleteNote(@NonNull String folderId, @NonNull String noteId) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        db.collection(path)
                .document(noteId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nota eliminada"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar nota", e));
    }

    // Obtiene todas las notas de una carpeta
    public void getNotesByFolder(@NonNull String folderId, OnSuccessListener<QuerySnapshot> listener) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        db.collection(path)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener notas por carpeta", e));
    }

    // Obtiene todas las notas favoritas de una carpeta
    public void getFavoriteNotes(@NonNull String folderId, OnSuccessListener<QuerySnapshot> listener) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        db.collection(path)
                .whereEqualTo("isFavorite", true)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener notas favoritas", e));
    }

    // Obtiene todas las notas de todos los usuarios
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

    public void countNotesInFolder(@NonNull String folderId,
                                   @NonNull OnSuccessListener<Integer> listener) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        db.collection(path)
                .get()
                .addOnSuccessListener(q -> listener.onSuccess(q.size()))
                .addOnFailureListener(e -> Log.e(TAG, "Error contando notas", e));
    }
}
