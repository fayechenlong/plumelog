package com.beeplay.easylog.ui.util;

import com.beeplay.easylog.core.dto.TraceLogMessage;
import com.beeplay.easylog.ui.dto.TraceLog;

import java.util.List;

/**
 * @ClassName LogUtil
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/18 18:54
 * @Version 1.0
 **/
public class LogUtil {

    private int zIndex=0;
    private List<TraceLog> _list;

    public List<TraceLog> formartTrace(List<TraceLogMessage> list){

        for(int i=0;i<list.size();i++){
            //如果postion是 '<' 说明是上一个方法的子方法
            if(list.get(i).getPosition().equals("<")){
                pushItem(list.get(i),true);
                zIndex++;

            }
            else if(list.get(i).getPosition().equals(">"))
            {
                zIndex--;
                pushItem(list.get(i),false);
            }
        }
        return _list;
    }
    private void pushItem(TraceLogMessage item,boolean isStart){

        List<TraceLog> _arrary = _list;
        for(int i=0;i<zIndex;i++){
            _arrary = _arrary.get(_arrary.size()-1).getChildren();
        }
        if(isStart){
            TraceLog tl=new TraceLog();
            tl.setAppName(item.getAppName());
            tl.setMethod(item.getMethod());
            tl.setStartTime(item.getTime());
            tl.setzIndex(zIndex);
        }else {
            for(int f=0;f<_arrary.size();f++){
                if(_arrary.get(f).getEndTime()!=null){
                    _arrary.get(f).setEndTime(item.getTime());
                    break;
                }
            }
        }
    }
}
