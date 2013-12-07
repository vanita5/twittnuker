package de.vanita5.twittnuker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import de.vanita5.twittnuker.Constants;


public class TestActivity extends Activity implements Constants {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startActivity(new Intent(this, SettingsWizardActivity.class));
		finish();
	}

}
