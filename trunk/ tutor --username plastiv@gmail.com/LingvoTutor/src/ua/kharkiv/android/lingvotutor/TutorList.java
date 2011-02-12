package ua.kharkiv.android.lingvotutor;

import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class TutorList extends ListActivity {
	// TODO Узнать за пар�?инг параметров в xml
	// TODO Познакомить�?�? �? теибл вью
	// TODO Добавить отображение перевода во вторую колонку
	// TODO Добавить �?охранение карточек во внутреннюю БД
	// TODO Add Test for working code
	
	static final String xmlUrlExample = "http://dl.dropbox.com/u/13226125/To%20kill%20a%20mockbird%28En-Ru%29.xml";
	static final String xmlFilenameExample = "sdcard/test.xml";
	private List<Cards> cards;
	
    private static final int OPENFILE_ID = Menu.FIRST;
    private static final int OPENURL_ID = Menu.FIRST + 1;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
    }
    
    private void showCards (){
    	List<String> words = new ArrayList<String>(cards.size());
    	for (Cards msg : cards){
    		words.add(msg.getWord());
    	}
    	ArrayAdapter<String> adapter = 
    		new ArrayAdapter<String>(this, R.layout.list_item,words);
    	this.setListAdapter(adapter);
    }

	private void loadFeedFromUrl(String xmlUrl){
    	try{
    		// TODO Check Internet connection availability

    		// TODO Check that url is correct
    	    URL feedUrl = new URL(xmlUrl);
    	    
    	    // TODO Check that IO operation is successful 
    	    FeedParser parser = new FeedParser(feedUrl.openConnection().getInputStream());
	    	cards = parser.parse();	   
	    	showCards();
    	} catch (Throwable t){
    		Log.e("AndroidNews",t.getMessage(),t);
    	}
	}
	
	private void loadFeedFromFile(String xmlFilename){
    	try{
    		// TODO Check SDCard availability
    	    
    	    // TODO Check that IO operation is successful 
    	    FeedParser parser = new FeedParser(new FileInputStream(xmlFilename));
	    	cards = parser.parse();	   
	    	showCards();
    	} catch (Throwable t){
    		Log.e("AndroidNews",t.getMessage(),t);
    	}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, OPENFILE_ID, 0, R.string.menu_openfile);
        menu.add(0, OPENURL_ID, 0, R.string.menu_openurl);
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case OPENFILE_ID:
            	loadFeedFromFile(xmlFilenameExample);
                return true;
            case OPENURL_ID:
            	loadFeedFromUrl(xmlUrlExample);
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
}