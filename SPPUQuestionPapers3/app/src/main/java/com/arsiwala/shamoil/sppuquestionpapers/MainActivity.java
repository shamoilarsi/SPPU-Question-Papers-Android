package com.arsiwala.shamoil.sppuquestionpapers;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.Toast;

import java.io.File;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    DrawerLayout drawer;
    float my_app_version, cloud_app_version;
    DatabaseHelper databaseHelper;
    int app_launch_count = -1, databaseVersion, cloud_databaseVersion;
    boolean isRated = false, database_downloaded = false, showTimetable = false;
    SharedPreferences sharedPreferences;
    String TAG = "SQP_LOGS_MainActivity", syllabus_name = "", branch_name, year_name;
    ProgressDialog progessDialog_for_downloading_database;
    NavController navController;
    FirebaseFirestore firebaseFirestore;

    // FOR OP2 : 87AE4EBEC30DE492057F7A76C6AB89DE
    // FOR OP6 : FB868D2E89162749B361D2FC8C0E7B73

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseFirestore = FirebaseFirestore.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.home, R.id.list_of_subjects, R.id.onlineWebView)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(MainActivity.this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return setNavigationItem(menuItem);
            }
        });

//        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
//        getSupportActionBar().setDisplayShowCustomEnabled(true);
//        getSupportActionBar().setCustomView(R.layout.actionbar_layout);

        MobileAds.initialize(this, "ca-app-pub-4429552861546129~3370597201");
        FirebaseApp.initializeApp(MainActivity.this);
        new Thread(new Firebase_database_fetch()).start();

        AdView mAdView = findViewById(R.id.id_adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("FB868D2E89162749B361D2FC8C0E7B73")
                .addTestDevice("87AE4EBEC30DE492057F7A76C6AB89DE")
                .build();
        mAdView.loadAd(adRequest);

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        sharedPreferences = getSharedPreferences("", 0);
        databaseVersion = sharedPreferences.getInt("databaseVersion", 0);
        app_launch_count = sharedPreferences.getInt("app_launch_count", -1);
        isRated = sharedPreferences.getBoolean("is_app_rated", false);
        branch_name = sharedPreferences.getString("branch_name", "");
        year_name = sharedPreferences.getString("year_name", "");
        database_downloaded = sharedPreferences.getBoolean("database_downloaded", false);
        showTimetable = sharedPreferences.getBoolean("showTimetable", true);

        sharedPreferences.edit().putInt("app_launch_count", ++app_launch_count).apply();
        Log.i(TAG, "App Launch Count : " + app_launch_count);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            my_app_version = Float.parseFloat(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (app_launch_count != 0 && !branch_name.equals("") && !year_name.equals("")) {
            Log.i(TAG, "Loading: " + branch_name + " - " + year_name);

            Bundle args = new Bundle();
            args.putString("branch_name", branch_name);
            args.putString("year_name", year_name);
            navController.popBackStack();
            navController.navigate(R.id.list_of_subjects, args);
        }
        if (app_launch_count % 10 == 0 && !isRated && app_launch_count != 0)    dialog_for_app_rating();

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Uri deepLink;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.i(TAG, "getDynamicLink: onSuccess: " + deepLink.toString());

                            Bundle args = new Bundle();
                            args.putString("subject_name", "");
                            args.putString("subject_link", deepLink.toString());
                            args.putBoolean("isSyllabus", true);

                            navController.navigate(R.id.onlineWebView, args);
//                            closeNavigationDrawer();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink: onFailure: ", e);
                    }
                });
    }

    BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "Download successful. Visit the \"Downloads\" section in the side menu to view the file", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Download successful");
        }
    };



    public void download_database() {
        Log.i(TAG, "download_database() called");
        Toast.makeText(getApplicationContext(), "Downloading database", Toast.LENGTH_SHORT).show();

        if (isStoragePermissionGranted()) {
            File rootPath = new File(Environment.getDataDirectory(), "/data/" + getPackageName() + "/databases/");
            if (!rootPath.exists()) //noinspection ResultOfMethodCallIgnored
                rootPath.mkdirs();

            final File localFile = new File(rootPath, "SPPU_QP.db");

            progessDialog_for_downloading_database = new ProgressDialog(MainActivity.this);
            progessDialog_for_downloading_database.setMessage("Downloading SPPU subjects...");
            progessDialog_for_downloading_database.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progessDialog_for_downloading_database.setIndeterminate(false);
            progessDialog_for_downloading_database.setCancelable(false);
            progessDialog_for_downloading_database.setProgress(0);
            progessDialog_for_downloading_database.show();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference("SPPU_QP.db");
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "Database download successful: " + localFile.toString());
                    sharedPreferences.edit().putBoolean("database_downloaded", true).apply();
                    database_downloaded = true;

                    databaseVersion = cloud_databaseVersion;
                    sharedPreferences.edit().putInt("databaseVersion", cloud_databaseVersion).apply();
                    progessDialog_for_downloading_database.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "Download failed: " + exception.toString());
                    progessDialog_for_downloading_database.dismiss();
                    Toast.makeText(getApplicationContext(), "Opps.. An error occurred while downloading", Toast.LENGTH_LONG).show();
                    email_developer("SPPU Question Papers - Database Download Failed", "Error occurred while downloading database: \n\n" + encode(exception.toString()) + "" +
                            "\n\nDevice : " + Build.DEVICE + "\nModel : " + Build.MODEL + "" +
                            "\nProduct : " + Build.PRODUCT, "Choose an Email client to report error:");

                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                    progessDialog_for_downloading_database.setProgress((int) (taskSnapshot.getBytesTransferred() / (float) taskSnapshot.getTotalByteCount() * 100));
                    Log.i(TAG, "onDownloadProgress: " + (taskSnapshot.getBytesTransferred() + " / " + taskSnapshot.getTotalByteCount()));
                }
            });
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Storage permission is granted");
                return true;
            } else {
                Log.e(TAG, "Storage permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.i(TAG, "Storage permission is granted by default");
            return true;
        }
    }

    public boolean setNavigationItem(MenuItem item) {
        final String[] branch = {"First Year", "Computer", "IT", "ENTC", "Civil", "Mechanical", "Electrical", "INC"};
        final String[] year = {"Second Year (SE)", "Third Year (TE)", "Fourth Year (BE)"};
        Bundle args = new Bundle();

        Log.i(TAG, "Selected " + item.getTitle() + " tab");

        if (sharedPreferences.getBoolean("database_downloaded", false)) {
            switch (item.getItemId()) {
                case R.id.id_help:
                    alertMessage("Help", new home().help_message);
                    firebaseFirestore.collection("Counter").document("Others")
                            .update("Help", FieldValue.increment(1));
                    closeNavigationDrawer();
                    item.setCheckable(false);
                    break;

                case R.id.id_Downloads:
                    firebaseFirestore.collection("Counter").document("Others")
                            .update("Downloads", FieldValue.increment(1));
                    navController.navigate(R.id.downloaded_papers);
                    closeNavigationDrawer();
                    break;

//                case R.id.id_Programs:
//                    Toast.makeText(getApplicationContext(), "Coming Soon...", Toast.LENGTH_LONG).show();
//                    item.setCheckable(false);
//                    break;

                case R.id.id_Syllabus:
                    item.setCheckable(false);

                    android.app.AlertDialog.Builder build = new android.app.AlertDialog.Builder(MainActivity.this);
                    build.setTitle("Select Branch");
                    build.setItems(branch, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int list_item_index) {
                            if (list_item_index != 0) {
                                syllabus_name = branch[list_item_index] + "-";
                                android.app.AlertDialog.Builder build = new android.app.AlertDialog.Builder(MainActivity.this);
                                build.setTitle("Select Year");
                                build.setItems(year, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int list_item_index) {
                                        firebaseFirestore.collection("Counter").document("Others")
                                                .update("Syllabus", FieldValue.increment(1));
                                        syllabus_name += year[list_item_index].substring(year[list_item_index].lastIndexOf('(') + 1, year[list_item_index].length() - 1);
                                        new Thread(new Get_Syllabus()).start();
                                    }
                                }).create().show();
                            } else {
                                syllabus_name = "FE";
                                new Thread(new Get_Syllabus()).start();
                            }
                        }
                    }).create().show();
                    break;

                case R.id.id_Timetable:
                    item.setCheckable(false);
                    if(showTimetable) {
                        android.app.AlertDialog.Builder build2 = new android.app.AlertDialog.Builder(MainActivity.this);
                        build2.setTitle("Select Year");

                        String[] yearWithFE = {"First Year (FE)", "Second Year (SE)", "Third Year (TE)", "Fourth Year (BE)"};
                        build2.setItems(yearWithFE, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int list_item_index) {
                                firebaseFirestore.collection("Counter").document("Others")
                                        .update("Timetable", FieldValue.increment(1));
                                new Thread(new Get_Timetable(list_item_index + 1)).start();
                            }
                        }).create().show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "New Timetable isn't available. Email me if you have any updates from the navigation menu", Toast.LENGTH_LONG).show();
                    }
                    break;

                case R.id.id_about:
                    closeNavigationDrawer();
                    firebaseFirestore.collection("Counter").document("Others")
                            .update("About", FieldValue.increment(1));
                    navController.navigate(R.id.about);
                    break;

                case R.id.id_share:
                    firebaseFirestore.collection("Counter").document("Others")
                            .update("Share", FieldValue.increment(1));
                    share_message("An Android Application for SPPU FE, SE, TE and BE Question Papers (Downloadable and Shareable) : https://goo.gl/WfK2n1");
                    item.setCheckable(false);
                    break;
                case R.id.id_oab:
                    item.setCheckable(false);
                    firebaseFirestore.collection("Counter").document("Others")
                            .update("Other Apps", FieldValue.increment(1));
                    Intent intent6 = new Intent(Intent.ACTION_VIEW);
                    intent6.setData(Uri.parse("https://bit.ly/shamoilArsiDev"));
                    startActivity(intent6);
                    break;
                case R.id.id_result:
                    item.setCheckable(false);
                    firebaseFirestore.collection("Counter").document("Others")
                            .update("Results", FieldValue.increment(1));
                    Intent intent_result = new Intent(Intent.ACTION_VIEW);
                    intent_result.setData(Uri.parse("http://results.unipune.ac.in/"));
                    startActivity(intent_result);
                    break;

                case R.id.id_revalresult:
                    item.setCheckable(false);
                    firebaseFirestore.collection("Counter").document("Others")
                            .update("Revaluation Results", FieldValue.increment(1));
                    Intent intent_revalresult = new Intent(Intent.ACTION_VIEW);
                    intent_revalresult.setData(Uri.parse("http://www.unipune.ac.in/university_files/Reval_Online_Results_online.htm"));
                    startActivity(intent_revalresult);
                    break;
                case R.id.id_reval:
                    item.setCheckable(false);
                    firebaseFirestore.collection("Counter").document("Others")
                            .update("Revaluation", FieldValue.increment(1));
                    Intent intent_reval = new Intent(Intent.ACTION_VIEW);
                    intent_reval.setData(Uri.parse("http://pun.unipune.ac.in/revalengg/"));
                    startActivity(intent_reval);
                    break;

                case R.id.id_FE:
                    sharedPreferences.edit().putString("branch_name", "FE").apply();
                    sharedPreferences.edit().putString("year_name", "FE").apply();

                    firebaseFirestore.collection("Counter").document("Branch")
                            .update("1 - FE", FieldValue.increment(1));

                    args.putString("branch_name", "FE");
                    args.putString("year_name", "FE");
                    navController.popBackStack();
                    navController.navigate(R.id.list_of_subjects, args);
                    closeNavigationDrawer();
                    break;

                default:
                    String title = getResources().getResourceEntryName(item.getItemId()).substring(3);  // removing the "id_" from "id_Computer", etc
                    select_year(title, year);
                    break;
            }
            return true;
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;

        if(activeNetwork != null)   download_database();
        else Toast.makeText(getApplicationContext(), "Database not downloaded. Check internet connection", Toast.LENGTH_SHORT).show();
        return false;
    }

    public void select_year(final String branch_name, final String[] year) {
        android.app.AlertDialog.Builder build = new android.app.AlertDialog.Builder(MainActivity.this);
        build.setTitle(branch_name);
        build.setItems(year, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int list_item_index) {
                String selectedYear = year[list_item_index].substring(year[list_item_index].lastIndexOf('(') + 1, year[list_item_index].length() - 1);

                sharedPreferences.edit().putString("branch_name", branch_name).apply();
                sharedPreferences.edit().putString("year_name", selectedYear).apply();

                firebaseFirestore.collection("Counter").document("Branch")
                        .update((selectedYear.equals("SE") ? "2" : selectedYear.equals("TE") ? "3" : "4") + " - " + branch_name, FieldValue.increment(1));
                Log.d(TAG, "Updating value of " + year_name + " - " + branch_name + " by 1");

                Bundle args = new Bundle();
                args.putString("branch_name", branch_name);
                args.putString("year_name", selectedYear);
                navController.popBackStack();
                navController.navigate(R.id.list_of_subjects, args);
                closeNavigationDrawer();
            }
        }).create().show();
    }

    class Get_Syllabus implements Runnable {
        @Override
        public void run() {
            databaseHelper = new DatabaseHelper(getApplicationContext(), "SPPU_QP.db", null, 1);
            final String link = (databaseHelper.get_syllabus_link(syllabus_name, syllabus_name.equals("FE")));

            Handler threadHandler = new Handler(Looper.getMainLooper());
            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    closeNavigationDrawer();

                    Bundle args = new Bundle();
                    args.putString("subject_name", syllabus_name);
                    args.putString("subject_link", link);
                    args.putBoolean("isSyllabus", !syllabus_name.equals("FE"));

                    navController.navigate(R.id.onlineWebView, args);
                }
            });
        }
    }

    class Get_Timetable implements Runnable {
        int year;

        Get_Timetable(int y){
            year = y;
        }
        @Override
        public void run() {
            databaseHelper = new DatabaseHelper(getApplicationContext(), "SPPU_QP.db", null, 1);
            final String link = (databaseHelper.get_timetable_link(year));
            final String name = (year == 1 ? "FE" : year == 2 ? "SE" : year == 3 ? "TE" : "BE") + " Timetable";

            Handler threadHandler = new Handler(Looper.getMainLooper());
            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    closeNavigationDrawer();

                    Bundle args = new Bundle();
                    args.putString("subject_name", name);
                    args.putString("subject_link", link);
                    args.putBoolean("isSyllabus", true);

                    navController.navigate(R.id.onlineWebView, args);
                }
            });
        }
    }

    class Firebase_database_fetch implements Runnable {
        @Override
        public void run(){
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference appVersionReference = firebaseDatabase.getReference("appVersion");
            appVersionReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //noinspection ConstantConditions
                    cloud_app_version = dataSnapshot.getValue(Float.class);
                    Log.d(TAG, "Cloud Version = " + cloud_app_version + ", My Version = " + my_app_version);
                    if (cloud_app_version - my_app_version > 0.02) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Update Available")
                                .setCancelable(false)
                                .setMessage("A new version of the app is available. Update the app to get the latest features!")
                                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.arsiwala.shamoil.sppuquestionpapers"));
                                        startActivity(intent);
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    cloud_app_version = my_app_version;
                    Log.e(TAG, "Error fetching cloud version number");
                }
            });

            DatabaseReference databaseLinkReference = firebaseDatabase.getReference("databaseVersion");
            databaseLinkReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    cloud_databaseVersion = Integer.parseInt(Objects.requireNonNull(dataSnapshot.getValue()).toString());
                    Log.i(TAG, "cloud_databaseVersion: " + cloud_databaseVersion + ", my_databaseVersion: " + databaseVersion);
                    if (cloud_databaseVersion > databaseVersion) {
                        database_downloaded = false;
                        sharedPreferences.edit().putBoolean("database_downloaded", false).apply();

                        download_database();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error fetching cloud database version");
                }
            });


            DatabaseReference showTimetableReference = firebaseDatabase.getReference("showTimetable");
            showTimetableReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    showTimetable = Boolean.parseBoolean(Objects.requireNonNull(dataSnapshot.getValue()).toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error fetching showTimetable");
                }
            });
        }
    }

    public void share_message(String message) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void alertMessage(String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setCancelable(true);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public void dialog_for_app_rating() {
        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .threshold(4)
                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        email_developer("SPPU Question Papers - Feedback", feedback, "Choose an Email client :");
                    }
                })
                .onThresholdCleared(new RatingDialog.Builder.RatingThresholdClearedListener() {
                    @Override
                    public void onThresholdCleared(RatingDialog ratingDialog, float rating, boolean thresholdCleared) {
                        sharedPreferences.edit().putBoolean("is_app_rated", true).apply();
                        isRated = true;

                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        ratingDialog.dismiss();
                    }
                }).build();
        ratingDialog.show();
    }

    public void openNavigationDrawer() {
        drawer.openDrawer(GravityCompat.START);
    }

    public void closeNavigationDrawer() {
        drawer.closeDrawer(GravityCompat.START);
    }

    public void email_developer(String subject_email, String message, String displayMessage) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.EMAIL", new String[]{"shamoilarsiwala16@gmail.com"});
        intent.putExtra("android.intent.extra.SUBJECT", subject_email);
        intent.putExtra("android.intent.extra.TEXT", message);
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, displayMessage));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
        else
            super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission: " + permissions[0] + " was " + grantResults[0]);
            download_database();
        }
    }

    private String encode(String s) {
        int i;
        char ch;
        StringBuilder f = new StringBuilder();
        for (i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            ch += 2;
            f.append(ch);
        }
        return f.toString();
    }

    public void setActionBarTitle(String title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newconfig){
        super.onConfigurationChanged(newconfig);
    }
}
