package com.example.android.searchabledict;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.util.Log;

import com.example.android.searchabledict.DictionaryParser.Dictionary;

public class Downloader {
	private static final String TAG = "Downloader";

	private ContentResolver mContentResolver;

	public Downloader(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
	}

	private void loadData(InputStream inputStream) {
		// TODO How can i minimize cost for calling db.SetWord()
		// FIXME Erase data somehow		
		ContentResolver contentResolver = mContentResolver;
		contentResolver.delete(DictionaryProvider.CONTENT_URI, null, null);
		
		List<Dictionary.Card> parseCards = DictionaryParser.parse(inputStream);

		for (Dictionary.Card i : parseCards) {
			ContentValues newValue = new ContentValues();
			newValue.put(DictionaryProvider.DatabaseHelper.KEY_WORD, i.getWord());
			newValue.put(DictionaryProvider.DatabaseHelper.KEY_TRANSLATION, i.getTranslationWord());
			newValue.put(DictionaryProvider.DatabaseHelper.KEY_EXAMPLE, i.getExample());
			newValue.put(DictionaryProvider.DatabaseHelper.KEY_TRANSCRIPTION, i.getTranscription());
			contentResolver.insert(DictionaryProvider.CONTENT_URI, newValue);
		}
	}

	public void fromFile(String xmlFilename) throws IOException {
		Log.d(TAG, "fromfile(): start loading");

		// TODO Check SDCard availability
		// FIXME add functional, that will pretend from dropDable in case
		// IOExeption
		loadData(new FileInputStream(xmlFilename));

		Log.d(TAG, "fromfile(): finish loading");
	}

	public void fromResourse(Resources resources) throws IOException {
		Log.d(TAG, "fromResources(): start loading");

		// FIXME add functional, that will pretend from dropDable in case
		// IOExeption
		loadData(resources.openRawResource(R.raw.irenanew));

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
