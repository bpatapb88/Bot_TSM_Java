package com.simanov;

import com.google.common.io.Resources;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import static com.simanov.Main.logger;

public class Bot extends TelegramLongPollingBot {
    private DatabaseHandler databaseHandler = new DatabaseHandler();
    public static final Long CHAT_ID = -1001623594259L;

    private static final String COMMAND = "psql -d raspdb -c ";

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
                Connection connection;
                try {
                    connection = databaseHandler.getDbConnection();
                    databaseHandler.selectQuery(connection);
                } catch (SQLException | ClassNotFoundException throwable) {
                    throwable.printStackTrace();
                }
                System.out.println("done");
            }

            //New member join
            if(!receivedMessage.getNewChatMembers().isEmpty()){
                NewUser newUser = (NewUser) receivedMessage.getNewChatMembers().get(0);
                changePermission(newUser,false);
                sendWelcomeButton(newUser);
//                try {
//                    execute(newUser.sendWelcomeButton());
//                } catch (TelegramApiException e) {
//                    e.printStackTrace();
//                }
            }

            //Member left chat
            if(receivedMessage.getLeftChatMember() != null){
                sayGoodBye();
            }

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

    private void loginButtonClicked(CallbackQuery callbackQuery) {
        if(callbackQuery.getData().split("_")[0].equals(callbackQuery.getFrom().getId().toString())){
            NewUser newUser = (NewUser) callbackQuery.getFrom();
            changePermission(callbackQuery.getFrom(),true);
            DeleteMessage deleteMessage = new DeleteMessage(CHAT_ID.toString(), callbackQuery.getMessage().getMessageId());
            try {
                execute(deleteMessage);
                execute(welcomeMessage(callbackQuery.getFrom()));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

    private SendMessage welcomeMessage(User welcomedUser) {

        String mention = "[" + welcomedUser.getFirstName() + "](tg://user?id=" + welcomedUser.getId() + ")";
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Добро пожаловать, " + mention + ", в наш дружный и теплый чат\n" +
                "Вам тут очень рады. Мы организовываем разные активиты в Брне и не только.\n" +
                "@transsiberianway - Инфоканал где все ближайшие мероприятия \n" +
                "https://instagram.com/transsiberianway?utm_medium=copy_link - Инста \n" +
                "Представьтесь, пожалуйста и расскажите о себе! Нам интересно, вам полезно. А кто не представился - тот бот! \n");
        sendMessage.setParseMode("Markdown");
        sendMessage.setChatId(CHAT_ID);
        return sendMessage;
    }

    private boolean checkIfChatCorrect(Message message){
        if(!Objects.equals(message.getChatId(), CHAT_ID)){
            System.out.println("Bot is used in " + message.getChatId() + "\n" + message);
            return false;
        }
        return true;
    }

    public void changePermission (User user, Boolean allow){
        RestrictChatMember restrictChatMember = RestrictChatMember.builder()
                    .chatId(CHAT_ID)
                    .userId(user.getId())
                    .permissions(new ChatPermissions(allow,allow,allow,allow,allow,false,allow,false))
                    .build();

        try {
            execute(restrictChatMember);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sayGoodBye() {
        System.out.println("Arrividercchi");
    }

    private void karmaChangeAction() {
        System.out.println("What did you say? Karma +/-");
    }

    private void sendWelcomeButton(User newUser) {
        String mention = "[" + newUser.getFirstName() + "](tg://user?id=" + newUser.getId() + ")";
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Зайти");
        inlineKeyboardButton.setCallbackData(newUser.getId() + "_login");
        List<InlineKeyboardButton> listButtons = Collections.singletonList(inlineKeyboardButton);
        List<List<InlineKeyboardButton>> listListButtons = Collections.singletonList(listButtons);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(listListButtons);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Привет " + mention + " Жми на ктопку Зайти");
        sendMessage.setChatId(CHAT_ID);
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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
