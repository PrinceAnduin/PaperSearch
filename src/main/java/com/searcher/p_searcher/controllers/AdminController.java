package com.searcher.p_searcher.controllers;

import com.searcher.p_searcher.entities.Paper;
import com.searcher.p_searcher.mappers.PaperMapper;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
@RequestMapping("papers/admin")
public class AdminController {
    @Autowired
    PaperMapper paperMapper;
    @RequestMapping(value = "",method = RequestMethod.GET)
    public String admin(){
        return "admin";
    }
    @ResponseBody
    @RequestMapping(value = "",method = RequestMethod.POST)
    public String upload(@RequestParam(value = "path" , defaultValue = "") String filePath) {
        try {
            getFilesDatas(filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "上传成功";
    }

    public Map<String, String> getFilesDatas(String filePath) throws IOException {
        Map<String, String> files = new HashMap<>();
        File file = new File(filePath); //需要获取的文件的路径
        String[] fileNameLists = file.list(); //存储文件名的String数组
        File[] filePathLists = file.listFiles(); //存储文件路径的String数组
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        for (int i = 0; i < filePathLists.length; i++) {
//            if(filePathLists[i].isFile()){
//                try {//读取指定文件路径下的文件内容
//                    String fileDatas = getTextFromPdf(filePathLists[i].getPath());
//                    //把文件名作为key,文件内容为value 存储在map中
//                    files.put(fileNameLists[i], fileDatas);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            String fileName = fileNameLists[i];
            String[] splitName = fileName.split("\\.");
            String pattern = ".*\\([0-9]+\\)";
            //排除重复以及非pdf
            if (splitName.length < 2 || !splitName[splitName.length - 1].equals("pdf")
                    || Pattern.matches(pattern, splitName[splitName.length - 2])) {
                continue;
            }
            String[] nameAndWriter = splitName[splitName.length - 2].split("_");
            if (nameAndWriter.length < 2) {
                continue;
            }

            StringBuilder sb = new StringBuilder();
            for (int count = 0; count < nameAndWriter.length - 1; count++) {
                sb.append(nameAndWriter[count]);
                if (count != nameAndWriter.length - 2) {
                    sb.append("_");
                }
            }
            String name = sb.toString();
            String writer = nameAndWriter[nameAndWriter.length - 1];
            String content = "";
            try {
                content = getTextFromPdf(filePathLists[i].getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!content.equals("")) {
                Paper paper = new Paper();
                paper.setAuthor(writer);
                paper.setName(name);
                paper.setContent(content);
                paper.setPath(filePathLists[i].getPath());
                //copyFileUsingJava7Files(filePathLists[i], new File(path + "static/pdf/" + fileNameLists[i]));
                paperMapper.insert(paper);
            }
        }
        return files;
    }

    private void copyFileUsingJava7Files(File source, File dest)
            throws IOException {
        Files.copy(source.toPath(), dest.toPath());
    }

    /**
     * 排除html不可显示字符
     * @param content 原内容
     * @return 重构后内容
     */
    private String rebuild(String content){
        char[] contentChars = new char[content.length()];
        content.getChars(0, content.length(), contentChars, 0);
        for (int i = 0; i < content.length(); i ++){
            if ((int)contentChars[i] >= (int)'\uD000'){
                contentChars[i] = ' ';
            }
        }
        return String.copyValueOf(contentChars);
    }

    //读取pdf文档
    public String getTextFromPdf(String pdfPath) throws Exception {
        // 是否排序
        boolean sort = false;

        // 开始提取页数
        int startPage = 1;

        // 结束提取页数
        int endPage = Integer.MAX_VALUE;

        String content = null;
        RandomAccessFile input = null;
        File pdfFile = new File(pdfPath);
        PDDocument document = null;

        try {
            input = new RandomAccessFile(pdfFile, "rw");

            // 加载 pdf 文档
            PDFParser parser = new PDFParser(input);
            parser.parse();
            document = parser.getPDDocument();

            // 获取内容信息
            PDFTextStripper pts = new PDFTextStripper();
            pts.setSortByPosition(sort);
            endPage = document.getNumberOfPages();
            //System.out.println("Total Page: " + endPage);
            pts.setStartPage(startPage);
            pts.setEndPage(endPage);
            try {
                content = pts.getText(document);
            } catch (Exception e) {
                throw e;
            }

            //System.out.println("Get PDF Content ...");

        } catch (Exception e) {
            throw e;

        } finally {
            if (null != input)
                input.close();

            if (null != document)
                document.close();
        }
        //重构
        return rebuild(content);
    }
}
