package br.com.edonde.notaentidade.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by maddo on 02/01/2016.
 */
public class NotaFiscalContract {

    public static final String CONTENT_AUTHORITY = "br.com.edonde.notaentidade";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);

    public static final String PATH_NOTA_FISCAL = "notafiscal";


    public static final class NotaFiscalEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTA_FISCAL).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTA_FISCAL;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTA_FISCAL;

        public static final String TABLE_NAME = "notafiscal";

        public static final String COLUMN_CNPJ = "cnpj";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_CF_NF = "cf_nf";
        public static final String COLUMN_VALIDATION_DATA = "validation_data";
        public static final String COLUMN_EXPORTED = "exported";
        public static final String NOT_EXPORTED = "0";
        public static final String EXPORTED = "1";

        public static String getNotaFiscalIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildNotaFiscalUri() {
            return CONTENT_URI;
        }

        public static Uri buildNotaFiscalUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
