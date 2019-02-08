package com.example.android.mynotesapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.mynotesapp.entity.Note;

import java.util.ArrayList;

import static android.provider.BaseColumns._ID;
import static com.example.android.mynotesapp.db.DatabaseContract.NoteColumns.DATE;
import static com.example.android.mynotesapp.db.DatabaseContract.NoteColumns.DESCRIPTION;
import static com.example.android.mynotesapp.db.DatabaseContract.NoteColumns.TITLE;
import static com.example.android.mynotesapp.db.DatabaseContract.TABLE_NOTE;

public class NoteHelper {

    private static final String DATABASE_TABLE = TABLE_NOTE;
    private static DatabaseHelper dataBaseHelper;
    private static NoteHelper INSTANCE;

    private static SQLiteDatabase database;

    private NoteHelper(Context context){
        dataBaseHelper = new DatabaseHelper(context);
    }

    public static NoteHelper getInstance(Context context){
        if(INSTANCE == null){
            synchronized (SQLiteOpenHelper.class){
                if(INSTANCE == null){
                    INSTANCE = new NoteHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    public void open() throws SQLException {
        database = dataBaseHelper.getWritableDatabase();
    }

    public void close(){
        dataBaseHelper.close();

        if(database.isOpen()){
            database.close();
        }
    }

    // Method ini berguna untuk melakukan proses load data pada NoteHelper, menjalankan Read pada proses CRUD
    public ArrayList<Note> getAllNotes(){
        ArrayList<Note> arrayList = new ArrayList<>();
        // Query method untuk mendapatkan data serta sort by ID in ascending order
        Cursor cursor = database.query(DATABASE_TABLE, null,
                null,
                null,
                null,
                null,
                _ID + " ASC",
                null);
        // Memindahkan cursor (table) ke baris pertama
        cursor.moveToFirst();
        Note note;
        if(cursor.getCount() > 0){
            // Do while statement itu terjadi ketika while statement nya itu masih dalam kondisi true
            do {
                note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TITLE)));
                note.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION)));
                note.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DATE)));

                arrayList.add(note);
                // Memindahkan cursor ke baris selanjutnya
                cursor.moveToNext();

            } while (!cursor.isAfterLast());
        }

        // Menutup cursor ketika semua data telah didapat
        cursor.close();
        return arrayList;
    }

    // Method tsb berguna untuk menjalankan proses penambahan data pada NoteHelper, menjalankan Create pada proses CRUD
    public long insertNote(Note note){
        ContentValues args = new ContentValues();
        args.put(TITLE, note.getTitle());
        args.put(DESCRIPTION, note.getDescription());
        args.put(DATE, note.getDate());
        return database.insert(DATABASE_TABLE, null, args);
    }

    // Method tsb berguna untuk menjalankan proses perubahan data pada NoteHelper, menjalankan Update pada proses CRUD
    public int updateNote(Note note){
        ContentValues args = new ContentValues();
        args.put(TITLE, note.getTitle());
        args.put(DESCRIPTION, note.getDescription());
        args.put(DATE, note.getDate());
        return database.update(DATABASE_TABLE, args, _ID + " = '" + note.getId() + "'", null);
    }

    // Method tsb berguna untuk menjalankan proses penghapusan data pada NoteHelper, menjalankan Delete pada proses CRUD
    public int deleteNote(int id){
        return database.delete(TABLE_NOTE, _ID + " = '" + id + "'", null);
    }
}
