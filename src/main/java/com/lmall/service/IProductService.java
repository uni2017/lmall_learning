package com.lmall.service;

import com.github.pagehelper.PageInfo;
import com.lmall.common.ServerResponse;
import com.lmall.pojo.Product;
import com.lmall.vo.ProductDetailVo;

/**
 * Created by Xyg on 2020/5/1.
 */
public interface IProductService {

    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    ServerResponse<PageInfo> manageProductList(int pageNum, int pageSize);

    ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);

    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId, int pageNum, int pageSize, String orderBy);

}
