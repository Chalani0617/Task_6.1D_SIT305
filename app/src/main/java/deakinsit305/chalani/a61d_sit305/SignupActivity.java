package deakinsit305.chalani.a61d_sit305;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;

public class SignupActivity extends AppCompatActivity {

    EditText username, email, confirmEmail, password, confirmPassword, phone;
    Button createAccountBtn;
    ImageView profileImage, addIcon;
    private Uri selectedImageUri = null;
    private final ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                profileImage.setImageURI(uri);

                SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                prefs.edit().putString("profileImageUri", uri.toString()).apply();
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        username = findViewById(R.id.et_username);
        email = findViewById(R.id.et_email);
        confirmEmail = findViewById(R.id.et_confirm_email);
        password = findViewById(R.id.et_password);
        confirmPassword = findViewById(R.id.et_confirm_password);
        phone = findViewById(R.id.et_phone);
        createAccountBtn = findViewById(R.id.btn_create_account);
        profileImage = findViewById(R.id.img_profile);
        addIcon = findViewById(R.id.img_add_icon);

        profileImage.setOnClickListener(v -> openImageChooser());
        addIcon.setOnClickListener(v -> openImageChooser());

        createAccountBtn.setOnClickListener(v -> {
            String uname = username.getText().toString().trim();
            String mail = email.getText().toString().trim();
            String confirmMail = confirmEmail.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();
            String phoneNumber = phone.getText().toString().trim();

            if (uname.isEmpty() || mail.isEmpty() || confirmMail.isEmpty() ||
                pass.isEmpty() || confirmPass.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!mail.equals(confirmMail)) {
                Toast.makeText(this, "Emails do not match.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!phoneNumber.matches("\\d{10}")) {
                Toast.makeText(this, "Enter a valid 10-digit phone number.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();

            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("username", uname);
            editor.putString("email", mail);
            editor.putString("password", pass);
            editor.putString("phone", phoneNumber);
            if (selectedImageUri != null) {
                editor.putString("profileImageUri", selectedImageUri.toString());
            }
            editor.apply();

            Toast.makeText(this, "Data saved locally.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SignupActivity.this, InterestsActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void openImageChooser() {
        imagePickerLauncher.launch(
            new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()
        );
    }

}
