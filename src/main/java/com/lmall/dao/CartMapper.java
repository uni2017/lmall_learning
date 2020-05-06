package com.lmall.dao;

import com.lmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectByUserIdAndProductId(@Param(value = "userId") Integer userId, @Param(value = "productId")Integer productId);

    List<Cart> selectByUserId(Integer userId);

    int selectCartProductCheckedByUserId(Integer userId);

    int deleteByProductIds(@Param(value = "userId")Integer userId,@Param(value = "productIdList") List<String> productIdList);

    int checkedOrUncheckedProduct(@Param(value = "userId")Integer userId,@Param(value = "productId")Integer productId,@Param(value = "checked") int checked);

    int getProductNum(Integer userId);

}