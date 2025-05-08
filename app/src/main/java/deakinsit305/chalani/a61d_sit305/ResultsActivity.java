package deakinsit305.chalani.a61d_sit305;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import com.airbnb.lottie.LottieAnimationView;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

public class ResultsActivity extends AppCompatActivity {

    private LinearLayout resultsContainer;
    private Button btnContinue;
    private LottieAnimationView confettiAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        resultsContainer = findViewById(R.id.results_container);
        btnContinue = findViewById(R.id.btn_continue);
        confettiAnimation = findViewById(R.id.confetti_animation);

        btnContinue.setVisibility(View.GONE);

        confettiAnimation.playAnimation();
        confettiAnimation.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                btnContinue.setVisibility(View.VISIBLE);
            }
        });

        String resultJson = getIntent().getStringExtra("results");
        if (resultJson != null) {
            populateResults(resultJson);
        }

        btnContinue.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            String savedTopics = prefs.getString("selectedTopicsList", "");
            String currentTopic = prefs.getString("selectedTopic", "");

            if (savedTopics.isEmpty()) {
                Toast.makeText(this, "No saved topics. Returning to Interests screen.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ResultsActivity.this, InterestsActivity.class));
                finish();
                return;
            }

            // Split and remove current topic
            String[] allTopics = savedTopics.split(",");
            ArrayList<String> filtered = new ArrayList<>();

            for (String t : allTopics) {
                if (!t.trim().equalsIgnoreCase(currentTopic.trim())) {
                    filtered.add(t.trim());
                }
            }

            if (filtered.isEmpty()) {
                Toast.makeText(this, "Only one topic available. Repeating same quiz.", Toast.LENGTH_SHORT).show();
                filtered.add(currentTopic); // fallback to same topic
            }

            String newTopic = filtered.get(new java.util.Random().nextInt(filtered.size()));

            prefs.edit().putString("selectedTopic", newTopic).apply();

            Intent intent = new Intent(ResultsActivity.this, GeneratedTaskActivity.class);
            intent.putExtra("topic", newTopic);
            startActivity(intent);
            finish();
        });

    }

    private void populateResults(String jsonString) {
        try {
            JSONArray results = new JSONArray(jsonString);
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                String question = item.getString("question");
                String answer = item.getString("answer");

                TextView resultView = new TextView(this);
                resultView.setText((i + 1) + ". " + question + "\n" + answer);
                resultView.setPadding(24, 24, 24, 24);
                resultView.setTextColor(getResources().getColor(android.R.color.white));
                resultView.setTextSize(16f);
                resultView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                resultView.setBackground(getResources().getDrawable(R.drawable.background_blue_box_questions));
                resultView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) resultView.getLayoutParams();
                layoutParams.setMargins(0, 12, 0, 0);
                resultView.setLayoutParams(layoutParams);

                resultsContainer.addView(resultView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
