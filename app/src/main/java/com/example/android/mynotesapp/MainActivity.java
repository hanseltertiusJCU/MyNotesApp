package com.example.android.mynotesapp;

import android.content.Intent;
import android.os.AsyncTask;
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
import com.example.android.mynotesapp.db.NoteHelper;
import com.example.android.mynotesapp.entity.Note;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

// Method ini mempunyai 2 fungsi, yaitu:
// - Menampilkan data dari database pada tabel Note
// - Menerima nilai balik (result) dari setiap aksi dan proses yang dilakukan di NoteAddUpdateActivity
public class MainActivity extends AppCompatActivity implements View.OnClickListener, LoadNotesCallback {

    private RecyclerView rvNotes;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;
    private static final String EXTRA_STATE = "EXTRA_STATE";
    private NoteAdapter adapter;
    private NoteHelper noteHelper;

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

        // Membuat Instance untuk NoteHelper
        noteHelper = NoteHelper.getInstance(getApplicationContext());
        // Membuka koneksi terhadap SQL
        noteHelper.open();

        progressBar = findViewById(R.id.progressbar);
        fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(this);

        adapter = new NoteAdapter(this);
        rvNotes.setAdapter(adapter);

        if(savedInstanceState == null){
            new LoadNotesAsync(noteHelper, this).execute();
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
            Intent intent = new Intent(MainActivity.this, NoteAddUpdateActivity.class);
            startActivityForResult(intent, NoteAddUpdateActivity.REQUEST_ADD);
        }
    }

    @Override
    public void preExecute() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void postExecute(ArrayList<Note> notes) {
        progressBar.setVisibility(View.INVISIBLE);
        adapter.setListNotes(notes);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_STATE, adapter.getListNotes());
    }

    private static class LoadNotesAsync extends AsyncTask<Void, Void, ArrayList<Note>>{
        // WeakReference digunakan karena AsyncTask akan dibuat dan dieksekusi scr bersamaan di method onCreate().
        // Selain itu, ketika Activity destroyed, Activity tsb dapat dikumpulkan oleh GarbageCollector, sehingga
        // dapat mencegah memory leak
        private final WeakReference<NoteHelper> weakNoteHelper;
        private final WeakReference<LoadNotesCallback> weakCallback;

        private LoadNotesAsync(NoteHelper noteHelper, LoadNotesCallback callback){
            weakNoteHelper = new WeakReference<>(noteHelper);
            weakCallback = new WeakReference<>(callback);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weakCallback.get().preExecute();
        }

        @Override
        protected ArrayList<Note> doInBackground(Void... voids) {
            return weakNoteHelper.get().getAllNotes();
        }

        @Override
        protected void onPostExecute(ArrayList<Note> notes) {
            super.onPostExecute(notes);
            weakCallback.get().postExecute(notes);
        }
    }

    // Method tsb berguna untuk mendapatkan hasil dari semua aksi yang dilakukan oleh NoteAddUpdateActivity (editor)
    // dengan menerima data dari Intent bedasarkan request dan result code
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Cek jika intent itu ada
        if(data != null){
            // Terjadi ketika terdapat penambahan data pada NoteAddUpdateActivity
            if(requestCode == NoteAddUpdateActivity.REQUEST_ADD){
                if(resultCode == NoteAddUpdateActivity.RESULT_ADD){
                    Note note = data.getParcelableExtra(NoteAddUpdateActivity.EXTRA_NOTE); // Membuat object Note baru dengan getParcelableExtra karena Note implement Parcelable
                    adapter.addItem(note); // Manggil method addItem di NoteAdapter
                    rvNotes.smoothScrollToPosition(adapter.getItemCount() - 1); // method ini digunakan agar recycler view akan melakukan smooth scrolling
                    showSnackbarMessage("Satu item berhasil ditambahkan");
                }
            }
            // Terjadi ketika terdapat perubahan atau penghapusan data pada NoteAddUpdateActivity
            else if(requestCode == NoteAddUpdateActivity.REQUEST_UPDATE){
                if(resultCode == NoteAddUpdateActivity.RESULT_UPDATE){
                    Note note = data.getParcelableExtra(NoteAddUpdateActivity.EXTRA_NOTE);
                    int position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0);
                    adapter.updateItem(position, note); // Memanggil method updateItem di NoteAdapter, arg nya ada 2 yaitu position (item yg d update) dan note
                    rvNotes.smoothScrollToPosition(position);
                    showSnackbarMessage("Satu item berhasil diubah");
                } else if(resultCode == NoteAddUpdateActivity.RESULT_DELETE){
                    int position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0);
                    adapter.removeItem(position); // Memanggil method deleteItem di NoteAdapter, argnya yaitu position (item yg d delete)
                    showSnackbarMessage("Satu item berhasil dihapus");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Menutup koneksi terhadap SQL
        noteHelper.close();
    }

    // Method tsb berguna untuk membuat Snackbar
    private void showSnackbarMessage(String message){
        Snackbar.make(rvNotes, message, Snackbar.LENGTH_SHORT).show();
    }
}
