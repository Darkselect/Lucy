package io.proj3ct.LucyDemoBot.config;

import com.vdurmont.emoji.EmojiParser;
import io.proj3ct.LucyDemoBot.model.WeatherModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class Weather {
    //token of weather site 636153360e46fa961782ac797cd02f24

    public static String getWeather(String message, WeatherModel model) throws IOException {
        //Нам нужно послать запрос к нашему API
        URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + message + "&units=metric&appid=636153360e46fa961782ac797cd02f24");

        Scanner in = new Scanner((InputStream) url.getContent());
        StringBuilder result = new StringBuilder();
        while (in.hasNext()) {
            result.append(in.nextLine());
        }

        JSONObject object = new JSONObject(result.toString().toString());
        model.setName(object.getString("name"));

        JSONObject main = object.getJSONObject("main");
        model.setTemp(main.getDouble("temp"));
        model.setHumidity(main.getDouble("humidity"));

        JSONArray getArray = object.getJSONArray("weather");
        for (int i = 0; i < getArray.length(); i++) {
            JSONObject obj = getArray.getJSONObject(i);
            model.setIcon((String) obj.get("icon"));
            model.setMain((String) obj.get("main"));
        }

        return "Город: " + model.getName() + " " + EmojiParser.parseToUnicode(":city_sunrise:") + "\n" +
                "Температура : " + model.getTemp() + " °C" + " " + EmojiParser.parseToUnicode(":temperature:") + "\n" +
                "Влажность: " + model.getHumidity() + "%" + " " + EmojiParser.parseToUnicode(":droplet:") + "\n" +
                "Какая погодка : " + model.getMain() + "\n" +
                "http://openweathermap.org/img/w/" + model.getIcon() + ".png";



    }
}
