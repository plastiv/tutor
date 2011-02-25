package ua.kharkiv.lingvotutor.provider;

import ua.kharkiv.lingvotutor.provider.DictionaryContract.Dictionary;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.SearchSuggest;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Words;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.WordsColumns;
import ua.kharkiv.lingvotutor.provider.DictionaryDatabase.Tables;
import ua.kharkiv.lingvotutor.utils.SelectionBuilder;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Provider that stores {@link DictionaryContract} data. Data is usually
 * inserted from xml file, and queried by various {@link Activity} instances.
 */
public class DictionaryProvider extends ContentProvider {

	// Provide a mechanism to identify all the incoming uri patterns.
	private static final int WORDS = 100;
	private static final int WORDS_ID = 101;
	private static final int WORDS_SEARCH = 102;

	private static final int DICTIONARY = 200;
	private static final int DICTIONARY_ID = 201;

	private static final int SEARCH_SUGGEST = 800;

	// Deal with OnCreate call back
	private DictionaryDatabase mDatabaseOpenHelper;

	private static final UriMatcher sUriMatcher = buildUriMatcher();

	/**
	 * Builds up a UriMatcher for search suggestion queries.
	 */
	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = DictionaryContract.CONTENT_AUTHORITY;

		matcher.addURI(authority, "words", WORDS);
		matcher.addURI(authority, "words/#", WORDS_ID);
		matcher.addURI(authority, "words/search/*", WORDS_SEARCH);
		matcher.addURI(authority, "words/search", WORDS_SEARCH);

		matcher.addURI(authority, "dictionary", DICTIONARY);
		matcher.addURI(authority, "dictionary/#", DICTIONARY_ID);

		matcher.addURI(authority, SearchManager.SUGGEST_URI_PATH_QUERY,
				SEARCH_SUGGEST);

		return matcher;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = new SelectionBuilder();

		switch (sUriMatcher.match(uri)) {
		case WORDS: {
			builder.table(Tables.WORDS);
			if (selection == null)
				builder.where("1", (String[]) null);
			else
				builder.where(selection, selectionArgs);
			return builder.delete(db);
		}
		case DICTIONARY: {
			// FIXME add trigger, that delete all words, when i delete dictionary
			builder.table(Tables.DICTIONARY);
			if (selection == null)
				builder.where("1", (String[]) null);
			else
				builder.where(selection, selectionArgs);
			return builder.delete(db);
		}
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			return SearchManager.SUGGEST_MIME_TYPE;
		case WORDS:
			return DictionaryContract.Words.CONTENT_TYPE;
		case WORDS_SEARCH:
			return DictionaryContract.Words.CONTENT_TYPE;
		case WORDS_ID:
			return DictionaryContract.Words.CONTENT_ITEM_TYPE;
		case DICTIONARY:
			return DictionaryContract.Dictionary.CONTENT_TYPE;
		case DICTIONARY_ID:
			return DictionaryContract.Dictionary.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		final SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		switch (match) {
		case WORDS: {
			final long wordId = db.insertOrThrow(Tables.WORDS, null, values);
			return ContentUris.withAppendedId(Words.CONTENT_URI, wordId);
		}
		case DICTIONARY: {
			final long dictionaryId = db.insertOrThrow(Tables.DICTIONARY, null,
					values);
			return ContentUris.withAppendedId(Dictionary.CONTENT_URI,
					dictionaryId);
		}
		default: {
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		}
	}

	@Override
	public boolean onCreate() {
		mDatabaseOpenHelper = new DictionaryDatabase(getContext());
		return true;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new IllegalArgumentException("Unknown URI " + uri);

	}

	/** {@inheritDoc} */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		final SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
		final SelectionBuilder builder = new SelectionBuilder();

		final int match = sUriMatcher.match(uri);
		switch (match) {
		case WORDS: {
			builder.table(Tables.WORDS);
			builder.map(BaseColumns._ID, "rowid");

			if (sortOrder == null)
				return builder.query(db, projection, Words.DEFAULT_SORT);
			else
				return builder.query(db, projection, sortOrder);
		}
		case WORDS_SEARCH: {
			builder.table(Tables.WORDS);
			selectionArgs[0] = selectionArgs[0] + "*";
			builder.where(selection, selectionArgs);
			builder.map(BaseColumns._ID, "rowid");

			return builder.query(db, projection, sortOrder);
		}
		case WORDS_ID: {
			final String wordId = Words.getWordId(uri);
			builder.table(Tables.WORDS);
			builder.where(BaseColumns._ID + "=?", wordId);
			builder.map(BaseColumns._ID, "rowid");

			return builder.query(db, projection, sortOrder);
		}
		case DICTIONARY: {
			builder.table(Tables.DICTIONARY);
			builder.map(BaseColumns._ID, "rowid");
			if (sortOrder == null)
				return builder.query(db, projection, Dictionary.DEFAULT_SORT);
			else
				return builder.query(db, projection, sortOrder);
		}
		case SEARCH_SUGGEST: {
			// Adjust incoming query to become SQL text match
			builder.table(Tables.WORDS);

			String selectionSearchSuggest = WordsColumns.WORD_NAME + " MATCH ?";
			selectionArgs[0] = selectionArgs[0] + "*";
			builder.where(selectionSearchSuggest, selectionArgs);

			builder.map(SearchManager.SUGGEST_COLUMN_TEXT_1,
					WordsColumns.WORD_NAME);
			builder.map(SearchManager.SUGGEST_COLUMN_TEXT_2,
					WordsColumns.WORD_TRANSLATION);
			builder.map(BaseColumns._ID, "rowid");
			builder.map(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid");

			projection = new String[] { BaseColumns._ID,
					SearchManager.SUGGEST_COLUMN_TEXT_1,
					SearchManager.SUGGEST_COLUMN_TEXT_2,
					SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };

			final String limit = uri
					.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT);

			return builder.query(db, projection, null, null,
					SearchSuggest.DEFAULT_SORT, limit);
		}
		default: {
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		}
	}
}
