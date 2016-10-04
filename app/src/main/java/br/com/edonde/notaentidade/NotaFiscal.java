package br.com.edonde.notaentidade;

import android.content.ContentValues;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.edonde.notaentidade.data.NotaFiscalContract;

/**
 * Created by maddo on 20/11/2015.
 */
public class NotaFiscal {

    private String code;
    private Date date;
    private String value;
    private String cnpj;
    private String validationData;
    private String cfNf;
    private int exported;
    private static SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static SimpleDateFormat sdfInput = new SimpleDateFormat("yyyyMMddHHmmss");
    private static NumberFormat nf = NumberFormat.getCurrencyInstance();


    public static NotaFiscal parseNF(String qrCodeData) {
        String[] data = qrCodeData.split("\\|");

        NotaFiscal nf = new NotaFiscal();
        nf.setCode(data[0]);

        try {
            nf.setDate(sdfInput.parse(data[1]));
        } catch (ParseException e) {
            //Ignore the date, and parse today's date
            nf.setDate(new Date());
        }
        nf.setValue(data[2]);
        nf.setCnpj(data[3]);
        nf.setValidationData(data[4]);

        return nf;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setValidationData(String validationData) {
        this.validationData = validationData;
    }

    public String getValidationData() {
        return validationData;
    }

    public String getCfNf() {
        return cfNf;
    }

    public void setCfNf(String cfNf) {
        this.cfNf = cfNf;
    }

    public int getExported() {
        return exported;
    }

    public void setExported(int exported) {
        this.exported = exported;
    }

    @Override
    public String toString() {
        return sdfOutput.format(date)+" "+nf.format(Double.valueOf(value));
    }

    public ContentValues toContentValues() {
        ContentValues notaFiscalValues = new ContentValues();
        notaFiscalValues.put(NotaFiscalContract.NotaFiscalEntry.COLUMN_CODE, code);
        notaFiscalValues.put(NotaFiscalContract.NotaFiscalEntry.COLUMN_DATE, date.getTime());
        notaFiscalValues.put(NotaFiscalContract.NotaFiscalEntry.COLUMN_VALUE, value);
        notaFiscalValues.put(NotaFiscalContract.NotaFiscalEntry.COLUMN_CF_NF, cfNf);
        notaFiscalValues.put(NotaFiscalContract.NotaFiscalEntry.COLUMN_EXPORTED, exported);
        return notaFiscalValues;
    }
}
