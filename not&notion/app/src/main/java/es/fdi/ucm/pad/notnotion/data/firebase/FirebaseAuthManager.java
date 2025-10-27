package es.fdi.ucm.pad.notnotion.data.firebase;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class FirebaseAuthManager {

    private final FirebaseAuth auth;

    public FirebaseAuthManager() {
        this.auth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public Intent getSignInIntent() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build();
    }

    public void signOut(Activity activity, @NonNull Runnable onComplete) {
        AuthUI.getInstance()
                .signOut(activity)
                .addOnCompleteListener(task -> onComplete.run());
    }

    public void handleSignInResult(int resultCode, Intent data,
                                   @NonNull Runnable onSuccess,
                                   @NonNull java.util.function.Consumer<String> onError) {
        IdpResponse response = IdpResponse.fromResultIntent(data);
        if (resultCode == Activity.RESULT_OK) {
            onSuccess.run();
        } else {
            String errorMessage = (response != null && response.getError() != null)
                    ? response.getError().getMessage()
                    : "Login cancelado o fallido";
            onError.accept(errorMessage);
        }
    }
}