/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.edonde.notaentidade.data;

import android.net.Uri;
import android.test.AndroidTestCase;

import br.com.edonde.notaentidade.data.NotaFiscalContract.NotaFiscalEntry;

public class TestNotaFiscalContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final Long TEST_NOTA_FISCAL_ID = 100L;

    public void testBuildNotaFiscal() {
        Uri notaFiscalUri = NotaFiscalEntry.buildNotaFiscalUri(TEST_NOTA_FISCAL_ID);
        assertNotNull("Failed creating Nota Fiscal Uri",
                notaFiscalUri);
        assertEquals("Failed getting id from Uri.",
                TEST_NOTA_FISCAL_ID.toString(), notaFiscalUri.getLastPathSegment());
        assertEquals("Error: Nota Fiscal Uri doesn't match our expected result",
                notaFiscalUri.toString(),
                "content://br.com.edonde.notaentidade/notafiscal/100");
    }
}
