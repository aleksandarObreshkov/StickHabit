package com.example.purenote;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class HabitsRVAdapter extends RecyclerView.Adapter {





    TextView habitText;
    ProgressBar progressBar;
    CheckBox check;
    MaterialCardView background;

    ArrayList<Goal> goals;




    public HabitsRVAdapter(ArrayList<Goal> goals){
        this.goals=goals;



    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        public ViewHolder(View itemView) {
            super(itemView);

            background=itemView.findViewById(R.id.habitHolderBackground);
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final Goal goal=goals.get(position);
        final ProgressBar progressBar=holder.itemView.findViewById(R.id.progressBar);
        final CheckBox check=holder.itemView.findViewById(R.id.checkBox);
        final TextView habit=holder.itemView.findViewById(R.id.textView);

        final String date=MainActivity.getFormattedDate();

        final int step=100/goal.getTargetSteps();





        habit.setText(goal.getText());

        if(date.equals(goal.getLastDate())){
            check.setChecked(true);
            check.setText("Done");


        }
        else {

            check.setText("Not done");

        }

        progressBar.setProgress(step*goal.getCompletedSteps());




        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int progress=progressBar.getProgress();


                if(!date.equals(goal.getLastDate())){

                        progressBar.setProgress(progress+step);
                        check.setText("Done");
                        goal.addDateChecked(date);
                        goal.setCompletedSteps(goal.getCompletedSteps()+1);

                    }


                else{
                    if(isChecked){
                        progressBar.setProgress(progress+step);
                        check.setText("Done");
                        goal.addDateChecked(date);
                        goal.setCompletedSteps(goal.getCompletedSteps()+1);
                    }

                    else {
                        progressBar.setProgress(progress-step);
                        check.setText("Not done");
                        goal.removeCheckedDate();
                        goal.setCompletedSteps(goal.getCompletedSteps()-1);

                    }
                }
            }
        });




    }

    @Override
    public int getItemCount() {
        return goals.size();
    }
}
