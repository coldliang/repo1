package com.cqupt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.util.CheckAppUpdateThread;
import com.cqupt.util.CheckNetwork;

public class NetStateChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(CheckNetwork.isConnectingToInternet(context)){
			SharedPreferences sp = context.getSharedPreferences("my_prefer",Context.MODE_PRIVATE);
			new CheckAppUpdateThread(sp, context).start();
		}
		
		if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)){  
             Toast.makeText(context, R.string.tip_network_unavailable,Toast.LENGTH_SHORT).show();
        }  
	}
}
