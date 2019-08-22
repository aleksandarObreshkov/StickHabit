package com.example.purenote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Scanner;

interface NoticeDialogListener{
    void OnPositiveClick(MainActivity.AddGoalDialog dialog);
    void OnNegativeClick(MainActivity.AddGoalDialog dialog);
}

public class MainActivity extends AppCompatActivity implements NoticeDialogListener {
    private static RecyclerView goalsLayoutRV;

    static ArrayList<Goal> goalsArray=new ArrayList<>();
    FloatingActionButton addGoalButton;
    private FirebaseAuth auth;


    DialogFragment dialog=new AddGoalDialog();




    public static String getFormattedDate(){
        Date date=new Date();

        Calendar calendar=new GregorianCalendar();

        String day=calendar.get(Calendar.DAY_OF_MONTH)+"/";
        String month=(calendar.get(Calendar.MONTH)+1)+"/";
        String year=calendar.get(Calendar.YEAR)+"";


            Log.i("Date parsing", day+month+year);


        return day+month+year;

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();

        /*
        if(user==null){
            Intent i=new Intent(MainActivity.this,LoginMenu.class);
            startActivity(i);
        }
        */



        goalsArray=Goal.readGoalFromFile();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);













        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},5);
        }

        auth=FirebaseAuth.getInstance();
        addGoalButton=findViewById(R.id.floatingActionButton);


        goalsLayoutRV=findViewById(R.id.recyclerView);
        goalsArray=Goal.readGoalFromFile();
        goalsLayoutRV.setAdapter(new HabitsRVAdapter(goalsArray));
        goalsLayoutRV.setLayoutManager(new LinearLayoutManager(this));


        addGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.show(getFragmentManager(),"Add");

            }
        });


    }


    @Override
    protected void onStop() {
        super.onStop();
        for (Goal a:goalsArray) {

            if(!a.getDatesChecked().isEmpty()) {
                Goal.writeGoalInFile(a, 2);
            }

        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        goalsArray=Goal.readGoalFromFile();
        goalsLayoutRV.setAdapter(new HabitsRVAdapter(goalsArray));
    }

    @Override
    protected void onResume() {
        super.onResume();
        goalsArray=Goal.readGoalFromFile();
        goalsLayoutRV.setAdapter(new HabitsRVAdapter(goalsArray));
    }

    @Override
    public void OnPositiveClick(AddGoalDialog dialog) {



    }

    @Override
    public void OnNegativeClick(AddGoalDialog dialog) {

    }


    public static class  AddGoalDialog extends DialogFragment {
        NoticeDialogListener mListener;
        AlertDialog.Builder builder;
        Bundle dialogBundle;
        TextInputLayout goalInput;
        TextInputLayout targetNumberInput;
        Button addButton;
        Button cancelButton;
        RadioGroup radioGroup;

        RadioButton daily, weekly, monthly, yearly;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            try {
                mListener = (NoticeDialogListener) context;
            } catch (Exception e) {
                throw new ClassCastException(context.toString() + "must implement NoticeDialogListener");
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View content = inflater.inflate(R.layout.dialog_add_goal, null);
            dialogBundle=new Bundle();
            builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Add new goal");
            builder.setView(content);

            radioGroup=content.findViewById(R.id.radioGroup);
            daily=content.findViewById(R.id.radioButton);
            weekly=content.findViewById(R.id.radioButton2);
            monthly=content.findViewById(R.id.radioButton3);
            yearly=content.findViewById(R.id.radioButton4);
            goalInput = content.findViewById(R.id.textInputLayout);
            targetNumberInput = content.findViewById(R.id.textInputLayout2);
            addButton=content.findViewById(R.id.button2);
            cancelButton=content.findViewById(R.id.button);




            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    try {
                        String goalName = goalInput.getEditText().getText().toString();
                        int targetNumber = Integer.parseInt(targetNumberInput.getEditText().getText().toString());
                        Goal newGoal = new Goal(goalName, targetNumber);
                        if(daily.isChecked()){
                            newGoal.setRepeatCycle("daily");
                        }

                        else if(weekly.isChecked()){
                            newGoal.setRepeatCycle("weekly");
                        }

                        else if(monthly.isChecked()){
                            newGoal.setRepeatCycle("monthly");
                        }

                        else if(yearly.isChecked()){
                            newGoal.setRepeatCycle("yearly");
                        }

                        else {
                            throw  new NullPointerException("No repeat cycle chosen");
                        }
                        goalsArray.add(newGoal);
                        goalsLayoutRV.setAdapter(new HabitsRVAdapter(goalsArray));
                        Goal.writeGoalInFile(newGoal,1);
                        dismiss();
                    }catch (NullPointerException e) {
                        if (e.getMessage() == null) {
                            Snackbar.make(content.findViewById(R.id.recyclerView), "Please fill in the fields", BaseTransientBottomBar.LENGTH_SHORT).show();
                        }

                        else {
                            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (NumberFormatException nfe){
                        nfe.printStackTrace();
                        Toast.makeText(getContext(),"Please fill in the fields",Toast.LENGTH_SHORT).show();
                    }

                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.OnNegativeClick(AddGoalDialog.this);
                    dismiss();
                }
            });


            return builder.create();
        }
    }



//TODO action bar control




}
