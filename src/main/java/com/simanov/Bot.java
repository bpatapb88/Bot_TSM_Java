package com.simanov;

import com.google.common.io.Resources;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import static com.simanov.Main.logger;

public class Bot extends TelegramLongPollingBot {
    private DatabaseHandler databaseHandler = new DatabaseHandler();
    public static final Long CHAT_ID = -1001623594259L;
    public static final Long BOT_ID = 173780137L;

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update){
        try {
        System.out.println("onUpdateReceived() [ENTER] update = " + update);

        if(update.getMessage() != null){
            Message receivedMessage = update.getMessage();
            if(isMessageSendToBotPM(receivedMessage)){
                PollHandler pollHandler = new PollHandler(receivedMessage,databaseHandler);
                if(isCommandToCreatePoll(receivedMessage)){
                    System.out.println("CREATE POLL START!!!");
                    // ask first question
                    execute(pollHandler.startCreatingPool());
                }else{
                    //ask second question

                    execute(pollHandler.endCreatingOrSend());
                }
            }

            //Avoid using bot in others chats
            else if (isMassageSentNotToTheChat(receivedMessage)){
                System.out.println("Chat is incorrect");
                return;
            }

            //Handle commands
            if(receivedMessage.isCommand()){

                return;
            }

            //New member join
            if(!receivedMessage.getNewChatMembers().isEmpty() && !isBot(receivedMessage.getNewChatMembers().get(0))){
                System.out.println("New member join");
                NewUser newUser = new NewUser(receivedMessage.getNewChatMembers().get(0));
                System.out.println(newUser);
                System.out.println("invitedBy " + receivedMessage.getFrom());
                if (receivedMessage.getFrom() != null){
                    databaseHandler.incrementInvited(receivedMessage.getFrom().getId());
                }
                execute(newUser.changePermission(false));
                execute(newUser.sendWelcomeButton());
                return;
            }

            //Member left chat
            if(receivedMessage.getLeftChatMember() != null && !isBot(receivedMessage.getLeftChatMember())){
                sayGoodBye(receivedMessage.getLeftChatMember());
                return;
            }

            // Messages ++
            if(!isBot(receivedMessage.getFrom())){
                incrementMessageCounter(receivedMessage.getFrom());
            }

            //Replay to somebody who's not bot
            if(receivedMessage.getReplyToMessage() != null &&
                    !isBot(receivedMessage.getReplyToMessage().getFrom()) &&
                    !Objects.equals(receivedMessage.getFrom().getId(), receivedMessage.getReplyToMessage().getFrom().getId())){
                KarmaHandler karmaHandler = new KarmaHandler(receivedMessage);
                int newKarma = karmaHandler.getNewKarma(databaseHandler);
                if (newKarma%karmaHandler.EACH == 0 && newKarma!= karmaHandler.getOldKarma()) {
                    execute(karmaHandler.getSendMessage());
                }
            }

            if(update.getMessage().getPoll() != null){
                System.out.println("pool = " + update.getMessage().getPoll().toString());
            }

        }



        if(update.hasCallbackQuery()){
            loginButtonClicked(update.getCallbackQuery());
        }

//        System.out.println("Test");
//        System.out.println("message - " + update.getMessage());
//        System.out.println("message - " + update.getMessage().getNewChatMembers());

        //logger.log(Level.INFO, "message= {0}", update.getMessage());

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean isMessageSendToBotPM(Message receivedMessage) {
        return Objects.equals(receivedMessage.getChatId(), receivedMessage.getFrom().getId());
    }

    private boolean isCommandToCreatePoll(Message receivedMessage) {
        return receivedMessage.getChatId().equals(receivedMessage.getFrom().getId()) &&
                isAdmin(receivedMessage.getFrom(), CHAT_ID) &&
                receivedMessage.isCommand() &&
                receivedMessage.getText().equals("/create_poll");
    }

    private boolean isAdmin(User from, Long chatId) {
        GetChatAdministrators getChatAdministrators = GetChatAdministrators.builder().chatId(chatId).build();
        try {
            List<ChatMember> admins = execute(getChatAdministrators);
            for (ChatMember chatMember: admins){
                if(chatMember.getUser().equals(from)){
                    return true;
                }
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void incrementMessageCounter(User from) {
        databaseHandler.incrementMessagesDB(from.getId());
    }

    private void loginButtonClicked(CallbackQuery callbackQuery) throws TelegramApiException{
        //Check if clicked by user which should be registered
        if(callbackQuery.getData().split("_")[0].equals(callbackQuery.getFrom().getId().toString()) &&
                callbackQuery.getData().split("_")[1].equals("login")){
            NewUser newUser = new NewUser(callbackQuery.getFrom());
            DeleteMessage deleteMessage = new DeleteMessage(CHAT_ID.toString(), callbackQuery.getMessage().getMessageId());
            execute(newUser.changePermission(true));
            databaseHandler.registerUserInDB(callbackQuery.getFrom());
            execute(deleteMessage);
            execute(newUser.welcomeMessage());
        }
    }


    private boolean isMassageSentNotToTheChat(Message message){
        if(!Objects.equals(message.getChatId(), CHAT_ID)){
            System.out.println("Bot is used in " + message.getChatId() + "\n" + message);
            return true;
        }
        return false;
    }


    private void sayGoodBye(User leftUser) throws TelegramApiException{
        String mention = "[" + leftUser.getFirstName() + "](tg://user?id=" + leftUser.getId() + ")";
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(CHAT_ID);
        sendMessage.setParseMode("Markdown");
        sendMessage.setText("Прощай " + mention + ", и ничего не общещай, и ничего не говори...");
        execute(sendMessage);
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

    public boolean isBot(User user){
        //TODO change to user.isBot() before release
        return false;
    }
}
