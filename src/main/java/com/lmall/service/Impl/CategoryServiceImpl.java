package com.lmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lmall.common.ServerResponse;
import com.lmall.dao.CategoryMapper;
import com.lmall.pojo.Category;
import com.lmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by Xyg on 2020/4/22.
 */

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName, Integer parentId){

        if (StringUtils.isBlank(categoryName) || parentId == null){
            return ServerResponse.createByErrorMessage("品类设置错误！");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        int rowCount = categoryMapper.insert(category);
        if(rowCount > 0){

            return ServerResponse.createBySuccess("添加品类成功！");

        }
        return ServerResponse.createByErrorMessage("添加品类失败！");
    }

    public ServerResponse updateCategoryName(Integer categoryId, String categoryName){
        if (StringUtils.isBlank(categoryName) || categoryId == null){
            return ServerResponse.createByErrorMessage("品类设置错误！");
        }

        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0){
            return ServerResponse.createBySuccessMessage("更新品类名称成功！");
        }
        return ServerResponse.createByErrorMessage("更新品类名称失败！");

    }

    public ServerResponse<List<Category>> getChildrenParalleCategory(Integer categoryId){
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查询本节点及孩子节点id
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){

        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet,categoryId);
        java.util.List<Integer> categoryList = Lists.newArrayList();
        if (categoryId != null){
            for(Category categoryItem : categorySet){
                categoryList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId){
        Category category =categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null){
            categorySet.add(category);
        }
        java.util.List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem : categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }

}
