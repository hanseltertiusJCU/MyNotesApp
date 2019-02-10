package com.example.android.mynotesapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.mynotesapp.db.NoteHelper;
import com.example.android.mynotesapp.entity.Note;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.example.android.mynotesapp.db.DatabaseContract.NoteColumns.CONTENT_URI;
import static com.example.android.mynotesapp.db.DatabaseContract.NoteColumns.DATE;
import static com.example.android.mynotesapp.db.DatabaseContract.NoteColumns.DESCRIPTION;
import static com.example.android.mynotesapp.db.DatabaseContract.NoteColumns.TITLE;

// Method ini mempunyai beberapa fungsi, yaitu:
// - Menyediakan form untuk proses input data dan pembaruan data (tergantung pada modenya)
// - Jika di mode pembaruan data, maka ada icon untuk delete data
// - Sebelum proses penghapusan data ataupun menekan tombol back atau up, maka ada dialog konfirmasi
// untuk melakukan tugasnya masing2
// - Ia membantu untuk menampilkan result ke Activity lain dengan membawa data ke onActivityResult()
// di Activity target
public class FormAddUpdateActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtTitle, edtDescription;
    private Button btnSubmit;

    public static final String EXTRA_NOTE = "extra_note";
    public static final String EXTRA_POSITION = "extra_position";

    private boolean isEdit = false;
    public static final int REQUEST_ADD = 100;
    public static final int REQUEST_UPDATE = 200;

    private Note note;
    private int position;

    private NoteHelper noteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_add_update);

        edtTitle = findViewById(R.id.edt_title);
        edtDescription = findViewById(R.id.edt_description);
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(this);

        noteHelper = NoteHelper.getInstance(getApplicationContext());

        note = getIntent().getParcelableExtra(EXTRA_NOTE);
        if(note != null){
            position = getIntent().getIntExtra(EXTRA_POSITION, 0);
            isEdit = true;
        } else {
            note = new Note();
        }

        Uri uri = getIntent().getData(); // Mendapatkan URI dari Adapter (linked from setData method)

        if(uri != null){
            Cursor cursor = getContentResolver().query(uri, null, null, null, null); // Membaca data bedasarkan ID dari URI, dan memanggil kembali query method agar URI tsb dapat dioper ke {@link NoteProvider}

            if(cursor != null){
                if(cursor.moveToFirst())
                    note = new Note(cursor);
                cursor.close();
            }
        }

        String actionBarTitle;
        String btnTitle;

        if(isEdit){
            actionBarTitle = "Ubah";
            btnTitle = "Update";
            if(note != null){
                edtTitle.setText(note.getTitle());
                edtDescription.setText(note.getDescription());
            }
        } else {
            actionBarTitle = "Tambah";
            btnTitle = "Simpan";
        }

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(actionBarTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnSubmit.setText(btnTitle);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_submit){
            String title = edtTitle.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();

            boolean isEmpty = false;

            if(TextUtils.isEmpty(title)){
                edtTitle.setError("Field cannot be blank");
                return;
            }

            Intent intent = new Intent();
            intent.putExtra(EXTRA_NOTE, note);
            intent.putExtra(EXTRA_POSITION, position);

            note.setTitle(title);
            note.setDescription(description);

            if(!isEmpty){
                ContentValues values = new ContentValues();
                values.put(TITLE, title);
                values.put(DESCRIPTION, description);

                if(isEdit){ // isEdit menjadi true ketika mengirimkan object listNotes {@link NoteAdapter -> MainActivity}
                    getContentResolver().update(getIntent().getData(), values, null, null);
                    Toast.makeText(FormAddUpdateActivity.this, "Satu item berhasil diedit", Toast.LENGTH_SHORT).show();

                    finish();
                } else {

                    values.put(DATE, getCurrentDate());
                    note.setDate(getCurrentDate());
                    Toast.makeText(FormAddUpdateActivity.this, "Satu item berhasil disimpan", Toast.LENGTH_SHORT).show();

                    getContentResolver().insert(CONTENT_URI, values);

                    finish();

                }
            }


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Ketika menjalankan proses pembaruan data, inflate menu delete
        if(isEdit){
            getMenuInflater().inflate(R.menu.menu_form, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                showAlertDialog(ALERT_DIALOG_DELETE);
                break;
            case android.R.id.home:
                showAlertDialog(ALERT_DIALOG_CLOSE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE);
    }

    private final int ALERT_DIALOG_CLOSE = 10;
    private final int ALERT_DIALOG_DELETE = 20;

    private void showAlertDialog(int type){
        final boolean isDialogClose = type == ALERT_DIALOG_CLOSE;
        String dialogTitle, dialogMessage;

        if(isDialogClose){
            dialogTitle = "Batal";
            dialogMessage = "Apakah anda ingin membatalkan perubahan pada form?";
        } else {
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?";
            dialogTitle = "Hapus note";
        }

        // Membuat AlertDialog sbg konfirmasi jika sebuah action (delete atau back to MainActivity) dilakukan
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(dialogTitle);
        alertDialogBuilder
                .setMessage(dialogMessage)
                .setCancelable(false)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(isDialogClose){
                            finish();
                        } else {
                            long result = noteHelper.delete(note.getId());
                            if(result > 0){
                                Intent intent = new Intent();
                                intent.putExtra(EXTRA_POSITION, position);
                                Toast.makeText(FormAddUpdateActivity.this, "Satu item berhasil dihapus", Toast.LENGTH_SHORT).show();

                                getContentResolver().delete(getIntent().getData(), null, null);
                                finish();
                            } else {
                                Toast.makeText(FormAddUpdateActivity.this, "Gagal menghapus data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private String getCurrentDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();

        return dateFormat.format(date);
    }
}
