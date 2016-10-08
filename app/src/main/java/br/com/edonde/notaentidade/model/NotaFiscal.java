package br.com.edonde.notaentidade.model;

import android.content.ContentValues;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.edonde.notaentidade.ExistingCpfCnpjException;
import br.com.edonde.notaentidade.data.NotaFiscalContract;
import br.com.edonde.notaentidade.utils.Utility;


/**
 * Nota fiscal class representing the Nota Fiscal on the database
 */
public class NotaFiscal {

    private String code;
    private Date date;
    private double value;
    private String cnpj;
    private String validationData;
    private String cfNf;
    private static SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static SimpleDateFormat sdfInput = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * Reads the QrCode data and parses it in a Nota Fiscal item. The QrCode shall be
     * in the format CODE|DATE|VALUE|CPFCNPJ|VALIDATION_DATA.
     * @param qrCodeData String representing the QrCode data read
     * @return The parsed Nota Fiscal item
     * @throws ParseException If the QrCode does not represent a Nota Fiscal item
     * @throws ExistingCpfCnpjException If the Nota Fiscal already has a CPF/CNPJ registered
     */
    public static NotaFiscal parseNF(String qrCodeData) throws ParseException, ExistingCpfCnpjException {
        if (qrCodeData == null || qrCodeData.isEmpty()) {
            throw new ParseException("QrCode data is empty", 0);
        }
        String[] data = qrCodeData.split("\\|");
        if (data.length < 5) {
            throw new ParseException("QrCode data isn't valid", 0);
        }
        if(data[3] != null && !data[3].isEmpty()) {
            throw new ExistingCpfCnpjException("There is an CPF/CNPJ on the NF already.");
        }


        NotaFiscal nf = new NotaFiscal();
        nf.setCode(data[0]);

        try {
            nf.setDate(sdfInput.parse(data[1]));
        } catch (ParseException e) {
            //Ignore the date, and parse today's date
            nf.setDate(new Date());
        }
        nf.setValue(Double.valueOf(data[2]));
        nf.setCnpj("");
        nf.setValidationData(data[4]);
        nf.setCfNf("CFeSAT");

        return nf;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public void setValidationData(String validationData) {
        this.validationData = validationData;
    }

    public void setCfNf(String cfNf) {
        this.cfNf = cfNf;
    }

    @Override
    public String toString() {
        return sdfOutput.format(date)+" "+ Utility.formatToCurrency(value);
    }

    /**
     * Converts the Nota Fiscal item to a ContentValues object, to be used on database operations
     *
     * @return ContentValues representing the Nota Fiscal item
     */
    public ContentValues toContentValues() {
        ContentValues notaFiscalValues = new ContentValues();
        notaFiscalValues.put(NotaFiscalContract.NotaFiscalEntry.COLUMN_CNPJ, cnpj);
        notaFiscalValues.put(NotaFiscalContract.NotaFiscalEntry.COLUMN_CODE, code);
        notaFiscalValues.put(NotaFiscalContract.NotaFiscalEntry.COLUMN_DATE, date.getTime());
        notaFiscalValues.put(NotaFiscalContract.NotaFiscalEntry.COLUMN_VALUE, value);
        notaFiscalValues.put(NotaFiscalContract.NotaFiscalEntry.COLUMN_CF_NF, cfNf);
        notaFiscalValues.put(NotaFiscalContract.NotaFiscalEntry.COLUMN_VALIDATION_DATA, validationData);
        return notaFiscalValues;
    }
}
