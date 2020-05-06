package com.example.assignment1.Util.Filter;

import android.media.ExifInterface;

import com.example.assignment1.Models.ImageExifModel;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

public class ImageFilter implements Serializable {
    public Date StartDate;
    public Date EndDate;
    public String Caption;
    private final String DATE_FORMAT = "yyyy-MM-dd";
    /***
     * Gets a list of filters to filter with
     * @return a list of filters
     */
    public List<Predicate<ImageExifModel>> GetFilters() {
        List<Predicate<ImageExifModel>> filters = new ArrayList<>();

        // If start date exist add a filter for the start date
        if (this.StartDate != null) {
            filters.add(f -> {
                try {
                    return new SimpleDateFormat(DATE_FORMAT).parse(f.ExifData.getAttribute(ExifInterface.TAG_DATETIME)).compareTo(this.StartDate) > 0;
                } catch (ParseException ex) {
                    return false;
                }
            });
        }

        // If the end date exist add a filter for the end date
        if (this.EndDate != null) {
            filters.add(f -> {
                try {
                    return new SimpleDateFormat(DATE_FORMAT).parse(f.ExifData.getAttribute(ExifInterface.TAG_DATETIME)).compareTo(this.EndDate) < 0;
                } catch (ParseException ex) {
                    return false;
                }
            });
        }

        // If caption filter exist add a filer for caption
        if(this.Caption != null) {
            filters.add(f -> f.ExifData.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION).contains(this.Caption));
        }

        return filters;
    }
}
