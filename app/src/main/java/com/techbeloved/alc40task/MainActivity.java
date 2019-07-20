package com.techbeloved.alc40task;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "MainActivity";
    private CollectionReference taskCollection;
    private FirebaseUser currentUser;
    private TasksAdapter tasksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton newTaskFab = findViewById(R.id.fab_new_task);
        newTaskFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newTaskIntent = new Intent(MainActivity.this, EditTaskActivity.class);
                startActivity(newTaskIntent);
            }
        });

        RecyclerView taskList = findViewById(R.id.recyclerview_tasks);

        tasksAdapter = new TasksAdapter(new ClickListener<Task>() {
            @Override
            public void onClick(Task item) {
                Intent editIntent = new Intent(MainActivity.this, EditTaskActivity.class);
                editIntent.putExtra(EditTaskActivity.EXTRA_TASK, item);
                startActivity(editIntent);
            }

            @Override
            public void onCompleteToggle(Task item) {
                updateTaskCompletionState(item);
            }
        });
        taskList.setAdapter(tasksAdapter);
        taskList.setLayoutManager(new LinearLayoutManager(this));
        taskList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        setTitle(R.string.my_tasks);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        Log.i(TAG, "onCreate: creating");
        // null means no user is currently logged in
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
        } else {
            initializeUserTasks();
        }
    }

    private void navigateToLogin() {
        // To kick off the FirebaseUI sign in flow, create a sign in intent with your preferred sign-in methods:
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                        .build(),
                RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                currentUser = FirebaseAuth.getInstance().getCurrentUser();
                // proceed with initialization
                initializeUserTasks();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                // For now, we just quite the application
                Log.w("MainActivity", "onActivityResult: ", response != null ? response.getError() : null);
                Toast.makeText(this, "Error login in", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initializeUserTasks() {

        Log.i(TAG, "initializeUserTasks: should init firestore here");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        taskCollection = firestore.collection("users")
                .document(currentUser.getUid())
                .collection("tasks");

        taskCollection.orderBy("updated", Query.Direction.DESCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException exception) {

                        if (queryDocumentSnapshots != null) {
                            List<Task> tasks = queryDocumentSnapshots.toObjects(Task.class);
                            Collections.sort(tasks, (t1, t2) -> {
                                int task1Complete = t1.isCompleted() ? 1 : 0;
                                int task2Complete = t2.isCompleted() ? 1 : 0;
                                return task1Complete - task2Complete;
                            });
                            tasksAdapter.submitList(tasks);
                        }

                        if (exception != null) {
                            Log.w(TAG, "onEvent: failed to get tasks", exception);
                        }
                    }
                });
    }

    private void updateTaskCompletionState(Task task) {
        taskCollection.document(task.getId())
                .update("completed", !task.isCompleted())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: task completion status changed");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onFailure: Failed to change task status");
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> finish());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
