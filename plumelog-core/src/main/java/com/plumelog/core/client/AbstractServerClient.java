package com.plumelog.core.client;
import java.util.List;

public abstract class AbstractServerClient {
    public abstract void insertListLog(List<String> list, String baseIndex, String type) throws Exception;

    public abstract void insertListTrace(List<String> list, String baseIndex, String type) throws Exception;

    public abstract void insertListComm(List<String> list, String baseIndex, String type) throws Exception;

    public abstract boolean deleteIndex(String index);

    public abstract String get(String url, String queryStr) throws Exception;

    public abstract String get(String indexStr, String queryStr,String from,String size) throws Exception;

    public abstract String group(String indexStr, String queryStr) throws Exception;

    public abstract String cat(String index);

    public abstract boolean creatIndice(String indice, String type);

    public abstract boolean creatIndiceTrace(String indice, String type);

    public abstract boolean existIndice(String indice);

    public abstract String getVersion();

    public abstract boolean creatIndiceNomal(String indice, String type);

    public abstract List<String> getExistIndices(String[] indices);

    public abstract void close();
}
