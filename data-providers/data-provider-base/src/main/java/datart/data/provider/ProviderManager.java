/*
 * Datart
 * <p>
 * Copyright 2021
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package datart.data.provider;

import datart.core.base.exception.Exceptions;
import datart.core.base.processor.ExtendProcessor;
import datart.core.base.processor.ProcessorResponse;
import datart.core.data.provider.*;
import datart.core.data.provider.processor.DataProviderPostProcessor;
import datart.core.data.provider.processor.DataProviderPreProcessor;
import datart.core.data.provider.sql.AggregateOperator;
import datart.core.data.provider.sql.Calc;
import datart.data.provider.calculator.AbstractCalculator;
import datart.data.provider.optimize.DataProviderExecuteOptimizer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ProviderManager extends DataProviderExecuteOptimizer implements DataProviderManager {

    @Autowired(required = false)
    private List<ExtendProcessor> extendProcessors = new ArrayList<ExtendProcessor>();

    private static final Map<String, DataProvider> cachedDataProviders = new ConcurrentHashMap<>();


    private static final Map<String, AbstractCalculator> cachedCalculators = new ConcurrentHashMap<>();

    public Map<String, DataProvider> getDataProviders() {
        if (cachedDataProviders.isEmpty()) {
            synchronized (ProviderManager.class) {
                if (cachedDataProviders.isEmpty()) {
                    ServiceLoader<DataProvider> load = ServiceLoader.load(DataProvider.class);
                    for (DataProvider dataProvider : load) {
                        try {
                            cachedDataProviders.put(dataProvider.getType(), dataProvider);
                        } catch (IOException e) {
                            log.error("", e);
                        }
                    }
                }
            }
        }
        return cachedDataProviders;
    }

    @Override
    public List<DataProviderInfo> getSupportedDataProviders() {
        ArrayList<DataProviderInfo> providerInfos = new ArrayList<>();
        for (DataProvider dataProvider : getDataProviders().values()) {
            try {
                providerInfos.add(dataProvider.getBaseInfo());
            } catch (IOException e) {
                log.error("DataProvider init error {" + dataProvider.getClass().getName() + "}", e);
            }
        }
        return providerInfos;
    }

    @Override
    public DataProviderConfigTemplate getSourceConfigTemplate(String type) throws IOException {
        DataProvider providerService = getDataProviderService(type);
        DataProviderConfigTemplate configTemplate = providerService.getConfigTemplate();
        if (!CollectionUtils.isEmpty(configTemplate.getAttributes())) {
            for (DataProviderConfigTemplate.Attribute attribute : configTemplate.getAttributes()) {
                attribute.setDisplayName(providerService.getConfigDisplayName(attribute.getName()));
                attribute.setDescription(providerService.getConfigDescription(attribute.getName()));
                if (!CollectionUtils.isEmpty(attribute.getChildren())) {
                    for (DataProviderConfigTemplate.Attribute child : attribute.getChildren()) {
                        child.setDisplayName(providerService.getConfigDisplayName(child.getName()));
                        child.setDescription(providerService.getConfigDescription(child.getName()));
                    }
                }
            }
        }
        return configTemplate;
    }

    @Override
    public Object testConnection(DataProviderSource source) throws Exception {
        return getDataProviderService(source.getType()).test(source);
    }

    @Override
    public Set<String> readAllDatabases(DataProviderSource source) throws SQLException {
        return getDataProviderService(source.getType()).readAllDatabases(source);
    }

    @Override
    public Set<String> readTables(DataProviderSource source, String database) throws SQLException {
        return getDataProviderService(source.getType()).readTables(source, database);
    }

    @Override
    public Set<TableInfo> readTableInfos(DataProviderSource source, String database) {
        return getDataProviderService(source.getType()).readTableInfos(source, database);
    }

    @Override
    public Set<Column> readTableColumns(DataProviderSource source, String database, String table) throws SQLException {
        return getDataProviderService(source.getType()).readTableColumns(source, database, table);
    }

    @Override
    public Set<Column> readTableColumnInfos(DataProviderSource source, String table) {
        return getDataProviderService(source.getType()).readTableColumnInfos(source, table);
    }

    @Override
    public boolean createTable(DataProviderSource target, String table,Set<Column> columns) {
        return getDataProviderService(target.getType()).createTable(target, table, columns);
    }

    @Override
    public Set<Column> readTableColumnsBySql(DataProviderSource source, String sql) {
        return getDataProviderService(source.getType()).readTableColumnsBySql(source,sql);
    }

    @Override
    public Dataframe execute(DataProviderSource source, QueryScript queryScript, ExecuteParam param) throws Exception {

        //sql + param preprocessing
        ProcessorResponse preProcessorRes = this.preProcessorQuery(source, queryScript, param);
        if (!preProcessorRes.isSuccess()) {
            return Dataframe.empty();
        }
        Dataframe dataframe;

        DataProvider dataProvider = getDataProviderService(source.getType());

        String queryKey = dataProvider.getQueryKey(source, queryScript, param);

        if (param.isCacheEnable()) {
            dataframe = getFromCache(queryKey);
            if (dataframe != null) {
                return dataframe;
            }
        }
        if (param.isConcurrencyOptimize()) {
            dataframe = runOptimize(queryKey, source, queryScript, param);
        } else {
            dataframe = run(source, queryScript, param);
        }
        if (param.isCacheEnable()) {
            setCache(queryKey, dataframe, param.getCacheExpires());
        }
        //data postprocessing
        ProcessorResponse postProcessorRes = this.postProcessorQuery(dataframe, source, queryScript, param);
        if (!postProcessorRes.isSuccess()) {
            return Dataframe.empty();
        }

        return dataframe;

    }

    private ProcessorResponse preProcessorQuery(DataProviderSource source, QueryScript queryScript, ExecuteParam param) {
        if (!CollectionUtils.isEmpty(extendProcessors)) {
            for (ExtendProcessor processor : extendProcessors) {
                if (processor instanceof DataProviderPreProcessor) {
                    ProcessorResponse response = ((DataProviderPreProcessor) processor).preRun(source, queryScript, param);
                    if (!response.isSuccess()) {
                        return response;
                    }
                }
            }
        }
        return ProcessorResponse.success();
    }

    private ProcessorResponse postProcessorQuery(Dataframe dataframe, DataProviderSource source, QueryScript queryScript, ExecuteParam param) {
        if (!CollectionUtils.isEmpty(extendProcessors)) {
            for (ExtendProcessor processor : extendProcessors) {
                if (processor instanceof DataProviderPostProcessor) {
                    ProcessorResponse response = ((DataProviderPostProcessor) processor).postRun(dataframe, source, queryScript, param);
                    if (!response.isSuccess()) {
                        return response;
                    }
                }
            }
        }
        return ProcessorResponse.success();
    }

    @Override
    public Set<StdSqlOperator> supportedStdFunctions(DataProviderSource source) {
        return getDataProviderService(source.getType()).supportedStdFunctions(source);
    }

    @Override
    public boolean validateFunction(DataProviderSource source, String snippet) {
        DataProvider provider = getDataProviderService(source.getType());
        return provider.validateFunction(source, snippet);
    }

    @Override
    public void updateSource(DataProviderSource source) {
        DataProvider providerService = getDataProviderService(source.getType());
        providerService.resetSource(source);
    }

    @Override
    public boolean callFunction(DataProviderSource source, String function) {
        DataProvider provider = getDataProviderService(source.getType());
        return provider.callFunction(source, function);
    }

    private AbstractCalculator getCalculator(String type) {
        if (cachedCalculators.size() == 0) {
            loadCalculators();
        }
        AbstractCalculator calculator = cachedCalculators.get(type);
        if (calculator == null) {
            Exceptions.msg("No Calculator type " + type);
        }
        return calculator;
    }

    private void loadCalculators() {
        ServiceLoader<AbstractCalculator> load = ServiceLoader.load(AbstractCalculator.class);
        for (AbstractCalculator calculator : load) {
            cachedCalculators.put(calculator.type(), calculator);
        }
    }


    private void excludeColumns(Dataframe data, Set<SelectColumn> include) {
        if (data == null
                || CollectionUtils.isEmpty(data.getColumns())
                || include == null
                || include.size() == 0
                || include.stream().anyMatch(selectColumn -> selectColumn.getColumnKey().contains("*"))) {
            return;
        }

        List<Integer> excludeIndex = new LinkedList<>();
        for (int i = 0; i < data.getColumns().size(); i++) {
            Column column = data.getColumns().get(i);
            if (include
                    .stream()
                    .noneMatch(selectColumn ->
                            column.columnKey().equals(selectColumn.getColumnKey())
                                    || column.columnKey().equals(selectColumn.getAlias())
                                    || column.columnKey().contains(selectColumn.getColumnKey()))) {
                excludeIndex.add(i);
            }
        }
        if (excludeIndex.size() > 0) {
            data.getRows().parallelStream().forEach(row -> {
                for (Integer index : excludeIndex) {
                    row.set(index, null);
                }
            });
        }
    }


    private DataProvider getDataProviderService(String type) {
        DataProvider dataProvider = getDataProviders().get(type);
        if (dataProvider == null) {
            Exceptions.msg("No data provider type " + type);
        }
        return dataProvider;
    }


    @Override
    public Dataframe run(DataProviderSource source, QueryScript queryScript, ExecuteParam param) throws Exception {
        Dataframe dataframe = getDataProviderService(source.getType()).execute(source, queryScript, param);
        excludeColumns(dataframe, param.getIncludeColumns());

        //判断是否需要进行快速计算
        Predicate<AggregateOperator> filter = aggregateOperator -> aggregateOperator.getCalc() != null;
        List<AggregateOperator> operators = param.getAggregators();
        int calcIndex = ListUtils.indexOf(operators, filter);
        if (calcIndex > -1) {

            for (int i = calcIndex, j = operators.size(); i < j; i++) {
                AggregateOperator aggregateOperator = operators.get(i);
                if (!filter.evaluate(aggregateOperator)) {
                    continue;
                }
                int dataIndex = param.getGroups().size() + i;

                Calc calc = aggregateOperator.getCalc();
                AbstractCalculator calculator = getCalculator(calc.getType());
                calculator.setConfig(calc.getConfig());
                calculator.setDataProvider(getDataProviderService(source.getType()));

                calculator.calc(dataframe, dataIndex, source, queryScript, param);
            }

        }

        return dataframe;
    }

}
