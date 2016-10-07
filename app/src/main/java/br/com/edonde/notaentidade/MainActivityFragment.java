package br.com.edonde.notaentidade;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import br.com.edonde.notaentidade.data.NotaFiscalContract.NotaFiscalEntry;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private static final String TAG = MainActivityFragment.class.getSimpleName();
    private static final int NOTA_FISCAL_LOADER = 0;
    private static final int SHARE_REQUEST_CODE = 429;
    private NotaFiscalAdapter adapter;

    private static final String[] NOTA_FISCAL_COLUMNS = {
            NotaFiscalEntry._ID,
            NotaFiscalEntry.COLUMN_VALUE,
            NotaFiscalEntry.COLUMN_DATE,
            NotaFiscalEntry.COLUMN_CODE,
            NotaFiscalEntry.COLUMN_CF_NF
    };

    public static final int COL_NF_ID = 0;
    public static final int COL_NF_VALUE = 1;
    public static final int COL_NF_DATE = 2;
    public static final int COL_NF_CODE = 3;
    public static final int COL_CF_NF = 4;

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
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.setData(NotaFiscalEntry.buildNotaFiscalUri(cursor.getLong(COL_NF_ID)));
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
        Log.d(LOG_TAG, "Activity result on fragment: "+ requestCode+" "+resultCode);
        if (requestCode == SHARE_REQUEST_CODE) {
            int deleted = getActivity().getContentResolver().delete(
                    NotaFiscalEntry.buildNotaFiscalUri(),
                    null,
                    null);
            Log.d(LOG_TAG, "Deleted "+deleted+" items");
        } else if (requestCode == IntentIntegrator.REQUEST_CODE){
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (scanResult != null && scanResult.getContents() != null) {
                Log.d(TAG, "Scan result: " + scanResult.getContents());

                final NotaFiscal nf = NotaFiscal.parseNF(scanResult.getContents());
                Uri nfUri = NotaFiscalEntry.buildNotaFiscalUri();
                Uri result = getActivity().getContentResolver().insert(nfUri, nf.toContentValues());
                Log.d(TAG, "New NF read, total " + result + " nf read");

            } else {
                Log.i(TAG, "No results returned");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_qrcode) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setTitleByID(R.string.zxing_title);
            integrator.setMessageByID(R.string.zxing_message);
            integrator.setButtonYesByID(R.string.zxing_yes);
            integrator.setButtonNoByID(R.string.zxing_no);
            integrator.addExtra("RESULT_DISPLAY_DURATION_MS", 500L);
            integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);

            return true;
        } else if (id == R.id.action_share) {
            Intent intent = createShareIntent(generateCsv());
            startActivityForResult(intent, SHARE_REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private Intent createShareIntent(Uri fileUri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Notas Fiscais lidas pelo Nota Entidade");
        intent.putExtra(Intent.EXTRA_TEXT, "Nota Entidade");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = NotaFiscalEntry._ID + " ASC";

        Uri nfUri = NotaFiscalEntry.buildNotaFiscalUri();
        return new CursorLoader(
                getActivity(),
                nfUri,
                NOTA_FISCAL_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Cursor cursor = adapter.getCursor();
        adapter.swapCursor(null);
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor cursor = adapter.getCursor();
        adapter.swapCursor(data);
        if (cursor != null) {
            cursor.close();
        }
    }

    private Uri generateCsv() {
        Uri fileUri = null;
        try {
            String fileName = "NotasCsv.csv";
            File csvDir = new File (getContext().getFilesDir(), "csv");
            File csvFile = new File(csvDir, fileName);
            csvDir.mkdir();

            Log.d(LOG_TAG, "Writing to output");
            final FileOutputStream out = new FileOutputStream(csvFile);
            OutputStreamWriter osw = new OutputStreamWriter(out);

            Cursor cursor = getActivity().getContentResolver().query(
                    NotaFiscalEntry.buildNotaFiscalUri(),
                    NOTA_FISCAL_COLUMNS,
                    null,
                    null,
                    NotaFiscalEntry._ID + " ASC");
            if (cursor.moveToFirst()) {
                osw.write("\"CNPJ\",\"Data\",\"Codigo\",\"Valor\",\"CFouNF\"\n");
                do {
                    osw.write(",");
                    osw.write("\"");
                    osw.write(Utility.formatDate(cursor.getLong(COL_NF_DATE)));
                    osw.write("\"");
                    osw.write(",");
                    osw.write("\"");
                    osw.write(cursor.getString(COL_NF_CODE));
                    osw.write("\"");
                    osw.write(",");
                    osw.write("\"");
                    osw.write(Utility.formatToDecimal(cursor.getDouble(COL_NF_VALUE)));
                    osw.write("\"");
                    osw.write(",");
                    osw.write("\"");
                    osw.write(cursor.getString(COL_CF_NF));
                    osw.write("\"");
                    osw.write("\n");
                } while (cursor.moveToNext());

                osw.flush();
                osw.close();
                out.close();

                fileUri = FileProvider.getUriForFile(getActivity(), "br.com.edonde.notaentidade.fileprovider", csvFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileUri;
    }


}
