package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;

/**
 * Created by zhjchen on 8/12/15.
 */
public abstract class BasicMapActivity extends FragmentActivity implements  Handler.Callback{

    private MapView mapView;
    private AMap aMap;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(getContentView());

        mapView = initView( savedInstanceState);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        init();

        initData();

        mHandler=new Handler(this);
    }

    public Handler getHandler() {
        return mHandler;
    }

    protected abstract int getContentView();

    protected abstract MapView initView(Bundle savedInstanceState);

    protected abstract void initData();

    public MapView getMapView() {
        return mapView;
    }

    public AMap getaMap() {
        return aMap;
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
