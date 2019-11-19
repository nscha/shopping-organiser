package com.nadisoft.shopping.organiser;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

public class HelpDialog extends Dialog {

	public enum HelpType{
		FIRST_TIME,SHOPPING,EDIT_ITEMS,EDIT_LISTS
	}

	public HelpDialog(Context context, HelpType type) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.help_first_time);
		setCancelable(true);
		setCanceledOnTouchOutside(true);
		switch (type) {
			case FIRST_TIME:
				firstTimeHelpDialog();
				break;
			case SHOPPING:
				shoppingHelpDialog();
				break;
		}
		ImageButton closeButton = (ImageButton) findViewById(R.id.close_button);
		closeButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
	}

	private void firstTimeHelpDialog(){
		TextView text = (TextView) findViewById(R.id.dialog_title);
		text.setText("Welcome to Shopping Organiser!");

		text = (TextView) findViewById(R.id.help_introduction);
		text.setText("This application is meant to ease the process of normal home shopping.\n" +
				"All screens have a Help menu item available at the top right, use it whenever you are " +
				"not sure about something.");

		text = (TextView) findViewById(R.id.help_normal_use_title);
		text.setText("Normal Use of this Application");

		text = (TextView) findViewById(R.id.help_normal_use);
		text.setText("To begin a new shopping spree use the 'Restart shopping' menu option.\n"+
				"Use the 'Home' view to go through your house, according to your specified layout, "+
				"checking off items you will need to buy.\n"+
				"On your local shop, use a 'Shop' view to see the items you need to buy, they will be "+
				"displayed in the order set up for the shop.\n"+
				"Go through the shop checking off the items as you pick them up to keep track of "+
				"your purchase.");

		text = (TextView) findViewById(R.id.help_setup_title);
		text.setText("Application Setup");

		text = (TextView) findViewById(R.id.help_setup);
		text.setText("You will need to edit the initial lists (or add new ones) and add all of the items "+
				"you normally buy (items appear in all lists, lists only setup the order in which "+
				"they appear).\n"+
				"Reorder the items using drag and drop in each edit list screen according to your Home "+
				"and local Shop layout (you can add more shops for more layouts).");
	}

	private void shoppingHelpDialog(){
		setTitle("Shopping Screen");
		TextView text = (TextView) findViewById(R.id.help_introduction);
		text.setText("This is the main application screen, it manages the Home view and as many Shop views "+
				"as you have created. On the Home view you can check items that you need to buy, and then " +
				"in the Shop view only those checked items will appear, and you can check them as you " +
				"pick them up");

		text = (TextView) findViewById(R.id.help_quick_reference_title);
		text.setText("Menu Quick Reference");

		text = (TextView) findViewById(R.id.help_quick_reference_help_menu);
		text.setText("Help: On the top right of your screen you will always have the question mark icon, "+
				"use it to get tips on the use of the current screen");

		text = (TextView) findViewById(R.id.help_quick_reference_edit_list);
		text.setText("Edit this list: Takes you to the screen where you can add items, and also set " +
				"the ordering of the items for the current list");

		text = (TextView) findViewById(R.id.help_quick_reference_edit_lists);
		text.setText("Edit lists: Takes you to the screen where you can add new shop lists (in case " +
				"you normally use more than one local shop and need more than one shop layout), you can also " +
				"edit the names of the initial lists");

		text = (TextView) findViewById(R.id.help_quick_reference_restart);
		text.setText("Restart shopping: allows you to quickly uncheck all items on all lists " +
				"so you can start your shopping anew");
	}

	public void cancel(View view){
		cancel();
	}
}
