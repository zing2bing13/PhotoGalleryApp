package com.example.assignment1.Util.Filter;

import android.media.ExifInterface;
import android.util.Log;

import com.example.assignment1.Models.ImageExifModel;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

public class ImageFilter implements Serializable {
    public Date StartDate;
    public Date EndDate;
    public String Caption;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    /***
     * Gets a list of filters to filter with
     * @return a list of filters
     */
    public List<Predicate<ImageExifModel>> GetFilters() {
        List<Predicate<ImageExifModel>> filters = new ArrayList<>();

        // Issue with parsing the EXIF data from the image to convert it to a format Date can compare to
        // If start date exist add a filter for the start date
        if (this.StartDate != null) {
            filters.add(f -> {

                try {
                    String dateString = f.ExifData.getAttribute(ExifInterface.TAG_DATETIME);

                    String dateParts[] = dateString.split("[: ]");
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]);
                    int day = Integer.parseInt(dateParts[2]);
                    int hour = Integer.parseInt(dateParts[3]);
                    int minute = Integer.parseInt(dateParts[4]);
                    int second = Integer.parseInt(dateParts[5]);
                    month = month - 1; //month is zero-based index

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DATE, day);
                    calendar.set(Calendar.HOUR, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, second);
                    Date imageDate = calendar.getTime();

                    //Log.d("new_date", imageDate.toString());

                    //Log.d("error_date", f.ExifData.getAttribute(ExifInterface.TAG_DATETIME));
                    SimpleDateFormat date = new SimpleDateFormat(DATE_FORMAT);
                    date.format(imageDate);

                    if (imageDate.compareTo(this.StartDate) > 0) {
                        return true;
                    } else {
                        return false;
                    }

            } catch (Exception ex) {
                }
                return false;
            });
        }

        // If the end date exist add a filter for the end date
        if (this.EndDate != null) {
            filters.add(f -> {
                try {
                    String dateString = f.ExifData.getAttribute(ExifInterface.TAG_DATETIME);

                    String dateParts[] = dateString.split("[: ]");
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]);
                    int day = Integer.parseInt(dateParts[2]);
                    int hour = Integer.parseInt(dateParts[3]);
                    int minute = Integer.parseInt(dateParts[4]);
                    int second = Integer.parseInt(dateParts[5]);
                    month = month - 1; // month is zero-based index

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DATE, day);
                    calendar.set(Calendar.HOUR, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, second);
                    Date imageDate = calendar.getTime();

                    //Log.d("new_date", imageDate.toString());

                    //Log.d("error_date", f.ExifData.getAttribute(ExifInterface.TAG_DATETIME));
                    SimpleDateFormat date = new SimpleDateFormat(DATE_FORMAT);
                    date.format(imageDate);

                    if (imageDate.compareTo(this.EndDate) < 0) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception ex) {
                    }
                return false;
            });
        }

        // If caption filter exist add a filter for caption
        if(this.Caption != null && this.Caption.length() != 0) {
            filters.add(f -> {
                try {
                    String imageCaption = f.ExifData.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
                    if (imageCaption != null) {
                        return imageCaption.toLowerCase().contains(this.Caption.toLowerCase());
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            });
        }

        return filters;
    }
}
