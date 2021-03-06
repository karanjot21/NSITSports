package com.nsit.jo.nsitsports;

/**
 * Created by jo on 18/01/18.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener{

    private FrameLayout mFrame;
    private ListView list;
    private ArrayList<String> sportsArrayList;
    private ArrayAdapter<String> arrayAdapter;
    private AdapterView.OnItemClickListener itemClickListener;
    private Spinner spinnerYear;
    private TextView textView;
    private DatabaseReference db;
    static protected String selectedYear;
    static protected String selectedSport;
    static protected boolean home = true;
    static boolean calledAlready = false;
    private ProgressDialog dialog;
    Snackbar snackbar;
    private NetworkStateReceiver networkStateReceiver;
    private LinearLayout ll;

    @Override
    public void onNetworkAvailable() {
        if (snackbar.isShown()) {
            snackbar.dismiss();
            Log.d("snackbar", "Hiding");
        }
    }

    @Override
    public void onNetworkUnavailable() {
        if (!snackbar.isShown()) {
            snackbar.show();
            Log.d("snackbar", "Showing");
        }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    home();
                    return true;
                case R.id.navigation_all:
                    all();
                    return true;
                case R.id.navigation_changeHome:
                    change();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);

        snackbar = Snackbar.make(findViewById(R.id.container), "Unable to connect to the Internet", Snackbar.LENGTH_INDEFINITE);
        networkStateReceiver = new NetworkStateReceiver(this);
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));


        if (!calledAlready) {
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            }
            catch (Exception e){
            }
            calledAlready = true;
        }

        db = FirebaseDatabase.getInstance().getReference()
                .child(GlobalVariables.sportListDB);
//        db.keepSynced(true);

        mFrame = (FrameLayout) findViewById(R.id.frame);
        sportsArrayList = new ArrayList<>();
        list = new ListView(this);
        arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                sportsArrayList);
        list.setAdapter(arrayAdapter);



        dialog = new ProgressDialog(FirebaseActivity.this);
        dialog.setMessage("Loading list of sports...");
        dialog.setCancelable(false);
        dialog.show();
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                dialog.hide();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    sportsArrayList.clear();
                    for(DataSnapshot ds:dataSnapshot.getChildren()) {
                        sportsArrayList.add(ds.getValue().toString());
                    }
                    arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        textView = new TextView(this);
        spinnerYear = new Spinner(this);
        List<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("1st Year");
        spinnerArray.add("2nd Year");
        spinnerArray.add("3rd Year");
        spinnerArray.add("4th Year");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapter);

        itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedSport = sportsArrayList.get(position);
                selectedYear = String.valueOf(spinnerYear.getSelectedItem());
                Intent intent = new Intent(FirebaseActivity.this, ScoreBoard.class);
                startActivity(intent);
            }
        };

        ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        home();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void home() {
//        mTextMessage.setText(MainActivity.YEAR + " " + MainActivity.BRANCH + " " + MainActivity.SECTION);
        home = true;
        mFrame.removeAllViews();

        ll.removeAllViews();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 35, 0, 0);
        ll.setLayoutParams(params);
        mFrame.addView(ll);

        params.setMargins(20, 0, 0, 0);
        textView.setTextSize(22);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(Color.parseColor("#4e3a6b"));
        textView.setText(MainActivity.YEAR + ", " + MainActivity.BRANCH + "-" + MainActivity.SECTION);
        textView.setLayoutParams(params);
        ((LinearLayout) ll).addView(textView);

        params.setMargins(0, 40, 0, 0);
        list.setLayoutParams(params);
        if (list.getParent() != null)
            ((ViewGroup) list.getParent()).removeView(list);
        ll.addView(list);

        list.setOnItemClickListener(itemClickListener);

    }

    private void all() {
        home = false;
        mFrame.removeAllViews();

        ll.removeAllViews();


        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 50, 0, 0);
        ll.setLayoutParams(params);
        mFrame.addView(ll);

        params.setMargins(20, 0, 0, 0);
        spinnerYear.setLayoutParams(params);
        if (spinnerYear.getParent() != null)
            ((ViewGroup) spinnerYear.getParent()).removeView(spinnerYear);
        ((LinearLayout) ll).addView(spinnerYear);

        params.setMargins(0, 40, 0, 0);
        list.setLayoutParams(params);
        if (list.getParent() != null)
            ((ViewGroup) list.getParent()).removeView(list);
        ll.addView(list);

        list.setOnItemClickListener(itemClickListener);
    }


    public void change() {
        Toast.makeText(this, "Ditching your Home Team? Sure?", Toast.LENGTH_LONG).show();
        startActivity(new Intent(FirebaseActivity.this, LogInActivity.class));
        finish();
    }

    public void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }
}
