package br.com.edonde.notaentidade;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import br.com.edonde.notaentidade.utils.Utility;

/**
 * Cursor Adapter with nota Fiscal items to be exhibited in a ListView
 */
public class NotaFiscalAdapter extends CursorAdapter {

    public NotaFiscalAdapter (Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_nfp, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.dateView.setText(
                Utility.formatDate(cursor.getLong(MainActivityFragment.COL_NF_DATE)));

        holder.valueView.setText(
                Utility.formatToCurrency(cursor.getDouble(MainActivityFragment.COL_NF_VALUE)));
    }

    /**
     * Holder of the views in the ListViewItem
     */
    private static class ViewHolder {
        public final TextView dateView;
        public final TextView valueView;

        public ViewHolder(View view) {
            dateView = (TextView) view.findViewById(R.id.list_item_nfp_date);
            valueView = (TextView) view.findViewById(R.id.list_item_nfp_value);
        }
    }

}
