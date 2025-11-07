package es.fdi.ucm.pad.notnotion.ui.user_register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.ui.main.MainActivity;
import es.fdi.ucm.pad.notnotion.ui.user_logging.LoginActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput, confirmInput;
    private Button registerButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        mAuth = FirebaseAuth.getInstance();

        // Referencias UI
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmInput = findViewById(R.id.confirm_input);
        registerButton = findViewById(R.id.btn_register);

        TextView goToLogin = findViewById(R.id.tv_go_to_login);
        goToLogin.setOnClickListener(v -> goToLoginActivity());

        ImageButton btnLanguage = findViewById(R.id.btn_language);
        btnLanguage.setOnClickListener(v -> showLanguageDialog());

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirm = confirmInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // Intentamos crear el usuario con FirebaseAuth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro correcto
                        FirebaseUser user = mAuth.getCurrentUser();

                        // (Opcional) guardar el nombre en el perfil
                        if (user != null) {
                            user.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build());
                        }

                        Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Ya existe una cuenta con ese correo", Toast.LENGTH_LONG).show();
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Correo o contraseña no válidos (mínimo 6 caracteres)", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al crear la cuenta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                });
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(es.fdi.ucm.pad.notnotion.utils.LocaleHelper.applyLocale(newBase));
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
}
