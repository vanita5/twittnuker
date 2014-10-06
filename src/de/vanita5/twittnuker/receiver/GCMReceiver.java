package de.vanita5.twittnuker.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import de.vanita5.twittnuker.service.PushService;

public class GCMReceiver extends WakefulBroadcastReceiver {

	public GCMReceiver() {

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Explicitly specify the IntentService that will handle the intent.
		ComponentName comp = new ComponentName(context.getPackageName(),
				PushService.class.getName());
		// Start the service, keeping the device awake while it is launching.
		startWakefulService(context, (intent.setComponent(comp)));
		setResultCode(Activity.RESULT_OK);
	}
}
