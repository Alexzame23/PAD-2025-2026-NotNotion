package es.fdi.ucm.pad.notnotion.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.ui.user_logging.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //SOLO PARA PRUEBA
        //*******************************
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // Cierra sesión de Firebase (y del proveedor si usas FirebaseUI)
            AuthUI.getInstance().signOut(this).addOnCompleteListener(t -> {
                // Nada más que hacer; el usuario ya está deslogueado y verá la UI de login
            });
        }
        //*******************************

        //contenedor para las notas
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
        */
        recyclerView = findViewById(R.id.recyclerItems);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));


        //Boton inicio sesión
        ImageButton btnPerfil = findViewById(R.id.btnPerfil);
        btnPerfil.setOnClickListener(v -> {
            // Lanzar LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
    }
}