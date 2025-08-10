package com.alphabetas.bot.comparator;


import com.alphabetas.bot.model.Name;

import java.util.Comparator;

public class NameStringLengthComparator implements Comparator<Name> {


    @Override
    public int compare(Name Name, Name t1) {
        String s1 = Name.getName();
        String s2 = t1.getName();
        if (s1.length() != s2.length())
            return s2.length() - s1.length();
        return s1.compareTo(s2);
    }
}
