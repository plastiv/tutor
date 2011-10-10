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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FileOpenActivity extends ListActivity {
	public static final String RESULT_PATH = "result_path";

	private static final String PREFS_NAME = "FileOpenPrefs";
	private static final String PREFS_FILE_DIRECTORY = "PrefsFileDirectory";

	private static final String FTYPE_XML = ".xml";
	private static final String FTYPE_ZIP = ".zip";

	private List<String> directoryEntries = new ArrayList<String>();
	private File currentDirectory = Environment.getExternalStorageDirectory();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fileopen);

		((TextView) findViewById(R.id.title_text)).setText(getTitle());

		browseToLastSaveOrRoot();
	}

	/** Handle "home" title-bar action. */
	public void onHomeClick(View v) {
		UIUtils.goHome(this);
	}

	/** Handle "search" title-bar action. */
	public void onSearchClick(View v) {
		UIUtils.goSearch(this);
	}

	public static boolean isExternalStorageAvailible() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	public static boolean isExternalStorageReadOnly() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
	}

	private void browseToLastSaveOrRoot() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				MODE_PRIVATE);

		String savedFileDirectory = settings.getString(PREFS_FILE_DIRECTORY,
				Environment.getExternalStorageDirectory().getAbsolutePath());

		browseTo(new File(savedFileDirectory));
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
									FileOpenActivity.this
											.onOpenFileClick(aDirectory);
								}
							});

			builder.create().show();
		}
	}

	private void onOpenFileClick(File aFile) {
		// Save last used directory to open next time
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREFS_FILE_DIRECTORY, aFile.getParentFile()
				.getAbsolutePath());
		editor.commit();

		// return filePath as a result
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
			if ((currentFile.isDirectory()
					|| currentFile.getName().contains(FTYPE_XML) || currentFile
					.getName().contains(FTYPE_ZIP)) && currentFile.canRead())
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
