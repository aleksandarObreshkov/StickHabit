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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        Goal goal=goals.get(position);
        final ProgressBar progressBar=holder.itemView.findViewById(R.id.progressBar);

        final String date=MainActivity.getFormattedDate();
        final int step=100/goals.get(position).getTargetSteps();

        habitText.setText(goal.getText());


        if(goal.getCompletedSteps()!=goals.get(position).getTargetSteps()) {
            if (date.equals(goal.getLastDate())) {
                check.setChecked(true);
                check.setText("Done");


            } else {

                check.setText("Not done");

            }
        }
        else {
            check.setEnabled(false);
            check.setText("All done");

        }

        progressBar.setProgress(step*goal.getCompletedSteps());




        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                int progress=progressBar.getProgress();
                int completedSteps=goals.get(position).getCompletedSteps();

                if(completedSteps!=goals.get(position).getTargetSteps()) {


                    if (isChecked) {
                        progressBar.setProgress(progress + step);
                        buttonView.setText("Done");
                        goals.get(position).addDateChecked(date);
                        completedSteps+=1;

                        goals.get(position).setCompletedSteps(completedSteps);




                    } else {
                        progressBar.setProgress(progress - step);
                        buttonView.setText("Not done");
                        goals.get(position).removeCheckedDate();
                        completedSteps-=1;


                        goals.get(position).setCompletedSteps(completedSteps);



                    }
                }
                else {
                    buttonView.setEnabled(false);
                    buttonView.setText("All done");
                }


                }



        });




    }

    @Override
    public int getItemCount() {
        return goals.size();
    }
}
