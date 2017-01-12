package com.pinasystems.servicebasketv2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;

public class AccountActivity extends AppCompatActivity {
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private EditText editTextname,editTextfinalInfo;
    private String userId,account,name,profile_status,loginwith,finalinfo;
    private boolean loginwithemail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        editTextname = (EditText) findViewById(R.id.name);
        editTextfinalInfo = (EditText)findViewById(R.id.finalinfo);
        userId = ((DataBank) getApplication()).getUserId();
        loginwith =((DataBank)getApplication()).getTag();
        if(loginwith.equalsIgnoreCase("telno")){
            editTextfinalInfo.setHint("Email");
            loginwithemail = false;
        }else if(loginwith.equalsIgnoreCase("email")){
            editTextfinalInfo.setHint("Mobile Number");
            editTextfinalInfo.setInputType(InputType.TYPE_CLASS_PHONE);
            loginwithemail = true;
        }

        addListenerOnButton();

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

    }

    public void addListenerOnButton() {

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        Button btnSubmit = (Button) findViewById(R.id.buttonsubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int selected = radioGroup.getCheckedRadioButtonId();
                radioButton = (RadioButton) findViewById(selected);
                if (radioButton != null) {
                    String choice = radioButton.getText().toString();
                    if (choice.equalsIgnoreCase("yes")) {
                        account = "provider";
                        profile_status = "1";
                    } else {
                        account = "requester";
                        profile_status = "0";
                    }
                    getData();
                } else {
                    Toast.makeText(getApplicationContext(), "Please select 1 option to continue", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void getData() {
        View focusView = null;
        boolean terminate = false;
        name = editTextname.getText().toString();
        finalinfo = editTextfinalInfo.getText().toString();
        if (TextUtils.isEmpty(name)) {
            editTextname.setError(getString(R.string.error_field_required));
            focusView = editTextname;
            terminate = true;
        }else{
            ((DataBank)getApplication()).setName(name);
        }
        if(TextUtils.isEmpty(finalinfo)){
            editTextfinalInfo.setError(getString(R.string.error_field_required));
            focusView = editTextfinalInfo;
            terminate = true;
        }
        if(loginwithemail){

            if(!isTelnoValid(finalinfo)){
                editTextfinalInfo.setError("Please enter a valid 10 digit number");
                focusView = editTextfinalInfo;
                terminate = true;
            }
            ((DataBank)getApplication()).setTelno(finalinfo);
        }else{
            if(!isEmailValid(finalinfo)){
                editTextfinalInfo.setError(getString(R.string.error_invalid_email));
                focusView = editTextfinalInfo;
                terminate = true;
            }
            ((DataBank)getApplication()).setEmail(finalinfo);
        }
        if (terminate) {
            focusView.requestFocus();
        } else {
            sendData();
        }
    }

    ProgressDialog loading;

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isTelnoValid(String telno){
        return (TextUtils.isDigitsOnly(telno) && telno.length() == 10);
    }

    private void sendData() {
        class Register extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ((DataBank)getApplication()).setAccount(account);
                loading = ProgressDialog.show(AccountActivity.this, "Updating...", "Wait...", false, false);
                loading.show();
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String, String> params = new HashMap<>();

                params.put(AppConfig.ACCOUNT, account);
                params.put(AppConfig.USER_ID,userId);
                params.put(AppConfig.NAME,name);
                params.put(AppConfig.MISSING,finalinfo);
                params.put(AppConfig.ACTION,loginwith);

                Requesthandler rh = new Requesthandler();
                String res = rh.sendPostRequest(AppConfig.SET_ACCOUNT_TYPE, params);
                return res;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (check_Response(s)) {
                    changeProfileStatus();
                }else{
                    View FocusView = editTextfinalInfo;
                    FocusView.requestFocus();
                    editTextfinalInfo.setError("Already in database");
                }
                Toast.makeText(AccountActivity.this, s, Toast.LENGTH_LONG).show();
            }
        }

        Register r = new Register();
        r.execute();
    }

    private boolean check_Response(String response){
        return response.equalsIgnoreCase("success");
    }

    private void nextActivity(){
        storeDataInMemory();
        if(account.matches("provider")){
            Intent intent = new Intent(getApplicationContext(),EnterProviderActivity.class);
            startActivity(intent);
            finish();
        }else if(account.matches("requester")){
            Intent intent = new Intent(getApplicationContext(),RequesterMainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void changeProfileStatus()
    {
        final String action = "write";

        class AddSend extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String, String> params = new HashMap<>();
                params.put(AppConfig.USER_ID,userId);
                params.put(AppConfig.PROFILESTATUS,profile_status);
                params.put(AppConfig.ACTION,action);
                Requesthandler rh = new Requesthandler();
                String res = rh.sendPostRequest(AppConfig.PROFILE_STATUS, params);
                return res;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                nextActivity();
            }
        }
        AddSend ar = new AddSend();
        ar.execute();
    }

    private void storeDataInMemory(){
        SharedPreferences.Editor editor = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(AppConfig.PREF_DATA,account);
        editor.apply();
    }
}