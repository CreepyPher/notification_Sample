package com.example.notification_sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.notification_sample.Utils.Constants;
import com.example.notification_sample.networl.ApiClient;
import com.example.notification_sample.networl.ApiService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main1 extends AppCompatActivity {

    EditText Uname,Message;
    Button send,Save;
    FirebaseFirestore db;

    private static String YOURTOKEN;


    private static String USER_NAME,TOKEN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        db = FirebaseFirestore.getInstance();

        Uname = findViewById(R.id.txtUserName);
        Message = findViewById(R.id.txtmessage);
        Save = findViewById(R.id.btnSaveToken);
        send = findViewById(R.id.btnSendM);

        Save.setOnClickListener(view -> {
            getToken(Uname.getText().toString());
        });
        send.setOnClickListener(v -> {
            String Msg = Message.getText().toString().trim();
            if(!Msg.isEmpty()){
                sendMessage();
            }else
                showToast("fill the field first!");
        });
    }
    private void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
    private void getToken(String Uname){

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful()){
                            return;
                        }
                        String token = task.getResult();
                        YOURTOKEN = token;
                        SaveFCMToken(Uname,token);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Failed to get Token", Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void SaveFCMToken(String User,String FCMToken){
        // Create a new user with a first and last name
        HashMap<String, Object> userToken = new HashMap<>();
        userToken.put("User", User);
        userToken.put("FCMTOKEN",FCMToken);

// Add a new document with a generated ID
        db.collection("usersTOKEN")
                .add(userToken)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(),"Save!", Toast.LENGTH_LONG).show();

                        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isRegistered", true);
                        editor.putString("Uname",User);
                        editor.putString("YourToken", FCMToken);
                        editor.commit();

                        Uname.setVisibility(View.INVISIBLE);
                        Save.setVisibility(View.INVISIBLE);
                        send.setVisibility(View.VISIBLE);
                        Message.setVisibility(View.VISIBLE);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Failed to Save!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean isRegistered = sharedPreferences.getBoolean("isRegistered", false);
        USER_NAME = sharedPreferences.getString("Uname","");
        TOKEN = sharedPreferences.getString("YourToken","");

        if(isRegistered){
            Uname.setVisibility(View.INVISIBLE);
            Save.setVisibility(View.INVISIBLE);
            send.setVisibility(View.VISIBLE);
            Message.setVisibility(View.VISIBLE);
        }
    }

    private void sendMessage() {
        if(YOURTOKEN.length()==0) {
            try {

                JSONArray tokens = new JSONArray();
                tokens.put(YOURTOKEN);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_FCM_TOKEN,YOURTOKEN);
                data.put(Constants.KEY_MESSAGE,
                        Message.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

                sendNotification(body.toString());

            } catch (Exception ex) {
                showToast(ex.getMessage());
            }
        }
    }
    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()){
                    try {
                        if(response.body() != null){
                            JSONObject responseJSON =  new JSONObject(response.body());
                            JSONArray results = responseJSON.getJSONArray("results");
                            if (responseJSON.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                //   showToast(error.getString("error"));
                                return;
                            }
                        }
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
                    //  showToast("Notification send successfully!");
                }
                else{
                    showToast("ERROR: "+response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }
}