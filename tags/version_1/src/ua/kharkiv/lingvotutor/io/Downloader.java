package ua.kharkiv.lingvotutor.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Dictionary;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Words;
import ua.kharkiv.lingvotutor.utils.DictionaryParser;
import ua.kharkiv.lingvotutor.utils.DictionaryParser.Dictionary.Card;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.util.Log;

public class Downloader {
	private static final String TAG = "Downloader";

	private ContentResolver mContentResolver;

	public Downloader(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
	}

	private void loadData(InputStream inputStream) {

		DictionaryParser.Dictionary dictionary = DictionaryParser
				.parse(inputStream);

		if (dictionary != null) {

			ContentResolver contentResolver = mContentResolver;
			contentResolver.delete(Words.CONTENT_URI, "1", null);
			contentResolver.delete(Dictionary.CONTENT_URI, "1", null);

			ContentValues newDictionaryValue = new ContentValues();
			newDictionaryValue.put(Dictionary.DICTIONARY_TITLE,
					dictionary.getTitle());
			newDictionaryValue.put(Dictionary.DICTIONARY_WORDS_COUNT,
					dictionary.getCount());
			contentResolver.insert(Dictionary.CONTENT_URI, newDictionaryValue);

			List<Card> parseCards = dictionary.getCards();

			for (Card i : parseCards) {
				ContentValues newValue = new ContentValues();
				newValue.put(Words.WORD_NAME, i.getWord());
				newValue.put(Words.WORD_TRANSLATION,
						i.getTranslationWord());
				newValue.put(Words.WORD_EXAMPLE, i.getExample());
				newValue.put(Words.WORD_TRANSCRIPTION,
						i.getTranscription());
				contentResolver.insert(Words.CONTENT_URI, newValue);
			}
		}
	}

	public void fromFile(String xmlFilename) throws IOException {
		Log.d(TAG, "fromfile(): start loading");

		// TODO Check SDCard availability
		loadData(new FileInputStream(xmlFilename));

		Log.d(TAG, "fromfile(): finish loading");
	}

	public void fromResourse(Resources resources) throws IOException {
		Log.d(TAG, "fromResources(): start loading");
		loadData(resources.openRawResource(R.raw.example));
		Log.d(TAG, "fromResources(): finish loading");
	}

	public void fromUrl(String xmlUrl) throws IOException {
		Log.d(TAG, "fromUrl(): start loading");

		// TODO Check Internet connection availability
		// TODO Check that url is correct
		URL feedUrl = new URL(xmlUrl);
		loadData(feedUrl.openConnection().getInputStream());

		Log.d(TAG, "fromUrl(): finish loading");
	}
}
