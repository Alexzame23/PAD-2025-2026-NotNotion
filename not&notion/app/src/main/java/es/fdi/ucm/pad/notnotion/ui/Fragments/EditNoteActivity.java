package es.fdi.ucm.pad.notnotion.ui.Fragments;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.Note;

public class EditNoteActivity extends AppCompatActivity {
    private EditText etTitle, etContent;
    private Note note;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);

        note = (Note) getIntent().getSerializableExtra("note");

        if (note != null) {
            etTitle.setText(note.getTitle());
            etContent.setText(note.getContent());
        }

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            note.setTitle(etTitle.getText().toString());
            note.setContent(etContent.getText().toString());
            note.setUpdatedAt(com.google.firebase.Timestamp.now());

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("notes").document(note.getId())
                    .set(note, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Nota actualizada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    });
        });
    }

}
