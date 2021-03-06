package eky.beaconmaps.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import eky.beaconmaps.R;
import eky.beaconmaps.utils.PreferencesUtil;

public class SignUpActivity extends BaseActivity implements View.OnClickListener{

    public static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private EditText etName, etEmail, etPassword, etRePassword;
    private MaterialButton signUpButton;
    private PreferencesUtil preferencesUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        preferencesUtil = new PreferencesUtil(this);

        etName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_Email);
        etPassword = findViewById(R.id.et_password1);
        etRePassword = findViewById(R.id.et_password2);
        signUpButton = findViewById(R.id.button_sign_up);
        signUpButton.setOnClickListener(this);

        isNetworkAvailable();

    }

    private void createAccount() {
        String email= String.valueOf(etEmail.getText());
        String password = String.valueOf(etPassword.getText());

        Log.d(TAG, "createAccount:" + etEmail);

        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        preferencesUtil.saveData("KEY_USER_NAME", etName.getText().toString());
                        finish();

                        Snackbar.make(this.findViewById(R.id.cl_main),
                                "Success.", Snackbar.LENGTH_LONG)
                                .setAction("Ok", view -> { })
                                .setActionTextColor(getResources().getColor(R.color.rallyGreen))
                                .show();

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());

                        Snackbar.make(this.findViewById(R.id.cl_main),
                                "Authentication failed.", Snackbar.LENGTH_LONG)
                                .setAction("Ok", view -> { })
                                .setActionTextColor(getResources().getColor(R.color.rallyGreen))
                                .show();
                    }
                });
    }

    private boolean validateForm() {

        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Required.");
            valid = false;
        } else {
            etEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Required.");
            valid = false;
        } else {
            etPassword.setError(null);
        }

        if(!password.equals(etRePassword.getText().toString())){
            etRePassword.setError("It's not the same input.");
            valid = false;
        }

        if (!email.contains("@")){
            etEmail.setError("Check your email info.");
            valid = false;
        }

        if (!(password.length() > 6)) {
            etEmail.setError("Password length has to be longer than 6");
            valid = false;
        }

        return valid;

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        switch (i) {
            case R.id.button_sign_up:
                createAccount();
                break;
            default:
                break;
        }

    }

    private void sendEmailVerification() { // no need

        // Send verification etEmail
        final FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    // [START_EXCLUDE]

                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this,
                                "Verification etEmail sent to " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                        Toast.makeText(SignUpActivity.this,
                                "Failed to send verification etEmail.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
