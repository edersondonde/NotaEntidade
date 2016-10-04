/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.edonde.notaentidade.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

import br.com.edonde.notaentidade.data.NotaFiscalContract.NotaFiscalEntry;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteTheDatabase() {
        mContext.deleteDatabase(NotaFiscalDbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(NotaFiscalEntry.TABLE_NAME);

        mContext.deleteDatabase(NotaFiscalDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new NotaFiscalDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        assertTrue("Error: Your database was created without the Nota Fiscal entry tables",
                tableNameHashSet.isEmpty());

        c = db.rawQuery("PRAGMA table_info(" + NotaFiscalEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        final HashSet<String> notaFiscalColumnHashSet = new HashSet<>();
        notaFiscalColumnHashSet.add(NotaFiscalEntry._ID);
        notaFiscalColumnHashSet.add(NotaFiscalEntry.COLUMN_CNPJ);
        notaFiscalColumnHashSet.add(NotaFiscalEntry.COLUMN_CODE);
        notaFiscalColumnHashSet.add(NotaFiscalEntry.COLUMN_DATE);
        notaFiscalColumnHashSet.add(NotaFiscalEntry.COLUMN_VALUE);
        notaFiscalColumnHashSet.add(NotaFiscalEntry.COLUMN_EXPORTED);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            notaFiscalColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required Nota Fiscal entry columns",
                notaFiscalColumnHashSet.isEmpty());
        db.close();
    }

    public void testNotaFiscalTable() {

        NotaFiscalDbHelper dbHelper = new NotaFiscalDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues notaFiscalValues = TestUtilities.createNotaFiscalValues();

        long notaFiscalRowId = db.insert(NotaFiscalEntry.TABLE_NAME, null, notaFiscalValues);
        assertTrue(notaFiscalRowId != -1);

        Cursor notaFiscalCursor = db.query(
                NotaFiscalEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertTrue("Error: No Records returned from Nota Fiscal query", notaFiscalCursor.moveToFirst());

        TestUtilities.validateCurrentRecord("testInsertReadDb notaFiscalEntry failed to validate",
                notaFiscalCursor, notaFiscalValues);

        assertFalse("Error: More than one record returned from Nota Fiscal query",
                notaFiscalCursor.moveToNext());

        notaFiscalCursor.close();
        dbHelper.close();
    }
}
