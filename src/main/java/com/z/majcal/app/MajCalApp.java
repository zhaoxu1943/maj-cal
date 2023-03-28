package com.z.majcal.app;


import com.z.majcal.core.MajCalculate;
import com.z.majcal.core.MajContext;
import com.z.majcal.db.MajDataBaseImpl;
import com.z.majcal.db.service.MajDataBase;

public class MajCalApp {



    public static void main(String[] args) {

        MajCalculate cal = new MajCalculate();
        MajDataBase db = new MajDataBaseImpl();

        String origin ="友人塌\n" +
                "1位 FeiClusell\n" +
                "3位 stephen1943\n" +
                "2位 吃肉的山羊72000\n" +
                "4位 毒瘤小张10600\n" +
                "24900\n" +
                "-7500\n" +
                "友人塌\n" +
                "1位 吃肉的山羊\n" +
                "3位 stephen1943\n" +
                "2位 FeiClusell33500\n" +
                "4位 毒瘤小张24700\n" +
                "33200\n" +
                "8600\n" +
                "友人塌\n" +
                "1位 吃肉的山羊\n" +
                "3位 FeiClusell\n" +
                "54900\n" +
                "2位 stephen1943\n" +
                "4位毒瘤小张16000\n" +
                "19000\n" +
                "10100";


        MajContext majContext = cal.calculate(origin);
        //进行maj存储
        db.saveAfterQuery(majContext);
    }


}
