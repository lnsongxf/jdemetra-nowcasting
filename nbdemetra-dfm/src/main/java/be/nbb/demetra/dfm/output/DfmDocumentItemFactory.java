/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package be.nbb.demetra.dfm.output;

import com.google.common.base.Optional;
import ec.tss.Dfm.DfmDocument;
import ec.tss.Dfm.DfmResults;
import ec.tstoolkit.utilities.DefaultInformationExtractor;
import ec.tstoolkit.utilities.Id;
import ec.tstoolkit.utilities.LinearId;
import ec.ui.view.tsprocessing.ComposedProcDocumentItemFactory;
import ec.ui.view.tsprocessing.DefaultItemUI;
import ec.ui.view.tsprocessing.IProcDocumentView;
import ec.ui.view.tsprocessing.ItemUI;
import ec.ui.view.tsprocessing.ProcDocumentItemFactory;
import javax.swing.JComponent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
abstract class DfmDocumentItemFactory<I> extends ComposedProcDocumentItemFactory<DfmDocument, Optional<DfmResults>> {

    protected DfmDocumentItemFactory(Id itemId, ItemUI<? extends IProcDocumentView<DfmDocument>, Optional<DfmResults>> itemUI) {
        super(DfmDocument.class, itemId, Extractor.INSTANCE, itemUI);
    }

    private static final class Extractor extends DefaultInformationExtractor<DfmDocument, Optional<DfmResults>> {

        public static final Extractor INSTANCE = new Extractor();

        @Override
        public Optional<DfmResults> retrieve(DfmDocument source) {
            return Optional.fromNullable(source.getResults().get("dfm", DfmResults.class));
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class)
    public static class ShocksDecompositionItemFactory extends DfmDocumentItemFactory<DfmResults> {

        public static final Id ID = new LinearId("ShocksDecomposition");

        public ShocksDecompositionItemFactory() {
            super(ID, new DefaultItemUI<IProcDocumentView<DfmDocument>, Optional<DfmResults>>() {
                @Override
                public JComponent getView(IProcDocumentView<DfmDocument> host, Optional<DfmResults> information) {
                    ShocksDecompositionView result = new ShocksDecompositionView();
                    result.setDfmResults(information);
                    return result;
                }
            });
        }
    }
}