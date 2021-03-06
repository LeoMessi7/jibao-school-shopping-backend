package com.t09.jibao.service;
import com.t09.jibao.domain.Upload;
import com.t09.jibao.domain.Withdraw;

public interface WithdrawService {

    // save
    Withdraw save(Withdraw withdraw);

    // find withdraw object by id
    Withdraw findById(Long id);

    // withdraw goods
    int withdrawGoods(Long uid, Long gid);
}
