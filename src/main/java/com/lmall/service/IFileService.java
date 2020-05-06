package com.lmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Xyg on 2020/5/2.
 */
public interface IFileService {

    String upload(MultipartFile file, String path);
}
