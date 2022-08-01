package com.simanov;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ActionSendPeriod {
    String URL_JOKE = "http://bomz.org/bash/?bash=random";
    String URL_FACTS = "https://randstuff.ru/fact/";

    public ActionSendPeriod() {
        try {
            URL url = new URL(URL_JOKE);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");
            InputStream responseStream = connection.getInputStream();
            Document document = Jsoup.parse(responseStream, "UTF-8", URL_JOKE);
            System.out.println(document.body());

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            Document doc = Jsoup.connect(URL_JOKE).data("query", "Java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(3000)
                    .get();
            System.out.println(doc.title());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
