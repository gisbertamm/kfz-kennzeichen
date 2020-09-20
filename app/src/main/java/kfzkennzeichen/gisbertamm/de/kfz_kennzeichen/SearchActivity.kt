package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.ResultFragment.Companion.newInstance
import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.SavedEntry

class SearchActivity : AppCompatActivity(), OnSearchCompletedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    override fun onResume() {
        super.onResume()
        val result = supportFragmentManager.findFragmentByTag(RESULT)
        if (result != null) { // redisplay search result if there is one
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.container, result, RESULT)
            ft.commit()
        } else { // no result; start new search
            var search = supportFragmentManager.findFragmentByTag(SEARCH)
            if (search == null) {
                search = SearchFragment()
            }
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.container, search, SEARCH)
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
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, result, RESULT).addToBackStack(RESULT)
        ft.commit()
    }

    override fun onStatisticsCompleted(statistics: Map<String, Int>?) {
        val statistics = StatisticsFragment.newInstance(statistics)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, statistics, RESULT).addToBackStack(STATISTICS)
        ft.commit()
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount != 0) {
            fragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val SEARCH = "searchFragment"
        private const val RESULT = "resultFragment"
        private const val STATISTICS = "statisticsFragment"
    }
}