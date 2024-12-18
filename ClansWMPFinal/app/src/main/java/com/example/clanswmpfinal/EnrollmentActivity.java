package com.example.clanswmpfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnrollmentActivity extends AppCompatActivity {

    private Button addButton, summaryButton;
    private LinearLayout subjectContainer;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int totalCredits = 0;
    private static final int MAX_CREDITS = 24;

    private final List<String> subjectList = Arrays.asList(
            "Mathematics - 4",
            "Biology - 4",
            "History - 4",
            "Computer Science - 4",
            "Physical Education - 4",
            "Chemistry - 4",
            "Physics - 4",
            "Economy - 4",
            "Geography - 4",
            "Fine Arts - 4"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        subjectContainer = findViewById(R.id.subjectContainer);
        addButton = findViewById(R.id.addButton);
        summaryButton = findViewById(R.id.summaryButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        populateSubjects();

        addButton.setOnClickListener(v -> addSubjects());

        summaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(EnrollmentActivity.this, EnrollmentSummaryActivity.class);
            startActivity(intent);
        });
    }

    private void populateSubjects() {
        for (String subjectInfo : subjectList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(subjectInfo);
            checkBox.setTag(subjectInfo);
            subjectContainer.addView(checkBox);
        }
    }

    private void addSubjects() {
        String userId = mAuth.getCurrentUser().getUid();
        List<String> selectedSubjects = new ArrayList<>();

        int totalSelectedCredits = 0;

        for (int i = 0; i < subjectContainer.getChildCount(); i++) {
            View view = subjectContainer.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isChecked()) {
                    String subjectInfo = (String) checkBox.getTag();
                    selectedSubjects.add(subjectInfo);
                    int credits = Integer.parseInt(subjectInfo.split("-")[1].trim());
                    totalSelectedCredits += credits;
                }
            }
        }

        if (selectedSubjects.isEmpty()) {
            Toast.makeText(this, "No subjects selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (totalCredits + totalSelectedCredits > MAX_CREDITS) {
            Toast.makeText(this, "Credit limit exceeded! Maximum allowed credits: " + MAX_CREDITS, Toast.LENGTH_SHORT).show();
            return;
        }

        totalCredits += totalSelectedCredits;

        for (String subjectInfo : selectedSubjects) {
            db.collection("students").document(userId)
                    .collection("enrolledSubjects")
                    .add(subjectInfo)
                    .addOnSuccessListener(docRef ->
                            Toast.makeText(this, subjectInfo + " added successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add " + subjectInfo, Toast.LENGTH_SHORT).show());
        }

        Toast.makeText(this, "Subjects added successfully!", Toast.LENGTH_SHORT).show();
    }
}
