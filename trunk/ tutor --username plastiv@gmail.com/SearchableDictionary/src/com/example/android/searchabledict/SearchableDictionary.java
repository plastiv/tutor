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

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

// FIXME add Javadoc
/**
 * The main activity for the dictionary. Displays search results triggered by
 * the search dialog and handles actions from search suggestions.
 */
public class SearchableDictionary extends Activity {
	private static final String TAG = "SearchableDictionary Activity";

	private TextView mTextView;
	private ListView mListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mTextView = (TextView) findViewById(R.id.text);
		mListView = (ListView) findViewById(R.id.list);

		Intent intent = getIntent();

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			// handles a click on a search suggestion; launches activity to show
			// word
			Intent wordIntent = new Intent(this, WordActivity.class);
			wordIntent.setData(intent.getData());
			startActivity(wordIntent);
			finish();
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// handles a search query
			String query = intent.getStringExtra(SearchManager.QUERY);
			showResults(query);
		} else if (Intent.ACTION_MAIN.equals(intent.getAction())) {
			showAllResults();
		}

		// Define the on-click listener for the list items
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Build the Intent used to open WordActivity with a
				// specific word Uri
				Intent wordIntent = new Intent(getApplicationContext(),
						WordActivity.class);
				Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
						String.valueOf(id));
				wordIntent.setData(data);
				startActivity(wordIntent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// Switch what operation dialog to show
		switch (id) {
		case R.id.delete:
			return ProgressDialog.show(SearchableDictionary.this, "",
					getString(R.string.dlg_delete), true);
		case R.id.file:
		case R.id.url:
		case R.id.example:
			return ProgressDialog.show(SearchableDictionary.this, "",
					getString(R.string.dlg_load), true);
		default:
			throw new IllegalArgumentException("Unknown operationId " + id);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			onSearchRequested();
			return true;
		case R.id.example:
			new DownloadTask(R.id.example).execute("unused");
			return true;
		case R.id.file:
			showOpenDialog(R.id.file);
			return true;
		case R.id.url:
			showOpenDialog(R.id.url);
			return true;
		case R.id.delete:
			new EraseDataTask().execute((Void) null);
			return true;
		default:
			return false;
		}
	}

	/**
	 * Searches the dictionary and displays results for the given query.
	 * 
	 * @param query
	 *            The search query
	 */
	private void showResults(String query) {
		Cursor cursor = managedQuery(DictionaryProvider.CONTENT_URI, null,
				null, new String[] { query }, null);

		if (cursor == null) {
			// There are no results
			mTextView.setText(getString(R.string.no_results,
					new Object[] { query }));
		} else {
			// Display the number of results

			int count = cursor.getCount();
			String countString = getResources().getQuantityString(
					R.plurals.search_results, count,
					new Object[] { count, query });
			mTextView.setText(countString);

			bindCursorView(cursor);
		}
	}

	private void showAllResults() {
		DictionaryDatabase mDictionary = new DictionaryDatabase(
				getApplicationContext());
		Cursor cursor = mDictionary.getAllWords();

		if (cursor == null) {
			// There are no results
			showToast(getString(R.string.dictionary_empty));
		} else {
			// Display the number of results
			showToast(getString(R.string.words_loaded, cursor.getCount()));
			bindCursorView(cursor);
		}
	}

	private void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
				.show();
	}

	private void showOpenDialog(final int dialogId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		// FIXME hardlink to url
		switch (dialogId) {
		case R.id.url:
			input.setText("http://dl.dropbox.com/u/13226125/To%20kill%20a%20mockbird%28En-Ru%29.xml");
			builder.setMessage(R.string.dlg_load_url_label);
			break;
		case R.id.file:
			input.setText("sdcard/test.xml");
			builder.setMessage(R.string.dlg_load_file_label);
			break;
		}

		input.selectAll();

		builder.setCancelable(false)
				.setView(input)
				.setNegativeButton(getString(R.string.btn_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				.setPositiveButton(getString(R.string.btn_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								switch (dialogId) {
								case R.id.url:
									new DownloadTask(R.id.url).execute(input
											.getText().toString());
									break;
								case R.id.file:
									new DownloadTask(R.id.file).execute(input
											.getText().toString());
									break;
								}
							}
						});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private void bindCursorView(Cursor cursor) {
		// Specify the columns we want to display in the result
		String[] from = new String[] { DictionaryDatabase.KEY_WORD,
				DictionaryDatabase.KEY_DEFINITION };

		// Specify the corresponding layout elements where we want the
		// columns to go
		int[] to = new int[] { R.id.word, R.id.definition };

		// Create a simple cursor adapter for the definitions and apply them
		// to the ListView
		SimpleCursorAdapter words = new SimpleCursorAdapter(this,
				R.layout.result, cursor, from, to);
		mListView.setAdapter(words);
	}

	private class DownloadTask extends AsyncTask<String, Integer, Long> {
		private int taskId;

		public DownloadTask(int id) {
			taskId = id;
		}

		protected void onPreExecute() {
			showDialog(taskId);
		}

		protected Long doInBackground(String... params) {
			try {
				switch (taskId) {
				case R.id.url:
					new Downloader(getApplicationContext()).fromUrl(params[0]);
					break;
				case R.id.file:
					new Downloader(getApplicationContext()).fromFile(params[0]);
					break;
				case R.id.example:
					new Downloader(getApplicationContext())
							.fromResourse(getResources());
					break;
				}
			} catch (IOException e) {
				Log.e(TAG, "DownloadTask.doInBackground(): IOExeption", e);
			}
			return null;
		}

		protected void onPostExecute(Long result) {
			dismissDialog(taskId);
			showAllResults();
		}
	}

	private class EraseDataTask extends AsyncTask<Void, Integer, Long> {

		protected void onPreExecute() {
			showDialog(R.id.delete);
		}

		protected Long doInBackground(Void... params) {
			try {
				new DictionaryDatabase(getApplicationContext()).eraseData();
			} catch (Throwable e) {
				Log.e(TAG, "EraseDataTask.doInBackground(): SqlExeption", e);
			}
			return null;
		}

		protected void onPostExecute(Long result) {
			dismissDialog(R.id.delete);
			showToast(getString(R.string.dictionary_empty));
			mListView.setAdapter(null);
		}
	}

}
