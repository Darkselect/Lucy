package io.proj3ct.LucyDemoBot.service;

import com.vdurmont.emoji.EmojiParser;
import io.proj3ct.LucyDemoBot.config.BotConfig;
import io.proj3ct.LucyDemoBot.config.Weather;
import io.proj3ct.LucyDemoBot.model.User;
import io.proj3ct.LucyDemoBot.model.UserRepository;
import io.proj3ct.LucyDemoBot.model.WeatherModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
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
            "Команда /start запускает меня\n\n"+
            "Команда /register позволяет Вам зарегистрироваться\n\n" +
            "Команда /mydata позволяет Вам посмотреть все ваши личные данные\n\n"+
            "Команда /deletedata позволяет Вам удалалить все ваши личные данные\n\n" +
            "Команда /help, открывает список всех доступных команд и функций\n\n" +
            "Команда /settings, позволяет Вам отредактировать Ваши личные данные\n\n" +
            "Так же вы моежете ввести в строку город и посмотреть погоду";

    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";
    static final String ERROR_TEXT = "Error occurred: ";

    public TelegramBot(BotConfig config) {
        this.config = config;

        //Лист всех комманд которые отображаются в меню
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начать работу"));
        listOfCommands.add(new BotCommand("/register", "Регистрация"));
        listOfCommands.add(new BotCommand("/mydata", "Посмотреть свои данные"));
        listOfCommands.add(new BotCommand("/deletedata", "Удалить свои данные"));
        listOfCommands.add(new BotCommand("/settings", "Настройки"));
        listOfCommands.add(new BotCommand("/help", "Помощь"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
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
   /* @Override
    public void onUpdateReceived(Update update) {

        WeatherModel weatherModel = new WeatherModel();
        Message message = update.getMessage();

        //Проверка есть ли текст от пользователя, дабы не получить NullPointerException, убеждаемся что нам что-то прислали и там есть текст
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            //Чтобы бот нам написал, ему необходимо знать чат ID это ID которое индифицирует пользователя, бот может общаться одновременно множество людей, чтобы он знал, что кому отправлять
            long chatId = update.getMessage().getChatId();

            //Рассылка всем пользователям определенного сообщения, которое задается вручную
            if (messageText.contains("/send") && config.getOwnerId() == chatId) {
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users = userRepository.findAll();
                for (User user: users) {
                    prepareAndMessage(user.getChatId(), textToSend);
                }


            } else {

                switch (messageText) {
                    case "/start":

                        registerUser(update.getMessage());
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        break;

                    case "/help":
                        prepareAndMessage(chatId, HELP_TEXT);
                        break;

                    case "/register":
                        register(chatId);
                        break;


                    case "Погода": {
                        try {
                            sendMessage(chatId, "Напишите город");
                            sendMessage(chatId, Weather.getWeather(message.getText(), weatherModel));
                        } catch (IOException e) {
                            e.printStackTrace();
                            sendMessage(chatId, "Исключение результата");
                        }

                        break;
                    }
//
                    default:
                    {
                        sendMessage(chatId, "Извините данная команда не поддерживается");
                    }


                }
            }

        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals(YES_BUTTON)) {
                String text = "Вы нажали кнопку: \"Да\"";
                executeEditMessageText(text, chatId, messageId);

            }
            else if (callBackData.equals(NO_BUTTON)) {
                String text = "Вы нажали кнопку: \"Нет\"";
                executeEditMessageText(text, chatId, messageId);
            }
        }


    }
*/
    @Override
    public void onUpdateReceived(Update update) {

        //Проверка есть ли текст от пользователя, дабы не получить NullPointerException, убеждаемся что нам что-то прислали и там есть текст
        if (update.hasMessage() && update.getMessage().hasText()) {

            String messageText = update.getMessage().getText();

            //Чтобы бот нам написал, ему необходимо знать чат ID это ID которое индифицирует пользователя, бот может общаться одновременно множество людей, чтобы он знал, что кому отправлять
            long chatId = update.getMessage().getChatId();



            //Рассылка всем пользователям определенного сообщения, которое задается вручную
            if (messageText.contains("/send") && config.getOwnerId() == chatId) {
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users = userRepository.findAll();
                for (User user : users) {
                    prepareAndMessage(user.getChatId(), textToSend);
                }

                return ;
            }

        }


            WeatherModel model = new WeatherModel();
            Message message = update.getMessage();


            if (message != null && message.hasText()) {
                switch (message.getText()) {
                    case "/start":
                        registerUser(update.getMessage());
                        startCommandReceived(message.getChatId(), update.getMessage().getChat().getFirstName());
                        break;

                    case "/help":
                        prepareAndMessage(message.getChatId(), HELP_TEXT);
                        break;

                    case "/register":
                        register(message.getChatId());
                        break;

                    case "/setting":
                        sendMessage(message.getChatId(), "Что будем настраивать?");
                        break;

                    default:
                        try {
                            sendMessage(message.getChatId(), Weather.getWeather(message.getText(), model));
                        } catch (IOException e) {
                            sendMessage(message.getChatId(), "Извините данная команда не поддерживается");
                        }


            }
        } else if (update.hasCallbackQuery()) {
                String callBackData = update.getCallbackQuery().getData();
                long messageId = update.getCallbackQuery().getMessage().getMessageId();
                long chatId = update.getCallbackQuery().getMessage().getChatId();

                if (callBackData.equals(YES_BUTTON)) {
                    String text = "Вы нажали кнопку: \"Да\"";
                    executeEditMessageText(text, chatId, messageId);

                }
                else if (callBackData.equals(NO_BUTTON)) {
                    String text = "Вы нажали кнопку: \"Нет\"";
                    executeEditMessageText(text, chatId, messageId);
                }
            }




    }



    private void register(long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вы согласны зарегистрироваться?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var yesButton  = new InlineKeyboardButton();
        yesButton.setText("Yes");

        //индификатор позволяющий понять боту какая кнопка была нажата
        yesButton.setCallbackData(YES_BUTTON);

        var noButton = new InlineKeyboardButton();

        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        executeMessage(message);

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



        String answer = EmojiParser.parseToUnicode("Привет, " + name + " , приятно познакомится! Меня зовут Люси, бот помогающий найти ответ на любой вопрос! "
                + "\n\nДля начала зарегистрируйся пожалуйста " + " :blush:");

        log.info("Replied to user " + name);


        sendMessage(chatId, answer);



    }




    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);



        //Клавиатура для ответа
      /*
       ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("Погода");
        row.add("Список заданий");

        keyboardRows.add(row);

        row = new KeyboardRow();


        keyboardRows.add(row);


        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);.

       */

        executeMessage(message);

    }

    private void executeEditMessageText(String text, long chatId, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(text);
        message.setMessageId((int) messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void prepareAndMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

}
