package cn.rongcloud.im.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/**
 * Created by zhjchen on 8/12/15.
 */
public class RongHorizontalScrollView extends HorizontalScrollView {

    public RongHorizontalScrollView(Context context) {
        super(context);
        initView();
    }

    public RongHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RongHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RongHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {

        LinearLayout linearLayout = new LinearLayout(getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(layoutParams);

        addView(linearLayout);
    }


    public void addLayoutChildView(View v) {
        if (v == null) return;

        View view = getChildAt(0);

        if (view != null) {
            if (view instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) view;
                layout.addView(v);
            }
        }
    }


    public void removeLayoutChildView(View v) {
        if (v == null) return;

        View view = getChildAt(0);

        if (view != null) {
            if (view instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) view;
                layout.removeView(v);
            }
        }
    }

    public View getLayoutChileView(String tag) {
        if (tag == null) return null;

        View layoutView = getChildAt(0);

        if (layoutView != null) {
            if (layoutView instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) layoutView;
                int childCount = layout.getChildCount();

                for (int i = 0; i < childCount; i++) {
                    View view = layout.getChildAt(i);

                    if (view.getTag() != null && tag.equals(view.getTag())) {
                        return view;
                    }
                }
            }
        }
        return null;
    }
}
