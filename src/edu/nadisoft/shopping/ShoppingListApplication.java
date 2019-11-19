package edu.nadisoft.shopping;

import android.app.Application;
import android.content.Context;

/**
 * Application Activity
 * @author Nadia
 * - arregle bug select none en shopping -> se hace none en home!
 */
public class ShoppingListApplication extends Application {

	private static ShoppingListApplication instance;

    public ShoppingListApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

}
