package com.z.majcal.db.service;


import com.z.majcal.core.MajContext;

import java.util.List;

public interface MajDataBase {

    //存储
    void saveAfterQuery(MajContext majContext);


    List<MajContext> queryAllFromFile();


    List<String> analyzeHistoryMaj(List<MajContext> majContexts);

}
