package ua.kharkiv.lingvotutor.ui;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.io.DownloadTask;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Dictionary;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Words;
import ua.kharkiv.lingvotutor.utils.DialogHelper;
import ua.kharkiv.lingvotutor.utils.NotifyingAsyncQueryHandler;
import ua.kharkiv.lingvotutor.utils.NotifyingAsyncQueryHandler.AsyncQueryListener;
import ua.kharkiv.lingvotutor.utils.UIUtils;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DictionaryActivity extends ListActivity implements
		AsyncQueryListener {

	private static final int PICK_FILE_OPEN = 0;
	private NotifyingAsyncQueryHandler mHandler;
	private DictionaryAdapter mAdapter;
	private ProgressBar mTitleProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Request for the progress bar to be shown in the title
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_dictionary);

		((TextView) findViewById(R.id.title_text)).setText(getTitle());
		mTitleProgressBar = (ProgressBar) findViewById(R.id.title_progress_bar);
		mTitleProgressBar.setVisibility(View.VISIBLE); // show the progress
														// circle

		mAdapter = new DictionaryAdapter(this);
		setListAdapter(mAdapter);

		mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);

		Intent intent = getIntent();

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			updateDictionaryList();
		} else
			throw new UnsupportedOperationException("Unknown Intent.Action");
	}

	/** {@inheritDoc} */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		// hide the progress circle
		mTitleProgressBar.setVisibility(View.INVISIBLE);

		startManagingCursor(cursor);
		mAdapter.changeCursor(cursor);
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteComplete(int token, Object cookie, int result) {
		// hide the progress circle
		mTitleProgressBar.setVisibility(View.INVISIBLE);
		updateDictionaryList();
		// FIXME Show how much rows were deleted
	}

	/** {@inheritDoc} */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (id >= 0) {
			// FIXME Dont forget to choose right dictionary in future
			startActivity(new Intent(Intent.ACTION_VIEW, Words.CONTENT_URI));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/** Handle "home" title-bar action. */
	public void onHomeClick(View v) {
		UIUtils.goHome(this);
	}

	/** Handle "search" title-bar action. */
	public void onSearchClick(View v) {
		UIUtils.goSearch(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dictionary_options_menu, menu);
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// Switch what operation dialog to show
		switch (id) {
		case R.id.dictionary_menu_delete:
			return ProgressDialog.show(this, "",
					getString(R.string.dlg_lbl_deleting), true);
		case R.id.dictionary_menu_file:
		case R.id.dictionary_menu_url:
		case R.id.dictionary_menu_example:
			return ProgressDialog.show(this, "",
					getString(R.string.dlg_lbl_loading), true);
		default:
			throw new IllegalArgumentException("Unknown operationId " + id);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.dictionary_menu_example:
			new DownloadTask(this, R.id.dictionary_menu_example)
					.execute("unused");
			return true;
		case R.id.dictionary_menu_file:
			startActivityForResult(new Intent(this, FileOpenActivity.class),
					PICK_FILE_OPEN);
			return true;
		case R.id.dictionary_menu_url:
			DialogHelper.getUrlOpenDialog(this);
			return true;
		case R.id.dictionary_menu_delete:
			deleteDictionary();
			return true;
		default:
			return true;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_FILE_OPEN) {
			if (resultCode == RESULT_OK) {
				// A contact was picked. Here we will just display it
				// to the user.
				String filename = data
						.getStringExtra(FileOpenActivity.RESULT_PATH);
				new DownloadTask(this, R.id.dictionary_menu_file)
						.execute(filename);
			} else
				throw new UnsupportedOperationException(
						"onActivityResult has incorrect code");
		} else
			throw new UnsupportedOperationException(
					"onActivityRequest has incorrect code");
	}

	private void deleteDictionary() {
		mTitleProgressBar.setVisibility(View.VISIBLE);
		mHandler.startDelete(Words.CONTENT_URI);

		mTitleProgressBar.setVisibility(View.VISIBLE);
		mHandler.startDelete(Dictionary.CONTENT_URI);
	}

	public void updateDictionaryList() {
		// Start background query to load dictionary
		mHandler.startQuery(Dictionary.CONTENT_URI, DictionaryQuery.PROJECTION);
	}

	/**
	 * {@link CursorAdapter} that renders a {@link VendorsQuery}.
	 */
	private class DictionaryAdapter extends CursorAdapter {
		public DictionaryAdapter(Context context) {
			super(context, null);
		}

		/** {@inheritDoc} */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return getLayoutInflater().inflate(R.layout.list_item_dictionary,
					parent, false);
		}

		/** {@inheritDoc} */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			((TextView) view.findViewById(R.id.list_item_dictionary_title))
					.setText(cursor.getString(DictionaryQuery.DICTIONARY_TITLE));
			((TextView) view.findViewById(R.id.list_item_dictionary_count))
					.setText("Words count: "
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
