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
    private SendMessage sendMessage;
    private String mentionNominated;
    private String mentionJudge;
    private int oldKarma;

    public KarmaHandler(Message replay) {
        this.replay = replay;
        this.nominated = replay.getReplyToMessage().getFrom();
        this.judge = replay.getFrom();
        this.sendMessage = new SendMessage();
        this.mentionNominated = "[" + nominated.getFirstName() + "](tg://user?id=" + nominated.getId() + ")";
        this.mentionJudge = "[" + judge.getFirstName() + "](tg://user?id=" + judge.getId() + ")";
        sendMessage.setChatId(replay.getChatId());
        sendMessage.setParseMode("Markdown");
    }

    public SendMessage getSendMessage() {
        return sendMessage;
    }

    public int getOldKarma() {
        return oldKarma;
    }

    /**
     * getNewKarma()
     * Karma will increment if positive word is in a message
     * Karma will decrement if negative word is in a message and positive word NOT
     *
     * @param databaseHandler - provide access to DB
     * @return new value of karma. If message don't include trigger word, will be returned old value
     */
    public int getNewKarma(DatabaseHandler databaseHandler) {
        System.out.println("getNewKarma() [ENTER] nominated = " + nominated.toString());
        oldKarma = databaseHandler.getKarma(nominated);
        String[] massageString = replay.getText().toLowerCase().split(" ");
        for (String trigger : positiveTriggers){
            if (Arrays.asList(massageString).contains(trigger)){
                int newKarma = databaseHandler.changeKarmaDB(nominated,true);
                System.out.println("getNewKarma() newKarma = " + newKarma);
                sendMessage.setText(mentionJudge + " повысил/а карму " + mentionNominated + " до " + newKarma);
                return newKarma;
            }
        }
        for (String trigger : negativeTriggers){
            if (Arrays.asList(massageString).contains(trigger)){
                int newKarma = databaseHandler.changeKarmaDB(nominated,false);
                System.out.println("getNewKarma() newKarma = " + newKarma);
                sendMessage.setText(mentionJudge + " понизил/а карму " + mentionNominated + " до " + newKarma);
                return newKarma;
            }
        }
        return oldKarma;
    }
}
