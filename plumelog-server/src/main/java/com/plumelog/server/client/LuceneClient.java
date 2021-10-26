package com.plumelog.server.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.dto.TraceLogMessage;
import com.plumelog.server.util.GfJsonUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.*;

public class LuceneClient extends AbstractServerClient {

    private String localpath;

    LuceneClient() {
        this.localpath = System.getProperty("user.dir") + "/";
    }

    public void create(Collection<Document> docs, String index) throws Exception {
        Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(localpath + index));
        NRTCachingDirectory nrtCachingDirectory = new NRTCachingDirectory(directory, 5, 60);
        SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(smartChineseAnalyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter indexWriter = new IndexWriter(nrtCachingDirectory, indexWriterConfig);
        indexWriter.addDocuments(docs);
        indexWriter.commit();
        indexWriter.close();

    }

    private Collection<Document> loadDocs(List<RunLogMessage> list) {
        Collection<Document> docs = new ArrayList<>();
        for (RunLogMessage rm : list) {
            Document document = new Document();
            document.add(new StringField("id", UUID.randomUUID().toString(), Field.Store.YES));
            document.add(new StringField("appName", (rm.getAppName() == null) ? "" : rm.getAppName(), Field.Store.YES));
            document.add(new StringField("env", (rm.getEnv() == null) ? "" : rm.getEnv(), Field.Store.YES));
            document.add(new StringField("className", (rm.getClassName() == null) ? "" : rm.getClassName(), Field.Store.YES));
            document.add(new StringField("method", (rm.getMethod() == null) ? "" : rm.getMethod(), Field.Store.YES));
            document.add(new StringField("serverName", (rm.getServerName() == null) ? "" : rm.getServerName(), Field.Store.YES));
            document.add(new StringField("logLevel", (rm.getLogLevel() == null) ? "" : rm.getLogLevel(), Field.Store.YES));
            document.add(new TextField("content", (rm.getContent() == null) ? "" : rm.getContent(), Field.Store.YES));
            document.add(new StringField("dateTime", (rm.getDateTime() == null) ? "" : rm.getDateTime(), Field.Store.YES));
            document.add(new SortedNumericDocValuesField("dtTime", rm.getDtTime()));
            document.add(new StoredField("dtTime", rm.getDtTime()));
            document.add(new StringField("logType", (rm.getLogType() == null) ? "" : rm.getLogType(), Field.Store.YES));
            document.add(new StringField("appNameWithEnv", (rm.getAppNameWithEnv() == null) ? "" : rm.getAppNameWithEnv(), Field.Store.YES));
            document.add(new StringField("traceId", (rm.getTraceId() == null) ? "" : rm.getTraceId(), Field.Store.YES));
            docs.add(document);
        }
        return docs;
    }

    private Collection<Document> loadTraceDocs(List<TraceLogMessage> list) {
        Collection<Document> docs = new ArrayList<>();
        for (TraceLogMessage rm : list) {
            Document document = new Document();
            document.add(new StringField("id", UUID.randomUUID().toString(), Field.Store.YES));
            document.add(new StringField("appName", (rm.getAppName() == null) ? "" : rm.getAppName(), Field.Store.YES));
            document.add(new StringField("env", (rm.getEnv() == null) ? "" : rm.getEnv(), Field.Store.YES));
            document.add(new StringField("position", (rm.getPosition() == null) ? "" : rm.getPosition(), Field.Store.YES));
            document.add(new StringField("method", (rm.getMethod() == null) ? "" : rm.getMethod(), Field.Store.YES));
            document.add(new StringField("serverName", (rm.getServerName() == null) ? "" : rm.getServerName(), Field.Store.YES));
            document.add(new SortedNumericDocValuesField("positionNum", (rm.getPositionNum() == null) ? 1L : rm.getPositionNum()));
            document.add(new StoredField("positionNum", (rm.getPositionNum() == null) ? 1L : rm.getPositionNum()));
            document.add(new SortedNumericDocValuesField("time", rm.getTime()));
            document.add(new StoredField("time", rm.getTime()));
            document.add(new StringField("appNameWithEnv", (rm.getAppNameWithEnv() == null) ? "" : rm.getAppNameWithEnv(), Field.Store.YES));
            document.add(new StringField("traceId", (rm.getTraceId() == null) ? "" : rm.getTraceId(), Field.Store.YES));
            docs.add(document);
        }
        return docs;
    }

    @Override
    public void insertListLog(List<String> list, String baseIndex, String type) throws Exception {

    }

    public void insertListLog(List<RunLogMessage> list, String baseIndex) throws Exception {
        create(loadDocs(list), baseIndex);
    }

    @Override
    public void insertListTrace(List<String> list, String baseIndex, String type) throws Exception {

    }

    public void insertListTrace(List<TraceLogMessage> list, String baseIndex) throws Exception {


        create(loadTraceDocs(list), baseIndex);

    }

    @Override
    public void insertListComm(List<String> list, String baseIndex, String type) throws Exception {

    }

    @Override
    public boolean deleteIndex(String index) {
        try {
            Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(localpath + index));
            Analyzer analyzer = new StandardAnalyzer();// 官方推荐
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter indexWriter = new IndexWriter(directory, config);
            indexWriter.deleteAll();
            indexWriter.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public String get(String url, String queryStr) throws Exception {



        /**
         * 组装返回数据格式
         */
        return null;
    }

    @Override
    public String get(String indexStr, String queryStr, String from, String size) throws Exception {

        JSONObject queryJson = JSON.parseObject(queryStr);
        JSONArray query = queryJson.getJSONObject("query").getJSONObject("bool").getJSONArray("must");

        String[] indexs=indexStr.split(",");

        IndexReader[] indexReaders = new IndexReader[indexs.length];
        for (int i = 0; i < indexs.length; i++) {
            Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(indexs[i]));
            IndexReader reader = DirectoryReader.open(directory);
            indexReaders[i] = reader;
        }
        MultiReader multiReader = new MultiReader(indexReaders);
        IndexSearcher searcher = new IndexSearcher(multiReader);
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        /**
         * 组装查询条件
         */
        for (int a = 0; a < query.size(); a++) {
            JSONObject js = query.getJSONObject(a);
            if (js.containsKey("match_phrase")) {
                String[] matchSet = new String[1];
                js.getJSONObject("match_phrase").keySet().toArray(matchSet);
                String matchKey = matchSet[0];
                String matchValue = js.getJSONObject("match_phrase").getJSONObject(matchKey).getString("query");
                QueryParser parser = new QueryParser(matchKey, new SmartChineseAnalyzer());
                Query filedQuery = parser.parse(matchValue);
                builder.add(filedQuery, BooleanClause.Occur.MUST);
            }
            if (js.containsKey("query_string")) {
                String qStr = js.getJSONObject("query_string").getString("query");
                QueryParser parser = new QueryParser("content", new SmartChineseAnalyzer());
                // 创建查询对象
                Query content = parser.parse(qStr);
                builder.add(content, BooleanClause.Occur.MUST);
            }
            if (js.containsKey("range")) {
                Long gte = js.getJSONObject("range").getJSONObject("dtTime").getLong("gte");
                Long lt = js.getJSONObject("range").getJSONObject("dtTime").getLong("lt");
                Query range = NumericDocValuesField.newSlowRangeQuery("dtTime", gte, lt);
                builder.add(range, BooleanClause.Occur.MUST);
            }
        }
        Sort sort = new Sort();
        SortField sortField = new SortField("dtTime", SortField.Type.LONG,true);
        sort.setSort(sortField);
        TopDocs topDocs = searcher.search(builder.build(), 10000);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;




        return null;
    }

    @Override
    public String cat(String index) {
        return null;
    }

    @Override
    public boolean creatIndice(String indice, String type) {
        return false;
    }

    @Override
    public boolean creatIndiceTrace(String indice, String type) {
        return false;
    }

    @Override
    public boolean existIndice(String indice) {
        return false;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean creatIndiceNomal(String indice, String type) {
        return false;
    }

    @Override
    public List<String> getExistIndices(String[] indices) {
        return Arrays.asList(indices.clone());
    }

    @Override
    public void close() {

    }
}

