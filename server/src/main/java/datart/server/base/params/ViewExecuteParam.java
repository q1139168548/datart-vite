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

package datart.server.base.params;

import datart.core.base.PageInfo;
import datart.core.common.JsonUtils;
import datart.core.data.provider.SelectColumn;
import datart.core.data.provider.sql.*;
import datart.security.base.ResourceType;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ViewExecuteParam implements Serializable {

    private String vizId;

    private String vizName;

    private ResourceType vizType;

    private String viewId;

    private List<SelectKeyword> keywords;

    private List<SelectColumn> columns;

    private Map<String, Set<String>> params;

    private List<FunctionColumn> functionColumns;

    private List<AggregateOperator> aggregators;

    private List<FilterOperator> filters;

    private List<GroupByOperator> groups;

    private List<OrderOperator> orders;

    private PageInfo pageInfo;

    private boolean concurrencyControl;

    private String concurrencyControlModel;

    private boolean cache;

    private int cacheExpires;

    private boolean script;

    private boolean analytics;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(columns)
                && CollectionUtils.isEmpty(aggregators)
                && CollectionUtils.isEmpty(groups);
    }


    public static void main(String[] args) {

        String json = "{\"cache\":true,\"cacheExpires\":4,\"concurrencyControl\":true,\"concurrencyControlMode\":\"DIRTYREAD\",\"viewId\":\"f5e793689202403d8ceb250005bc40b5\",\"aggregators\":[{\"column\":\"complete\",\"sqlOperator\":\"SUM\"},{\"column\":\"score\",\"sqlOperator\":\"SUM\"}],\"groups\":[{\"column\":\"u_name\"},{\"column\":\"weight\"},{\"column\":\"start_time\"},{\"column\":\"due_time\"}],\"filters\":[],\"orders\":[{\"column\":\"start_time\",\"operator\":\"DESC\"}],\"pageInfo\":{\"countTotal\":false,\"pageNo\":1,\"pageSize\":100},\"functionColumns\":[{\"alias\":\"score\",\"snippet\":\"complete / 7 * 0.3 * weight * rate\"}],\"columns\":[],\"script\":false,\"params\":{}}";

//        JSON.parseObject(json,ViewExecuteParam.class);

        ViewExecuteParam v =  JsonUtils.toObject(json,ViewExecuteParam.class);

        System.out.println(v);
    }

}
