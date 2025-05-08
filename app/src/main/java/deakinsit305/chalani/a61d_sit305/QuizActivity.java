package deakinsit305.chalani.a61d_sit305;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import com.airbnb.lottie.LottieAnimationView;

public class QuizActivity extends AppCompatActivity {

    private TextView tvTaskTitle, tvTaskDescription;
    private LinearLayout questionsContainer;
    private Button btnSubmit;
    private LottieAnimationView loadingAnimation;
    private final String BASE_URL = "http://10.0.2.2:5000/getQuiz?topic=";
    private final ArrayList<RadioGroup> questionRadioGroups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        loadingAnimation = findViewById(R.id.loading_animation);
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();

        tvTaskTitle = findViewById(R.id.tv_task_title);
        tvTaskDescription = findViewById(R.id.tv_task_description);
        questionsContainer = findViewById(R.id.questions_container);
        btnSubmit = findViewById(R.id.btn_submit);

        Intent intent = getIntent();
        String topic = intent.getStringExtra("topic");

        if (topic != null) {
            fetchAndDisplayQuiz(topic);
        }

        btnSubmit.setOnClickListener(v -> {
            JSONArray results = new JSONArray();

            for (int i = 0; i < questionRadioGroups.size(); i++) {
                RadioGroup group = questionRadioGroups.get(i);
                int selectedId = group.getCheckedRadioButtonId();

                try {
                    JSONObject item = new JSONObject();
                    item.put("question", "Question " + (i + 1));

                    if (selectedId != -1) {
                        RadioButton selected = findViewById(selectedId);
                        item.put("answer", selected.getText().toString());
                    } else {
                        item.put("answer", "No answer selected");
                    }

                    results.put(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Intent resultIntent = new Intent(QuizActivity.this, ResultsActivity.class);
            resultIntent.putExtra("results", results.toString());
            startActivity(resultIntent);
        });
    }

    private void fetchAndDisplayQuiz(String topic) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + topic);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                String description = json.getString("description");
                JSONArray quizArray = json.getJSONArray("quiz");

                runOnUiThread(() -> {
                    loadingAnimation.cancelAnimation();
                    loadingAnimation.setVisibility(View.GONE);

                    tvTaskDescription.setText(description);

                    for (int i = 0; i < quizArray.length(); i++) {
                        try {
                            JSONObject question = quizArray.getJSONObject(i);
                            String questionText = question.getString("question");
                            JSONArray options = question.getJSONArray("options");

                            LinearLayout card = new LinearLayout(this);
                            card.setOrientation(LinearLayout.VERTICAL);
                            card.setPadding(24, 24, 24, 24);
                            card.setBackgroundResource(R.drawable.background_blue_box_questions);

                            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            cardParams.setMargins(0, 16, 0, 16);
                            card.setLayoutParams(cardParams);

                            TextView qTitle = new TextView(this);
                            qTitle.setText((i + 1) + ". " + questionText);
                            qTitle.setTextColor(Color.WHITE);
                            qTitle.setTextSize(16f);
                            qTitle.setTypeface(null, Typeface.BOLD);
                            qTitle.setPadding(8, 0, 8, 12);
                            card.addView(qTitle);

                            RadioGroup rg = new RadioGroup(this);
                            rg.setOrientation(RadioGroup.VERTICAL);
                            for (int j = 0; j < options.length(); j++) {
                                RadioButton rb = new RadioButton(this);
                                rb.setText(options.getString(j));
                                rb.setTextColor(Color.WHITE);
                                rb.setTextSize(14f);
                                rb.setPadding(16, 8, 16, 8);
                                rg.addView(rb);
                            }

                            card.addView(rg);
                            questionRadioGroups.add(rg);
                            questionsContainer.addView(card);

                        } catch (Exception e) {
                            Toast.makeText(this, "Error loading question: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadingAnimation.cancelAnimation();
                    loadingAnimation.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
