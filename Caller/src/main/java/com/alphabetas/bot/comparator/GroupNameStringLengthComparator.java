package com.alphabetas.bot.comparator;


import com.alphabetas.bot.model.GroupName;

import java.util.Comparator;

public class GroupNameStringLengthComparator implements Comparator<GroupName> {

    @Override
    public int compare(GroupName o1, GroupName o2) {
        String s1 = o1.getName();
        String s2 = o2.getName();
        if (s1.length() != s2.length())
            return s2.length() - s1.length();
        return s1.compareTo(s2);
    }
}
