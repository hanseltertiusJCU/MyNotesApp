package com.example.android.mynotesapp.db;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

// Kelas ini berguna untuk mendefinisikan table
public class DatabaseContract {

    // Component untuk content provider URI
    public static final String AUTHORITY = "com.example.android.mynotesapp"; // base authority untuk dapat mengakses MyNotesApp (manifestnya hrs berisi authority yg sama)
    private static final String SCHEME = "content"; // standard prefix

    private DatabaseContract(){}

    // Class ini berguna untuk mendefinisikan column
    public static final class NoteColumns implements BaseColumns {
        // Kita tidak perlu column _ID karena BaseColumns sdh mendefinisikan column tsb
        public static final String TABLE_NAME = "note";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String DATE = "date";

        // Bangun URI untuk mengakses data tabel note dari ContentProvider (NoteProvider)
        public static final Uri CONTENT_URI = new Uri.Builder().scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(TABLE_NAME)
                .build();
    }

    public static String getColumnString(Cursor cursor, String columnName){
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    public static int getColumnInt(Cursor cursor, String columnName){
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    public static long getColumnLong(Cursor cursor, String columnName){
        return cursor.getLong(cursor.getColumnIndex(columnName));
    }
}
