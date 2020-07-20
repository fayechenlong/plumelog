package com.plumelog.core.dto;

import com.plumelog.core.dto.WarningRule;

/**
 * className：WarningRule
 * description：错误告警规则
 * time：2020/7/2  10:52
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class WarningRuleDto extends WarningRule {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
