package de.vanita5.twittnuker.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import kotlinx.android.synthetic.main.activity_keyboard_shortcut_input.*
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.VALUE_THEME_BACKGROUND_DEFAULT
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler.KeyboardShortcutSpec

class KeyboardShortcutPreferenceCompatActivity : BaseActivity(), OnClickListener {

    private var keySpec: KeyboardShortcutSpec? = null
    private var metaState: Int = 0

    override val themeBackgroundOption: String
        get() = VALUE_THEME_BACKGROUND_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyboard_shortcut_input)
        title = KeyboardShortcutsHandler.getActionLabel(this, keyAction)

        buttonPositive.setOnClickListener(this)
        buttonNegative.setOnClickListener(this)
        buttonNeutral.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonPositive -> {
                if (keySpec == null) return
                keyboardShortcutsHandler.register(keySpec, keyAction)
                finish()
            }
            R.id.buttonNeutral -> {
                keyboardShortcutsHandler.unregister(keyAction)
                finish()
            }
            R.id.buttonNegative -> {
                finish()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (KeyEvent.isModifierKey(keyCode)) {
            metaState = metaState or KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (KeyEvent.isModifierKey(keyCode)) {
            metaState = metaState and KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode).inv()
        }
        val keyAction = keyAction ?: return false
        val spec = KeyboardShortcutsHandler.getKeyboardShortcutSpec(contextTag,
                keyCode, event, KeyEvent.normalizeMetaState(metaState or event.metaState))
        if (spec == null || !spec.isValid) {
            return super.onKeyUp(keyCode, event)
        }
        keySpec = spec
        keysLabel.text = spec.toKeyString()
        val oldAction = keyboardShortcutsHandler.findAction(spec)
        val copyOfSpec = spec.copy()
        copyOfSpec.contextTag = null
        val oldGeneralAction = keyboardShortcutsHandler.findAction(copyOfSpec)
        if (!TextUtils.isEmpty(oldAction) && keyAction != oldAction) {
            // Conflicts with keys in same context tag
            conflictLabel.visibility = View.VISIBLE
            val label = KeyboardShortcutsHandler.getActionLabel(this, oldAction)
            conflictLabel.text = getString(R.string.conflicts_with_name, label)

            buttonPositive.setText(R.string.overwrite)
        } else if (!TextUtils.isEmpty(oldGeneralAction) && keyAction != oldGeneralAction) {
            // Conflicts with keys in root context
            conflictLabel.visibility = View.VISIBLE
            val label = KeyboardShortcutsHandler.getActionLabel(this, oldGeneralAction)
            conflictLabel.text = getString(R.string.conflicts_with_name, label)
            buttonPositive.setText(R.string.overwrite)
        } else {
            conflictLabel.visibility = View.GONE
            buttonPositive.setText(android.R.string.ok)
        }
        return true
    }

    private val contextTag: String
        get() = intent.getStringExtra(EXTRA_CONTEXT_TAG)

    private val keyAction: String?
        get() = intent.getStringExtra(EXTRA_KEY_ACTION)

    companion object {

        val EXTRA_CONTEXT_TAG = "context_tag"
        val EXTRA_KEY_ACTION = "key_action"
    }
}