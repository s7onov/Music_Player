package org.hyperskill.musicplayer


import android.R.attr.version
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.openOrCreateDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File


class PlayerDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val appContext = context
    override fun onCreate(db: SQLiteDatabase) {
        val path = appContext.getDir("database", Context.MODE_PRIVATE).path;
        val dbName = path + File.separator + DATABASE_NAME;
        /*myDatabase = */openOrCreateDatabase(dbName,null)
        db.execSQL(DATABASE_CREATE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }
    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "musicPlayerDatabase.db"
        private const val DATABASE_TABLE_NAME = "playlist"
        private const val COLUMN_NAME_PLAYLIST_NAME = "playlistName"
        private const val COLUMN_NAME_SONG_ID = "songId"
        const val DATABASE_CREATE =
            "create table if not exists $DATABASE_TABLE_NAME ( $COLUMN_NAME_PLAYLIST_NAME TEXT," +
                    "$COLUMN_NAME_SONG_ID INTEGER);"
    }

    fun savePlaylist(name: String, list: List<Song>) {
        if (queryPlaylist(name).isNotEmpty()) deletePlaylist(name)
        // Gets the data repository in write mode
        val db = this.writableDatabase
        list.forEach {
            // Create a new map of values, where column names are the keys
            val values = ContentValues().apply {
                put(COLUMN_NAME_PLAYLIST_NAME, name)
                put(COLUMN_NAME_SONG_ID, it.id)
            }
            // Insert the new row, returning the primary key value of the new row
            val newRowId = db?.insert(DATABASE_TABLE_NAME, null, values)
        }
    }

    fun deletePlaylist(name: String) {
        val db = this.writableDatabase
        // Define 'where' part of query.
        val selection = "$COLUMN_NAME_PLAYLIST_NAME = ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(name)
        // Issue SQL statement.
        val deletedRows = db.delete(DATABASE_TABLE_NAME, selection, selectionArgs)
        Log.w("db", "$deletedRows rows deleted")
    }

    fun queryPlaylistNames(): List<String> {
        val db = this.readableDatabase
        val projection = arrayOf(COLUMN_NAME_PLAYLIST_NAME)
        val groupBy = COLUMN_NAME_PLAYLIST_NAME
        val cursor = db.query(
            DATABASE_TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            null,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            groupBy,                   // group the rows
            null,               // filter by row groups
            null               // The sort order
        )
        val list = mutableListOf<String>()
        with(cursor) {
            while (moveToNext()) {
                val item = getString(getColumnIndexOrThrow(COLUMN_NAME_PLAYLIST_NAME))
                list.add(item)
            }
        }
        cursor.close()
        return list.toList()
    }

    fun queryPlaylist(name: String): List<Int> {
        val db = this.readableDatabase
        val projection = arrayOf(COLUMN_NAME_PLAYLIST_NAME, COLUMN_NAME_SONG_ID)
        val selection = "$COLUMN_NAME_PLAYLIST_NAME = ?"
        val selectionArgs = arrayOf(name)
        //val sortOrder = "$COLUMN_NAME_SONG_ID DESC"
        val cursor = db.query(
            DATABASE_TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )
        val list = mutableListOf<Int>()
        with(cursor) {
            while (moveToNext()) {
                val item = getInt(getColumnIndexOrThrow(COLUMN_NAME_SONG_ID))
                list.add(item)
            }
        }
        cursor.close()
        return list
    }
}