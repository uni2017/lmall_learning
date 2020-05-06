package com.lmall.service;

import com.lmall.common.ServerResponse;
import com.lmall.vo.CartVo;

/**
 * Created by Xyg on 2020/5/6.
 */
public interface ICartService {

    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count);

    ServerResponse<CartVo> deleteProduct(Integer userId, String productIds);

    ServerResponse<CartVo> cartList(Integer userId);

    ServerResponse<CartVo> checkedOrUnChecked(Integer userId,Integer productId, int checked);

    ServerResponse<Integer> getProductNum(Integer userId);

}
