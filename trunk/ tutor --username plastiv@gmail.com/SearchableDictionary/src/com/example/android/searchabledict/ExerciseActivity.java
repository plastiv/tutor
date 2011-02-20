package com.example.android.searchabledict;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ExerciseActivity extends Activity {

	TextView mWord;
	EditText mTranslation;
	String mCorrect;
	int mId = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exercise);

		mWord = (TextView) findViewById(R.id.txt_word);
		mTranslation = (EditText) findViewById(R.id.edit_translation);

		mTranslation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				InputMethodManager imm = (InputMethodManager) v.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, 0);
			}
		});

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

		newTask(mId);
	}

	public void onBtnCheckClick(View v) {
		check();
	}

	private void check() {
		String input = mTranslation.getText().toString();
		if (mCorrect.contains(input)) {
			showToast("correct!");
			newTask(++mId);
		} else
			showToast("no!");
	}

	private void newTask(int id) {

		Uri taskUri = ContentUris.withAppendedId(
				DictionaryProvider.CONTENT_URI, id);

		Cursor cursor = managedQuery(taskUri, null, null, null, null);

		if (cursor == null) {
			showToast("no such id in dict");
		} else {
			cursor.moveToFirst();

			int wIndex = cursor
					.getColumnIndexOrThrow(DictionaryProvider.DatabaseHelper.KEY_WORD);
			int tIndex = cursor
					.getColumnIndexOrThrow(DictionaryProvider.DatabaseHelper.KEY_TRANSLATION);

			mWord.setText(cursor.getString(wIndex));
			mTranslation.setText("");
			mCorrect = cursor.getString(tIndex);
		}
	}

	private void showToast(String message) {

		Toast newToast = Toast.makeText(getApplicationContext(), message,
				Toast.LENGTH_LONG);
		newToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		newToast.show();
	}
}
