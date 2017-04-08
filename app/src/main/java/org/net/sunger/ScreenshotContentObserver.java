package org.net.sunger;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;

/**
 * Created by sunger on 2017/4/7.
 */
public class ScreenshotContentObserver {
    //截图文件名关键字
    private static final String[] KEYWORDS = {"screenshot", "screen_shot", "screen-shot", "screen shot", "screencapture", "screen_capture", "screen-capture", "screen capture", "screencap", "screen_cap", "screen-cap", "screen cap", "截屏"};
    //内容观察者中读取的信息
    private static final String[] MEDIA_PHOTO = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.MediaColumns.WIDTH, MediaStore.MediaColumns.HEIGHT};
    private Activity mContext;
    private Handler mHandler;
    //内置存储器观察者
    private ContentObserver mInternalObserver;
    //外置存储器内容观察者
    private ContentObserver mExternalObserver;
    //回调
    private ICallBack iCallBack;

    public ScreenshotContentObserver(Activity context) {
        this.mContext = context;
    }

    public void startObserve(ICallBack iCallBack) {
        this.iCallBack = iCallBack;
        HandlerThread mHandlerThread = new HandlerThread("Screenshot_Observer");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        //初始化
        mInternalObserver = new MediaContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, mHandler);
        mExternalObserver = new MediaContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mHandler);
        //注册
        mContext.getContentResolver().registerContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, false, mInternalObserver);
        mContext.getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, mExternalObserver);
    }

    public void stopObserve() {
        //取消注册
        if (mInternalObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mInternalObserver);
            mContext.getContentResolver().unregisterContentObserver(mExternalObserver);
        }
    }


    private boolean matchAddTime(long addTime) {
        return System.currentTimeMillis() - addTime * 1000 < 6000;
    }

    private boolean matchSize(Point point) {
        Point screenSize = Util.getScreenWidthAndHeight(mContext);
        return screenSize.x == point.x && screenSize.y - point.y <= 50;
    }

    private boolean matchPath(String filePath) {
        String lower = filePath.toLowerCase();
        for (String keyWork : KEYWORDS) {
            if (lower.contains(keyWork)) {
                return true;
            }
        }
        return true;
    }


    private void handleMediaContentChange(Uri contentUri) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    MEDIA_PHOTO,
                    null,
                    null,
                    MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1"
            );
            //判断是否为空
            if (cursor == null) {
                return;
            }
            if (!cursor.moveToFirst()) {
                return;
            }
            //获取图片的路径，创建时间，分辨率
            int dataIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            int dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);
            int dataWidth = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH);
            int dataHeight = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT);

            String data = cursor.getString(dataIndex);
            long dateTaken = cursor.getLong(dateTakenIndex);
            int width = cursor.getInt(dataWidth);
            int height = cursor.getInt(dataHeight);
            Point point = new Point(width, height);
            //根据条件判断是不是截图
            if (matchAddTime(dateTaken) && matchPath(data) && matchSize(point)) {
                if (iCallBack != null) {
                    iCallBack.onScreenShot(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }


    //自定义内容观察者
    private class MediaContentObserver extends ContentObserver {
        private Uri mContentUri;

        public MediaContentObserver(Uri contentUri, Handler handler) {
            super(handler);
            mContentUri = contentUri;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            handleMediaContentChange(mContentUri);
        }
    }

    public interface ICallBack {
        void onScreenShot(String path);
    }
}