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
    private ArrayList<String> dateChecked;

    public Goal(String text,int target,int completed){
        this.text=text;
        this.targetSteps=target;
        this.completedSteps=completed;
        dateChecked=new ArrayList<>();

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
        this.dateChecked.add(dateChecked);
    }

    public ArrayList<String> getDatesChecked() {
        return dateChecked;
    }


    public void removeCheckedDate(){
        this.dateChecked.remove(dateChecked.size()-1);
    }

    public String getLastDate(){

        if(dateChecked.isEmpty()) {
            dateChecked.add(MainActivity.getFormattedDate());
            return MainActivity.getFormattedDate();
        }
        else{
            return dateChecked.get(dateChecked.size()-1);

        }
    }

    public static void writeGoalInFile(String text,int completedSteps, int targetSteps,String date){

        FileWriter writer=null;
        String sFileName=text+".txt";
        Log.i("Write in file",sFileName);

        try {

            File root = new File(Environment.getExternalStorageDirectory(), "PureNote");
            Log.i("Write in file",root.getAbsolutePath());
            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, sFileName);
            Log.i("Write in file",gpxfile.getAbsolutePath());
            writer = new FileWriter(gpxfile);


                writer.append(text);
                writer.append(":");
                writer.append(completedSteps+ "/"+targetSteps);
                writer.append("AT*");
                writer.append(date);
                writer.append("*");


                //new goal:5/7AT08/08/2019
                Log.i("Write in file",writer.toString());
                writer.flush();



            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Write in file","Error");
        }


    }

    public static ArrayList<Goal> readGoalFromFile(){

        File root = new File(Environment.getExternalStorageDirectory(), "PureNote");
        File[] filesGoals=root.listFiles();
        ArrayList<String> goalString=new ArrayList<>();



        Scanner scan=null;
        try{
            for (File file:filesGoals) {
                scan=new Scanner(file);
                if(scan.hasNext()){
                    goalString.add(scan.nextLine());

                }

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

        ArrayList<Goal> goalArrayList=new ArrayList<>();
        try{

            for (String a:goalString) {
                String goalName=a.substring(0,a.indexOf(":"));
                int completedNumber=Integer.parseInt(a.substring(a.indexOf(":")+1,a.indexOf("/")));
                int targetNumber=Integer.parseInt(a.substring(a.indexOf("/")+1,a.indexOf("AT")));
                String date=a.substring(a.indexOf("*"),a.lastIndexOf("*"));
                Log.i("Read from file", goalName+completedNumber+targetNumber);

                Goal tempGoal=new Goal(goalName,targetNumber,completedNumber);
                tempGoal.addDateChecked(date);
                goalArrayList.add(new Goal(goalName,targetNumber,completedNumber));

            }
        }catch (Exception e){
            e.printStackTrace();
            Log.i("Read from file", "Error");
            return new ArrayList<>();
        }

        return goalArrayList;


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
