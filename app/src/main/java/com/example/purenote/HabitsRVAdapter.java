package com.example.purenote;


import android.app.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;



public class HabitsRVAdapter extends RecyclerView.Adapter {





    private TextView habitText;
    private ProgressBar progressBar;
    private CheckBox check;
    private MaterialCardView background;

    private static ArrayList<Goal> goals;


    private android.app.DialogFragment dialog=new DisplayGoalDataDialog();

    private static android.app.FragmentManager currentManager;

     static void setCurrentManager(android.app.FragmentManager manager){
        currentManager=manager;
    }




    public HabitsRVAdapter(ArrayList<Goal> goal){
        goals=goal;


    }

    private class ViewHolder extends RecyclerView.ViewHolder {


        private ViewHolder(View itemView) {
            super(itemView);

            background=itemView.findViewById(R.id.habitCard);
            progressBar=itemView.findViewById(R.id.progressBar);
            check=itemView.findViewById(R.id.checkBox);
            habitText=itemView.findViewById(R.id.textView);





        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_layout,parent,false);
        return new ViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final Goal goal=goals.get(position);
        final ProgressBar progressBar=holder.itemView.findViewById(R.id.progressBar);
        final String currentDate=MainActivity.getFormattedDate();
        final int step=100/goals.get(position).getTargetSteps();
        final CheckBox checkBox=holder.itemView.findViewById(R.id.checkBox);


        boolean shouldRefresh=Goal.compareDates(goal);




        habitText.setText(goal.getText());
        if(shouldRefresh){
            goal.setCompletedSteps(0);
            checkBox.setChecked(false);
            progressBar.setProgress(0);

        }

        if(goal.getLastDateChecked().equals(currentDate)){
            checkBox.setChecked(true);
            checkBox.setText(R.string.checkbox_alldone);
            progressBar.setProgress(100);
        }else {

            if (goal.getCompletedSteps() != goals.get(position).getTargetSteps()) {
                if (currentDate.equals(goal.getLastDate())) {

                    checkBox.setChecked(true);
                    checkBox.setText(R.string.checkbox_done);


                } else {

                    checkBox.setText(R.string.checkbox_notdone);


                }
            } else {

                checkBox.setClickable(false);
                checkBox.setText(R.string.checkbox_alldone);
                checkBox.setChecked(true);

            }
            progressBar.setProgress(step*goal.getCompletedSteps());
        }








        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                int progress=progressBar.getProgress();
                int completedSteps=goals.get(position).getCompletedSteps();

                if(completedSteps!=goals.get(position).getTargetSteps()) {


                    if (isChecked) {
                        progressBar.setProgress(progress + step);
                        buttonView.setText(R.string.checkbox_done);
                        goals.get(position).addDateChecked(currentDate);
                        completedSteps+=1;
                        goals.get(position).setCompletedSteps(completedSteps);
                        goal.setChecked(true);




                    } else {
                        progressBar.setProgress(progress - step);
                        buttonView.setText(R.string.checkbox_notdone);
                        goals.get(position).removeCheckedDate();
                        completedSteps-=1;
                        goal.setChecked(false);
                        if(!goal.getDatesChecked().isEmpty()) {
                            goal.deleteLastDateFromFile();
                        }


                        goals.get(position).setCompletedSteps(completedSteps);






                    }
                }
                else {
                    buttonView.setChecked(true);
                    buttonView.setText(R.string.checkbox_alldone);
                    goals.get(position).setDone(true);


                }


                }



        });

        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //android.app.FragmentManager manager=callerActivity.displayGoalData();

                Bundle bundle=new Bundle();

                bundle.putBoolean("checked",checkBox.isChecked());
                bundle.putInt("progressInt",progressBar.getProgress());
                bundle.putInt("position", position);


                dialog.setArguments(bundle);

                dialog.show(currentManager,"tag");



            }
        });










    }

    @Override
    public int getItemCount() {
        return goals.size();
    }



    public static class  DisplayGoalDataDialog extends android.app.DialogFragment {

        AlertDialog.Builder builder;
        Bundle dialogBundle;

        TextInputLayout goalNameInput;
        TextView progressTextView;
        Spinner spinner;
        CheckBox checkBox;
        ProgressBar progressBar;
        Button buttonSave, buttonCancel, buttonDelete;


        String goalName;

        boolean checked;

        int completedSteps;
        int targetSteps;
        String repeatCycle;

        int position, progress;

        String progressText;



        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View content = inflater.inflate(R.layout.goal_preview_dialog, null);
            dialogBundle=new Bundle();
            builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Details");
            builder.setView(content);

            dialogBundle=getArguments();




            goalNameInput = content.findViewById(R.id.textInputLayout4);
            progressTextView = content.findViewById(R.id.progressTextView);
            spinner = content.findViewById(R.id.spinner);
            checkBox = content.findViewById(R.id.checkBox2);
            progressBar = content.findViewById(R.id.progressBar2);
            buttonSave = content.findViewById(R.id.button4);
            buttonCancel = content.findViewById(R.id.button5);
            buttonDelete=content.findViewById(R.id.button6);


            final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.repeatCycle, android.R.layout.simple_spinner_item);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner.setAdapter(adapter);




            position=dialogBundle.getInt("position");
            checked=dialogBundle.getBoolean("checked");
            progress=dialogBundle.getInt("progressInt");
            final Goal goal=goals.get(position);



            goalName=goal.getText();
            completedSteps=goal.getCompletedSteps();
            targetSteps=goal.getTargetSteps();
            repeatCycle=goal.getRepeatCycle();



            progressText=completedSteps+"/"+targetSteps;

            progressBar.setProgress(progress);
            progressTextView.setText(progressText);
            checkBox.setChecked(checked);

            if (checkBox.isChecked())checkBox.setText(R.string.checkbox_done);
            else checkBox.setText(R.string.checkbox_notdone);
            goalNameInput.getEditText().setText(goalName);
            goalNameInput.clearFocus();




            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                }
            });

            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String editText=goalNameInput.getEditText().getText().toString();
                    String newRepeatCycle=spinner.getSelectedItem().toString();

                    Log.i("Repeat", newRepeatCycle);




                    if (!repeatCycle.equals(newRepeatCycle)){
                        goal.setRepeatCycle(newRepeatCycle);
                        goal.changeRepeatCycle(newRepeatCycle);
                    }

                    if(!editText.equals(goal.getText())&&!editText.equals("")){
                        boolean result=goal.changeGoalNameInFile(editText);
                        goal.setText(editText);
                        if (result){
                            MainActivity.goalsLayoutRV.setAdapter(new HabitsRVAdapter(goals));
                        }
                        goal.uploadFile();
                        getDialog().dismiss();
                    }





                    if (editText.equals("")){
                        goalNameInput.setErrorEnabled(true);
                        goalNameInput.setError("Please write a name for the goal");
                    }

                    else {
                        getDialog().dismiss();
                    }
                }
            });

            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isDeleted=goal.deleteFile();
                    goals.remove(position);
                    MainActivity.goalsLayoutRV.setAdapter(new HabitsRVAdapter(goals));

                    String message;
                    if (isDeleted)message="File deleted";
                    else message="File not deleted";

                    Snackbar.make(getActivity().findViewById(R.id.recyclerView), message, BaseTransientBottomBar.LENGTH_SHORT).show();
                    getDialog().dismiss();


                }
            });

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        checkBox.setText(R.string.checkbox_done);
                        completedSteps+=1;
                        Log.i("completedSteps", completedSteps+"");
                        progressText=completedSteps+"/"+targetSteps;
                        progressTextView.setText(progressText);
                    }
                    else{
                        checkBox.setText(R.string.checkbox_notdone);
                        completedSteps-=1;
                        Log.i("completedSteps", completedSteps+"");
                        progressText=completedSteps+"/"+targetSteps;
                        progressTextView.setText(progressText);
                    }
                }
            });





            return builder.create();
        }
    }








}
