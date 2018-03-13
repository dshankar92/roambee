package vvdn.in.ble_ota.dfu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import vvdn.in.ble_ota.view.DfuActivityOperation;


/**
 * Created by Durgesh-Shankar on 1/19/2018.
 */

public class NotificationActivity extends Activity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If this activity is the root activity of the task, the app is not running
        if (isTaskRoot()) {
            // Start the app before finishing
            final Intent intent = new Intent(this, DfuActivityOperation.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtras(getIntent().getExtras()); // copy all extras
            startActivity(intent);
        }

        // Now finish, which will drop you to the activity at which you were at the top of the task stack
        finish();
    }

}
