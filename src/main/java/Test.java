import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.collect.ImmutableMap;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.apache.commons.io.IOUtils.copyLarge;

/**
 * @description:
 * @author: jiangtao.tian
 * @createDate: 2021/3/16
 */
public class Test {
    static Logger logger = Logger.getLogger(Test.class.getCanonicalName());
    static ExecutorService executorService = Executors.newFixedThreadPool(20);
    static final Map<String, String> mapTextStyle = ImmutableMap.of(
            // "下載 updb 檔", "updb"
            // "下載 prc 檔", "prc"
            "下載直式 mobi 檔", "mobi"
            //"下載 epub 檔", "epub"
    );

    static final Map<String, String> mapTitle = ImmutableMap.of(
             "http://www.haodoo.net/?M=hd&P=100", "世紀百強",
             "http://www.haodoo.net/?M=hd&P=wisdom", "隨身智囊",
            "http://www.haodoo.net/?M=hd&P=history", "歷史煙雲",
            "http://www.haodoo.net/?M=hd&P=martial","武俠小說",
            "http://www.haodoo.net/?M=hd&P=mystery","懸疑小說"
            //"下載 epub 檔", "epub"
    );
    static {
        mapTitle.put("http://www.haodoo.net/?M=hd&P=scifi","奇幻小說");//
        mapTitle.put("http://www.haodoo.net/?M=hd&P=romance","言情小說");
        mapTitle.put("http://www.haodoo.net/?M=hd&P=fiction","小說園地");
    }

            /**
             * @param args
             * @return
             * @description
             * @author Liruilong
             * @date 2020年10月15日  03:10:12
             **/

    public static void main(String[] args) {


        //String[] strings = new String[]{"http://www.haodoo.net/?M=hd&P=100",
        // "http://www.haodoo.net/?M=hd&P=wisdom", "http://www.haodoo.net/?M=hd&P=history",
        // "http://www.haodoo.net/?M=hd&P=martial", "http://www.haodoo.net/?M=hd&P=mystery",
         //       "http://www.haodoo.net/?M=hd&P=scifi", "http://www.haodoo.net/?M=hd&P=romance", "http://www.haodoo.net/?M=hd&P=fiction"};
        String[] strings = new String[]{"http://www.haodoo.net/?M=hd&P=100"};

        for (int j = 0; j < strings.length; j++) {
            try {
                Document doc = null;
                int finalJ = j;
                doc = Jsoup.connect(strings[j]).get();
                Elements s = doc.select("a[href]");
                logger.info("爬取：" + strings[j] + "__________---------——————————————" + Thread.currentThread().getName());
                List<Element> elements = s.stream().filter(a -> a.attr("abs:href").indexOf("book") != -1 && a.text().length() > 1)
                        .collect(Collectors.toList());
                executorService.execute(() -> {
                    for (int i = 0; i < elements.size(); i++) {
                        try {
                            WebClient webclient = new WebClient();
                            logger.info("爬取：" + elements.get(i).text() + "__________---------——————————————" + Thread.currentThread().getName());
                            HtmlPage htmlpage = null;
                            htmlpage = webclient.getPage(elements.get(i).attr("abs:href"));
                            List<DomElement> domElements = htmlpage.getElementsByTagName("input").stream().filter(o ->
                                    mapTextStyle.containsKey(o.getAttribute("value"))).collect(Collectors.toList());
                            for (int i1 = 0; i1 < domElements.size(); i1++) {
                                try {
                                    String textNameStyle = mapTextStyle.get(domElements.get(i1).getAttribute("value"));
                                    logger.info("爬取：" + elements.get(i).text() + "___" + textNameStyle + "_______---------——————————————" + Thread.currentThread().getName());
                                    HtmlPage page = domElements.get(i1).click();
                                    TimeUnit.SECONDS.sleep(2);
                                    DomElement button = page.getElementById("okButton");
                                    if (Objects.isNull(button)) {
                                        TimeUnit.SECONDS.sleep(2);
                                    }
                                    final InputStream inputStream = button.click().getWebResponse().getContentAsStream();
                                    saveFile(inputStream, elements.get(i).text(), textNameStyle,strings[finalJ]);
                                } catch (Exception e) {
                                    continue;
                                }
                            }
                            webclient.close();//关掉
                        } catch (IOException e) {
                            continue;
                        }
                    }

                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * @param io
     * @param s
     * @return 文件保存
     * @description
     * @author Liruilong
     * @date 2020年10月15日  00:10:06
     **/

    public static void saveFile(InputStream io, String... s) {
        executorService.execute(() -> {
            logger.info("-----------------------------------------------------------------------导入开始：" + s[0] + "__________---------——————————————" + Thread.currentThread().getName());
            File dir= new File("F:\\books\\" + s[2]);
            try (
                    OutputStream outputStream = new FileOutputStream(new File("F:\\books\\" + s[0]
                    .replaceAll("【", "").replaceAll("】", "")
                    .replaceAll("《", "").replaceAll("》", "") + "." + s[1]));) {
                copyLarge(io, outputStream);
                outputStream.flush();
                logger.info("-------------------------------------------------------------------------导入结束：" + s[0] + "__________---------——————————————" + Thread.currentThread().getName());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

