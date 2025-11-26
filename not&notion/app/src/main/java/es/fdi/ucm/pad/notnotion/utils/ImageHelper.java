package es.fdi.ucm.pad.notnotion.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

// Helper para convertir imágenes a Base64 y viceversa
public class ImageHelper {

    private static final String TAG = "ImageHelper";
    private static final int MAX_IMAGE_SIZE = 800; // Ancho/alto máximo en pixels
    private static final int COMPRESSION_QUALITY = 70; // Calidad JPEG (0-100)

    /**
     * Convierte una imagen URI a Base64 string
     *
     * @param context Contexto de la aplicación
     * @param imageUri URI de la imagen seleccionada
     * @return String en Base64 o null si hay error
     */
    public static String convertImageToBase64(Context context, Uri imageUri) {
        try {
            Log.d(TAG, "Iniciando conversión a Base64");
            Log.d(TAG, "URI: " + imageUri);

            // Leer la imagen desde la URI
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "No se pudo abrir el InputStream");
                return null;
            }

            // Decodificar a Bitmap
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) {
                Log.e(TAG, "No se pudo decodificar el Bitmap");
                return null;
            }

            Log.d(TAG, "Imagen original: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());

            // Redimensionar la imagen para que sea más pequeña
            Bitmap resizedBitmap = resizeBitmap(originalBitmap, MAX_IMAGE_SIZE);

            Log.d(TAG, "Imagen redimensionada: " + resizedBitmap.getWidth() + "x" + resizedBitmap.getHeight());

            // Comprimir a JPEG
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Convertir a Base64
            String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Calcular tamaño
            int sizeKB = base64String.length() / 1024;
            Log.d(TAG, "✓ Conversión exitosa. Tamaño: " + sizeKB + " KB");

            if (sizeKB > 500) {
                Log.w(TAG, "⚠️ ADVERTENCIA: Imagen grande (" + sizeKB + " KB). Puede causar problemas.");
            }

            // Liberar recursos
            originalBitmap.recycle();
            resizedBitmap.recycle();
            byteArrayOutputStream.close();

            return base64String;

        } catch (Exception e) {
            Log.e(TAG, "Error al convertir imagen a Base64", e);
            return null;
        }
    }

    /**
     * Convierte un string Base64 a Bitmap
     *
     * @param base64String String en Base64
     * @return Bitmap o null si hay error
     */
    public static Bitmap convertBase64ToBitmap(String base64String) {
        try {
            if (base64String == null || base64String.isEmpty()) {
                return null;
            }

            // Decodificar Base64 a bytes
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

            // Convertir bytes a Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap != null) {
                Log.d(TAG, "✓ Bitmap decodificado: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            }

            return bitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error al convertir Base64 a Bitmap", e);
            return null;
        }
    }

    //Redimensiona un Bitmap manteniendo la proporción
    private static Bitmap resizeBitmap(Bitmap originalBitmap, int maxSize) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        // Si ya es pequeña, no redimensionar
        if (width <= maxSize && height <= maxSize) {
            return originalBitmap;
        }

        // Calcular nuevo tamaño manteniendo proporción
        float ratio = Math.min(
                (float) maxSize / width,
                (float) maxSize / height
        );

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }

    //Verifica si un string es Base64 válido
    public static boolean isValidBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        try {
            Base64.decode(str, Base64.DEFAULT);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}