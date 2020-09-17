package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.ResultFragment.Companion.newInstance
import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.SavedEntry

class SearchActivity : Activity(), OnSearchCompletedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    override fun onResume() {
        super.onResume()
        val result = fragmentManager.findFragmentByTag(RESULT)
        if (result != null) { // redisplay search result if there is one
            val ft = fragmentManager.beginTransaction()
            ft.replace(R.id.container, result as ResultFragment, RESULT)
            ft.commit()
        } else { // no result; start new search
            var search = fragmentManager.findFragmentByTag(SEARCH)
            if (search == null) {
                search = SearchFragment()
            }
            val ft = fragmentManager.beginTransaction()
            ft.replace(R.id.container, search as SearchFragment, SEARCH)
            ft.commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_search, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onSearchCompleted(entry: SavedEntry?, code: String?) {
        if (entry != null) {
            Log.d(this.javaClass.simpleName, "Matching entry: $entry")
        } else {
            Log.d(this.javaClass.simpleName, "Nothing found for $code")
        }
        val result = newInstance(entry)
        val ft = fragmentManager.beginTransaction()
        ft.replace(R.id.container, result, RESULT).addToBackStack(RESULT)
        ft.commit()
    }

    override fun onBackPressed() {
        val fragmentManager = fragmentManager
        if (fragmentManager.backStackEntryCount != 0) {
            fragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val SEARCH = "searchFragment"
        private const val RESULT = "resultFragment"
    }
}