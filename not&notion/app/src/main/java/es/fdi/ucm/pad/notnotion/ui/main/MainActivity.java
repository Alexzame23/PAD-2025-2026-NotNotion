package es.fdi.ucm.pad.notnotion.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.FirebaseFirestoreManager;
import es.fdi.ucm.pad.notnotion.ui.calendar.CalendarFragment;
import es.fdi.ucm.pad.notnotion.ui.user_logging.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        //contenedor para cargar las pantallas principales
        FrameLayout contentContainer = findViewById(R.id.contentContainer);
        getLayoutInflater().inflate(R.layout.notes_main, contentContainer, true);
        // Ajuste para pantallas Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(contentContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        /*
        Esto para hacer que el recyclerView muestre los items de 3 en 3

        recyclerView = findViewById(R.id.recyclerItems);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        */

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ImageButton btnPerfil = findViewById(R.id.btnPerfil);
        if (user != null) {
            // Mandar a profile_activity
            if (user.getPhotoUrl() != null) {
                Uri photoUri = user.getPhotoUrl();

                // Cargar la foto en el botón
                Picasso.get()
                        .load(photoUri)
                        .placeholder(R.drawable.ic_user) // Imagen por defecto
                        .error(R.drawable.ic_user)       // Si falla la carga
                        .into(btnPerfil);
            }
        }
        else{
            btnPerfil.setOnClickListener(v -> {
                // Lanzar LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestoreManager firestoreManager = new FirebaseFirestoreManager();

        if (firebaseUser != null) {
            firestoreManager.getCurrentUserData(usr -> {
                if (usr != null) {
                    // Aquí tienes todos los datos del usuario desde Firestore
                    Log.d("MainActivity", "Usuario: " + usr.getUsername());
                    Log.d("MainActivity", "Email: " + usr.getEmail());
                    Log.d("MainActivity", "Idioma: " + usr.getPreferences().get("language"));
                    Log.d("MainActivity", "Tema: " + usr.getPreferences().get("theme"));

                    // Ejemplo: actualizar UI
                    // AQUI RECOGER LOS DATOS DE LA CARPETA RAIZ (parentFolderID == null)
            });
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            // Limpiar el contenedor antes de transformar
            int id = item.getItemId();
            contentContainer.removeAllViews();

            //navegamos entre ambos botones
            if (id == R.id.nav_notes) {
                getLayoutInflater().inflate(R.layout.notes_main, contentContainer, true);
            } else if (id == R.id.nav_calendar) {
                getLayoutInflater().inflate(R.layout.calendar_main, contentContainer, true);
                // Cargar fragmento dentro del FrameLayout
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.contentContainer, new CalendarFragment())
                        .commit();
            }

            return true;
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
    }
}