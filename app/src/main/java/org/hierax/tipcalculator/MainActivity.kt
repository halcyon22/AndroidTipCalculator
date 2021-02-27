package org.hierax.tipcalculator

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import org.hierax.tipcalculator.databinding.ActivityMainBinding
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.calculateButton.isEnabled = false
        binding.calculateButton.setOnClickListener { calculate() }
        binding.costInput.addTextChangedListener(CostWatcher(binding))
        binding.costInput.setOnKeyListener { v, keyCode, event -> hideKeyboardOnEnter(v, keyCode) }
        binding.costInput.filters = arrayOf(MoneyInputFilter())
        binding.tipOptionGroup.setOnCheckedChangeListener { group, checkedId -> calculate() }
        binding.roundUpSwitch.setOnClickListener { calculate() }
    }

    private fun calculate() {
        val cost = binding.costInput.text.toString().toDoubleOrNull() ?: return
        val tip = (cost * tipPercent()).toBigDecimal().setScale(
            roundingScale(),
            RoundingMode.UP
        ).toDouble()
        val formattedTip = NumberFormat.getCurrencyInstance().format(tip)
        binding.tipView.text = getString(R.string.tip_format, formattedTip)
        val formattedTotal = NumberFormat.getCurrencyInstance().format(cost + tip)
        binding.totalView.text = getString(R.string.total_format, formattedTotal)
    }

    private fun roundingScale(): Int {
        return when (binding.roundUpSwitch.isChecked) {
            true -> 0
            else -> 2
        }
    }

    private fun tipPercent(): Double {
        return when (binding.tipOptionGroup.checkedRadioButtonId) {
            R.id.tip_forty_button -> .40
            R.id.tip_thirty_button -> .30
            R.id.tip_twentyfive_button -> .25
            else -> .20
        }
    }

    private fun hideKeyboardOnEnter(view: View, keyCode: Int): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            return true
        }
        return false
    }
}

class CostWatcher(private val binding: ActivityMainBinding) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        val cost = binding.costInput.text.toString().toDoubleOrNull()
        binding.calculateButton.isEnabled = cost != null
        binding.tipView.text = ""
        binding.totalView.text = ""
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}

// based on https://stackoverflow.com/a/13716269/421245
class MoneyInputFilter : InputFilter {
    private val inputPattern = Pattern.compile("(0|[1-9]+[0-9]*)?(\\.[0-9]{0,2})?")

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val charsBefore = dest?.subSequence(0, dstart) ?: ""
        val charsAfter = dest?.subSequence(dend, dest.length) ?: ""
        val matcher: Matcher = inputPattern.matcher("${charsBefore}${source}${charsAfter}")
        if (!matcher.matches()) {
            return dest?.subSequence(dstart, dend)
        }
        return null
    }
}