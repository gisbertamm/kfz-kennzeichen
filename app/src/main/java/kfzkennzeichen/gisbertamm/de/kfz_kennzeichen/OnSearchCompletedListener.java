package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen;

import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.SavedEntry;

public interface OnSearchCompletedListener {
    public void onSearchCompleted(SavedEntry entry, String code);
}
