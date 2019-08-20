package com.example.purenote;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class Goal {
    private String text;
    private int completedSteps;
    private int targetSteps;
    private ArrayList<String> datesChecked;
    private String repeatCycle;
    private boolean done;



    public Goal(String text,int target,int completed){
        this.text=text;
        this.targetSteps=target;
        this.completedSteps=completed;
        datesChecked=new ArrayList<>();

    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }


    public void setRepeatCycle(String repeatCycle) {
        this.repeatCycle = repeatCycle;
    }

    public String getRepeatCycle() {
        return repeatCycle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCompletedSteps() {
        return this.completedSteps;
    }

    public void setCompletedSteps(int completed) {
        this.completedSteps = completed;
        if(this.completedSteps==this.targetSteps)done=true;
        else done=false;
    }

    public int getTargetSteps() {
        return this.targetSteps;
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

            return "08/6/2001";
        }
        else{
            return datesChecked.get(datesChecked.size()-1);

        }
    }

    public static void writeGoalInFile(String text,int completedSteps, int targetSteps,ArrayList<String> dates, String repeatCycle){

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
                String progress=completedSteps+"/"+targetSteps;
                writer.append(progress);
                writer.append("\n");
                writer.append(repeatCycle);

                for (int i=0;i<dates.size();i++){
                    writer.append("\n");
                    writer.append(dates.get(i));
                }




                writer.flush();



            writer.close();

            Goal.uploadFile(gpxfile);

        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Write in file","Error IO");
        }


    }


    public static void writeGoalInFile(Goal goal){


        String text=goal.getText();
        int completedSteps=goal.getCompletedSteps();
        Log.i("completed steps",completedSteps+"");
        int targetSteps=goal.getTargetSteps();
        ArrayList<String> dates=goal.getDatesChecked();
        FileWriter writer=null;
        String repeatCycle=goal.getRepeatCycle();
        String sFileName=text+".txt";
        boolean done=goal.isDone();


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
            String progress=completedSteps+ "/"+targetSteps;
            writer.append(progress);

            writer.append("\n");
            writer.append(repeatCycle);


            for (int i=0;i<dates.size();i++){
                writer.append("\n");
                writer.append(dates.get(i));
                Log.i("Dates to write", dates.get(i));
            }

            if (done||Goal.compareDates(goal)){
                writer.append("\n");
                writer.append("-");
            }






            writer.flush();



            writer.close();

            Goal.uploadFile(gpxfile);

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
        String repeatCycle=dataFromFile.get(1);
        tempGoal=new Goal(goalName,targetNumber,completedNumber);
        tempGoal.setRepeatCycle(repeatCycle);



        ArrayList<String> tempDates=new ArrayList<>();



        //Iterator to input all the dates on which the goal has been done(checked)
        //starts from 2 to skip the first two lines, which include the name, the progress,  and the repeat cycle

        for (int i=2;i<dataFromFile.size();i++){



                if (!dataFromFile.get(i).equals("-")){
                    tempGoal.addDateChecked(dataFromFile.get(i));
                }
                Log.i("Read from file", "Dates:"+dataFromFile.get(i));

        }










        return tempGoal;

    }

    public static void uploadFile(File fileToUpload){

         FirebaseAuth mAuth=FirebaseAuth.getInstance();

        StorageReference storageFirebase= FirebaseStorage.getInstance().getReference();

        try {
            Uri file = Uri.fromFile(fileToUpload);
            StorageReference tempRef = storageFirebase.child(mAuth.getUid());

            tempRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });
        }catch (NullPointerException npe){
            npe.printStackTrace();
            Log.i("upload task","geUid returned null");
        }

        catch (Exception e){
            e.printStackTrace();
            Log.i("upload task", "probably no internet");
        }
    }

    public static boolean compareDates(Goal goal) {
        ArrayList<String> datesChecked = goal.getDatesChecked();
        int progress = goal.getCompletedSteps();
        int allDatesNumber = datesChecked.size();


        if (!datesChecked.isEmpty()) {
            String repeatCycle = goal.getRepeatCycle();
            String firstDate = datesChecked.get(allDatesNumber - progress);
            Log.i("first date", firstDate);


            int day = Integer.parseInt(firstDate.substring(0, firstDate.indexOf("/")));
            int month = Integer.parseInt(firstDate.substring(firstDate.indexOf("/") + 1, firstDate.lastIndexOf("/")));
            int year = Integer.parseInt(firstDate.substring(firstDate.lastIndexOf("/") + 1));

            Calendar tempCal = Calendar.getInstance();
            tempCal.setTime(new Date());


            if (repeatCycle.equals("daily")) {
                if (day > Calendar.DAY_OF_WEEK-1) return true;
            }

            if (repeatCycle.equals("weekly")){
                int lastweek = Calendar.DAY_OF_WEEK - 7;
                if(day>lastweek) return true;
            }


            if (repeatCycle.equals("monthly")){
                int lastMonth = Calendar.MONTH-1;
                if(month>lastMonth) return true;
            }

            if (repeatCycle.equals("yearly")){
                int lastYear=Calendar.YEAR-1;
                if(year>lastYear)return true;
            }




            // Get current date


            // 7 days ago






        }
        return false;
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
        builder.append(repeatCycle);
        builder.append(" ");

        return builder.toString();
    }
}
