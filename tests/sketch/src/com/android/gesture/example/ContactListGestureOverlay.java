/*
 * Copyright (C) 2008-2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gesture.example;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.gesture.Gesture;
import com.android.gesture.GestureActionListener;
import com.android.gesture.GestureOverlay;
import com.android.gesture.LetterRecognizer;
import com.android.gesture.Prediction;
import com.android.gesture.TouchThroughGesturing;

import java.util.ArrayList;

public class ContactListGestureOverlay extends Activity {

    private static final String LOGTAG = "ContactListGestureOverlay";
    
    private static final String SORT_ORDER = People.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

    private static final String[] CONTACTS_PROJECTION = new String[] {
            People._ID, // 0
            People.DISPLAY_NAME, // 1
    };

    private GestureOverlay mOverlay;

    private ContactAdapter mContactAdapter;

    private TouchThroughGesturing mGestureProcessor;

    private LetterRecognizer mRecognizer;

    private ListView mContactList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.overlaydemo);

        setProgressBarIndeterminateVisibility(true);

        // create a letter recognizer
        mRecognizer = LetterRecognizer.getLetterRecognizer(this, LetterRecognizer.LATTIN_LOWERCASE);

        // load the contact list
        mContactList = (ListView) findViewById(R.id.list);
        registerForContextMenu(mContactList);
        mContactList.setTextFilterEnabled(true);
        mContactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (!mGestureProcessor.isGesturing()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(
                            People.CONTENT_URI, id));
                    startActivity(intent);
                }
            }
        });
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(People.CONTENT_URI, CONTACTS_PROJECTION, null, null,
                SORT_ORDER);
        ArrayList<ContactItem> list = new ArrayList<ContactItem>();
        while (cursor.moveToNext()) {
            list.add(new ContactItem(cursor.getLong(0), cursor.getString(1)));
        }
        mContactAdapter = new ContactAdapter(this, list);
        mContactList.setAdapter(mContactAdapter);

        setProgressBarIndeterminateVisibility(false);

        // add a gesture overlay on top of the ListView
        mOverlay = new GestureOverlay(this);
        mGestureProcessor = new TouchThroughGesturing(mContactList);
        mGestureProcessor.setGestureType(TouchThroughGesturing.MULTIPLE_STROKE);
        mGestureProcessor.addGestureActionListener(new GestureActionListener() {
            public void onGesturePerformed(GestureOverlay overlay, Gesture gesture) {
                ArrayList<Prediction> predictions = mRecognizer.recognize(gesture);
                if (!predictions.isEmpty()) {
                    Log.v(LOGTAG, "1st Prediction : " + predictions.get(0).name);
                    Log.v(LOGTAG, "2nd Prediction : " + predictions.get(1).name);
                    Log.v(LOGTAG, "3rd Prediction : " + predictions.get(2).name);
                    int index = mContactAdapter.search(predictions.get(0).name);
                    if (index != -1) {
                        mContactList.setSelection(index);
                    }
                }
            }
        });
        mOverlay.addGestureListener(mGestureProcessor);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        this.addContentView(mOverlay, params);
    }
}