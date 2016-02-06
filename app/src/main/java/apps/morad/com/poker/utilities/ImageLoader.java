package apps.morad.com.poker.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Morad on 12/12/2015.
 */
public class ImageLoader extends AsyncTask<String, Integer, Bitmap> {

    private final WeakReference<ImageView> viewReference;

    public ImageLoader(ImageView view) {
        viewReference = new WeakReference<ImageView>(view);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        return loadBitmap(params[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        ImageView imageView = viewReference.get();
        if (imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    private Bitmap loadBitmap(String url)
    {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
    }
}
