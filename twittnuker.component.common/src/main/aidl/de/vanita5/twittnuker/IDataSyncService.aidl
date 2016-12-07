// IDataSyncService.aidl
package de.vanita5.twittnuker;

// Declare any non-default types here with import statements
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;

import de.vanita5.twittnuker.model.SyncAuthInfo;

interface IDataSyncService {
    SyncAuthInfo getAuthInfo();

    Intent getAuthRequestIntent(in SyncAuthInfo info);

    void onPerformSync(in SyncAuthInfo info, in Bundle extras, in SyncResult syncResult);
}