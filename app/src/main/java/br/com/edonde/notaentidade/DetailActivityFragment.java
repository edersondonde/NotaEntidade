package br.com.edonde.notaentidade;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.edonde.notaentidade.data.NotaFiscalContract.NotaFiscalEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;

    private static final String[] NOTA_FISCAL_COLUMNS = {
            NotaFiscalEntry._ID,
            NotaFiscalEntry.COLUMN_CNPJ,
            NotaFiscalEntry.COLUMN_VALUE,
            NotaFiscalEntry.COLUMN_DATE,
            NotaFiscalEntry.COLUMN_CODE
    };

    public static final int COL_NF_CNPJ = 1;
    public static final int COL_NF_VALUE = 2;
    public static final int COL_NF_DATE = 3;
    public static final int COL_NF_CODE = 4;

    private TextView mCnpjView;
    private TextView mValueView;
    private TextView mDateView;
    private TextView mCodeView;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mCnpjView = (TextView) rootView.findViewById(R.id.detail_cnpj_value);
        mValueView = (TextView) rootView.findViewById(R.id.detail_value_value);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_value);
        mCodeView = (TextView) rootView.findViewById(R.id.detail_code_value);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }
        Uri uri = intent.getData();
        Log.d(LOG_TAG, "Detail uri: "+uri.toString());
        return new CursorLoader(
                getActivity(),
                uri,
                NOTA_FISCAL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null && data.moveToFirst()) {
            String cnpj = data.getString(COL_NF_CNPJ);
            cnpj = Utility.formatCpfCnpj(cnpj);
            mCnpjView.setText(cnpj);

            double value = data.getDouble(COL_NF_VALUE);
            mValueView.setText(Utility.formatToCurrency(value));

            long date = data.getLong(COL_NF_DATE);
            mDateView.setText(Utility.formatDate(date));

            String code = data.getString(COL_NF_CODE);
            mCodeView.setText(code);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
