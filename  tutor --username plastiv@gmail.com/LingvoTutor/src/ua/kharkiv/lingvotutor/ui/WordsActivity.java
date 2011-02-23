package ua.kharkiv.lingvotutor.ui;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.provider.DictionaryContract;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Words;
import ua.kharkiv.lingvotutor.utils.UIUtils;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class WordsActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_words);

		((TextView) findViewById(R.id.title_text)).setText(getTitle());

		Intent intent = getIntent();
		Cursor cursor;

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			final Uri wordsUri = getIntent().getData();

			cursor = managedQuery(wordsUri, WordsQuery.PROJECTION,
					WordsQuery.DEFAULT_SELECTION, null, Words.DEFAULT_SORT);

		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

			// handles a search query
			String query = intent.getStringExtra(SearchManager.QUERY);

			cursor = managedQuery(
					DictionaryContract.Words.buildSearchUri(query),
					WordsQuery.PROJECTION, WordsQuery.DEFAULT_SELECTION,
					new String[] { query }, Words.DEFAULT_SORT);
		} else
			throw new UnsupportedOperationException("Unknown Intent.Action");

		if (cursor != null) {
			// FIXME Display the number of results
			CursorAdapter mAdapter = new WordsAdapter(this);
			setListAdapter(mAdapter);
			startManagingCursor(cursor);
			mAdapter.changeCursor(cursor);
		}
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
		String[] PROJECTION = { BaseColumns._ID, Words.WORD_NAME,
				Words.WORD_TRANSLATION };

		String DEFAULT_SELECTION = Words.WORD_NAME + " MATCH ?";

		int WORD_NAME = 1;
		int WORD_TRANSLATION = 2;
	}
}
