package com.t09.jibao.service.implement;

import com.t09.jibao.Vo.GoodsVo;
import com.t09.jibao.dao.GoodsDAO;
import com.t09.jibao.dao.PurchaseDAO;
import com.t09.jibao.dao.SelectionDAO;
import com.t09.jibao.dao.UserDAO;
import com.t09.jibao.domain.*;
import com.t09.jibao.service.PurchaseService;
import com.t09.jibao.service.SelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseDAO purchaseDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private GoodsDAO goodsDAO;
    @Autowired
    private SelectionDAO selectionDAO;

    /**
     * save
     * @param purchase purchase object
     * @return purchase object
     */
    @Override
    public Purchase save(Purchase purchase) {
        return purchaseDAO.save(purchase);
    }

    /**
     * find purchase object by id
     * @param id purchase id
     * @return purchase object
     */
    @Override
    public Purchase findById(Long id){
        return null;
    }

    /**
     * find goods from purchase table by uid
     * @param uid user id
     * @return goods list
     */
    @Override
    public List<Goods> findGoodsByUid(Long uid){
        User user = userDAO.findById(uid).get();
        List<Purchase> purchaseList = purchaseDAO.findPurchaseByUser(user);
        List<Goods> goodsList = new ArrayList<>();
        for(Purchase purchase : purchaseList){
            Goods goods = goodsDAO.findById(purchase.getId()).get();
            goodsList.add(goods);
        }
        return goodsList;
    }

    @Override
    public List<GoodsVo> findGoodsVoByUid(Long uid) {
        return purchaseDAO.findPurchaseAndGoodsByUid(uid);
    }


    /**
     * buy goods
     * @param uid user id
     * @param gid goods id
     * @return error code
     */
    @Override
    public int purchase(Long uid, Long gid){
        User user = userDAO.findById(uid).get();
        Goods goods = goodsDAO.findById(gid).get();
        int balance = user.getBalance();
        int price = goods.getPrice();
        // insufficient balance
        if(balance < price)
            return 1;
        if(goods.getStatus() != 0)
            return 2;
        Purchase purchase = new Purchase();

        // Purchase object
        purchase.setPurchaseTime(new Date());
        purchase.setUser(user);
        purchase.setGoods(goods);

        // set the balance of user
        user.setBalance(balance - price);
        userDAO.save(user);

        // set the status of goods
        goods.setStatus(1);
        goodsDAO.save(goods);
        save(purchase);
        return 0;
    }

    /**
     * purchase all
     * @param uid user id
     * @param total total price
     * @param gid_list goods list
     * @return error code
     */
    @Override
    public int purchaseAll(Long uid, int total, List<Long> gid_list) {
        User user = userDAO.findById(uid).get();
        if(user.getBalance() < total)
            return 1;
        List<Goods> goodsList = goodsDAO.findAllByIdIn(gid_list);
        boolean flag = false;
        for(Goods goods: goodsList){
            if(goods.getStatus() != 0){
                flag = true;
                SelectionPK selectionPK = new SelectionPK();
                selectionPK.setGoods(goods);
                selectionPK.setUser(user);
                selectionDAO.deleteById(selectionPK);
            }
        }
        if(flag)
            return 2;
        for(Goods goods: goodsList){
            Purchase purchase = new Purchase();
            SelectionPK selectionPK = new SelectionPK();
            selectionPK.setUser(user);
            selectionPK.setGoods(goods);
            selectionDAO.deleteById(selectionPK);
            goods.setStatus(1);
            goodsDAO.save(goods);
            purchase.setPurchaseTime(new Date());
            purchase.setUser(user);
            purchase.setGoods(goods);
            purchaseDAO.save(purchase);
        }
        user.setBalance(user.getBalance() - total);
        userDAO.save(user);
        return 0;
    }
}