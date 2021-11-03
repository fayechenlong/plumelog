package com.plumelog.core.lucene;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.plumelog.core.client.AbstractServerClient;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.dto.TraceLogMessage;
import com.plumelog.core.util.GfJsonUtil;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author chenlongfei
 * @version 1.0.0
 * time:2021-11-01
 * description:lucene作为日志检索存储引擎
 */
public class LuceneClient extends AbstractServerClient {

    private String localpath;

    public LuceneClient(String rootPath) {
        this.localpath = rootPath + "/data/";
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
            document.add(new SortedDocValuesField("appName", new BytesRef((rm.getAppName() == null) ? "" : rm.getAppName())));
            document.add(new StringField("env", (rm.getEnv() == null) ? "" : rm.getEnv(), Field.Store.YES));
            document.add(new StringField("className", (rm.getClassName() == null) ? "" : rm.getClassName(), Field.Store.YES));
            document.add(new StringField("method", (rm.getMethod() == null) ? "" : rm.getMethod(), Field.Store.YES));
            document.add(new StringField("serverName", (rm.getServerName() == null) ? "" : rm.getServerName(), Field.Store.YES));
            document.add(new StringField("logLevel", (rm.getLogLevel() == null) ? "" : rm.getLogLevel(), Field.Store.YES));
            document.add(new SortedDocValuesField("logLevel", new BytesRef((rm.getLogLevel() == null) ? "" : rm.getLogLevel())));
            document.add(new TextField("content", (rm.getContent() == null) ? "" : rm.getContent(), Field.Store.YES));
            document.add(new StringField("dateTime", (rm.getDateTime() == null) ? "" : rm.getDateTime(), Field.Store.YES));
            document.add(new NumericDocValuesField("dtTime", rm.getDtTime()));
            document.add(new StoredField("dtTime", rm.getDtTime()));
            document.add(new StringField("logType", (rm.getLogType() == null) ? "" : rm.getLogType(), Field.Store.YES));
            document.add(new StringField("appNameWithEnv", (rm.getAppNameWithEnv() == null) ? "" : rm.getAppNameWithEnv(), Field.Store.YES));
            document.add(new SortedDocValuesField("appNameWithEnv", new BytesRef((rm.getAppNameWithEnv() == null) ? "" : rm.getAppNameWithEnv())));
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
            document.add(new SortedDocValuesField("appName", new BytesRef((rm.getAppName() == null) ? "" : rm.getAppName())));
            document.add(new StringField("env", (rm.getEnv() == null) ? "" : rm.getEnv(), Field.Store.YES));
            document.add(new StringField("position", (rm.getPosition() == null) ? "" : rm.getPosition(), Field.Store.YES));
            document.add(new StringField("method", (rm.getMethod() == null) ? "" : rm.getMethod(), Field.Store.YES));
            document.add(new StringField("serverName", (rm.getServerName() == null) ? "" : rm.getServerName(), Field.Store.YES));
            document.add(new NumericDocValuesField("positionNum", (rm.getPositionNum() == null) ? 1L : rm.getPositionNum()));
            document.add(new StoredField("positionNum", (rm.getPositionNum() == null) ? 1L : rm.getPositionNum()));
            document.add(new NumericDocValuesField("time", rm.getTime()));
            document.add(new StoredField("time", rm.getTime()));
            document.add(new StringField("appNameWithEnv", (rm.getAppNameWithEnv() == null) ? "" : rm.getAppNameWithEnv(), Field.Store.YES));
            document.add(new SortedDocValuesField("appNameWithEnv", new BytesRef((rm.getAppNameWithEnv() == null) ? "" : rm.getAppNameWithEnv())));
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
            List<String> list = getIndex(index);
            for (String in : list) {
                Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(localpath + in));
                Analyzer analyzer = new StandardAnalyzer();// 官方推荐
                IndexWriterConfig config = new IndexWriterConfig(analyzer);
                IndexWriter indexWriter = new IndexWriter(directory, config);
                indexWriter.deleteAll();
                indexWriter.close();
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public String get(String url, String queryStr) throws Exception {
        return null;
    }


    private List<String> getIndex(String indexStr) {
        List<String> indexsList = new ArrayList<>();
        String[] indexs = indexStr.split(",");
        for (int i = 0; i < indexs.length; i++) {
            if (indexs[i].contains("*")) {
                File dir = new File(this.localpath);
                if (dir.isDirectory()) {
                    File[] subs = dir.listFiles();
                    for (File sub : subs) {
                        String name = sub.getName();
                        if (Pattern.matches(indexs[i].replace("*", ".*"), name)) {
                            indexsList.add(name);
                        }
                    }
                }
            } else {
                indexsList.add(indexs[i]);
            }
        }
        return indexsList;
    }

    private String queryData(String indexStr, String queryStr, String from, String size) throws Exception {

        JSONObject queryJson = JSON.parseObject(queryStr);
        if (queryJson.containsKey("query")) {
            JSONArray query = queryJson.getJSONObject("query").getJSONObject("bool").getJSONArray("must");

            List<String> indexs = getIndex(indexStr);

            IndexReader[] indexReaders = new IndexReader[indexs.size()];
            for (int i = 0; i < indexs.size(); i++) {
                Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(this.localpath + indexs.get(i)));
                IndexReader reader = DirectoryReader.open(directory);
                indexReaders[i] = reader;
            }
            MultiReader multiReader = new MultiReader(indexReaders);
            IndexSearcher searcher = new IndexSearcher(multiReader);
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
            for (int a = 0; a < query.size(); a++) {
                JSONObject js = query.getJSONObject(a);
                if (js.containsKey("match_phrase")) {
                    String[] matchSet = new String[1];
                    js.getJSONObject("match_phrase").keySet().toArray(matchSet);
                    String matchKey = matchSet[0];
                    String matchValue = js.getJSONObject("match_phrase").getJSONObject(matchKey).getString("query");
                    TermQuery termQuery = new TermQuery(new Term(matchKey, matchValue));
                    builder.add(termQuery, BooleanClause.Occur.MUST);
                }
                if (js.containsKey("match")) {
                    String[] matchSet = new String[1];
                    js.getJSONObject("match").keySet().toArray(matchSet);
                    String matchKey = matchSet[0];
                    String matchValue = js.getJSONObject("match").getJSONObject(matchKey).getString("query");
                    TermQuery termQuery = new TermQuery(new Term(matchKey, matchValue));
                    builder.add(termQuery, BooleanClause.Occur.MUST);
                }
                if (js.containsKey("query_string")) {
                    String qStr = js.getJSONObject("query_string").getString("query");
                    QueryParser parser = new QueryParser("content", analyzer);
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
            if (builder.build().clauses().isEmpty()) {
                builder.add(new WildcardQuery(new Term("appName", "*")), BooleanClause.Occur.SHOULD);
            }
            Sort sort = new Sort();
            if (queryJson.containsKey("sort")) {
                List<SortField> sortFieldList = new ArrayList<>();
                JSONArray sortJsons = queryJson.getJSONArray("sort");
                for (int i = 0; i < sortJsons.size(); i++) {
                    JSONObject sortJson = sortJsons.getJSONObject(i);
                    String[] keys = new String[sortJson.keySet().size()];
                    sortJson.keySet().toArray(keys);
                    for (int j = 0; j < keys.length; j++) {
                        String key = keys[j];
                        String value = sortJson.getString(key);
                        boolean reverse = true;
                        if (value.equals("asc")) {
                            reverse = false;
                        }
                        sortFieldList.add(new SortField(key, SortField.Type.LONG, reverse));
                    }
                }
                SortField[] sortFields = new SortField[sortFieldList.size()];
                sortFieldList.toArray(sortFields);
                sort.setSort(sortFields);
            }

            int min = Integer.parseInt(from);
            int count = Integer.parseInt(size);
            int max = min + count;
            if (max == 0) {
                max = 10000;
            }
            TopDocs topDocs = searcher.search(builder.build(), max, sort);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            QueryRs qr = new QueryRs();
            qr.setTotal(topDocs.totalHits);
            List<Map<String, Object>> hits = new ArrayList<>();

            QueryScorer scorer = new QueryScorer(builder.build());
            Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
            SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<em>", "</em>");
            Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
            highlighter.setTextFragmenter(fragmenter);

            for (int i = min; i < scoreDocs.length; i++) {
                ScoreDoc scoreDoc = scoreDocs[i];
                int docID = scoreDoc.doc;
                Document doc = multiReader.document(docID);
                Map<String, Object> hit = new HashMap<>();
                hit.put("_id", docID);
                hit.put("_source", mapCopy(doc));
                hit.put("highlight", highlighterMapCopy(doc, builder.build(), analyzer));
                hits.add(hit);
            }
            qr.setHits(hits);
            Map<String, Object> rs = new HashMap<>();
            rs.put("hits", qr);
            rs.put("timed_out", false);
            return JSONObject.toJSONString(rs);
        }
        return null;
    }

    private Map<String, Object> highlighterMapCopy(Document paramsMap, BooleanQuery booleanQuery, SmartChineseAnalyzer analyzer) {

        QueryScorer scorer = new QueryScorer(booleanQuery);
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<em>", "</em>");
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
        highlighter.setTextFragmenter(fragmenter);
        Map<String, Object> resultMap = new HashMap<>();
        Iterator<IndexableField> it = paramsMap.iterator();
        while (it.hasNext()) {
            IndexableField entry = it.next();
            TokenStream tokenStream = analyzer.tokenStream(entry.name(), new StringReader(entry.stringValue()));
            //获取最高的片段
            try {
                resultMap.put(entry.name(), highlighter.getBestFragments(tokenStream, entry.stringValue(), 10));
            } catch (Exception e) {
                resultMap.put(entry.name(), entry.stringValue());
            }
        }
        return resultMap;
    }

    @Override
    public String get(String indexStr, String queryStr, String from, String size) throws Exception {
        JSONObject queryJson = JSON.parseObject(queryStr);
        if (queryJson.containsKey("query")) {
            return queryData(indexStr, queryStr, from, size);
        }
        if (queryJson.containsKey("aggregations")) {
            return queryGroup(indexStr, queryStr);
        }
        return null;
    }

    private String queryGroup(String indexStr, String queryStr) throws Exception {
        JSONObject queryJson = JSON.parseObject(queryStr);
        String fieldName = queryJson.getJSONObject("aggregations").getJSONObject("dataCount").getJSONObject("terms").getString("field");
        List<String> indexs = getIndex(indexStr);

        IndexReader[] indexReaders = new IndexReader[indexs.size()];
        for (int i = 0; i < indexs.size(); i++) {
            Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(this.localpath + indexs.get(i)));
            IndexReader reader = DirectoryReader.open(directory);
            indexReaders[i] = reader;
        }
        MultiReader multiReader = new MultiReader(indexReaders);
        IndexSearcher searcher = new IndexSearcher(multiReader);
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        if (builder.build().clauses().isEmpty()) {
            builder.add(new WildcardQuery(new Term("appName", "*")), BooleanClause.Occur.SHOULD);
        }
        GroupingSearch groupingSearch = new GroupingSearch(fieldName);
        groupingSearch.setGroupSort(new Sort(SortField.FIELD_SCORE));
        groupingSearch.setFillSortFields(true);
        groupingSearch.setCachingInMB(4.0, true);
        groupingSearch.setAllGroups(true);
        groupingSearch.setGroupDocsLimit(10000);

        TopGroups<BytesRef> topDocs = groupingSearch.search(searcher, builder.build(), 0, 10000);

        List<Map<String, Object>> hits = new ArrayList<>();

        for (GroupDocs<BytesRef> groupDocs : topDocs.groups) {
            String groupName = groupDocs.groupValue.utf8ToString();
            Map<String, Object> map = new HashMap<>();
            map.put("key", groupName);
            map.put("doc_count", groupDocs.totalHits);
            hits.add(map);
        }
        Map<String, Object> buckets = new HashMap<>();
        buckets.put("buckets", hits);
        Map<String, Object> dataCount = new HashMap<>();
        dataCount.put("dataCount", buckets);
        Map<String, Object> aggregations = new HashMap<>();
        aggregations.put("aggregations", dataCount);
        return JSONObject.toJSONString(aggregations);
    }

    @Override
    public String group(String indexStr, String queryStr) throws Exception {
        return queryGroup(indexStr, queryStr);
    }

    private Map<String, Object> mapCopy(Document paramsMap) {

        Map<String, Object> resultMap = new HashMap<>();
        Iterator<IndexableField> it = paramsMap.iterator();
        while (it.hasNext()) {
            IndexableField entry = it.next();
            resultMap.put(entry.name(), entry.stringValue());
        }
        return resultMap;
    }

    @Override
    public String cat(String indexStr) {
        List<String> indexs = getIndex(indexStr);
        List<Map<String, Object>> infos = new ArrayList<>();
        try {
            for (String index : indexs) {

                long size = FileUtils.sizeOfDirectory(new File(this.localpath + index));
                String sizeStr = size + " bytes";
                if (size > 1024 && size <= 1024 * 1024) {
                    sizeStr = size / 1024 + " kb";
                }
                if (size > 1024 * 1024 && size <= 1024 * 1024 * 1024) {
                    sizeStr = size / (1024 * 1024) + " M";
                }
                if (size > 1024 * 1024 * 1024) {
                    sizeStr = size / (1024 * 1024 * 1024) + " G";
                }
                Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(this.localpath + index));
                NRTCachingDirectory nrtCachingDirectory = new NRTCachingDirectory(directory, 5, 60);
                IndexReader reader = DirectoryReader.open(nrtCachingDirectory);
                IndexSearcher searcher = new IndexSearcher(reader);
                Query query = new WildcardQuery(new Term("appName", "*"));
                TopDocs topDocs = searcher.search(query, 100);

                Map<String, Object> map = new HashMap<>();
                map.put("health", "green");
                map.put("index", index);
                map.put("store.size", sizeStr);
                map.put("pri.store.size", sizeStr);
                map.put("docs.count", topDocs.totalHits);
                infos.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return GfJsonUtil.toJSONString(infos);
    }

    @Override
    public boolean creatIndice(String indice, String type) {
        return true;
    }

    @Override
    public boolean creatIndiceTrace(String indice, String type) {
        return true;
    }

    @Override
    public boolean existIndice(String indice) {
        return true;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean creatIndiceNomal(String indice, String type) {
        return true;
    }

    @Override
    public List<String> getExistIndices(String[] indices) {
        return Arrays.asList(indices.clone());
    }

    @Override
    public void close() {

    }

    @Override
    public boolean addShards(Long shardCount) {
        return false;
    }
}

