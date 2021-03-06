package com.example.agape.sdb_skudeybore;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.text.format.DateFormat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    public static int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessage> adapter;
    RelativeLayout activity_main;
    FloatingActionButton fab;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(activity_main, "You have been signed out.", Snackbar.LENGTH_LONG).show();
                    finish();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(activity_main,"Successfully signed in welcome", Snackbar.LENGTH_LONG).show();
                displayChatMessage();
            } else {
                Snackbar.make(activity_main,"We couldn't sign you in please try again later", Snackbar.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = findViewById(R.id.activity_main);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = findViewById(R.id.input);
                final String email = (FirebaseAuth.getInstance().getCurrentUser() != null &&
                        FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) ?
                        FirebaseAuth.getInstance().getCurrentUser().getEmail() : "";

                if(input.getText() != null && !email.isEmpty()) {
                    final ChatMessage message = new ChatMessage(input.getText().toString(), email);
                    FirebaseDatabase.getInstance().getReference().child("/messages").push().setValue(message);
                    input.setText("");
                }
            }
        });

        //check if sign-in then navigate signin page
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            launchAuthUIActivity();
        } else {
            Log.i("Firebase user is ", "Current user is "+FirebaseAuth.getInstance().getCurrentUser());
            Snackbar.make(activity_main, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_LONG).show();
            displayChatMessage();
        }
    }

    public void launchAuthUIActivity() {
        try{
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);
        } catch (Exception e) {
            Log.e(MainActivity.class.getSimpleName(), e.getMessage());
        } finally {
            Log.i(MainActivity.class.getSimpleName(), "Inside finally block now, do something?");
        }
    }

    public void displayChatMessage() {
        ListView listOfMessage = findViewById(R.id.list_of_message);
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,R.layout.list_item,FirebaseDatabase
                .getInstance().getReference("/messages")) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView messageText, messageUser, messageTime;
                messageText = v.findViewById(R.id.message_text);
                messageUser = v.findViewById(R.id.message_user);
                messageTime = v.findViewById(R.id.message_time);

                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("mm-dd-yy (HH:mm:ss)",model.getMessageTime()));
            }
        };
        listOfMessage.setAdapter(adapter);
    }
}
