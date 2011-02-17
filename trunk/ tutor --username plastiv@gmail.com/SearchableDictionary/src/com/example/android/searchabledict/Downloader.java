package com.example.android.searchabledict;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class Downloader {
	private static final String TAG = "Downloader";

	private DictionaryDatabase db;

	public Downloader(Context context) {
		db = new DictionaryDatabase(context);
	}

	private void loadData(InputStream inputStream) {
		// TODO How can i minimize cost for calling db.SetWord()
		db.eraseData();
		List<WordsParserItem> parseCards = WordsParser.parse(inputStream);

		for (WordsParserItem msg : parseCards) {
			db.setWord(msg.getWord(), msg.getTranslationWord());
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
