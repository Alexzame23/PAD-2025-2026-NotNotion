package es.fdi.ucm.pad.notnotion.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.MenuItem;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.firebase.ui.auth.AuthUI;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.FirebaseFirestoreManager;
import es.fdi.ucm.pad.notnotion.data.adapter.FoldersAdapter;
import es.fdi.ucm.pad.notnotion.data.adapter.NotesAdapter;
import es.fdi.ucm.pad.notnotion.data.firebase.FoldersManager;
import es.fdi.ucm.pad.notnotion.data.firebase.NotesManager;
import es.fdi.ucm.pad.notnotion.data.model.Folder;
import es.fdi.ucm.pad.notnotion.data.model.Note;
import es.fdi.ucm.pad.notnotion.ui.Fragments.CalendarFragment;
import es.fdi.ucm.pad.notnotion.ui.profile.ProfileActivity;
import es.fdi.ucm.pad.notnotion.ui.user_logging.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestoreManager firestoreManager;
    private FoldersManager foldersManager;
    private NotesManager notesManager;
    private es.fdi.ucm.pad.notnotion.data.model.User currentUser;
    private Folder currentFolder;
    private FoldersAdapter foldersAdapter;
    private NotesAdapter notesAdapter;
    private RecyclerView recyclerFolders;
    private RecyclerView recyclerNotes;
    private TextView emptyMessage;
    private final List<String> routePath = new ArrayList<>();
    private final List<Folder> navigationStack = new ArrayList<>();
    private TextView routeTextShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FrameLayout contentContainer = findViewById(R.id.contentContainer);

        FirebaseFirestore.setLoggingEnabled(true);
        Log.d("FirestoreDebug", "Firestore logging enabled");

        firestoreManager = new FirebaseFirestoreManager();
        foldersManager   = new FoldersManager();
        notesManager = new NotesManager();

        if (contentContainer != null) {
            getLayoutInflater().inflate(R.layout.notes_main, contentContainer, true);

            ViewCompat.setOnApplyWindowInsetsListener(contentContainer, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });

            // --- Inicializar componentes ---
            recyclerFolders = contentContainer.findViewById(R.id.recyclerFolders);
            recyclerNotes = contentContainer.findViewById(R.id.recyclerNotes);
            emptyMessage = contentContainer.findViewById(R.id.emptyMessage);

            foldersAdapter = new FoldersAdapter();
            notesAdapter   = new NotesAdapter();

            recyclerFolders.setLayoutManager(new GridLayoutManager(this, 3));
            recyclerNotes.setLayoutManager(new GridLayoutManager(this, 3));

            recyclerFolders.setAdapter(foldersAdapter);
            recyclerNotes.setAdapter(notesAdapter);

            ImageButton btnGoBack = contentContainer.findViewById(R.id.btnGoBack);
            if (btnGoBack != null) {
                btnGoBack.setOnClickListener(v -> goBack());
            }
            ImageButton btnAddNote = contentContainer.findViewById(R.id.btnAddNote);

            btnAddNote.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(MainActivity.this, btnAddNote);
                popup.getMenu().add("Nueva carpeta");
                popup.getMenu().add("Nueva nota");

                popup.setOnMenuItemClickListener(itm -> {
                    String title = itm.getTitle().toString();

                    if (title.equals("Nueva carpeta")) {
                        createNewFolderDialog();
                        return true;
                    }
                    if (title.equals("Nueva nota")) {
                        // TODO
                        return true;
                    }
                    return false;
                });

                popup.show();
            });
            // --- Perfil ---
            ImageButton btnPerfil = findViewById(R.id.btnPerfil);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {

                // Cargar datos del usuario desde Firestore
                firestoreManager.getCurrentUserData(userData -> {
                    currentUser = userData;

                    Log.d("MainActivity", "Usuario Firestore cargado:");
                    Log.d("MainActivity", "Email: " + currentUser.getEmail());
                    Log.d("MainActivity", "Username: " + currentUser.getUsername());
                    Log.d("MainActivity", "Preferencias: " + currentUser.getPreferences());
                });

                // Foto en botón de perfil
                if (user.getPhotoUrl() != null) {
                    Picasso.get()
                            .load(user.getPhotoUrl())
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .into(btnPerfil);
                }

                btnPerfil.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                });

            } else {
                // Si NO hay usuario → ir a Login
                btnPerfil.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
            currentFolder = new Folder("root", "Root", "None", null, null, 0);
            loadFolderContent(currentFolder);
            foldersAdapter.setOnFolderClickListener(folder -> {
                navigationStack.add(currentFolder);
                loadFolderContent(folder);
            });
        }

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {

                int id = item.getItemId();
                contentContainer.removeAllViews();

                // -------- TAB NOTAS --------
                if (id == R.id.nav_notes) {

                    contentContainer.removeAllViews();
                    getLayoutInflater().inflate(R.layout.notes_main, contentContainer, true);

                    recyclerFolders = contentContainer.findViewById(R.id.recyclerFolders);
                    recyclerNotes   = contentContainer.findViewById(R.id.recyclerNotes);
                    emptyMessage    = contentContainer.findViewById(R.id.emptyMessage);

                    foldersAdapter = new FoldersAdapter();
                    notesAdapter   = new NotesAdapter();

                    recyclerFolders.setLayoutManager(new GridLayoutManager(this, 3));
                    recyclerNotes.setLayoutManager(new GridLayoutManager(this, 3));

                    recyclerFolders.setAdapter(foldersAdapter);
                    recyclerNotes.setAdapter(notesAdapter);

                    foldersManager = new FoldersManager();
                    notesManager   = new NotesManager();

                    if (currentFolder == null) {
                        currentFolder = new Folder("root", "Root", "None", null, null, 0);
                    }
                    loadFolderContent(currentFolder);
                    routePath.clear();
                    updateRouteText();

                    ImageButton btnGoBack = contentContainer.findViewById(R.id.btnGoBack);
                    if (btnGoBack != null) {
                        btnGoBack.setOnClickListener(v -> goBack());
                    }

                    foldersAdapter.setOnFolderClickListener(folder -> {
                        navigationStack.add(currentFolder);
                        loadFolderContent(folder);
                    });

                    ImageButton btnAddNote = contentContainer.findViewById(R.id.btnAddNote);

                    btnAddNote.setOnClickListener(v -> {
                        PopupMenu popup = new PopupMenu(MainActivity.this, btnAddNote);
                        popup.getMenu().add("Nueva carpeta");
                        popup.getMenu().add("Nueva nota");

                        popup.setOnMenuItemClickListener(itm -> {
                            String title = itm.getTitle().toString();

                            if (title.equals("Nueva carpeta")) {
                                createNewFolderDialog();
                                return true;
                            }
                            if (title.equals("Nueva nota")) {
                                // TODO: implementar
                                return true;
                            }
                            return false;
                        });

                        popup.show();
                    });
                }

                else if (id == R.id.nav_calendar) {
                    getLayoutInflater().inflate(R.layout.calendar_main, contentContainer, true);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.contentContainer, new CalendarFragment())
                            .commit();
                }

                return true;
            });
        }

    }
    private void goBack() {
        if (navigationStack.isEmpty()) return;

        Folder prev = navigationStack.remove(navigationStack.size() - 1);

        if (!routePath.isEmpty())
            routePath.remove(routePath.size() - 1);

        loadFolderContent(prev);
    }
    private void createNewFolderDialog() {
        final EditText input = new EditText(this);
        input.setHint("Nombre de la carpeta");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Crear nueva carpeta")
                .setView(input)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String folderName = input.getText().toString().trim();

                    if (folderName.isEmpty()) {
                        folderName = "Nueva carpeta";
                    }

                    createFolder(folderName);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    private void createFolder(String folderName) {

        if (currentFolder == null) return;

        foldersManager.createFolder(
                folderName,
                currentFolder.getId(),
                () -> {

                    loadFolderContent(currentFolder);
                }
        );
    }

    private void loadFolderContent(Folder folder) {

        this.currentFolder = folder;

        updateRouteText();

        foldersManager.getSubfolders(folder.getId(), folderSnapshot -> {

            List<Folder> subfolders = new ArrayList<>();
            for (QueryDocumentSnapshot doc : folderSnapshot) {
                subfolders.add(doc.toObject(Folder.class));
            }

            foldersAdapter.setFolders(subfolders);

            loadNotesForFolder(folder, subfolders);
        });
    }

    private void updateRouteText() {
        routeTextShow = findViewById(R.id.routeTextShow);
        if (routeTextShow == null) return;

        if (currentFolder.getId().equals("root")) {
            routePath.clear();
            routeTextShow.setText("C:/root");
            return;
        }
        if (routePath.isEmpty() ||
                !routePath.get(routePath.size() - 1).equals(currentFolder.getName())) {
            routePath.add(currentFolder.getName());
        }

        StringBuilder sb = new StringBuilder("C:/root");

        for (String name : routePath) {
            sb.append("/").append(name);
        }

        routeTextShow.setText(sb.toString());
    }
    private void loadNotesForFolder(Folder folder, List<Folder> subfolders) {

        notesManager.getNotesByFolder(folder.getId(), noteSnapshot -> {

            List<Note> notes = new ArrayList<>();
            for (QueryDocumentSnapshot doc : noteSnapshot) {
                notes.add(doc.toObject(Note.class));
            }

            notesAdapter.setNotes(notes);

            // CONTROL FINAL: ¿hay algo?
            boolean isEmpty = (subfolders.isEmpty() && notes.isEmpty());

            if (isEmpty) {
                emptyMessage.setVisibility(View.VISIBLE);
            } else {
                emptyMessage.setVisibility(View.GONE);
            }
        });
    }


    private void showProfileMenu(ImageButton anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Cerrar sesión"); // Solo una opción por ahora

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Cerrar sesión")) {
                logout();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        AuthUI.getInstance().signOut(this); // Por si usó Google Sign-In
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ya no hacemos signOut automático
    }
}
