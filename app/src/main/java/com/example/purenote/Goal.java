package com.example.purenote;

import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Goal {
    private String text;
    private int completedSteps;
    private int targetSteps;
    private ArrayList<String> datesChecked;

    public Goal(String text,int target,int completed){
        this.text=text;
        this.targetSteps=target;
        this.completedSteps=completed;
        datesChecked=new ArrayList<>();

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCompletedSteps() {
        return completedSteps;
    }

    public void setCompletedSteps(int completedSteps) {
        this.completedSteps = completedSteps;
    }

    public int getTargetSteps() {
        return targetSteps;
    }

    public void setTargetSteps(int targetSteps) {
        this.targetSteps = targetSteps;
    }

    public void addDateChecked(String dateChecked) {
        this.datesChecked.add(dateChecked);
    }

    public ArrayList<String> getDatesChecked() {
        return this.datesChecked;
    }


    public void removeCheckedDate(){
        this.datesChecked.remove(datesChecked.size()-1);
    }

    public String getLastDate(){

        if(datesChecked.isEmpty()) {
            datesChecked.add(MainActivity.getFormattedDate());
            return MainActivity.getFormattedDate();
        }
        else{
            return datesChecked.get(datesChecked.size()-1);

        }
    }

    public static void writeGoalInFile(String text,int completedSteps, int targetSteps,ArrayList<String> dates){

        FileWriter writer=null;
        String sFileName=text+".txt";


        try {

            File root = new File(Environment.getExternalStorageDirectory(), "PureNote");
            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, sFileName);


            Log.i("Write in file",gpxfile.getAbsolutePath());
            writer = new FileWriter(gpxfile);


                writer.append(text);
                writer.append(":");
                writer.append(completedSteps+ "/"+targetSteps);

                for (int i=0;i<dates.size();i++){
                    writer.append("\n");
                    writer.append(dates.get(i));
                }




                writer.flush();



            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Write in file","Error IO");
        }


    }


    public static void writeGoalInFile(Goal goal){


        String text=goal.getText();
        int completedSteps=goal.getCompletedSteps();
        int targetSteps=goal.getTargetSteps();
        ArrayList<String> dates=goal.getDatesChecked();
        FileWriter writer=null;
        String sFileName=text+".txt";


        try {

            File root = new File(Environment.getExternalStorageDirectory(), "PureNote");

            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, sFileName);
            Log.i("Write in file",gpxfile.getAbsolutePath());
            writer = new FileWriter(gpxfile);


            writer.append(text);
            writer.append(":");
            writer.append(completedSteps+ "/"+targetSteps);

            for (int i=0;i<dates.size();i++){
                writer.append("\n");
                writer.append(dates.get(i));
            }



            writer.flush();



            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Write in file","Error IO");
        }


    }

    public static ArrayList<Goal> readGoalFromFile(){

        File root = new File(Environment.getExternalStorageDirectory(), "PureNote");
        File[] filesGoals=root.listFiles();
        ArrayList<String> goalString=new ArrayList<>();
        ArrayList<Goal> goalsFromFile=new ArrayList<>();



        Scanner scan=null;
        try{
            for (File file:filesGoals) {
                scan=new Scanner(file);
                while (scan.hasNext()){
                    goalString.add(scan.nextLine());

                }

                Goal tempGoal=decode(goalString);
                goalsFromFile.add(tempGoal);
                goalString.clear();

            }



            scan.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
            Log.i("Read from file", "File not found");
            return new ArrayList<>();

        }
        catch (NullPointerException npe){
            npe.printStackTrace();
            Log.i("Read goal from file","No files");
        }




        return goalsFromFile;


    }

    public static Goal decode(ArrayList<String> dataFromFile){
        Goal tempGoal=null;

        //First line of the txt file represents the goal and completed/target steps
        String a=dataFromFile.get(0);

        //Separating the name, the completed and the target steps
        String goalName=a.substring(0,a.indexOf(":"));
        int completedNumber=Integer.parseInt(a.substring(a.indexOf(":")+1,a.indexOf("/")));
        int targetNumber=Integer.parseInt(a.substring(a.indexOf("/")+1));
        tempGoal=new Goal(goalName,targetNumber,completedNumber);



        //Iterator to input all the dates on which the goal has been done(checked)
        //starts from 1 to skip the first line
        for (int i=1;i<dataFromFile.size();i++) {
            tempGoal.addDateChecked(dataFromFile.get(i));
        }







        return tempGoal;

    }







    @NonNull
    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append(this.text);
        builder.append(" ");
        builder.append(completedSteps);
        builder.append("/");
        builder.append(targetSteps);
        builder.append(" ");
        builder.append(this.getLastDate());

        return builder.toString();
    }
}
