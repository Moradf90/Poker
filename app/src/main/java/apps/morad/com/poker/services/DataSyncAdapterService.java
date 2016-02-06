package apps.morad.com.poker.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Objects;

import apps.morad.com.poker.syncAdapters.DataSyncAdapter;

/**
 * Created by Morad on 1/24/2016.
 */
public class DataSyncAdapterService extends Service {

    private DataSyncAdapter _syncAdapter;
    private final static Object lock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();

        if(_syncAdapter == null){
            synchronized (lock){
                if(_syncAdapter == null){
                    _syncAdapter = new DataSyncAdapter(getApplicationContext(), true);
                }
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return _syncAdapter.getSyncAdapterBinder();
    }
}
