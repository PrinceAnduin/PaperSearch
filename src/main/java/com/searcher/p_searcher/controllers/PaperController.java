package com.searcher.p_searcher.controllers;

import com.searcher.p_searcher.entities.Paper;
import com.searcher.p_searcher.entities.PaperTag;
import com.searcher.p_searcher.entities.Tag;
import com.searcher.p_searcher.mappers.PaperMapper;
import com.searcher.p_searcher.mappers.TagMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("papers")
public class PaperController {

    @Autowired
    SolrClient solrClient;
    @Autowired
    TagMapper tagMapper;
    @Autowired
    PaperMapper paperMapper;
    //private static final Logger LOGGER = LoggerFactory.getLogger(SolrImportDataFromPostgresqlSchedule.class);

    @RequestMapping("/test")
    public String test() {
        String x = "\uE057\uE057\uE057";
        return "index";
    }

    @RequestMapping(value = "/tags", method = GET)
    public List<PaperTag> getTags(@RequestParam(value = "keyword") int paperId) {
        return tagMapper.queryTags(paperId);
    }

    @RequestMapping(value = "/search", method = GET)
    public String deliveringPackages(@RequestParam(value = "keyword", defaultValue = "") String keyword,
                                     @RequestParam(value = "id", defaultValue = "-1") int id,
                                     @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
                                     @RequestParam(value = "pageSize", defaultValue = "5") int pageSize, Model model, HttpSession session) {
//        user = (User) session.getAttribute("loginUser");
//        model.addAttribute("loginUser", user);
        if (id != -1) {
            //        user = (User) session.getAttribute("loginUser");
//        model.addAttribute("loginUser", user);
            model.addAttribute("pageNo", pageNo);
            try {
                List<Paper> paperList;
                HashMap<Tag, Integer> tagMap = new HashMap<>();
                List<Tag> tagList = new ArrayList<>();
                paperList = paperMapper.findByTag(id);
                model.addAttribute("allPages", paperList.size() / pageSize + 1);
                for (Paper paper : paperList) {
                    List<PaperTag> tags = tagMapper.queryTags(paper.getId());
                    paper.setTags(tags);
                    for (PaperTag pt : tags) {
                        Tag t = pt.getTag();
                        if (tagMap.get(t) == null) {
                            tagMap.put(t, 1);
                        } else {
                            tagMap.put(t, tagMap.get(t) + 1);
                        }
                    }
                }
                List<Map.Entry<Tag,Integer>> tList = tagMap.entrySet().stream()
                        .sorted((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()))
                        .collect(Collectors.toList());
                for (Map.Entry<Tag,Integer> t : tList) {
                    tagList.add(t.getKey());
                }
                model.addAttribute("papers", paperList);
                model.addAttribute("tags", tagList);
            } catch (Exception e) {
                //LOGGER.info(e.getMessage());
                e.printStackTrace();
            }
            return "index";
        }

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.addHighlightField("name,content");

        model.addAttribute("keyword", keyword);

        solrQuery.setHighlightSimplePre("<font color='red'>");
        solrQuery.setHighlightSimplePost("</font>");
        //若设置了关键词
        if (!keyword.equals("")) {
            solrQuery.set("q", keyword);
            solrQuery.set("df", "paper_resources");
        } else {
            //否则查询全部
            solrQuery.set("q", "*:*");
        }
        int aboveS;
        int start = pageNo * pageSize;
        model.addAttribute("pageNo", pageNo);
        solrQuery.set("start", start);
        solrQuery.set("rows", pageSize);
        try {
            QueryResponse queryResponse = solrClient.query(solrQuery);
            SolrDocumentList results = queryResponse.getResults();
            long allNum = results.getNumFound();
            model.addAttribute("allPages", allNum / pageSize + 1);
            ArrayList<Paper> paperList = new ArrayList<>();
            HashMap<Tag, Integer> tagMap = new HashMap<>();
            //高亮
            Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
            List<Tag> tagList = new ArrayList<>();
            for (SolrDocument result : results) {
                Paper paper = new Paper();
                //System.out.println(result);
                paper.setId(Integer.parseInt(result.get("id").toString()));
                paper.setName(result.get("name").toString());
                paper.setAuthor(result.get("author").toString());
                paper.setContent(result.get("content").toString());
                paper.setPath(result.get("path").toString());
                Map<String, List<String>> map = highlighting.get(result.get("id"));
                List<String> list = map.get("name");
                if (null != list && list.size() > 0) {
                    String name = list.get(0);
                    paper.setName(name);
                }
                //截取字符串，使高亮部分位于第一位
                List<String> list1 = map.get("content");
                if (null != list1 && list1.size() > 0) {
                    String description = list1.get(0);
//                    int index = description.indexOf("<font color='red'>");
//                    if (index != -1 && index != 0) {
//                        description = "..." + description.substring(index);
//                    }
                    paper.setContent(description);
                }
                List<PaperTag> tags = tagMapper.queryTags(paper.getId());
                paper.setTags(tags);
                paperList.add(paper);
                for (PaperTag pt : tags) {
                    Tag t = pt.getTag();
                    if (tagMap.get(t) == null) {
                        tagMap.put(t, 1);
                    } else {
                        tagMap.put(t, tagMap.get(t) + 1);
                    }
                }
            }
            List<Map.Entry<Tag,Integer>> tList = tagMap.entrySet().stream()
                    .sorted((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()))
                    .collect(Collectors.toList());
            for (Map.Entry<Tag,Integer> t : tList) {
                tagList.add(t.getKey());
            }
            model.addAttribute("papers", paperList);
            model.addAttribute("tags", tagList);
        } catch (Exception e) {
            //LOGGER.info(e.getMessage());
            System.out.println(e.getMessage());
        }
        return "index";
    }


    @RequestMapping(value = "/searchByTag", method = GET)
    public String searchByTag(@RequestParam(value = "id") int id,
                                     @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
                                     @RequestParam(value = "pageSize", defaultValue = "5") int pageSize, Model model, HttpSession session) {
//        user = (User) session.getAttribute("loginUser");
//        model.addAttribute("loginUser", user);
        model.addAttribute("pageNo", pageNo);
        try {
            List<Paper> paperList;
            HashMap<Tag, Integer> tagMap = new HashMap<>();
            List<Tag> tagList = new ArrayList<>();
            paperList = paperMapper.findByTag(id);
            model.addAttribute("allPages", paperList.size() / pageSize + 1);
            for (Paper paper : paperList) {
                List<PaperTag> tags = tagMapper.queryTags(paper.getId());
                paper.setTags(tags);
                for (PaperTag pt : tags) {
                    Tag t = pt.getTag();
                    if (tagMap.get(t) == null) {
                        tagMap.put(t, 1);
                    } else {
                        tagMap.put(t, tagMap.get(t) + 1);
                    }
                }
            }
            List<Map.Entry<Tag,Integer>> tList = tagMap.entrySet().stream()
                    .sorted((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()))
                    .collect(Collectors.toList());
            for (Map.Entry<Tag,Integer> t : tList) {
                tagList.add(t.getKey());
            }
            model.addAttribute("papers", paperList);
            model.addAttribute("tags", tagList);
        } catch (Exception e) {
            //LOGGER.info(e.getMessage());
            e.printStackTrace();
        }
        return "index";
    }


    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String download(@RequestParam("path") String path, HttpServletResponse response) throws UnsupportedEncodingException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(14);
        String filepath = path;
        String[] x = path.split("/");
        String name = x[x.length - 1];
        String[] y = path.split("///");
        filepath = y[y.length - 1];
        System.out.println(filepath);
        // 如果文件名不为空，则进行下载
        if (filepath != null) {
            File file = new File(filepath);
            // 如果文件存在，则进行下载
            if (file.exists()) {
                // 配置文件下载
                response.setHeader("content-type", "application/octet-stream");
                response.setContentType("application/octet-stream");
                // 下载文件能正常显示中文
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(name, "UTF-8"));
                // 实现文件下载
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    OutputStream os = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }
                    System.out.println("Download successfully!");
                    return "successfully";

                } catch (Exception e) {
                    System.out.println("Download  failed!");
                    return "failed";

                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return "";
    }
}