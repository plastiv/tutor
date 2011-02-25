package ua.kharkiv.lingvotutor.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.utils.UIUtils;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FileOpenActivity extends ListActivity {
	public static final String RESULT_PATH = "result_path";

	// FIXME check permission for open folder
	// TODO Put startDirectory to settings for future open

	// TODO Log Activity private static final String TAG = "FileOpenActivity";
	private static final String FTYPE = ".xml";

	private List<String> directoryEntries = new ArrayList<String>();
	private File currentDirectory = Environment.getExternalStorageDirectory();

	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fileopen);

		((TextView) findViewById(R.id.title_text)).setText(getTitle());

		browseToRoot();
		updateExternalStorageState(); // TODO check SD Card availability
	}

	/** Handle "home" title-bar action. */
	public void onHomeClick(View v) {
		UIUtils.goHome(this);
	}

	/** Handle "search" title-bar action. */
	public void onSearchClick(View v) {
		UIUtils.goSearch(this);
	}

	private void updateExternalStorageState() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}

	/**
	 * This function browses to the root-directory of the file-system.
	 */
	private void browseToRoot() {
		browseTo(Environment.getExternalStorageDirectory());
	}

	/**
	 * This function browses up one level according to the field:
	 * currentDirectory
	 */
	private void upOneLevel() {
		if (this.currentDirectory.getParent() != null)
			this.browseTo(this.currentDirectory.getParentFile());
	}

	private void browseTo(final File aDirectory) {
		// display the full path under title
		((TextView) findViewById(R.id.txt_lbl_currentDir)).setText(aDirectory
				.getAbsolutePath());

		if (aDirectory.isDirectory()) {
			this.currentDirectory = aDirectory;
			fill(aDirectory.listFiles());
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.dlg_lbl_fileOpen)
					+ aDirectory.getName());

			builder.setCancelable(false)
					.setNegativeButton(getString(R.string.dlg_btn_cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							})
					.setPositiveButton(getString(R.string.dlg_btn_ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									FileOpenActivity.this.openFile(aDirectory);
								}
							});

			builder.create().show();
		}
	}

	private void openFile(File aFile) {
		getIntent().putExtra(RESULT_PATH, aFile.getAbsolutePath());
		setResult(RESULT_OK, getIntent());
		finish();
	}

	private void fill(File[] files) {
		this.directoryEntries.clear();

		// Add the ".." == 'Up one level'
		if (this.currentDirectory.getParent() != null)
			this.directoryEntries
					.add(getString(R.string.filename_up_on_one_level));

		// Cut the current-path at the beginning
		int currentPathStringLenght = this.currentDirectory.getAbsolutePath()
				.length();

		for (File currentFile : files) {
			if (currentFile.isDirectory()
					|| currentFile.getName().contains(FTYPE))
				this.directoryEntries.add(currentFile.getAbsolutePath()
						.substring(currentPathStringLenght));
		}

		Collections.sort(this.directoryEntries);

		this.setListAdapter(new ArrayAdapter<String>(this,
				R.layout.list_item_filename, this.directoryEntries));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String selectedFileString = this.directoryEntries.get(position);

		if (selectedFileString
				.equals(getString(R.string.filename_up_on_one_level))) {
			this.upOneLevel();
		} else {
			File clickedFile = new File(this.currentDirectory.getAbsolutePath()
					+ this.directoryEntries.get(position));

			if (clickedFile != null)
				this.browseTo(clickedFile);
		}
	}
}
