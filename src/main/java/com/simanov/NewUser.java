package com.simanov;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collections;
import java.util.List;

public class NewUser extends User {


    public SendMessage sendWelcomeButton() {
        String mention = "[" + this.getFirstName() + "](tg://user?id=" + this.getId() + ")";
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Зайти");
        inlineKeyboardButton.setCallbackData(this.getId() + "_login");
        List<InlineKeyboardButton> listButtons = Collections.singletonList(inlineKeyboardButton);
        List<List<InlineKeyboardButton>> listListButtons = Collections.singletonList(listButtons);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(listListButtons);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Привет " + mention + " Жми на ктопку Зайти");
        sendMessage.setChatId(Bot.CHAT_ID);
        sendMessage.setParseMode("Markdown");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }
}
