/*
 * Copyright 2014 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package be.nbb.demetra.dfm.output.simulation;

import be.nbb.demetra.dfm.output.news.outline.CustomNode;
import be.nbb.demetra.dfm.output.news.outline.NewsTreeModel;
import be.nbb.demetra.dfm.output.news.outline.XOutline;
import be.nbb.demetra.dfm.output.news.outline.XOutline.Title;
import be.nbb.demetra.dfm.output.simulation.outline.SimulationNode;
import be.nbb.demetra.dfm.output.simulation.outline.SimulationOutlineCellRenderer;
import be.nbb.demetra.dfm.output.simulation.outline.SimulationRowModel;
import be.nbb.demetra.dfm.output.simulation.utils.FilterEvaluationSamplePanel;
import com.google.common.base.Optional;
import ec.nbdemetra.ui.DemetraUI;
import ec.tss.dfm.DfmDocument;
import ec.tss.dfm.DfmSeriesDescriptor;
import ec.tss.dfm.DfmSimulation;
import ec.tss.dfm.DfmSimulationResults;
import ec.tss.dfm.ForecastEvaluationResults;
import ec.tss.timeseries.diagnostics.GlobalForecastingEvaluation;
import ec.tss.tsproviders.utils.Formatters;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataCollector;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.util.chart.ColorScheme;
import ec.util.chart.swing.SwingColorSchemeSupport;
import ec.util.various.swing.ModernUI;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.tree.TreeModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;

/**
 *
 * @author Mats Maggi
 */
public class SimulationQuantifiedResultsView extends JPanel {

    public static final String DFM_SIMULATION_PROPERTY = "dfmSimulation";
    public static final String D_M_TEST = "Squared loss";
    public static final String D_M_ABS_TEST = "Absolute loss";
    public static final String ENCOMPASING_TEST = "Encompasing Test";

    // Top bar
    private final JComboBox comboBox;
    private final JPanel comboBoxPanel;
    private final JLabel variableLabel;
    private final JComboBox typeComboBox;
    private final JLabel typeLabel;
    private final JButton filterButton;

    // Center component
    private final XOutline outline;

    private final DemetraUI demetraUI;
    private Formatters.Formatter<Number> formatter;
    private SwingColorSchemeSupport defaultColorSchemeSupport;

    private Optional<DfmSimulation> dfmSimulation;

    private List<CustomNode> nodes;
    private FilterEvaluationSamplePanel filterPanel;

    public SimulationQuantifiedResultsView(DfmDocument doc) {
        setLayout(new BorderLayout());

        // Top panel
        comboBoxPanel = new JPanel();
        comboBoxPanel.setLayout(new BoxLayout(comboBoxPanel, BoxLayout.LINE_AXIS));
        variableLabel = new JLabel("Variable :");
        variableLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 10));
        comboBoxPanel.add(variableLabel);

        comboBox = new JComboBox();
        comboBox.setRenderer(new ComboBoxRenderer());
        comboBoxPanel.add(comboBox);

        typeLabel = new JLabel("Type :");
        typeLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 10));
        comboBoxPanel.add(typeLabel);
        typeComboBox = new JComboBox(new DefaultComboBoxModel(new String[]{"Level", "Year On Year", "Quarter On Quarter"}));
        comboBoxPanel.add(typeComboBox);

        filterButton = new JButton("Filter sample");
        typeLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 0, 0));
        filterButton.addActionListener((ActionEvent evt) -> {
            filterButtonActionPerformed(evt);
        });
        comboBoxPanel.add(filterButton);

        demetraUI = DemetraUI.getDefault();
        formatter = demetraUI.getDataFormat().numberFormatter();
        defaultColorSchemeSupport = new SwingColorSchemeSupport() {
            @Override
            public ColorScheme getColorScheme() {
                return demetraUI.getColorScheme();
            }
        };

        this.dfmSimulation = Optional.absent();

        outline = new XOutline();
        outline.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        typeComboBox.addItemListener((ItemEvent e) -> {
            filterPanel = null;
            updateOutlineModel();
        });

        comboBox.addItemListener((ItemEvent e) -> {
            filterPanel = null;
            updateOutlineModel();
        });

        JScrollPane p = ModernUI.withEmptyBorders(new JScrollPane());
        p.setViewportView(outline);

        addPropertyChangeListener((PropertyChangeEvent evt) -> {
            switch (evt.getPropertyName()) {
                case DFM_SIMULATION_PROPERTY:
                    updateComboBox();
                    updateOutlineModel();
            }
        });

        updateComboBox();
        updateOutlineModel();

        demetraUI.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            switch (evt.getPropertyName()) {
                case DemetraUI.DATA_FORMAT_PROPERTY:
                    onDataFormatChanged();
                    break;
                case DemetraUI.COLOR_SCHEME_NAME_PROPERTY:
                    onColorSchemeChanged();
                    break;
            }
        });

        add(comboBoxPanel, BorderLayout.NORTH);
        add(p, BorderLayout.CENTER);
    }

    private void onDataFormatChanged() {
        formatter = demetraUI.getDataFormat().numberFormatter();
    }

    private void onColorSchemeChanged() {
        defaultColorSchemeSupport = new SwingColorSchemeSupport() {
            @Override
            public ColorScheme getColorScheme() {
                return demetraUI.getColorScheme();
            }
        };
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public Optional<DfmSimulation> getSimulationResults() {
        return dfmSimulation;
    }

    public void setSimulationResults(Optional<DfmSimulation> dfmSimulation) {
        Optional<DfmSimulation> old = this.dfmSimulation;
        this.dfmSimulation = dfmSimulation != null ? dfmSimulation : Optional.<DfmSimulation>absent();
        firePropertyChange(DFM_SIMULATION_PROPERTY, old, this.dfmSimulation);
    }
    //</editor-fold>

    private void updateComboBox() {
        filterPanel = null;
        if (dfmSimulation.isPresent()) {
            comboBox.setModel(toComboBoxModel(dfmSimulation.get().getDescriptions()));
            comboBox.setEnabled(true);
        } else {
            comboBox.setModel(new DefaultComboBoxModel());
            comboBox.setEnabled(false);
        }
    }

    private DefaultComboBoxModel toComboBoxModel(List<DfmSeriesDescriptor> data) {
        List<DfmSeriesDescriptor> desc = new ArrayList<>();
        List<Boolean> watched = dfmSimulation.get().getWatched();
        for (int i = 0; i < watched.size(); i++) {
            if (watched.get(i)) {
                desc.add(data.get(i));
            }
        }
        DefaultComboBoxModel result = new DefaultComboBoxModel(desc.toArray());
        return result;
    }

    private void updateOutlineModel() {
        if (dfmSimulation != null
                && dfmSimulation.isPresent()
                && comboBox.getSelectedIndex() != -1
                && typeComboBox.getSelectedIndex() != -1) {
            calculateData();
            refreshModel();
        }
    }

    private void filterButtonActionPerformed(ActionEvent evt) {
        int r = JOptionPane.showConfirmDialog(outline, filterPanel, "Select evaluation sample", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            updateOutlineModel();
        }
    }

    private void refreshModel() {
        TreeModel treeMdl = new NewsTreeModel(nodes);
        OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeMdl, new SimulationRowModel(titles), true);
        outline.setDefaultRenderer(String.class, new SimulationOutlineCellRenderer(formatter));
        outline.setModel(mdl);

        outline.getColumnModel().getColumn(0).setHeaderValue(" ");
        for (int i = 1; i < outline.getColumnCount(); i++) {
            outline.getColumnModel().getColumn(i).setPreferredWidth(60);
        }
        outline.expandAll();
    }

    private List<TsPeriod> periods;
    private List<Integer> horizons;
    private List<Title> titles;
    private List<TsPeriod> filteredPeriods;

    private void createTitles(List<Integer> data) {
        titles = new ArrayList<>();
        data.stream().forEach((i) -> {
            titles.add(new Title(String.valueOf(i)));
        });
        outline.setTitles(titles);
    }

    private void calculateData() {
        nodes = new ArrayList<>();
        int selectedIndex = comboBox.getSelectedIndex();
        int type = typeComboBox.getSelectedIndex();

        DfmSimulationResults dfm = dfmSimulation.get().getDfmResults().get(selectedIndex);
        DfmSimulationResults arima = dfmSimulation.get().getArimaResults().get(selectedIndex);
        periods = dfm.getEvaluationSample();
        horizons = dfm.getForecastHorizons();

        List<Double> trueValues = type == 1 ? dfm.getTrueValuesYoY() : type == 2 ? dfm.getTrueValuesQoQ() : dfm.getTrueValues();
        Double[][] dfmFcts = type == 1 ? dfm.getForecastsArrayYoY() : type == 2 ? dfm.getForecastsArrayQoQ() : dfm.getForecastsArray();
        Double[][] arimaFcts = type == 1 ? arima.getForecastsArrayYoY() : type == 2 ? arima.getForecastsArrayQoQ() : arima.getForecastsArray();
        Map<Integer, TsData> dfmTs = new HashMap<>();
        Map<Integer, TsData> arimaTs = new HashMap<>();

        // Remove periods of evaluation sample not in true values domain
        filteredPeriods = filterEvaluationSample(trueValues);

        if (filterPanel == null) {
            filterPanel = new FilterEvaluationSamplePanel(filteredPeriods);
        }

        TsFrequency freq = periods.get(0).getFrequency();

        TsDataCollector coll = new TsDataCollector();
        for (int i = 0; i < periods.size(); i++) {
            if (trueValues.get(i) != null) {
                coll.addObservation(periods.get(i).middle(), trueValues.get(i));
            } else {
                coll.addMissingValue(periods.get(i).middle());
            }
        }
        TsData trueTsData = coll.make(freq, TsAggregationType.None);

        fillMap(dfmTs, dfmFcts, freq);
        fillMap(arimaTs, arimaFcts, freq);

        List<Integer> filteredHorizons = new ArrayList<>();
        filteredHorizons.addAll(dfmTs.keySet());
        Collections.sort(filteredHorizons);

        createTitles(filteredHorizons);

        TsPeriod start = filteredPeriods.get(filterPanel.getStart());
        TsPeriod end = filteredPeriods.get(filterPanel.getEnd());
        TsDomain dom = new TsDomain(start, end.minus(start) + 1);

        // Base
        List<Double> valuesRMSE = new ArrayList<>(), relValuesRMSE = new ArrayList<>();
        List<Double> valuesMAE = new ArrayList<>(), relValuesMAE = new ArrayList<>();
        List<Double> valuesMdAE = new ArrayList<>(), relValuesMdAE = new ArrayList<>();

        List<Double> valuesRMSPE = new ArrayList<>(), relValuesRMSPE = new ArrayList<>();
        List<Double> values_sMAPE = new ArrayList<>(), relValues_sMAPE = new ArrayList<>();
        List<Double> values_sMdAPE = new ArrayList<>(), relValues_sMdAPE = new ArrayList<>();

        List<Double> valuesRMSSE = new ArrayList<>(), relValuesRMSSE = new ArrayList<>();
        List<Double> valuesMASE = new ArrayList<>(), relValuesMASE = new ArrayList<>();
        List<Double> values_MdASE = new ArrayList<>(), relValues_MdASE = new ArrayList<>();

        List<Double> pbValues = new ArrayList<>();
        List<Double> dmSqValues = new ArrayList<>();
        List<Double> dmAbsValues = new ArrayList<>();
        List<Double> encValues = new ArrayList<>();
        List<Double> biasValues = new ArrayList<>();
        List<Double> efficiencyValues = new ArrayList<>();

        for (Integer horizon : filteredHorizons) {
            TsData fcts = dfmTs.get(horizon) == null ? null : dfmTs.get(horizon).fittoDomain(dom);
            TsData fctsBench = arimaTs.get(horizon) == null ? null : arimaTs.get(horizon).fittoDomain(dom);
            TsData trueData = trueTsData.fittoDomain(dom);
            ForecastEvaluationResults rslt = new ForecastEvaluationResults(fcts, fctsBench, trueData);
            valuesRMSE.add(rslt.calcRMSE());
            valuesMAE.add(rslt.calcMAE());
            valuesMdAE.add(rslt.calcMdAE());
            valuesRMSPE.add(rslt.calcRMSPE());
            values_sMAPE.add(rslt.calc_sMAPE());
            values_sMdAPE.add(rslt.calc_sMdAPE());
            valuesRMSSE.add(rslt.calcRMSSE());
            valuesMASE.add(rslt.calcMASE());
            values_MdASE.add(rslt.calcMdASE());
            relValuesRMSE.add(rslt.calcRelRMSE());
            relValuesMAE.add(rslt.calcRelMAE());
            relValuesMdAE.add(rslt.calcRelMdAE());
            relValuesRMSPE.add(rslt.calcRelRMSPE());
            relValues_sMAPE.add(rslt.calcRel_sMAPE());
            relValues_sMdAPE.add(rslt.calcRel_sMdAPE());
            relValuesRMSSE.add(rslt.calcRelRMSSE());
            relValuesMASE.add(rslt.calcRelMASE());
            relValues_MdASE.add(rslt.calcRelMdASE());

            pbValues.add(rslt.calcPB());

            GlobalForecastingEvaluation test = new GlobalForecastingEvaluation(
                    fcts, fctsBench, trueTsData, ec.tss.timeseries.diagnostics.AccuracyTests.AsymptoticsType.STANDARD);
            test.setDelay(horizon);
            dmSqValues.add(test.getDieboldMarianoTest().getPValue());
            dmAbsValues.add(test.getDieboldMarianoAbsoluteTest().getPValue());
            encValues.add(test.getModelEncompassesBenchmarkTest().getPValue());
            biasValues.add(test.getBiasTest().getPValue());
            efficiencyValues.add(test.getEfficiencyTest().getPValue());
        }

        nodes.add(new SimulationNode("Scale dependent")
                .addChild(new SimulationNode("RMSE", valuesRMSE))
                .addChild(new SimulationNode("MAE", valuesMAE))
                .addChild(new SimulationNode("MdAE", valuesMdAE)));

        nodes.add(new SimulationNode("Percentage errors")
                .addChild(new SimulationNode("RMSPE", valuesRMSPE))
                .addChild(new SimulationNode("sMAPE", values_sMAPE))
                .addChild(new SimulationNode("sMdAPE", values_sMdAPE)));

        nodes.add(new SimulationNode("Scaled errors")
                .addChild(new SimulationNode("RMSSE", valuesRMSSE))
                .addChild(new SimulationNode("MASE", valuesMASE))
                .addChild(new SimulationNode("MdASE", values_MdASE)));

        // Relative
        nodes.add(new SimulationNode("Relative")
                .addChild(new SimulationNode("Scale dependent")
                        .addChild(new SimulationNode("RMSE", relValuesRMSE))
                        .addChild(new SimulationNode("MAE", relValuesMAE))
                        .addChild(new SimulationNode("MdAE", relValuesMdAE)))
                .addChild(new SimulationNode("Percentage error")
                        .addChild(new SimulationNode("RMSPE", relValuesRMSPE))
                        .addChild(new SimulationNode("sMAPE", relValues_sMAPE))
                        .addChild(new SimulationNode("sMdAPE", relValues_sMdAPE)))
                .addChild(new SimulationNode("Scaled errors")
                        .addChild(new SimulationNode("RMSSE", relValuesRMSSE))
                        .addChild(new SimulationNode("MASE", relValuesMASE))
                        .addChild(new SimulationNode("MdASE", relValues_MdASE)))
                .addChild(new SimulationNode("Percentage better", pbValues))
        );

        // Tests
        nodes.add(new SimulationNode("Diebold Mariano")
                .addChild(new SimulationNode("Standard")
                        .addChild(new SimulationNode(D_M_TEST, dmSqValues))
                        .addChild(new SimulationNode(D_M_ABS_TEST, dmAbsValues)))
        );

        nodes.add(new SimulationNode(ENCOMPASING_TEST)
                .addChild(new SimulationNode("Standard", encValues)));

        nodes.add(new SimulationNode("Bias")
                .addChild(new SimulationNode("Standard", biasValues)));

        nodes.add(new SimulationNode("Efficiency Test")
                .addChild(new SimulationNode("Standard", efficiencyValues)));
    }

    private List<TsPeriod> filterEvaluationSample(List<Double> trueValues) {
        List<TsPeriod> p = new ArrayList<>();
        for (int i = 0; i < trueValues.size(); i++) {
            if (trueValues.get(i) != null) {
                p.add(periods.get(i));
            }
        }
        return p;
    }

    private void fillMap(Map<Integer, TsData> map, Double[][] fcts, TsFrequency freq) {
        TsDataCollector coll = new TsDataCollector();
        for (int i = 0; i < horizons.size(); i++) {
            coll.clear();
            for (int j = 0; j < periods.size(); j++) {
                if (fcts[i][j] != null) {
                    coll.addObservation(periods.get(j).middle(), fcts[i][j]);
                } else {
                    coll.addMissingValue(periods.get(j).middle());
                }
            }

            TsData ts = coll.make(freq, TsAggregationType.None);
            ts = ts.cleanExtremities();

            if (ts.getStart().isNotAfter(filteredPeriods.get(filterPanel.getStart()))
                    && ts.getEnd().isNotBefore(filteredPeriods.get(filterPanel.getEnd()))) {
                map.put(horizons.get(i), coll.make(freq, TsAggregationType.None));
            }
        }
    }
}
