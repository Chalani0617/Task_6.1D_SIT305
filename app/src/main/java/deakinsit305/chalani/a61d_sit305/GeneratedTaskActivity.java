package deakinsit305.chalani.a61d_sit305;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.airbnb.lottie.LottieAnimationView;

public class GeneratedTaskActivity extends AppCompatActivity {

    private TextView tvName, tvTaskDescription;
    private ImageView btnGo, profileImage;
    private LottieAnimationView loadingAnimation;
    private String topic = "";
    private final String BASE_URL = "http://10.0.2.2:5000/getQuiz?topic=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generated_task);

        loadingAnimation = findViewById(R.id.loading_animation);
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();

        tvName = findViewById(R.id.tv_name);
        tvTaskDescription = findViewById(R.id.tv_task_description);
        btnGo = findViewById(R.id.btn_go);
        profileImage = findViewById(R.id.img_profile);

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        String username = prefs.getString("username", "Guest");
        tvName.setText(username);

        String imageUriString = prefs.getString("profileImageUri", null);
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            profileImage.setImageURI(imageUri);
        }

        topic = getIntent().getStringExtra("topic");

        if (topic == null || topic.isEmpty()) {
            topic = prefs.getString("selectedTopic", "");
        }

        if (topic == null || topic.isEmpty()) {
            Toast.makeText(this, "No topic provided.", Toast.LENGTH_SHORT).show();
            loadingAnimation.cancelAnimation();
            loadingAnimation.setVisibility(View.GONE);
            return;
        }

        fetchGeneratedTask(topic);

        btnGo.setOnClickListener(v -> {
            Intent intent = new Intent(GeneratedTaskActivity.this, QuizActivity.class);
            intent.putExtra("topic", topic);
            startActivity(intent);
        });
    }

    private void fetchGeneratedTask(String topic) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + topic);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                JSONObject responseJson = new JSONObject(responseBuilder.toString());
                JSONArray quizArray = responseJson.getJSONArray("quiz");

                runOnUiThread(() -> {
                    loadingAnimation.cancelAnimation();
                    loadingAnimation.setVisibility(View.GONE);

                    if (quizArray.length() > 0) {
                        try {
                            JSONObject firstQuestion = quizArray.getJSONObject(0);
                            String question = firstQuestion.getString("question");
                            tvTaskDescription.setText(question);
                        } catch (Exception e) {
                            tvTaskDescription.setText("Error parsing question.");
                        }
                    } else {
                        tvTaskDescription.setText("No questions received.");
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadingAnimation.cancelAnimation();
                    loadingAnimation.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
