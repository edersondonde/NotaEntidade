package br.com.edonde.notaentidade.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class with methods to help the exhibition of data in the interface
 */
public class Utility {

    /**
     * Converts a double to the format R$ #.###,##
     * @param value Double value to be converted
     * @return String with the representation of the data
     */
    public static String formatToCurrency (double value) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);

        return "R$ "+format.format(value);
    }

    /**
     * Converts a double to the format ####,##
     * @param value Double value to be converted
     * @return String with the representation of the data
     */
    public static String formatToDecimal (double value) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        format.setGroupingUsed(false);

        return format.format(value);
    }

    /**
     * Formats a string to CPF or CNPJ format.
     * @param cpfcnpj String to be formatted
     * @return String with the CPF or CNPJ formatted, or a message if
     */
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

    /**
     * Format a date in milliseconds to dd/MM/yyyy format
     * @param dateInMillis Date in milliseconds
     * @return String representing the date in the format dd/MM/yyyy
     */
    public static String formatDate(long dateInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(new Date(dateInMillis));
    }
}
