package com.lmall.service.Impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.lmall.common.Const;
import com.lmall.common.ResponseCode;
import com.lmall.common.ServerResponse;
import com.lmall.dao.CartMapper;
import com.lmall.dao.ProductMapper;
import com.lmall.pojo.Cart;
import com.lmall.pojo.Product;
import com.lmall.service.ICartService;
import com.lmall.util.BigDecimalUtil;
import com.lmall.util.PropertiesUtil;
import com.lmall.vo.CartProductVo;
import com.lmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Xyg on 2020/5/6.
 */

@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    public ServerResponse<CartVo> add(Integer userId,Integer productId,Integer count){
        if (userId == null || productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart = cartMapper.selectByUserIdAndProductId(userId,productId);
        if (cart == null){
            Cart cartItem = new Cart();
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setUserId(userId);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartMapper.insert(cartItem);
        }else {
            cart.setQuantity(count+cart.getQuantity());
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count){

        if (userId == null || productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId,productId);
        if (cart != null){
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);

    }

    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds){
        if (productIds == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productIdList)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        cartMapper.deleteByProductIds(userId,productIdList);
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);

    }

    public ServerResponse<CartVo> cartList(Integer userId){
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> checkedOrUnChecked(Integer userId,Integer productId, int checked){
        if (checked != 0 && checked != 1){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.checkedOrUncheckedProduct(userId,productId,checked);
        return this.cartList(userId);
    }

    public ServerResponse<Integer> getProductNum(Integer userId){
        if (userId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return ServerResponse.createBySuccess(cartMapper.getProductNum(userId));
    }

    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");

        if (CollectionUtils.isNotEmpty(cartList)){
            for(Cart cartItem : cartList){
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductStock(product.getStock());
                    cartProductVo.setProductSubtitle(product.getSubtitle());

                    //cartProductVo.setProductChecked();
                    //cartProductVo.setProductTotalPrice();
                    //cartProductVo.setQuantity();
                    int buyLimitCount = 0;
                    if (cartItem.getQuantity() <= product.getStock()){
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCES);
                    }else {
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartForQuantity.setId(cartItem.getId());
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity().doubleValue()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                if (cartItem.getChecked() == Const.Cart.CHECKED){
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }

        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        cartVo.setAllChecked(this.cartProductAllChecked(userId));

        return cartVo;
    }

    private boolean cartProductAllChecked(Integer userId){
        if (userId == null){
            return false;
        }
        return cartMapper.selectCartProductCheckedByUserId(userId)==0;

    }

}
