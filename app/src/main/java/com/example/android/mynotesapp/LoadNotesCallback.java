package com.example.android.mynotesapp;

import android.database.Cursor;

import com.example.android.mynotesapp.entity.Note;

import java.util.ArrayList;

public interface LoadNotesCallback {
    void preExecute();
    void postExecute(Cursor notes);
}