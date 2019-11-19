package com.nadisoft.app;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nadisoft.shopping.organiser.R;

public class CustomAlertDialogBuilder extends AlertDialog.Builder {

    protected final Context mContext;
	protected View mCustomTitle;
	protected View mCustomBody;
    private TextView mTitle;
    private ImageView mIcon;
    private TextView mMessage;
    private boolean titleSet;
    private boolean viewSet;
    private boolean messageSet;

    public CustomAlertDialogBuilder(Context context) {
    	super(context);
    	mContext = context;
    	init(context, R.layout.alert_dialog_title, R.layout.alert_dialog_message);
    }

    public CustomAlertDialogBuilder(Context context, int bodyView) {
    	super(context);
    	mContext = context;
    	init(context, R.layout.alert_dialog_title, bodyView);
    }

    public CustomAlertDialogBuilder(Context context, int titleView, int bodyView) {
        super(context);
        mContext = context;
        init(context, titleView, bodyView);
    }

	private void init(Context context, int titleView, int bodyView) {
        mCustomTitle = View.inflate(mContext, titleView, null);
        mTitle = (TextView) mCustomTitle.findViewById(R.id.alertTitle);
        mIcon = (ImageView) mCustomTitle.findViewById(R.id.icon);
        super.setCustomTitle(mCustomTitle);

        mCustomBody = View.inflate(mContext, bodyView, null);
        mMessage = (TextView) mCustomBody.findViewById(R.id.message);
        super.setView(mCustomBody);
	}

	@Override
    public CustomAlertDialogBuilder setTitle(int textResId) {
		titleSet = true;
        mTitle.setText(textResId);
        return this;
    }
    @Override
    public CustomAlertDialogBuilder setTitle(CharSequence text) {
		titleSet = true;
        mTitle.setText(text);
        return this;
    }

    @Override
    public CustomAlertDialogBuilder setMessage(int textResId) {
    	messageSet = true;
        mMessage.setText(textResId);
        return this;
    }

    @Override
    public CustomAlertDialogBuilder setMessage(CharSequence text) {
    	messageSet = true;
        mMessage.setText(text);
        return this;
    }

    @Override
    public CustomAlertDialogBuilder setIcon(int drawableResId) {
        mIcon.setImageResource(drawableResId);
        return this;
    }

    @Override
    public CustomAlertDialogBuilder setIcon(Drawable icon) {
        mIcon.setImageDrawable(icon);
        return this;
    }

	@Override
	public CustomAlertDialogBuilder setView(View view) {
		viewSet = true;
		mCustomBody = view;
		super.setView(view);
		return this;
	}

	@Override
	public AlertDialog create() {
		AlertDialog dialog = super.create();
		Log.d("NADIA","on Custom create");
		if ( !titleSet ){
			Log.d("NADIA","on Custom create // titleNotSet, hidding title and divider");
			mCustomTitle.findViewById(R.id.title_template).setVisibility(View.GONE);
			mCustomTitle.findViewById(R.id.titleDivider).setVisibility(View.GONE);
		}
		if ( (viewSet && messageSet) || (!titleSet && messageSet) ){
			Log.d("NADIA","on Custom create // view and message Set! moving text to title template");
			TextView message = (TextView) mCustomTitle.findViewById(R.id.message);
			message.setText(mMessage.getText());
			message.setVisibility(View.VISIBLE);
		}
		if ( !titleSet && messageSet ){
			mMessage.setVisibility(View.GONE);
		}
		return dialog;
	}

}