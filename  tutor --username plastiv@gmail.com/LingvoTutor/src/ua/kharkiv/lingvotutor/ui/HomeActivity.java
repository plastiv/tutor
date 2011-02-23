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
		startActivity(new Intent(this, ExerciseActivity.class));
	}

	/** Handle "exercise2" button action. */
	public void onExercise_2Click(View v) {
		showToast("Not implemented yet =)");
	}

	/** Handle "settings" button action. */
	public void onSettingsClick(View v) {
		showToast("Not implemented yet =)");
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
		startActivity(new Intent(Intent.ACTION_VIEW, Words.CONTENT_URI));
	}

	private void showToast(String message) {

		Toast newToast = Toast.makeText(getApplicationContext(), message,
				Toast.LENGTH_SHORT);
		newToast.show();
	}

	private void setStatusText() {
		Cursor cursor = managedQuery(Dictionary.CONTENT_URI,
				DictionaryQuery.PROJECTION, null, null, Dictionary.DEFAULT_SORT);

		// FIXME Put strings to resource
		if (cursor.getCount() == 0) {
			((TextView) findViewById(R.id.txt_status_line_1))
					.setText("Current dictionary: none");
			((TextView) findViewById(R.id.txt_status_line_2))
					.setText("Words in dictionary: none");
		} else {
			cursor.moveToFirst();
			((TextView) findViewById(R.id.txt_status_line_1))
					.setText("Current dictionary: "
							+ cursor.getString(DictionaryQuery.DICTIONARY_TITLE));
			((TextView) findViewById(R.id.txt_status_line_2))
					.setText("Words in dictionary: "
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
