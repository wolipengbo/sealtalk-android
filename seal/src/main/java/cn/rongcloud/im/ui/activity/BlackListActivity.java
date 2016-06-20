package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetBlackListResponse;
import cn.rongcloud.im.server.widget.LoadDialog;

/**
 * Created by Bob on 2015/4/9.
 */
public class BlackListActivity extends BaseActionBarActivity {

    private static final int GETBLACKLIST = 66;
    private String TAG = BlackListActivity.class.getSimpleName();

    private TextView isShowData;

    private ListView blackList;

    private List<GetBlackListResponse.ResultEntity> dataList;

    private MyBlackListAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_black);
        getSupportActionBar().setTitle(R.string.the_blacklist);
        initView();
        requestData();
    }

    private void requestData() {
        LoadDialog.show(mContext);
        request(GETBLACKLIST);
    }

    private void initView() {
        isShowData = (TextView) findViewById(R.id.blacklsit_show_data);
        blackList = (ListView) findViewById(R.id.blacklsit_list);
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        return action.getBlackList();
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            GetBlackListResponse response = (GetBlackListResponse) result;
            if (response.getCode() == 200) {
                LoadDialog.dismiss(mContext);
                dataList =  response.getResult();
                if (dataList != null) {
                    if (dataList.size() > 0) {
                        adapter = new MyBlackListAdapter(dataList);
                        blackList.setAdapter(adapter);
                    }else {
                        isShowData.setVisibility(View.VISIBLE);
                    }
                }
            }

        }
    }

    class MyBlackListAdapter extends BaseAdapter{

        private List<GetBlackListResponse.ResultEntity> dataList;

        public MyBlackListAdapter(List<GetBlackListResponse.ResultEntity> dataList){
            this.dataList = dataList;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHoler viewHolder = null;
            GetBlackListResponse.ResultEntity bean = dataList.get(position);
            if (convertView == null) {
                viewHolder = new ViewHoler();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.black_item_new, null);
                viewHolder.mName = (TextView) convertView.findViewById(R.id.blackname);
                viewHolder.mHead = (ImageView) convertView.findViewById(R.id.blackuri);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHoler) convertView.getTag();
            }
            viewHolder.mName.setText(bean.getUser().getNickname());
            ImageLoader.getInstance().displayImage(bean.getUser().getPortraitUri(), viewHolder.mHead, App.getOptions());
            return convertView;
        }


        class ViewHoler {
            ImageView mHead;
            TextView mName;
        }
    }
}
