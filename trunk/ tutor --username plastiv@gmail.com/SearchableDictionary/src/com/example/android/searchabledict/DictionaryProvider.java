package com.example.android.searchabledict;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class DictionaryProvider extends ContentProvider {

	public static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String TAG = "DatabaseHelper";

		public static final String DATABASE_NAME = "dictionary.db";
		public static final int DATABASE_VERSION = 2;

		public static final String TABLE_NAME = "words";

		// The columns we'll include in the dictionary table
		public static final String KEY_WORD = SearchManager.SUGGEST_COLUMN_TEXT_1;
		public static final String KEY_TRANSLATION = SearchManager.SUGGEST_COLUMN_TEXT_2;
		public static final String KEY_EXAMPLE = "example";
		public static final String KEY_TRANSCRIPTION = "transcription";

		public static final String DEFAULT_SORT_ORDER = KEY_WORD + " ASC";

		/*
		 * Note that FTS3 does not support column constraints and thus, you
		 * cannot declare a primary key. However, "rowid" is automatically used
		 * as a unique identifier, so when making requests, we will use "_id" as
		 * an alias for "rowid"
		 */
		private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE "
				+ TABLE_NAME + " USING fts3 (" + KEY_WORD + ", "
				+ KEY_TRANSLATION + ", " + KEY_EXAMPLE + ", " + KEY_TRANSCRIPTION + ");";

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		// Create the database
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(FTS_TABLE_CREATE);
		}

		// Deal with version changes
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}

	public static final String AUTHORITY = "com.example.android.searchabledict.DictionaryProvider";

	// uri and MIME type definitions
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/words");
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.example.android.searchabledict";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/vnd.example.android.searchabledict";

	// Provide a mechanism to identify all the incoming uri patterns.
	private static final int SEARCH_WORDS = 0;
	private static final int GET_WORD = 1;
	private static final int SEARCH_SUGGEST = 2;

	private static final UriMatcher sUriMatcher = buildUriMatcher();

	private static final HashMap<String, String> mColumnMap = buildColumnMap();

	/**
	 * Builds a map for all columns that may be requested, which will be given
	 * to the SQLiteQueryBuilder. This is a good way to define aliases for
	 * column names, but must include all columns, even if the value is the key.
	 * This allows the ContentProvider to request columns w/o the need to know
	 * real column names and create the alias itself.
	 */
	private static HashMap<String, String> buildColumnMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(DatabaseHelper.KEY_WORD, DatabaseHelper.KEY_WORD);
		map.put(DatabaseHelper.KEY_TRANSLATION, DatabaseHelper.KEY_TRANSLATION);
		map.put(DatabaseHelper.KEY_EXAMPLE, DatabaseHelper.KEY_EXAMPLE);
		map.put(DatabaseHelper.KEY_TRANSCRIPTION, DatabaseHelper.KEY_TRANSCRIPTION);
		map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
		map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS "
				+ SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
		return map;
	}

	/**
	 * Builds up a UriMatcher for search suggestion queries.
	 */
	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		// to get definitions...
		matcher.addURI(AUTHORITY, "words", SEARCH_WORDS);
		matcher.addURI(AUTHORITY, "words/#", GET_WORD);
		// to get suggestions...
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
				SEARCH_SUGGEST);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*",
				SEARCH_SUGGEST);

		return matcher;
	}

	// Deal with OnCreate call back
	private DatabaseHelper mDatabaseOpenHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
		int count;

		switch (sUriMatcher.match(uri)) {
		case SEARCH_WORDS:
			count = db.delete(DatabaseHelper.TABLE_NAME, selection,
					selectionArgs);
			break;
		case GET_WORD:
			String rowId = uri.getPathSegments().get(1);
			count = db.delete(DatabaseHelper.TABLE_NAME, BaseColumns._ID
					+ "="
					+ rowId
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private Cursor getSuggestions(String query) {
		query = query.toLowerCase();
		String[] columns = new String[] { BaseColumns._ID,
				DatabaseHelper.KEY_WORD, DatabaseHelper.KEY_TRANSLATION,
				SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };
		String selection = DatabaseHelper.KEY_WORD + " MATCH ?";
		String[] selectionArgs = new String[] { query + "*" };

		return query(selection, selectionArgs, columns);

		/*
		 * This builds a query that looks like: SELECT <columns> FROM <table>
		 * WHERE <KEY_WORD> MATCH 'query*' which is an FTS3 search for the query
		 * text (plus a wildcard) inside the word column.
		 * 
		 * - "rowid" is the unique id for all rows but we need this value for
		 * the "_id" column in order for the Adapters to work, so the columns
		 * need to make "_id" an alias for "rowid" - "rowid" also needs to be
		 * used by the SUGGEST_COLUMN_INTENT_DATA alias in order for suggestions
		 * to carry the proper intent data. These aliases are defined in the
		 * DictionaryProvider when queries are made. - This can be revised to
		 * also search the definition text with FTS3 by changing the selection
		 * clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
		 * the entire table, but sorting the relevance could be difficult.
		 */
	}

	private Cursor getAllRows() {
		String[] columns = new String[] { BaseColumns._ID,
				DatabaseHelper.KEY_WORD, DatabaseHelper.KEY_TRANSLATION };

		return query(null, null, columns);

		/*
		 * This builds a query that looks like: SELECT <columns> FROM <table>
		 * WHERE <KEY_WORD> MATCH 'query*' which is an FTS3 search for the query
		 * text (plus a wildcard) inside the word column.
		 * 
		 * - "rowid" is the unique id for all rows but we need this value for
		 * the "_id" column in order for the Adapters to work, so the columns
		 * need to make "_id" an alias for "rowid" - "rowid" also needs to be
		 * used by the SUGGEST_COLUMN_INTENT_DATA alias in order for suggestions
		 * to carry the proper intent data. These aliases are defined in the
		 * DictionaryProvider when queries are made. - This can be revised to
		 * also search the definition text with FTS3 by changing the selection
		 * clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
		 * the entire table, but sorting the relevance could be difficult.
		 */
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case SEARCH_WORDS:
			return CONTENT_TYPE;
		case GET_WORD:
			return CONTENT_ITEM_TYPE;
		case SEARCH_SUGGEST:
			return SearchManager.SUGGEST_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	private Cursor getWord(Uri uri) {
		String rowId = uri.getLastPathSegment();
		String[] columns = new String[] { DatabaseHelper.KEY_WORD,
				DatabaseHelper.KEY_TRANSLATION, DatabaseHelper.KEY_EXAMPLE,
				DatabaseHelper.KEY_TRANSCRIPTION };

		String selection = "rowid = ?";
		String[] selectionArgs = new String[] { rowId };

		return query(selection, selectionArgs, columns);

		/*
		 * This builds a query that looks like: SELECT <columns> FROM <table>
		 * WHERE rowid = <rowId>
		 */
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {

		// Validate the requested uri
		if (sUriMatcher.match(uri) != SEARCH_WORDS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// validate input fields
		// Make sure that the fields are all set
		if (initialValues.containsKey(DatabaseHelper.KEY_WORD) == false) {
			throw new SQLException(
					"Failed to insert row because KEY_WORD is needed " + uri);
		}
		if (initialValues.containsKey(DatabaseHelper.KEY_TRANSLATION) == false) {
			initialValues.put(DatabaseHelper.KEY_TRANSLATION, "unknown");
		}
		if (initialValues.containsKey(DatabaseHelper.KEY_EXAMPLE) == false) {
			initialValues.put(DatabaseHelper.KEY_EXAMPLE, "no_example");
		}
		if (initialValues.containsKey(DatabaseHelper.KEY_TRANSCRIPTION) == false) {
			initialValues.put(DatabaseHelper.KEY_TRANSCRIPTION, "no_transcription");
		}

		SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
		long rowId = db.insert(DatabaseHelper.TABLE_NAME, null, initialValues);

		if (rowId > 0) {
			Uri insertedBookUri = ContentUris
					.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(insertedBookUri,
					null);
			return insertedBookUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mDatabaseOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	/**
	 * Performs a database query.
	 * 
	 * @param selection
	 *            The selection clause
	 * @param selectionArgs
	 *            Selection arguments for "?" components in the selection
	 * @param columns
	 *            The columns to return
	 * @return A Cursor over all rows matching the query
	 */
	private Cursor query(String selection, String[] selectionArgs,
			String[] columns) {
		/*
		 * The SQLiteBuilder provides a map for all possible columns requested
		 * to actual columns in the database, creating a simple column alias
		 * mechanism by which the ContentProvider does not need to know the real
		 * column names
		 */
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DatabaseHelper.TABLE_NAME);
		builder.setProjectionMap(mColumnMap);

		Cursor cursor = builder.query(
				mDatabaseOpenHelper.getReadableDatabase(), columns, selection,
				selectionArgs, null, null, DatabaseHelper.DEFAULT_SORT_ORDER);

		if (cursor == null) {
			return null;
		} else if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		return cursor;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Use the UriMatcher to see what kind of query we have and format the
		// db query accordingly
		switch (sUriMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			if (selectionArgs == null) {
				throw new IllegalArgumentException(
						"selectionArgs must be provided for the Uri: " + uri);
			}
			// Could provide refresh shortcuts but not now
			return getSuggestions(selectionArgs[0]);
		case SEARCH_WORDS:
			// FIXME return a check for null pls. But how to show all rows?
			if (selectionArgs == null) {
				return getAllRows();
				// throw new IllegalArgumentException(
				// "selectionArgs must be provided for the Uri: " + uri);
			}
			// TODO Could be different from suggestions
			return getSuggestions(selectionArgs[0]);
		case GET_WORD:
			return getWord(uri);
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
		int count;

		switch (sUriMatcher.match(uri)) {
		case SEARCH_WORDS:
			count = db.update(DatabaseHelper.TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case GET_WORD:
			String rowId = uri.getPathSegments().get(1);
			count = db.update(
					DatabaseHelper.TABLE_NAME,
					values,
					BaseColumns._ID
							+ "="
							+ rowId
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
