package ua.kharkiv.lingvotutor.ui;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Words;
import ua.kharkiv.lingvotutor.utils.UIUtils;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.TextView;

public class WordsDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_words_detail);

		((TextView) findViewById(R.id.title_text)).setText(getTitle());

		Intent intent = getIntent();

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			final Uri wordUri = getIntent().getData();

			Cursor cursor = managedQuery(wordUri, WordsQuery.PROJECTION,
					null, null, null);
			
			// FIXME Put strings to resource
			if (cursor.getCount() == 0) {
				throw new IllegalArgumentException("No such Id " + wordUri);
			} else {
				cursor.moveToFirst();
				((TextView) findViewById(R.id.words_detail_name))
						.setText(cursor.getString(WordsQuery.WORD_NAME));
				((TextView) findViewById(R.id.words_detail_transcription))
				.setText(cursor.getString(WordsQuery.WORD_TRANSCRIPTION));
				((TextView) findViewById(R.id.words_detail_translation))
				.setText(cursor.getString(WordsQuery.WORD_TRANSLATION));
				((TextView) findViewById(R.id.words_detail_example))
				.setText(cursor.getString(WordsQuery.WORD_EXAMPLE));
			}			
		} else
			throw new UnsupportedOperationException("Unknown Intent.Action");
	}

	/** Handle "home" title-bar action. */
	public void onHomeClick(View v) {
		UIUtils.goHome(this);
	}

	/** Handle "search" title-bar action. */
	public void onSearchClick(View v) {
		UIUtils.goSearch(this);
	}

	private interface WordsQuery {
		String[] PROJECTION = { BaseColumns._ID, Words.WORD_NAME,
				Words.WORD_TRANSLATION , Words.WORD_TRANSCRIPTION, Words.WORD_EXAMPLE};

		int WORD_NAME = 1;
		int WORD_TRANSLATION = 2;
		int WORD_TRANSCRIPTION = 3;
		int WORD_EXAMPLE = 4;
	}
}
