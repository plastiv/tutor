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

package ua.kharkiv.lingvotutor.ui;

import java.util.Random;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Dictionary;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Words;
import ua.kharkiv.lingvotutor.utils.UIUtils;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Displays a word and its definition.
 */
public class ExerciseActivity extends Activity {

	TextView mWord;
	EditText mTranslation;
	String mCorrect;
	Random random;
	int wordsCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_exercise);

		if (!getWordsCount()) {
			showToast(getString(R.string.toast_open_dictionary_first));
			finish();
		} else {

			((TextView) findViewById(R.id.title_text)).setText(getTitle());

			mWord = (TextView) findViewById(R.id.txt_word);
			mTranslation = (EditText) findViewById(R.id.edit_translation);

			random = new Random();

			// FIXME How to show soft input automatically when activity started
			/*
			 * InputMethodManager imm = (InputMethodManager)
			 * mTranslation.getContext()
			 * .getSystemService(Context.INPUT_METHOD_SERVICE);
			 * imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			 */

			mTranslation
					.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId,
								KeyEvent event) {
							if (actionId == EditorInfo.IME_ACTION_DONE) {
								check();
								return true;
							}

							return false;
						}
					});

			newTask();
		}
	}

	public void onBtnCheckClick(View v) {
		check();
	}

	public void onBtnTipClick(View v) {
		mTranslation.setTextKeepState(mCorrect.substring(0, 3));
		mTranslation.setSelection(3);
	}

	/** Handle "home" title-bar action. */
	public void onHomeClick(View v) {
		UIUtils.goHome(this);
	}

	/** Handle "search" title-bar action. */
	public void onSearchClick(View v) {
		UIUtils.goSearch(this);
	}

	private void check() {
		String input = mTranslation.getText().toString();
		if (mCorrect.contains(input)) {
			showToast(mCorrect);
			newTask();
		} else {
			showToast("no!");
			mTranslation.setText("");
		}
	}

	private void newTask() {
		int id = random.nextInt(wordsCount);
		final Uri wordUri = Words.buildWordUri(id);

		Cursor cursor = managedQuery(wordUri, WordsQuery.PROJECTION, null,
				null, Words.DEFAULT_SORT);

		if (cursor == null) {
			throw new NullPointerException("ExerciseActivity.newTask() no such id:" + id);
		} else {
			cursor.moveToFirst();

			mWord.setText(cursor.getString(WordsQuery.WORD_NAME));
			// Reset EditText
			mTranslation.setText("");
			mCorrect = cursor.getString(WordsQuery.WORD_TRANSLATION);
		}
	}

	private boolean getWordsCount() {
		Cursor cursor = managedQuery(Dictionary.CONTENT_URI,
				DictionaryQuery.PROJECTION, null, null,
				Dictionary.DEFAULT_SORT);

		if (cursor.getCount() == 0)
			return false;
		else {
			cursor.moveToFirst();
			String stringCount = cursor
					.getString(DictionaryQuery.DICTIONARY_WORDS_COUNT);
			// FIXME Can random reach all words?
			wordsCount = Integer.parseInt(stringCount) - 1;
			return true;
		}
	}

	private void showToast(String message) {

		Toast newToast = Toast.makeText(getApplicationContext(), message,
				Toast.LENGTH_LONG);
		newToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		newToast.show();
	}

	private interface WordsQuery {
		String[] PROJECTION = { BaseColumns._ID, Words.WORD_NAME,
				Words.WORD_TRANSLATION };

		int WORD_NAME = 1;
		int WORD_TRANSLATION = 2;
	}

	private interface DictionaryQuery {
		String[] PROJECTION = { BaseColumns._ID, Dictionary.DICTIONARY_TITLE,
				Dictionary.DICTIONARY_WORDS_COUNT };
		int DICTIONARY_WORDS_COUNT = 2;
	}
}
