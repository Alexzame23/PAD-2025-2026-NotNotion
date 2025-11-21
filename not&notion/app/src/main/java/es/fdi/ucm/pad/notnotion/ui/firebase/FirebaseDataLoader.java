package es.fdi.ucm.pad.notnotion.ui.firebase;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.data.model.Folder;
import es.fdi.ucm.pad.notnotion.data.model.Note;

public class FirebaseDataLoader {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FirebaseDataLoader() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public void loadFolders(OnSuccessListener<List<Folder>> listener) {
        String uid = getUserId();
        if (uid == null) {
            listener.onSuccess(new ArrayList<>());
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("folders")
                .get()
                .addOnSuccessListener(query -> {
                    List<Folder> result = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        Folder f = doc.toObject(Folder.class);
                        result.add(f);
                    }
                    listener.onSuccess(result);
                });
    }

    public void loadFoldersInRoute(String parentFolderId,
                                   OnSuccessListener<List<Folder>> listener) {

        String uid = getUserId();
        if (uid == null) {
            listener.onSuccess(new ArrayList<>());
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("folders")
                .whereEqualTo("parentId", parentFolderId)
                .get()
                .addOnSuccessListener(query -> {
                    List<Folder> result = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        result.add(doc.toObject(Folder.class));
                    }
                    listener.onSuccess(result);
                });
    }
    
    public void loadNotesInFolder(String folderId,
                                  OnSuccessListener<List<Note>> listener) {

        String uid = getUserId();
        if (uid == null) {
            listener.onSuccess(new ArrayList<>());
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("notes")
                .whereEqualTo("folderId", folderId)
                .get()
                .addOnSuccessListener(query -> {
                    List<Note> result = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        result.add(doc.toObject(Note.class));
                    }
                    listener.onSuccess(result);
                });
    }
}
