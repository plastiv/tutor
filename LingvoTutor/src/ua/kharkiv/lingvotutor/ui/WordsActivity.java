package ua.kharkiv.lingvotutor.ui;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.provider.DictionaryContract;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Words;
import ua.kharkiv.lingvotutor.utils.NotifyingAsyncQueryHandler;
import ua.kharkiv.lingvotutor.utils.NotifyingAsyncQueryHandler.AsyncQueryListener;
import ua.kharkiv.lingvotutor.utils.UIUtils;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class WordsActivity extends ListActivity implements AsyncQueryListener {

	// FIXME Made include layout for top header (button with title)
	private NotifyingAsyncQueryHandler mHandler;
	private WordsAdapter mAdapter;
	private ProgressBar mTitleProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Request for the progress bar to be shown in the title
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_words);

		((TextView) findViewById(R.id.title_text)).setText(getTitle());
		mTitleProgressBar = (ProgressBar) findViewById(R.id.title_progress_bar);
		// show the progress circle
		mTitleProgressBar.setVisibility(View.VISIBLE);

		mAdapter = new WordsAdapter(this);
		setListAdapter(mAdapter);

		Intent intent = getIntent();

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			final Uri wordsUri = getIntent().getData();

			// Start background query to load tracks
			mHandler = new NotifyingAsyncQueryHandler(getContentResolver(),
					this);
			mHandler.startQuery(wordsUri, WordsQuery.PROJECTION,
					Words.DEFAULT_SORT);

		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

			// handles a search query
			String query = intent.getStringExtra(SearchManager.QUERY);

			((TextView) findViewById(R.id.title_text))
					.setText(getString(R.string.lbl_search_results) + "\""
							+ query + "\"");

			// Start background query to load tracks
			mHandler = new NotifyingAsyncQueryHandler(getContentResolver(),
					this);
			mHandler.startQuery(DictionaryContract.Words.buildSearchUri(query),
					WordsQuery.PROJECTION, WordsQuery.DEFAULT_SELECTION,
					new String[] { query }, Words.DEFAULT_SORT);

		} else
			throw new UnsupportedOperationException("Unknown Intent.Action");
	}

	/** {@inheritDoc} */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		// hide the progress circle
		mTitleProgressBar.setVisibility(View.INVISIBLE);

		startManagingCursor(cursor);
		mAdapter.changeCursor(cursor);

		showToast(getString(R.string.toast_words_are_showed, cursor.getCount()));
	}

	/** {@inheritDoc} */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (id >= 0) {
			// Edit an existing word
			final Uri wordUri = Words.buildWordUri(id);
			startActivity(new Intent(Intent.ACTION_VIEW, wordUri));

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

	private void showToast(String message) {
		Toast toast = Toast.makeText(getApplicationContext(), message,
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * {@link CursorAdapter} that renders a {@link VendorsQuery}.
	 */
	private class WordsAdapter extends CursorAdapter {
		public WordsAdapter(Context context) {
			super(context, null);
		}

		/** {@inheritDoc} */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return getLayoutInflater().inflate(R.layout.list_item_word, parent,
					false);
		}

		/** {@inheritDoc} */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			((TextView) view.findViewById(R.id.list_item_word_name))
					.setText(cursor.getString(WordsQuery.WORD_NAME));
			((TextView) view.findViewById(R.id.list_item_word_translation))
					.setText(cursor.getString(WordsQuery.WORD_TRANSLATION));
		}
	}

	private interface WordsQuery {
		String[] PROJECTION = { Words._ID, Words.WORD_NAME,
				Words.WORD_TRANSLATION };

		String DEFAULT_SELECTION = Words.WORD_NAME + " MATCH ?";

		int WORD_NAME = 1;
		int WORD_TRANSLATION = 2;
	}

	@Override
	public void onDeleteComplete(int token, Object cookie, int result) {
		// TODO Auto-generated method stub
	}
}
