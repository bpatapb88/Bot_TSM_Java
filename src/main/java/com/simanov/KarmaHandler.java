package com.simanov;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Arrays;

public class KarmaHandler {
    String[] positiveTriggers = {"хвалю","одобряю", "спасибо", "благодарю"};
    String[] negativeTriggers = {"осуждаю","порицаю"};
    private User nominated;
    private User judge;
    private Message replay;

    public KarmaHandler(Message replay) {
        this.replay = replay;
        this.nominated = replay.getReplyToMessage().getFrom();
        this.judge = replay.getFrom();
    }

    public SendMessage changeKarma(DatabaseHandler databaseHandler) {
        SendMessage resultMessage = new SendMessage();
        String mentionNominated = "[" + nominated.getFirstName() + "](tg://user?id=" + nominated.getId() + ")";
        String mentionJudge = "[" + judge.getFirstName() + "](tg://user?id=" + judge.getId() + ")";
        resultMessage.setChatId(replay.getChatId());
        resultMessage.setParseMode("Markdown");
        String[] massageString = replay.getText().toLowerCase().split(" ");
        for (String trigger : positiveTriggers){
            if (Arrays.asList(massageString).contains(trigger)){
                int newKarma = databaseHandler.changeKarmaDB(nominated,true);
                if (newKarma%5==0){
                    resultMessage.setText(mentionJudge + " повысил карму " + mentionNominated + " до " + newKarma);
                    return resultMessage;
                }else{
                    System.out.println("Karma was incremented but not sent");
                }
            }
        }
        for (String trigger : negativeTriggers){
            if (Arrays.asList(massageString).contains(trigger)){
                int newKarma = databaseHandler.changeKarmaDB(nominated,false);
                if (newKarma%5==0){
                    resultMessage.setText(mentionJudge + " понизил карму " + mentionNominated + " до " + newKarma);
                    return resultMessage;
                }else{
                    System.out.println("Karma was decremented but not sent");
                }
            }
        }
        return null;
    }
}
