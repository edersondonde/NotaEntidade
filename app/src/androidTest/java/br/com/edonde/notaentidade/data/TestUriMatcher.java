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

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import br.com.edonde.notaentidade.data.NotaFiscalContract.NotaFiscalEntry;


public class TestUriMatcher extends AndroidTestCase {

    private static final long _ID = 100L;
    private static final Uri TEST_NOTA_FISCAL_DIR = NotaFiscalEntry.CONTENT_URI;
    private static final Uri TEST_NOTA_FISCAL = NotaFiscalEntry.buildNotaFiscalUri(_ID);

    /*
        Students: This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.  Uncomment this when you are
        ready to test your UriMatcher.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = NotaFiscalProvider.buildUriMatcher();

        assertEquals("Error: The Nota Fiscal URI was matched incorrectly.",
                testMatcher.match(TEST_NOTA_FISCAL_DIR), NotaFiscalProvider.NOTA_FISCAL);
        assertEquals("Error: The URI with Nota Fiscal id was matched incorrectly.",
                testMatcher.match(TEST_NOTA_FISCAL), NotaFiscalProvider.NOTA_FISCAL_ITEM);
    }
}
