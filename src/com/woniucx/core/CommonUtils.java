package com.woniucx.core;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class CommonUtils {
    public static void main(String[] paramArrayOfString) {
        System.out.println(System.currentTimeMillis());
    }
    
    public static String generateDateTime(String paramString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(paramString);
        Date date = new Date();
        return simpleDateFormat.format(Long.valueOf(date.getTime()));
    }
    
    public static String[][] readExcel(String paramString) {
        File file = new File(paramString);
        String[][] arrayOfString = (String[][])null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Workbook workbook = Workbook.getWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheets()[0];
            int i = sheet.getRows();
            int j = sheet.getColumns();
            if (j != 7)
                return (String[][])null; 
            Cell[] arrayOfCell = sheet.getRow(0);
            boolean bool = (arrayOfCell[0].getContents().equals("编码") && arrayOfCell[1].getContents().equals("商品名称") && arrayOfCell[2].getContents().equals("数量") && arrayOfCell[3].getContents().equals("单价") && arrayOfCell[4].getContents().equals("金额") && arrayOfCell[5].getContents().equals("折后价") && arrayOfCell[6].getContents().equals("折后金额")) ? true : false;
            if (!bool)
                return (String[][])null; 
            arrayOfString = new String[i - 1][j];
            for (byte b = 1; b < i; b++) {
                Cell[] arrayOfCell1 = sheet.getRow(b);
                for (byte b1 = 0; b1 < arrayOfCell1.length; b1++)
                    arrayOfString[b - 1][b1] = arrayOfCell1[b1].getContents(); 
            } 
            fileInputStream.close();
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        } 
        return arrayOfString;
    }
}
