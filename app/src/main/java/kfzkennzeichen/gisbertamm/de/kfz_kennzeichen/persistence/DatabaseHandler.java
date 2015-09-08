package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;

    private static final String DATABASE_NAME = "NumberplateCodesManager.sqlite";

    private static final String TABLE_NUMBERPLATE_CODES = "numberplate_codes";
    private static final String TABLE_JOKES = "jokes";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CODE = "code";
    private static final String COLUMN_DISTRICT = "district";
    private static final String COLUMN_DISTRICT_CENTER = "district_center";
    private static final String COLUMN_STATE = "state";
    private static final String COLUMN_DISTRICT_WIKIPEDIA_URL = "district_wikipedia_url";
    public static final String WIKIPEDIA_BASE_URL = "https://de.wikipedia.org";
    private static final List<String[]> data = new ArrayList<String[]>();

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/kfzkennzeichen.gisbertamm.de.kfz_kennzeichen/databases/";

    private SQLiteDatabase dataBase;

    private final Context context;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     *
     * @param context
     */
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        // copy database implicitly when class instance is created for the first time
        createDataBase();
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    public void createDataBase() {

        SQLiteDatabase dbExist = checkDataBase();

        if (dbExist != null) {
            int version = dbExist.getVersion();
            Log.d(this.getClass().getSimpleName(), "EXISTING DATABASE VERSION: " + version);
            dbExist.close();

            if (DATABASE_VERSION > version) {
                // update existing database by overriding it
                copyDataBase();
            }
        } else {

            //By calling this method an empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            copyDataBase();

        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private SQLiteDatabase checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {

            //database does't exist yet.

        }
        return checkDB;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private void copyDataBase() {
        try {
            //Open your local db as the input stream
            InputStream myInput = context.getAssets().open(DATABASE_NAME);

            // Path to the just created empty db
            String outFileName = DB_PATH + DATABASE_NAME;

            Log.d(this.getClass().getSimpleName(),
                    "Copying database from assets/ " + DATABASE_NAME + " to " + outFileName);

            //Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);

            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            //Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy database", e);
        }

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DATABASE_NAME;
        dataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if (dataBase != null)
            dataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // do nothing because database is provided from assets
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing because database is provided from assets
    }

    public SavedEntry searchForCode(String code) {
        try {
            this.openDataBase();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        Cursor cursor = this.dataBase.query(TABLE_NUMBERPLATE_CODES, new String[]{"*"}, COLUMN_CODE + "=?",
                new String[]{String.valueOf(code)}, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        } else {
            Log.e(this.getClass().getSimpleName(), "cursor is null");
            return null;
        }

        SavedEntry savedEntry = null;

        if (cursor.getCount() > 0) {
            savedEntry = new SavedEntry(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5));

            if (!addJokes(code, savedEntry)) return null;
        }

        return savedEntry;
    }

    private boolean addJokes(String code, SavedEntry savedEntry) {
        Cursor cursor2 = this.dataBase.query(TABLE_JOKES, new String[]{"*"}, COLUMN_CODE + "=?",
                new String[]{String.valueOf(code)}, null, null, null, null);

        if (cursor2 != null) {
            cursor2.moveToFirst();
        } else {
            Log.e(this.getClass().getSimpleName(), "cursor2 is null");
            return false;
        }
        if (cursor2.getCount() > 0) {
            while (!cursor2.isAfterLast()) {
                final String joke = cursor2.getString(2);
                savedEntry.setJoke(joke);
                cursor2.moveToNext();
            }
        }
        return true;
    }

    public SavedEntry searchRandom() {
        try {
            this.openDataBase();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        Cursor cursor = this.dataBase.query(TABLE_NUMBERPLATE_CODES + " ORDER BY RANDOM() LIMIT 1",
                new String[]{"*"}, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        } else {
            Log.e(this.getClass().getSimpleName(), "cursor is null");
            return null;
        }

        SavedEntry savedEntry = null;

        if (cursor.getCount() > 0) {
            savedEntry = new SavedEntry(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5));
        }

        if (!addJokes(cursor.getString(1), savedEntry)) return null;

        return savedEntry;
    }
}
