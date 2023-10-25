package com.cao.ruijie.controller;


import com.cao.ruijie.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    //声明转存文件时，目标目录的虚拟路径（也就是浏览器访问服务器时，能够访问到这个目录的路径）
    @Value("${ruijipath.virtualPath}")
    private String virtualPath;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file, HttpServletRequest request) throws IOException {
        //file 是一个临时文件，需要转存到指定位置，否则本次请求结束后临时文件会删除
        log.info("文件上传：{}",file);
        //获取原始文件名
        String originalFilename = file.getOriginalFilename();
        //防止文件名重复覆盖，使用 uuid 给图片重命名，并且去掉 uuid 的四个“-”
        String fileName = UUID.randomUUID().toString();
        //获取图片的后缀
        String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
        //fileName+extName 拼接成新的文件名称
        String newFileName = fileName+extName;

        //准备转存文件时，目标目录的路径。File 对象要求这个路径是一个完整的物理路径。
        //而这个物理路径又会因为在服务器上部署运行时所在的系统环境不同而不同，所以不能写死。
        //需要调用 servletContext 对象的 getRealPath() 方法，将目录的虚拟路径转换为实际的物理路径
        String targetDirPath = request.getServletContext().getRealPath(virtualPath);
        log.info("真实的物理路径：{}",targetDirPath);
        //创建 File 类型的对象用于文件转存
        File filePath = new File(targetDirPath,newFileName);
        log.info("物理路径：{}",filePath.getParentFile());
        if(!filePath.getParentFile().exists()){
            //如果文件夹不存在就新建一个
            filePath.getParentFile().mkdirs();
        }
        //转存文件
        file.transferTo(filePath);

        return Result.success(newFileName);
    }
    @GetMapping("/download")
    public void download(@RequestParam("name") String fileName, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("image/jpeg");
        //得到保存图片的实际路径
        log.info("文件下载，文件路径：{}",request.getServletContext().getRealPath(virtualPath));
        String realPath = request.getServletContext().getRealPath(virtualPath) + fileName;
        File file = new File(realPath);
        //将图片保存到字节数组中
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             ServletOutputStream sos = response.getOutputStream();
            ){
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len=bis.read(bytes))!=-1){
                sos.write(bytes,0,len);
                sos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
