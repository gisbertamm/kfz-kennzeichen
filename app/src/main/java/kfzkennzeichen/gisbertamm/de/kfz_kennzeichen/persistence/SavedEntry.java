package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SavedEntry implements Serializable {
    private int _id;
    private String _code;
    private String _district;
    private String _district_center;
    private String _state;

    public String getDistrictCenter() {
        return _district_center;
    }

    public void setDistrictCenter(String districtCenter) {
        this._district_center = districtCenter;
    }

    public String get_state() {
        return _state;
    }

    public void set_state(String _state) {
        this._state = _state;
    }

    private String _district_wikipedia_url;
    private List<String> _jokes;

    public SavedEntry() {
    }

    public SavedEntry(int id, String code, String district, String districtCenter,
                      String state, String districtWikipediaUrl) {
        this._id = id;
        this._code = code;
        this._district = district;
        this._district_center = districtCenter;
        this._state = state;
        this._district_wikipedia_url = districtWikipediaUrl;
        this._jokes = new ArrayList<String>();
    }

    public int getId() {
        return _id;

    }

    public void setId(int id) {
        this._id = id;
    }

    public String getCode() {
        return _code;
    }

    public void setCode(String code) {
        this._code = code;
    }

    public String getDistrict() {
        return _district;
    }

    public void setDistrict(String district) {
        this._district = district;
    }

    public String getDistrictWikipediaUrl() {
        return _district_wikipedia_url;
    }

    public void setDistrictWikipediaUrl(String districtWikipediaUrl) {
        this._district_wikipedia_url = districtWikipediaUrl;
    }

    public List<String> getJokes() {
        return _jokes;
    }

    public void setJoke(String joke) {
        this._jokes.add(joke);
    }

    @Override
    public String toString() {
        return "SavedEntry{" +
                "_id=" + _id +
                ", _code='" + _code + '\'' +
                ", _district='" + _district + '\'' +
                ", _district_center='" + _district_center + '\'' +
                ", _state='" + _state + '\'' +
                ", _district_wikipedia_url='" + _district_wikipedia_url + '\'' +
                ", _jokes='" + _jokes + '\'' +
                '}';
    }

    public String getCrestIdentifier() {
        String s = getCode().toLowerCase();
        // cannot use reserved Java words
        if ("for".equals(s) || "do".equals(s) || "new".equals(s)) {
            s = s + s;
        }
        // cannot use German umlauts
        return s.replaceAll("ä", "ae").replaceAll("ö", "oe").replaceAll("ü", "ue");
    }
}
