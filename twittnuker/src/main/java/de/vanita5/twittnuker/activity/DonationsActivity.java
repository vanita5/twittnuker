/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.sufficientlysecure.donations.DonationsFragment;

import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.R;

public class DonationsActivity extends FragmentActivity {

    /**
     * Google
     */
    private static final String GOOGLE_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs2KZ58y8Z56KchEP2iQHvuznrZAyDf9ULm+L0C2PZKcZjHGxC3XbXH9VC9qVV1GUcPJEIXht0VanUGYPHbCQDVnRPQuNyrF4rOLB5qLEh71IxnlK0OjnKGXRolSTldsZUhC1ja8n5MI0bi3r1oRduM0fDC4E+piIrfRZBjPm6p9OLckwgzz+rulYFErmQAoPhmUr4AvV3WgYNm0Lof+eLpZVpGfxqxOpmt3fMe30/nEnvLVHdOU1wNix9hq94uLrzHVLBuXTT7v99QnX/HB5dztnI54lGK7GvmwCTfrjcgdyf63D4+r1eF/E3Bx2kp/ZtezE0vWGda6bXgecdlJ/LQIDAQAB";
    private static final String[] GOOGLE_CATALOG = new String[]{
            "twittnuker.donate.1",
            "twittnuker.donate.2",
            "twittnuker.donate.5",
            "twittnuker.donate.10",
    };

    /**
     * PayPal
     */
    private static final String PAYPAL_USER = "eliahwinkler@yahoo.de";
    private static final String PAYPAL_CURRENCY_CODE = "EUR";

    /**
     * Flattr
     */
    private static final String FLATTR_PROJECT_URL = "https://github.com/vanita5/twittnuker";
    // FLATTR_URL without http:// !
    private static final String FLATTR_URL = "flattr.com/thing/3694175/vanita5twittnuker-on-GitHub";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_donations);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DonationsFragment donationsFragment;
        donationsFragment = DonationsFragment.newInstance(
                BuildConfig.DEBUG,
                true, GOOGLE_PUBKEY, GOOGLE_CATALOG, getResources().getStringArray(R.array.donation_google_catalog_values), //Google
                true, PAYPAL_USER, PAYPAL_CURRENCY_CODE, getString(R.string.donation),                                      //Paypal
                true, FLATTR_PROJECT_URL, FLATTR_URL,                                                                       //Flattr
                false, null);                                                                                               //Bitcoin


        ft.replace(R.id.donations_activity_container, donationsFragment, "donationsFragment");
        ft.commit();
    }

    /**
     * Needed for Google Play In-app Billing. It uses startIntentSenderForResult(). The result is not propagated to
     * the Fragment like in startActivityForResult(). Thus we need to propagate manually to our Fragment.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("donationsFragment");
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

}