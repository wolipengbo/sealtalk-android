package cn.rongcloud.im.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class SwitchItemView extends TextView implements PinnedHandler {

	public SwitchItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	public void handlerPinnedView(View view) {
		if (view != null && view instanceof TextView) {
			((TextView) view).setText(getText().toString());
		}
	}

}
