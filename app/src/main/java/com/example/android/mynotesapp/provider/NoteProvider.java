package com.example.android.mynotesapp.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.mynotesapp.MainActivity;
import com.example.android.mynotesapp.db.NoteHelper;

import android.os.Handler;

import static com.example.android.mynotesapp.db.DatabaseContract.AUTHORITY;
import static com.example.android.mynotesapp.db.DatabaseContract.NoteColumns.CONTENT_URI;
import static com.example.android.mynotesapp.db.DatabaseContract.NoteColumns.TABLE_NAME;

public class NoteProvider extends ContentProvider {

    // Deklarasikan variable untuk ContentProvider
    private static final int NOTE = 1;
    private static final int NOTE_ID = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private NoteHelper noteHelper;

    // Code tsb berguna untuk membandingkan URI dengan nilai tertentu
    static {
        // content://com.example.android.mynotesapp/note
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME, NOTE); // Cocokkan URI dengan nilai int 1 (NOTE)

        // content://com.example.android.mynotesapp/note/id
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", NOTE_ID); // Cocokkan URI dengan nilai int 2 (NOTE_ID) dan # mrupakan value placeholder
    }

    @Override
    public boolean onCreate() {
        noteHelper = NoteHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        noteHelper.open();
        Cursor cursor;
        // Cek untuk mendapatkan nilai pada variable NOTE atau NOTE_ID agar mendapatkan data bedasarkan URI dari nilai variable tsb
        switch (sUriMatcher.match(uri)){
            case NOTE:
                // Select semua data
                cursor = noteHelper.queryProvider();
                break;
            case NOTE_ID:
                // Select data bedasarkan ID
                cursor = noteHelper.queryByIdProvider(uri.getLastPathSegment()); // getLastPathSegment berguna untuk mengambil segment terakhir dari object URI
                break;
            default:
                cursor = null;
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        noteHelper.open();
        long added;
        switch (sUriMatcher.match(uri)){
            case NOTE:
                added = noteHelper.insertProvider(contentValues);
                break;
            default:
                added = 0;
                break;
        }

        getContext().getContentResolver().notifyChange(CONTENT_URI, new MainActivity.DataObserver(new Handler(), getContext()));
        return Uri.parse(CONTENT_URI + "/" + added);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        noteHelper.open();
        int deleted;
        switch (sUriMatcher.match(uri)){
            case NOTE_ID:
                deleted = noteHelper.deleteProvider(uri.getLastPathSegment());
                break;
            default:
                deleted = 0;
                break;
        }
        getContext().getContentResolver().notifyChange(CONTENT_URI, new MainActivity.DataObserver(new Handler(), getContext()));
        return deleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        noteHelper.open();
        int updated;
        switch (sUriMatcher.match(uri)){
            case NOTE_ID:
                updated = noteHelper.updateProvider(uri.getLastPathSegment(), contentValues);
                break;
            default:
                updated = 0;
                break;
        }
        getContext().getContentResolver().notifyChange(CONTENT_URI, new MainActivity.DataObserver(new Handler(), getContext()));
        return updated;
    }
}
