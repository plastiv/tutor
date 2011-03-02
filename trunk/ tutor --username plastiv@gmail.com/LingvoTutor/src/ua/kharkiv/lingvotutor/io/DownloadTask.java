package ua.kharkiv.lingvotutor.io;

import java.io.IOException;

import ua.kharkiv.lingvotutor.R;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadTask extends AsyncTask<String, String, Boolean> {
	private static final String TAG = "DownloadTask";
	
	protected final Resources mResources;

	private int taskId;
	private Boolean mResult;
	private String mProgressMessage;
	private ContentResolver mContentResolver;
	private IProgressTracker mProgressTracker;

	public DownloadTask(ContentResolver contentResolver, Resources resources, int idTask) {
		taskId = idTask;
		mContentResolver = contentResolver;
		// Keep reference to resources
		mResources = resources;
		// Initialise initial pre-execute message
		mProgressMessage = resources.getString(R.string.dlg_lbl_loading);
	}

	/* UI Thread */
	public void setProgressTracker(IProgressTracker progressTracker) {
		// Attach to progress tracker
		mProgressTracker = progressTracker;
		// Initialise progress tracker with current task state
		if (mProgressTracker != null) {
			mProgressTracker.onProgress(mProgressMessage);
			if (mResult != null) {
				mProgressTracker.onComplete();
			}
		}
	}

	/* UI Thread */
	@Override
	protected void onCancelled() {
		// Detach from progress tracker
		mProgressTracker = null;
	}

	/* UI Thread */
    @Override
    protected void onProgressUpdate(String... values) {
	// Update progress message 
	mProgressMessage = values[0];
	// And send it to progress tracker
	if (mProgressTracker != null) {
	    mProgressTracker.onProgress(mProgressMessage);
	}
    }

    /* Separate Thread */
    @Override
	protected Boolean doInBackground(String... params) {

		// Check if task is cancelled
		if (isCancelled()) {
			// This return causes onPostExecute call on UI thread
			return false;
		}
		
		// This call causes onProgressUpdate call on UI thread
		publishProgress(mResources.getString(R.string.dlg_lbl_loading));

		try {
			switch (taskId) {
			case R.id.dictionary_menu_url:
				new Downloader(mContentResolver)
						.fromUrl(params[0]);
				break;
			case R.id.dictionary_menu_file:
				new Downloader(mContentResolver)
						.fromFile(params[0]);
				break;
			case R.id.dictionary_menu_example:
				new Downloader(mContentResolver)
						.fromResourse(mResources);
				break;
			}
		} catch (IOException e) {
			Log.e(TAG, ".doInBackground(): IOExeption", e);
			return false;
		} catch (UnsupportedOperationException e) {
			Log.e(TAG, ".doInBackground(): UnsupportedOperationException", e);
			return false;
		}

		return true;
	}

	/* UI Thread */
	@Override
	protected void onPostExecute(Boolean result) {
		// Update result
		mResult = result;
		// And send it to progress tracker
		if (mProgressTracker != null) {
			mProgressTracker.onComplete();
		}
		// Detach from progress tracker
		mProgressTracker = null;
	}
}
