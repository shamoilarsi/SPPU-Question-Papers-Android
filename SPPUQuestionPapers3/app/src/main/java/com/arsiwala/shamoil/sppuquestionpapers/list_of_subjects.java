package com.arsiwala.shamoil.sppuquestionpapers;


import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

public class list_of_subjects extends Fragment implements AdapterView.OnItemClickListener {
    private String year_name = "", branch_name = "", TAG = "SQP_LOGS_listOfSubjects", s_t1, s_t2, s_t3, link, subject_name;
    private String[] label_1, label_2, label_3;
    private ListView lv1, lv2, lv3;
    private TextView tv1, tv2, tv3;
    private ArrayAdapter<String> adap1, adap2, adap3;
    private DatabaseHelper databaseHelper;
    private int currentSem = 0;
    private boolean isFE = false;
    private ProgressDialog progressDialog;
    private int mInterstitialAd_count = 0;
    private InterstitialAd mInterstitialAd;
    private SharedPreferences sharedPreferences;


    public list_of_subjects() {
        // Required empty public constructor
    }

    // SEM 1 : June(6) - December(12)
    // SEM 2 : January(1) - May(5)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_of_subjects, container, false);
        Bundle args = this.getArguments();

        assert args != null;
        branch_name = args.getString("branch_name", "");
        year_name = args.getString("year_name", "");

        //updating the value in firecloud was here

        sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences("",0);
        mInterstitialAd_count = sharedPreferences.getInt("mInterstitialAd_count", 0);

        mInterstitialAd = new InterstitialAd(Objects.requireNonNull(getContext()));
//        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); // Test Ads
        mInterstitialAd.setAdUnitId("ca-app-pub-4429552861546129/8356998874"); // Real Ads
        requestNewInterstitial();

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });


        isFE = (year_name != null && year_name.equals("FE"));

        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();
        ((MainActivity) getActivity()).setActionBarTitle((!isFE) ? (branch_name + " - " + year_name) : ("First Year"));

        lv1 = rootView.findViewById(R.id.list_view1);
        lv2 = rootView.findViewById(R.id.list_view2);
        lv3 = rootView.findViewById(R.id.list_view3);

        tv1 = rootView.findViewById(R.id.textView1);
        tv2 = rootView.findViewById(R.id.textView2);
        tv3 = rootView.findViewById(R.id.textView3);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Getting Subjects");
        progressDialog.setIcon(R.mipmap.app_logo_transparent);
        progressDialog.setCancelable(false);

        progressDialog.show();
        new Thread(new Get_Subject()).start();

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mInterstitialAd.isLoaded() && ++mInterstitialAd_count % 3 == 0){
            mInterstitialAd.show();
            Log.i(TAG, "Showing Interstitial Ad");
        }

        sharedPreferences.edit().putInt("mInterstitialAd_count", mInterstitialAd_count).apply();

        progressDialog.setMessage("Getting Link");
        progressDialog.show();
        new Thread(new Get_Link(parent, position)).start();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest1 = new AdRequest.Builder()
                .addTestDevice("87AE4EBEC30DE492057F7A76C6AB89DE")
                .addTestDevice("FB868D2E89162749B361D2FC8C0E7B73")
                .build();

        mInterstitialAd.loadAd(adRequest1);
    }

    class Get_Subject implements Runnable {
        @Override
        public void run() {
            databaseHelper = new DatabaseHelper(getContext(), "SPPU_QP.db", null, 1);
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            int month = calendar.get(Calendar.MONTH) + 1;
//            month = 3;

            if (!year_name.equals("FE")) {
                label_1 = databaseHelper.get_label(branch_name, year_name, 1);
                label_2 = databaseHelper.get_label(branch_name, year_name, 2);

                if(label_1 == null){
                    Handler threadHandler = new Handler(Looper.getMainLooper());
                    threadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            sharedPreferences.edit().putBoolean("database_downloaded", false).apply();
                            ((MainActivity) Objects.requireNonNull(getActivity())).openNavigationDrawer();
                        }
                    });
                }

                if (month >= 6 && month <= 12) {
                    s_t1 = "Semester 1";
                    s_t2 = "Semester 2";

                    currentSem = 1;
                } else {
                    s_t1 = "Semester 2";
                    s_t2 = "Semester 1";

                    currentSem = 2;
                }

            } else {
                label_1 = databaseHelper.get_FE_data(1, -1);
                label_2 = new String[]{"2015 Pattern"};

                s_t1 = "2019 Pattern";
                s_t2 = "2015 Pattern";

                currentSem = 0;
            }

            s_t3 = "2012 Pattern";
            label_3 = new String[]{"2012 Pattern"};

            try {
                adap1 = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.list_layout, R.id.text, label_1);
                adap2 = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.list_layout, R.id.text, label_2);
                adap3 = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.list_layout, R.id.text, label_3);
            } catch (Exception e) {
                Log.e(TAG, "App crashed while setting array adapter, " + e.getMessage());
                e.printStackTrace();
                progressDialog.dismiss();
            }

            Handler threadHandler = new Handler(Looper.getMainLooper());
            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    tv1.setText(s_t1);
                    tv2.setText(s_t2);
                    tv3.setText(s_t3);

                    if (currentSem == 0 || currentSem == 1) {
                        lv1.setAdapter(adap1);
                        lv2.setAdapter(adap2);
                    } else if (currentSem == 2) {
                        lv1.setAdapter(adap2);
                        lv2.setAdapter(adap1);
                    }

                    lv3.setAdapter(adap3);

                    lv1.setOnItemClickListener(list_of_subjects.this);
                    lv2.setOnItemClickListener(list_of_subjects.this);
                    lv3.setOnItemClickListener(list_of_subjects.this);

                    progressDialog.dismiss();

                    setListViewHeightBasedOnChildren(lv1);
                    setListViewHeightBasedOnChildren(lv2);
                    setListViewHeightBasedOnChildren(lv3);
                }
            });
        }
    }

    class Get_Link implements Runnable {
        int position;
        AdapterView<?> parent;

        Get_Link(AdapterView<?> parent, int i) {
            this.position = i;
            this.parent = parent;
        }

        @Override
        public void run() {
            if (isFE) {
                if (parent == lv1) {
                    link = databaseHelper.get_FE_data(2, position)[0];
                    subject_name = label_1[position];
                } else if (parent == lv2) {
                    link = databaseHelper.get_FE_data(3, position)[0];
                    subject_name = label_2[position];
                } else if (parent == lv3) {
                    link = databaseHelper.get_FE_data(4, position)[0];
                    subject_name = label_3[position];
                }
            } else {
                if (parent == lv1) {
                    link = databaseHelper.get_link(branch_name, year_name, currentSem, position);
                    subject_name = currentSem == 1 ? label_1[position] : label_2[position];
                } else if (parent == lv2) {
                    link = databaseHelper.get_link(branch_name, year_name, currentSem == 1 ? 2 : 1, position);
                    subject_name = currentSem == 2 ? label_1[position] : label_2[position];
                } else if (parent == lv3) {
                    link = databaseHelper.get_2012_link(branch_name, year_name);
                    subject_name = label_3[position];
                }
            }
            Log.i(TAG, subject_name + " - " + link);

            String[] array = link.split("/");
            link = array[0] + "//" + array[2] + "/drive/mobile/folders/" + array[3].substring(8) + "?usp=drive_open";

            Handler threadHandler = new Handler(Looper.getMainLooper());
            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Bundle args = new Bundle();
                    args.putString("subject_name", subject_name);
                    args.putString("subject_link", link);
                    args.putBoolean("isSyllabus", false);
                    NavHostFragment.findNavController(list_of_subjects.this).navigate(R.id.onlineWebView, args);

                    progressDialog.dismiss();
                }
            });
        }
    }

    private static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
