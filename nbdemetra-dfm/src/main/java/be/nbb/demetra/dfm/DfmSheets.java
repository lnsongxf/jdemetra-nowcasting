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
package be.nbb.demetra.dfm;

import be.nbb.demetra.dfm.properties.DaysEditor;
import ec.nbdemetra.ui.properties.NodePropertySetBuilder;
import ec.tstoolkit.dfm.DfmEstimationSpec;
import ec.tstoolkit.dfm.DfmModelSpec;
import ec.tstoolkit.dfm.DfmSimulationSpec;
import ec.tstoolkit.dfm.EmSpec;
import ec.tstoolkit.dfm.NumericalProcessingSpec;
import ec.tstoolkit.dfm.PcSpec;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.var.VarSpec;
import ec.tstoolkit.var.VarSpec.Initialization;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * @author Philippe Charles
 */
final class DfmSheets {

    private DfmSheets() {
        // static class
    }

    // shared because methods called only in EDT
    private static final NodePropertySetBuilder B = new NodePropertySetBuilder();

    @NbBundle.Messages({
        "pcSpec.enabled.display=Enabled",
        "pcSpec.span.display=Estimation span",
        "pcSpec.minPartNonMissingSeries.display=Data availability (min %)"
    })
    private static void withPcSpec(PcSpec bean) {
        B.withBoolean()
                .select(bean, "enabled")
                .display(Bundle.pcSpec_enabled_display())
                .add();
        B.with(TsPeriodSelector.class)
                .select(bean, "span")
                .display(Bundle.pcSpec_span_display())
                .add();
        B.withDouble()
                .select(bean, "minPartNonMissingSeries")
                .display(Bundle.pcSpec_minPartNonMissingSeries_display())
                .min(0).max(1)
                .add();
    }

    @NbBundle.Messages({
        "emSpec.enabled.display=Enabled",
        "emSpec.version.display=Version",
        "emSpec.maxIter.display=Max iterations",
        "emSpec.maxNumIter.display=Max numerical iterations",
        "emSpec.precision.display=Precision"
    })
    private static void withEmSpec(EmSpec bean) {
        B.withBoolean()
                .select(bean, "enabled")
                .display(Bundle.emSpec_enabled_display())
                .add();
        B.withInt()
                .select(bean, "version")
                .display(Bundle.emSpec_version_display())
                .min(1).max(2)
                .add();
        B.withInt()
                .select(bean, "maxIter")
                .display(Bundle.emSpec_maxIter_display())
                .min(1)
                .add();
        B.withInt()
                .select(bean, "maxNumIter")
                .display(Bundle.emSpec_maxNumIter_display())
                .min(1)
                .add();
        B.withDouble()
                .select(bean, "precision")
                .display(Bundle.emSpec_precision_display())
                .min(0)
                .add();
    }

    @NbBundle.Messages({
        "numericalProcessingSpec.enabled.display=Enabled",
        "numericalProcessingSpec.maxIter.display=Max number iterations",
        "numericalProcessingSpec.maxInitialIter.display=Simplified model iterations",
        "numericalProcessingSpec.maxStepIter.display=Max number iterations in optimization by block",
        "numericalProcessingSpec.independentShocks.display=Independent VAR shocks",
        "numericalProcessingSpec.blockIterations.display=Iterations by blocks",
        "numericalProcessingSpec.mixedEstimation.display=Mixed estimation",
        "numericalProcessingSpec.method.display=Optimization method",
        "numericalProcessingSpec.precision.display=Precision"
    })
    private static void withNumericalProcessingSpec(NumericalProcessingSpec bean) {
        B.withBoolean()
                .select(bean, "enabled")
                .display(Bundle.numericalProcessingSpec_enabled_display())
                .add();
        B.withInt()
                .select(bean, "maxIter")
                .display(Bundle.numericalProcessingSpec_maxIter_display())
                .min(1)
                .add();
        B.withInt()
                .select(bean, "maxIntermediateIter")
                .display(Bundle.numericalProcessingSpec_maxStepIter_display())
                .min(1)
                .add();
        B.withInt()
                .select(bean, "maxInitialIter")
                .display(Bundle.numericalProcessingSpec_maxInitialIter_display())
                .min(0)
                .add();
        B.withBoolean()
                .select(bean, "independentVarShocks")
                .display(Bundle.numericalProcessingSpec_independentShocks_display())
                .add();
        B.withBoolean()
                .select(bean, "blockIterations")
                .display(Bundle.numericalProcessingSpec_blockIterations_display())
                .add();
        B.withBoolean()
                .select(bean, "mixedEstimation")
                .display(Bundle.numericalProcessingSpec_mixedEstimation_display())
                .add();
        B.withEnum(NumericalProcessingSpec.Method.class)
                .select(bean, "method")
                .display(Bundle.numericalProcessingSpec_method_display())
                .add();
        B.withDouble()
                .select(bean, "precision")
                .display(Bundle.numericalProcessingSpec_precision_display())
                .min(1e-15)
                .add();
    }

    @NbBundle.Messages({
        "principalComponentsSpec.display=Principal components",
        "preEmSpec.display=Preliminary EM",
        "numericalProcessingSpec.display=Numerical optimization",
        "postEmSpec.display=Final EM"
    })
    public static Sheet onDfmEstimationSpec(DfmEstimationSpec spec) {
        Sheet result = new Sheet();

        B.reset("principalComponentsSpec").display(Bundle.principalComponentsSpec_display());
        withPcSpec(spec.getPrincipalComponentsSpec());
        result.put(B.build());

        B.reset("preEmSpec").display(Bundle.preEmSpec_display());
        withEmSpec(spec.getPreEmSpec());
        result.put(B.build());

        B.reset("numericalProcessingSpec").display(Bundle.numericalProcessingSpec_display());
        withNumericalProcessingSpec(spec.getNumericalProcessingSpec());
        result.put(B.build());

        B.reset("postEmSpec").display(Bundle.postEmSpec_display());
        withEmSpec(spec.getPostEmSpec());
        result.put(B.build());

        return result;
    }

    @NbBundle.Messages({
        "varSpec.display=VAR model",
        "varSpec.nvars.display=Equations count",
        "varSpec.nlags.display=Lags count",
        "varSpec.initialization.display=Initialization",
        "modelSpec.display=Dfm model",
        "modelSpec.forecastHorizon.display=Forecast horizon",
        "simulationSpec.display=Simulation spec"
    })

    public static Sheet onModelSpec(final DfmModelSpec spec) {
        final VarSpec vspec = spec.getVarSpec();
        Sheet result = new Sheet();
        B.reset("varSpec").display(Bundle.varSpec_display());
        withVarSpec(spec.getVarSpec());
        result.put(B.build());
        B.reset("modelSpec").display(Bundle.modelSpec_display());
        withModelSpec(spec);
        result.put(B.build());
        return result;
    }
    
    public static Sheet onSimulationSpec(final DfmSimulationSpec spec) {
        Sheet result = new Sheet();
        B.reset("Simulation spec").display(Bundle.simulationSpec_display());
        B.with(Day[].class)
                .select(spec, "estimationDays")
                .editor(DaysEditor.class)
                .display("Estimation days")
                .description("Days where the model estimation has to be forced")
                .add();
        B.withInt()
                .select(spec, "numberOfYears")
                .display("Number of years")
                .min(1)
                .description("Number of past years used for the simulation")
                .add();
        result.put(B.build());
        return result;
    }

    private static void withVarSpec(final VarSpec vspec) {
        B.withInt()
                .select(new PropertySupport.ReadWrite<Integer>("nvars", Integer.class, Bundle.varSpec_nvars_display(), null) {
                    @Override
                    public Integer getValue() {
                        return vspec.getEquationsCount();
                    }

                    @Override
                    public void setValue(Integer val) {
                        vspec.setSize(val, vspec.getLagsCount());
                    }
                })
                .min(1)
                .add();
        B.withInt()
                .select(new PropertySupport.ReadWrite<Integer>("nlags", Integer.class, Bundle.varSpec_nlags_display(), null) {
                    @Override
                    public Integer getValue() {
                        return vspec.getLagsCount();
                    }

                    @Override
                    public void setValue(Integer val) {
                        vspec.setSize(vspec.getEquationsCount(), val);
                    }
                })
                .min(1)
                .add();
        B.withEnum(Initialization.class)
                .select(vspec, "initialization")
                .display(Bundle.varSpec_initialization_display())
                .add();
    }

    private static void withModelSpec(final DfmModelSpec spec) {
        B.withInt()
                .select(new PropertySupport.ReadWrite<Integer>("forecastHorizon", Integer.class, Bundle.modelSpec_forecastHorizon_display(), null) {
                    @Override
                    public Integer getValue() {
                        return spec.getForecastHorizon();
                    }

                    @Override
                    public void setValue(Integer val) {
                        spec.setForecastHorizon(val);
                    }
                })
                .min(1)
                .add();
    }
}
