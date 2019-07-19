package com.techbeloved.alc40task;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class EditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK = "extraTask";

    private static final String SAVED_TASK_ID = "savedTaskId";

    private static final String TAG = "EditTaskActivity";
    private static final String SAVED_TASK = "savedTask";

    /**
     * Collection of our tasks
     */
    private CollectionReference taskCollection;
    private EditText titleText;
    private EditText detailText;

    // Saves the currently editing task id
    private String currentTaskId;

    private Task oldTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        titleText = findViewById(R.id.edittext_task_title);
        detailText = findViewById(R.id.edittext_task_detail);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // Check that user is authenticated
        if (currentUser == null) {
            Toast.makeText(this, "You must sign in to create new task", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            taskCollection = firestore.collection("users")
                    .document(currentUser.getUid())
                    .collection("tasks");

            Intent intent = getIntent();
            if (intent.hasExtra(EXTRA_TASK)) {
                // We are editing or viewing a task, get it from intent

                Task editTask = intent.getParcelableExtra(EXTRA_TASK);

                // Cache the task received
                oldTask = editTask;

                populateTaskDetail(editTask);
            } else {
                if (savedInstanceState == null) {
                    // We want to create new task.
                    // First get the id for future reference
                    currentTaskId = taskCollection.document().getId();
                } else {
                    currentTaskId = savedInstanceState.getString(SAVED_TASK_ID);
                    oldTask = savedInstanceState.getParcelable(SAVED_TASK);
                }
            }
        }

        setTitle(null);

    }

    @Override
    protected void onStop() {
        super.onStop();
        saveCurrentTask();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(SAVED_TASK_ID, currentTaskId);
        outState.putParcelable(SAVED_TASK, oldTask);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            deleteCurrentTask();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteCurrentTask() {
        taskCollection.document(currentTaskId).delete()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        clearTaskDetails();
                        finish();
                        Log.i(TAG, "onComplete: Deleted");
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "onFailure: delete", e);
            }
        });
    }

    /**
     * Clear task details, on preparation for deletion
     */
    private void clearTaskDetails() {
        titleText.setText(null);
        detailText.setText(null);
    }

    private void populateTaskDetail(Task editTask) {
        titleText.setText(editTask.getTitle());
        detailText.setText(editTask.getDetail());
        currentTaskId = editTask.getId();
    }

    /**
     * Begins the process of saving a task whether new or edited. Only saves if title or detail has some text
     * and text has been edited
     */
    private void saveCurrentTask() {
        String title = titleText.getText().toString();
        String detail = detailText.getText().toString();

        if (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(detail)) {

            boolean edited = oldTask == null
                    || (oldTask.getTitle() != null && !oldTask.getTitle().equals(title))
                    || (oldTask.getDetail() != null && !oldTask.getDetail().equals(detail));

            if (edited) {
                Date updated = new Date();
                Task taskToSave = new Task(currentTaskId, title, detail, updated, false);
                saveTask(taskToSave);
            }
        }
    }

    /**
     * Takes care of saving the task in firestore
     *
     * @param task is the task to be saved
     */
    private void saveTask(Task task) {
        taskCollection.document(task.getId())
                .set(task)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Task saved!
                        Log.i(TAG, "onSuccess: Task saved");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "onFailure: task not saved!", e);
                    }
                });
    }
}
