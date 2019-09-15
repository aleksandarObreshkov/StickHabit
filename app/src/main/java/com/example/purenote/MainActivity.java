package com.example.purenote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

interface NoticeDialogListener{
    void OnPositiveClick(MainActivity.AddGoalDialog dialog);
    void OnNegativeClick(MainActivity.AddGoalDialog dialog);
}

public class MainActivity extends AppCompatActivity implements NoticeDialogListener,DisplayGoalData {
    static RecyclerView goalsLayoutRV;

    static ArrayList<Goal> goalsArray=new ArrayList<>();
    FloatingActionButton addGoalButton;
    private FirebaseAuth auth;


    DialogFragment dialog=new AddGoalDialog();

    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser user=mAuth.getCurrentUser();
    StorageReference storageReference= FirebaseStorage.getInstance().getReference();






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



        if(user==null){
            Intent i=new Intent(MainActivity.this,LoginMenu.class);
            startActivity(i);
        }







        goalsArray=Goal.readGoalFromFile();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("SignOut");
        menu.add("Change  theme");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("SignOut")){
            mAuth.signOut();
            Intent i=new Intent(MainActivity.this,LoginMenu.class);
            startActivity(i);
        }

        if(item.getTitle().equals("Change theme")){
            getApplicationContext().setTheme(R.style.IvaTheme);


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);













        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},5);
        }

        auth=FirebaseAuth.getInstance();
        addGoalButton=findViewById(R.id.floatingActionButton);


        goalsLayoutRV=findViewById(R.id.recyclerView);
        goalsArray=Goal.readGoalFromFile();
        goalsLayoutRV.setAdapter(new HabitsRVAdapter(goalsArray,MainActivity.this));
        goalsLayoutRV.setLayoutManager(new LinearLayoutManager(this));


        addGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //dialog.show(getFragmentManager(),"Add");
                Goal.downloadFile(storageReference, mAuth);

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
        goalsLayoutRV.setAdapter(new HabitsRVAdapter(goalsArray,MainActivity.this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        goalsArray=Goal.readGoalFromFile();
        goalsLayoutRV.setAdapter(new HabitsRVAdapter(goalsArray,MainActivity.this));
    }

    @Override
    public void OnPositiveClick(AddGoalDialog dialog) {



    }

    @Override
    public void OnNegativeClick(AddGoalDialog dialog) {

    }

    @Override
    public FragmentManager displayGoalData() {
        return getFragmentManager();

    }


    public static class  AddGoalDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {
        NoticeDialogListener mListener;
        AlertDialog.Builder builder;
        Bundle dialogBundle;
        TextInputLayout goalInput;
        TextInputLayout targetNumberInput;
        Button addButton;
        Button cancelButton;
        Spinner choices;

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


            goalInput = content.findViewById(R.id.textInputLayout);
            targetNumberInput = content.findViewById(R.id.textInputLayout2);
            addButton=content.findViewById(R.id.button2);
            cancelButton=content.findViewById(R.id.button);
            choices=content.findViewById(R.id.spinner2);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.repeatCycle, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
            choices.setAdapter(adapter);





            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    try {
                        String goalName = goalInput.getEditText().getText().toString();
                        int targetNumber = Integer.parseInt(targetNumberInput.getEditText().getText().toString());
                        Goal newGoal = new Goal(goalName, targetNumber);
                        goalsArray.add(newGoal);


                        String a="";
                        a=String.valueOf(choices.getSelectedItem());
                        newGoal.setRepeatCycle(a);
                        goalsLayoutRV.setAdapter(new HabitsRVAdapter(goalsArray,getActivity()));
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

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }







//TODO action bar control




}
