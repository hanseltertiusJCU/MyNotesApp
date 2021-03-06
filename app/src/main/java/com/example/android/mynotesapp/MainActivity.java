package com.example.android.mynotesapp;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.example.android.mynotesapp.adapter.NoteAdapter;
import com.example.android.mynotesapp.entity.Note;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.example.android.mynotesapp.db.DatabaseContract.NoteColumns.CONTENT_URI;
import static com.example.android.mynotesapp.helper.MappingHelper.mapCursorToArrayList;

// Method ini mempunyai 2 fungsi, yaitu:
// - Menampilkan data dari database pada tabel Note
// - Menerima nilai balik (result) dari setiap aksi dan proses yang dilakukan di FormAddUpdateActivity
public class MainActivity extends AppCompatActivity implements View.OnClickListener, LoadNotesCallback {

    private RecyclerView rvNotes;
    private ProgressBar progressBar;

    private FloatingActionButton fabAdd;
    private static final String EXTRA_STATE = "EXTRA_STATE";

    private NoteAdapter adapter;

    private static HandlerThread handlerThread;
    private DataObserver myObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Notes");
        }

        rvNotes = findViewById(R.id.rv_notes);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        rvNotes.setHasFixedSize(true);

        progressBar = findViewById(R.id.progressbar);

        handlerThread = new HandlerThread("DataObserver");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        myObserver = new DataObserver(handler, this);
        getContentResolver().registerContentObserver(CONTENT_URI, true, myObserver);

        fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(this);

        adapter = new NoteAdapter(this);

        rvNotes.setAdapter(adapter);

        if(savedInstanceState == null){
            new LoadNotesAsync(this, this).execute();
        } else {
            ArrayList<Note> list = savedInstanceState.getParcelableArrayList(EXTRA_STATE);
            if(list != null){
               adapter.setListNotes(list);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.fab_add){
            Intent intent = new Intent(MainActivity.this, FormAddUpdateActivity.class);
            startActivityForResult(intent, FormAddUpdateActivity.REQUEST_ADD);
        }
    }

    @Override
    public void preExecute() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void postExecute(Cursor notes) {
        progressBar.setVisibility(View.INVISIBLE);

        ArrayList<Note> listNotes = mapCursorToArrayList(notes);
        if(listNotes.size() > 0){
            adapter.setListNotes(listNotes);
        } else {
            adapter.setListNotes(new ArrayList<Note>());
            showSnackbarMessage("Tidak ada data saat ini");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_STATE, adapter.getListNotes());
    }

    // Create class untuk AsyncTask
    private static class LoadNotesAsync extends AsyncTask<Void, Void, Cursor>{
        // WeakReference digunakan karena AsyncTask akan dibuat dan dieksekusi scr bersamaan di method onCreate().
        // Selain itu, ketika Activity destroyed, Activity tsb dapat dikumpulkan oleh GarbageCollector, sehingga
        // dapat mencegah memory leak
        private final WeakReference<Context> weakContext;
        private final WeakReference<LoadNotesCallback> weakCallback;

        private LoadNotesAsync(Context context, LoadNotesCallback callback){
            weakContext = new WeakReference<>(context);
            weakCallback = new WeakReference<>(callback);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weakCallback.get().preExecute();
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            Context context = weakContext.get();
            // Untuk meretrieve semua data
            return context.getContentResolver().query(CONTENT_URI, null, null, null, null); // Mengakses content resolver lalu memanggil method query dengan meneruskan URI ke content provider ->
            // URI tsb akan dipakai ke method query di content provider
        }

        @Override
        protected void onPostExecute(Cursor notes) {
            super.onPostExecute(notes);
            weakCallback.get().postExecute(notes);
        }
    }

    // Method tsb berguna untuk membuat Snackbar
    private void showSnackbarMessage(String message){
        Snackbar.make(rvNotes, message, Snackbar.LENGTH_SHORT).show();
    }

    public static class DataObserver extends ContentObserver{
        final Context context;
        public DataObserver(Handler handler, Context context){
            super(handler);
            this.context = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            new LoadNotesAsync(context, (LoadNotesCallback) context).execute();
        }
    }
}
