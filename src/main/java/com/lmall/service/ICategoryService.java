package com.lmall.service;

import com.lmall.common.ServerResponse;
import com.lmall.pojo.Category;

import java.util.List;

/**
 * Created by Xyg on 2020/4/22.
 */
public interface ICategoryService {

    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse updateCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getChildrenParalleCategory(Integer categoryId);

    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);

}
