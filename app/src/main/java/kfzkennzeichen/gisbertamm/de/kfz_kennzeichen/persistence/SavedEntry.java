package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SavedEntry {
    private int _id;
    private String _code;
    private String _district;
    private String _district_wikipedia_url;
    private String _jokes;

    public SavedEntry() {
    }

    public SavedEntry(int _id, String _code, String _district, String _district_wikipedia_url, String _jokes) {
        this._id = _id;
        this._code = _code;
        this._district = _district;
        this._district_wikipedia_url = _district_wikipedia_url;
        this._jokes = _jokes;
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
        String[] jokes = _jokes.split(";");
        return new ArrayList<String>(Arrays.asList(jokes));
    }

    public void setJokes(String jokes) {
        this._jokes = jokes;
    }

    @Override
    public String toString() {
        return "SavedEntry{" +
                "id=" + _id +
                ", code='" + _code + '\'' +
                ", district='" + _district + '\'' +
                ", district_wikipedia_url='" + _district_wikipedia_url + '\'' +
                ", jokes='" + _jokes + '\'' +
                '}';
    }
}
