package br.com.edonde.notaentidade.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import br.com.edonde.notaentidade.data.NotaFiscalContract.NotaFiscalEntry;

/**
 * DbHelper class for the Nota Entidade database
 */
public class NotaFiscalDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    /**
     * File Name of the database
     */
    static final String DATABASE_NAME = "notafiscal.db";

    public NotaFiscalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_NOTAFISCAL_TABLE = "CREATE TABLE " + NotaFiscalEntry.TABLE_NAME + " (" +
                NotaFiscalEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                NotaFiscalEntry.COLUMN_CNPJ + " TEXT, " +
                NotaFiscalEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                NotaFiscalEntry.COLUMN_CODE + " TEXT NOT NULL, " +
                NotaFiscalEntry.COLUMN_VALUE + " REAL NOT NULL, " +
                NotaFiscalEntry.COLUMN_CF_NF + " TEXT, " +
                NotaFiscalEntry.COLUMN_VALIDATION_DATA + " TEXT, " +
                "UNIQUE (" + NotaFiscalEntry.COLUMN_CODE + ") " +
                "ON CONFLICT REPLACE" +
                ");";
        db.execSQL(SQL_CREATE_NOTAFISCAL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NotaFiscalEntry.TABLE_NAME);
        onCreate(db);
    }
}
