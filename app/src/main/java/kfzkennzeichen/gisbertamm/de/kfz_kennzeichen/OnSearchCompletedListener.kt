package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen

import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.SavedEntry

interface OnSearchCompletedListener {
    fun onSearchCompleted(entry: SavedEntry?, code: String?)
    fun onStatisticsCompleted(statistics: Map<String, Int>?)
}