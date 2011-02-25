package ua.kharkiv.lingvotutor.utils;

import ua.kharkiv.lingvotutor.R;
import ua.kharkiv.lingvotutor.io.DownloadTask;
import ua.kharkiv.lingvotutor.ui.DictionaryActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.ClipboardManager;
import android.webkit.URLUtil;
import android.widget.EditText;

public class DialogHelper {

	public static AlertDialog getUrlOpenDialog(final DictionaryActivity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final EditText input = new EditText(activity);

		ClipboardManager clipboard = (ClipboardManager) activity
				.getSystemService(Context.CLIPBOARD_SERVICE);
		if (clipboard.hasText()
				&& URLUtil.isFileUrl(clipboard.getText().toString()))
			input.setText(clipboard.getText());
		else
			// FIXME Hardlink to Url
			input.setText("http://");
		builder.setMessage(R.string.dlg_lbl_url);

		input.setSelection(7); // TODO magic number

		builder.setCancelable(false)
				.setView(input)
				.setNegativeButton(activity.getString(R.string.dlg_btn_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				.setNeutralButton(activity.getString(R.string.dlg_btn_search),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// FIXME When come back from browser program
								// show homActivity and expected to show this
								// dialog
								Intent browserIntent = new Intent(
										"android.intent.action.VIEW",
										Uri.parse("http://www.google.com.ua/search?hl=ru&q=lingvo+tutor"));
								activity.startActivity(browserIntent);
							}
						})
				.setPositiveButton(activity.getString(R.string.dlg_btn_load),
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
