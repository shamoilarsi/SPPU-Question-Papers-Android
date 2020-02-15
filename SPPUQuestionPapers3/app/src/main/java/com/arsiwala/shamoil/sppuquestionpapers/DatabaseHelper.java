package com.arsiwala.shamoil.sppuquestionpapers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private String TAG = "SQP_LOGS_DatabaseHelper";

    DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    String[] get_label(String branch_name, String year_name, int sem) {
        SQLiteDatabase database = getWritableDatabase();
        try {
            Cursor cursor = database.rawQuery("SELECT " + year_name + "_" + sem + "_label FROM " +
                    "" + branch_name + " WHERE " + year_name + "_" + sem + "_label != \"\" ", null);

            String[] array = new String[cursor.getCount()];

            int index = 0;
            if (cursor.moveToFirst()) {
                do {
                    array[index++] = cursor.getString(0);
                } while (cursor.moveToNext());
            }
            cursor.close();
            database.close();
            return array;
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
            database.close();
            return null;
        }
    }

    String get_link(String branch_name, String year_name, int sem, int id) {
        SQLiteDatabase database = getWritableDatabase();
        try {
            Cursor cursor = database.rawQuery("SELECT " + year_name + "_" + sem + "_link FROM " +
                    "" + branch_name + " WHERE id=" + (id + 1), null);
            String[] array = new String[cursor.getCount()];

            int index = 0;
            if (cursor.moveToFirst()) {
                do {
                    array[index++] = cursor.getString(0);
                } while (cursor.moveToNext());
            }
            cursor.close();
            database.close();
            return array[0];
        }
        catch (Exception e){
        Log.e(TAG, e.getMessage());
        database.close();
        return null;
    }
    }

    String get_2012_link(String branch_name, String year_name) {
        SQLiteDatabase database = getWritableDatabase();

        try{
            Cursor cursor = database.rawQuery("SELECT \"2012_" + year_name + "\" FROM " +
                    "" + branch_name + " WHERE id = 1", null);
            String[] array = new String[cursor.getCount()];

            int index = 0;
            if (cursor.moveToFirst()) {
                do {
                    array[index++] = cursor.getString(0);
                } while (cursor.moveToNext());
            }
            cursor.close();
            database.close();
            return array[0];
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
            database.close();
            return null;
        }
    }

    String[] get_FE_data(int whichAction, int id){  //1: label, 2: link, 3: 2015_link, 4: 2012_link
        SQLiteDatabase database = getWritableDatabase();
        try {
            Cursor cursor;

            if (whichAction == 1) {
                cursor = database.rawQuery("SELECT label FROM FE WHERE label != \"\"", null);
            } else if (whichAction == 3 || whichAction == 4) {
                String col = (whichAction == 3) ? "2015_link" : "2012_link";
                cursor = database.rawQuery("SELECT \"" + col + "\" FROM FE WHERE id = 1", null);
            } else {  //IF LINK IS ASKED
                cursor = database.rawQuery("SELECT link FROM FE WHERE id=" + (id + 1), null);
            }

            String[] array = new String[cursor.getCount()];

            int index = 0;
            if (cursor.moveToFirst()) {
                do {
                    array[index++] = cursor.getString(0);
                } while (cursor.moveToNext());
            }
            cursor.close();
            database.close();
            return array;
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
            database.close();
            return null;
        }
    }

    String get_syllabus_link(String name, boolean isFE){
        SQLiteDatabase database = getWritableDatabase();
        if(!isFE) {
            String[] temp = name.split("-");
            name = temp[1] + "-" + temp[0];
        }

        try {
            Cursor cursor = database.rawQuery("SELECT Link FROM Syllabus WHERE Course=\"" + name + "\"", null);
            String[] array = new String[cursor.getCount()];

            int index = 0;
            if (cursor.moveToFirst()) {
                do {
                    array[index++] = cursor.getString(0);
                } while (cursor.moveToNext());
            }
            cursor.close();
            database.close();
            return array[0];
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
            database.close();
            return null;
        }
    }

    String get_timetable_link(int year){
        SQLiteDatabase database = getWritableDatabase();

        try {
            Cursor cursor = database.rawQuery("SELECT link FROM Timetable WHERE id=" + year, null);
            String[] array = new String[cursor.getCount()];

            int index = 0;
            if (cursor.moveToFirst()) {
                do {
                    array[index++] = cursor.getString(0);
                } while (cursor.moveToNext());
            }
            cursor.close();
            database.close();
            return array[0];
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
            database.close();
            return null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
