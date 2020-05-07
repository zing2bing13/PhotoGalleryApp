package com.example.assignment1.Util.Filter;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Filter
{
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static <T> List ApplyFilters(List<T> source, List<Predicate<T>> filters) {

        List<T> filteredlist = source;
        // Note not very performant since it does a collect every time we add a filter...
        // TODO: Possibly build out the filter expression tree if performance is not good
        for(Predicate<T> filter : filters) {
            filteredlist = (List<T>) filteredlist
                            .stream()
                            .filter(filter)
                            .collect(Collectors.toList());
        }

        return filteredlist;
    }
}
