package es.fdi.ucm.pad.notnotion.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LocaleHelper {

    public static void setLocale(Context context, String languageCode) {
        persistLanguage(context, languageCode);
        updateResources(context, languageCode);
    }

    public static String getLanguage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("app_language", "es");
    }

    private static void persistLanguage(Context context, String language) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("app_language", language)
                .apply();
    }

    @SuppressLint("ObsoleteSdkInt")
    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
    }

    public static Context applyLocale(Context context) {
        return updateBaseContextLocale(context);
    }

    private static Context updateBaseContextLocale(Context context) {
        String lang = getLanguage(context);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }
}
