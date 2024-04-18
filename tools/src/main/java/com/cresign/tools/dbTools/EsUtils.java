package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.DeleteAliasRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Calendar;

/**
 * @author Jevon
 * @ver 1.0
 * @updated 2020/8/18 15:12
 * ##description: ES通用工具类
 */
@Service
public class EsUtils {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    public CreateIndexResponse addIndex(String indexName, Calendar calendar, JSONObject mapping) throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName + "-" + calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH));
        createIndexRequest.settings(Settings.builder().put("index.number_of_shards", 5)
                .put("index.number_of_replicas", 1));
        JSONObject aliases = new JSONObject();
        aliases.put(indexName, new JSONObject());
        aliases.put(indexName + "-read", new JSONObject());
        aliases.put(indexName + "-write", new JSONObject());
        createIndexRequest.aliases(aliases);
        createIndexRequest.mapping(mapping);
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        return createIndexResponse;
    }

    public Boolean existIndex(String indexName) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        return exists;
    }

    public AcknowledgedResponse addAlias(String indexName, String aliasName) throws IOException {
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        IndicesAliasesRequest.AliasActions aliasAction = new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD).index(indexName).alias(aliasName);
        indicesAliasesRequest.addAliasAction(aliasAction);
        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().updateAliases(indicesAliasesRequest, RequestOptions.DEFAULT);
        return acknowledgedResponse;
    }

    public org.elasticsearch.client.core.AcknowledgedResponse deleteAlias(String indexName, String aliasName) throws IOException {
        DeleteAliasRequest deleteAliasRequest = new DeleteAliasRequest(indexName, aliasName);
        org.elasticsearch.client.core.AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().deleteAlias(deleteAliasRequest, RequestOptions.DEFAULT);
        return acknowledgedResponse;
    }

    public CreateIndexResponse addMapping(String aliases, JSONObject analysis, JSONObject jsonMapping) throws IOException {
        String mapping = aliases.toLowerCase();
        String newMapping = null;
        GetIndexRequest request = new GetIndexRequest(mapping);
        boolean isExists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        if (isExists) {
            newMapping = mapping + "1";
        } else {
            newMapping = mapping;
            mapping = mapping + "1";
        }
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(newMapping);

        createIndexRequest.settings(Settings.builder().put("index.number_of_shards", 5)
                .put("index.analysis", (Path) analysis));
        JSONObject jsonAliases = new JSONObject();
        jsonAliases.put(aliases, new JSONObject());
        createIndexRequest.aliases(jsonAliases);
        createIndexRequest.mapping(jsonMapping);
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        return createIndexResponse;
    }

}
