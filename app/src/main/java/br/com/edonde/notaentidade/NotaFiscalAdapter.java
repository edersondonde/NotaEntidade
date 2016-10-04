package br.com.edonde.notaentidade;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.edonde.notaentidade.data.NotaFiscalContract;

/**
 * Created by maddo on 24/04/2016.
 */
public class NotaFiscalAdapter extends CursorAdapter {

    public NotaFiscalAdapter (Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_nfp, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView dateText = (TextView) view.findViewById(R.id.list_item_nfp_date);
        int idxDate = cursor.getColumnIndex(NotaFiscalContract.NotaFiscalEntry.COLUMN_VALUE);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        dateText.setText(sdf.format(new Date(cursor.getLong(idxDate))));

        TextView otherText = (TextView) view.findViewById(R.id.list_item_nfp_value);
        otherText.setText(cursorRowToNFString(cursor));
    }

    private String cursorRowToNFString(Cursor cursor) {
        int idxCode = cursor.getColumnIndex(NotaFiscalContract.NotaFiscalEntry.COLUMN_CODE);
        int idxValue = cursor.getColumnIndex(NotaFiscalContract.NotaFiscalEntry.COLUMN_VALUE);
        NumberFormat nf = NumberFormat.getCurrencyInstance();

        String text = nf.format(cursor.getDouble(idxValue));
        return text;
    }
}
