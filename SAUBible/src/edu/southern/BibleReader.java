package edu.southern;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import edu.southern.resources.BibleHelper;
import edu.southern.resources.Chapter;
import edu.southern.resources.Verse;

public class BibleReader extends Fragment {
	static ArrayAdapter<Verse> adapter;
	BibleHelper Bible = new BibleHelper();
	int scrollto = 0; //Keeps track of the selected verse's textview
	LayoutInflater inflater;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_bible_reader, container,
				false);
	}

	/**
	 * Go through the activity to display the correct action bar layout for this
	 * fragment Initiallize the button to display the current book and chapter
	 * 
	 * @param book
	 *            Current book name
	 * @param chapter
	 *            Current chapter number
	 */
	public void initializeActionBar(String book, int chapter) {
		HomeScreen home = (HomeScreen) getActivity();
		home.setActionBarView(R.layout.actionbar_reading);
		updateActionBar(book, chapter);
	}

	/**
	 * Set the text of the action bar button to display the current book and
	 * chapter
	 * 
	 * @param book
	 *            Current book name
	 * @param chapter
	 *            Current chapter number
	 */
	public void updateActionBar(String book, int chapter) {
		// String building
		String currentLocation = book.concat(" ").concat(
				Integer.toString(chapter));
		((Button) getActivity().findViewById(R.id.ActionBarReading))
				.setText(currentLocation);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Adding the layout programatically
		final ScrollView scrollview = (ScrollView) getActivity().findViewById(
				R.id.scrollView1);
		final LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		scrollview.addView(linearLayout);

		// Get the value of the book selected from SharedPreferences
		SharedPreferences prefs = this.getActivity().getSharedPreferences(
				"edu.southern", Context.MODE_PRIVATE);
		int book_value = prefs.getInt("book_value", 0);

		// prevent a bad book value from crashing the program by defaulting to
		// Genesis
		if (book_value < 0 || book_value > 65)
			book_value = 0;
		int chapter_value = prefs.getInt("chapter_value", 0) + 1;
		int verse_value = prefs.getInt("verse_value", 0) + 1;
		final String bookName = Bible.getBookName(book_value);
		Chapter chapter = null;
		try {
			chapter = Bible.getChapterText(bookName, chapter_value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// set the action bar layout
		initializeActionBar(bookName, chapter_value);

		ArrayList<Verse> bible = chapter.verses;
		for (int i = 0; i < chapter.numVerses; i++) {
			// Populating the layout with verses with different id
			TextView bibleDisplay = new TextView(getActivity());
			bibleDisplay.setId(i + 1);
			Verse verseInfo = bible.get(i);
			String verse = verseInfo.getText();
			int verseNumber = verseInfo.getVerseNumber();

			String bibleInfo = "<strong>" + verseNumber + "</strong>" + " "
					+ "<font size=\"10\">" + verse + "</font>";

			bibleDisplay.setPadding(10, 0, 10, 0);
			bibleDisplay.setText(Html.fromHtml(bibleInfo));
			if(i+1==verse_value)
		    	scrollto = i+1;
			linearLayout.addView(bibleDisplay);
			// Verses onClick handler
			bibleDisplay.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// create Account
					int id = v.getId();
					String text = String.valueOf(id);
					Toast.makeText(getActivity().getApplicationContext(), text,
							Toast.LENGTH_LONG).show();
				}
			});
		}

		// set the text on the currently reading button in the nav drawer
		((HomeScreen) getActivity()).updateCurrentlyReading(book_value,
				chapter_value, verse_value);
		scroll(scrollview);
		
		//Button stuff		
			// Get the value of the book selected from SharedPreferences
		final int bvalue = book_value;
		final int cvalue = chapter_value;
			//Get button IDs
		Button BackBtn = (Button) getActivity().findViewById(R.id.back);
		Button NextBtn = (Button) getActivity().findViewById(R.id.next);
			//Disable button logic
		if (bvalue == 0 && cvalue == 1){ //If at Gen 1, Disable the Back button
			BackBtn.setEnabled(false);
			BackBtn.setClickable(false);
		}
		if (bvalue == 65 && cvalue == 20){ // If at Rev 20, Disable the Next button
			NextBtn.setEnabled(false);
			NextBtn.setClickable(false);
		}
			//Button click logic
				//On NextBtn click, check if end of book
					//If true, go to next book chapter 1
					//If false, go to same book next chapter
		NextBtn.setOnClickListener(new View.OnClickListener() {
			   public void onClick(View v) {
				   try {
					   if (cvalue == Bible.getChapterCount(bookName)){
						   ((HomeScreen) getActivity()).updateCurrentlyReading
						   (bvalue+1, 1, 1);
					   }
					   else{
						   ((HomeScreen) getActivity()).updateCurrentlyReading
						   (bvalue, cvalue+1, 1);
					   }
				   } catch (Exception e) {
					   // TODO Auto-generated catch block
					   e.printStackTrace();
				}
			                }
			            });
				//On BackBtn click, check if beginning of book
					//If true, go to previous book end chapter
					//If false, go to same book previous chapter
		BackBtn.setOnClickListener(new View.OnClickListener() {
			   public void onClick(View v) {
				   try {
					   // Logic needs to be fixed, does not go to end of previous book
					   // Currently goes to previous book chapter 1
					   if (cvalue == Bible.getChapterCount(bookName)){
						   ((HomeScreen) getActivity()).updateCurrentlyReading
						   (bvalue-1, 1, 1);
					   }
					   else{
						   ((HomeScreen) getActivity()).updateCurrentlyReading
						   (bvalue, cvalue-1, 1);
					   }
				   } catch (Exception e) {
					   // TODO Auto-generated catch block
					   e.printStackTrace();
				}
			                }
			            });
	    
	}
	
	/**
	 * Scroll to the selected verse's textview
	 * @param ScrollView
	 *       			scrollview
	 */
	public void scroll(final ScrollView scrollview) {
		scrollview.post(new Runnable() {
	        @Override
	        public void run() {
	        	View contextV  = (TextView) getView().findViewById(scrollto);
	        	scrollto = contextV.getTop();
            	scrollview.scrollTo(0, scrollto);
	        }
	    });
	}
}
