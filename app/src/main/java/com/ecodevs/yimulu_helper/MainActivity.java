package com.ecodevs.yimulu_helper;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener {

    private static final String YIMULU_PREF = "com.ecodevs.yimulu.helper.PREFERENCE";
    private static final int CALL_REQUEST_CODE = 1;
    private static final String SEND_COUNT = "Send Count";
    private static final String PHONE_NUMBERS = "com.ecodevs.yimulu.helper.PHONES";
    private static final int SELECT_PHONE_NUMBER = 2;
    private static boolean IS_FROM_ON_CREATE = false;
    private static String sendString = "*922*2*%s*%s*%s*1#";

    Button buttonSend;

    EditText amountField;
    EditText phoneNoField;
    EditText pinCodeField;
    ImageView contact;
    LinearLayout options;
    TextView titleText,subText;
    LinearLayout developer;

    String amount;
    String phoneNumber;
    String pinCode;

    PopupMenu menu;

    SharedPreferences yimuluPreference;
    SharedPreferences phoneNumbersPref;

    Intent intent;
    Animation intro,fields,two,three,button,dev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Animation instances
        /*
        intro = AnimationUtils.loadAnimation(this,R.anim.intro);
        fields = AnimationUtils.loadAnimation(this,R.anim.field_one);
        button = AnimationUtils.loadAnimation(this,R.anim.field_three);
        dev = AnimationUtils.loadAnimation(this,R.anim.dev);
        */

        //initialize shared pref for activity tracking

        yimuluPreference = this.getSharedPreferences(YIMULU_PREF, Context.MODE_PRIVATE);
        phoneNumbersPref = this.getSharedPreferences(PHONE_NUMBERS, Context.MODE_PRIVATE);

        //Initialize views

        amountField  = findViewById(R.id.amountField);
        contact      = findViewById(R.id.contact);
        phoneNoField = findViewById(R.id.phoneNoField);
        pinCodeField = findViewById(R.id.pinField);
        options      = findViewById(R.id.options);
        buttonSend   = findViewById(R.id.ButtonSend);
        //relativeLayout = findViewById(R.id.rela);
        titleText = findViewById(R.id.titleText);
        subText = findViewById(R.id.titleSubtext);
        developer = findViewById(R.id.developer);

        /*
        //Animation
        amountField.startAnimation(fields);
        developer.startAnimation(dev);
        pinCodeField.startAnimation(fields);
        buttonSend.startAnimation(button);
        rela.startAnimation(fields);
        titleText.startAnimation(intro);
        subText.startAnimation(intro);
        */

        //Listener for events

        buttonSend.setOnClickListener(this);
        contact.setOnClickListener(this);
        options.setOnClickListener(this);

        //Check for Permission

        if (!isPhonePermissionsGranted()) {
            IS_FROM_ON_CREATE = true;
            askPhonePermission();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        //checks for build Version

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_color));
        }

        //Since editText is wrapped in Relative layout we need to change focus for both
        //editText and Relative Layout at the same time for nice effect
        // in addition that we need to change background as the focus is changed

        EditText phone = findViewById(R.id.phoneNoField);

        phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            RelativeLayout container = findViewById(R.id.rela);

            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    container.setBackgroundResource(R.drawable.edit_text_sel);
                }else {
                    container.setBackgroundResource(R.drawable.edit_text_background_unsel);
                }
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();

        //TODO menu item implementation, since there is no action bar.

        if (id == R.id.change_pin){
            Toast.makeText(this,"Coming Soon",Toast.LENGTH_SHORT).show();
        }else if (id == R.id.about){
            Toast.makeText(this,"Coming Soon",Toast.LENGTH_SHORT).show();
        }else if (id == R.id.donate){
            if (phoneNoField != null){
            phoneNoField.setText("0912121388");
            Toast.makeText(this,"Thank You!",Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        SharedPreferences.Editor editorP;

        if (id == R.id.ButtonSend) {

            //check for permission

            if (isPhonePermissionsGranted() || !isBuildMorGreater()) {

                amount = amountField.getText().toString().trim();
                phoneNumber = phoneNoField.getText().toString().trim();
                pinCode = pinCodeField.getText().toString().trim();

                //checks if the input values are empty or invalid
                //@checkInputs methods sends boolean if all data entered is okay
                boolean isValid = checkInputs(phoneNumber, amount, pinCode);


                if (isValid) {

                    //Concatenates the sendString with the data give by the user phoneNumber, amount & pinCode
                    String s = String.format(sendString, phoneNumber, amount, pinCode);

                    intent = new Intent(Intent.ACTION_CALL);

                    int count = phoneNumbersPref.getInt(SEND_COUNT, 0);
                    count = count + 1;

                    editorP = phoneNumbersPref.edit();
                    editorP.putInt(SEND_COUNT, count);
                    editorP.putString( phoneNumber, amount);
                    editorP.apply();


                    intent.setData(Uri.parse("tel:" + Uri.encode(s)));

                    //if the amount of money provided by the user exceeds 200 ask for confirmation
                    if (!(Integer.parseInt(amount) >= 200)){

                        startActivity(intent);

                    }else {
                        showConfirmDialogue(amount,phoneNumber,count);
                    }
                }
            } else
                askPhonePermission();

        }else if(id == R.id.options){
            menu = new PopupMenu(this,options);
            menu.getMenuInflater().inflate(R.menu.main_menu,menu.getMenu());
            menu.setOnMenuItemClickListener(this);
            menu.show();
        }else if(id == R.id.contact){
            intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0912121388"));
            startActivity(intent);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && !IS_FROM_ON_CREATE) {
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            if (data != null) {

                Uri contactUri = data.getData();

                String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                assert contactUri != null;
                Cursor cursor = this.getContentResolver().query(contactUri, projection,
                        null, null, null);

                // If the cursor returned is valid, get the phone number
                if (cursor != null && cursor.moveToFirst()) {
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String number = cursor.getString(numberIndex);
                    // Do something with the phone number

                    String trimedNum = trimNum(number);
                    phoneNoField.setText(trimedNum);
                }

                cursor.close();
            }
        }
    }

    boolean isPhonePermissionsGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) ==
                PackageManager.PERMISSION_GRANTED;
    }

    void askPhonePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_REQUEST_CODE);
    }

    boolean isBuildMorGreater() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    boolean checkInputs(String phone, String amount, String pin) {

        if (phone.isEmpty()) {
            phoneNoField.setError("Phone Field Empty");
            return false;
        } else if (amount.isEmpty()) {
            amountField.setError("Amount Field Empty");
            return false;
        } else if (pin.isEmpty()) {
            pinCodeField.setError("Pin Field Empty");
            return false;
        }

        return true;
    }

    boolean transfer = true;

    private void showConfirmDialogue(String amount, String phoneNumber, int count) {


        final String s = String.valueOf(count);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setCancelable(false);
        builder.setTitle("Transfer?");
        builder.setMessage("Are you sure you want to transfer " + amount + " Birr to \n" + phoneNumber);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                startActivity(intent);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                transfer = false;
            }
        });

        builder.create();
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.change_pin) {
        }
        return true;
    }

    void startContactPicker() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(i, SELECT_PHONE_NUMBER);
    }

    public void onContactPickerClicked(View view) {
        startContactPicker();

    }

    //trims unnecessary infos to the phone number from Contact Number
    String trimNum(String phoneNumber) {

        String newString = phoneNumber.replaceAll("\\s", "");

        boolean b = phoneNumber.contains("+251");

        if (b) {

            newString = phoneNumber.replace("+251", "0").trim().replaceAll("\\s", "");
        }

        return newString;
    }


    public void closeImageClick(View view) {
        this.finish();
    }
}
