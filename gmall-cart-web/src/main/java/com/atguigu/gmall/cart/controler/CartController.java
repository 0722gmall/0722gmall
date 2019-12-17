package com.atguigu.gmall.cart.controler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotation.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    CartService cartService;

    @Reference
    SkuService skuService;

    @RequestMapping("/addToCart")
    @LoginRequired(isNeedeSuccess=false)
    public String addToCart(HttpServletRequest request, OmsCartItem omsCartItem, HttpServletResponse response){

        String id=omsCartItem.getProductSkuId();
        //通过id查pmsSkuInfo
        PmsSkuInfo pmsSkuInfo=skuService.getSkuById(id);
        //
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        //设置是否被选中
        omsCartItem.setIsChecked(("1"));
        //设置选中omsCartItem的总价格
        omsCartItem.setTotalPrice(pmsSkuInfo.getPrice().multiply(omsCartItem.getQuantity()));
        //设置默认图片
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        //设置名字
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());

        //设置三级分类id
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());

        //设置创建时间
        omsCartItem.setCreateDate(new Date());
        //设置单价
        omsCartItem.setPrice(pmsSkuInfo.getPrice());

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //获取用户id

      String userId= (String) request.getAttribute("userId");

        if(StringUtils.isNotBlank(userId)){
            //用户登录
            //添加购物车的服务
            //设置omsCartItem的用户id
            omsCartItem.setMemberId(userId);
            //设置skuId
            omsCartItem.setProductSkuId(omsCartItem.getProductSkuId());

            //根据页面传来的omsCartItem查询数据库是否有
           OmsCartItem omsCartItem1FormDb= cartService.isCartExists(omsCartItem);

           if (omsCartItem1FormDb!=null&&StringUtils.isNotBlank(omsCartItem1FormDb.getId())){
               //修改
               //设置数量
               omsCartItem1FormDb.setQuantity(omsCartItem1FormDb.getQuantity().add(omsCartItem.getQuantity()));
               //设置总价
               omsCartItem1FormDb.setTotalPrice(omsCartItem1FormDb.getPrice().multiply(omsCartItem1FormDb.getQuantity()));
               //修改数量
             cartService.updateCart(omsCartItem1FormDb);

           }else {
               //新增
               //设置总价
               omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
               //新增到数据库
               OmsCartItem omsCartItemForCache= cartService.addCart(omsCartItem);

               //修改缓存中的商品
               //cartService.updateCartForCache(omsCartItemForCache);

           }

           //同步缓存
            cartService.combineCart(userId);

        }else {
            //用户没登录
            //调用客户端的cookie操作

            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);

            // /判断cartListCookie和要添加的商品是否相同
            if (StringUtils.isNotBlank(cartListCookie)){
                //将cookie转化为OmsCartItem集合
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //判断omsCartItems和页面传来的omsCartItem是否相同
                Boolean b=if_new_cart(omsCartItems,omsCartItem);

                if (b){
                    //不同，新增
                    omsCartItems.add(omsCartItem);
                }else {
                    //修改
                    for (OmsCartItem cartItem : omsCartItems) {
                        if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                           //修改数量
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                        }

                    }
                }
            }else {

                //新增
                omsCartItems.add(omsCartItem);
            }

            //覆盖cookie
            CookieUtil.setCookie(request,response,"cartListCookie",JSON.toJSONString(omsCartItems),1000*60*60*24,true);



        }

        return "redirect:/success.html";
    }
   ////判断omsCartItems和页面传来的omsCartItem是否相同的方法
    private Boolean if_new_cart(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b=true;
        for (OmsCartItem cartItem:omsCartItems){
            if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                b=false;
            }
        }
        return  b;
    }


    @RequestMapping("/cartList")
    @LoginRequired(isNeedeSuccess = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){

        //用户id
      String userId= (String) request.getAttribute("userId");


         List<OmsCartItem> omsCartItems=new ArrayList<>();

        //未登录状态
      if(StringUtils.isBlank(userId)) {
          //用户未登录  访问cookie
          String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
          if (StringUtils.isNotBlank(cartListCookie)) {
              //把字符串cartListCookie转化为OmsCartItem
              omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
              }


          }else {
              //用户登录，访问缓存或db
              omsCartItems = cartService.getCartListByUserId(userId);

          }

        /*//获取每样商品的总价钱
        for (OmsCartItem omsCartItem : omsCartItems) {
          omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));

        }*/



       //保存到域对象中
        modelMap.put("amount",getMyCartAmout(omsCartItems));
        modelMap.put("cartList",omsCartItems);

        return "cartList";
    }

    //获取全部商品的总价钱
    private BigDecimal getMyCartAmout(List<OmsCartItem> omsCartItems) {
        BigDecimal amount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            //遍历每种商品的总价钱
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
            //判断是否被选中
            if (omsCartItem.getIsChecked().equals("1")){
                //累加全部商品的价钱
                amount=amount.add(omsCartItem.getTotalPrice());
            }

        }

        return amount;
    }


    //购物车中选中的商品方法
    @RequestMapping("/checkCart")
    @LoginRequired(isNeedeSuccess = false)
    public String checkCart(HttpServletRequest request,HttpServletResponse response,OmsCartItem omsCartItem,ModelMap modelMap){

        //创建一个OmsCartItem集合
        List<OmsCartItem> omsCartItems=new ArrayList<>();

        //用户id
      String userId= (String) request.getAttribute("userId");
        //判断id是否为空
        if (StringUtils.isNotBlank(userId)){
            //修改选中的状态
            //设置用户id
            omsCartItem.setMemberId(userId);
            //修改数据库里的购物车商品状态
            cartService.updateCart(omsCartItem);
            //通过用户id获得购物车列表商品，再添加商品到omsCartItem集合里
            omsCartItems=cartService.getCartListByUserId(userId);
        }else {

        }

       /* //获取每种商品的总价钱
        for (OmsCartItem cartItem : omsCartItems) {
            cartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));

        }
*/


        //将商品返回页面
        modelMap.put("cartList",omsCartItems);
        //返回总价钱
        modelMap.put("amount",getMyCartAmout(omsCartItems));

        return "cartListInner";
    }




}
