package es.fdi.ucm.pad.notnotion.ui.user_logging;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.ui.main.MainActivity;
import es.fdi.ucm.pad.notnotion.ui.user_register.RegisterActivity;

public class LoginActivity extends AppCompatActivity {

    // 1) CREA EL LAUNCHER USANDO EL CONTRATO DE FIREBASEUI
    private EditText emailField, passwordField;
    private Button loginButton;
    private SignInButton googleButton;
    private FirebaseAuth mAuth;
    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(
                    new FirebaseAuthUIActivityResultContract(),
                    this::onSignInResult
            );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            goToMainActivity();
            return;
        }

        ImageButton btnLanguage = findViewById(R.id.btn_language);
        btnLanguage.setOnClickListener(v -> showLanguageDialog());

        TextView goToRegister = findViewById(R.id.new_user);
        goToRegister.setOnClickListener(v -> goToRegisterActivity());

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.log_in);
        googleButton = findViewById(R.id.btn_google);

        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce el correo y la contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            goToMainActivity();
                        } else {
                            Toast.makeText(this, "Error de autenticación: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        googleButton.setOnClickListener(v -> launchGoogleSignIn());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(es.fdi.ucm.pad.notnotion.utils.LocaleHelper.applyLocale(newBase));
    }
    private void launchGoogleSignIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_launcher_foreground) // opcional
                .setTheme(R.style.Base_Theme_Notnotion)     // opcional
                .setIsSmartLockEnabled(false)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        int resultCode = result.getResultCode();
        IdpResponse response = result.getIdpResponse();

        if (resultCode == RESULT_OK) {
            goToMainActivity();
        } else {
            if (response == null) {
                Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error: " + response.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish(); // opcional: para que no pueda volver al login con "atrás"
    }

    private void showLanguageDialog() {
        final String[] languages = {"Español", "English"};
        final String[] codes = {"es", "en"};

        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_language))
                .setItems(languages, (dialog, which) -> {
                    String selectedLang = codes[which];
                    es.fdi.ucm.pad.notnotion.utils.LocaleHelper.setLocale(this, selectedLang);
                    recreate(); // Recarga la actividad con el nuevo idioma
                })
                .show();
    }
}
