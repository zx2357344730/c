package com.cresign.purchase.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.pojo.po.Init;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author tangzejin
 * @updated 2019/8/23
 * @ver 1.0.0
 * ##description: excel工具类
 */
@Service
public class ExcelUtils {

    @Autowired
    private Qt qt;

    private static final String[] excelList = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM", "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AW", "AX", "AY", "AZ"};

    /**
     * 生成统计excel
     * @authorRachel
     * @Date 2021/10/22
     * @param arrayExcel excel内容
     * @Return java.io.File
     * @Card
     **/
    public static File statisticExcel(JSONArray arrayExcel, JSONArray fields, String writeName) throws IOException {
        long one = System.currentTimeMillis();
        //创建excel文件并命名
        File excelFile = File.createTempFile(writeName, ".xlsx");
        FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet");
        sheet.createFreezePane(0, 1, 0, 1);
        JSONObject lengths = new JSONObject();
        //获取每个字段最大长度
        JSONObject jsonCell = arrayExcel.getJSONObject(0);
        for (Map.Entry<String, Object> entry : jsonCell.entrySet()) {
            int length = jsonCell.getJSONObject(entry.getKey()).getString("wrdN").getBytes(StandardCharsets.UTF_8).length;
            if (lengths.getInteger(entry.getKey()) == null || lengths.getInteger(entry.getKey()) < length) {
                lengths.put(entry.getKey(), length);
            }
        }
        for (int i = 1; i < arrayExcel.size(); i++) {
            System.out.println("arrayExcel.i=" + arrayExcel.getJSONObject(i));
            jsonCell = arrayExcel.getJSONObject(i);
            for (Map.Entry<String, Object> entry : jsonCell.entrySet()) {
                int length = jsonCell.getString(entry.getKey()).getBytes(StandardCharsets.UTF_8).length;
                if (lengths.getInteger(entry.getKey()) == null || lengths.getInteger(entry.getKey()) < length) {
                    lengths.put(entry.getKey(), length);
                }
            }
        }
        System.out.println("lengths=" + lengths);
        XSSFCellStyle cellTitleStyle = workbook.createCellStyle();
        cellTitleStyle.setFillForegroundColor(new XSSFColor(new Color(0, 255, 0)));
//        cellTitleStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        cellTitleStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        long two = System.currentTimeMillis();
        //遍历填入Excel内容
//        if (arrayExcel.getJSONObject(0).getJSONObject("price") != null) {
//            System.out.println("price");
//            fields.add("count");
//            fields.add("price");
//            fields.add("sum");
//        } else {
//            System.out.println("null");
//            fields.add("count");
//        }
        for (int i = 0; i < arrayExcel.size(); i++) {
            XSSFRow row = sheet.createRow(i);
            jsonCell = arrayExcel.getJSONObject(i);
            System.out.println("jsonCell=" + jsonCell);
            for (int j = 0; j < fields.size(); j++) {
                XSSFCell cell = row.createCell(j);
//                if (j == fields.size()) {
//                    if (i == 0) {
//                        cell.setCellValue("公式");
//                    } else {
////                        cell.setCellFormula("PRODUCT(F" + (i + 1) + ":G" + (i + 1) + ")");
//                        cell.setCellFormula("F" + (i + 1) + "*G" + (i + 1));
//                    }
//                } else {
                    if (i == 0) {
                        sheet.setColumnWidth(j, lengths.getInteger(fields.getString(j)) * 256 + 200);
                        cell.setCellStyle(cellTitleStyle);
                        cell.setCellValue(jsonCell.getJSONObject(fields.getString(j)).getString("wrdN"));
                    } else {
                        String type = arrayExcel.getJSONObject(0).getJSONObject(fields.getString(j)).getString("type");
                        switch (type) {
                            case "String":
                                cell.setCellValue(jsonCell.getString(fields.getString(j)));
                                break;
                            case "Integer":
                                cell.setCellValue(jsonCell.getInteger(fields.getString(j)));
                                break;
                            case "Double":
                                cell.setCellValue(jsonCell.getDouble(fields.getString(j)));
                                break;
                        }
                    }
//                }
            }
        }
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        long three = System.currentTimeMillis();
        System.out.println("settime=" + (two - one) + "ms");
        System.out.println("write=" + (three - two) + "ms");
        return excelFile;
    }

    public static File createExcel(JSONArray arrayField, JSONArray arrayExcel, JSONArray arrayRowField, JSONArray arrayListField) throws IOException {
        File excelFile = File.createTempFile("excel", ".xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet");
        workbook = statExcel(workbook, sheet, arrayExcel, arrayField, arrayRowField, arrayListField);
        FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        return excelFile;
    }

    public static File createExcel2(JSONArray arrayField, JSONArray arrayExcel, JSONArray arrayRowField, JSONArray arrayListField) throws IOException {
        File excelFile = File.createTempFile("excel", ".xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet");
        workbook = statExcel2(workbook, sheet, arrayExcel, arrayField, arrayRowField, arrayListField);
        FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        return excelFile;
    }

    public static File createSheet(File excelFile, JSONArray arrayExcel, JSONArray arrayField, JSONArray arrayRowField, JSONArray arrayListField, String sheetName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(excelFile);
        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
        XSSFSheet sheet = workbook.createSheet(sheetName);
        workbook = statExcel(workbook, sheet, arrayExcel, arrayField, arrayRowField, arrayListField);
        FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        return excelFile;
    }

    public static File updateSheet(File excelFile, JSONArray arrayExcel, JSONArray arrayField, JSONArray arrayRowField, JSONArray arrayListField, Integer sheetIndex) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(excelFile);
        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
        XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
        workbook = statExcel(workbook, sheet, arrayExcel, arrayField, arrayRowField, arrayListField);
        FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        return excelFile;
    }

    //rowField[{"field":"PRODUCT(##{6},##{7})","wrdN":"总价"}]
    //listField[{"index":6,"type":"SUM"}]
    private static XSSFWorkbook statExcel(XSSFWorkbook workbook, XSSFSheet sheet, JSONArray arrayExcel, JSONArray arrayField, JSONArray arrayRowField, JSONArray arrayListField) throws IOException {
        sheet.createFreezePane(0, 1, 0, 1);
        //获取每个字段最大长度
//        for (int i = 0; i < arrayField.size(); i++) {
//            JSONObject jsonField = arrayField.getJSONObject(i);
//            if (jsonField.getBoolean("isEx")) {
//                int length = jsonField.getString("txt").getBytes(StandardCharsets.UTF_8).length;
//                jsonField.put("length", length);
//            }
//        }
//        System.out.println("arrayField=" + arrayField);
//        for (int i = 0; i < arrayExcel.size(); i++) {
//            JSONArray arrayRow = arrayExcel.getJSONArray(i);
//            for (int j = 0; j < arrayField.size(); j++) {
//                JSONObject jsonField = arrayField.getJSONObject(j);
//                if (jsonField.getBoolean("isEx")) {
//                    int length = arrayRow.getString(j).getBytes(StandardCharsets.UTF_8).length;
//                    jsonField.put("length", length);
//                }
//            }
//
//            for (int j = 0; j < arrayRow.size(); j++) {
//                int length = arrayRow.getString(j).getBytes(StandardCharsets.UTF_8).length;
//                if (arrayField.getJSONObject(j).getInteger("length") < length) {
//                    arrayField.getJSONObject(j).put("length", length);
//                }
//            }
//        }
        System.out.println("arrayField=" + arrayField);
        //设置样式
        //表头
        XSSFCellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFillForegroundColor(new XSSFColor(new Color(0, 255, 0)));
//        cellTitleStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        titleStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        //换行居左
        XSSFCellStyle warpLeftStyle = workbook.createCellStyle();
        warpLeftStyle.setWrapText(true);
        warpLeftStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        warpLeftStyle.setAlignment(HorizontalAlignment.LEFT);
        //换行居中
        XSSFCellStyle warpCenterStyle = workbook.createCellStyle();
        warpCenterStyle.setWrapText(true);
        warpCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        warpCenterStyle.setAlignment(HorizontalAlignment.CENTER);
        //换行居右
        XSSFCellStyle warpRightStyle = workbook.createCellStyle();
        warpRightStyle.setWrapText(true);
        warpRightStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        warpRightStyle.setAlignment(HorizontalAlignment.RIGHT);
        //居左
        XSSFCellStyle leftStyle = workbook.createCellStyle();
        leftStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        leftStyle.setAlignment(HorizontalAlignment.LEFT);
        //居中
        XSSFCellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        //居右
        XSSFCellStyle rightStyle = workbook.createCellStyle();
        rightStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        rightStyle.setAlignment(HorizontalAlignment.RIGHT);
        //遍历填入Excel内容
        XSSFRow title = sheet.createRow(0);
        int length = 0;
        for (int i = 0; i < arrayField.size(); i++) {
            JSONObject jsonField = arrayField.getJSONObject(i);
            if (jsonField.getBoolean("isEx") != null && jsonField.getBoolean("isEx")) {
                XSSFCell cell = title.createCell(length);
                cell.setCellStyle(titleStyle);
                cell.setCellValue(jsonField.getString("txt"));
                length ++;
            }
        }
        for (int i = 0; i < arrayRowField.size(); i++) {
            JSONObject jsonField = arrayRowField.getJSONObject(i);
            XSSFCell cell = title.createCell(i + length);
            cell.setCellStyle(titleStyle);
            cell.setCellValue(jsonField.getString("txt"));
        }
        for (int i = 0; i < arrayExcel.size(); i++) {
            JSONObject jsonExcel = arrayExcel.getJSONObject(i);
            XSSFRow row = sheet.createRow(i + 1);
            length = 0;
            for (int j = 0; j < arrayField.size(); j++) {
                JSONObject jsonField = arrayField.getJSONObject(j);
                if (jsonField.getBoolean("isEx") != null && jsonField.getBoolean("isEx")) {
                    String field = jsonField.getString("field");
                    String valType = jsonField.getString("valType");
                    Boolean isWarp = jsonField.getBoolean("isWarp");
                    String align = jsonField.getString("align");
                    XSSFCell cell = row.createCell(length);
                    if (isWarp != null && isWarp) {
                        if (align == null || align.equals("left")) {
                            cell.setCellStyle(warpLeftStyle);
                        } else if (align.equals("center")) {
                            cell.setCellStyle(warpCenterStyle);
                        } else if (align.equals("right")) {
                            cell.setCellStyle(warpRightStyle);
                        }
                    } else {
                        if (align == null || align.equals("left")) {
                            cell.setCellStyle(leftStyle);
                        } else if (align.equals("center")) {
                            cell.setCellStyle(centerStyle);
                        } else if (align.equals("right")) {
                            cell.setCellStyle(rightStyle);
                        }
                    }
                    System.out.println(jsonField.getString("txt") + ":" + valType);
                    if (jsonExcel.get(field).equals(""))
                    {
                        cell.setCellValue("");
                    } else if (valType.equals("String") || valType.equals("lang") || valType.equals("grp") || valType.equals("list")) {
                        cell.setCellValue(jsonExcel.getString(field));
                    } else if (valType.equals("Integer")) {
                        cell.setCellValue(jsonExcel.getInteger(field));
                    } else if (valType.equals("Double") ) {
                        cell.setCellValue(jsonExcel.getDouble(field));
                    }
                    length ++;
                }
            }
            for (int j = 0; j < arrayRowField.size(); j++) {
                JSONObject jsonField = arrayRowField.getJSONObject(j);
                XSSFCell cell = row.createCell(j + length);
                String field = jsonField.getString("field");
                Boolean isWarp = jsonField.getBoolean("isWarp");
                String align = jsonField.getString("align");
                if (isWarp != null && isWarp) {
                    if (align == null || align.equals("left")) {
                        cell.setCellStyle(warpLeftStyle);
                    } else if (align.equals("center")) {
                        cell.setCellStyle(warpCenterStyle);
                    } else if (align.equals("right")) {
                        cell.setCellStyle(warpRightStyle);
                    }
                } else {
                    if (align == null || align.equals("left")) {
                        cell.setCellStyle(leftStyle);
                    } else if (align.equals("center")) {
                        cell.setCellStyle(centerStyle);
                    } else if (align.equals("right")) {
                        cell.setCellStyle(rightStyle);
                    }
                }
                String[] fieldSplits = field.split("##\\{");
                StringBuffer val = new StringBuffer();
                for (int k = 0; k < fieldSplits.length; k++) {
                    String fieldSplit = fieldSplits[k];
                    String[] rowIndex = fieldSplit.split("\\}");
                    if (rowIndex.length == 2) {
                        String rowRef = excelList[Integer.parseInt(rowIndex[0])];
                        val.append(rowRef).append(i + 1).append(rowIndex[1]);
                    } else {
                        val.append(fieldSplit);
                    }
                }
                cell.setCellFormula(val.toString());
            }
        }
        for (int i = 0; i < arrayListField.size(); i++) {
            JSONObject jsonField = arrayListField.getJSONObject(i);
            Integer index = jsonField.getInteger("index");
            XSSFRow row = sheet.createRow(i + arrayExcel.size());
            XSSFCell cell = row.createCell(index);
            String rowRef = excelList[index];
            StringBuffer val = new StringBuffer(jsonField.getString("type")).append("(").append(rowRef).append(2).append(":")
                    .append(rowRef).append(arrayExcel.size() - 1).append(")");
            cell.setCellFormula(val.toString());
        }
        length = 0;
        for (int i = 0; i < arrayField.size(); i++) {
            JSONObject jsonField = arrayField.getJSONObject(i);
            if (jsonField.getBoolean("isEx") != null && jsonField.getBoolean("isEx")) {
                Integer maxWidth = jsonField.getInteger("maxWidth");
                sheet.autoSizeColumn(length);
                if (maxWidth != null && sheet.getColumnWidth(i) > maxWidth) {
                    sheet.setColumnWidth(i, maxWidth);
                }
                length ++;
            }
        }
        for (int i = 0; i < arrayRowField.size(); i++) {
            JSONObject jsonField = arrayRowField.getJSONObject(i);
            Integer maxWidth = jsonField.getInteger("maxWidth");
            int index = i + length;
            sheet.autoSizeColumn(index);
            if (maxWidth != null && sheet.getColumnWidth(index) > maxWidth) {
                sheet.setColumnWidth(index, maxWidth);
            }
        }
        return workbook;
    }

    private static XSSFWorkbook statExcel2(XSSFWorkbook workbook, XSSFSheet sheet, JSONArray arrayExcel, JSONArray arrayField, JSONArray arrayRowField, JSONArray arrayListField) throws IOException {
        sheet.createFreezePane(0, 1, 0, 1);
        //获取每个字段最大长度
//        for (int i = 0; i < arrayField.size(); i++) {
//            JSONObject jsonField = arrayField.getJSONObject(i);
//            if (jsonField.getBoolean("isEx")) {
//                int length = jsonField.getString("txt").getBytes(StandardCharsets.UTF_8).length;
//                jsonField.put("length", length);
//            }
//        }
//        System.out.println("arrayField=" + arrayField);
//        for (int i = 0; i < arrayExcel.size(); i++) {
//            JSONArray arrayRow = arrayExcel.getJSONArray(i);
//            for (int j = 0; j < arrayField.size(); j++) {
//                JSONObject jsonField = arrayField.getJSONObject(j);
//                if (jsonField.getBoolean("isEx")) {
//                    int length = arrayRow.getString(j).getBytes(StandardCharsets.UTF_8).length;
//                    jsonField.put("length", length);
//                }
//            }
//
//            for (int j = 0; j < arrayRow.size(); j++) {
//                int length = arrayRow.getString(j).getBytes(StandardCharsets.UTF_8).length;
//                if (arrayField.getJSONObject(j).getInteger("length") < length) {
//                    arrayField.getJSONObject(j).put("length", length);
//                }
//            }
//        }
        System.out.println("arrayField=" + arrayField);
        //设置样式
        //表头
        XSSFCellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFillForegroundColor(new XSSFColor(new Color(0, 255, 0)));
//        cellTitleStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        titleStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        //换行居左
        XSSFCellStyle warpLeftStyle = workbook.createCellStyle();
        warpLeftStyle.setWrapText(true);
        warpLeftStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        warpLeftStyle.setAlignment(HorizontalAlignment.LEFT);
        //换行居中
        XSSFCellStyle warpCenterStyle = workbook.createCellStyle();
        warpCenterStyle.setWrapText(true);
        warpCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        warpCenterStyle.setAlignment(HorizontalAlignment.CENTER);
        //换行居右
        XSSFCellStyle warpRightStyle = workbook.createCellStyle();
        warpRightStyle.setWrapText(true);
        warpRightStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        warpRightStyle.setAlignment(HorizontalAlignment.RIGHT);
        //居左
        XSSFCellStyle leftStyle = workbook.createCellStyle();
        leftStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        leftStyle.setAlignment(HorizontalAlignment.LEFT);
        //居中
        XSSFCellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        //居右
        XSSFCellStyle rightStyle = workbook.createCellStyle();
        rightStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        rightStyle.setAlignment(HorizontalAlignment.RIGHT);
        //遍历填入Excel内容
        XSSFRow title = sheet.createRow(0);
        int length = 0;
        for (int i = 0; i < arrayField.size(); i++) {
            JSONObject jsonField = arrayField.getJSONObject(i);
            if (jsonField.getBoolean("isEx") != null && jsonField.getBoolean("isEx")) {
                XSSFCell cell = title.createCell(length);
                cell.setCellStyle(titleStyle);
                cell.setCellValue(jsonField.getString("txt"));
                length ++;
            }
        }
        for (int i = 0; i < arrayRowField.size(); i++) {
            JSONObject jsonField = arrayRowField.getJSONObject(i);
            XSSFCell cell = title.createCell(i + length);
            cell.setCellStyle(titleStyle);
            cell.setCellValue(jsonField.getString("txt"));
        }
        for (int i = 0; i < arrayExcel.size(); i++) {
            JSONArray arrayRow = arrayExcel.getJSONArray(i);
            XSSFRow row = sheet.createRow(i + 1);
            length = 0;
            for (int j = 0; j < arrayField.size(); j++) {
                JSONObject jsonField = arrayField.getJSONObject(j);
                if (jsonField.getBoolean("isEx") != null && jsonField.getBoolean("isEx")) {
                    String valType = jsonField.getString("valType");
                    Boolean isWarp = jsonField.getBoolean("isWarp");
                    String align = jsonField.getString("align");
                    XSSFCell cell = row.createCell(length);
                    if (isWarp != null && isWarp) {
                        if (align == null || align.equals("left")) {
                            cell.setCellStyle(warpLeftStyle);
                        } else if (align.equals("center")) {
                            cell.setCellStyle(warpCenterStyle);
                        } else if (align.equals("right")) {
                            cell.setCellStyle(warpRightStyle);
                        }
                    } else {
                        if (align == null || align.equals("left")) {
                            cell.setCellStyle(leftStyle);
                        } else if (align.equals("center")) {
                            cell.setCellStyle(centerStyle);
                        } else if (align.equals("right")) {
                            cell.setCellStyle(rightStyle);
                        }
                    }
                    System.out.println(jsonField.getString("txt") + ":" + valType);
                    if (arrayRow.get(j).equals(""))
                    {
                        cell.setCellValue("");
                    } else if (valType.equals("String") || valType.equals("lang")
                            || valType.equals("grp") || valType.equals("list")
                            || valType.equals("logData") || valType.equals("chkInData")) {
                        cell.setCellValue(arrayRow.getString(j));
                    } else if (valType.equals("Integer")) {
                        cell.setCellValue(arrayRow.getInteger(j));
                    } else if (valType.equals("Double") ) {
                        cell.setCellValue(arrayRow.getDouble(j));
                    }
                    length ++;
                }
            }
            for (int j = 0; j < arrayRowField.size(); j++) {
                JSONObject jsonField = arrayRowField.getJSONObject(j);
                XSSFCell cell = row.createCell(j + length);
                String field = jsonField.getString("field");
                Boolean isWarp = jsonField.getBoolean("isWarp");
                String align = jsonField.getString("align");
                if (isWarp != null && isWarp) {
                    if (align == null || align.equals("left")) {
                        cell.setCellStyle(warpLeftStyle);
                    } else if (align.equals("center")) {
                        cell.setCellStyle(warpCenterStyle);
                    } else if (align.equals("right")) {
                        cell.setCellStyle(warpRightStyle);
                    }
                } else {
                    if (align == null || align.equals("left")) {
                        cell.setCellStyle(leftStyle);
                    } else if (align.equals("center")) {
                        cell.setCellStyle(centerStyle);
                    } else if (align.equals("right")) {
                        cell.setCellStyle(rightStyle);
                    }
                }
                String[] fieldSplits = field.split("##\\{");
                StringBuffer val = new StringBuffer();
                for (int k = 0; k < fieldSplits.length; k++) {
                    String fieldSplit = fieldSplits[k];
                    String[] rowIndex = fieldSplit.split("\\}");
                    if (rowIndex.length == 2) {
                        String rowRef = excelList[Integer.parseInt(rowIndex[0])];
                        val.append(rowRef).append(i + 1).append(rowIndex[1]);
                    } else {
                        val.append(fieldSplit);
                    }
                }
                cell.setCellFormula(val.toString());
            }
        }
        for (int i = 0; i < arrayListField.size(); i++) {
            JSONObject jsonField = arrayListField.getJSONObject(i);
            Integer index = jsonField.getInteger("index");
            XSSFRow row = sheet.createRow(i + arrayExcel.size());
            XSSFCell cell = row.createCell(index);
            String rowRef = excelList[index];
            StringBuffer val = new StringBuffer(jsonField.getString("type")).append("(").append(rowRef).append(2).append(":")
                    .append(rowRef).append(arrayExcel.size() - 1).append(")");
            cell.setCellFormula(val.toString());
        }
        length = 0;
        for (int i = 0; i < arrayField.size(); i++) {
            JSONObject jsonField = arrayField.getJSONObject(i);
            if (jsonField.getBoolean("isEx") != null && jsonField.getBoolean("isEx")) {
                Integer maxWidth = jsonField.getInteger("maxWidth");
                sheet.autoSizeColumn(i);
                if (maxWidth != null && sheet.getColumnWidth(i) > maxWidth) {
                    sheet.setColumnWidth(i, maxWidth);
                }
                length ++;
            }
        }
        for (int i = 0; i < arrayRowField.size(); i++) {
            JSONObject jsonField = arrayRowField.getJSONObject(i);
            Integer maxWidth = jsonField.getInteger("maxWidth");
            int index = i + arrayField.size();
            sheet.autoSizeColumn(index);
            if (maxWidth != null && sheet.getColumnWidth(index) > maxWidth) {
                sheet.setColumnWidth(index, maxWidth);
            }
        }
        return workbook;
    }

    public static File pivotTable(File excelFile, Integer sheetIndex) throws IOException {

        FileInputStream fileInputStream = new FileInputStream(excelFile);
        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
        XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
        System.out.println("sheet=" + sheet);
        int lastRowIndex = sheet.getLastRowNum();
        System.out.println("lastRowIndex=" + lastRowIndex);
        short lastCellIndex = sheet.getRow(0).getLastCellNum();
        System.out.println("lastCellIndex=" + lastCellIndex);
        //数据位置
//        AreaReference areaReference = new AreaReference("A1:" + excelList[lastCellIndex - 1] + lastRowIndex);
        AreaReference areaReference = new AreaReference("A1:H44");
        //数据透视表生成位置
//        CellReference cellReference = new CellReference(excelList[lastCellIndex + 2] + 2);
        XSSFSheet sheet2 = workbook.getSheetAt(sheetIndex + 1);
        CellReference cellReference = new CellReference("A1");
        //生成数据透视图
        XSSFPivotTable pivotTable = sheet2.createPivotTable(areaReference, cellReference);
        for (int i = 0; i < lastCellIndex; i++) {
            pivotTable.addRowLabel(i);
        }
        FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        return excelFile;
    }

    public static JSONArray readExcel(File excelFile, JSONArray arrayField) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(excelFile);
        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);
        XSSFRow title = sheet.getRow(0);
        for (int i = 0; i < arrayField.size(); i++) {
            JSONObject jsonField = arrayField.getJSONObject(i);
            String field = jsonField.getString("field");
            for (int j = 0; j < title.getLastCellNum(); j++) {
                if (field.equals(title.getCell(j).getStringCellValue())) {
                    jsonField.put("index", j);
                    break;
                }
            }
        }
        JSONArray arrayResult = new JSONArray();
        for (int i = 1; i < sheet.getLastRowNum(); i++) {
            XSSFRow row = sheet.getRow(i);
            JSONArray arrayRow = new JSONArray();
            for (int j = 0; j < arrayField.size(); j++) {
                JSONObject jsonField = arrayField.getJSONObject(j);
                Integer index = jsonField.getInteger("index");
                String valType = jsonField.getString("valType");
                if (index == null) {
                    if (valType.equals("String") || valType.equals("lang") || valType.equals("grp") || valType.equals("list")) {
                        arrayRow.add("");
                    } else if (valType.equals("Integer")) {
                        arrayRow.add(0);
                    } else if (valType.equals("Double")) {
                        arrayRow.add(0.0);
                    }
                } else {
                    XSSFCell cell = row.getCell(index);
                   if (valType.equals("String") || valType.equals("lang") || valType.equals("grp") || valType.equals("list")) {
                        arrayRow.add(cell.getStringCellValue());
                    } else if (valType.equals("Integer")) {
                       arrayRow.add((int) cell.getNumericCellValue());
                    } else if (valType.equals("Double")) {
                       arrayRow.add(cell.getNumericCellValue());
                    }
                }
            }
            arrayResult.add(arrayRow);
        }
        return arrayResult;
    }
//    public static File statExcel(JSONArray arrayStatistic, JSONArray fields, String writeName) throws IOException {
//        long one = System.currentTimeMillis();
//        //创建excel文件并命名
//        File excelFile = File.createTempFile(writeName, ".xlsx");
//        FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
//        XSSFWorkbook workbook = new XSSFWorkbook();
//        XSSFSheet sheet = workbook.createSheet("sheet");
//        sheet.createFreezePane(0, 1, 0, 1);
//
//        JSONObject jsonTitel = new JSONObject();
//        JSONObject jsonCell = arrayStatistic.getJSONObject(0);
//        jsonCell.forEach((k, v) ->{
//
//        });
//        //获取每个字段最大长度
//        JSONObject lengths = new JSONObject();
//        for (Map.Entry<String, Object> entry : jsonCell.entrySet()) {
//            int length = jsonCell.getJSONObject(entry.getKey()).getString("wrdN").getBytes(StandardCharsets.UTF_8).length;
//            if (lengths.getInteger(entry.getKey()) == null || lengths.getInteger(entry.getKey()) < length) {
//                lengths.put(entry.getKey(), length);
//            }
//        }
//        for (int i = 1; i < arrayStatistic.size(); i++) {
//            System.out.println("arrayStatistic.i=" + arrayStatistic.getJSONObject(i));
//            jsonCell = arrayStatistic.getJSONObject(i);
//            for (Map.Entry<String, Object> entry : jsonCell.entrySet()) {
//                int length = jsonCell.getString(entry.getKey()).getBytes(StandardCharsets.UTF_8).length;
//                if (lengths.getInteger(entry.getKey()) == null || lengths.getInteger(entry.getKey()) < length) {
//                    lengths.put(entry.getKey(), length);
//                }
//            }
//        }
//        System.out.println("lengths=" + lengths);
//        XSSFCellStyle cellTitleStyle = workbook.createCellStyle();
//        cellTitleStyle.setFillForegroundColor(new XSSFColor(new Color(0, 255, 0)));
////        cellTitleStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
//        cellTitleStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
//        long two = System.currentTimeMillis();
//        //遍历填入Excel内容
////        if (arrayStatistic.getJSONObject(0).getJSONObject("price") != null) {
////            System.out.println("price");
////            fields.add("count");
////            fields.add("price");
////            fields.add("sum");
////        } else {
////            System.out.println("null");
////            fields.add("count");
////        }
//        for (int i = 0; i < arrayStatistic.size(); i++) {
//            XSSFRow row = sheet.createRow(i);
//            jsonCell = arrayStatistic.getJSONObject(i);
//            System.out.println("jsonCell=" + jsonCell);
//            for (int j = 0; j < fields.size(); j++) {
//                XSSFCell cell = row.createCell(j);
////                if (j == fields.size()) {
////                    if (i == 0) {
////                        cell.setCellValue("公式");
////                    } else {
//////                        cell.setCellFormula("PRODUCT(F" + (i + 1) + ":G" + (i + 1) + ")");
////                        cell.setCellFormula("F" + (i + 1) + "*G" + (i + 1));
////                    }
////                } else {
//                if (i == 0) {
//                    sheet.setColumnWidth(j, lengths.getInteger(fields.getString(j)) * 256 + 200);
//                    cell.setCellStyle(cellTitleStyle);
//                    cell.setCellValue(jsonCell.getJSONObject(fields.getString(j)).getString("wrdN"));
//                } else {
//                    String type = arrayStatistic.getJSONObject(0).getJSONObject(fields.getString(j)).getString("type");
//                    switch (type) {
//                        case "String":
//                            cell.setCellValue(jsonCell.getString(fields.getString(j)));
//                            break;
//                        case "Integer":
//                            cell.setCellValue(jsonCell.getInteger(fields.getString(j)));
//                            break;
//                        case "Double":
//                            cell.setCellValue(jsonCell.getDouble(fields.getString(j)));
//                            break;
//                    }
//                }
////                }
//            }
//        }
//        workbook.write(fileOutputStream);
//        fileOutputStream.close();
//        long three = System.currentTimeMillis();
//        System.out.println("settime=" + (two - one) + "ms");
//        System.out.println("write=" + (three - two) + "ms");
//        return excelFile;
//    }
}