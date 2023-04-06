package io.proj3ct.LucyDemoBot.service;

import io.proj3ct.LucyDemoBot.config.BotConfig;
import io.proj3ct.LucyDemoBot.model.User;
import io.proj3ct.LucyDemoBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j

public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    //Прикрутить репозиторий
    private UserRepository userRepository;
    final BotConfig config;

    static final String HELP_TEXT = "Ознакомиться с моими командами вы сможете в левом нижнем углу: \n\n" +
            "Команда /start запускает меня\n\n" +
            "Команда /registration поздволяет полность зарегистрироваться вам как пользователю\n\n"+
            "Команда /mydata позволяет Вам посмотреть все ваши личные данные\n\n"+
            "Команда /deletedata позволяет Вам удалалить все ваши личные данные\n\n" +
            "Команда /help, открывает список всех доступных команд и функций\n\n" +
            "Команда /settings, позволяет вам отредактировать Ваши личные данные";

    public TelegramBot(BotConfig config) {
        this.config = config;

        //Лист всех комманд которые отображаются в меню
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/mydata", "get your data stored"));
        listofCommands.add(new BotCommand("/deletedata", "delete my data"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot"));
        listofCommands.add(new BotCommand("/settings", "set your preferences"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
       return config.getToken();
    }

    // Самый главный метод, именно здесь происходит взаимодействие с пользователем
    @Override
    public void onUpdateReceived(Update update) {

        //Проверка есть ли текст от пользователя, дабы не получить NullPointerException, убеждаемся что нам что-то прислали и там есть текс
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            //Чтобы бот нам написать, ему необходимо знать чат ID это ID которое индифицирует пользователя, бот может общаться одновременно множество людей, чтобы он знал, что кому отправлять
            long chatId = update.getMessage().getChatId();


            switch (messageText) {
                case "/start":

                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;

                default: sendMessage(chatId, "Извините, данная команда пока что не поддерживается");

            }
        }


    }

    private void registerUser(Message msg) {

        //Смотрим существует ли пользователь ... Дабы не повторять регистрацию данных еще раз
        if (userRepository.findById(msg.getChatId()).isEmpty()) {

            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved: " + user);


        }

    }

    private void startCommandReceived(long chatId, String name)  {


        String answer = "Привет, " + name + "! , приятно познакомится. Меня зовут Люси, бот помогающий найти ответ на любой вопрос! " +
                "\n\nДля начала зарегистрируйся пожалуйста :)";
        log.info("Replied to user " + name);


        sendMessage(chatId, answer);



    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
