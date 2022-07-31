package com.simanov;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.List;

public class NewUser {
    User newUser;

    public NewUser(User newUser) {
        this.newUser = newUser;
    }

    public SendMessage sendWelcomeButton() {
        System.out.println("sendWelcomeButton");
        String mention = "[" + newUser.getFirstName() + "](tg://user?id=" + newUser.getId() + ")";
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Зайти");
        inlineKeyboardButton.setCallbackData(newUser.getId() + "_login");

        List<InlineKeyboardButton> listButtons = Collections.singletonList(inlineKeyboardButton);
        List<List<InlineKeyboardButton>> listListButtons = Collections.singletonList(listButtons);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(listListButtons);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Привет " + mention + " Жми на ктопку Зайти");
        sendMessage.setChatId(Bot.CHAT_ID);
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        System.out.println("sendWelcomeButton [EXIT] " + sendMessage);
        return sendMessage;
    }

    public RestrictChatMember changePermission (Boolean allow){
        return RestrictChatMember.builder()
                .chatId(Bot.CHAT_ID)
                .userId(newUser.getId())
                .permissions(new ChatPermissions(allow,allow,allow,allow,allow,false,allow,false))
                .build();
    }

    public SendMessage welcomeMessage() {
        String mention = "[" + newUser.getFirstName() + "](tg://user?id=" + newUser.getId() + ")";
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Добро пожаловать, " + mention + ", в наш дружный и теплый чат\n" +
                "Вам тут очень рады. Мы организовываем разные активиты в Брне и не только.\n" +
                "@transsiberianway - Инфоканал где все ближайшие мероприятия \n" +
                "https://instagram.com/transsiberianway?utm_medium=copy_link - Инста \n" +
                "Представьтесь, пожалуйста и расскажите о себе! Нам интересно, вам полезно. А кто не представился - тот бот! \n");
        sendMessage.setParseMode("Markdown");
        sendMessage.setChatId(Bot.CHAT_ID);
        return sendMessage;
    }

}
