package edu.nadisoft.shopping;

/**
 * AGREGAR UNA LISTA NUEVA, AGREGAR UN ITEM NUEVO, MOVERLO EN CADA LISTA.
 * CERRAR LA APP Y VOLVER Y NO ANDAN BIEN LAS COSAS
 * ADEMAS EL REORDER DESPUES DE TERMINAR BIEN APARECE DE NUEVO CUANDO SE TOCA BACK
 * A VECES LA APP VUELVE CON UN DIALOGO ABIERTO O.o
 */

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import edu.nadisoft.shopping.entities.ShoppingList;
import edu.nadisoft.shopping.entities.ShoppingLists;

/**
 * EditingListsActivity is the Activity used to display the different
 * ShoppingLists, and allows for creation, edition and deletion of them.
 * Also acts as the main menu of the application, giving access to the Reset DB Option
 * @author Nadia
 */
public class EditingListsActivity extends MenuedListActivity {

	private static final int CONFIRM_DELETE_LIST_DIALOG = 1;
	private static final int CHOOSE_LIST_NAME_DIALOG = 2;
	private static Integer list_selected;
	private List<ShoppingList> lists;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        lists = ShoppingLists.getLists();
        Collections.sort(lists, new Comparator<ShoppingList>(){
			public int compare(ShoppingList list1, ShoppingList list2) {
				return list1.getName().compareTo(list2.getName());
			}});
        
        setListAdapter(new ArrayAdapter<ShoppingList>(this, android.R.layout.simple_list_item_1, lists));
		
		ListView lv = getListView();
		registerForContextMenu(lv);
		
		lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object o = parent.getItemAtPosition(position);
				if (o instanceof ShoppingList){
					ShoppingList list = (ShoppingList) o;
					Intent intent = new Intent(getApplicationContext(), ShoppingListActivity.class);
					// TODO che no mejorar esto eh
			        intent.putExtra(EXTRA_LIST_NAME, list.getName());
			        // TODO si se rompe mucho volver a usar FLAG_ACTIVITY_CLEAR_TOP
			        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			        startActivity(intent);
				}
			}});
    }

	@Override
	protected void onPause() {
		ShoppingLists.save();
		super.onPause();
	}

	@SuppressWarnings("unchecked") //TODO mejorar
	public void addNewList(String name) {
    	if ( name.length() > 0 ) {
	    	ListView lv = getListView();
	    	ArrayAdapter<ShoppingList> adapter = (ArrayAdapter<ShoppingList>) lv.getAdapter();
	    	ShoppingLists.addShoppingList(name);
	    	adapter.notifyDataSetChanged();
    	}
    }
	
    @Override
	protected Integer getMenuLayout() {
		return R.menu.lists_main_menu;
	}

	@Override
	protected Integer getContextMenuLayout() {
		return R.menu.lists_context_menu;
	}
	
	@SuppressWarnings("unchecked") /** TODO MEJORAR */
	private void deleteList(int position) {
		ArrayAdapter<ShoppingList> adapter = (ArrayAdapter<ShoppingList>) getListAdapter();
		adapter.remove(adapter.getItem(position));
		ShoppingLists.save();
		//TODO ver si realmente se borra de la base
	}

	@Override
	public boolean handleMenuSelection (int itemId) {
		// Handle item selection
		switch (itemId) {
		case R.id.new_list:
			list_selected = null;
			removeDialog(CHOOSE_LIST_NAME_DIALOG);
			showDialog(CHOOSE_LIST_NAME_DIALOG);
			return true;
		case R.id.factory_settings:
			ShoppingLists.resetListsToFactory();
			lists = ShoppingLists.getLists();
			setListAdapter(new ArrayAdapter<ShoppingList>(this, android.R.layout.simple_list_item_1, lists));
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean handleContextMenuSelection(int itemId, int position) {
		switch (itemId) {
		case R.id.delete_list:
			if (!lists.get(position).filterByNeed()) {
				Toast.makeText(this, R.string.error_cant_delete_home_list, 5).show();
			} else {
				list_selected  = position;
				showDialog(CONFIRM_DELETE_LIST_DIALOG);
			}
			return true;
		case R.id.edit_list:
			list_selected = position;
			removeDialog(CHOOSE_LIST_NAME_DIALOG);
			showDialog(CHOOSE_LIST_NAME_DIALOG);
			return true;
		case R.id.mark_home_list:
			ShoppingLists.setAsHomeList(lists.get(position));
			return true;
		default:
			return false;
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog;
	    switch(id) {
	    case CONFIRM_DELETE_LIST_DIALOG:
	        dialog = createConfirmDeleteListDialog();
	        break;
	    case CHOOSE_LIST_NAME_DIALOG:
	        dialog = createChooseListNameDialog();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}

	private Dialog createConfirmDeleteListDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.confirm_delete_list_question)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					EditingListsActivity.this.deleteList(list_selected);
					list_selected = null;
				}
			})
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					list_selected = null;
					dialog.cancel();
				}
			});
		AlertDialog alert = builder.create();
		return alert;
	}
	
	private Dialog createChooseListNameDialog(){
		final EditText input = new EditText(this);
		if ( list_selected != null ) {
			input.setText(lists.get(list_selected).getName());
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setMessage(R.string.list_name_prompt)
	    	.setView(input)
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int whichButton) {
	    			String value = input.getText().toString();
	    			if ( list_selected != null ) {
	    				lists.get(list_selected).setName(value);
	    				list_selected = null;
	    			}
	    			else {
	    				addNewList(value);
	    			}
	    		}
	    	}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int whichButton) {
	    			list_selected = null;
	    		}
	    	});
		
		return builder.show();
	}
}
