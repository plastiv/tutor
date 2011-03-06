package ua.kharkiv.lingvotutor.ui;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.io.AsyncTaskManager;
import ua.kharkiv.lingvotutor.io.DownloadTask;
import ua.kharkiv.lingvotutor.io.OnTaskCompleteListener;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Dictionary;
import ua.kharkiv.lingvotutor.utils.DialogHelper;
import ua.kharkiv.lingvotutor.utils.NotifyingAsyncQueryHandler;
import ua.kharkiv.lingvotutor.utils.NotifyingAsyncQueryHandler.AsyncQueryListener;
import ua.kharkiv.lingvotutor.utils.UIUtils;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DictionaryActivity extends ListActivity implements
		AsyncQueryListener, OnTaskCompleteListener {
	// FIXME Provide mechanizm to select what dictionary delete

	private static final int PICK_FILE_OPEN = 0;

	private NotifyingAsyncQueryHandler mHandler;
	private DictionaryAdapter mAdapter;
	private ProgressBar mTitleProgressBar;
	public AsyncTaskManager mAsyncTaskManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Request for the progress bar to be shown in the title
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_dictionary);
		registerForContextMenu(getListView());

		((TextView) findViewById(R.id.title_text)).setText(getTitle());
		mTitleProgressBar = (ProgressBar) findViewById(R.id.title_progress_bar);

		mAdapter = new DictionaryAdapter(this);
		setListAdapter(mAdapter);

		mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);

		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			updateDictionaryList();
		} else
			throw new UnsupportedOperationException("Unknown Intent.Action");

		// Create manager and set this activity as context and listener
		mAsyncTaskManager = new AsyncTaskManager(this, this);
		// Handle task that can be retained before
		mAsyncTaskManager.handleRetainedTask(getLastNonConfigurationInstance());
	}

	/** {@inheritDoc} */
	@Override
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
		showToast(getString(R.string.toast_words_were_deleted, result));
	}

	/** {@inheritDoc} */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (id >= 0) {
			Uri dictionaryUri = Dictionary.buildWordsUri(id);
			startActivity(new Intent(Intent.ACTION_VIEW, dictionaryUri));
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
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dictionary_context_menu, menu);

		Cursor c = mAdapter.getCursor();
		c.moveToPosition(((AdapterContextMenuInfo) menuInfo).position);
		menu.setHeaderTitle(c.getString(DictionaryQuery.DICTIONARY_TITLE));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.dictionary_menu_example:
			// Create and run task and progress dialog
			mAsyncTaskManager.setupTask(new DownloadTask(getContentResolver(),
					getResources(), R.id.dictionary_menu_example),
					"unusedParametr");
			return true;
		case R.id.dictionary_menu_file:
			if (FileOpenActivity.isExternalStorageAvailible())
				startActivityForResult(
						new Intent(this, FileOpenActivity.class),
						PICK_FILE_OPEN);
			else
				showToast(getString(R.string.toast_sdcard_not_available));
			return true;
		case R.id.dictionary_menu_url:
			if (isInternetAvailable())
				DialogHelper.getUrlOpenDialog(this).show();
			else
				showToast(getString(R.string.toast_no_connection));
			return true;
		case R.id.dictionary_menu_delete:
			// FIXME deleteDictionary();
			return true;
		default:
			return true;
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.dictionary_context_menu_delete:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			deleteDictionary(info.id);
			return true;
		case R.id.dictionary_context_menu_update:
			// TODO add update dictionary functional
			AdapterContextMenuInfo info2 = (AdapterContextMenuInfo) item
					.getMenuInfo();
			showToast("Update" + Long.toString(info2.id));
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_FILE_OPEN) {
			if (resultCode == RESULT_OK) {
				// A contact was picked. Here we will just display it
				// to the user.
				String filename = data
						.getStringExtra(FileOpenActivity.RESULT_PATH);
				// Create and run task and progress dialog
				mAsyncTaskManager.setupTask(new DownloadTask(
						getContentResolver(), getResources(),
						R.id.dictionary_menu_file), filename);
			} else
				throw new UnsupportedOperationException(
						"onActivityResult has incorrect code");
		} else
			throw new UnsupportedOperationException(
					"onActivityRequest has incorrect code");
	}

	private void deleteDictionary(long id) {
		mTitleProgressBar.setVisibility(View.VISIBLE);

		Uri dictionaryUri = Dictionary.buildDictionaryUri(id);
		mHandler.startDelete(dictionaryUri);
	}

	private void updateDictionaryList() {
		mTitleProgressBar.setVisibility(View.VISIBLE);
		// Start background query to load dictionary
		mHandler.startQuery(Dictionary.CONTENT_URI, DictionaryQuery.PROJECTION,
				Dictionary.DEFAULT_SORT);
	}

	private void showToast(String message) {
		Toast toast = Toast.makeText(getApplicationContext(), message,
				Toast.LENGTH_SHORT);
		toast.show();
	}

	private boolean isInternetAvailable() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mMobile = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		return mWifi.isAvailable() || mMobile.isAvailable();
	}

	@Override
	public void onTaskComplete(DownloadTask task) {
		if (task.isCancelled()) {
			// FIXME add functional, that will actually cancel task
			showToast(getString(R.string.toast_task_was_canceled));
		} else {
			// Get result
			Integer result = null;
			try {
				result = task.get();
			} catch (Exception e) {
				e.printStackTrace();
			}

			updateDictionaryList();
			showToast(getString(R.string.toast_words_were_loaded, result));
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Delegate task retain to manager
		return mAsyncTaskManager.retainTask();
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

			String nextWordIdStr = cursor
					.getString(DictionaryQuery.DICTIONARY_WORDS_COUNT);
			int nextWordId = Integer.parseInt(nextWordIdStr);
			nextWordId--;

			((TextView) view.findViewById(R.id.list_item_dictionary_count))
					.setText(getString(R.string.lbl_words_count)
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
