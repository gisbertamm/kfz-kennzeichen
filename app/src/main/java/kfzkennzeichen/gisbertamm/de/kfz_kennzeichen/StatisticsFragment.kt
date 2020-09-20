package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen

import android.R.attr.textAppearanceMedium
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_statistics.view.*


class StatisticsFragment : Fragment() {
    private val statistics: Array<String>
        get() {
            return arguments?.getSerializable(STATISTICS_KEYS) as Array<String>
        }

    @SuppressLint("ResourceType")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)
        val savedEntry = statistics
        for (key in statistics) {
            val textView = TextView(activity)
            val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, // This will define text view width
                    RelativeLayout.LayoutParams.WRAP_CONTENT // This will define text view height
            )
            params.setMargins(16, 12, 16, 12)
            textView.layoutParams = params

            textView.text = key
            view.statistics_container.addView(textView)
        }
        return view
    }

    companion object {
        const val STATISTICS_KEYS = "statistics_keys"
        const val STATISTICS_VALUES = "statistics_values"

        @JvmStatic
        fun newInstance(statistics: Map<String, Int>?): StatisticsFragment {
            val f = StatisticsFragment()
            val args = Bundle()
            val display = statistics?.map { (key, value) -> "$key: $value" }?.toList()
            args.putStringArray(STATISTICS_KEYS, display?.toTypedArray())
            f.arguments = args
            return f
        }
    }
}