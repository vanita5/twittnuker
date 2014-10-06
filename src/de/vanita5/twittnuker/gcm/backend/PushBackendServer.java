package de.vanita5.twittnuker.gcm.backend;

import java.util.List;

import de.vanita5.twittnuker.model.GoogleAccountItem;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Query;

public interface PushBackendServer {

	public static class AccountItems {
		List<AccountMSG> accounts;
	}

	public static class AccountMSG {
		public String accountid;
		public boolean nmentions;
		public boolean ndms;
		public boolean nfollower;

		public AccountMSG(String accountid) {
			this.accountid = accountid;
		}

		public AccountMSG(GoogleAccountItem account) {
			accountid = account.accountId;
			nmentions = account.nMentions;
			ndms = account.nDMs;
			nfollower = account.nFollower;
		}
	}

	public static class Regid {
		public String regid;
	}

	public static class Dummy {

	}

	/**
	 * Register on backend
	 * @param token
	 * @param regid
	 * @return Dummy
	 */
	@POST("/registergcm")
	Dummy registerGCM(@Header("Authorization") String token, @Body Regid regid);

	/**
	 * Unregister on backend
	 * @param token
	 * @param regid
	 * @return Dummy
	 */
	@POST("/unregistergcm")
	Dummy unregisterGCM(@Header("Authorization") String token, @Body Regid regid);

	@POST("/addaccount")
	AccountMSG addAccount(@Header("Authorization") String token, @Body AccountMSG account);

	@POST("/removeaccount")
	Dummy removeAccount(@Header("Authorization") String token, @Body AccountMSG account);

	/**
	 * Get settings from server
	 * @param token
	 * @return AccountItems (Settings per Account)
	 */
	@GET("/settings")
	AccountItems listAccounts(@Header("Authorization") String token);

	/**
	 * Set settings on server
	 * @param token
	 * @param item
	 * @param regid
	 * @return AccountMSG (Should be the same data that has been sent)
	 */
	@POST("/settings")
	AccountMSG addLink(@Header("Authorization") String token, @Body AccountMSG item, @Query("regid") String regid);
}
