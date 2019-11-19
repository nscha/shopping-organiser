package com.nadisoft.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.nadisoft.shopping.organiser.R;

public class HelpDialogBuilder extends CustomAlertDialogBuilder{

	private static final String PREFS = "ShoppingHelpPrefs";
	private static final String FIRST_TIME_NEVER_SHOW_AGAIN = "first_time_help_never_show_again";
	private static final String SHOPPING_HELP_NEVER_SHOW_AGAIN = "shopping_help_never_show_again";
	private static final String EDIT_ITEMS_HELP_NEVER_SHOW_AGAIN = "edit_items_help_never_show_again";
	private static final String EDIT_LISTS_HELP_NEVER_SHOW_AGAIN = "edit_lists_help_never_show_again";

	private HelpType type;

	public HelpDialogBuilder(Context context, HelpType type) {
        super(context, type.getTitleView(), type.getBodyView());
        this.type = type;
        setTitle(type.getTitle());
    }

    @Override
	public AlertDialog create() {
		final AlertDialog dialog = super.create();
		dialog.setCanceledOnTouchOutside(true);
		ImageButton closeButton = (ImageButton) mCustomTitle.findViewById(R.id.close_button);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
			@Override
			public void onDismiss(DialogInterface dialog) {
				boolean neverShowAgain = true;
				if ( HelpType.FIRST_TIME.equals(type)){
					CheckBox check = (CheckBox) mCustomBody.findViewById(R.id.help_never_show_again);
					neverShowAgain = check.isChecked();
				}
				SharedPreferences prefs = mContext.getSharedPreferences(PREFS, 0);
				Editor editor = prefs.edit();
				editor.putBoolean(type.getNeverShowAgainPref(),neverShowAgain);
				editor.commit();
			}
		});

		return dialog;
	}

    public static boolean showAutoHelpDialog(Context context, HelpType type){
    	SharedPreferences prefs = context.getSharedPreferences(PREFS, 0);
    	boolean show = !prefs.getBoolean(type.getNeverShowAgainPref(), false);
    	return show;
    }

	public enum HelpType{
		FIRST_TIME(R.string.first_time_help_title,R.layout.help_first_time, FIRST_TIME_NEVER_SHOW_AGAIN),
		SHOPPING(R.string.shopping_help_title,R.layout.help_shopping, SHOPPING_HELP_NEVER_SHOW_AGAIN),
		EDIT_ITEMS(R.string.edit_items_help_title,R.layout.help_edit_items, EDIT_ITEMS_HELP_NEVER_SHOW_AGAIN),
		EDIT_LISTS(R.string.edit_lists_help_title,R.layout.help_edit_lists, EDIT_LISTS_HELP_NEVER_SHOW_AGAIN);

		private int titleView;
		private int title;
		private int bodyView;
		private String neverShowAgainPref;

		HelpType(int title, int bodyView, String neverShowAgainPref){
			this.titleView = R.layout.help_dialog_title;
			this.title = title;
			this.bodyView = bodyView;
			this.neverShowAgainPref = neverShowAgainPref;
		}

		public int getTitleView() {
			return titleView;
		}

		public int getTitle() {
			return title;
		}

		public int getBodyView() {
			return bodyView;
		}

		public String getNeverShowAgainPref() {
			return neverShowAgainPref;
		}
	}

}
