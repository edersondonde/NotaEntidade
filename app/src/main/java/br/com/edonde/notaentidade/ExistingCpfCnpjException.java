package br.com.edonde.notaentidade;

/**
 * Expection to be thrown when a Nota Fiscal read already has CPF/CNPJ registered
 */
public class ExistingCpfCnpjException extends Exception {

    /**
     * Basic constructor with the error message
     * @param message Error message
     */
    public ExistingCpfCnpjException(String message) {
        super(message);
    }
}
