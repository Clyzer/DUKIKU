package knf.kuma.pojos;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Created by jordy on 26/03/2018.
 */
@Keep
@Entity
public class GenreStatusObject implements Comparable<GenreStatusObject> {
    public String name;
    public int count;
    @PrimaryKey
    public int key;

    public GenreStatusObject() {
        this.key = 0;
        this.name = "";
        this.count = 0;
    }

    public GenreStatusObject(int key, String name, int count) {
        this.key = key;
        this.name = name;
        this.count = count;
    }

    @Ignore
    public GenreStatusObject(String name) {
        this.key = Math.abs(name.hashCode());
        this.name = name;
        this.count = 0;
    }

    public static List<String> names(List<GenreStatusObject> list) {
        List<String> names = new ArrayList<>();
        for (GenreStatusObject object : list)
            names.add(object.name);
        return names;
    }

    public boolean isBlocked() {
        return count < 0;
    }

    public void add(int number) {
        count += number;
    }

    public void sub(int number) {
        count -= number;
        if (count < 0) count = 0;
    }

    public void block() {
        count = -1;
    }

    public void reset() {
        count = 0;
    }

    @Override
    public int compareTo(@NonNull GenreStatusObject o) {
        return name.compareTo(o.name);
    }
}
