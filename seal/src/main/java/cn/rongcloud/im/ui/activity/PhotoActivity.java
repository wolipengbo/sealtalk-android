package cn.rongcloud.im.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import cn.rongcloud.im.R;
import io.rong.imkit.tools.PhotoFragment;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;

/**
 * Created by DragonJ on 15/4/13.
 */
public class PhotoActivity extends BaseActionBarActivity {
    PhotoFragment mPhotoFragment;
    Uri mUri;
    Uri mDownloaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        mPhotoFragment = (PhotoFragment) getSupportFragmentManager().findFragmentById(R.id.photo_fragment);

        Message message = getIntent().getParcelableExtra("message");
        ImageMessage imageMessage = (ImageMessage) message.getContent();
        if (imageMessage != null) {
        Uri uri = imageMessage.getLocalUri() == null ? imageMessage.getRemoteUri() : imageMessage.getLocalUri();
        mUri = uri;
        if (uri != null)
            mPhotoFragment.initPhoto(message, new PhotoFragment.PhotoDownloadListener() {
                @Override
                public void onDownloaded(Uri uri) {
                    mDownloaded = uri;
                }

                @Override
                public void onDownloadError() {
                    Toast.makeText(PhotoActivity.this, io.rong.imkit.R.string.rc_notice_download_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mUri != null && mUri.getScheme() != null && mUri.getScheme().startsWith("http")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.de_fix_username, menu);
            return super.onCreateOptionsMenu(menu);
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.icon) {
            if (mDownloaded == null) {
                Toast.makeText(this, "正在下载，请稍后保存！", Toast.LENGTH_SHORT).show();
                return true;
            }

            File path = Environment.getExternalStorageDirectory();
            File dir = new File(path, "RongCloud/Image");
            if (!dir.exists())
                dir.mkdirs();

            String name = new File(mDownloaded.getPath()).getName() + ".jpg";
            File to = new File(dir.getAbsolutePath(), name);
            if (to.exists()) {
                Toast.makeText(this, "文件保存至" + to.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                return true;
            }
            copyFile(mDownloaded.toString(), to.getAbsolutePath());
        }
        return super.onOptionsItemSelected(item);
    }

    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "文件保存出错！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            Toast.makeText(this, "文件保存至" + newPath, Toast.LENGTH_SHORT).show();
        }
    }

}
