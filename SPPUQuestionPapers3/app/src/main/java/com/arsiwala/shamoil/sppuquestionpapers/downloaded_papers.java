package com.arsiwala.shamoil.sppuquestionpapers;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.Objects;


public class downloaded_papers extends Fragment {

    private String[] file_names;
    private String TAG = "SQP_LOGS_downloaded_papers";
    private File[] file_list;
    private File filePath;
    private ListView listView;
    private boolean noDownloads = true;
    private EditText editText;

    public downloaded_papers() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_downloaded_papers, container, false);

        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();
        ((MainActivity)getActivity()).setActionBarTitle("Downloads");

        listView = rootView.findViewById(R.id.lv);
        load_listview();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(noDownloads) return;
                Log.i(TAG, "Item Clicked. Name : " + file_names[i] + ", Path : " + file_list[i]);
                Bundle args = new Bundle();
                args.putString("file_path", file_list[i].getAbsolutePath());

                NavHostFragment.findNavController(downloaded_papers.this).navigate(R.id.pdf_viewer, args);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                if(noDownloads) return false;
                Log.i(TAG, "Item Long Clicked. Name : " + file_names[i] + ", Path : " + file_list[i]);
                new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                        .setCancelable(true)
                        .setTitle(file_names[i])
                        .setNeutralButton("Export", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseFirestore.getInstance().collection("Counter").document("Others")
                                        .update("Export", FieldValue.increment(1));
                                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/" + getString(R.string.app_name) + "/" + file_names[i] + ".pdf");

                                File directory = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));
                                if(!directory.exists()) //noinspection ResultOfMethodCallIgnored
                                    directory.mkdirs();
                                int c = 0;
                                while(file.exists() && !file.isDirectory())
                                    file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/" + getString(R.string.app_name) + "/" + file_names[i] + "-" + ++c + ".pdf");
                                if(file_list[i].renameTo(file)) {
                                    Toast.makeText(getContext(), "Exported to : " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                                    Log.i(TAG, "Exported to : " + file.getAbsolutePath());
                                }
                                else {
                                    Toast.makeText(getContext(), "Export failed", Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "Export Failed");
                                }
                                load_listview();
                            }
                        })
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i1) {
                                if (file_list[i].delete()) {
                                    Toast.makeText(getContext(), "Delete successful", Toast.LENGTH_LONG).show();
                                    Log.i(TAG, file_list[i] + " Deleted");
                                }
                                else {
                                    Log.i(TAG, file_list[i] + " Not Deleted");
                                    Toast.makeText(getContext(), "Delete unsuccessful", Toast.LENGTH_LONG).show();
                                }
                                load_listview();
                            }
                        })
                        .setNegativeButton("Rename", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i1) {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                                LayoutInflater inflater = getLayoutInflater();
                                @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.edittext_alertdialog_layout, null);
                                dialogBuilder.setView(dialogView);

                                editText = dialogView.findViewById(R.id.id_file_name);

//                                dialogBuilder.setTitle("Name");
//                                dialogBuilder.setMessage("Enter name for the paper - ");
                                dialogBuilder.setTitle("File name: ");
                                dialogBuilder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        String newFileName = editText.getText().toString();
                                        File file = new File(filePath, "." + newFileName + ".pdf");
                                        if (file_list[i].renameTo(file)) {
                                            Toast.makeText(getContext(), "Rename successful", Toast.LENGTH_LONG).show();
                                            Log.i(TAG, file_list[i] + " Renamed");
                                        }
                                        else {
                                            Toast.makeText(getContext(), "Rename unsuccessful", Toast.LENGTH_LONG).show();
                                            Log.i(TAG, file_list[i] + " Not Renamed");
                                        }
                                        load_listview();
                                    }
                                });
                                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                });
                                dialogBuilder.create().show();
                            }
                        })
                        .show();
                return true;
            }
        });
        return rootView;
    }

    private void load_listview(){
        Log.d(TAG, "load_listview: called");
        filePath = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.arsiwala.shamoil.sppuquestionpapers/files/downloaded_papers/");
        file_list = filePath.listFiles();

        if(file_list != null && file_list.length != 0){
            noDownloads = false;

            int list_length = file_list.length;
            file_names = new String[list_length];
            for (int i = 0; i < list_length; i++) {
                file_names[i] = file_list[i].getName().substring(1, file_list[i].getName().length() - 4);
                try {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.list_layout, R.id.text, file_names);
                    listView.setAdapter(adapter);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Oops..! Looks Like The App Crashed. Select The Desired Course Again", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "App crashed while setting array adapter. ", e);
                }
            }
        } else {
            Toast.makeText(getContext(), "No downloads", Toast.LENGTH_LONG).show();
            noDownloads = true;
        }
    }
}
