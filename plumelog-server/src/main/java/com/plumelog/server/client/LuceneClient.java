package com.plumelog.server.client;

import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.server.util.GfJsonUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class LuceneClient extends AbstractServerClient {


    public void create(Collection<Document> docs, String index) throws Exception {
        Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(index));
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


    @Override
    public void insertListLog(List<String> list, String baseIndex, String type) throws Exception {
        List<RunLogMessage> messageList = GfJsonUtil.parseList(list, RunLogMessage.class);
        create(loadDocs(messageList), baseIndex);
    }

    @Override
    public void insertListTrace(List<String> list, String baseIndex, String type) throws Exception {
        List<RunLogMessage> messageList = GfJsonUtil.parseList(list, RunLogMessage.class);
        create(loadDocs(messageList), baseIndex);
    }

    @Override
    public void insertListComm(List<String> list, String baseIndex, String type) throws Exception {
        List<RunLogMessage> messageList = GfJsonUtil.parseList(list, RunLogMessage.class);
        create(loadDocs(messageList), baseIndex);
    }

    @Override
    public boolean deleteIndex(String index) {
        try {
            Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(index));
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
        return null;
    }

    @Override
    public void close() {

    }
}

