package es.fdi.ucm.pad.notnotion.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
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
import es.fdi.ucm.pad.notnotion.data.adapter.FoldersAdapter;
import es.fdi.ucm.pad.notnotion.data.adapter.NotesAdapter;
import es.fdi.ucm.pad.notnotion.data.firebase.FoldersManager;
import es.fdi.ucm.pad.notnotion.data.firebase.NotesManager;
import es.fdi.ucm.pad.notnotion.data.model.Folder;
import es.fdi.ucm.pad.notnotion.data.model.Note;
import es.fdi.ucm.pad.notnotion.ui.Fragments.CalendarFragment;
import es.fdi.ucm.pad.notnotion.ui.user_logging.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FrameLayout contentContainer = findViewById(R.id.contentContainer);

        if (contentContainer != null) {
            getLayoutInflater().inflate(R.layout.notes_main, contentContainer, true);

            ViewCompat.setOnApplyWindowInsetsListener(contentContainer, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            ImageButton btnPerfil = findViewById(R.id.btnPerfil);

            if (user != null) {
                // Si el usuario tiene foto, la mostramos en el botón
                if (user.getPhotoUrl() != null) {
                    Uri photoUri = user.getPhotoUrl();
                    Picasso.get()
                            .load(photoUri)
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .into(btnPerfil);
                }

                // NUEVA FUNCIÓN: menú del perfil
                btnPerfil.setOnClickListener(v -> showProfileMenu(btnPerfil));

            } else {
                // Si no hay usuario logueado, mandamos a LoginActivity
                btnPerfil.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            // Configurar RecyclerViews
            RecyclerView recyclerFolders = contentContainer.findViewById(R.id.recyclerFolders);
            RecyclerView recyclerNotes = contentContainer.findViewById(R.id.recyclerNotes);

            // NUEVA FUNCIÓN: menú del perfil
            btnPerfil.setOnClickListener(v -> showProfileMenu(btnPerfil));

            recyclerFolders.setLayoutManager(new GridLayoutManager(this, 3));
            recyclerNotes.setLayoutManager(new GridLayoutManager(this, 3));

            /*
            recyclerFolders.setAdapter(foldersAdapter);
            recyclerNotes.setAdapter(notesAdapter);

            // Cargar datos de Firebase (ejemplo, carpetas raíz y notas raíz)
            new FoldersManager().getRootFolders(querySnapshot -> {
                List<Folder> folders = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    folders.add(doc.toObject(Folder.class));
                }
                foldersAdapter.setFolders(folders);
            });

            new NotesManager().getAllNotes(querySnapshot -> {
                List<Note> notes = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    notes.add(doc.toObject(Note.class));
                }
                notesAdapter.setNotes(notes);
            });
             */
        }

        // BottomNavigation y resto de tu código tal como lo tenías
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null && contentContainer != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                contentContainer.removeAllViews();

                if (id == R.id.nav_notes) {
                    getLayoutInflater().inflate(R.layout.notes_main, contentContainer, true);
                    // Aquí volverías a configurar los RecyclerViews como arriba si cambias de pestaña
                } else if (id == R.id.nav_calendar) {
                    getLayoutInflater().inflate(R.layout.calendar_main, contentContainer, true);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.contentContainer, new CalendarFragment())
                            .commit();
                }
                return true;
            });
        }

        // Botones de perfil, notas, calendario y barra de búsqueda igual que en tu código original
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
