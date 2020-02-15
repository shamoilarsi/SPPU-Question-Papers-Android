package com.arsiwala.shamoil.sppuquestionpapers;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class home extends Fragment {
  String TAG = "SQP_LOGS_home";
  String help_message = "To Download or Share a paper:-\n\n" +
          "1.Open the paper you wish to download/share.\n\n" +
          "2. Click on the 3 dots on top right corner, Click on \"Open in " +
          "new window\". Allow the page to load.\n\n" +
          "3. Enter desired name for the paper and click on the " +
          "desired option.\n\n" +
          "\n" +
          "----------\n" +
          "\n" +
          "--> To find the downloaded papers, slide the menu from " +
          "the left side. Click on \"Downloads\".\n\n" +
          "--> If you have an ad blocker installed, you cannot " +
          "download the papers.\n\n" +
          "--> Slow internet connection can delay the download " +
          "process. Keep the app open till paper appears in the " +
          "\"Downloads\" section.";

  public home() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    final View rootView = inflater.inflate(R.layout.fragment_home, container, false);
    Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();
    Objects.requireNonNull(getActivity()).setTitle("SPPU Question Papers");
    Log.d(TAG, "Home Page launched");

    TextView textView = rootView.findViewById(R.id.firstpage_text2);
    textView.setText("\"It always seems impossible until it’s done\"");

    ((MainActivity) Objects.requireNonNull(getActivity())).alertMessage("Note", help_message);

    Button get_started = rootView.findViewById(R.id.firstpage_getstarted_button);
    get_started.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((MainActivity) Objects.requireNonNull(getActivity())).openNavigationDrawer();
      }
    });

    return rootView;
  }
}
