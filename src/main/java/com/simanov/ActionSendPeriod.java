package com.simanov;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ActionSendPeriod implements Runnable{
    String URL_JOKE = "http://bomz.org/bash/?bash=random";
    String URL_FACTS = "https://randstuff.ru/fact/";
    HttpURLConnection connection;
    Bot bot;

    public ActionSendPeriod(Bot bot) {
        this.bot = bot;
    }

    private SendMessage getSendJoke() {
        Document document = getDocumentJsoup(URL_JOKE);
        Elements elements = document.getElementsByAttributeValue("style", "border-right: 1px dashed #D8D8D8;border-bottom: 1px dashed #F0F0F0;border-top: 1px dashed #F0F0F0;");
        String text = elements.get(0).text();
        return new SendMessage(Bot.CHAT_ID.toString(),text);
    }

    private Document getDocumentJsoup(String urlString){
        Document document = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");
            InputStream responseStream = connection.getInputStream();
            document = Jsoup.parse(responseStream, "windows-1251", urlString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    @Override
    public void run() {
        while (true){
            try {
                bot.execute(getSendJoke());
                Thread.sleep(50000);
            } catch (TelegramApiException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
