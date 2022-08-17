package com.simanov;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileInputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    static Logger logger;
    static {
        try(FileInputStream ins = new FileInputStream("log.config")){
            LogManager.getLogManager().readConfiguration(ins);
            logger = Logger.getLogger(Main.class.getName());
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
    }


    public static void main(String[] args) {
	// write your code here
        try {
            Bot bot = new Bot();
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
//            ActionSendPeriod actionSendPeriod = new ActionSendPeriod(bot);
//            actionSendPeriod.run();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
