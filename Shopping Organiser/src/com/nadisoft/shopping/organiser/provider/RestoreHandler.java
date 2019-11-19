/**
 * +----------------------------------------------------------------------+
 * | Taken from CycleSystem      http://cycletodo.jasonantman.com/        |
 * | http://svn.jasonantman.com/Android-CycleSys/trunk/src/com/           |
 * | jasonantman/cycletodo/BackupHandler.java                             |
 * | originally from from http://www.screaming-penguin.com/node/7749      |
 * +----------------------------------------------------------------------+
 * @author Jason Antman <jason@jasonantman.com>
 */
//package com.jasonantman.cycletodo;
package com.nadisoft.shopping.organiser.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.nadisoft.shopping.organiser.R;

public class RestoreHandler extends AsyncTask<Void, Void, Boolean> {
	private static final String TAG = "RestoreHandler"; // for debugging

	private Context parent;
	private ProgressDialog dialog;
	private String filename;

	public RestoreHandler(Context parent) {
		super();
		this.parent = parent;
		this.dialog = new ProgressDialog(this.parent);
		this.filename = ShoppingContract.DB_NAME;
	}

	// can use UI thread here
	protected void onPreExecute() {
		String text = parent.getResources().getString(R.string.setting_import_process);
		this.dialog.setMessage(text+" "+this.filename+" ...");
		this.dialog.show();
	}

	// automatically done on worker thread (separate from UI thread)
	protected Boolean doInBackground(Void... params) {
		File dbFile = new File(Environment.getDataDirectory()
				+ ShoppingContract.INNER_DATA_DB_PATH);
		File restoreFile = new File(Environment.getExternalStorageDirectory(),
				filename);

		try {
			this.copyFile(restoreFile, dbFile);
			Log.d(TAG, " finished copying file.");
			return true;
		} catch (IOException e) {
			Log.d(TAG, " IOException while copying file: " + e.getMessage());
			return false;
		}
	}

	// can use UI thread here
	protected void onPostExecute(final Boolean success) {
		if (this.dialog.isShowing()) {
			this.dialog.dismiss();
		}

		if (success) {
			Toast.makeText(this.parent, R.string.setting_import_success,
					Toast.LENGTH_LONG).show();
			/*
			 * AlertDialog.Builder dlgAlert = new
			 * AlertDialog.Builder(this.parent);
			 * dlgAlert.setMessage("Restore Successful.");
			 * //dlgAlert.setTitle(R.string.backup_ok_title);
			 * dlgAlert.setPositiveButton("OK", null);
			 * dlgAlert.setCancelable(true); dlgAlert.create().show();
			 */
		} else {
			Toast.makeText(this.parent, "Error", Toast.LENGTH_LONG).show();
			/*
			 * AlertDialog.Builder dlgAlert = new
			 * AlertDialog.Builder(this.parent); dlgAlert.setMessage(
			 * "Restore Failure. I'm sorry, but there was an error restoring the backup file."
			 * ); //dlgAlert.setTitle(R.string.backup_error_title);
			 * dlgAlert.setPositiveButton("OK", null);
			 * dlgAlert.setCancelable(true); dlgAlert.create().show();
			 */
		}
	}

	private void copyFile(File src, File dst) throws IOException {
		FileInputStream is = new FileInputStream(src);
		FileOutputStream os = new FileOutputStream(dst);
		FileChannel inChannel = is.getChannel();
		FileChannel outChannel = os.getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null) {
				inChannel.close();
			}
			if (outChannel != null) {
				outChannel.close();
			}
			if (is != null) {
				is.close();
			}

			if (os != null) {
				os.close();
			}

		}
	}

}
