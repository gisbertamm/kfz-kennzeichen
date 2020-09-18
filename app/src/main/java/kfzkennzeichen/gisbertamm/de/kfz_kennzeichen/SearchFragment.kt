package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen

import android.app.Activity
import android.os.Bundle
import android.text.Html
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.InputFilter.LengthFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.DatabaseHandler
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
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val carSymbol = view.findViewById<View>(R.id.car_symbol) as TextView
        carSymbol.text = Html.fromHtml(UTF8_CAR_SYMBOL)
        val numberplateCodeInput = view.findViewById<View>(R.id.numberplate_code_input) as EditText
        configureInputFilters(numberplateCodeInput)
        val searchButton = view.findViewById<View>(R.id.search_button) as Button
        searchButton.setOnClickListener {
            val db = DatabaseHandler(activity)
            val code = numberplateCodeInput.text.toString()
            val savedEntry = db.searchForCode(code)

            // clear input field
            numberplateCodeInput.editableText.clear()

            // propagate result to parent activity for further processing
            mListener!!.onSearchCompleted(savedEntry, code)
        }
        val randomButton = view.findViewById<View>(R.id.random_button) as Button
        randomButton.setOnClickListener {
            val db = DatabaseHandler(activity)
            val savedEntry = db.searchRandom()

            // propagate result to parent activity for further processing
            mListener!!.onSearchCompleted(savedEntry, savedEntry.code)
        }
        return view
    }

    private fun configureInputFilters(numberplateCodeInput: EditText) {
        val inputFilterlist: MutableList<InputFilter> = ArrayList()
        inputFilterlist.add(LengthFilter(3))
        inputFilterlist.add(AllCaps())
        // add more filters here, if needed
        numberplateCodeInput.filters = inputFilterlist.toTypedArray()
    }

    companion object {
        const val UTF8_CAR_SYMBOL = "&#x1f697;"
    }
}