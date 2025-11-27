package es.fdi.ucm.pad.notnotion.ui.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.utils.ImageHelper;

public class ProfileEditActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private ImageView imgProfilePhotoEdit;
    private Button btnChangePhoto, btnSaveChanges;
    private EditText etName, etCurrentPassword, etNewPassword, etRepeatPassword;

    private String profileBase64 = null; // FOTO EN BASE64

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI
        imgProfilePhotoEdit = findViewById(R.id.imgProfilePhotoEdit);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        etName = findViewById(R.id.etName);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etRepeatPassword = findViewById(R.id.etRepeatPassword);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = user.getUid();

        // Cargar nombre y foto desde Firestore
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {

                        // Nombre
                        if (doc.getString("username") != null) {
                            etName.setText(doc.getString("username"));
                        }

                        // FOTO Base64
                        if (doc.getString("photoBase64") != null) {
                            profileBase64 = doc.getString("photoBase64");

                            Bitmap bmp = ImageHelper.convertBase64ToBitmap(profileBase64);
                            if (bmp != null) imgProfilePhotoEdit.setImageBitmap(bmp);
                        } else {
                            imgProfilePhotoEdit.setImageResource(R.drawable.ic_user);
                        }
                    }
                });

        // Botón cambiar foto
        btnChangePhoto.setOnClickListener(v -> openImagePicker());

        // Guardar
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    // ============================================
    // SELECCIÓN DE IMAGEN
    // ============================================

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            // Convertir la imagen a Base64
            profileBase64 = ImageHelper.convertImageToBase64(this, uri);

            // Mostrar imagen
            Bitmap bmp = ImageHelper.convertBase64ToBitmap(profileBase64);
            imgProfilePhotoEdit.setImageBitmap(bmp);
        }
    }

    // ============================================
    // GUARDADO DE CAMBIOS
    // ============================================

    private void saveChanges() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String newName = etName.getText().toString().trim();
        String currentPass = etCurrentPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String repeatPass = etRepeatPassword.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean wantsPassword =
                !currentPass.isEmpty() || !newPass.isEmpty() || !repeatPass.isEmpty();

        if (wantsPassword) {

            if (currentPass.isEmpty() || newPass.isEmpty() || repeatPass.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos de contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(repeatPass)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(this, "Mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            reauthenticateAndUpdate(user, newName, currentPass, newPass);

        } else {
            updateProfileData(user, newName, null);
        }
    }

    // ============================================
    // REAUTENTICACIÓN
    // ============================================

    private void reauthenticateAndUpdate(FirebaseUser user, String newName,
                                         String currentPass, String newPass) {

        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), currentPass))
                .addOnSuccessListener(unused -> updateProfileData(user, newName, newPass))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
                );
    }

    // ============================================
    // ACTUALIZAR FIRESTORE + AUTH
    // ============================================

    private void updateProfileData(FirebaseUser user, String newName, @Nullable String newPass) {

        String uid = user.getUid();

        // 1) Actualizar Auth SOLO el displayName
        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName);

        // FirebaseAuth NO soporta Base64 → ponemos la foto a null
        builder.setPhotoUri(null);

        user.updateProfile(builder.build());

        // 2) Actualizar Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newName);

        if (profileBase64 != null) updates.put("photoBase64", profileBase64);

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {

                    if (newPass != null && !newPass.isEmpty()) {

                        user.updatePassword(newPass)
                                .addOnSuccessListener(unused2 -> {
                                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error al cambiar contraseña", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error actualizando Firestore", Toast.LENGTH_SHORT).show());
    }
}
