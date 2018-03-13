package vvdn.in.ble_ota.listener;

import android.view.View;

/**
 * This interface to manage single as well as long press click
 *
 * @author Durgesh-Shankar
 */
public interface RecyclerViewItemClickListener {
    public void onClick(View view, int position);

    public void onLongClick(View view, int position);
}