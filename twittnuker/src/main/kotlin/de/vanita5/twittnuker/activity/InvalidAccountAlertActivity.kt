package de.vanita5.twittnuker.activity

import android.accounts.AccountManager
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_INTENT
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.extension.model.isAccountValid
import de.vanita5.twittnuker.extension.onShow
import de.vanita5.twittnuker.fragment.BaseDialogFragment
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.util.support.removeAccountSupport

class InvalidAccountAlertActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val df = InvalidAccountAlertDialogFragment()
        df.show(supportFragmentManager, "invalid_account_alert")
    }


    class InvalidAccountAlertDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.title_error_invalid_account)
            builder.setMessage(R.string.message_error_invalid_account)
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                val am = AccountManager.get(context)
                AccountUtils.getAccounts(am).filter { !am.isAccountValid(it) }.forEach { account ->
                    am.removeAccountSupport(account)
                }
                val intent = activity.intent.getParcelableExtra<Intent>(EXTRA_INTENT)
                if (intent != null) {
                    activity.startActivity(intent)
                }
            }
            builder.setNegativeButton(android.R.string.cancel) { _, _ ->

            }
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }

        override fun onDismiss(dialog: DialogInterface?) {
            super.onDismiss(dialog)
            if (!activity.isFinishing) {
                activity.finish()
            }
        }

        override fun onCancel(dialog: DialogInterface?) {
            super.onCancel(dialog)
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
    }

}