package com.cresign.timer.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.common.Constants;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * ##author: tangzejin
 * ##updated: 2019/8/23
 * ##version: 1.0.0
 * ##description: excel工具类
 */
@Service
public class ExcelUtils {

    /**
     * 生成excel表格
     * ##Params: excelData	表格数据
     * ##Params: path	路径
     * ##Params: name	名称
     * ##return: java.lang.String  返回结果: 返回上传到对象存储的数据
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:29
     * ##exception:    捕捉所有异常
     */
    static String generateExcel(ExcelData excelData, String path, String name) throws Exception {
        //设置excel文件名和后缀
        final File excelFile = File.createTempFile("pdfFile-"+System.currentTimeMillis(), Constants.EXCEL_X_SUFFIX);
        FileOutputStream out = new FileOutputStream(excelFile);

        //把数据和excel文件传进去
        exportExcel(excelData, out);

        // 调用腾讯云工具上传文件
        // 并返回url给前端
        String url = CosUpload.uploadPE(excelFile, path, name, 0, null);

        //程序结束时，删除临时文件
        CosUpload.deleteFile(excelFile);

        return url;
    }

    /**
     * 生成excel表格2
     * ##Params: data	表数据
     * ##Params: out	表文件
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:30
     * ##exception:    捕捉所有异常
     */
    @SuppressWarnings("unchecked")
    private static void exportExcel(ExcelData data, OutputStream out) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        int end = -1;
        int start = 0;
        try {
            XSSFSheet sheet = wb.createSheet(Constants.STRING_ALL);
            List<Map<String, Object>> dataKey = data.getDataKey();
            int ji = 0;
            for (int i = 0; i < data.getExcelFormat().size(); i++) {
                Map<String, Object> map = dataKey.get(i);
                switch (data.getExcelFormat().get(i)){
                    case "mergeTitleFront":
                        if (null != map) {
                            if (null != map.get(Constants.GET_NAME)) {
                                if (null != map.get(Constants.GET_MERGE_FORM)) {
                                    int occupyRow = GenericUtils.objToInteger(map.get(Constants.GET_OCCUPY_ROW));
                                    int fontSize = GenericUtils.objToInteger(map.get(Constants.GET_FONT_SIZE));
                                    if (ji > 0) {
                                        start += end + Constants.INT_ONE;
                                    }
                                    ji++;
                                    boolean background = (boolean) map.get(Constants.GET_BACKGROUND);
                                    String mergeForm = map.get(Constants.GET_MERGE_FORM).toString();
                                    end += occupyRow;
                                    boolean isBold = (boolean) map.get(Constants.GET_IS_BOLD);
                                    if (Constants.STRING_ALL.equals(mergeForm)) {
                                        toTitle(wb,(short) (data.getTitles().size() - Constants.INT_ONE)
                                                ,sheet, background, start,end
                                                ,map.get(Constants.GET_NAME).toString(),isBold,fontSize);
                                    } else {
                                        int s = GenericUtils.objToInteger(mergeForm);
                                        toTitle(wb,(short) s
                                                ,sheet, background,start,end
                                                ,map.get(Constants.GET_NAME).toString()
                                                ,isBold,fontSize);
                                    }
                                }
                            }
                        }
                        break;
                    case "formBody":
                        end += Constants.INT_ONE;
                        start += Constants.INT_ONE;
                        boolean isFixed = false;
                        List<Integer> pagingRow = null;
                        if (null != map) {
                            if (null != map.get(Constants.GET_IS_FIXED)) {
                                isFixed = (boolean) map.get(Constants.GET_IS_FIXED);
                            }
                            if (null != map.get(Constants.GET_PAGING_ROW)) {
                                pagingRow = (List<Integer>) map.get(Constants.GET_PAGING_ROW);
                            }
                        }
                        writeExcel(wb, sheet, data, end, isFixed,pagingRow);
                        start += data.getRows().size();
                        end += data.getRows().size();
                        break;
                    case "mergeTitleAfter":
                        if (null != map) {
                            if (null != map.get(Constants.GET_NAME)) {
                                if (null != map.get(Constants.GET_MERGE_FORM)) {
                                    boolean background = (boolean) map.get(Constants.GET_BACKGROUND);
                                    int occupyRow = GenericUtils.objToInteger(map.get(Constants.GET_OCCUPY_ROW));
                                    int fontSize = GenericUtils.objToInteger(map.get(Constants.GET_FONT_SIZE));
                                    start = end + Constants.INT_ONE;
                                    String mergeForm = map.get(Constants.GET_MERGE_FORM).toString();
                                    end += occupyRow;
                                    boolean isBold = (boolean) map.get(Constants.GET_IS_BOLD);
                                    if (Constants.STRING_ALL.equals(mergeForm)) {
                                        toTitle(wb,(short) (data.getTitles().size() - Constants.INT_ONE)
                                                ,sheet, background, start,end
                                                ,map.get(Constants.GET_NAME).toString(),isBold,fontSize);
                                    } else {
                                        int s = GenericUtils.objToInteger(mergeForm);
                                        toTitle(wb,(short) s
                                                ,sheet, background,start,end
                                                ,map.get(Constants.GET_NAME).toString(),isBold,fontSize);
                                    }
                                }
                            }
                        }
                        break;
                    default:
            break;
        }
    }

            wb.write(out);
} catch (Exception e) {
        e.printStackTrace();
        } finally {
            //此处需要关闭 wb 变量
            out.close();
        }
    }

    /**
     * 表显示字段
     * ##Params: wb	表格生成需要对象
     * ##Params: sheet	表格生成需要对象
     * ##Params: data	表格数据
     * ##Params: rowIndex	开始位置
     * ##Params: isFinxd	判断是否固定头部
     * ##Params: pagingRow 分页行
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:30
     */
    private static void writeExcel(XSSFWorkbook wb, Sheet sheet, ExcelData data
            ,int rowIndex,boolean isFinxd,List<Integer> pagingRow) {
        rowIndex = writeTitlesToExcel(rowIndex,wb, sheet, data.getTitles(),isFinxd);
        writeRowsToExcel(wb, sheet, data.getRows(), rowIndex, pagingRow);
        autoSizeColumns(sheet, data.getTitles().size() + 1);
    }

    /**
     * 处理表格头部数据
     * ##Params: wb	表格生成需要对象
     * ##Params: shu	终止列
     * ##Params: sheet	表格生成需要对象
     * ##Params: isBackground	判断
     * ##Params: start	起始行
     * ##Params: end	终止行
     * ##Params: mergeTitle	头部数据
     * ##Params: isBold	是否需要边框
     * ##Params: fontSize	字体大小
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:32
     */
    private static void toTitle(XSSFWorkbook wb,short shu,XSSFSheet sheet
            ,boolean isBackground,int start,int end,String mergeTitle
            ,boolean isBold, int fontSize){
        Font titleFont = wb.createFont();
        //设置字体
        titleFont.setFontName("simsun");
        if (isBold) {
            //设置粗体
            titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        }
        //设置字号
        titleFont.setFontHeightInPoints((short) fontSize);
        XSSFCellStyle titleStyle = wb.createCellStyle();
        //水平居中
        titleStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        //垂直居中
        titleStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        if (isBackground) {
            //设置图案颜色
            titleStyle.setFillForegroundColor(new XSSFColor(new Color(182, 184, 192)));
            //设置图案样式
            titleStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        }
        titleStyle.setFont(titleFont);
        //参数1：起始行 参数2：终止行 参数3：起始列 参数4：终止列
        CellRangeAddress region1 = new CellRangeAddress(start
                , end, (short) Constants.INT_ZERO, shu);
        sheet.addMergedRegion(region1);
        XSSFRow row = sheet.createRow(start);
        Cell cell = row.createCell(Constants.INT_ZERO);
        cell.setCellValue(mergeTitle);
        cell.setCellStyle(titleStyle);
    }

    /**
     * 设置表头
     * ##Params: rowIndex	开始位置
     * ##Params: wb	表格生成需要对象
     * ##Params: sheet	表格生成需要对象
     * ##Params: titles	表头数据
     * ##Params: isFinxd	是否固定表头
     * ##return: int  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:34
     */
    private static int writeTitlesToExcel(int rowIndex,XSSFWorkbook wb
            , Sheet sheet, List<String> titles, boolean isFinxd) {
//        int rowIndex = 2;
        int colIndex;
        Font titleFont = wb.createFont();
        //设置字体
        titleFont.setFontName("simsun");
        //设置粗体
        titleFont.setBoldweight(Short.MAX_VALUE);
        //设置字号
        titleFont.setFontHeightInPoints((short) 14);
        //设置颜色
        titleFont.setColor(IndexedColors.BLACK.index);
        XSSFCellStyle titleStyle = wb.createCellStyle();
        //水平居中
        titleStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        //垂直居中
        titleStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        //设置图案颜色
        titleStyle.setFillForegroundColor(new XSSFColor(new Color(182, 184, 192)));
        //设置图案样式
        titleStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        titleStyle.setFont(titleFont);
        setBorder(titleStyle, BorderStyle.THIN, new XSSFColor(new Color(0, 0, 0)));
        Row titleRow = sheet.createRow(rowIndex);
        titleRow.setHeightInPoints(25);
        if (isFinxd) {
            // 固定titles.size()行(rowIndex+ Constants.INT_ONE)列
            sheet.createFreezePane(titles.size(), (rowIndex+ Constants.INT_ONE));
        }
        colIndex = 0;
        for (int i = 0; i < titles.size(); i++){
            Cell cell = titleRow.createCell(colIndex);
            cell.setCellValue(titles.get(i));
            cell.setCellStyle(titleStyle);
            colIndex++;
        }
        rowIndex++;
        return rowIndex;
    }

    /**
     * 设置表格内容
     * ##Params: wb	表格生成需要对象
     * ##Params: sheet	表格生成需要对象
     * ##Params: rows	行数据
     * ##Params: rowIndex	开始位置
     * ##Params: pagingRow	分页行
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:35
     */
    private static void writeRowsToExcel(XSSFWorkbook wb, Sheet sheet
            , List<List<String>> rows, int rowIndex
            , List<Integer> pagingRow) {
        int colIndex;
        Font dataFont = wb.createFont();
        dataFont.setFontName("simsun");
        dataFont.setFontHeightInPoints((short) 14);
        dataFont.setColor(IndexedColors.BLACK.index);

        XSSFCellStyle dataStyle = wb.createCellStyle();
        dataStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        dataStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        dataStyle.setFont(dataFont);
        setBorder(dataStyle, BorderStyle.THIN, new XSSFColor(new Color(0, 0, 0)));
        int i = 0;
        for (int ro = 0; ro < rows.size(); ro++){
//        for (List<String> rowData : rows) {
            Row dataRow = sheet.createRow(rowIndex);
            dataRow.setHeightInPoints(25);
            colIndex = 0;
            for (int ce = 0; ce < rows.get(ro).size(); ce++){
//            for (String cellData : rows.get(ro)) {
                Cell cell = dataRow.createCell(colIndex);
                if (rows.get(ro).get(ce) != null) {
                    cell.setCellValue(rows.get(ro).get(ce));
                } else {
                    cell.setCellValue("");
                }
                cell.setCellStyle(dataStyle);
                colIndex++;
            }
            for (int pa = 0; pa < pagingRow.size(); pa++){
//            for (Integer ints : pagingRow) {
                if (i == pagingRow.get(pa)) {
                    sheet.setRowBreak(i);
                    break;
                }
            }
            i++;
            rowIndex++;
        }
    }

    /**
     * 自动调整列宽
     * ##Params: sheet	表格生成需要对象
     * ##Params: columnNumber	单元格数量
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:36
     */
    private static void autoSizeColumns(Sheet sheet, int columnNumber) {
        for (int i = 0; i < columnNumber; i++) {
//            int orgWidth = sheet.getColumnWidth(i);
//            sheet.autoSizeColumn(i, true);
//            int newWidth = (sheet.getColumnWidth(i) + 20);
//            if (newWidth > orgWidth) {
//                sheet.setColumnWidth(i, newWidth);
//            } else {
//                sheet.setColumnWidth(i, orgWidth);
//            }
            int colWidth = sheet.getColumnWidth(i)*2;
            if(colWidth<255*256){
                sheet.setColumnWidth(i, colWidth < 3000 ? 3000 : colWidth);
            }else{
                sheet.setColumnWidth(i,6000 );
            }
            sheet.autoSizeColumn(i, true);
        }
    }

    /**
     * 设置边框
     * ##Params: style	excel样式对象
     * ##Params: border	excel样式对象
     * ##Params: color	excel样式对象
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:36
     */
    private static void setBorder(XSSFCellStyle style, BorderStyle border, XSSFColor color) {
        style.setBorderTop(border);
        style.setBorderLeft(border);
        style.setBorderRight(border);
        style.setBorderBottom(border);
        style.setBorderColor(BorderSide.TOP, color);
        style.setBorderColor(BorderSide.LEFT, color);
        style.setBorderColor(BorderSide.RIGHT, color);
        style.setBorderColor(BorderSide.BOTTOM, color);
    }

    /**
     * 生成统计excel
     * @Author Rachel
     * @Date 2021/10/22
     * ##param arrayExcel excel内容
     * ##param startDate 开始时间
     * ##param endDate 结束时间
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


    public static File statExcel(JSONArray arrayStatistic, JSONArray fields, String writeName) throws IOException {
        long one = System.currentTimeMillis();
        //创建excel文件并命名
        File excelFile = File.createTempFile(writeName, ".xlsx");
        FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet");
        sheet.createFreezePane(0, 1, 0, 1);

        JSONObject jsonTitel = new JSONObject();
        JSONObject jsonCell = arrayStatistic.getJSONObject(0);
        jsonCell.forEach((k, v) ->{

        });
        //获取每个字段最大长度
        JSONObject lengths = new JSONObject();
        for (Map.Entry<String, Object> entry : jsonCell.entrySet()) {
            int length = jsonCell.getJSONObject(entry.getKey()).getString("wrdN").getBytes(StandardCharsets.UTF_8).length;
            if (lengths.getInteger(entry.getKey()) == null || lengths.getInteger(entry.getKey()) < length) {
                lengths.put(entry.getKey(), length);
            }
        }
        for (int i = 1; i < arrayStatistic.size(); i++) {
            System.out.println("arrayStatistic.i=" + arrayStatistic.getJSONObject(i));
            jsonCell = arrayStatistic.getJSONObject(i);
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
//        if (arrayStatistic.getJSONObject(0).getJSONObject("price") != null) {
//            System.out.println("price");
//            fields.add("count");
//            fields.add("price");
//            fields.add("sum");
//        } else {
//            System.out.println("null");
//            fields.add("count");
//        }
        for (int i = 0; i < arrayStatistic.size(); i++) {
            XSSFRow row = sheet.createRow(i);
            jsonCell = arrayStatistic.getJSONObject(i);
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
                    String type = arrayStatistic.getJSONObject(0).getJSONObject(fields.getString(j)).getString("type");
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
}