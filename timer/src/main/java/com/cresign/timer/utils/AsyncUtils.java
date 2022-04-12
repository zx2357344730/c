package com.cresign.timer.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.timer.enumeration.TimerEnum;
import com.cresign.timer.service.StatService;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;

@Component
public class AsyncUtils {

    @Autowired
    private StatService statService;

    @Autowired
    private DbUtils dbUtils;

    @Async
    public void setSumm00s(String id_C, String id_A, Integer index, JSONObject jsonObjData) throws IOException, ParseException {
        JSONObject jsonTermField = jsonObjData.getJSONObject("termField");
        JSONObject jsonRangeField = jsonObjData.getJSONObject("rangeField");
        JSONObject jsonSecond = jsonObjData.getJSONObject("second");
        JSONArray arrayExcelField = jsonObjData.getJSONArray("excelField");
        String logType = jsonObjData.getString("logType");
        JSONArray arrayStatField = jsonObjData.getJSONArray("statField");
        JSONArray arrayStat = statService.getStatArrayByEs(jsonTermField, jsonRangeField, jsonSecond, arrayExcelField, logType, arrayStatField);
        JSONObject jsonHit = new JSONObject();
        jsonHit.put("id_C", id_C);
        if (arrayStat.size() > 1000) {
            jsonHit.put("zcndesc", "统计失败，统计数据大于1000条");
            dbUtils.addES(jsonHit, "msg");
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, TimerEnum.STAT_LENGTH_GT.getCode(), null);
        }
        JSONObject jsonStatResult = statService.statFilter(id_C, arrayExcelField, logType, 0, arrayStat);
        System.out.println(jsonStatResult.getJSONObject("jsonStatTitle"));
        System.out.println(jsonStatResult.getJSONArray("arrayStat"));
        JSONObject jsonUpdate = new JSONObject();
        String path = "summ00s.objData." + index;
        jsonUpdate.put(path + ".isUpdate", false);
        jsonUpdate.put(path + ".statTitle", jsonStatResult.getJSONObject("jsonStatTitle"));
        jsonUpdate.put(path + ".statContent", jsonStatResult.getJSONArray("arrayStat"));
        dbUtils.setMongoValues(id_A, jsonUpdate, Asset.class);
        jsonHit.put("zcndesc", "统计完成");
        dbUtils.addES(jsonHit, "msg");
    }
}
