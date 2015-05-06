package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.DatabaseHandler;
import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.SavedEntry;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchActivityFragment extends Fragment {

    public static final String UTF8_CAR_SYMBOL = "&#x1f697;";

    public SearchActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        TextView carSymbol = (TextView) view.findViewById(R.id.car_symbol);
        carSymbol.setText(Html.fromHtml(UTF8_CAR_SYMBOL));

        final EditText numberplateCodeInput = (EditText) view.findViewById(R.id.numberplate_code_input);
        configureInputFilters(numberplateCodeInput);

        Button searchButton = (Button) view.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler db = new DatabaseHandler(getActivity());
                String code = numberplateCodeInput.getText().toString();
                SavedEntry savedEntry = db.searchForCode(code);
                if (savedEntry != null) {
                    Log.d(this.getClass().getSimpleName(), "Matching entry: " + savedEntry);
                } else {
                    Log.d(this.getClass().getSimpleName(), "Nothing found for " + code);
                }
            }
        });

        return view;
    }

    private void configureInputFilters(EditText numberplateCodeInput) {
        List<InputFilter> inputFilterlist = new ArrayList<InputFilter>();

        inputFilterlist.add(new InputFilter.LengthFilter(3));
        inputFilterlist.add(new InputFilter.AllCaps());
        // add more filters here, if needed

        numberplateCodeInput.setFilters(inputFilterlist.toArray(new InputFilter[inputFilterlist.size()]));
    }
}
