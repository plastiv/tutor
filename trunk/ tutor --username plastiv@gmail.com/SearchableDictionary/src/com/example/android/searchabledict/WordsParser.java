package com.example.android.searchabledict;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

/*
 * Example of xml to Parse
 * 
<?xml version="1.0" encoding="utf-16"?>
<dictionary xmlns="" formatVersion="4" title="TestDictionary" sourceLanguageId="1033" destinationLanguageId="1049" nextWordId="3">
	<statistics defectiveMeaningsQuantity="0" suspendedMeaningsQuantity="0" readyMeaningsQuantity="0" activeMeaningsQuantity="2" repeatedMeaningsQuantity="0" learnedMeaningsQuantity="0"/>
	<card>
		<word wordId="1">TestWord</word>

		<meanings>
			<meaning transcription="TestTranscription">
				<statistics status="3"/>

				<translations>
					<word>Translation Mean</word>
				</translations>

				<examples>
					<example>TestExample</example>
				</examples>
			</meaning>
		</meanings>
	</card>
</dictionary>
 */

public class WordsParser {

	// names of the XML tags
	static final String DICTIONARY = "dictionary";	// root element
	static final String CARD = "card";				// level 1
	static final String WORD = "word";				// level 2
	static final String MEANINGS = "meanings";		// level 2
	static final String MEANING = "meaning";		// level 3
	static final String TRANSLATIONS = "translations";	// level 4
	static final String EXAMPLES = "examples";	// level 4
	static final String EXAMPLE = "example";	// level 5
	
	public WordsParser(){}

	public static List<WordsParserItem> parse(InputStream inputStream) {
		final List<WordsParserItem> wordsParserItem = new ArrayList<WordsParserItem>();
		final WordsParserItem currentCard = new WordsParserItem();
		
		RootElement root = new RootElement(DICTIONARY);
		
		Element card = root.getChild(CARD);
		Element meanings = card.getChild(MEANINGS);
		Element meaning = meanings.getChild(MEANING);
		Element examples = meaning.getChild(EXAMPLES);
		Element translations = meaning.getChild(TRANSLATIONS);
		
		card.setEndElementListener(new EndElementListener(){
			public void end() {
				wordsParserItem.add(currentCard.copy());
			}
		});
		
		card.getChild(WORD).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				currentCard.setWord(body);
			}
		});
		
		examples.getChild(EXAMPLE).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				currentCard.setExample(body);
			}
		});
		
		translations.getChild(WORD).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				currentCard.setTranslationWord(body);
			}
		});
		
		try {
			Xml.parse(inputStream, Xml.Encoding.UTF_16, root.getContentHandler());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return wordsParserItem;
	}
}