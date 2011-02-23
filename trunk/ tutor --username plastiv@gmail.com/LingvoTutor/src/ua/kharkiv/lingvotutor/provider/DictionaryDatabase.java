
package ua.kharkiv.lingvotutor.provider;

import ua.kharkiv.lingvotutor.provider.DictionaryContract.DictionaryColumns;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.WordsColumns;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper for managing {@link SQLiteDatabase} that stores data for
 * {@link DictionaryProvider}.
 */
public class DictionaryDatabase extends SQLiteOpenHelper {
	private static final String TAG = "DictionaryDatabase";

	private static final String DATABASE_NAME = "dictionary.db";

	// NOTE: carefully update onUpgrade() when bumping database versions to make
	// sure user data is saved.

	private static final int DATABASE_VERSION = 2;

	interface Tables {
		String WORDS = "words";
		String DICTIONARY = "dictionary";
	}

	public DictionaryDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		/*
		 * Note that FTS3 does not support column constraints and thus, you
		 * cannot declare a primary key. However, "rowid" is automatically used
		 * as a unique identifier, so when making requests, we will use "_id" as
		 * an alias for "rowid"
		 */
		db.execSQL("CREATE VIRTUAL TABLE " + Tables.WORDS + " USING fts3 ("
				+ WordsColumns.WORD_NAME + ", " + WordsColumns.WORD_TRANSLATION
				+ ", " + WordsColumns.WORD_EXAMPLE + ", "
				+ WordsColumns.WORD_TRANSCRIPTION + ");");
		db.execSQL("CREATE VIRTUAL TABLE " + Tables.DICTIONARY + " USING fts3 ("
				+ DictionaryColumns.DICTIONARY_TITLE + ", " + DictionaryColumns.DICTIONARY_WORDS_COUNT
 + ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.WORDS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.DICTIONARY);
		onCreate(db);
	}
}
