package me.timschneeberger.rootlessjamesdsp.retroid

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import me.timschneeberger.rootlessjamesdsp.activity.BaseActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class RetroidSetupActivity : BaseActivity() {
    @Suppress("unused")
    private companion object {
        private const val HIDDEN_CREDIT = "Retroid JamesJSP Idea by Andiweli"
    }

    private lateinit var deviceStatus: TextView
    private lateinit var bootSwitch: SwitchCompat
    private val presetButtons = LinkedHashMap<String, MaterialButton>()

    private val retroidPrefs by lazy {
        getSharedPreferences(RetroidPreferenceApplier.RETROID_PREFS, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Retroid JamesDSP"
        supportActionBar?.title = "Retroid JamesDSP"

        val scroll = ScrollView(this).apply {
            tag = HIDDEN_CREDIT
            isFillViewport = true
            setBackgroundColor(colorAttr(android.R.attr.windowBackground, 0x000000))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(14))
        }
        scroll.addView(root)

        root.addView(headerRow(), full(t = 0, b = 8))
        root.addView(actionRow(), full(t = 6, b = 2))
        root.addView(bootRow(), full(t = 0, b = 10))

        root.addView(section("Arbitrary response equalizer presets"), full(t = 8, b = 7))
        root.addView(presetRow(), full(t = 0, b = 0))

        setContentView(scroll)
        updatePresetHighlight()
    }

    override fun onResume() {
        super.onResume()
        deviceStatus.text = buildDeviceText()
        updatePresetHighlight()
    }

    private fun headerRow(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val titleColumn = LinearLayout(this@RetroidSetupActivity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        titleColumn.addView(TextView(this@RetroidSetupActivity).apply {
            text = "Retroid JamesDSP"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.START
            setTextColor(colorAttr(android.R.attr.textColorPrimary, 0xffffffff.toInt()))
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        deviceStatus = TextView(this@RetroidSetupActivity).apply {
            text = buildDeviceText()
            textSize = 14f
            gravity = Gravity.START
            setPadding(0, dp(2), 0, 0)
            setTextColor(colorAttr(android.R.attr.textColorSecondary, 0xffcccccc.toInt()))
        }
        titleColumn.addView(deviceStatus, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        addView(titleColumn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        addView(secondaryButton("Open full\nJamesDSP settings", heightDp = 52) {
            runCatching {
                startActivity(Intent(this@RetroidSetupActivity, Class.forName("me.timschneeberger.rootlessjamesdsp.activity.SettingsActivity")))
            }.onFailure {
                Toast.makeText(this@RetroidSetupActivity, "JamesDSP settings not available: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }.apply {
            textSize = 14f
            maxLines = 2
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(dp(12), 0, dp(12), 0)
        }, LinearLayout.LayoutParams(dp(214), LinearLayout.LayoutParams.WRAP_CONTENT))
    }

    private fun actionRow(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER

        addView(materialButton("Enable Retroid\naudio patch", heightDp = 94) {
            runPatchAction("Retroid audio patch", enable = true)
        }, rowWeight(left = false, right = true))

        addView(materialButton("Disable / restore\noriginal mounts", heightDp = 94) {
            runPatchAction("Retroid cleanup", enable = false)
        }, rowWeight(left = true, right = false))
    }

    private fun bootRow(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(0, 0, 0, 0)

        addView(TextView(this@RetroidSetupActivity).apply {
            text = "Apply Retroid patch at boot"
            textSize = 15f
            setTextColor(colorAttr(android.R.attr.textColorPrimary, 0xffffffff.toInt()))
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        bootSwitch = SwitchCompat(this@RetroidSetupActivity).apply {
            isChecked = retroidPrefs.getBoolean(RetroidPreferenceApplier.KEY_APPLY_ON_BOOT, false)
            minWidth = dp(48)
            setPadding(dp(4), 0, 0, 0)
            setOnCheckedChangeListener { _, checked ->
                retroidPrefs.edit().putBoolean(RetroidPreferenceApplier.KEY_APPLY_ON_BOOT, checked).apply()
            }
        }
        addView(bootSwitch, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
    }

    private fun presetRow(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        presetButtons.clear()

        RetroidPresets.all.forEachIndexed { index, preset ->
            val button = materialButton(twoLineText(preset.title, preset.subtitle), heightDp = 94) {
                RetroidPreferenceApplier.applyPreset(this@RetroidSetupActivity, preset, enableStereoWide = true)
                Toast.makeText(this@RetroidSetupActivity, "Applied: ${preset.title}", Toast.LENGTH_LONG).show()
                updatePresetHighlight()
            }.apply {
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
                maxLines = 4
                isSingleLine = false
                insetTop = 0
                insetBottom = 0
                minimumHeight = dp(94)
                textSize = 13.5f
                setPadding(dp(4), dp(4), dp(4), dp(4))
            }
            presetButtons[preset.id] = button
            addView(button, rowWeight(left = index != 0, right = index != RetroidPresets.all.lastIndex))
        }
    }

    private fun runPatchAction(label: String, enable: Boolean) {
        Toast.makeText(this, "$label started", Toast.LENGTH_SHORT).show()
        Thread {
            val result = if (enable) RetroidSystemPatch.enable(this) else RetroidSystemPatch.disable(this)
            runOnUiThread { showResult(label, result) }
        }.start()
    }

    private fun buildDeviceText(): String {
        val model = android.os.Build.MODEL ?: "unknown"
        return "Device: $model"
    }

    private fun showResult(label: String, result: Result<String?>) {
        val msg = if (result.isSuccess) {
            "$label command sent"
        } else {
            "$label failed: ${result.exceptionOrNull()?.message ?: "unknown error"}"
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        deviceStatus.text = buildDeviceText()
    }

    private fun updatePresetHighlight() {
        val activeId = retroidPrefs.getString(RetroidPreferenceApplier.KEY_ACTIVE_PRESET_ID, null)
        val primaryContainer = colorAttr(com.google.android.material.R.attr.colorPrimaryContainer, colorAttr(com.google.android.material.R.attr.colorPrimary, 0xff4f8cff.toInt()))
        val onPrimaryContainer = colorAttr(com.google.android.material.R.attr.colorOnPrimaryContainer, colorAttr(com.google.android.material.R.attr.colorOnPrimary, 0xffffffff.toInt()))
        val surface = colorAttr(com.google.android.material.R.attr.colorSurface, colorAttr(android.R.attr.windowBackground, 0xff202124.toInt()))
        val onSurface = colorAttr(com.google.android.material.R.attr.colorOnSurface, colorAttr(android.R.attr.textColorPrimary, 0xffffffff.toInt()))
        val outline = colorAttr(com.google.android.material.R.attr.colorOutline, 0xff777777.toInt())

        presetButtons.forEach { (id, button) ->
            val active = id == activeId
            button.backgroundTintList = ColorStateList.valueOf(if (active) primaryContainer else surface)
            button.setTextColor(if (active) onPrimaryContainer else onSurface)
            button.strokeColor = ColorStateList.valueOf(if (active) primaryContainer else outline)
            button.strokeWidth = if (active) dp(2) else dp(1)
        }
    }

    private fun section(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 19f
        typeface = Typeface.DEFAULT_BOLD
        setPadding(0, 0, 0, 0)
        setTextColor(colorAttr(android.R.attr.textColorPrimary, 0xffffffff.toInt()))
    }

    private fun materialButton(text: CharSequence, heightDp: Int, onClick: (View) -> Unit): MaterialButton =
        MaterialButton(this).apply {
            this.text = text
            isAllCaps = false
            minHeight = dp(heightDp)
            cornerRadius = dp(12)
            setOnClickListener(onClick)
        }

    private fun secondaryButton(text: CharSequence, heightDp: Int, onClick: (View) -> Unit): MaterialButton =
        materialButton(text, heightDp, onClick).apply {
            val surface = colorAttr(com.google.android.material.R.attr.colorSurface, colorAttr(android.R.attr.windowBackground, 0xff202124.toInt()))
            val onSurface = colorAttr(com.google.android.material.R.attr.colorOnSurface, colorAttr(android.R.attr.textColorPrimary, 0xffffffff.toInt()))
            val outline = colorAttr(com.google.android.material.R.attr.colorOutline, 0xff777777.toInt())
            backgroundTintList = ColorStateList.valueOf(surface)
            setTextColor(onSurface)
            strokeColor = ColorStateList.valueOf(outline)
            strokeWidth = dp(1)
        }

    private fun full(t: Int = 3, b: Int = 3): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { setMargins(0, dp(t), 0, dp(b)) }

    private fun rowWeight(left: Boolean, right: Boolean): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        0,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        1f
    ).apply {
        setMargins(if (left) dp(4) else 0, dp(3), if (right) dp(4) else 0, dp(3))
    }

    private fun twoLineText(title: String, subtitle: String): SpannableString {
        val text = "$title\n$subtitle"
        return SpannableString(text).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(RelativeSizeSpan(1.10f), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(RelativeSizeSpan(0.78f), title.length + 1, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun colorAttr(attr: Int, fallback: Int): Int {
        val typedValue = TypedValue()
        if (!theme.resolveAttribute(attr, typedValue, true)) return fallback
        if (typedValue.resourceId != 0) {
            return runCatching {
                ContextCompat.getColorStateList(this, typedValue.resourceId)?.defaultColor
                    ?: ContextCompat.getColor(this, typedValue.resourceId)
            }.getOrDefault(fallback)
        }
        return typedValue.data
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
