package ua.kharkiv.lingvotutor.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class DictionaryParser {
	private static final String TAG = "DictionaryParser";

	public static Dictionary parse(InputStream inputStream) {

		try {
			/** Handling XML */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			DictionaryXmlHandler myHandler = new DictionaryParser().new DictionaryXmlHandler();
			xr.setContentHandler(myHandler);
			InputSource inputSource = new InputSource(inputStream);
			xr.parse(inputSource);
			return myHandler.dictionary;
		} catch (SAXException e) {
			Log.e(TAG, "parse(): SAXException", e);
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "parse(): ParserConfigurationException", e);
		} catch (IOException e) {
			Log.e(TAG, "parse(): IOException", e);
		}

		// should not reach here in theory. Should return valid list or throw
		// exception
		throw new UnsupportedOperationException("DictionaryParser.parse(): unreacheable zone reached");
	}

	private class DictionaryXmlHandler extends DefaultHandler {

		private Boolean isInCard = false;
		private Boolean isInWord = false;
		private Boolean isInTranslations = false;
		private Boolean isInExample = false;

		private Dictionary dictionary = null;

		/**
		 * Called when tag starts ( ex:- <name>AndroidPeople</name> -- <name> )
		 */
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			if (localName.equals("dictionary")) {
				dictionary = new Dictionary();
				dictionary.title = attributes.getValue("title");
				dictionary.count = attributes.getValue("nextWordId");
			} else if (localName.equals("card")) {
				isInCard = true;
			} else if (localName.equals("word")) {
				isInWord = true;
			} else if (localName.equals("translations")) {
				isInTranslations = true;
			} else if (localName.equals("example")) {
				isInExample = true;
			}else if (localName.equals("meaning")) {
				dictionary.currentCard.transcription = attributes.getValue("transcription");
			}
		}

		/**
		 * Called when tag closing ( ex:- <name>AndroidPeople</name> -- </name>
		 * )
		 */
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {

			if (localName.equals("card")) {
				isInCard = false;
				dictionary.cards.add(dictionary.currentCard.copy());
			} else if (localName.equals("word")) {
				isInWord = false;
			} else if (localName.equals("translations")) {
				isInTranslations = false;
			} else if (localName.equals("example")) {
				isInExample = false;
			}
		}

		/**
		 * Called to get tag characters ( ex:- <name>AndroidPeople</name> -- to
		 * get AndroidPeople Character )
		 */
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {

			if (isInCard) {
				if (isInWord) {
					if (isInTranslations) {
						dictionary.currentCard.setTranslationWord(new String(
								ch, start, length));
					} else {
						dictionary.currentCard.word = new String(ch, start,
								length);
					}
				} else if (isInExample) {
					dictionary.currentCard.example = new String(ch, start,
							length);
				}
			}
		}
	}

	public class Dictionary {
		private String title;
		private String count;
		private Card currentCard = new Card();
		private List<Card> cards = new ArrayList<Card>();

		public List<Card> getCards() {
			return cards;
		}

		public String getCount() {
			return count;
		}

		public String getTitle() {
			return title;
		}

		public class Card implements Comparable<Card> {
			private String word;
			private String translationWord;
			private String example;
			private String transcription;
			private boolean isTranslationPlural = false;

			public String getWord() {
				return word;
			}

			public String getExample() {
				return example;
			}
			
			public String getTranscription() {
				return transcription;
			}

			public void setTranslationWord(String translationWord) {
				if (isTranslationPlural) {
					this.translationWord = new StringBuilder(
							this.translationWord).append("; ")
							.append(translationWord).toString();
				} else {
					this.translationWord = translationWord;
					isTranslationPlural = true;
				}
			}

			public String getTranslationWord() {
				return translationWord;
			}

			@Override
			public int compareTo(Card another) {
				// FIXME Auto-generated method stub
				return 1;
			}

			public Card copy() {
				Card copy = new Card();
				copy.word = word;
				copy.translationWord = translationWord;
				copy.example = example;
				copy.transcription = transcription;
				this.isTranslationPlural = false;
				return copy;
			}
		}
	}
}
