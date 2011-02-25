package ua.kharkiv.lingvotutor.io;

import java.io.IOException;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.ui.DictionaryActivity;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadTask extends AsyncTask<String, Integer, Long> {
	private static final String TAG = "DownloadTask";

	private int taskId;
	private DictionaryActivity mActivity;

	public DownloadTask(DictionaryActivity activity, int id) {
		taskId = id;
		mActivity = activity;
	}

	protected void onPreExecute() {
		mActivity.showDialog(taskId);
	}

	protected Long doInBackground(String... params) {
		try {
			switch (taskId) {
			case R.id.dictionary_menu_url:
				new Downloader(mActivity.getContentResolver())
						.fromUrl(params[0]);
				break;
			case R.id.dictionary_menu_file:
				new Downloader(mActivity.getContentResolver())
						.fromFile(params[0]);
				break;
			case R.id.dictionary_menu_example:
				new Downloader(mActivity.getContentResolver())
						.fromResourse(mActivity.getResources());
				break;
			}
		} catch (IOException e) {
			Log.e(TAG, ".doInBackground(): IOExeption", e);
		}
		return null;
	}

	protected void onPostExecute(Long result) {
		mActivity.updateDictionaryList();
		mActivity.dismissDialog(taskId);
	}
}
