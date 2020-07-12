package knf.kuma.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import knf.kuma.search.SearchAdvObject;

@Keep
@Entity
public class FavoriteObject implements Comparable<FavoriteObject> {
    @SerializedName("CATEGORY_NONE")
    @Ignore
    public static final String CATEGORY_NONE = "_NONE_";
    @SerializedName("key")
    @PrimaryKey
    public int key;
    @SerializedName("aid")
    public String aid;
    @SerializedName("name")
    public String name;
    @SerializedName("img")
    public String img;
    @SerializedName("type")
    public String type;
    @SerializedName("link")
    public String link;
    @SerializedName("category")
    public String category;
    @SerializedName("isSection")
    @Ignore
    public boolean isSection = false;

    @Ignore
    public FavoriteObject() {

    }

    public FavoriteObject(int key, String aid, String name, String img, String type, String link, String category) {
        this.key = key;
        this.aid = aid;
        this.name = name;
        this.img = img;
        this.type = type;
        this.link = link;
        this.category = category;
    }

    @Ignore
    public FavoriteObject(AnimeObject object) {
        if (object != null) {
            this.key = object.key;
            this.aid = object.aid;
            this.name = object.name;
            this.img = object.img;
            this.type = object.type;
            this.link = object.link;
            this.category = CATEGORY_NONE;
        }
    }

    @Ignore
    public FavoriteObject(SearchAdvObject object) {
        if (object != null) {
            this.key = object.getKey();
            this.aid = object.getAid();
            this.name = object.getName();
            this.img = object.getImg();
            this.type = object.getType();
            this.link = object.getLink();
            this.category = CATEGORY_NONE;
        }
    }

    public static List<String> getNames(List<FavoriteObject> list) {
        List<String> strings = new ArrayList<>();
        for (FavoriteObject object : list) {
            strings.add(object.name);
        }
        return strings;
    }

    public static List<String> getCategories(List<FavoriteObject> list) {
        List<String> strings = new ArrayList<>();
        for (FavoriteObject object : list) {
            if (object.category.equals(CATEGORY_NONE))
                strings.add("Sin categoría");
            else
                strings.add(object.category);
        }
        return strings;
    }

    public static Integer[] getIndex(List<FavoriteObject> list, String category) {
        List<Integer> index = new ArrayList<>();
        int i = 0;
        for (FavoriteObject object : list) {
            if (object.category.equals(category))
                index.add(i);
            i++;
        }
        return index.toArray(new Integer[0]);
    }

    public void setCategory(String category) {
        if (category == null)
            this.category = CATEGORY_NONE;
        else
            this.category = category;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + (isSection ? 1 : -1);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof FavSection || obj instanceof FavoriteObject) && name.equals(((FavoriteObject) obj).name);
    }

    @Override
    public int compareTo(@NonNull FavoriteObject o) {
        return name.compareTo(o.name);
    }
}
