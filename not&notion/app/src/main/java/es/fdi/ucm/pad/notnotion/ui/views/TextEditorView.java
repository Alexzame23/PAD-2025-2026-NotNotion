package es.fdi.ucm.pad.notnotion.ui.views;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.widget.Toast;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.ContentBlock;
import es.fdi.ucm.pad.notnotion.utils.ImageHelper;

// Vista para el editor de texto, splannable para aplicar en el momento
public class TextEditorView extends LinearLayout {

    private static final String TAG = "TextEditorView";

    private Context context;
    private EditText mainEditor;

    // Estado del estilo actual
    private int currentTextStyle = ContentBlock.STYLE_NORMAL;
    private int currentTextSize = 16;

    // Posición donde empezó el estilo actual
    private int styleStartPosition = 0;

    // Flag para evitar bucles infinitos
    private boolean isUpdatingText = false;

    // Rastrear spans activos que se están extendiendo
    private List<Object> activeSpans = new ArrayList<>();

    // Imágenes insertadas (Base64)
    private List<String> insertedImages = new ArrayList<>();

    // PDFs insertados (guardamos uriString)
    private List<String> insertedPdfs = new ArrayList<>();

    public TextEditorView(Context context) {
        super(context);
        init(context);
    }

    public TextEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context ctx) {
        this.context = ctx;
        setOrientation(VERTICAL);
        setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        if (insertedImages == null) {
            insertedImages = new ArrayList<>();
        }
        if (insertedPdfs == null) {
            insertedPdfs = new ArrayList<>();
        }

        createMainEditor();
    }

    private void createMainEditor() {
        mainEditor = new EditText(context);
        mainEditor.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        mainEditor.setBackground(null);
        mainEditor.setTextColor(context.getResources().getColor(R.color.black));
        mainEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSize);
        mainEditor.setHint("Escribe tu nota aquí...");

        // Necesario para que ClickableSpans funcionen en EditText
        mainEditor.setMovementMethod(LinkMovementMethod.getInstance());
        mainEditor.setLinksClickable(true);

        mainEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isUpdatingText) {
                    return;
                }

                int currentPosition = mainEditor.getSelectionStart();

                if (currentPosition > styleStartPosition) {
                    applyStyleToRange(editable, styleStartPosition, currentPosition);
                }
            }
        });

        addView(mainEditor);
    }

    // Aplica el estilo actual a un rango de texto
    private void applyStyleToRange(Editable editable, int start, int end) {
        if (start >= end || start < 0 || end > editable.length()) {
            return;
        }

        isUpdatingText = true;

        // NO limpiar spans - solo extender los activos
        applyCurrentStyle(editable, start, end);

        isUpdatingText = false;
    }

    // Aplica el estilo actual al rango especificadp
    private void applyCurrentStyle(Editable editable, int start, int end) {
        // Aplicar tamaño
        if (currentTextSize != 16) {
            AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(currentTextSize, true);
            editable.setSpan(sizeSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            activeSpans.add(sizeSpan);
        }

        // Aplicar estilos de texto
        switch (currentTextStyle) {
            case ContentBlock.STYLE_BOLD:
                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                editable.setSpan(boldSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(boldSpan);
                break;

            case ContentBlock.STYLE_ITALIC:
                StyleSpan italicSpan = new StyleSpan(Typeface.ITALIC);
                editable.setSpan(italicSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(italicSpan);
                break;

            case ContentBlock.STYLE_BOLD_ITALIC:
                StyleSpan boldItalicSpan = new StyleSpan(Typeface.BOLD_ITALIC);
                editable.setSpan(boldItalicSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(boldItalicSpan);
                break;

            case ContentBlock.STYLE_UNDERLINE:
                UnderlineSpan underlineSpan = new UnderlineSpan();
                editable.setSpan(underlineSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(underlineSpan);
                break;

            case ContentBlock.STYLE_BOLD_UNDERLINE:
                StyleSpan boldSpan2 = new StyleSpan(Typeface.BOLD);
                UnderlineSpan underlineSpan2 = new UnderlineSpan();
                editable.setSpan(boldSpan2, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                editable.setSpan(underlineSpan2, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(boldSpan2);
                activeSpans.add(underlineSpan2);
                break;

            case ContentBlock.STYLE_ITALIC_UNDERLINE:
                StyleSpan italicSpan2 = new StyleSpan(Typeface.ITALIC);
                UnderlineSpan underlineSpan3 = new UnderlineSpan();
                editable.setSpan(italicSpan2, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                editable.setSpan(underlineSpan3, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(italicSpan2);
                activeSpans.add(underlineSpan3);
                break;

            case ContentBlock.STYLE_BOLD_ITALIC_UNDERLINE:
                StyleSpan boldItalicSpan2 = new StyleSpan(Typeface.BOLD_ITALIC);
                UnderlineSpan underlineSpan4 = new UnderlineSpan();
                editable.setSpan(boldItalicSpan2, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                editable.setSpan(underlineSpan4, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(boldItalicSpan2);
                activeSpans.add(underlineSpan4);
                break;
        }
    }

    // Cierra los spans activos convirtiéndolos de INCLUSIVE a EXCLUSIVE
    private void closeActiveSpans() {
        if (activeSpans.isEmpty()) {
            return;
        }

        Editable editable = mainEditor.getText();
        int currentPosition = mainEditor.getSelectionStart();

        isUpdatingText = true;

        for (Object span : activeSpans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);

            if (spanStart >= 0 && spanEnd >= 0) {
                editable.removeSpan(span);

                if (span instanceof StyleSpan) {
                    StyleSpan styleSpan = (StyleSpan) span;
                    StyleSpan newSpan = new StyleSpan(styleSpan.getStyle());
                    editable.setSpan(newSpan, spanStart, currentPosition,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (span instanceof UnderlineSpan) {
                    UnderlineSpan newSpan = new UnderlineSpan();
                    editable.setSpan(newSpan, spanStart, currentPosition,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (span instanceof AbsoluteSizeSpan) {
                    AbsoluteSizeSpan oldSpan = (AbsoluteSizeSpan) span;
                    AbsoluteSizeSpan newSpan = new AbsoluteSizeSpan(oldSpan.getSize(), true);
                    editable.setSpan(newSpan, spanStart, currentPosition,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        activeSpans.clear();
        isUpdatingText = false;

        Log.d(TAG, "Spans cerrados en posición: " + currentPosition);
    }

    // Cambia el estilo actual
    public void setCurrentTextStyle(int style) {
        closeActiveSpans();

        this.currentTextStyle = style;
        this.styleStartPosition = mainEditor.getSelectionStart();

        Log.d(TAG, "Estilo cambiado a: " + style + " en posición: " + styleStartPosition);
    }

    // Cambia el tamaño actual
    public void setCurrentTextSize(int size) {
        closeActiveSpans();

        this.currentTextSize = size;
        this.styleStartPosition = mainEditor.getSelectionStart();

        Log.d(TAG, "Tamaño cambiado a: " + size + " en posición: " + styleStartPosition);
    }

    public int getCurrentTextStyle() {
        return currentTextStyle;
    }

    public int getCurrentTextSize() {
        return currentTextSize;
    }


    // Carga contenido desde una lista de bloques
    public void loadContent(List<ContentBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            mainEditor.setText("");
            insertedImages.clear();
            insertedPdfs.clear();
            // eliminar views entre mainEditor si las hubiera (para evitar duplicados)
            removeAllNonEditorViews();
            return;
        }

        // limpiar vistas previas y listas
        removeAllNonEditorViews();
        insertedImages.clear();
        insertedPdfs.clear();

        SpannableStringBuilder textBuilder = new SpannableStringBuilder();

        for (ContentBlock block : blocks) {
            if (block.getType() == ContentBlock.TYPE_TEXT) {
                // Cargar TEXTO
                String text = block.getTextContent();
                if (text == null || text.isEmpty()) {
                    continue;
                }

                int start = textBuilder.length();
                textBuilder.append(text);
                int end = textBuilder.length();

                applyStyleToBlock(textBuilder, start, end, block.getTextStyle(), block.getTextSize());

            } else if (block.getType() == ContentBlock.TYPE_IMAGE) {
                // Cargar IMAGEN: se añade como vista separada
                String base64Image = block.getMediaUrl();
                if (base64Image != null && !base64Image.isEmpty()) {
                    addImageBlock(base64Image);
                    Log.d(TAG, "Imagen cargada desde ContentBlock");
                }
            } else if (block.getType() == ContentBlock.TYPE_PDF) {
                // Cargar PDF: se añade como vista separada (bloque)
                String uriString = block.getMediaUrl();
                if (uriString != null && !uriString.isEmpty()) {
                    // Intentamos derivar un displayName razonable
                    String displayName = "documento";
                    try {
                        Uri u = Uri.parse(uriString);
                        String last = u.getLastPathSegment();
                        if (last != null && !last.isEmpty()) displayName = last;
                    } catch (Exception e) { /* ignore */ }

                    addDocumentView(displayName, uriString, "*/*");
                    Log.d(TAG, "PDF cargado desde ContentBlock: " + uriString);
                }
            }
        }

        isUpdatingText = true;
        mainEditor.setText(textBuilder);
        // Aseguramos movement method para que los ClickableSpans funcionen (aunque ahora los PDFs son bloques)
        mainEditor.setMovementMethod(LinkMovementMethod.getInstance());
        mainEditor.setSelection(mainEditor.getText().length());
        isUpdatingText = false;

        Log.d(TAG, "Contenido cargado: " + blocks.size() + " bloques");
    }

    // Helper para eliminar vistas que no sean el mainEditor (imágenes/pdf) antes de recargar
    private void removeAllNonEditorViews() {
        // iterar de atrás hacia delante para evitar problemas con índices
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child == mainEditor) continue;
            removeViewAt(i);
        }
    }

    // Aplica estilo a un bloque específico al cargar
    private void applyStyleToBlock(SpannableStringBuilder builder, int start, int end,
                                   int style, int size) {
        if (size != 16) {
            builder.setSpan(new AbsoluteSizeSpan(size, true), start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        switch (style) {
            case ContentBlock.STYLE_BOLD:
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case ContentBlock.STYLE_ITALIC:
                builder.setSpan(new StyleSpan(Typeface.ITALIC), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case ContentBlock.STYLE_BOLD_ITALIC:
                builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case ContentBlock.STYLE_UNDERLINE:
                builder.setSpan(new UnderlineSpan(), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case ContentBlock.STYLE_BOLD_UNDERLINE:
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new UnderlineSpan(), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case ContentBlock.STYLE_ITALIC_UNDERLINE:
                builder.setSpan(new StyleSpan(Typeface.ITALIC), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new UnderlineSpan(), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case ContentBlock.STYLE_BOLD_ITALIC_UNDERLINE:
                builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new UnderlineSpan(), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
        }
    }

    //Obtiene el contenido como lista de bloques
    public List<ContentBlock> getContentBlocks() {
        List<ContentBlock> blocks = new ArrayList<>();

        Editable editable = mainEditor.getText();
        String text = editable.toString();

        // Añadir bloques de TEXTO (igual que antes)
        if (!text.trim().isEmpty()) {
            int length = text.length();
            int currentPos = 0;

            while (currentPos < length) {
                int nextChange = findNextStyleChange(editable, currentPos);
                if (nextChange <= currentPos) nextChange = currentPos + 1;

                String segmentText = text.substring(currentPos, Math.min(nextChange, text.length()));
                if (!segmentText.isEmpty()) {
                    int style = getStyleAtPosition(editable, currentPos);
                    int size = getSizeAtPosition(editable, currentPos);

                    // Evitar guardar sólo saltos de línea vacíos
                    if (!segmentText.trim().isEmpty()) {
                        blocks.add(ContentBlock.createTextBlock(segmentText, style, size));
                        Log.d(TAG, "Bloque de texto guardado: '" + segmentText + "' estilo=" + style);
                    }
                }

                currentPos = nextChange;
            }
        }

        // Añadir bloques de IMÁGENES (se mantienen como lo tenías)
        for (String base64Image : insertedImages) {
            blocks.add(ContentBlock.createImageBlock(base64Image));
            Log.d(TAG, "Bloque de imagen guardado (Base64 length: " + base64Image.length() + ")");
        }

        // Añadir bloques de PDF (ahora se guardan como bloques separados)
        for (String uriString : insertedPdfs) {
            blocks.add(ContentBlock.createPdfBlock(uriString));
            Log.d(TAG, "Bloque de PDF guardado: " + uriString);
        }

        Log.d(TAG, "Total bloques guardados: " + blocks.size() +
                " (" + (blocks.size() - insertedImages.size() - insertedPdfs.size()) + " texto, " +
                insertedImages.size() + " imágenes, " + insertedPdfs.size() + " pdfs)");
        return blocks;
    }

    private int findNextStyleChange(Editable editable, int start) {
        int length = editable.length();
        Object[] spans = editable.getSpans(start, length, Object.class);

        int nearestChange = length;

        for (Object span : spans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);

            if (spanStart > start && spanStart < nearestChange) {
                nearestChange = spanStart;
            }

            if (spanEnd > start && spanEnd < nearestChange) {
                nearestChange = spanEnd;
            }
        }

        return nearestChange;
    }

    private int getStyleAtPosition(Editable editable, int position) {
        if (position >= editable.length()) {
            return ContentBlock.STYLE_NORMAL;
        }

        StyleSpan[] styleSpans = editable.getSpans(position, position + 1, StyleSpan.class);
        UnderlineSpan[] underlineSpans = editable.getSpans(position, position + 1, UnderlineSpan.class);

        boolean isBold = false;
        boolean isItalic = false;
        boolean isUnderline = underlineSpans.length > 0;

        for (StyleSpan span : styleSpans) {
            int style = span.getStyle();
            if (style == Typeface.BOLD) {
                isBold = true;
            } else if (style == Typeface.ITALIC) {
                isItalic = true;
            } else if (style == Typeface.BOLD_ITALIC) {
                isBold = true;
                isItalic = true;
            }
        }

        if (isBold && isItalic && isUnderline) {
            return ContentBlock.STYLE_BOLD_ITALIC_UNDERLINE;
        } else if (isBold && isItalic) {
            return ContentBlock.STYLE_BOLD_ITALIC;
        } else if (isBold && isUnderline) {
            return ContentBlock.STYLE_BOLD_UNDERLINE;
        } else if (isItalic && isUnderline) {
            return ContentBlock.STYLE_ITALIC_UNDERLINE;
        } else if (isBold) {
            return ContentBlock.STYLE_BOLD;
        } else if (isItalic) {
            return ContentBlock.STYLE_ITALIC;
        } else if (isUnderline) {
            return ContentBlock.STYLE_UNDERLINE;
        } else {
            return ContentBlock.STYLE_NORMAL;
        }
    }

    private int getSizeAtPosition(Editable editable, int position) {
        if (position >= editable.length()) {
            return 16;
        }

        AbsoluteSizeSpan[] sizeSpans = editable.getSpans(position, position + 1, AbsoluteSizeSpan.class);

        if (sizeSpans.length > 0) {
            return sizeSpans[0].getSize();
        }

        return 16;
    }


    // Añade un bloque de imagen desde Base64 con opción de eliminar
    public void addImageBlock(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            Log.e(TAG, "Base64 de imagen vacío");
            return;
        }

        // Guardar en la lista
        insertedImages.add(base64Image);

        // Crear contenedor
        LinearLayout imageContainer = new LinearLayout(context);
        imageContainer.setOrientation(VERTICAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 16, 0, 16);
        imageContainer.setLayoutParams(containerParams);

        // Guardar el Base64 en el TAG para identificar esta imagen
        imageContainer.setTag(base64Image);

        // Crear ImageView
        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                600
        );
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setAdjustViewBounds(true);

        // Convertir Base64 a Bitmap
        Bitmap bitmap = ImageHelper.convertBase64ToBitmap(base64Image);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            Log.d(TAG, "Imagen cargada desde Base64");
        } else {
            Log.e(TAG, "Error al decodificar Base64");
            imageView.setImageResource(R.drawable.icon_note);
        }

        imageContainer.addView(imageView);

        // Click largo para borrar
        imageContainer.setOnLongClickListener(v -> {
            showDeleteImageDialog(imageContainer);
            return true; // Consumir el evento
        });

        // Feedback visual: cambiar fondo al presionar
        imageContainer.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    imageContainer.setAlpha(0.7f); // Oscurecer al presionar
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    imageContainer.setAlpha(1.0f); // Restaurar
                    break;
            }
            return false; // No consumir, dejar que el LongClick funcione
        });

        // Añadir antes del mainEditor
        int insertPosition = indexOfChild(mainEditor);
        addView(imageContainer, insertPosition);

        Log.d(TAG, "Imagen añadida. Total: " + insertedImages.size());
    }


    // Muestra diálogo de confirmación para eliminar una imagen
    private void showDeleteImageDialog(View imageContainer) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar imagen")
                .setMessage("¿Estás seguro de que quieres eliminar esta imagen?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Obtener el Base64 del TAG
                    String base64ToRemove = (String) imageContainer.getTag();

                    if (base64ToRemove != null) {
                        // Eliminar de la lista
                        boolean removed = insertedImages.remove(base64ToRemove);

                        if (removed) {
                            Log.d(TAG, "✓ Imagen eliminada de la lista");
                        } else {
                            Log.w(TAG, "⚠️ Imagen no encontrada en la lista");
                        }

                        // Eliminar de la vista
                        removeView(imageContainer);

                        Log.d(TAG, "Total imágenes restantes: " + insertedImages.size());

                        // Feedback al usuario
                        Toast.makeText(context,
                                "Imagen eliminada",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Inserta un bloque interactivo para documento: ahora COMO BLOQUE (opción B)
    public void addDocumentBlock(String displayName, String uriString, String mimeType) {
        // Esta función se usa cuando el usuario añade un documento en tiempo de ejecución.
        // Se crea una vista tipo bloque y se añade antes del editor.
        if (displayName == null) displayName = "documento";
        if (uriString == null) return;
        if (mimeType == null) mimeType = "*/*";

        addDocumentView(displayName, uriString, mimeType);
    }

    // Añade la vista de documento (bloque separado) y guarda la uri en insertedPdfs
    private void addDocumentView(String displayName, String uriString, String mimeType) {
        // Guardar en la lista
        insertedPdfs.add(uriString);

        // Contenedor horizontal
        LinearLayout docContainer = new LinearLayout(context);
        docContainer.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 12, 0, 12);
        docContainer.setLayoutParams(containerParams);
        docContainer.setPadding(12, 12, 12, 12);
        docContainer.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        // Tag para identificar
        docContainer.setTag(uriString);

        // Icono (usar ic_pdf si existe; sino tu icono por defecto)
        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                96,
                96
        );
        iconParams.gravity = Gravity.CENTER_VERTICAL;
        icon.setLayoutParams(iconParams);
        // intenta usar ic_pdf, si no existe usa icon_note
        int pdfIconRes = getResIdOrFallback("ic_pdf", "drawable", R.drawable.icon_note);
        icon.setImageResource(pdfIconRes);
        docContainer.addView(icon);

        // Texto con nombre de documento
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tvParams.gravity = Gravity.CENTER_VERTICAL;
        tvParams.setMargins(16, 0, 0, 0);
        tv.setLayoutParams(tvParams);
        tv.setText(displayName);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tv.setMaxLines(2);
        tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        docContainer.addView(tv);

        // Click para abrir
        docContainer.setOnClickListener(v -> {
            try {
                Uri uri = Uri.parse(uriString);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, mimeType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent chooser = Intent.createChooser(intent, "Abrir documento");
                context.startActivity(chooser);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "No hay aplicación para abrir este tipo de archivo.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "Error al abrir documento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Long click para eliminar
        docContainer.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar documento")
                    .setMessage("¿Eliminar " + displayName + "?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        String uriTag = (String) docContainer.getTag();
                        if (uriTag != null) {
                            boolean removed = insertedPdfs.remove(uriTag);
                            if (removed) {
                                Log.d(TAG, "✓ PDF eliminado de la lista");
                            } else {
                                Log.w(TAG, "⚠️ PDF no encontrado en la lista");
                            }
                        }
                        removeView(docContainer);
                        Toast.makeText(context, "Documento eliminado", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        });

        // Feedback visual: cambiar alpha al tocar
        docContainer.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    docContainer.setAlpha(0.7f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    docContainer.setAlpha(1.0f);
                    break;
            }
            return false;
        });

        // Añadir antes del mainEditor
        int insertPosition = indexOfChild(mainEditor);
        addView(docContainer, insertPosition);

        Log.d(TAG, "Documento añadido. Total pdfs: " + insertedPdfs.size());
    }

    // Helper: intenta obtener un recurso por nombre o devolver fallback
    private int getResIdOrFallback(String name, String defType, int fallback) {
        int id = context.getResources().getIdentifier(name, defType, context.getPackageName());
        if (id == 0) return fallback;
        return id;
    }

    // Métodos de compatibilidad
    public void changeStyleAndCreateNewBlock(int style) {
        setCurrentTextStyle(style);
    }

    public void changeSizeAndCreateNewBlock(int size) {
        setCurrentTextSize(size);
    }
}
