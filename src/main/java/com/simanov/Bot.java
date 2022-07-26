package com.simanov;

import com.google.common.io.Resources;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;
import static com.simanov.Main.logger;

public class Bot extends TelegramLongPollingBot {
    private DatabaseHandler databaseHandler = new DatabaseHandler();
    //public static final Long CHAT_ID = -1001623594259L;
    public static final Long CHAT_ID = -1001623594259L;

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if(update.getMessage() != null){
            Message receivedMessage = update.getMessage();
            //Avoid using bot in others chats
            if (!checkIfChatCorrect(receivedMessage)){
                System.out.println("Chat is incorrect");
                return;
            }

            //Handle commands
            if(receivedMessage.isCommand()){
                System.out.println("Command was sent " + databaseHandler.getSocialValue(receivedMessage.getFrom().getId(),"InvitedFriends"));
                return;
            }

            //New member join
            if(!receivedMessage.getNewChatMembers().isEmpty()){
                //TODO: add check if new user is bot
                System.out.println("New member join");
                NewUser newUser = new NewUser(receivedMessage.getNewChatMembers().get(0));
                System.out.println(newUser);
                System.out.println("invitedBy " + receivedMessage.getFrom());
                if (receivedMessage.getFrom() != null){
                    databaseHandler.incrementInvited(receivedMessage.getFrom().getId());
                }

                try {
                    execute(newUser.changePermission(false));
                    execute(newUser.sendWelcomeButton());
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }

            //Member left chat
            if(receivedMessage.getLeftChatMember() != null){
                sayGoodBye();
                return;
            }

            incrementMessageCounter(receivedMessage.getFrom());

            //Replay to somebody who's not bot
            if(receivedMessage.getReplyToMessage() != null &&
                    !receivedMessage.getReplyToMessage().getFrom().getIsBot() &&
                    !Objects.equals(receivedMessage.getFrom().getId(), receivedMessage.getReplyToMessage().getFrom().getId())){
                karmaChangeAction();
            }
        }

        if(update.hasCallbackQuery()){
            loginButtonClicked(update.getCallbackQuery());
        }

//        System.out.println("Test");
//        System.out.println("message - " + update.getMessage());
//        System.out.println("message - " + update.getMessage().getNewChatMembers());

        //logger.log(Level.INFO, "message= {0}", update.getMessage());

    }

    private void incrementMessageCounter(User from) {
        databaseHandler.incrementMessagesDB(from.getId());
    }

    private void loginButtonClicked(CallbackQuery callbackQuery) {
        //Check if clicked by user which should be registered
        if(callbackQuery.getData().split("_")[0].equals(callbackQuery.getFrom().getId().toString())){
            NewUser newUser = new NewUser(callbackQuery.getFrom());
            DeleteMessage deleteMessage = new DeleteMessage(CHAT_ID.toString(), callbackQuery.getMessage().getMessageId());
            try {
                execute(newUser.changePermission(true));
                databaseHandler.registerUserInDB(callbackQuery.getFrom());
                execute(deleteMessage);
                execute(newUser.welcomeMessage());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean checkIfChatCorrect(Message message){
        if(!Objects.equals(message.getChatId(), CHAT_ID)){
            System.out.println("Bot is used in " + message.getChatId() + "\n" + message);
            return false;
        }
        return true;
    }


    private void sayGoodBye() {
        System.out.println("Arrividercchi");
    }

    private void karmaChangeAction() {
        System.out.println("What did you say? Karma +/-");
    }

    /**
     * This method returns the bot's name, which was specified during registration.
     * @return bot name
     */
    @Override
    public String getBotUsername() {
        return "tsm_test88_bot";
    }

    /**
     * This method returns the bot's token for communicating with the Telegram server
     * @return the bot's token
     */
    @Override
    public String getBotToken() {
        String apiKey = "";
        try {
            URL url = Resources.getResource("Config.txt");
            apiKey = Resources.toString(url, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.log(Level.INFO, "Not possible to send Bot Token {0}.", e.toString());
            e.printStackTrace();
        }
        return apiKey;
    }
}
