package ua.kharkiv.lingvotutor.ui;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Dictionary;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Words;
import ua.kharkiv.lingvotutor.utils.UIUtils;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Front-door {@link Activity} that displays high-level features the schedule
 * application offers to users.
 */
public class HomeActivity extends Activity {

	private boolean isDictionaryOpen = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		((TextView) findViewById(R.id.title_text)).setText(getTitle());
		setStatusText();
	}

	/** Handle "dictionary" title-bar action. */
	public void onDictionaryClick(View v) {
		startActivity(new Intent(Intent.ACTION_VIEW, Dictionary.CONTENT_URI));
	}

	/** Handle "exercise" button action. */
	public void onExerciseClick(View v) {
		if (isDictionaryOpen)
			startActivity(new Intent(this, ExerciseActivity.class));
		else
			showToast(getString(R.string.toast_open_dictionary_first));
	}

	/** Handle "exercise2" button action. */
	public void onExercise_2Click(View v) {
		showToast(getString(R.string.toast_not_implemented));
	}

	/** Handle "settings" button action. */
	public void onSettingsClick(View v) {
		showToast(getString(R.string.toast_not_implemented));
	}

	/** Handle "about" button action. */
	public void onAboutClick(View v) {
		showToast("Lingvo Tutor v.0.1 february 2011");
	}

	/** Handle "search" title-bar action. */
	public void onSearchClick(View v) {
		UIUtils.goSearch(this);
	}

	/** Handle "words" button action. */
	public void onWordsClick(View v) {
		if (isDictionaryOpen)
			startActivity(new Intent(Intent.ACTION_VIEW, Words.CONTENT_URI));
		else
			showToast(getString(R.string.toast_open_dictionary_first));
	}

	private void showToast(String message) {
		Toast toast = Toast.makeText(getApplicationContext(), message,
				Toast.LENGTH_SHORT);
		toast.show();
	}

	private void setStatusText() {
		Cursor cursor = managedQuery(Dictionary.CONTENT_URI,
				DictionaryQuery.PROJECTION, null, null, Dictionary.DEFAULT_SORT);

		if (cursor.getCount() == 0) {
			((TextView) findViewById(R.id.txt_status_line_1))
					.setText(getString(R.string.lbl_status_text_1_empty));
			((TextView) findViewById(R.id.txt_status_line_2))
					.setText(getString(R.string.lbl_status_text_2_empty));
		} else {
			isDictionaryOpen = true;
			cursor.moveToFirst();

			((TextView) findViewById(R.id.txt_status_line_1))
					.setText(getString(R.string.lbl_status_text_1)
							+ cursor.getString(DictionaryQuery.DICTIONARY_TITLE));
			((TextView) findViewById(R.id.txt_status_line_2))
					.setText(getString(R.string.lbl_status_text_2)
							+ cursor.getString(DictionaryQuery.DICTIONARY_WORDS_COUNT));
		}
	}

	private interface DictionaryQuery {
		String[] PROJECTION = { BaseColumns._ID, Dictionary.DICTIONARY_TITLE,
				Dictionary.DICTIONARY_WORDS_COUNT };

		int DICTIONARY_TITLE = 1;
		int DICTIONARY_WORDS_COUNT = 2;
	}
}
