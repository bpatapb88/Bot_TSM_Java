package com.simanov;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PollHandler {
    private final int NOT_ASKED_YET = 0;
    private final int ASKED_QUESTION = 1;
    private final int ASKED_OPTION = 2;

    User initiatorAdmin;
    DatabaseHandler databaseHandler;
    Message receivedMessage;

    public PollHandler(Message recivedMessage, DatabaseHandler databaseHandler) {
        this.receivedMessage = recivedMessage;
        this.initiatorAdmin = recivedMessage.getFrom();
        this.databaseHandler = databaseHandler;
    }

    public SendMessage startCreatingPool() {
        if(getUserStatus() == NOT_ASKED_YET){
            databaseHandler.incrementStatus(initiatorAdmin);
            return sendMessagePM("Введить вопрос на голосование:");
        }else{
            return sendMessagePM("Вы уже в процессе создания опроса");
        }
    }

    public BotApiMethodMessage endCreatingOrSend() {
        if(getUserStatus() == ASKED_QUESTION){
            // TODO: create new table in DB for Polls
            //databaseHandler.saveNewPoll(receivedMessage);
            databaseHandler.incrementStatus(initiatorAdmin);
            return sendMessagePM("Введить опции через запятую. Положительные должны заканчиваться \"!\"");
        }else if (getUserStatus() == ASKED_OPTION){
            List<String> optionsCorrect = verifyAnswer(receivedMessage.getText());
            if (!optionsCorrect.isEmpty()) {
                //String question = databaseHandler.getQuestion(receivedMessage.getFrom().getId());
                String question = "Test question";
                //databaseHandler.addOptionsToPoll(receivedMessage);
                SendPoll poll = new SendPoll();
                poll.setChatId(Bot.CHAT_ID);
                poll.setAllowMultipleAnswers(false);
                poll.setIsAnonymous(false);
                poll.setQuestion(question);
                poll.setOptions(optionsCorrect);
                //databaseHandler.resetStatus(initiatorAdmin);
                return poll;
            }
            return sendMessagePM("Опции введены неправильно!");
        }else{
            return sendMessagePM("Отправте команду");
        }
    }

    private List<String> verifyAnswer(String text) {
        boolean optionsAreGood = text.contains(",") && text.contains("!") && text.length()>4;
        String[] options = text.split(",");
        for (String option: options){
            optionsAreGood = optionsAreGood && !option.isEmpty();
        }
        return optionsAreGood ? Arrays.asList(options): new ArrayList<>();
    }

    private int getUserStatus(){
        return databaseHandler.getStatus(initiatorAdmin);
    }

    private SendMessage sendMessagePM(String text){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(initiatorAdmin.getId());
        sendMessage.setParseMode("Markdown");
        return sendMessage;
    }
}
