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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;

import br.com.edonde.notaentidade.data.NotaFiscalContract.NotaFiscalEntry;
import br.com.edonde.notaentidade.model.NotaFiscal;
import br.com.edonde.notaentidade.utils.Utility;


/**
 * MainActivityFragment contains the interface and methods for reading,
 * exhibiting and sharing the Nota Fiscal items
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private static final String TAG = MainActivityFragment.class.getSimpleName();
    private static final int NOTA_FISCAL_LOADER = 0;
    private static final int SHARE_REQUEST_CODE = 429;
    private NotaFiscalAdapter adapter;

    /**
     * Columns used as projection for reading and exhibiting
     * the Nota Fiscal items on the screen
     */
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

        registerForContextMenu(listView);

        //On item click, opens the Detail Activity exhibiting the Nota Fiscal relative to it
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    final long notaFiscalId = cursor.getLong(COL_NF_ID);
                    startDetailActivity(notaFiscalId);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_command, menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        //Deletes the Nota Fiscal selected
        if (item.getItemId() == R.id.action_delete) {
            Cursor c = (Cursor)adapter.getItem(info.position);
            deleteNotaFiscal(
                    NotaFiscalEntry._ID + " = ?",
                    new String[]{Long.toString(c.getLong(COL_NF_ID))});
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(NOTA_FISCAL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "Activity result on fragment: "+ requestCode+" "+resultCode);
        //After sharing the Nota Fiscal items, deletes all of them
        if (requestCode == SHARE_REQUEST_CODE) {
            int deleted = deleteNotaFiscal(null, null);
            Log.d(LOG_TAG, "Deleted "+deleted+" items");
        }
        //After reading the QrCode, parses it and stores on the Database
        else if (requestCode == IntentIntegrator.REQUEST_CODE){
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (scanResult != null && scanResult.getContents() != null) {
                Log.d(TAG, "Scan result: " + scanResult.getContents());

                final NotaFiscal nf;
                try {
                    nf = NotaFiscal.parseNF(scanResult.getContents());
                    Uri nfUri = NotaFiscalEntry.buildNotaFiscalUri();
                    Uri result = getActivity().getContentResolver().insert(nfUri, nf.toContentValues());
                    Log.d(TAG, "New NF read, total " + result + " nf read");
                } catch (ParseException e) {
                    //Exhibits a toast to the user if reading data was unsucessful
                    Toast.makeText(getActivity(), R.string.error_wrong_nf, Toast.LENGTH_LONG).show();
                } catch (ExistingCpfCnpjException e) {
                    //Exhibits a toast to the user if the
                    // Nota Fiscal read already has CPF/CNPJ registered
                    Toast.makeText(getActivity(), R.string.error_existing_cpf_cnpj, Toast.LENGTH_LONG).show();
                }
            } else {
                Log.i(TAG, "No results returned");
            }
        }
    }

    /**
     * Deletes the Nota Fiscal items that matches the selection.
     * If selection and selection args are null, deletes all items
     * @param selection Selection string
     * @param selectionArgs List of arguments
     * @return The number of items deleted
     */
    public int deleteNotaFiscal(String selection, String[] selectionArgs) {
        return getActivity().getContentResolver().delete(
                NotaFiscalEntry.buildNotaFiscalUri(),
                selection,
                selectionArgs);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Starts the QrCode intent. Opens an option to
        // install Barcode Scanner if it is not installed
        if (id == R.id.add_qrcode) {
            startQrCodeIntent();
            return true;
        }
        //Starts the share activity
        else if (id == R.id.action_share) {
            Intent intent = createShareIntent(generateCsv());
            startActivityForResult(intent, SHARE_REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
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

    /**
     * Creates the share intent with all the options and the csv file
     * @param fileUri File Uri of the csv file
     * @return Share intent
     */
    @NonNull
    private Intent createShareIntent(Uri fileUri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body));
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    /**
     * Creates the QrCode intent and initiates it.
     */
    public void startQrCodeIntent() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setTitleByID(R.string.zxing_title);
        integrator.setMessageByID(R.string.zxing_message);
        integrator.setButtonYesByID(R.string.zxing_yes);
        integrator.setButtonNoByID(R.string.zxing_no);
        integrator.addExtra("RESULT_DISPLAY_DURATION_MS", 500L);
        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    /**
     * Creates and starts the intent for the Detail Activity
     * @param notaFiscalId The id of the Nota Fiscal item to the Detail Activity
     */
    public void startDetailActivity(long notaFiscalId) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.setData(NotaFiscalEntry.buildNotaFiscalUri(notaFiscalId));
        getActivity().startActivity(intent);
    }

    /**
     * Reads the database and fills a csv file with the data.
     * The data is passed in the sequence CNPJ, Data, Codigo, Valor, CFouNF.
     * Each Nota Fiscal item is a line of the csv file.
     * Creating a new file will override the old file
     *
     * @return The uri of the file created.
     */
    private Uri generateCsv() {
        Uri fileUri = null;
        try {
            String fileName = "NotasCsv.csv";
            File csvDir = new File (getContext().getFilesDir(), "csv");
            File csvFile = new File(csvDir, fileName);
            if (!csvDir.exists())
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
            if (cursor != null && cursor.moveToFirst()) {
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
                cursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileUri;
    }


}
