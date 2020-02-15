package com.arsiwala.shamoil.sppuquestionpapers;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.util.Objects;


public class pdf_viewer extends Fragment {
    private String TAG = "SQP_LOGS_pdf_viewer";
    private File paper_path;

    public pdf_viewer() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pdf_viewer, container, false);
        PDFView pdfView = rootView.findViewById(R.id.pdfView);

        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();
        Bundle args = this.getArguments();

        assert args != null;
        paper_path = new File(args.getString("file_path"));
        ((MainActivity)getActivity()).setActionBarTitle(paper_path.getName().substring(1, paper_path.getName().length() - 4));

        pdfView.fromFile(paper_path).load();

        Log.d(TAG, "Offline Loading " + paper_path);
        return rootView;
    }

}
