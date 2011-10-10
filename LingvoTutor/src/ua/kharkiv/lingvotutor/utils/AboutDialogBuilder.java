package ua.kharkiv.lingvotutor.utils;

import ua.kharkiv.lingvotutor.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutDialogBuilder {
	public static AlertDialog create(Context context)
			throws NameNotFoundException {

		// Set up the TextView
		final TextView message = new TextView(context);

		message.setText(Html.fromHtml(context.getString(R.string.txt_about)));
		message.setPadding(5, 5, 5, 5);
		message.setMovementMethod(LinkMovementMethod.getInstance());

		return new AlertDialog.Builder(context)
				.setTitle(R.string.app_name)
				.setCancelable(true)
				.setIcon(R.drawable.ic_launcher)
				.setInverseBackgroundForced(true)
				.setPositiveButton(context.getString(android.R.string.ok), null)
				.setView(message).create();
	}
}
