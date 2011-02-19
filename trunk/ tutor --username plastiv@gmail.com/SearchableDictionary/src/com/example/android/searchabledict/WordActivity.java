/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.example.android.searchabledict;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Displays a word and its definition.
 */
public class WordActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.word);

		Uri uri = getIntent().getData();
		Cursor cursor = managedQuery(uri, null, null, null, null);

		if (cursor == null) {
			finish();
		} else {
			cursor.moveToFirst();

			TextView definition = (TextView) findViewById(R.id.definition);

			int wIndex = cursor
					.getColumnIndexOrThrow(DictionaryProvider.DatabaseHelper.KEY_WORD);
			int dIndex = cursor
					.getColumnIndexOrThrow(DictionaryProvider.DatabaseHelper.KEY_TRANSLATION);
			int eIndex = cursor
					.getColumnIndexOrThrow(DictionaryProvider.DatabaseHelper.KEY_EXAMPLE);
			int tIndex = cursor
			.getColumnIndexOrThrow(DictionaryProvider.DatabaseHelper.KEY_TRANSCRIPTION);

			Resources res = getResources();
			String text = String.format(res.getString(R.string.html_word_desc),
					cursor.getString(wIndex), cursor.getString(tIndex),
					cursor.getString(dIndex),cursor.getString(eIndex) );
			CharSequence styledText = Html.fromHtml(text);

			definition.setText(styledText);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			onSearchRequested();
			return true;
		default:
			return false;
		}
	}
}
