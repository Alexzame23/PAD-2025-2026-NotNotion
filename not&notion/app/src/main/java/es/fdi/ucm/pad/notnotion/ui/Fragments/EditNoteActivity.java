package es.fdi.ucm.pad.notnotion.ui.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.UUID;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.NotesManager;
import es.fdi.ucm.pad.notnotion.data.model.ContentBlock;
import es.fdi.ucm.pad.notnotion.data.model.Note;
import es.fdi.ucm.pad.notnotion.ui.views.TextEditorView;
import es.fdi.ucm.pad.notnotion.utils.ImageHelper;

// Activity encargada de editar notas comunicandose con MainActivity

public class EditNoteActivity extends AppCompatActivity {

    private static final String TAG = "EditNoteActivity";

    // Constantes para los extras del Intent
    public static final String EXTRA_NOTE = "extra_note";
    public static final String EXTRA_FOLDER_ID = "extra_folder_id";

    // Componentes de la interfaz
    private EditText etTitle;
    private FrameLayout coverContainer;
    private ImageView coverImage;
    private TextView btnAddCover;
    private TextEditorView textEditor;

    // Botones de formato
    private TextView btnBold;
    private TextView btnItalic;
    private FrameLayout btnUnderline;
    private TextView btnTextSize;

    // Botones de navegación
    private FloatingActionButton btnBack;
    private FloatingActionButton btnAddContent;

    // Datos de la nota
    private Note note;
    private String folderId;
    private Uri selectedCoverUri; // Imagen temporal de portada antes de subirla

    // Estado de los botones de formato
    private boolean isBoldActive = false;
    private boolean isItalicActive = false;
    private boolean isUnderlineActive = false;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private NotesManager notesManager;

    // Lanzadores de actividad para seleccionar archivos
    private ActivityResultLauncher<Intent> pickCoverLauncher;
    private ActivityResultLauncher<Intent> pickContentImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);

        Log.d(TAG, "EditNoteActivity iniciada");

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        notesManager = new NotesManager();

        // Obtener datos del Intent
        // MainActivity nos pasa la nota y el ID de carpeta por aquí
        if (!loadIntentData()) {
            // Si no pudimos cargar los datos, algo salió mal
            Toast.makeText(this, "Error: datos incompletos", Toast.LENGTH_SHORT).show();
            finish(); // Cerrar esta Activity y volver a MainActivity
            return;
        }

        // Inicializar vistas
        initializeViews();

        // Configurar lanzadores de archivos
        setupActivityLaunchers();

        // Cargar datos de la nota en la interfaz
        loadNoteData();

        // Configurar listeners de los botones
        setupListeners();
    }


     // Carga los datos que MainActivity nos pasó mediante el Intent.
    private boolean loadIntentData() {
        Intent intent = getIntent();

        // Intentar obtener la nota del Intent
        note = (Note) intent.getSerializableExtra(EXTRA_NOTE);
        folderId = intent.getStringExtra(EXTRA_FOLDER_ID);

        // Si no hay nota, crear una nueva vacía
        if (note == null) {
            Log.d(TAG, "No se recibió nota, creando una nueva");
            note = new Note();
            note.setContentBlocks(new ArrayList<>());
        }

        // folderId para saber dónde guardar
        if (folderId == null || folderId.isEmpty()) {
            Log.e(TAG, "No se recibió folderId");
            return false;
        }

        return true;
    }


    // Inicializa todas las referencias a las vistas del layout.
    private void initializeViews() {
        // Componentes principales
        etTitle = findViewById(R.id.etTitle);
        coverContainer = findViewById(R.id.coverContainer);
        coverImage = findViewById(R.id.coverImage);
        btnAddCover = findViewById(R.id.btnAddCover);
        textEditor = findViewById(R.id.richEditor);

        // Botones de formato
        btnBold = findViewById(R.id.btnBold);
        btnItalic = findViewById(R.id.btnItalic);
        btnUnderline = findViewById(R.id.btnUnderline);
        btnTextSize = findViewById(R.id.btnTextSize);

        // Botones de navegación
        btnBack = findViewById(R.id.btnBack);
        btnAddContent = findViewById(R.id.btnAddContent);
    }


    // Configura los lanzadores para seleccionar archivos
    private void setupActivityLaunchers() {
        // Lanzador para seleccionar imagen de PORTADA
        pickCoverLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();

                        Log.d(TAG, "PORTADA: Imagen seleccionada");
                        Log.d(TAG, "URI: " + selectedUri);

                        // Convertir a Base64
                        String base64Image = ImageHelper.convertImageToBase64(this, selectedUri);

                        if (base64Image != null) {
                            // Guardar el Base64
                            note.setCoverImageUrl(base64Image);

                            // Mostrar la imagen inmediatamente en la interfaz
                            coverImage.setVisibility(View.VISIBLE);
                            btnAddCover.setVisibility(View.GONE);

                            // Convertir Base64 a Bitmap para mostrar
                            Bitmap bitmap = ImageHelper.convertBase64ToBitmap(base64Image);
                            if (bitmap != null) {
                                coverImage.setImageBitmap(bitmap);
                                Log.d(TAG, "Portada cargada exitosamente");
                            }
                        } else {
                            Toast.makeText(this, "Error al procesar la imagen",
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error al convertir imagen a Base64");
                        }
                    }
                }
        );

        // Lanzador para añadir imágenes al CONTENIDO
        pickContentImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();

                        Log.d(TAG, "CONTENIDO: Imagen seleccionada");
                        Log.d(TAG, "URI: " + selectedUri);

                        // Mostrar diálogo de progreso
                        Toast.makeText(this, "Procesando imagen...", Toast.LENGTH_SHORT).show();

                        // Convertir a Base64
                        String base64Image = ImageHelper.convertImageToBase64(this, selectedUri);

                        if (base64Image != null) {
                            // Añadir al editor
                            textEditor.addImageBlock(base64Image);
                            Toast.makeText(this, "Imagen añadida", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Imagen añadida al contenido");
                        } else {
                            Toast.makeText(this, "Error al procesar la imagen",
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error al convertir imagen a Base64");
                        }
                    }
                }
        );

        /*// Lanzador para añadir imágenes al contenido
        pickContentImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();

                        Log.d(TAG, "Imagen de contenido seleccionada, subiendo...");

                        // Subir la imagen a Firebase Storage
                        uploadImageToStorage(imageUri, url -> {
                            // Una vez subida, añadir el bloque al editor
                            textEditor.addImageBlock(url);
                            Toast.makeText(this, "Imagen añadida", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Imagen subida y añadida al editor: " + url);
                        });
                    }
                }
        );*/
    }


    // Carga los datos de la nota en la interfaz
    private void loadNoteData() {
        // Cargar título
        if (note.getTitle() != null) {
            etTitle.setText(note.getTitle());
        }

        // Cargar imagen de portada si existe
        if (note.getCoverImageUrl() != null && !note.getCoverImageUrl().isEmpty()) {
            coverImage.setVisibility(View.VISIBLE);
            btnAddCover.setVisibility(View.GONE);

            // Convertir Base64 a Bitmap
            Bitmap bitmap = ImageHelper.convertBase64ToBitmap(note.getCoverImageUrl());
            if (bitmap != null) {
                coverImage.setImageBitmap(bitmap);
                Log.d(TAG, "✓ Portada cargada desde Base64");
            } else {
                Log.e(TAG, "✗ Error al decodificar portada");
            }
        }

        // Cargar contenido en el editor
        if (note.getContentBlocks() != null && !note.getContentBlocks().isEmpty()) {
            textEditor.loadContent(note.getContentBlocks());
        } else {
            textEditor.loadContent(new ArrayList<>());
        }
    }

    // Configura todos los listeners de los botones.
    private void setupListeners() {

        // IMAGEN DE PORTADA(No func)
        coverContainer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickCoverLauncher.launch(intent);
        });


        // Botones de estilos
        btnBold.setOnClickListener(v -> {
            isBoldActive = !isBoldActive;
            updateButtonState(btnBold, isBoldActive);
            updateTextStyle();
            Log.d(TAG, "Bold toggled: " + isBoldActive);
        });

        btnItalic.setOnClickListener(v -> {
            isItalicActive = !isItalicActive;
            updateButtonState(btnItalic, isItalicActive);
            updateTextStyle();
            Log.d(TAG, "Italic toggled: " + isItalicActive);
        });

        btnUnderline.setOnClickListener(v -> {
            isUnderlineActive = !isUnderlineActive;

            if (isUnderlineActive) {
                btnUnderline.setBackgroundColor(getResources().getColor(R.color.light_yellow));
            } else {
                btnUnderline.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }

            updateTextStyle();
            Log.d(TAG, "Underline toggled: " + isUnderlineActive);
        });

        btnTextSize.setOnClickListener(v -> showTextSizeDialog());


        // Boton para añadir contenido
        btnAddContent.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, btnAddContent);
            popup.getMenu().add(0, 1, 0, "Añadir imagen");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickContentImageLauncher.launch(intent);
                    return true;
                }
                return false;
            });

            popup.show();
        });


        // Boton volver
        btnBack.setOnClickListener(v -> {
            saveNoteAndFinish();
        });
    }

    // Actualiza el estilo del texto según los botones activos
    private void updateTextStyle() {
        int style = ContentBlock.STYLE_NORMAL;

        // Determinar el estilo basándose en las combinaciones activas
        if (isBoldActive && isItalicActive && isUnderlineActive) {
            style = ContentBlock.STYLE_BOLD_ITALIC_UNDERLINE;
        } else if (isBoldActive && isItalicActive) {
            style = ContentBlock.STYLE_BOLD_ITALIC;
        } else if (isBoldActive && isUnderlineActive) {
            style = ContentBlock.STYLE_BOLD_UNDERLINE;
        } else if (isItalicActive && isUnderlineActive) {
            style = ContentBlock.STYLE_ITALIC_UNDERLINE;
        } else if (isBoldActive) {
            style = ContentBlock.STYLE_BOLD;
        } else if (isItalicActive) {
            style = ContentBlock.STYLE_ITALIC;
        } else if (isUnderlineActive) {
            style = ContentBlock.STYLE_UNDERLINE;
        }

        textEditor.setCurrentTextStyle(style);
    }

    // Actualiza visualmente el estado de un botón de formato
    private void updateButtonState(TextView button, boolean active) {
        if (active) {
            button.setBackgroundColor(getResources().getColor(R.color.light_yellow));
        } else {
            button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }


    // Muestra un diálogo para seleccionar el tamaño del texto
    private void showTextSizeDialog() {
        String[] sizes = {"12sp", "14sp", "16sp", "18sp", "20sp", "24sp", "28sp", "32sp"};
        int[] sizeValues = {12, 14, 16, 18, 20, 24, 28, 32};

        new AlertDialog.Builder(this)
                .setTitle("Tamaño de texto")
                .setItems(sizes, (dialog, which) -> {
                    textEditor.setCurrentTextSize(sizeValues[which]);
                    btnTextSize.setText(sizes[which]);
                    Log.d(TAG, "Tamaño de texto cambiado a: " + sizes[which]);
                })
                .show();
    }

    // Sube una imagen a Firebase Storage y devuelve la URL mediante un callback (No func)
    /*private void uploadImageToStorage(Uri imageUri, OnUploadCompleteListener listener) {
        // Generar un nombre único para la imagen
        String filename = "images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(filename);

        Log.d(TAG, "Iniciando subida de imagen: " + filename);

        // Subir el archivo
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Imagen subida exitosamente");

                    // Obtener la URL de descarga
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d(TAG, "URL de imagen obtenida: " + uri.toString());
                        listener.onUploadComplete(uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al subir imagen", e);
                    Toast.makeText(this, "Error al subir imagen: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }*/


    // Guarda la nota y cierra la Activity
    private void saveNoteAndFinish() {
        Log.d(TAG, "Iniciando guardado de nota");

        // Verificar que hay un usuario autenticado
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Error: usuario no logueado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Validar que hay un título
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show();
            etTitle.requestFocus(); // Poner el cursor en el campo de título
            return;
        }

        // Actualizar los datos de la nota
        note.setTitle(title);
        note.setContentBlocks(textEditor.getContentBlocks());
        note.setFolderId(folderId);
        note.setUpdatedAt(Timestamp.now());

        // Si hay una nueva imagen de portada, subirla primero
        /*if (selectedCoverUri != null) {
            Log.d(TAG, "Subiendo nueva imagen de portada");
            uploadImageToStorage(selectedCoverUri, url -> {
                note.setCoverImageUrl(url);
                saveToFirestore(user.getUid());
            });
        } else {
            // Si no hay imagen nueva, guardar directamente
            saveToFirestore(user.getUid());
        }*/
        saveToFirestore(user.getUid());
    }


    // Guarda la nota en Firestore.
    private void saveToFirestore(String userId) {
        String noteId = note.getId();

        if (noteId == null || noteId.isEmpty()) {
            // Crea nueva nota
            Log.d(TAG, "Creando nueva nota");

            // Generar ID para la nota nueva
            String newNoteId = UUID.randomUUID().toString();
            note.setId(newNoteId);
            note.setCreatedAt(Timestamp.now());

            // Guardar en Firestore
            db.collection("users")
                    .document(userId)
                    .collection("folders")
                    .document(folderId)
                    .collection("notes")
                    .document(newNoteId)
                    .set(note)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Nota creada exitosamente: " + note.getId());
                        Toast.makeText(this, "Nota creada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al crear nota", e);
                        Toast.makeText(this, "Error al crear nota: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });

        } else {
            // Actualiza nota existente
            Log.d(TAG, "Actualizando nota existente: " + noteId);

            db.collection("users")
                    .document(userId)
                    .collection("folders")
                    .document(folderId)
                    .collection("notes")
                    .document(noteId)
                    .set(note, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Nota actualizada exitosamente");
                        Toast.makeText(this, "Nota guardada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al guardar nota", e);
                        Toast.makeText(this, "Error al guardar: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Interfaz para el callback de subida de imágenes (No func)
    private interface OnUploadCompleteListener {
        void onUploadComplete(String url);
    }
}