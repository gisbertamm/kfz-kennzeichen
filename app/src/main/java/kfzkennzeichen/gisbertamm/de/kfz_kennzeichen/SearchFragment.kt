package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen

import android.app.Activity
import android.os.Bundle
import android.text.Html
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.InputFilter.LengthFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.DatabaseHandler
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.*

class SearchFragment : Fragment() {
    private var mListener: OnSearchCompletedListener? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        mListener = try {
            activity as OnSearchCompletedListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement " + OnSearchCompletedListener::class.java.simpleName)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        car_symbol.text = Html.fromHtml(UTF8_CAR_SYMBOL)
        configureInputFilters(numberplate_code_input)
        search_button.setOnClickListener {
            val db = activity?.let { it1 -> DatabaseHandler(it1) }
            val code = numberplate_code_input.text.toString()
            val savedEntry = db?.searchForCode(code)

            // clear input field
            numberplate_code_input.editableText.clear()

            // propagate result to parent activity for further processing
            mListener?.onSearchCompleted(savedEntry, code)
        }
        random_button.setOnClickListener {
            val db = activity?.let { it1 -> DatabaseHandler(it1) }
            val savedEntry = db?.searchRandom()

            // propagate result to parent activity for further processing
            mListener?.onSearchCompleted(savedEntry, savedEntry?.code)
        }
        jokes_statistics_button.setOnClickListener {
            val db = activity?.let { it1 -> DatabaseHandler(it1) }
            val statistics = db?.createStatistics()
            mListener?.onStatisticsCompleted(statistics)
            Log.d(TAG, statistics.toString())
        }
    }

    private fun configureInputFilters(numberplateCodeInput: EditText) {
        val inputFilterlist: MutableList<InputFilter> = ArrayList()
        inputFilterlist.add(LengthFilter(3))
        inputFilterlist.add(AllCaps())
        // add more filters here, if needed
        numberplateCodeInput.filters = inputFilterlist.toTypedArray()
    }

    companion object {
        const val TAG = "SearchFragment"
        const val UTF8_CAR_SYMBOL = "&#x1f697;"
    }
}