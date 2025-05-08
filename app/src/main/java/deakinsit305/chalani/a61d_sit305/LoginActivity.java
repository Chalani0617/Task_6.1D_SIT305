package deakinsit305.chalani.a61d_sit305;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignup = findViewById(R.id.tv_signup_redirect);

        btnLogin.setOnClickListener(v -> {
            String uname = etUsername.getText().toString().trim();

            if (uname.isEmpty()) {
                Toast.makeText(this, "Please enter your username.", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            prefs.edit().putString("username", uname).apply();

            Intent intent = new Intent(LoginActivity.this, GeneratedTaskActivity.class);
            intent.putExtra("topic", "Algorithms");
            startActivity(intent);
        });


        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }
}

