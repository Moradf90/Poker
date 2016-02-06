package apps.morad.com.poker.customUI;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;

/**
 * Created by Morad on 12/27/2015.
 */
public class LinearViewAdapter extends LinearLayout {

    private Adapter adapter;

    AdapterView.OnItemClickListener mOnItemClickListener;

    private final DataSetObserver observer = new DataSetObserver() {

        @Override
        public void onChanged() {
            refreshViewsFromAdapter();
        }

        @Override
        public void onInvalidated() {
            removeAllViews();
        }
    };

    public LinearViewAdapter(Context context) {
        super(context);
    }

    public LinearViewAdapter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearViewAdapter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public Adapter getAdapter() {
        return adapter;
    }

    public void setAdapter(Adapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(observer);
        }
        this.adapter = adapter;
        if (this.adapter != null) {
            this.adapter.registerDataSetObserver(observer);
        }
        initViewsFromAdapter();
    }

    protected void initViewsFromAdapter() {
        removeAllViews();

        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {

                final View view = adapter.getView(i, null, this);

                final int j = i;

                if(mOnItemClickListener != null){
                    view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnItemClickListener.onItemClick(null, view, j, 0);
                        }
                    });
                }

                addView(view, i);
            }
        }
    }

    protected void refreshViewsFromAdapter() {
        int childCount = getChildCount();
        int adapterSize = adapter.getCount();
        int reuseCount = Math.min(childCount, adapterSize);

        for (int i = 0; i < reuseCount; i++) {

            final View view = adapter.getView(i, getChildAt(i), this);

            final int j = i;

            if(mOnItemClickListener != null){
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onItemClick(null, view, j, 0);
                    }
                });
            }

        }

        if (childCount < adapterSize) {
            for (int i = childCount; i < adapterSize; i++) {
                final View view = adapter.getView(i, null, this);

                final int j = i;

                if(mOnItemClickListener != null){
                    view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            mOnItemClickListener.onItemClick(null, view, j, 0);
                        }
                    });
                }
                addView(view, i);
            }
        } else if (childCount > adapterSize) {
            removeViews(adapterSize, childCount);
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public final AdapterView.OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }


}
