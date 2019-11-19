package com.nadisoft.shopping.organiser;

import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.nadisoft.app.CustomAlertDialogBuilder;
import com.nadisoft.shopping.organiser.provider.BackupHandler;
import com.nadisoft.shopping.organiser.provider.RestoreHandler;
import com.nadisoft.shopping.organiser.provider.ShoppingContract;

public class SettingsActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	// supporting API 7
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		setupSettingsButtons();
	}

	@SuppressWarnings("deprecation")
	// supporting API 7
	private void setupSettingsButtons() {
		Preference exportOption = (Preference) findPreference("setting_export");
		StringBuilder sb = new StringBuilder();
		sb.append(getResources().getString(R.string.setting_export_summary_1));
		sb.append(" ");
		sb.append(ShoppingContract.DB_NAME);
		sb.append(" ");
		sb.append(getResources().getString(R.string.setting_export_summary_2));
		exportOption.setSummary(sb.toString());
		exportOption.setOnPreferenceClickListener(exportAction());

		Preference importOption = (Preference) findPreference("setting_import");
		sb = new StringBuilder();
		sb.append(getResources().getString(R.string.setting_import_summary_1));
		sb.append(" ");
		sb.append(ShoppingContract.DB_NAME);
		sb.append(" ");
		sb.append(getResources().getString(R.string.setting_import_summary_2));
		importOption.setSummary(sb.toString());
		importOption.setOnPreferenceClickListener(importAction());
	}

	private OnPreferenceClickListener exportAction() {
		return new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				doBackupToSD();
				return true;
			}
		};
	}

	private OnPreferenceClickListener importAction() {
		return new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				doRestoreFromSD();
				return true;
			}
		};
	}

	/**
	 * Backup the SQLite DB to the SD card
	 */
	protected void doBackupToSD() {
		if (checkExternalStorage()) {
			new BackupHandler(this).execute();
		}
	}

	/**
	 * Restore the SQLite backup from the SD card
	 */
	protected void doRestoreFromSD() {
		if (checkExternalStorage()) {
			new RestoreHandler(this).execute();
		}
	}

	private boolean checkExternalStorage() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(this);
			builder.setMessage(R.string.setting_backup_no_ext);
			builder.setTitle(R.string.error);
			builder.setPositiveButton(R.string.ok, null);
			builder.setCancelable(true);
			builder.create().show();
			return false;
		}
		return true;
	}
}
