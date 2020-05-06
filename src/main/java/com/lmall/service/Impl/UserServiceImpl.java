package com.lmall.service.impl;

import com.lmall.common.Const;
import com.lmall.common.ResponseCode;
import com.lmall.common.ServerResponse;
import com.lmall.common.TokenCache;
import com.lmall.dao.UserMapper;
import com.lmall.pojo.User;
import com.lmall.service.IUserService;
import com.lmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by Xyg on 2020/4/14.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService{

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password){

        //if the username exist
        int resultCount = userMapper.checkUsername(username);
        if(resultCount == 0)
            return ServerResponse.createByErrorMessage("The User is not exist!");

        //if the password isright
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5Password);
        if(user == null)
            return ServerResponse.createByErrorMessage("Password mistick!");

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("Login success!",user);

    }

    public ServerResponse<String> register(User user) {

        //if the username exist
        ServerResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()) return validResponse;

        validResponse = this.checkValid(user.getEmail(),Const.Email);
        if(!validResponse.isSuccess()) return validResponse;

        int resultCount;
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        //user.setRole(Const.Role.ROLE_ADMIN);

        resultCount = userMapper.insert(user);
        if(resultCount == 0)
            return ServerResponse.createByErrorMessage("Register false!");

        return ServerResponse.createBySuccessMessage("Register success!");

    }

    public ServerResponse<String> checkValid(String str, String type   ){

        if(StringUtils.isNoneBlank(type)){
            //开始校验
            if(Const.USERNAME.equals(type)){

                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0)
                    return ServerResponse.createByErrorMessage("Username exist!");

            }
            if (Const.Email.equals(type)){

                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0)
                    return ServerResponse.createByErrorMessage("Email exist!");

            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误！");
        }

        return ServerResponse.createBySuccessMessage("用户不存在！");

    }

    public ServerResponse<String> selectQuestion(String username){

        int resultCount = userMapper.checkUsername(username);
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在！");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNoneBlank(question)){
            return ServerResponse.createBySuccess(question);
        }

        return ServerResponse.createByErrorMessage("未设置问题！");

    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer){

        int resultCount = userMapper.checkAnswer(username,question,answer);
        if(resultCount > 0){

            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);

        }
        return ServerResponse.createByErrorMessage("答案错误！");

    }

    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){

        if (StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，token需传递！");
        }
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            return validResponse;
        }
        String token = TokenCache.getkey(TokenCache.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token过期或无效！");
        }

        if(StringUtils.equals(token,forgetToken)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);
            if(rowCount > 0){
                return ServerResponse.createBySuccessMessage("密码修改成功！");
            }
        }else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取！");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");

    }

    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user){

        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误！");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("密码更新成功！");
        }

        return ServerResponse.createByErrorMessage("密码更新失败！");

    }

    public ServerResponse<User> updateInformation(User user){

        //username不能更新，email进行校验
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("email已存在，请更换后尝试！");
        }

        User updateuser = new User();
        updateuser.setId(user.getId());
        updateuser.setEmail(user.getEmail());
        updateuser.setQuestion(user.getQuestion());
        updateuser.setAnswer(user.getAnswer());
        updateuser.setPhone(user.getPhone());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateuser);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功！",updateuser);
        }

        return ServerResponse.createByErrorMessage("更新个人信息失败！");

    }

    public ServerResponse<User> getInformation(Integer userId ){

        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户！");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);

    }

    /**
     * 校验是否为管理员
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }return ServerResponse.createByError();
    }

}
