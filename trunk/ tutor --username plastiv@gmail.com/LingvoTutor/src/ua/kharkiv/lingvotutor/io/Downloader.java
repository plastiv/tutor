package ua.kharkiv.lingvotutor.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Dictionary;
import ua.kharkiv.lingvotutor.provider.DictionaryContract.Words;
import ua.kharkiv.lingvotutor.utils.DictionaryParser;
import ua.kharkiv.lingvotutor.utils.DictionaryParser.Dictionary.Card;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

public class Downloader {
	private static final String TAG = "Downloader";
	private static final String FTYPE_XML = ".xml";
	private static final String FTYPE_ZIP = ".zip";

	private ContentResolver mContentResolver;

	public Downloader(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
	}

	private int loadData(InputStream inputStream) {

		DictionaryParser.Dictionary dictionary = DictionaryParser
				.parse(inputStream);

		if (dictionary != null) {

			ContentResolver contentResolver = mContentResolver;

			ContentValues newDictionaryValue = new ContentValues();
			newDictionaryValue.put(Dictionary.DICTIONARY_TITLE,
					dictionary.getTitle());
			newDictionaryValue.put(Dictionary.DICTIONARY_WORDS_COUNT,
					dictionary.getCount());

			Uri dictionaryUri = contentResolver.insert(Dictionary.CONTENT_URI,
					newDictionaryValue);
			long dictionaryId = Long.parseLong(Dictionary
					.getDictionaryId(dictionaryUri));

			List<Card> parseCards = dictionary.getCards();

			for (Card i : parseCards) {
				ContentValues newValue = new ContentValues();
				newValue.put(Words.WORD_DICTIONARY_ID, dictionaryId);
				newValue.put(Words.WORD_NAME, i.getWord());
				newValue.put(Words.WORD_TRANSLATION, i.getTranslationWord());
				newValue.put(Words.WORD_EXAMPLE, i.getExample());
				newValue.put(Words.WORD_TRANSCRIPTION, i.getTranscription());

				contentResolver.insert(Words.CONTENT_URI, newValue);
			}
			
			return parseCards.size();
		}
		// TODO Write Else behavior
		return -1;
	}

	public int fromFile(String xmlFilename) throws IOException {
		Log.d(TAG, "fromfile(): start loading");
		if (xmlFilename.contains(FTYPE_ZIP)) {
			ZipFile zipfile = new ZipFile(xmlFilename);
			for (Enumeration<? extends ZipEntry> e = zipfile.entries(); e
					.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				if (entry.getName().contains(FTYPE_XML)) {
					return loadData(zipfile.getInputStream(entry));
				}
			}
		} else if (xmlFilename.contains(FTYPE_XML))
			return loadData(new FileInputStream(xmlFilename));
		else
			throw new UnsupportedOperationException("Unknow filetype to open");

		Log.d(TAG, "fromfile(): finish loading");
		// TODO
		return -1;
	}

	public int fromResourse(Resources resources) throws IOException {
		Log.d(TAG, "fromResources(): start loading");
		return loadData(resources.openRawResource(R.raw.example));
	}

	public int fromUrl(String xmlUrl) throws IOException {
		Log.d(TAG, "fromUrl(): start loading");

		if (xmlUrl.contains(FTYPE_XML)) {
			URL feedUrl = new URL(xmlUrl);
			InputStream is = feedUrl.openConnection().getInputStream();
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
			try {
				ZipEntry ze;
				while ((ze = zis.getNextEntry()) != null) {
					if (ze.getName().contains(FTYPE_XML)) {
						// FIXME show msg if zip dont contain xml
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] buffer = new byte[1024];
						int count;
						while ((count = zis.read(buffer)) != -1) {
							baos.write(buffer, 0, count);
						}
						byte[] bytes = baos.toByteArray();
						InputStream xmlIs = new ByteArrayInputStream(bytes);
						return loadData(xmlIs);
					}
				}
			} finally {
				zis.close();
			}
		} else if (xmlUrl.contains(FTYPE_XML)) {
			// TODO Check that url is correct
			URL feedUrl = new URL(xmlUrl);
			return loadData(feedUrl.openConnection().getInputStream());
		} else
			throw new UnsupportedOperationException("Unknow filetype to open");

		Log.d(TAG, "fromUrl(): finish loading");
		return -1;
	}
}
