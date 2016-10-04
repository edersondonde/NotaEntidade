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
        updatedValues.put(NotaFiscalEntry.COLUMN_EXPORTED, 1);
        //updatedValues.put(NotaFiscalEntry.COLUMN_VALUE, 1.10);

        Cursor notaFiscalCursor = mContext.getContentResolver().query(NotaFiscalEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        notaFiscalCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                NotaFiscalEntry.CONTENT_URI, updatedValues, NotaFiscalEntry._ID + "= ?",
                new String[] { Long.toString(notaFiscalRowId)});
        assertEquals(count, 1);

        //tco.waitForNotificationOrFail();

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


    /*static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertWeatherValues(long locationRowId) {
        long currentTestDate = TestUtilities.TEST_DATE;
        long millisecondsInADay = 1000*60*60*24;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentTestDate+= millisecondsInADay ) {
            ContentValues weatherValues = new ContentValues();
            weatherValues.put(NotaFiscalEntry.COLUMN_LOC_KEY, locationRowId);
            weatherValues.put(NotaFiscalEntry.COLUMN_DATE, currentTestDate);
            weatherValues.put(NotaFiscalEntry.COLUMN_DEGREES, 1.1);
            weatherValues.put(NotaFiscalEntry.COLUMN_HUMIDITY, 1.2 + 0.01 * (float) i);
            weatherValues.put(NotaFiscalEntry.COLUMN_PRESSURE, 1.3 - 0.01 * (float) i);
            weatherValues.put(NotaFiscalEntry.COLUMN_MAX_TEMP, 75 + i);
            weatherValues.put(NotaFiscalEntry.COLUMN_MIN_TEMP, 65 - i);
            weatherValues.put(NotaFiscalEntry.COLUMN_SHORT_DESC, "Asteroids");
            weatherValues.put(NotaFiscalEntry.COLUMN_WIND_SPEED, 5.5 + 0.2 * (float) i);
            weatherValues.put(NotaFiscalEntry.COLUMN_WEATHER_ID, 321);
            returnContentValues[i] = weatherValues;
        }
        return returnContentValues;
    }

    // Student: Uncomment this test after you have completed writing the BulkInsert functionality
    // in your provider.  Note that this test will work with the built-in (default) provider
    // implementation, which just inserts records one-at-a-time, so really do implement the
    // BulkInsert ContentProvider function.
    public void testBulkInsert() {
        // first, let's create a location value
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
        Uri locationUri = mContext.getContentResolver().insert(NotaFiscalEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                NotaFiscalEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating NotaFiscalEntry.",
                cursor, testValues);

        // Now we can bulkInsert some weather.  In fact, we only implement BulkInsert for weather
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertWeatherValues(locationRowId);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(NotaFiscalEntry.CONTENT_URI, true, weatherObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(NotaFiscalEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        weatherObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        cursor = mContext.getContentResolver().query(
                NotaFiscalEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                NotaFiscalEntry.COLUMN_DATE + " ASC"  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating NotaFiscalEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }*/
}
