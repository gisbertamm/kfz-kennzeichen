package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.SavedEntry;

public class ResultFragment extends Fragment {

    public static final String ENTRY = "entry";

    public static ResultFragment newInstance(SavedEntry entry) {
        ResultFragment f = new ResultFragment();

        Bundle args = new Bundle();
        args.putSerializable(ENTRY, entry);
        f.setArguments(args);

        return f;
    }

    public SavedEntry getSavedEntry() {
        return (SavedEntry) getArguments().getSerializable(ENTRY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        TextView code = (TextView) view.findViewById(R.id.result_code);
        TextView district = (TextView) view.findViewById(R.id.result_district);
        TextView districtCenter = (TextView) view.findViewById(R.id.result_district_center);
        TextView jokes = (TextView) view.findViewById(R.id.result_jokes);

        SavedEntry savedEntry = getSavedEntry();

        if (savedEntry != null) {
            code.setText(savedEntry.getCode());

            district.setText(savedEntry.getDistrict());
            districtCenter.setText("abgeleitet von: " + savedEntry.getDistrictCenter()
                    + " (" + savedEntry.get_state() + ")");

            String jokesText = concatJokesStrings(savedEntry.getJokes());
            jokes.setText(jokesText);
        } else {
            code.setText(getString(R.string.nothing_found));
        }

        return view;
    }

    private String concatJokesStrings(List<String> jokesList) {
        StringBuilder jokesBuff = new StringBuilder();
        for (String joke : jokesList) {
            jokesBuff.append(joke);
            jokesBuff.append("\n");
        }
        return jokesBuff.toString();
    }
}
