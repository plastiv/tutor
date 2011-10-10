package ua.kharkiv.lingvotutor.ui;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Dictionary;
import ua.kharkiv.lingvotutor.utils.AboutDialogBuilder;
import ua.kharkiv.lingvotutor.utils.UIUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

	/** Handle "search" title-bar action. */
	public void onSearchClick(View v) {
		UIUtils.goSearch(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case R.id.home_menu_settings:
			showToast(getString(R.string.toast_not_implemented));
			return true;
		case R.id.home_menu_about:
			
			AlertDialog builder;
			try {
				builder = AboutDialogBuilder.create(this);
				builder.show();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			new AlertDialog.Builder(HomeActivity.this)  
			.setTitle(R.string.app_name)  
			.setMessage(R.string.txt_about)
			.setIcon(R.drawable.ic_launcher)  
			.setPositiveButton(R.string.dlg_btn_ok, null)  
			.show(); */

			return true;
		default:
			// FIXME What is on default menu
			return true;
		}
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

			String nextWordIdStr = cursor
					.getString(DictionaryQuery.DICTIONARY_WORDS_COUNT);
			int nextWordId = Integer.parseInt(nextWordIdStr);
			nextWordId--;

			((TextView) findViewById(R.id.txt_status_line_2))
					.setText(getString(R.string.lbl_status_text_2)
							+ Integer.toString(nextWordId));
		}
	}

	private interface DictionaryQuery {
		String[] PROJECTION = { Dictionary._ID, Dictionary.DICTIONARY_TITLE,
				Dictionary.DICTIONARY_WORDS_COUNT };

		int DICTIONARY_TITLE = 1;
		int DICTIONARY_WORDS_COUNT = 2;
	}
}
