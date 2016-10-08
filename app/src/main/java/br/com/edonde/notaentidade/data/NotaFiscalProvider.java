package br.com.edonde.notaentidade.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import br.com.edonde.notaentidade.data.NotaFiscalContract.NotaFiscalEntry;


/**
 * Provider class for the Nota Fiscal database
 */
public class NotaFiscalProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = buildUriMatcher();
    static final int NOTA_FISCAL = 100;
    static final int NOTA_FISCAL_ITEM = 101;
    private NotaFiscalDbHelper openHelper;

    /**
     * Selection for searching by id
     */
    private static final String notaFiscalById =
            NotaFiscalEntry.TABLE_NAME + "." + NotaFiscalEntry._ID + " = ? ";

    @Override
    public boolean onCreate() {
        openHelper = new NotaFiscalDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (uriMatcher.match(uri)) {
            case NOTA_FISCAL:
                retCursor = getNotaFiscal(selection, selectionArgs, projection, sortOrder);
                break;
            case NOTA_FISCAL_ITEM:
                retCursor = getNotaFiscalById(uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /**
     * Queries the database for a Nota Fiscal item
     * @param uri Uri containing the reference to the Nota Fiscal item
     * @param projection List of columns to be selected
     * @param sortOrder Order of the selectoin
     * @return Cursor with the query result
     */
    private Cursor getNotaFiscalById(Uri uri, String[] projection, String sortOrder) {
        String selection = notaFiscalById;
        String id = NotaFiscalEntry.getNotaFiscalIdFromUri(uri);
        String[] selectionArgs = new String[]{id};

        return openHelper.getReadableDatabase().query(
                NotaFiscalEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /**
     * Generic query of the database
     * @param selection Selection string
     * @param selectionArgs List of arguments for the selection
     * @param projection List of columns of the projection
     * @param sortOrder Sort order
     * @return Cursor with the result
     */
    private Cursor getNotaFiscal(String selection, String[] selectionArgs, String[] projection, String sortOrder) {

        return openHelper.getReadableDatabase().query(
                NotaFiscalEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case NOTA_FISCAL:
                return NotaFiscalEntry.CONTENT_TYPE;
            case NOTA_FISCAL_ITEM:
                return NotaFiscalEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case NOTA_FISCAL: {
                long _id = db.insert(NotaFiscalEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = NotaFiscalEntry.buildNotaFiscalUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted;

        if (selection == null) {
            selection = "1";
        }

        switch (match) {
            case NOTA_FISCAL:
                rowsDeleted = db.delete(
                        NotaFiscalEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case NOTA_FISCAL:
                rowsUpdated = db.update(NotaFiscalEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    /**
     * Uri matcher builder, accepts uril like content://br.com.edonde.notaentidade/notafiscal
     *  and content://br.com.edonde.notaentidade/notafiscal/#
     *
     * @return UriMatcher object
     */
    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NotaFiscalContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, NotaFiscalContract.PATH_NOTA_FISCAL, NOTA_FISCAL);
        matcher.addURI(authority, NotaFiscalContract.PATH_NOTA_FISCAL + "/#", NOTA_FISCAL_ITEM);
        return matcher;
    }
}
