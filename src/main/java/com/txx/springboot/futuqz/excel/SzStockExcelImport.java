package com.txx.springboot.futuqz.excel;


import com.google.common.base.Strings;
import com.txx.springboot.futuqz.entity.Stock;
import com.txx.springboot.futuqz.service.StockService;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/***
 * 直接处理深交所中所有的股票信息
 * 地址：http://www.szse.cn/market/product/stock/list/index.html
 * 保存地址用于下次更新上市公司代码
 */
//@Controller
@RequestMapping(value = "/szExcel")
public class SzStockExcelImport {

    @Autowired
    private StockService stockService;

    public static final String DOC_PATH = "/Users/ouyangyu/IdeaProjects/qz/doc/";
    static DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    @RequestMapping("/insert")
    public void insert() throws IOException{
        String filePath = DOC_PATH +"import/SZ_STOCK.xlsx";
        FileInputStream inputStream = new FileInputStream (filePath);
        XSSFWorkbook sheets = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = sheets.getSheet ("A股列表");
        //获取行数
        int rows = sheet.getPhysicalNumberOfRows();
        for(int i=1;i<rows;i++){
            //获取列数
            XSSFRow row = sheet.getRow(i);
            if(row.getCell (0)==null || row.getCell (0).getStringCellValue ()==null || row.getCell (0).getStringCellValue ().equals ("")){
                continue;
            }
            Stock stock = new Stock ();
            int columns = row.getPhysicalNumberOfCells();
            for(int j=0;j<columns;j++){
                XSSFCell xssfCell = row.getCell (j);
                if(xssfCell!=null) {
                    switch (j){
                        case 0:
                            String plate = xssfCell.getStringCellValue ();
                            if(!Strings.isNullOrEmpty (plate)){
                                stock.setPlate (plate);
                            }
                            break;
                        case 1:
                            String company = xssfCell.getStringCellValue ();
                            if(!Strings.isNullOrEmpty (company)){
                                stock.setCompany (company);
                            }
                            break;
                        case 2:
                            String companyEn = xssfCell.getStringCellValue ();
                            if(!Strings.isNullOrEmpty (companyEn)){
                                stock.setCompanyEn (companyEn);
                            }
                            break;
                        case 3:
                            String regAddress = xssfCell.getStringCellValue ();
                            if(!Strings.isNullOrEmpty (regAddress)){
                                stock.setRegAddress (regAddress);
                            }
                            break;
                        case 4:
                            String code = xssfCell.getStringCellValue ();
                            if(!Strings.isNullOrEmpty (code)){
                                stock.setCode (code);
                            }
                            break;
                        case 5:
                            //代码
                            String name = xssfCell.getStringCellValue ();
                            if(!Strings.isNullOrEmpty (name)){
                                //替换名称中的空格
                                stock.setName (name.replace (" ",""));
                            }
                            break;
                        case 6:
                            String dateStr = xssfCell.getStringCellValue ();
                            Date date = DTF.parseDateTime (dateStr).toDate ();
                            stock.setMarketDate (date);
                            break;
                        case 7:
                            String totalCapital = xssfCell.getStringCellValue ();
                            stock.setTotalCapital (BigDecimal.valueOf (Double.valueOf (totalCapital.replaceAll (",",""))));
                            break;
                        case 8:
                            String circulCapital = xssfCell.getStringCellValue ();
                            stock.setCirculCapital (BigDecimal.valueOf (Double.valueOf (circulCapital.replaceAll (",",""))));
                            break;
                        case 14:
                            String area = xssfCell.getStringCellValue ();
                            if(!Strings.isNullOrEmpty (area)){
                                stock.setArea (area);
                            }
                            break;
                        case 15:
                            String province = xssfCell.getStringCellValue ();
                            if(!Strings.isNullOrEmpty (province)){
                                stock.setProvince (province);
                            }
                            break;
                        case 16:
                            String city = xssfCell.getStringCellValue ();
                            if(!Strings.isNullOrEmpty (city)){
                                stock.setCity (city);
                            }
                            break;
                        case 17:
                            String industry = xssfCell.getStringCellValue ();
                            if(!Strings.isNullOrEmpty (industry)){
                                stock.setIndustry (industry);
                            }
                            break;
                        case 18:
                            String website = xssfCell.getStringCellValue ();
                            if(!Strings.isNullOrEmpty (website)){
                                stock.setWebsite (website);
                            }
                            break;
                        case 19:
                            String market = xssfCell.getStringCellValue ();
                            if(!Strings.isNullOrEmpty (market)){
                                stock.setMarket (market);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            stockService.save (stock);
            System.out.println (stock);
        }
    }


    public static void main(String[] args) throws IOException {


    }






}
