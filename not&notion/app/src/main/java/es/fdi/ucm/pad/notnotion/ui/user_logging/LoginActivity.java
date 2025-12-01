package es.fdi.ucm.pad.notnotion.ui.user_logging;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.FirebaseFirestoreManager;
import es.fdi.ucm.pad.notnotion.ui.main.MainActivity;
import es.fdi.ucm.pad.notnotion.ui.user_register.RegisterActivity;
import es.fdi.ucm.pad.notnotion.utils.LocaleHelper;

public class LoginActivity extends AppCompatActivity {

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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            initializeUserInFirestore(currentUser, () -> {
                goToMainActivity();
            });
            return;
        }
        ImageButton btnLanguage = findViewById(R.id.btn_language);
        btnLanguage.setOnClickListener(v -> showLanguageDialog());

        TextView goToRegister = findViewById(R.id.new_user);
        goToRegister.setOnClickListener(v -> {
            checkInternetConnection(
                    () -> goToRegisterActivity(),
                    () -> Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_LONG).show()
            );
        });

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.log_in);
        googleButton = findViewById(R.id.btn_google);

        // LOGIN EMAIL/PASSWORD
        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce el correo y la contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            checkInternetConnection(
                    () -> {
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                        initializeUserInFirestore(firebaseUser, this::goToMainActivity);
                                    } else {
                                        Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    },

                    () -> Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_LONG).show()
            );
        });

        googleButton.setOnClickListener(v ->
                checkInternetConnection(
                        this::launchGoogleSignIn,
                        () -> Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_LONG).show()
                )
        );
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(es.fdi.ucm.pad.notnotion.utils.LocaleHelper.applyLocale(newBase));
    }




    // GOOGLE LOGIN
    private void launchGoogleSignIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_launcher_foreground)
                .setTheme(R.style.Base_Theme_Notnotion)
                .setIsSmartLockEnabled(false)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        int resultCode = result.getResultCode();
        IdpResponse response = result.getIdpResponse();

        if (resultCode == RESULT_OK) {
            FirebaseUser firebaseUser = mAuth.getCurrentUser();

            initializeUserInFirestore(firebaseUser, () -> {
                goToMainActivity();
            });
        } else {
            if (response == null) {
                Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error: " + response.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeUserInFirestore(FirebaseUser firebaseUser, Runnable onComplete) {

        if (firebaseUser == null) {
            onComplete.run();
            return;
        }

        FirebaseFirestoreManager ffm = new FirebaseFirestoreManager();

        ffm.getCurrentUserData(userData -> {

            if (userData == null) {
                Log.w("InitUser", "Usuario Firestore NO existe → creando estructura...");

                ffm.initializeUserStructure(firebaseUser, () -> {
                    Log.d("InitUser", "Estructura creada → continuando");
                    onComplete.run();
                });

            } else {
                Log.i("InitUser", "Usuario Firestore YA existe");
                onComplete.run();
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
    private void showLanguageDialog() {
        final String[] languages = {"Español", "English"};
        final String[] codes = {"es", "en"};

        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_language))
                .setItems(languages, (dialog, which) -> {
                    String selectedLang = codes[which];
                    es.fdi.ucm.pad.notnotion.utils.LocaleHelper.setLocale(this, selectedLang);
                    recreate();
                })
                .show();
    }



    private void checkInternetConnection(Runnable onSuccess, Runnable onTimeout) {
        new Thread(() -> {
            try {
                java.net.InetAddress ipAddr = java.net.InetAddress.getByName("8.8.8.8");
                if (!ipAddr.equals("")) {
                    runOnUiThread(onSuccess);
                }
            } catch (Exception e) {
                runOnUiThread(onTimeout);
            }
        }).start();

        new android.os.Handler(getMainLooper()).postDelayed(onTimeout, 5000);
    }
}
