package ru.retverd.jira.reporter.progress.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

/**
 * Created by rtverdok on 10.04.2015.
 */
public class StringLocale extends XmlAdapter<String, Locale> {
    @Override
    public String marshal(Locale locale) {
        Locale localeValue;
        Class<Locale> c = Locale.class;
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod)) {
                try {
                    localeValue = (Locale) f.get(null);
                } catch (IllegalAccessException e) {
                    return null;
                }
                if (localeValue.equals(locale)) {
                    return f.getName();
                }
            }
        }
        return null;
    }

    @Override
    public Locale unmarshal(String localeString) {
        Field field;
        Locale localeValue;
        try {
            field = Locale.class.getField(localeString);
        } catch (NoSuchFieldException e) {
            return null;
        }
        try {
            localeValue = (Locale) field.get(null);
        } catch (IllegalAccessException e) {
            return null;
        }
        return localeValue;
    }
}
