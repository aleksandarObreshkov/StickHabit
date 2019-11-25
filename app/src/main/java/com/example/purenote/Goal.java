package com.example.purenote;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import static java.security.AccessController.getContext;

//TODO ima problem s resetwaneto, sled kato e minala primerno sedmica, unchekva poleto, ama ne svalq progresa, a chak sled oshte edin restart


public class Goal   {

    interface GoalsDownloadedListener{
        void goalsDownloaded(File file);
    }


    private String text;
    private int completedSteps;
    private int targetSteps;
    private ArrayList<String> datesChecked;
    private String repeatCycle;
    private boolean done;
    private boolean checked;
    private String lastDateChecked;
    private final StorageReference storageRef=FirebaseStorage.getInstance().getReference();
    private final FirebaseAuth mAuth=FirebaseAuth.getInstance();

    private static  GoalsDownloadedListener listener;



    public Goal(String text,int target){
        this.text=text;
        this.targetSteps=target;
        this.completedSteps=0;
        datesChecked=new ArrayList<>();
        this.checked=false;


    }

    public static void setListener(GoalsDownloadedListener newListener){
        listener=newListener;
    }

    public void setLastDateChecked(String lastDateChecked) {
        this.lastDateChecked = lastDateChecked;
    }


    public String getLastDateChecked() {
        if(this.lastDateChecked==null){
            return "";
        }
        return lastDateChecked;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
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
        return this.datesChecked.size();
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

    public static void writeGoalInFile(Goal goal, int code){






        String text=goal.getText();
        int targetSteps=goal.getTargetSteps();
        String repeatCycle=goal.getRepeatCycle();
        String sFileName=text+".txt";
        boolean done=goal.isDone();
        File root = new File(Environment.getExternalStorageDirectory(), "PureNote");
        if (!root.exists()) {
            root.mkdirs();
        }
        File gpxfile = new File(root, sFileName);


        try (FileWriter writer=new FileWriter(gpxfile, true)){


            if(code==1){
                writer.append(text);
                writer.append("\n");
                writer.append(targetSteps+"");
                writer.append("\n");
                writer.append(repeatCycle);
                //dates start from here
                writer.append("\n");
                writer.append("-");
                writer.append("\n");
            }

            if (code==2&&goal.isChecked()){
                writer.append(goal.getLastDate());
                writer.append("\n");
            }




            if (done||Goal.compareDates(goal)){
                writer.append("\n");
                writer.append("-");
                writer.append("\n");
            }



            writer.flush();



        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Write in file","Error IO");
        }


            goal.uploadFile();



    }


    public static ArrayList<Goal> readGoalFromFile(){

        File root = new File(Environment.getExternalStorageDirectory(), "PureNote");
        File[] filesGoals=root.listFiles();
        ArrayList<String> goalString=new ArrayList<>();
        ArrayList<Goal> goalsFromFile=new ArrayList<>();








            for (File file:filesGoals) {

                try(Scanner scan=new Scanner(file)){
                while (scan.hasNext()){
                    goalString.add(scan.nextLine());

                }
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                    Log.i("Read from file", "File not found");


                }
                catch (NullPointerException npe){
                    npe.printStackTrace();
                    Log.i("Read goal from file","Scanner error");

                }

                Goal tempGoal=decode(goalString);
                goalsFromFile.add(tempGoal);
                goalString.clear();

            }




        return goalsFromFile;


    }


    public static Goal decode(ArrayList<String> dataFromFile){
        Goal tempGoal=null;

        //Separating the name and the target steps
        String goalName=dataFromFile.get(0);
        int targetNumber=Integer.parseInt(dataFromFile.get(1));
        String repeatCycle=dataFromFile.get(2);
        tempGoal=new Goal(goalName,targetNumber);
        tempGoal.setRepeatCycle(repeatCycle);









        int i=dataFromFile.size()-1;

        if(dataFromFile.get(i).equals("-")){
            String tempString=dataFromFile.get(i-1);
            if(!tempString.equals("daily")&&
                    !tempString.equals("weekly")&&
                    !tempString.equals("monthly")&&
                    !tempString.equals("yearly")){
                tempGoal.setLastDateChecked(tempString);
            }
            else {
                tempGoal.datesChecked=new ArrayList<>();
            }
        }
        else {
            while (true){

                if(dataFromFile.get(i).equals("-"))break;
                else {
                    tempGoal.addDateChecked(dataFromFile.get(i));
                    i--;
                }
            }
        }










        return tempGoal;

    }


    public static boolean compareDates(Goal goal) {
        ArrayList<String> datesChecked = goal.getDatesChecked();
        int progress = goal.getCompletedSteps();
        int allDatesNumber = datesChecked.size();


        if (!datesChecked.isEmpty()) {
            String repeatCycle = goal.getRepeatCycle();
            String firstDate = datesChecked.get(allDatesNumber - progress);
            String currentDate=MainActivity.getFormattedDate();
            Log.i("first date", firstDate);


            int day = Integer.parseInt(firstDate.substring(0, firstDate.indexOf("/")));
            int month = Integer.parseInt(firstDate.substring(firstDate.indexOf("/") + 1, firstDate.lastIndexOf("/")));
            int year = Integer.parseInt(firstDate.substring(firstDate.lastIndexOf("/") + 1));


            int currentDay = Integer.parseInt(currentDate.substring(0, currentDate.indexOf("/")));
            int currentMonth = Integer.parseInt(currentDate.substring(currentDate.indexOf("/") + 1, currentDate.lastIndexOf("/")));
            int currentYear = Integer.parseInt(currentDate.substring(currentDate.lastIndexOf("/") + 1));




            if (repeatCycle.equals("daily")) {
                if(month==currentMonth){
                    if(day<currentDay){
                        return true;
                    }
                }
                else {
                    if (day>currentDay) return true;
                }
            }

            if (repeatCycle.equals("weekly")){

                switch (month%2){
                    case 1:{
                        if(currentMonth!=month){
                            if(31-day+currentDay>=7)return true;
                        }
                        else {
                            if(currentDay-day>=7)return true;
                        }
                    }

                    case 0:{
                        //TODO list cases with february

                        if(month==8){
                            if(currentMonth!=month){
                                if(31-day+currentDay>=7)return true;
                            }
                            else {
                                if(currentDay-day>=7)return true;
                            }
                        }
                        if(currentMonth!=month){
                            if(30-day+currentDay>=7)return true;
                        }
                        else {
                            if(currentDay-day>=7)return true;
                        }
                    }



                }
            }


            if (repeatCycle.equals("monthly")){

                if(currentMonth!=month){
                    if(31-day+currentDay>=30)return true;
                }
                else {
                    if(currentDay-day>=30)return true;
                }



            }

            if (repeatCycle.equals("yearly")){

                if((year<currentYear)&&((month<=currentMonth)||(day<=currentDay)))return true;
            }







        }
        return false;
    }

    public boolean deleteFile() {

        File dir = new File(Environment.getExternalStorageDirectory(),"PureNote");
        String goalName=this.getText()+".txt";
        boolean isDeleted=false;
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            Log.i("Directory goal", goalName);
            for (int i = 0; i < children.length; i++)
            {
                Log.i("Directory", children[i]);

                if(goalName.equals(children[i])){
                    isDeleted=new File(dir, children[i]).delete();

                    break;
                }
            }

        }

        StorageReference fileToDelete=this.storageRef.child(this.mAuth.getCurrentUser().getUid()+"/"+goalName);
        fileToDelete.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Delete", "online storage: deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Delete", "online storage: not deleted");
            }
        }
        );


        return isDeleted;

    }

    public boolean changeGoalNameInFile(String newName){

        boolean result=true;
        Goal goal=Goal.this;
        ArrayList<String> goalString=new ArrayList<>();

        File root = new File(Environment.getExternalStorageDirectory(), "PureNote");

        if (!root.exists()) {
            root.mkdirs();
        }

        File file = new File(root, goal.getText()+".txt");

        try(Scanner scan=new Scanner(file)){
                while (scan.hasNext()){
                    goalString.add(scan.nextLine());

                }

                goalString.set(0,newName);
                file.delete();


                File newFile=new File(root, newName+".txt");

                FileWriter writer;

                writer = new FileWriter(newFile);

            for (String a:goalString) {
                writer.append(a);
                writer.append("\n");
            }

            writer.flush();
            writer.close();


        }catch (FileNotFoundException e){
            result=false;
            e.printStackTrace();
            Log.i("Change goal name", "File not found");

        }
        catch (NullPointerException npe){
            result=false;
            npe.printStackTrace();
            Log.i("Change goal name","No files");
        }

        catch (IOException ioe){
            result=false;
            ioe.printStackTrace();
            Log.i("Change goal name","FileWriter error");
        }

        return result;
    }

    public void deleteLastDateFromFile(){
        Goal goal=Goal.this;

        ArrayList<String> goalString=new ArrayList<>();

        File root = new File(Environment.getExternalStorageDirectory(), "PureNote");

        if (!root.exists()) {
            root.mkdirs();
        }

        File file = new File(root, goal.getText()+".txt");

        try(Scanner scan=new Scanner(file)){
            while (scan.hasNext()){
                goalString.add(scan.nextLine());

            }

            goalString.remove(goalString.size()-1);

            FileWriter writer;

            writer = new FileWriter(file);

            for (String a:goalString) {
                writer.append(a);
                writer.append("\n");
            }

            writer.flush();
            writer.close();



        }catch (FileNotFoundException e){
            e.printStackTrace();
            Log.i("Delete date from file", "File not found");

        }
        catch (NullPointerException npe){
            npe.printStackTrace();
            Log.i("Delete date from file","No files");
        }

        catch (IOException ioe){
            ioe.printStackTrace();
            Log.i("Delete date from file","FileWriter error");
        }

    }

    public void changeRepeatCycle(String newRepeatCycle){


        Goal goal=Goal.this;

        ArrayList<String> goalString=new ArrayList<>();

        File root = new File(Environment.getExternalStorageDirectory(), "PureNote");

        if (!root.exists()) {
            root.mkdirs();
        }

        File file = new File(root, goal.getText()+".txt");

        try(Scanner scan=new Scanner(file)){
            while (scan.hasNext()){
                goalString.add(scan.nextLine());

            }

            goalString.set(2,newRepeatCycle);

            FileWriter writer;

            writer = new FileWriter(file);

            for (String a:goalString) {
                writer.append(a);
                writer.append("\n");
            }

            writer.flush();
            writer.close();





    }catch (FileNotFoundException e){
            e.printStackTrace();
            Log.i("Delete date from file", "File not found");

        }
        catch (NullPointerException npe){
            npe.printStackTrace();
            Log.i("Delete date from file","No files");
        }

        catch (IOException ioe){
            ioe.printStackTrace();
            Log.i("Delete date from file","FileWriter error");
        }
    }

    public void uploadFile(){

        Goal goal=Goal.this;
        File root = new File(Environment.getExternalStorageDirectory(), "PureNote");

        if (!root.exists()) {
            root.mkdirs();
        }

        File file = new File(root, goal.getText()+".txt");
        Uri fileUri = Uri.fromFile(file);
        StorageReference goalRef = storageRef.child(mAuth.getCurrentUser().getUid()+"/"+goal.getText()+".txt");
        Log.i("Upload", mAuth.getCurrentUser().getUid());

        goalRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Log.i("Upload", "Successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.i("Upload", "Unsuccessful");

                    }
                });
    }

    public static  Goal singleFileDecode(File file) {
        Goal tempGoal = null;

        ArrayList<String> goalString = new ArrayList<>();

        try (Scanner scan=new Scanner(file)){

            while (scan.hasNext()) {
                goalString.add(scan.nextLine());

            }
            tempGoal = decode(goalString);
            scan.close();

        }catch (FileNotFoundException e){
            e.printStackTrace();
            Log.i("Read from file", "File not found");


        }
        catch (NullPointerException npe){
            npe.printStackTrace();
            Log.i("Read goal from file","Scanner error");

        }

        return tempGoal;
    }

    public static void  downloadFile(){


            StorageReference reference=FirebaseStorage.getInstance().getReference();
            FirebaseAuth auth=FirebaseAuth.getInstance();





            try {
                final StorageReference goalRef = reference.child(auth.getCurrentUser().getUid());


                goalRef.listAll()
                        .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        Log.i("Download", "Items listed");



                        if(!listResult.getItems().isEmpty()) {


                            for (StorageReference a : listResult.getItems()) {
                                String objectString = a.toString();
                                String goalString = objectString.substring(objectString.lastIndexOf("/") + 1, objectString.lastIndexOf("."));


                                File root = new File(Environment.getExternalStorageDirectory(), "PureNote");

                                if (!root.exists()) {
                                    root.mkdirs();
                                }

                                final File fileToDownload = new File(root, goalString + ".txt");


                                a.getFile(fileToDownload)
                                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        Log.i("Download", "Success");
                                        //TODO make a simple method to read a single file to array and call it in MainActivity
                                        listener.goalsDownloaded(fileToDownload);



                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("Download", "Doesn't work");
                                    }
                                });

                            }

                        }

                    }


                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Download", "Items not listed");
                        e.printStackTrace();
                    }
                });


            }catch (NullPointerException npe){
                npe.printStackTrace();
                Log.i("DownloadFile", "Error");
            }






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
