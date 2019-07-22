package com.example.dailytasker;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailytasker.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.time.temporal.ValueRange;
import java.util.Date;

public class Home extends AppCompatActivity {

//    Firebase

    private FloatingActionButton fabBtn;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

//    Toolbar

    private Toolbar toolbar;

//    Recycler

    private RecyclerView recyclerView;

//    update input field
    private EditText titleUp;
    private EditText noteUp;
    private Button btn_up;
    private Button btn_dlt;

//    Variables

    private String title;
    private String note;
    private String post_key;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        toolbar=findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Daily Tasker");




        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();
        String uID = mUser.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("TaskNote").child(uID);
        mDatabase.keepSynced(true);

        fabBtn=findViewById(R.id.fab_btn);
        recyclerView = findViewById(R.id.recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder myDialog = new AlertDialog.Builder(Home.this);

                LayoutInflater inflater = LayoutInflater.from(Home.this);
                View myview = inflater.inflate(R.layout.custom_input,null);
                myDialog.setView(myview);
                final AlertDialog dialog = myDialog.create();

                final EditText title = myview.findViewById(R.id.edt_title);
                final EditText note = myview.findViewById(R.id.edt_note);

                 Button btnsave = myview.findViewById(R.id.btn_save);

                 btnsave.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {

                         String mtitle = title.getText().toString().trim();
                         String mnote = note.getText().toString().trim();

                         if(TextUtils.isEmpty(mtitle)){
                             title.setError("Required Field...");
                             return;
                         }
                         if(TextUtils.isEmpty(mnote)){
                             note.setError("Required Field...");
                             return;
                         }

                         String id  = mDatabase.push().getKey();
                         String datee = DateFormat.getDateInstance().format(new Date());

                         Data data = new Data(mtitle,mnote,datee,id);
                         mDatabase.child(id).setValue(data);
                         Toast.makeText(getApplicationContext(),"Data Inserted",Toast.LENGTH_SHORT).show();
                         dialog.dismiss();
                     }
                 });

                dialog.show();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Data,MyViewHolder>adapter=new FirebaseRecyclerAdapter<Data, MyViewHolder>(
                Data.class,
                R.layout.idemdata,
                MyViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, final Data model, final int i) {

                viewHolder.setTitle(model.getTitle());
                viewHolder.setNote(model.getNote());
                viewHolder.setDate(model.getDate());

                viewHolder.myview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        post_key=getRef(i).getKey();
                        title = model.getTitle();
                        note = model.getNote();

                        update();

                    }
                });

            }
        };

        recyclerView.setAdapter(adapter);


    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        View myview;

        public MyViewHolder( View itemView) {
            super(itemView);
            myview=itemView;
        }

        public void setTitle(String title){
            TextView mtitle = myview.findViewById(R.id.title);
            mtitle.setText(title);
        }

        public void setNote(String note){
            TextView mNote = myview.findViewById(R.id.note);
            mNote.setText(note);
        }

        public void setDate (String date){
            TextView mDate = myview.findViewById(R.id.date);
            mDate.setText(date);
        }
    }

    public void update(){
        AlertDialog.Builder mydialog = new AlertDialog.Builder(Home.this);
        LayoutInflater inflater= LayoutInflater.from(Home.this);

        View myview = inflater.inflate(R.layout.update,null);
        mydialog.setView(myview);
        final AlertDialog dialog = mydialog.create();

        titleUp = myview.findViewById(R.id.upd_title);
        noteUp = myview.findViewById(R.id.upd_note);

        titleUp.setText(title);
        titleUp.setSelection(title.length());

        noteUp.setText(note);
        noteUp.setSelection(note.length());

        btn_dlt=myview.findViewById(R.id.btn_delete);
        btn_up=myview.findViewById(R.id.btn_update);

        btn_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                title = titleUp.getText().toString().trim();
                note = noteUp.getText().toString().trim();
                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(title,note,mDate,post_key);
                mDatabase.child(post_key).setValue(data);
                Toast.makeText(getApplicationContext(),"Updated",Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        btn_dlt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(post_key).removeValue();
                Toast.makeText(getApplicationContext(),"Deleted",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
            mAuth.signOut();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
