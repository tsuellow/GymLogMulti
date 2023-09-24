package com.example.android.gymlogmulti.data;

import android.util.Log;

import com.example.android.gymlogmulti.utils.MiscellaneousUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

public class Receptionist {
    public String userName;
    public String phone;
    public String password = MiscellaneousUtils.randomString(6);


    public Receptionist(String userName, String phone) {
        this.userName = userName;
        this.phone = phone;
    }

    public Receptionist(String blob) {
        String[] parts=blob.split("&&");
        this.userName = parts[0];
        this.phone = parts[1];
        this.password = parts[2];
    }

    public String toBlob(){
        return userName+"&&"+phone+"&&"+password;
    }


    public static String blobToReceptionistArray(String superBlob){
        String result="";
        try{
            JSONArray jsonResult=new JSONArray();
            ArrayList<String> list=new ArrayList<String>(Arrays.asList(superBlob.split("###")));
            for (String blob:list){
                JSONObject element=new JSONObject();
                Receptionist recep=new Receptionist(blob);
                element.put("username",recep.userName);
                element.put("password",recep.password);
                jsonResult.put(element);
            }
            result=jsonResult.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d("kerson",result);
        return result;
    }


}
