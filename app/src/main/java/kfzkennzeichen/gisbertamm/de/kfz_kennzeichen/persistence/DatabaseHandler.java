package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "NumberplateCodesManager";

    private static final String TABLE_NUMBERPLATE_CODES = "numberplate_codes";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CODE = "code";
    private static final String COLUMN_DISTRICT = "district";
    private static final String COLUMN_DISTRICT_CENTER = "district_center";
    private static final String COLUMN_STATE = "state";
    private static final String COLUMN_DISTRICT_WIKIPEDIA_URL = "district_wikipedia_url";
    private static final String COLUMN_JOKES = "jokes";
    public static final String WIKIPEDIA_BASE_URL = "http://de.wikipedia.org/wiki/";

    private List<String[]> data = new ArrayList<String[]>();

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO create database with DBUnit and install the app with the readily prepared database file
        data.add(new String[]{"A", "Landkreis Augsburg", "Augsburg", "Bayern", WIKIPEDIA_BASE_URL + "Augsburg", "keine Sprüche bekannt"});
        data.add(new String[]{"AA", "Ostalbkreis", "Aalen", "Baden-Württemberg", WIKIPEDIA_BASE_URL + "Aalen", "Alle Achtung;Alles Arschlöcher"});
        // data.add(new String[]{"", "", WIKIPEDIA_BASE_URL + "", ""});
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NUMBERPLATES_CODE_TABLE = "CREATE TABLE " + TABLE_NUMBERPLATE_CODES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_CODE + " TEXT,"
                + COLUMN_DISTRICT + " TEXT," + COLUMN_DISTRICT_CENTER + " TEXT," + COLUMN_STATE + " TEXT,"
                + COLUMN_DISTRICT_WIKIPEDIA_URL + " TEXT," + COLUMN_JOKES + " TEXT" + ")";
        db.execSQL(CREATE_NUMBERPLATES_CODE_TABLE);

        // TODO create database with DBUnit and install the app with the readily prepared database file
        insertData(db);
    }

    private void insertData(SQLiteDatabase db) {
        for (String[] entry : this.data) {
            ContentValues values = new ContentValues();

            values.put(COLUMN_CODE, entry[0]);
            values.put(COLUMN_DISTRICT, entry[1]);
            values.put(COLUMN_DISTRICT_CENTER, entry[2]);
            values.put(COLUMN_STATE, entry[3]);
            values.put(COLUMN_DISTRICT_WIKIPEDIA_URL, entry[4]);
            values.put(COLUMN_JOKES, entry[5]);

            db.insert(TABLE_NUMBERPLATE_CODES, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NUMBERPLATE_CODES);

        onCreate(db);
    }

    public SavedEntry searchForCode(String code) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NUMBERPLATE_CODES, new String[]{COLUMN_ID,
                        COLUMN_CODE, COLUMN_DISTRICT, COLUMN_DISTRICT_CENTER, COLUMN_STATE,
                        COLUMN_DISTRICT_WIKIPEDIA_URL, COLUMN_JOKES}, COLUMN_CODE + "=?",
                new String[]{String.valueOf(code)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        SavedEntry savedEntry = null;

        if (cursor.getCount() > 0) {
            savedEntry = new SavedEntry(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5), cursor.getString(6));
        }

        return savedEntry;
    }
}
