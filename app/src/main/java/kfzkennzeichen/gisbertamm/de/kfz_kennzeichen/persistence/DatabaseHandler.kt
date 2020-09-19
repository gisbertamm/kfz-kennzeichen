package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.sql.SQLException
import java.util.*

class DatabaseHandler(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private var dataBase: SQLiteDatabase? = null

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    fun createDataBase() {
        val dbExist = checkDataBase()
        if (dbExist != null) {
            val version = dbExist.version
            Log.d(this.javaClass.simpleName, "EXISTING DATABASE VERSION: $version")
            dbExist.close()
            if (DATABASE_VERSION > version) {
                // update existing database by overriding it
                copyDataBase()
            }
        } else {

            //By calling this method an empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.readableDatabase
            copyDataBase()
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private fun checkDataBase(): SQLiteDatabase? {
        var checkDB: SQLiteDatabase? = null
        try {
            checkDB = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null)
        } catch (e: SQLiteException) {

            //database does't exist yet.
        }
        return checkDB
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private fun copyDataBase() {
        try {
            //Open your local db as the input stream
            val myInput = context.assets.open(DATABASE_NAME)

            // Path to the just created empty db
            val outFileName = context.getDatabasePath(DATABASE_NAME).path
            Log.d(this.javaClass.simpleName,
                    "Copying database from assets/ " + DATABASE_NAME + " to " + outFileName)

            //Open the empty db as the output stream
            val myOutput: OutputStream = FileOutputStream(outFileName)

            //transfer bytes from the inputfile to the outputfile
            val buffer = ByteArray(1024)
            var length: Int
            while (myInput.read(buffer).also { length = it } > 0) {
                myOutput.write(buffer, 0, length)
            }

            //Close the streams
            myOutput.flush()
            myOutput.close()
            myInput.close()
        } catch (e: IOException) {
            throw RuntimeException("Cannot copy database", e)
        }
    }

    @Throws(SQLException::class)
    fun openDataBase() {

        //Open the database
        dataBase = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null)
    }

    @Synchronized
    override fun close() {
        if (dataBase != null) dataBase!!.close()
        super.close()
    }

    override fun onCreate(db: SQLiteDatabase) {
        // do nothing because database is provided from assets
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // do nothing because database is provided from assets
    }

    fun searchForCode(code: String): SavedEntry? {
        try {
            openDataBase()
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
        val cursor = dataBase!!.query(TABLE_NUMBERPLATE_CODES, arrayOf("*"), COLUMN_CODE + "=?", arrayOf(code), null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
        } else {
            Log.e(this.javaClass.simpleName, "cursor is null")
            return null
        }
        var savedEntry: SavedEntry? = null
        if (cursor.count > 0) {
            savedEntry = SavedEntry(cursor.getString(0).toInt(),
                    cursor.getString(1), cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5))
            if (!addJokes(code, savedEntry)) return null
        }
        return savedEntry
    }

    private fun addJokes(code: String, savedEntry: SavedEntry?): Boolean {
        val cursor = dataBase!!.query(TABLE_JOKES, arrayOf("*"), COLUMN_CODE + "=?", arrayOf(code), null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
        } else {
            Log.e(this.javaClass.simpleName, "cursor is null")
            return false
        }
        if (cursor.count > 0) {
            while (!cursor.isAfterLast) {
                val joke = cursor.getString(2)
                savedEntry!!.setJoke(joke)
                cursor.moveToNext()
            }
        }
        return true
    }

    fun searchRandom(): SavedEntry? {
        try {
            openDataBase()
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
        val cursor = dataBase!!.query(TABLE_NUMBERPLATE_CODES + " ORDER BY RANDOM() LIMIT 1", arrayOf("*"), null, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
        } else {
            Log.e(this.javaClass.simpleName, "cursor is null")
            return null
        }
        var savedEntry: SavedEntry? = null
        if (cursor.count > 0) {
            savedEntry = SavedEntry(cursor.getString(0).toInt(),
                    cursor.getString(1), cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5))
        }
        return if (!addJokes(cursor.getString(1), savedEntry)) null else savedEntry
    }

    fun createStatistics() {
        try {
            openDataBase()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        val cursor = dataBase!!.query(TABLE_JOKES, arrayOf("COUNT(*)"), "$COLUMN_JOKES like ?", arrayOf("%Esel%"), null, null, null, null)
        cursor?.moveToFirst() ?: Log.e(this.javaClass.simpleName, "cursor is null")
        if (cursor!!.count > 0) {
            while (!cursor.isAfterLast) {
                val code = cursor.getString(0)
                Log.d(TAG, "Number of Esel is $code")
                cursor.moveToNext()
            }
        }
    }

    companion object {
        const val WIKIPEDIA_BASE_URL = "https://de.wikipedia.org"
        const val TAG = "DatabaseHandler"
        private const val DATABASE_VERSION = 9
        private const val DATABASE_NAME = "NumberplateCodesManager.sqlite"
        private const val TABLE_NUMBERPLATE_CODES = "numberplate_codes"
        private const val TABLE_JOKES = "jokes"
        private const val COLUMN_JOKES = "jokes"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_CODE = "code"
        private const val COLUMN_DISTRICT = "district"
        private const val COLUMN_DISTRICT_CENTER = "district_center"
        private const val COLUMN_STATE = "state"
        private const val COLUMN_DISTRICT_WIKIPEDIA_URL = "district_wikipedia_url"
        private val data: List<Array<String>> = ArrayList()
    }

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     *
     * @param context
     */
    init {
        // copy database implicitly when class instance is created for the first time
        createDataBase()
    }
}