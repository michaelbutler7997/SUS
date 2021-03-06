package com.example.sus.Activities;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.sus.Activities.Adapters.ArticleAdapter;
import com.example.sus.Activities.Adapters.EventAdapter;
import com.example.sus.Activities.Models.Article_Model;
import com.example.sus.Activities.Models.Event_Model;
import com.example.sus.Activities.Models.User_Model;
import com.example.sus.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EventsActivity extends AppCompatActivity {


    static Dialog new_event_dialog;
    ArrayList<Event_Model> all_events = new ArrayList<>();

    Menu menu;
    User_Model current_user;
    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    Context context;

    //Layout Manager
    LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        //add back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get Current user
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    current_user = dataSnapshot.getValue(User_Model.class);

                    if (current_user != null) {
                        //Toast.makeText(context, "You are a " + current_user.getaccess_level(), Toast.LENGTH_SHORT).show();

                        switch (current_user.getaccess_level()) {
                            case "Student":
                                //Do stuff for a student here
                                if (menu != null) {
                                    menu.findItem(R.id.action_add_event).setVisible(false);
                                }
                                break;

                            case "Admin":
                                //Do stuff for an Admin here
                                if (menu != null) {
                                    menu.findItem(R.id.action_add_event).setVisible(true);
                                }
                                break;

                            default:break;
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_event) {
            //Show new Article Dialog input box for the Admin to create a new Artcile
            showNewEventDialog();

        }


        return super.onOptionsItemSelected(item);
    }

    //creating the menu in action bar to create events or articles
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ehome, menu);
        return true;
    }


    //showing the events
    public void showNewEventDialog() {
        new_event_dialog = new Dialog(this);
        new_event_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        new_event_dialog.setContentView(R.layout.new_event_dialog);
        new_event_dialog.setCancelable(true);

        lp.copyFrom(new_event_dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        new_event_dialog.show();


        ((ImageButton) new_event_dialog.findViewById(R.id.event_bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new_event_dialog.dismiss();
            }
        });

        ((Button) new_event_dialog.findViewById(R.id.event_bt_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event_Model model = new Event_Model();
                model.setevent_title(((EditText) new_event_dialog.findViewById(R.id.event_title_et)).getText().toString());
                model.setevent_description(((EditText) new_event_dialog.findViewById(R.id.event_description_et)).getText().toString());
                model.setevent_price(((EditText) new_event_dialog.findViewById(R.id.event_price_et)).getText().toString());
                model.setevent_by(current_user.getfull_name());
                model.setevent_timestamp(getTimestamp());

                //Write new event model to Fire base
                FirebaseDatabase.getInstance().getReference().child("subjects").child("events").child(model.getevent_timestamp()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            getApplicationContext();
                            Toast.makeText(getApplicationContext(), "Event added successfully", Toast.LENGTH_SHORT).show();
                            new_event_dialog.dismiss();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to add new event", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });


    }

    public static String getTimestamp() {
        DateFormat timestamp_format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date currentDate = new Date();
        return timestamp_format.format(currentDate);
    }


}
