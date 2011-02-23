package ua.kharkiv.lingvotutor.ui;

import java.io.IOException;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.io.Downloader;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Dictionary;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Words;
import ua.kharkiv.lingvotutor.utils.UIUtils;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class DictionaryActivity extends ListActivity {

	private static final String TAG = "WordsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dictionary);

		((TextView) findViewById(R.id.title_text)).setText(getTitle());

		Intent intent = getIntent();

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			final Uri dictionaryUri = getIntent().getData();
			showDictionaryList(dictionaryUri);
		} else
			throw new UnsupportedOperationException("Unknown Intent.Action");
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

	private void showDictionaryList(Uri dictionaryUri) {
		Cursor cursor = managedQuery(dictionaryUri, DictionaryQuery.PROJECTION,
				null, null, Dictionary.DEFAULT_SORT);

		if (cursor != null) {
			CursorAdapter mAdapter = new DictionaryAdapter(this);
			setListAdapter(mAdapter);
			startManagingCursor(cursor);
			mAdapter.changeCursor(cursor);
		}
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
			new DownloadTask(R.id.dictionary_menu_example).execute("unused");
			return true;
		case R.id.dictionary_menu_file:
			showOpenDialog(R.id.dictionary_menu_file);
			return true;
		case R.id.dictionary_menu_url:
			showOpenDialog(R.id.dictionary_menu_url);
			return true;
		case R.id.dictionary_menu_delete:
			new EraseDataTask().execute((Void) null);
			return true;
		default:
			return true;
		}
	}

	private void showOpenDialog(final int dialogId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		// FIXME hardlink to url
		switch (dialogId) {
		case R.id.dictionary_menu_url:
			input.setText("http://dl.dropbox.com/u/13226125/To%20kill%20a%20mockbird%28En-Ru%29.xml");
			builder.setMessage(R.string.dlg_lbl_url);
			break;
		case R.id.dictionary_menu_file:
			input.setText("sdcard/test.xml");
			builder.setMessage(R.string.dlg_lbl_file);
			break;
		}

		input.selectAll();

		builder.setCancelable(false)
				.setView(input)
				.setNegativeButton(getString(R.string.dlg_btn_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				.setPositiveButton(getString(R.string.dlg_btn_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								switch (dialogId) {
								case R.id.dictionary_menu_url:
									new DownloadTask(R.id.dictionary_menu_url)
											.execute(input.getText().toString());
									break;
								case R.id.dictionary_menu_file:
									new DownloadTask(R.id.dictionary_menu_file)
											.execute(input.getText().toString());
									break;
								}
							}
						});

		AlertDialog alert = builder.create();
		alert.show();
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
				case R.id.dictionary_menu_url:
					new Downloader(getContentResolver()).fromUrl(params[0]);
					break;
				case R.id.dictionary_menu_file:
					new Downloader(getContentResolver()).fromFile(params[0]);
					break;
				case R.id.dictionary_menu_example:
					new Downloader(getContentResolver())
							.fromResourse(getResources());
					break;
				}
			} catch (IOException e) {
				Log.e(TAG, "DownloadTask.doInBackground(): IOExeption", e);
			}
			return null;
		}

		protected void onPostExecute(Long result) {
			showDictionaryList(Dictionary.CONTENT_URI);
			dismissDialog(taskId);
		}
	}

	private class EraseDataTask extends AsyncTask<Void, Integer, Integer> {

		protected void onPreExecute() {
			showDialog(R.id.dictionary_menu_delete);
		}

		protected Integer doInBackground(Void... params) {
			try {
				ContentResolver contentResolver = getContentResolver();
				contentResolver.delete(Dictionary.CONTENT_URI, "1", null);
				return contentResolver.delete(Words.CONTENT_URI, "1", null);
			} catch (Throwable e) {
				Log.e(TAG,
						"EraseDataTask.doInBackground(): SqlExeption during delete all rows in table",
						e);
			}
			// TODO How can i reach this code?
			return Integer.MIN_VALUE;
		}

		protected void onPostExecute(Integer result) {
			showDictionaryList(Dictionary.CONTENT_URI);
			dismissDialog(R.id.dictionary_menu_delete);
		}
	}

	private interface DictionaryQuery {
		String[] PROJECTION = { BaseColumns._ID, Dictionary.DICTIONARY_TITLE,
				Dictionary.DICTIONARY_WORDS_COUNT };

		int DICTIONARY_TITLE = 1;
		int DICTIONARY_WORDS_COUNT = 2;
	}
}
