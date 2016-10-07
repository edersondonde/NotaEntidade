package br.com.edonde.notaentidade;

import android.support.annotation.NonNull;
import android.util.Log;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by maddo on 04/10/2016.
 */

public class Utility {

    public static String formatToCurrency (double value) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);

        return "R$ "+format.format(value);
    }

    public static String formatToDecimal (double value) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        format.setGroupingUsed(false);

        return format.format(value);
    }

    @NonNull
    public static String formatCpfCnpj(String cpfcnpj) {
        if (cpfcnpj != null && !cpfcnpj.equals("")) {
            if (cpfcnpj.length() == 11) {//cpf
                StringBuilder builder = new StringBuilder();
                builder.append(cpfcnpj.substring(0, 3))
                        .append(".")
                        .append(cpfcnpj.substring(3,6))
                        .append(".")
                        .append(cpfcnpj.substring(6,9))
                        .append("-")
                        .append(cpfcnpj.substring(9));
                cpfcnpj = builder.toString();
            } else if (cpfcnpj.length() == 14) { //cnpj
                StringBuilder builder = new StringBuilder();
                builder.append(cpfcnpj.substring(0, 2))
                        .append(".")
                        .append(cpfcnpj.substring(2,5))
                        .append(".")
                        .append(cpfcnpj.substring(5,8))
                        .append("/")
                        .append(cpfcnpj.substring(8,12))
                        .append("-")
                        .append(cpfcnpj.substring(12));
                cpfcnpj = builder.toString();
            }
        } else {
            cpfcnpj = "Sem CPF/CNPJ";
        }
        return cpfcnpj;
    }

    public static String formatDate(long dateInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(new Date(dateInMillis));
    }
}
