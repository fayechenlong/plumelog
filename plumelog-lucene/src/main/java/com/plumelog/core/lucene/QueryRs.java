package com.plumelog.core.lucene;

import java.util.List;
import java.util.Map;

public class QueryRs {


    private Long total;
    private List<Map<String,Object>> hits;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<Map<String, Object>> getHits() {
        return hits;
    }

    public void setHits(List<Map<String, Object>> hits) {
        this.hits = hits;
    }
}
