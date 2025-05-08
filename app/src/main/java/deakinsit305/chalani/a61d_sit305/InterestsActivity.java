package deakinsit305.chalani.a61d_sit305;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class InterestsActivity extends AppCompatActivity {

    private GridLayout gridTopics;
    private Button nextBtn;
    private final ArrayList<String> selectedTopics = new ArrayList<>();
    private final int MAX_SELECTION = 10;

    private final String[] topics = {
        "Algorithms", "Data Structures",
        "Web Development", "Testing",
        "Cloud Computing", "Artificial Intelligence",
        "Computer Networks", "Operating Systems",
        "UI/UX Design", "Version Control",
        "Machine Learning", "Frontend Frameworks",
        "DevOps", "Software Architecture",
        "Backend Development", "Internet of Things"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        gridTopics = findViewById(R.id.grid_topics);
        nextBtn = findViewById(R.id.btn_next);

        populateTopicButtons();

        nextBtn.setOnClickListener(v -> {
            if (selectedTopics.isEmpty()) {
                Toast.makeText(this, "Please select at least one topic", Toast.LENGTH_SHORT).show();
            } else {
                String topic = selectedTopics.get(0);
                SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                prefs.edit()
                    .putString("selectedTopic", topic)
                    .putString("selectedTopicsList", String.join(",", selectedTopics))
                    .apply();

                Intent intent = new Intent(InterestsActivity.this, GeneratedTaskActivity.class);
                intent.putExtra("topic", topic);
                startActivity(intent);
            }
        });
    }

    private void populateTopicButtons() {
        boolean shift = false;

        for (int i = 0; i < topics.length; i++) {
            String topic = topics[i];
            MaterialButton btn = new MaterialButton(this);

            btn.setText(topic);
            btn.setTextColor(Color.BLACK);
            btn.setAllCaps(false);
            btn.setBackgroundResource(R.drawable.topic_button_background);
            btn.setCornerRadius(5);
            btn.setBackgroundTintList(null);
            btn.setStrokeWidth(0);
            btn.setPadding(24, 16, 24, 16);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins( 8, 8, 8, 8);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                if (selectedTopics.contains(topic)) {
                    selectedTopics.remove(topic);
                    btn.setBackgroundResource(R.drawable.topic_button_background);
                } else {
                    if (selectedTopics.size() >= MAX_SELECTION) {
                        Toast.makeText(this, "Maximum 10 topics allowed", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedTopics.add(topic);
                    btn.setBackgroundResource(R.drawable.topic_button_selected);
                }
            });

            gridTopics.addView(btn);
        }
    }

}
