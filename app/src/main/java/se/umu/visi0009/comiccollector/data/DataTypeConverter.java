package se.umu.visi0009.comiccollector.data;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import se.umu.visi0009.comiccollector.enums.AchievementDifficulty;
import se.umu.visi0009.comiccollector.enums.CardCondition;

public class DataTypeConverter {

    @TypeConverter
    public static Date converterLongToDate(Long longValue) {
        return longValue == null ? null : new Date(longValue);
    }

    @TypeConverter
    public static Long converterDatetoLong(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String converterArrayListToString(ArrayList<String> list) {
        return list == null ? null : new Gson().toJson(list);
    }

    @TypeConverter
    public static ArrayList<String> converterStringToArrayList(String string) {

        if(string == null) {
            new ArrayList<String>();
        }

        Type listType = new TypeToken<ArrayList<String>>() {}.getType();

        return new Gson().fromJson(string, listType);
    }

    @TypeConverter
    public static CardCondition converterStringToCardCondition(String string) {
        return string == null ? null : CardCondition.valueOf(string);
    }

    @TypeConverter
    public static String converterCardConditionToString(CardCondition cardCondition) {
        return cardCondition == null ? null : cardCondition.name();
    }

    @TypeConverter
    public static AchievementDifficulty converterStringToAchievementDifficulty(String string) {
        return string == null ? null : AchievementDifficulty.valueOf(string);
    }

    @TypeConverter
    public static String converterAchievementDifficultyToString(AchievementDifficulty achievementDifficulty) {
        return achievementDifficulty == null ? null : achievementDifficulty.name();
    }
}
