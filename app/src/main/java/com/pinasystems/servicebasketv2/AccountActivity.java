package com.pinasystems.servicebasketv2;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.HashMap;

public class AccountActivity extends AppCompatActivity {
    private EditText editTextname,editTextfinalInfo;
    private String userId,account,name,profile_status,loginwith,finalinfo;
    private boolean loginwithemail;
    private InterstitialAd mInterstitialAd;

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
        btnSubmit = (Button) findViewById(R.id.buttonsubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnButton();
            }
        });

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        TextView textViewtandc = (TextView) findViewById(R.id.terms_and_conditions);
        textViewtandc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewTermsAndConditions();
            }
        });

        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.video_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                btnSubmit.setEnabled(true);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                btnSubmit.setEnabled(true);
            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.
                if(proceed){
                    nextActivity();
                }
            }
        });
        return interstitialAd;
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void viewTermsAndConditions(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Terms of Use – Pina Systems");
        builder.setMessage(tandc);
        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    Button btnSubmit;

    public void OnButton() {

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        int selected = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(selected);
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
            showInterstitial();
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

    boolean proceed = false;

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
                return rh.sendPostRequest(AppConfig.SET_ACCOUNT_TYPE, params);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (check_Response(s)) {
                    changeProfileStatus();
                    proceed = true;
                }else{
                    View FocusView = editTextfinalInfo;
                    FocusView.requestFocus();
                    proceed = false;
                    editTextfinalInfo.setError("Already in database");
                    mInterstitialAd = new InterstitialAd(getApplicationContext());
                    loadInterstitial();
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
                return rh.sendPostRequest(AppConfig.PROFILE_STATUS, params);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
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

    final String tandc = "Welcome to Service Basket by Pina Systems. You are currently on the Terms of Use page of Pina Systems Mobile Application (mentioned as 'Pina Systems'/' Mobile Application'/'we', 'us' /'Company' hereon). Pina Systems Mobile Application is an aggregator platform for variety of services and enables the connection between individuals seeking to obtain services (“Pina Systems Customer/User”) and/or individuals seeking to provide services (“Pina Systems Service Provider”). Pina Systems Customers are hereinafter referred to as “Users” and individually as “User”. Those certain services requested by the Pina Systems Customer, which are to be completed by the Pina Systems Service Provider, are hereinafter referred to as “Services”.\n" +
            "We strongly recommend that you go through the Terms of use mentioned on this page attentively before browsing the content on the other pages of the website or conducting any activity on it.\n" +
            "Pina Systems does not provide the services listed and Pina Systems is not a service provider. It is a SOFTWARE / Technology company which enables the user to book a Service. It is solely upto the third party Service Providers to offer the services which may be scheduled through the use of the Website or our Android / iOS App. Pina Systems provides the information and means to obtain such third party services, but does not and does not intend to provide the services or act in any way as a service provider and has no responsibility or liability for any services provided to you by such third parties Service Providers.\n" +
            "Please note that by availing/offering any service on this website, or generally browsing through this website, you agree to these Terms of use, privacy policy, security terms, code of conduct and any other policies mentioned on this website. The term 'you' refers to the user or viewer of our website.\n" +
            "In addition to the Content (in form of text, images, moving images, forms) on the  Mobile Application, the  Mobile Application provides you with opportunities to submit content on the site. By using our website and posting comments on it, you accept the terms of use as mentioned below. In case you choose not to agree to these terms of use, we advise you not to use Pina Systems Mobile Application. We reserve the right to modify or amend the terms of use without notice, and your continued use of the website in any form after these changes will mean that you accept the revised terms of use.\n" +
            "These Terms of Use also incorporate by reference all of Company's policies, fee schedules, code of conduct, privacy policy, rules, guidelines, Terms of use on the  Mobile Application related to the offering and accepting of services. By using this  Mobile Application, you agree to all such Terms of use.\n" +
            "User Agreement\n" +
            "User / users means any individual or business entity or organization that legally operates in India or in other countries, uses and has the right to use the Services provided by Pina Systems. You must create an account on Pina Systems Mobile Application to request/offer service. Please make sure that you provide complete and correct information on the registration form to enable us to serve you better.\n" +
            "You are the sole authorized user of your account. You are responsible for maintaining the confidentiality of any password and account number provided by you or Pina Systems for accessing the Service. You are solely and fully responsible for all activities that occur under your password or account. Pina Systems has no control over the use of any User's account and expressly disclaims any liability derived there from. Should you suspect that any unauthorized party may be using your password or account or you suspect any other breach of security, you will contact Pina Systems immediately.\n" +
            "You are advised not to use someone else's account for any objective at any time. It would lead to you being immediately barred from using the website. Pina Systems Mobile Application might charge you a fine or take legal action against you for unlawfully accessing someone else's account.\n" +
            "The Services provided by Pina Systems is a technology based service which enables the hiring of Servicemen for a home based solution of problems in categories such as cleaning, appliances, electronics, laundry, beauty, etc, through the internet and / or mobile telecommunications devices. Our Services are available only to those individuals or companies who can form legally binding contracts under the applicable law. You may access the Pina Systems Website/App and the Services only if You are 18 years of age or older and are legally capable of entering into a binding contract as per applicable law, including, in particular, the Indian Contract Act, 1872.\n" +
            "Pina Systems advises its users that while accessing the Mobile Application, they must follow/abide by the related laws. We are not responsible for the possible consequences caused by your behavior during use of  Mobile Application. Pina Systems may, in its sole discretion, refuse the service to anyone at any time.\n" +
            "Pina Systems reserves the right to collect user data including name, contact information, physical location and other details to facilitate services or use of its platform to avail services. All information collected from the user are on a bonafide basis. User accounts bearing contact number and email IDs are created and owned by Pina Systems. Any promotional discounts, offers and reward points accumulated can be revoked without prior notice in the event of suspicious account activity or malafide intent of the user.\n" +
            "In the case where the system is unable to establish unique identity of the user against a valid mobile number or e-mail ID, the account shall be indefinitely suspended. Pina Systems reserves the full discretion to temporarily or permanently suspend a user's account in the above event and does not have the liability to share any account information whatsoever.\n" +
            "Pina Systems may change, modify, amend, or update this agreement from time to time without any prior notification to users and the amended and restated terms and conditions of use shall be effective immediately on posting. If you do not adhere to the changes, you must stop using the service. Your continuous use of the Services will signify your acceptance of the changed terms.\n" +
            "Service and Service Providers:\n" +
            "The following Terms & Conditions shall apply to customers utilizing the Services offered by Pina Systems:\n" +
            "Pina Systems Mobile Application is a neutral platform enabling connections between users and service providers.\n" +
            "Although Pina Systems Mobile Application does perform background checks of service providers, Pina Systems Mobile Application does not assume any responsibility for the accuracy or reliability of this information or any information on the Service.\n" +
            "Neither Pina Systems Mobile Application nor its affiliates or licensors is responsible for the conduct, whether online or offline, of any service provider of the service. Pina Systems Mobile Application and its affiliates and licensors will not be liable for any claim, injury or damage arising in connection with your use of the service.\n" +
            "While the service providers may have been verified and Pina Systems Mobile Application shall make the best of efforts to resolve a dispute, we would advise users to exhibit the best of their discretion to make a choice.\n" +
            "In case you have a dispute with one or more other service providers, you hereby release The Company/Pina Systems Mobile Application, its proprietor, officers, employees, executives and consultants from claims, demands and damages (actual and consequential) of every kind or nature. Website or company would have no liability for any loss or damage incurring to you due to any communications or dealings with any of the service seekers, service providers, or other users on the  Mobile Application.\n" +
            "We are committed to making your experience on the Mobile Application a helpful one. If you are a service seeker, once you put in your requirement for the service provider, the website would inform the service providers qualified for that job. However, a service provider would apply for your job at his/her own discretion. Pina Systems holds all rights to select or reject a Service Provider. Pina Systems also holds all rights to accept and reject requests coming from users.\n" +
            "You understand and agree that we make no warranties about the service seekers. While we would try our best to avoid any payment related issues, we cannot be held responsible for payments not being made by service seekers in any form, and we would not make any payments to service providers, in case service seekers fail to do so.\n" +
            "The service providers are executives of the company on contract, and the company is not an executive of the service providers.\n" +
            "The customer shall pay to Service Provider the service or labour cost, visit or transportation cost (if applicable), item cost (if applicable), convenience cost (if applicable) and any fee or levy presently payable or hereinafter imposed by the law or required to be paid for availing the Local Services.\n" +
            "Any reimbursement expenses that are incurred by a Pina Systems service provider in connection with the completion of a Service may, however, be paid in cash/cheque offline as deemed suitable by Service seeker and Service provider. Pina Systems will not be responsible for any monetary transaction between the seekers and providers.\n" +
            "Pina Systems Service seekers are obligated to pay for the services of the Service, unless specifically notified otherwise. Pina Systems Service seekers can also pay at the end of a completed Service transaction.\n" +
            "The Service may be used for your personal, non-commercial use only; you may not use the Service in connection with any commercial endeavors whatsoever without the express prior written consent of Pina Systems Mobile Application.\n" +
            "As a customer, you share not misuse, soil or damage any of the devices (technical/non-technical) of the Service Provider. You shall not ask the Service Provider to break any government rules for any purpose. The Service Provider has the right to refuse such a request. Misusing the Service or the Software for unlawful purposes or for causing nuisance, annoyance or inconvenience is prohibited.\n" +
            "The customer agrees and acknowledges that the use of the Services offered by Company is at the sole risk of the customer and that Company disclaims all representations and warranties of any kind, whether express or implied as to condition, suitability, quality, merchantability and fitness for any purposes are excluded to the fullest extent permitted by law.\n" +
            "The Company makes no representation or warranty that the Services will meet the customer's requirements or will be uninterrupted, timely, secure, or error-free.\n" +
            "The Company shall not be responsible or liable for any loss or damage, howsoever caused or suffered by the Customer arising out of the use of service offered by Company or due to the failure of Company to provide Services to the Customer for any reason whatsoever including but not limited to the Customer's non-compliance with the Services' recorded voice instructions, malfunction, partial or total failure of any network terminal, data processing system, computer tele-transmission or telecommunications system or other circumstances whether or not beyond the control of Company or any person or any organization involved in the above mentioned systems.\n" +
            "The Company is hereby authorized to use the location based information provided by any of the telecommunication companies when the Customer uses the mobile phone to make a Service Provider booking. The location based information will be used only to facilitate and improve the probability of locating a Service Provider for the Customer.\n" +
            "The Company shall not be liable for any conduct of the Servicemen. However, the Company encourages you to notify it of any complaints that you may have against any Serviceman that you may have hired using the Company's Services.\n" +
            "The Company shall be entitled to add to, vary or amend any or all these terms and conditions at any time and the Customer shall be bound by such addition, variation or amendment once such addition, variation or amendment are incorporated into these terms and conditions at Company's website at www.Pina Systems Mobile Application on the date that Company may indicate that such addition, variation or amendment is to come into effect.\n" +
            "All the calls made to the Company's call centre are recorded by the Company for quality and training purposes.\n" +
            "The Company has the right to use the customer contact information for its own marketing purposes. The Company may send regular SMS updates to the mobile numbers registered with it.\n" +
            "The courts of Mumbai, India shall have the sole and exclusive jurisdiction in respect of any matters arising from the use of the Services offered by Company or the agreement or arrangement between Company and the Customer. The language of the arbitration shall be English.\n" +
            "Intellectual Property Rights\n" +
            "Pina Systems is the sole owner and lawful licensee of all the rights to the  Mobile Application and its content.  Mobile Application content means its text, graphics, editorial content, data, formatting, graphs, designs, HTML, look and feel, photographs, music, sounds, images, software, videos, designs, typefaces, etc. The  Mobile Application content incorporate trade secrets and intellectual property rights protected under the Indian laws. All title, ownership and intellectual property rights in the  Mobile Application and its content shall remain with Pina Systems. Users may not copy, download, use, modify, alter, sell, transfer, redesign, reconfigure, or retransmit anything from the Service without Pina Systems's express prior written consent.\n" +
            "The information contained in this  Mobile Application is intended, solely to provide general information for personal use of the reader, who accepts full responsibility for its use. Pina Systems does not represent or endorse the accuracy or reliability of any information, or advertisements contained on, distributed through, or linked, downloaded or accessed from any of the Services contained on this  Mobile Application, or the quality of any products, information or other materials displayed, or obtained by you as a result of an advertisement or any other information or offer in or in connection with the Service.\n" +
            "We accept no responsibility for any errors or omissions, or for the results obtained from the use of this information. All information in this  Mobile Application is provided with no guarantee of completeness, accuracy, timeliness or of the results obtained from the use of this information, and without warranty of any kind, express or implied, including, but not limited to warranties of performance, merchantability and fitness for a particular purpose. Nothing herein shall to any extent substitute for the independent investigations and the sound technical and business judgment of the users. In no event shall Pina Systems be liable for any direct, indirect, incidental, punitive, or consequential damages of any kind whatsoever with respect to the Service. Users of this site must hereby acknowledge that any reliance upon any content shall be at their sole risk.\n" +
            "Pina Systems reserves the right, in its sole discretion and without any obligation, to make improvements to, or correct any error or omissions in any portion of the Service or the website.\n" +
            "All related icons and logos are applied for trademarks or service marks of Pina Systems.. in various jurisdictions and are protected under applicable copyright, trademark and other proprietary rights laws. The unauthorized copying, modification, use or publication of these marks is strictly prohibited.\n" +
            "Copyright\n" +
            "All content on this  Mobile Application is the copyright of Pina Systems. Systematic retrieval of Pina Systems content to create or compile, directly or indirectly, a collection, compilation, database or directory, whether through robots, spiders, automatic devices or manual processes without written permission from Pina Systems is prohibited.\n" +
            "In addition, use of the content for any purpose not expressly permitted in this Agreement is prohibited and may invite legal action. As a condition of your access to and use of Pina Systems's Services, you agree that you will not use the  Mobile Application service to infringe the intellectual property rights of others in any way. Pina Systems reserves the right to terminate the account of a user/users upon any infringement of the rights of others in conjunction with use of the Pina Systems service, or if Pina Systems believes that user/users conduct is harmful to the interests of Pina Systems, its affiliates, service providers, investors, or other users, or for any other reason in Pina Systems's sole discretion, with or without cause.\n" +
            "No Liability\n" +
            "YOU AGREE NOT TO HOLD PINA SYSTEMS, ITS AFFILIATES, ITS LICENSORS, OR ANY OF SUCH PARTIES, AGENTS, EMPLOYEES, OFFICERS, DIRECTORS, CORPORATE PARTNERS, OR PARTICIPANTS LIABLE FOR ANY DAMAGE, SUITS, CLAIMS, AND/OR CONTROVERSIES (COLLECTIVELY, “LIABILITIES”) THAT HAVE ARISEN OR MAY ARISE, WHETHER KNOWN OR UNKNOWN, RELATING TO YOUR USE OF OR INABILITY TO USE THE SERVICE, INCLUDING WITHOUT LIMITATION ANY LIABILITIES ARISING IN CONNECTION WITH THE CONDUCT, ACT OR OMISSION OF ANY USER (INCLUDING WITHOUT LIMITATION STALKING, HARASSMENTTHAT IS SEXUAL OR OTHERWISE, ACTS OF PHYSICAL VIOLENCE, AND DESTRUCTION OF PERSONAL PROPERTY), ANY DISPUTE WITH ANY USER, ANY INSTRUCTION, ADVICE, ACT, OR SERVICE PROVIDED BY PINA SYSTEMS OR ITS AFFILIATES OR LICENSORS AND ANY DESTRUCTION OF YOUR INFORMATION.\n" +
            "UNDER NO CIRCUMSTANCES WILL PINA SYSTEMS ITS AFFILIATES, ITS LICENSORS, OR ANY OF SUCH PARTIES' AGENTS, EMPLOYEES, OFFICERS, DIRECTORS, CORPORATE PARTNERS, OR PARTICIPANTS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, CONSEQUENTIAL, SPECIAL OR EXEMPLARY DAMAGES ARISING IN CONNECTION WITH YOUR USE OF OR INABILITY TO USE THE SERVICES, EVEN IF ADVISED OF THE POSSIBILITY OF THE SAME.\n" +
            "IF, NOTWITHSTANDING THE FOREGOING EXCLUSIONS, IT IS DETERMINED THAT PINA SYSTEMS OR ITS AFFILIATES, ITS LICENSORS, OR ANY OF SUCH PARTIES' AGENTS, EMPLOYEES, OFFICERS, DIRECTORS, CORPORATE PARTNERS, OR PARTICIPANTS IS LIABLE FOR DAMAGES, IN NO EVENT WILL THE AGGREGATE LIABILITY, WHETHER ARISING IN CONTRACT, TORT, STRICT LIABILITY OR OTHERWISE, EXCEED THE TOTAL FEES PAID BY YOU TO PINA SYSTEMS DURING THE THREE MONTHS PRIOR TO THE TIME SUCH CLAIM AROSE.\n" +
            "Indemnification\n" +
            "You hereby agree to indemnify, defend, and hold harmless Pina Systems, its directors, investors, officers, employees, agents, licensors, attorneys, independent contractors, providers, subsidiaries, and affiliates from and against any and all claim, loss, expense or demand of liability, including attorneys' fees and costs incurred, arising from your use or inability to use the Services.\n" +
            "Rescheduling and Cancellation Policy\n" +
            "To avoid cancellation charges (5% of total order) please reschedule or cancel the order 24 hours before the scheduled time.\n" +
            "Cancellation Policy:\n" +
            "Your service request may be rescheduled or cancelled anytime online through Pina Systems platform (web/app)\n" +
            "The cancellation charges will be 5% of the total order value.\n" +
            "If service request is cancelled 24 hours before the scheduled service time, the cancellation charges will NOT be applicable. If the service is cancelled after this, customer is liable to pay the cancellation charges\n" +
            "If the order is booked within 24 hours of scheduled service appointment, then cancellation charges will be applicable anyways\n" +
            "If the service is cancelled from Pina Systems end, then the cancellation charges will NOT be applicable\n" +
            "Due to any unforeseen circumstances or internal reasons, Pina Systems reserves the right to cancel any order at any point of time, and will not be liable to anyone for the same.\n" +
            "If the visit is cancelled by the service professional then customer is NOT liable to pay cancellation charges.\n" +
            "Laundry Service Terms and Conditions\n" +
            "Minimum order value is Rs 300. Please refer our price list before placing the order. A convenience charge of Rs 59 will be levied if the order value is less than Rs 300.\n" +
            "In case of designer clothes, bridal wear, lehengas, heavy curtains etc the final price, if different will be informed to you before processing your article.\n" +
            "In case of cancellation of an order after receiving the clothes, a cancellation fee of Rs 59 will be charged to the customer.\n" +
            "Please count the no. of clothes at the time of handing over of clothes. The exact number of clothes and type of clothes will be informed to the customer via email after receiving it at our facility. In case of any discrepancy Pina Systems count must be accepted as final.\n" +
            "Our price list is indicative. The final price will be informed via email/sms after receiving the clothes at our processing unit. You can make the payment online or through cash at the time of delivery.\n" +
            "Pina Systems strives to provide the best possible service to our customer, please note that there are certain risk involved in laundry of clothes.\n" +
            "Pina Systems tries it’s best to provide best quality cleaning according to industry standards but removal of stains from clothing articles is not 100% guaranteed.\n" +
            "Pina Systems does not take responsibility for color bleeding, shrinkage, minor damages of any sorts. We request the customer to be wary of that.\n" +
            "Pina Systems shall not entertain any complaints regarding mix-up/missing clothes if the same is not brought to our attention, at the time of delivery. Failure to inform us at the time of delivery constitutes waiver of a claim for any lost or damaged items.\n" +
            "For any report of damage, contact our customer care team within 24 hrs of delivery.\n" +
            "In case of loss or damage to a cloth, the company will try to rectify the damage. In case of failure in doing so, 10 times the amount of the service availed for, will be reimbursed.\n" +
            "Pina Systems will try to deliver the clothes in promised time slots but if due to some circumstances the delivery gets delayed, customer is not entitled for any discount or compensation for the same.\n" +
            "Any valuable items, removable clothing parts (buttons, latkan etc) must be removed by the customer before handing over the clothes. The company can not be held responsible for any loss in such a case.\n" +
            "Pina Systems reserves the right to amend the terms of service and prices with or without prior notice. The latest version of the terms of service, shall be available on our website. You understand and agree that if you use the Services after the date on which these Terms have changed, Pina Systems will treat your use as acceptance of the updated terms.\n" +
            "Terms and Conditions related to Cleaning service\n" +
            "Pina Systems tries to provide the best quality cleaning service according to industry standards but removal of stains is not 100% guaranteed.\n" +
            "For any additional request other than the Scope of Service, extra charges will be levied.\n" +
            "For any damage or loss of property, Pina Systems is not liable for any claim.\n" +
            "Valuables to be kept safe before the professionals reach your doorstep; we are not liable if any valuables are lost.\n" +
            "For any report on damage or loss, contact Pina Systems within 24 hours.\n" +
            "Wardrobes, Cabinets, Storage areas will not be cleaned internally, but if need be, the customer should first empty the same.\n" +
            "We do not use corrosive liquids or acids for cleaning purposes.\n" +
            "Heavy objects will not be moved and cleaned, if needed the customer has to arrange for the same.\n" +
            "The professionals will not clean inaccessible and unsafe places.\n" +
            "Post cleaning inspection should be done by the customer in the presence of the professionals and no-rework would be guaranteed if post inspection is done by a third party. However, if there are any unsatisfactory complaints, please contact our customer care team within 24 hours of the service.\n" +
            "Cancellation charges of Rs. 100 or 5% of the total order value whichever is higher is applicable to jobs cancelled within 24 hours of the scheduled service time. To avoid any cancellation charges we request you to cancel or re-schedule the job 24 hours prior to the scheduled service time.\n" +
            "If the order is booked within 24 hours of scheduled service appointment, then cancellation charges will be applicable.\n" +
            "If the service is cancelled from our end, then the cancellation charges are NOT applicable.\n" +
            "Due to any unforeseen circumstances or internal reasons, Pina Systems reserves the right to cancel the order at any point of time, and will not be liable for the same.\n" +
            "Terms and Conditions related to Beauty service\n" +
            "Service available for women customers only.\n" +
            "Minimum booking order is Rs. 500 for Mumbai and Rs. 800 for Delhi and Bangalore.\n" +
            "Beautician will carry all necessary products and equipment.\n" +
            "Customer to specify hair colour number for colour application services.\n" +
            "Valuables to be kept safe before the professionals reach your doorstep; we are not liable if any valuables are lost.\n" +
            "Terms and Conditions related to Pest Control service\n" +
            "All the chemicals used are Pest Control Association of India (PCAI) approved and are completely odorless, non messy and non smelly. All chemicals used are 100% safe and are not harmful to health.\n" +
            "Prior to the service please cover your food, utensils, toys, toothbrushes and other personal items.\n" +
            "It is advised to keep distance from the professionals while they mix and apply the chemicals.\n" +
            "Avoid direct contact with the chemicals, especially liquids that are still wet or if an obvious layer of insecticide powder remains.\n" +
            "It is advised to keep family members and pets away from the treated area for 4-6 hours or until the chemical have dried up.\n" +
            "After the pest-control is done, keep the doors and windows open to let fresh air pass through. This is very important for people with allergies.\n" +
            "Pina Systems tries to provide the best quality pest-control service according to industry standards but removal of bugs is not 100% guaranteed. For complete eradication multiple services might be required.\n" +
            "We offer a Guarantee only in our Annual Maintainance Contract (AMC).\n" +
            "For any damage or loss of property, Pina Systems is not liable for any claim.\n" +
            "Valuables to be kept safe before the professionals reach your doorstep; we are not liable if any valuables are lost.\n" +
            "For any report on damage or loss, contact Pina Systems within 24 hours.\n" +
            "Wardrobes, cabinets, storage areas to be emptied by the customer prior to the arrival of the professionals.\n" +
            "Customers are expected to help the professionals in moving heavy objects\n" +
            "Inaccessible and unsafe places will not be serviced by the professionals.\n" +
            "Terms and Conditions related to Appliance Repairs service\n" +
            "Minimum charges applicable (as listed on our website) if no service availed after inspection\n" +
            "The customer shall pay the service or labour cost, visit or inspection cost (if applicable), material cost (if applicable) and any fee or levy presently payable.\n" +
            "The customer shall ensure that he/she will not indulge in any of the following activities:\n" +
            "Misusing, soiling or damaging any of the devices (technical/non-technical) of the Serviceman.\n" +
            "Asking the Serviceman to break any government rules for any purpose. The Serviceman has the right to refuse such a request by the customer. The Serviceman also has the right to refuse such a work or leave such customer's house.\n" +
            "It is expressly made clear to you hereby that the Company does not own any service providers nor does it directly or indirectly employ any Servicemen. Servicemen are all supplied by third parties and the Company disclaims any and all liability in respect of the Servicemen and services provided alike.\n" +
            "Pina Systems is not liable for any damage or loss of property.\n" +
            "Public Areas\n" +
            "The Service may contain profiles, email systems, blogs, message boards, applications, job postings, chat areas, news groups, forums, communities and/or other message or communication facilities (“Public Areas”) that allow Users to communicate with other Users. You may only use such community areas to send and receive messages and material that are relevant and proper to the applicable forum. Without limitation, you may not:\n" +
            "Defame, abuse, harass, stalk, threaten or otherwise violate the legal rights of others.\n" +
            "Impersonate another person or allow any other person or entity to use your identification to post or view comments.\n" +
            "Use the Service for any purpose which is in violation of local, state, national, or international laws.\n" +
            "Publish, post, upload, distribute or disseminate any profane, defamatory, infringing, obscene or unlawful topic, name, material or information.\n" +
            "Upload files that contain software or other material that violates the intellectual property rights or rights of privacy of any third party.\n" +
            "Upload content that is offensive and/or harmful, including, but not limited to, content that advocates, endorses, condones or promotes racism, bigotry, hatred or physical harm of any kind against any individual or group of individuals.\n" +
            "Advertise or offer to sell any goods or services for any commercial purpose on the Service which are not relevant to the services offered on the Service.\n" +
            "Conduct or forward surveys, contests, pyramid schemes, or chain letters.\n" +
            "Upload files that contain viruses, Trojan horses, corrupted files, or any other similar software that may damage the operation of another's computer.\n" +
            "Post the same note repeatedly referred to as 'spamming'. Spamming is strictly prohibited.\n" +
            "Download any file posted by another User that a User knows, or reasonably should know, cannot be legally distributed through the Service.\n" +
            "Restrict or inhibit any other User from using and enjoying the Public Areas.\n" +
            "Imply or state that any statements you make are endorsed by Pina Systems, without the prior written consent of Pina Systems.\n" +
            "Use a robot, spider, manual and/or automatic processes or devices to data-mine, data- crawl, scrape or index Pina Systems in any manner.\n" +
            "Hack or interfere with Pina Systems Mobile Application, its servers or any connected networks.\n" +
            "Adapt, alter, license, sublicense or translate Pina Systems Mobile Application for your own personal or commercial use.\n" +
            "Remove or alter, visually or otherwise, any copyrights, trademarks or proprietary marks and rights owned by Pina Systems Mobile Application.\n" +
            "Upload content that provides materials or access to materials that exploit people under the age of 18 in an abusive, violent or sexual manner.\n" +
            "Do note that all submissions made to Public Areas will be public, and Pina Systems Mobile Application will not be responsible for the action of other Users with respect to any information or materials posted in Public Areas.\n" +
            "User Content\n" +
            "Permission to use:\n" +
            "In case you submit any content on the website (included but not limited to photographs and text) you grant us permission to use, reproduce, display, distribute and promote such content in any form, and media. However, as mentioned in the Privacy Policy, your personal details would not be made available in public domain without your prior permission. However, you shall be solely responsible if any content, material, photographs or text is found to be defamatory or derogative or pornographic or violent or hurting the religious or cultural or social or political sentiment, the consequences whereof shall be to your cost and detrimental.\n" +
            "Content Restrictions:\n" +
            "You are solely responsible for any content that you submit or post on Pina Systems Mobile Application. You may not post or submit any content that is defamatory, sexually explicit, derogatory to any caste. If the post(s) do not bide with these guidelines, it would be removed from the website. The company reserves the right to bar you from using the Website or take legal action against you.\n" +
            "No Obligation to Post Content:\n" +
            "We are under no obligation to post any content from you or anyone else on the website. In case we find content posted by you in any way objectionable, not abiding by our Terms of use, or inadequate for some other reason, we reserve the right to delete it from our website and bar you from the use of our website.CopyrightPina Systems Mobile Application is protected by copyright, trademark, patent, IPR and other laws of the sovereign republic of India. You are not permitted to infringe on our copyright in any format, inclusive but not limited to use of our logo, domain name or content. Your feedback and suggestions would be unconditionally used by us at our own discretion without any kind of compulsions or commitments. Calling our helpline allows us to contact you through calls and text messages. Pina Systems is authorized to update customers, who have registered or called up the helpline, about offers and new services through email and SMS.\n" +
            "Links to Third Party Sites\n" +
            "Links (such as hyperlinks) from Pina Systems Mobile Application to other sites on the Web do not constitute as the endorsement by Pina Systems of those sites or their content. Such links are provided as an information service, for reference and convenience only. Pina Systems does not control any such sites and is not responsible for their content.\n" +
            "Pina Systems may allow user/users access to content, products or Services offered by third parties through hyper links (in the form of word link, banners, channels or otherwise) to such Third Party's  Mobile Application. You are cautioned to read such sites' terms and conditions and/or privacy policies before using such sites in order to be aware of the terms and conditions of your use of such sites. Pina Systems believes that user/users acknowledge that Pina Systems has no control over such third party's site, does not monitor such sites, and Pina Systems shall not be responsible or liable to anyone for such third party site, or any content, products or Services made available on such a site. You hereby agree to hold Pina Systems harmless from any liability that may result from the use of links that may appear on the Service.\n" +
            "Termination\n" +
            "Most content and some of the features on the  Mobile Application are made available to visitors free of charge. However, Pina Systems reserves the right to terminate access to certain areas or features of the  Mobile Application at any time for any reason, with or without notice. Pina Systems also reserves the universal right to deny access to particular users to any/all of its Services without any prior notice/explanation in order to protect the interests of Pina Systems and/or other visitors to the  Mobile Application. Pina Systems reserves the right to limit, deny or create different access to the  Mobile Application and its features with respect to different user/users, or to change any of the features or introduce new features, promotions, offers without prior notice.\n" +
            "Pina Systems may terminate or suspend your right to use the Service if you breach any term of this Agreement or any policy of Pina Systems posted on the Service from time to time, or if Pina Systems otherwise finds that you have engaged in inappropriate and/or offensive behavior. If Pina Systems terminates or suspends your right to use the Service for any of these reasons, you will not be entitled to any refund of unused balance in your account. In addition to terminating or suspending your account, Pina Systems reserves the right to take appropriate legal action, including without limitation pursuing civil, criminal, and injunctive redress.\n" +
            "Pina Systems may terminate or suspend your right to use the Service at anytime for any or no reason by providing you with written or email notice of such termination, and termination will be effective immediately upon delivery of such notice. Even after your right to use the Service is terminated or suspended, this Agreement will remain enforceable against you.";
}