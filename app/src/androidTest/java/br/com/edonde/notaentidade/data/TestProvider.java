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

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import br.com.edonde.notaentidade.data.NotaFiscalContract.NotaFiscalEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                NotaFiscalEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                NotaFiscalEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Nota Fiscal table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                NotaFiscalProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: NotaFiscalProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + NotaFiscalContract.CONTENT_AUTHORITY,
                    NotaFiscalContract.CONTENT_AUTHORITY, providerInfo.authority);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: NotaFiscalProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {
        // content://br.com.edonde.notaentidade/notafiscal/
        String type = mContext.getContentResolver().getType(NotaFiscalEntry.CONTENT_URI);
        // vnd.android.cursor.dir/br.com.edonde.notaentidade/notafiscal
        assertEquals("Error: the NotaFiscalEntry CONTENT_URI should return NotaFiscalEntry.CONTENT_TYPE",
                NotaFiscalEntry.CONTENT_TYPE, type);

        Long testId = 94074L;
        // content://br.com.edonde.notaentidade/notafiscal/94074
        type = mContext.getContentResolver().getType(
                NotaFiscalEntry.buildNotaFiscalUri(testId));
        // vnd.android.cursor.dir/br.com.edonde.notaentidade/notafiscal
        assertEquals("Error: the NotaFiscalEntry CONTENT_URI with id should return NotaFiscalEntry.CONTENT_TYPE",
                NotaFiscalEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testBasicNotaFiscalQuery() {
        NotaFiscalDbHelper dbHelper = new NotaFiscalDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues notaFiscalValues = TestUtilities.createNotaFiscalValues();

        long notaFiscalRowId = db.insert(NotaFiscalEntry.TABLE_NAME, null, notaFiscalValues);
        assertTrue("Unable to Insert NotaFiscalEntry into the Database", notaFiscalRowId != -1);

        db.close();

        Cursor notaFiscalCursor = mContext.getContentResolver().query(
                NotaFiscalEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBasicNotaFiscalQuery", notaFiscalCursor, notaFiscalValues);
    }

    public void testUpdateNotaFiscal() {
        ContentValues values = TestUtilities.createNotaFiscalValues();

        Uri notaFiscalUri = mContext.getContentResolver().
                insert(NotaFiscalEntry.CONTENT_URI, values);
        long notaFiscalRowId = ContentUris.parseId(notaFiscalUri);

        assertTrue(notaFiscalRowId != -1);
        Log.d(LOG_TAG, "New row id: " + notaFiscalRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(NotaFiscalEntry._ID, notaFiscalRowId);

        Cursor notaFiscalCursor = mContext.getContentResolver().query(NotaFiscalEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        notaFiscalCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                NotaFiscalEntry.CONTENT_URI, updatedValues, NotaFiscalEntry._ID + "= ?",
                new String[] { Long.toString(notaFiscalRowId)});
        assertEquals(count, 1);

        notaFiscalCursor.unregisterContentObserver(tco);
        notaFiscalCursor.close();

        Cursor cursor = mContext.getContentResolver().query(
                NotaFiscalEntry.CONTENT_URI,
                null,
                null, //NotaFiscalEntry._ID + " = " + notaFiscalRowId,
                null,
                null
        );


        TestUtilities.validateCursor("testUpdateNotaFiscal.  Error validating Nota Fiscal entry update.",
                cursor, updatedValues);

        cursor.close();
    }


    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createNotaFiscalValues();

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(NotaFiscalEntry.CONTENT_URI, true, tco);
        Uri notaFiscalUri = mContext.getContentResolver().insert(NotaFiscalEntry.CONTENT_URI, testValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long notaFiscalRowId = ContentUris.parseId(notaFiscalUri);

        assertTrue(notaFiscalRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                NotaFiscalEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating NotaFiscalEntry.",
                cursor, testValues);
    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        TestUtilities.TestContentObserver notaFiscalObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(NotaFiscalEntry.CONTENT_URI, true, notaFiscalObserver);

        deleteAllRecordsFromProvider();

        notaFiscalObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(notaFiscalObserver);
    }
}
