package es.fdi.ucm.pad.notnotion.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.ui.user_logging.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfilePhoto;
    private TextView tvEmail, tvName, tvUid, tvVerified;
    private Button btnLogout, btnSendVerifyEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // --- Referencias UI ---
        imgProfilePhoto = findViewById(R.id.imgProfilePhoto);
        tvEmail         = findViewById(R.id.tvEmail);
        tvName          = findViewById(R.id.tvName);
        tvUid           = findViewById(R.id.tvUid);
        tvVerified      = findViewById(R.id.tvVerified);
        btnLogout       = findViewById(R.id.btnLogout);
        btnSendVerifyEmail = findViewById(R.id.btnSendVerifyEmail);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // ---------------------------------------------------------
        //      SI NO HAY USUARIO → REDIRIGIR A LOGIN
        // ---------------------------------------------------------
        if (user == null) {
            Toast.makeText(this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }


        // ---------------------------------------------------------
        //      RECARGAR INFO DEL USUARIO (RELOAD)
        // ---------------------------------------------------------
        user.reload().addOnSuccessListener(unused -> {
            updateUserUI(user);  // 🔥 Actualizamos toda la interfaz aquí
        });


        // ---------------------------------------------------------
        //      LOGOUT
        // ---------------------------------------------------------
        btnLogout.setOnClickListener(v -> logout());
    }


    // ********************************************************************
    //   ACTUALIZA TODA LA UI CON LOS DATOS DEL USUARIO (MODULARIZADO)
    // ********************************************************************
    private void updateUserUI(FirebaseUser user) {

        // --- Info Básica ---
        tvEmail.setText("Correo: " + user.getEmail());
        tvName.setText("Nombre: " +
                (user.getDisplayName() != null ? user.getDisplayName() : "No disponible"));
        tvUid.setText("UID: " + user.getUid());

        boolean verified = user.isEmailVerified();
        tvVerified.setText(verified ? "Correo verificado ✓" : "Correo no verificado ✗");

        // --- Foto del usuario ---
        Uri photoUri = user.getPhotoUrl();
        if (photoUri != null) {
            Picasso.get()
                    .load(photoUri)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .into(imgProfilePhoto);
        } else {
            imgProfilePhoto.setImageResource(R.drawable.ic_user);
        }

        // ---------------------------------------------------------
        //      BOTÓN "ENVIAR VERIFICACIÓN" (solo si NO está verificado)
        // ---------------------------------------------------------
        if (!verified) {
            btnSendVerifyEmail.setVisibility(View.VISIBLE);

            btnSendVerifyEmail.setOnClickListener(v -> {
                user.sendEmailVerification()
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(this,
                                        "Correo de verificación enviado.",
                                        Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this,
                                        "Error enviando email: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show());
            });
        } else {
            btnSendVerifyEmail.setVisibility(View.GONE);
        }
    }


    // ********************************************************************
    //   LOGOUT
    // ********************************************************************
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        AuthUI.getInstance().signOut(this); // Para cerrar sesión de Google
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
