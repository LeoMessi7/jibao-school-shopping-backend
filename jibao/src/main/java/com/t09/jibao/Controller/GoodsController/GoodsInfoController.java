package com.t09.jibao.Controller.GoodsController;


import com.alibaba.fastjson.JSONObject;
import com.t09.jibao.Vo.GoodsVo;
import com.t09.jibao.Vo.SelectionVo;
import com.t09.jibao.domain.*;
import com.t09.jibao.service.*;
import com.t09.jibao.utils.CategoryUtil;
import com.t09.jibao.utils.GoodsUtil;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class GoodsInfoController {
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private WithdrawService withdrawService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private SelectionService selectionService;

    @Autowired
    private HttpServletRequest request;





    /**
     * search the information of goods
     * @param params request params
     *               contains:
     *                  key_word: the key word user searched
     * @return the information of goods
     */
    @PostMapping("/search")
    public String search(@RequestParam Map<String,String> params){
        // key word
        String key_word = params.get("key_word");
        JSONObject response = new JSONObject();
        List<Goods> goodsList = goodsService.search(key_word);
        response.put("goodsInfoList", GoodsUtil.fillGoods(goodsList));
        response.put("length", goodsList.size());
        return response.toJSONString();
    }


    /**
     * get goods detail
     * @param params request params
     *               contains:
     *                  gid: goods id
     * @return response
     */
    @PostMapping("/get/detail")
    public String getSellerDetail(@RequestParam Map<String,String> params){
        String gid_str = params.get("gid");
        Long gid = (long) Integer.parseInt(gid_str);
        JSONObject response = new JSONObject();
        Pair<User, List<Comment>> sellerInfo = uploadService.findSellersInfoByGid(gid);
        User seller = sellerInfo.getKey();
        List<Comment> comments = sellerInfo.getValue();
        response.put("seller_name", seller.getName());
        response.put("avatar_url", seller.getAvatarPath());
        double mark = 0;
        for(Comment comment: comments)
            mark += comment.getMark();
        if(comments.isEmpty())
            mark = 5;
        else
            mark /= comments.size();
        response.put("avg_mark", mark);
        response.put("comments", comments.stream().map(Comment::getContent).collect(Collectors.toList()));
        response.put("comment_time", comments.stream().map(Comment::getCommentTime).collect(Collectors.toList()));
        response.put("marks", comments.stream().map(Comment::getMark).collect(Collectors.toList()));
        return response.toJSONString();
    }


    /**
     * random search
     * @return response
     */
    @PostMapping("/randomSearch")
    public String randomSearch(){
        // key word
        JSONObject response = new JSONObject();
        List<Goods> goodsList = goodsService.search("");
        Random random = new Random();
        Set<Goods> randomSet = new HashSet<>();
        while (randomSet.size() < 9 && randomSet.size() != goodsList.size()) {
            randomSet.add(goodsList.get(random.nextInt(goodsList.size())));
        }
        List<Goods> goodsRandomList = new ArrayList<>(randomSet);
        response.put("goodsInfoList", GoodsUtil.fillGoods(goodsRandomList));
        response.put("length", goodsList.size());
        return response.toJSONString();
    }

    /**
     * upload goods
     * @param params request params
     *               contains:
     *                  description: the description of goods
     *                  name: the name of goods
     *                  price: the price of goods
     *                  category & sub_category: the category of goods
     *
     * @param image item picture
     * @return response
     */
    @PostMapping("/goods/upload")
    public String upload(@RequestParam Map<String,String> params,
                         @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        Long uid = (long) request.getSession().getAttribute("uid");
        String description = params.get("description");
        String name = params.get("name");
        String sub_category = params.get("sub_category");
        String price_str = params.get("price");
        String campus = params.get("campus");
        System.out.println(description+name+sub_category+price_str);
        int price = Integer.parseInt(price_str);
        Goods goods = goodsService.add(uid, sub_category, name, price, description, campus, image);
        JSONObject response = new JSONObject();
        if(goods == null)
            response.put("code", 1);
        else
            response.put("code", 0);
        return response.toJSONString();
    }



    /**
     * get goods that user had bought
     * note that user should be logged in
     * @return response purchase list
     */
    @PostMapping("/goods/getPurchase")
    public String getPurchase() {
        JSONObject response = new JSONObject();
        Long uid = (long) request.getSession().getAttribute("uid");
        List<GoodsVo> purchaseList = purchaseService.findGoodsVoByUid(uid);
        // information
        response.put("goodsInfoList", GoodsUtil.fillGoodsAndUser(purchaseList));
        response.put("length", purchaseList.size());
        return response.toJSONString();
    }

    /**
     * withdraw the goods
     * @param params request params:
     *               contains:
     *                  gid: the id of goods
     * @return response
     */
    @PostMapping("/goods/withdraw")
    public String withdraw(@RequestParam Map<String,String> params) {
        JSONObject response = new JSONObject();
        Object uid_object = request.getSession().getAttribute("uid");
        Long gid = (long) Integer.parseInt(params.get("gid"));
        Long uid = (long) uid_object;
        Upload upload = uploadService.findByGid(gid);
        // upload is null:  the goods hasn't been uploaded
        // not equal: something wrong
        if (upload == null || !uid.equals(upload.getUser().getId()))
            response.put("code", 1);
        else {
            int code = withdrawService.withdrawGoods(uid, gid);
            response.put("code", code);
        }
        return response.toJSONString();
    }


    /**
     * get goods uploaded by user
     * @return response
     */
    @PostMapping("/goods/getUpload")
    public String getUpload() {
        JSONObject response = new JSONObject();
        Object uid_object = request.getSession().getAttribute("uid");
        Long uid = (long) uid_object;
        List<GoodsVo> goodsVoList = uploadService.findGoodsVoInfoByUid(uid);
        response.put("goodsInfoList", GoodsUtil.fillGoodsAndUser(goodsVoList));
        response.put("length", goodsVoList.size());
        return response.toJSONString();
    }


    /**
     * get category group by category
     * @return response
     */
    @PostMapping("/getCategory")
    public String getCategory() {
        Map<String, List<String>> categories = categoryService.findAll();
        JSONObject response = new JSONObject();
        response.put("category", CategoryUtil.fillCategory(categories));
        return response.toJSONString();
    }


    /**
     * add goods to shopping cart
     * @return response
     */
    @PostMapping("/goods/select")
    public String selectGoods(@RequestParam Map<String,String> params) {
        JSONObject response = new JSONObject();
        Object uid_object = request.getSession().getAttribute("uid");
        Long uid = (long) uid_object;
        Long gid = (long) Integer.parseInt(params.get("gid"));
        response.put("code", selectionService.select(uid, gid));
        return response.toJSONString();
    }



    /**
     * get shopping cart
     * @return response
     */
    @PostMapping("/goods/getSelection")
    public String getSelection(@RequestParam Map<String,String> params) {
        Object uid_object = request.getSession().getAttribute("uid");
        Long uid = (long) uid_object;
        JSONObject response = new JSONObject();
        List<SelectionVo> selectionVoList = selectionService.findByUid(uid);
        response.put("selection", GoodsUtil.fillSelection(selectionVoList));
        response.put("length", selectionVoList.size());
        return response.toJSONString();
    }


    /**
     * delete shopping cart
     * @return response
     */
    @PostMapping("/goods/deleteSelection")
    public String deleteSelection(@RequestParam Map<String,String> params) {
        Object uid_object = request.getSession().getAttribute("uid");
        Long uid = (long) uid_object;
        JSONObject response = new JSONObject();
        Long gid = (long) Integer.parseInt(params.get("gid"));
        System.out.println(gid);
        selectionService.delete(uid, gid);
        response.put("code", 0);
        return response.toJSONString();
    }

    /**
     * update goods information
     * @param params request params
     *               contains:
     *                  description: the description of goods
     *                  name: the name of goods
     *                  price: the price of goods
     *                  category & sub_category: the category of goods
     *
     * @param image item picture
     * @return response
     */
    @PostMapping("/goods/update")
    public String goodsUpdate(@RequestParam Map<String,String> params,
                         @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        Long uid = (long) request.getSession().getAttribute("uid");
        String description = params.get("description");
        String name = params.get("name");
        String sub_category = params.get("sub_category");
        String price_str = params.get("price");
        String campus = params.get("campus");
        int price = Integer.parseInt(price_str);
        Long gid = (long) Integer.parseInt(params.get("gid"));
        String image_url = goodsService.update(uid, gid, sub_category, name, price, description, campus, image);
        JSONObject response = new JSONObject();
        response.put("image_url", image_url);
        return response.toJSONString();
    }

    /**
     * clear shopping cart (buy all of them)
     * @param total_str total price
     * @param gid_list_str goods list
     * @return response
     */
    @PostMapping("/goods/buyAll")
    public String goodsBuyAll(@RequestParam(value = "total") String total_str,
                              @RequestParam(value = "gid_list") List<String> gid_list_str) {
        Long uid = (long) request.getSession().getAttribute("uid");
        int total = Integer.parseInt(total_str);
        List<Long> gid_list = new ArrayList<>();
        for(String gid_str: gid_list_str){
            gid_list.add((long) Integer.parseInt(gid_str));
        }
        JSONObject response = new JSONObject();
        response.put("code", purchaseService.purchaseAll(uid, total, gid_list));
        return response.toJSONString();
    }

    /**
     * purchase goods
     * @param params request params:
     *                  contains:
     *                      gid: goods id
     * @return response
     */
    @PostMapping("/goods/purchase")
    public String goodsBuyAll(@RequestParam Map<String,String> params) {
        Long uid = (long) request.getSession().getAttribute("uid");
        Long gid = (long) Integer.parseInt(params.get("gid"));
        int code = purchaseService.purchase(uid, gid);
        JSONObject response = new JSONObject();
        response.put("code", code);
        return response.toJSONString();
    }


}
