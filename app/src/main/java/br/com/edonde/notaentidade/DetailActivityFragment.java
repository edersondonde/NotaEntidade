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
import br.com.edonde.notaentidade.utils.Utility;

/**
 * DetailActivityFragment contains the interface and methods for creating an interface with the
 * details of a Nota Fiscal item. It loads the item by a LoaderManager
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;

    /**
     * List of columns to be used as projection when loading the cursor
     */
    private static final String[] NOTA_FISCAL_COLUMNS = {
            NotaFiscalEntry._ID,
            NotaFiscalEntry.COLUMN_CNPJ,
            NotaFiscalEntry.COLUMN_VALUE,
            NotaFiscalEntry.COLUMN_DATE,
            NotaFiscalEntry.COLUMN_CODE
    };

    private static final int COL_NF_CNPJ = 1;
    private static final int COL_NF_VALUE = 2;
    private static final int COL_NF_DATE = 3;
    private static final int COL_NF_CODE = 4;

    private TextView mCnpjView;
    private TextView mValueView;
    private TextView mDateView;
    private TextView mCodeView;

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
        //Loading the data based on the uri passed by the intent
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
        //Filling the fields with the data loaded from the cursor
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
