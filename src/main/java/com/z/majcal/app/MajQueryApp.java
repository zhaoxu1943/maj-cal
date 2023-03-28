package com.z.majcal.app;



import com.z.majcal.core.MajContext;
import com.z.majcal.db.MajDataBaseImpl;
import com.z.majcal.db.service.MajDataBase;

import java.util.List;

public class MajQueryApp {

    public static void main(String[] args) {
        MajDataBase db = new MajDataBaseImpl();
        List<MajContext> majContexts = db.queryAllFromFile();
        for (MajContext context:majContexts) {
            System.out.println(context);
        }
    }
}
