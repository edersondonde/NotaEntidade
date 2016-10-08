package br.com.edonde.notaentidade;

import org.junit.Test;

import br.com.edonde.notaentidade.utils.Utility;

import static org.junit.Assert.*;

public class UtilityTest {

    @Test
    public void testCurrencyValues () {
        String value = Utility.formatToCurrency(1.01);
        assertEquals("Value is different than expected", "R$ 1,01", value);

        value = Utility.formatToCurrency(1.00009);
        assertEquals("Value is different than expected", "R$ 1,00", value);

        value = Utility.formatToCurrency(1000.88);
        assertEquals("Value is different than expected", "R$ 1.000,88", value);
    }

    @Test
    public void testCnpjFormatter () {
        String cnpj = Utility.formatCpfCnpj("12345678000190");
        assertEquals("CNPJ is different than expected", "12.345.678/0001-90", cnpj);
    }

    @Test
    public void testCpfFormatter () {
        String cpf = Utility.formatCpfCnpj("12345678901");
        assertEquals("Cpf is different than expected", "123.456.789-01", cpf);
    }

    @Test
    public void testEmptyCpfCnpjFormatter () {
        String cpfCnpj = Utility.formatCpfCnpj(null);
        assertEquals("Result is different than expected", "Sem CPF/CNPJ", cpfCnpj);

        cpfCnpj = Utility.formatCpfCnpj("");
        assertEquals("Result is different than expected", "Sem CPF/CNPJ", cpfCnpj);
    }

    @Test
    public void testWrongCpfCnpjFormatter () {
        String cpfCnpj = Utility.formatCpfCnpj("12345");
        assertEquals("Result is different than expected", "12345", cpfCnpj);
    }
}
