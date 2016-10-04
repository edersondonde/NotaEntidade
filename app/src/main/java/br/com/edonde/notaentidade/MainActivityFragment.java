package br.com.edonde.notaentidade;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Random;

import br.com.edonde.notaentidade.data.NotaFiscalContract;
import br.com.edonde.notaentidade.data.NotaFiscalContract.NotaFiscalEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CUSTOM_REQUEST_QR_SCANNER = 429;
    private static final String TAG = MainActivityFragment.class.getSimpleName();
    private static final int NOTA_FISCAL_LOADER = 0;
    private NotaFiscalAdapter adapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        adapter = new NotaFiscalAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_nfp);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    int idx = cursor.getColumnIndex(NotaFiscalEntry._ID);
                    intent.setData(NotaFiscalEntry.buildNotaFiscalUri(cursor.getLong(idx)));
                    getActivity().startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(NOTA_FISCAL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null && scanResult.getContents() != null) {
            Log.d(TAG, "Scan result: " + scanResult.getContents());

            final NotaFiscal nf = NotaFiscal.parseNF(scanResult.getContents());
            Uri nfUri = NotaFiscalEntry.buildNotaFiscalUri();
            Cursor cursor = getActivity().getContentResolver().query(
                    nfUri,
                    new String[]{NotaFiscalEntry._ID},
                    NotaFiscalEntry.COLUMN_CODE + " = ?",
                    new String[]{nf.getCode()},
                    null);
            if (!cursor.moveToFirst()) {
                Uri result =getActivity().getContentResolver().insert(nfUri, nf.toContentValues());
                Log.d(TAG, "New NF read, total "+ result + " nf read");
            } else {

            }
        } else {
            Log.i(TAG, "No results returned");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_qrcode) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);

            return true;
        } else if (id == R.id.share_csv) {
            return true;
        } //TODO: Deletar depois de testes
        else if (id == R.id.add_random) {
            NotaFiscal nf = new NotaFiscal();
            nf.setCfNf("");
            nf.setCnpj("");
            Random random = new Random();
            StringBuilder code = new StringBuilder();
            for (int i=0; i<44; i++) {
                code.append(random.nextInt(10));
            }
            nf.setCode(code.toString());
            nf.setDate(new Date());
            nf.setExported(0);
            nf.setValidationData("abcdefghijklmnopqrstuvwxyz");
            String value = Utility.formatToCurrency(random.nextDouble()*1000);
            nf.setValue(value);
            getActivity().getContentResolver().insert(NotaFiscalEntry.buildNotaFiscalUri(), nf.toContentValues());
            return true;
        } //TODO: Deletar depois de testes
        else if (id == R.id.delete_all) {
            getActivity().getContentResolver().delete(NotaFiscalEntry.buildNotaFiscalUri(), "1=1", new String[]{});
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = NotaFiscalEntry._ID + " DESC";
        String selection = NotaFiscalEntry.COLUMN_EXPORTED + " = ? ";
        String[] selectionArgs = new String[]{NotaFiscalEntry.NOT_EXPORTED};

        Uri nfUri = NotaFiscalEntry.buildNotaFiscalUri();
        return new CursorLoader(
                getActivity(),
                nfUri,
                null,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor cursor = adapter.getCursor();
        adapter.swapCursor(data);
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Cursor cursor = adapter.getCursor();
        adapter.swapCursor(null);
        if (cursor != null) {
            cursor.close();
        }
    }
}
