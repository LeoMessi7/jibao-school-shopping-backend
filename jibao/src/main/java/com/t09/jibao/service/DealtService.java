package com.t09.jibao.service;

import com.t09.jibao.domain.Chat;
import com.t09.jibao.domain.Dealt;

public interface DealtService {
    // save
    Dealt save(Dealt dealt);

    // add
    void add(String user_name, Long aid, Long fid, String content);
}
