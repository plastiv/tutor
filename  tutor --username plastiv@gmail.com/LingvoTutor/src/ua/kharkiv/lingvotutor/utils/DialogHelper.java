package ua.kharkiv.lingvotutor.utils;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.io.DownloadTask;
import ua.kharkiv.lingvotutor.ui.DictionaryActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.ClipboardManager;
import android.widget.EditText;

public class DialogHelper {
	
	public static AlertDialog getUrlOpenDialog(final DictionaryActivity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final EditText input = new EditText(activity);

		ClipboardManager clipboard = (ClipboardManager) activity
				.getSystemService(Context.CLIPBOARD_SERVICE);
		if (clipboard.hasText())
			input.setText(clipboard.getText());
		else
			// FIXME Hardlink to Url
			input.setText("http://dl.dropbox.com/u/13226125/To%20kill%20a%20mockbird%28En-Ru%29.xml");
		builder.setMessage(R.string.dlg_lbl_url);

		input.selectAll();

		builder.setCancelable(false)
				.setView(input)
				.setNegativeButton(activity.getString(R.string.dlg_btn_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
			// FIXME add search button who open google with parameters filetype:xml
				.setPositiveButton(activity.getString(R.string.dlg_btn_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								new DownloadTask(activity,
										R.id.dictionary_menu_url).execute(input
										.getText().toString());
							}
						});

		return builder.create();
	}

}
