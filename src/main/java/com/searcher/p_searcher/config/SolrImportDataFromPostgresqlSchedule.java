package com.searcher.p_searcher.config;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class SolrImportDataFromPostgresqlSchedule {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrImportDataFromPostgresqlSchedule.class);

    //@Scheduled(cron = "0/5 * * * * ?")
    //或直接指定时间间隔，例如：30秒
    @Scheduled(fixedRate=30000)
    private void configureTasks() {
        String uri =
                "http://localhost:8983/solr/paper_searcher/dataimport?command=full-import&clean=%20true&commit=true&wt=json&indent=true&verbose=false&optimize=false&debug=false&id=1";
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(uri);

            client = HttpClients.createDefault();
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            LOGGER.info(result);
        } catch (ClientProtocolException ex) {
            LOGGER.info(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.info(ex.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ex) {
                    LOGGER.info(ex.getMessage());
                }
            }
            if (client != null) {
                try {
                    client.close();
                } catch (IOException ex) {
                    LOGGER.info(ex.getMessage());
                }
            }
        }
    }
}
