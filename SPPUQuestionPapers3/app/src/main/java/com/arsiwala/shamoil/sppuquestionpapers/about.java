package com.arsiwala.shamoil.sppuquestionpapers;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

public class about extends Fragment {
    private String TAG = "SQP_LOGS_about";

    public about() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();

        ((MainActivity)getActivity()).setActionBarTitle("About");

        CardView cd1 = rootView.findViewById(R.id.card1);
        CardView cd2 = rootView.findViewById(R.id.card2);
        CardView cd3 = rootView.findViewById(R.id.card3);


        cd1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Name Selected");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://bit.ly/shamoilArsiDev"));
                startActivity(intent);
            }
        });

        cd2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Email Selected");
                Intent intent = new Intent("android.intent.action.SEND");
                intent.putExtra("android.intent.extra.EMAIL", new String[]{"shamoilarsiwala16@gmail.com"});
                intent.putExtra("android.intent.extra.SUBJECT", "SPPU Question Papers - Contact");
                intent.putExtra("android.intent.extra.TEXT", "Explain Here as much as Possible");
                intent.setType("message/rfc822");
                startActivity(Intent.createChooser(intent, "Choose an Email client :"));
            }
        });

        cd3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Instagram Selected");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri url = Uri.parse("https://bit.ly/shamoilArsiInsta");
                intent.setData(url);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
