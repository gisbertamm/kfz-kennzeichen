package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.SavedEntry;


public class SearchActivity extends ActionBarActivity implements OnSearchCompletedListener {

    private static final String SEARCH = "searchFragment";
    private static final String RESULT = "resultFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SearchFragment search = (SearchFragment)
                getFragmentManager().findFragmentByTag(SEARCH);
        if (search == null) {
            search = new SearchFragment();
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.container, search, SEARCH).addToBackStack(SEARCH);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSearchCompleted(SavedEntry entry, String code) {
        if (entry != null) {
            Log.d(this.getClass().getSimpleName(), "Matching entry: " + entry);
        } else {
            Log.d(this.getClass().getSimpleName(), "Nothing found for " + code);
        }

        ResultFragment result = (ResultFragment)
                getFragmentManager().findFragmentByTag(RESULT);
        if (result == null) {
            result = ResultFragment.newInstance(entry);

        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.container, result, RESULT).addToBackStack(RESULT);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
