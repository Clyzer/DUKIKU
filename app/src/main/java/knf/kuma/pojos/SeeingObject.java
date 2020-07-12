package knf.kuma.pojos;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import knf.kuma.database.CacheDBWrap;
import knf.kuma.seeing.FavToSeeing;

@Keep
@Entity
public class SeeingObject {
    @SerializedName("STATE_WATCHING")
    @Ignore
    public static final int STATE_WATCHING = 1;
    @SerializedName("STATE_CONSIDERING")
    @Ignore
    public static final int STATE_CONSIDERING = 2;
    @SerializedName("STATE_COMPLETED")
    @Ignore
    public static final int STATE_COMPLETED = 3;
    @SerializedName("STATE_DROPPED")
    @Ignore
    public static final int STATE_DROPPED = 4;
    @SerializedName("STATE_PAUSED")
    @Ignore
    public static final int STATE_PAUSED = 5;

    @SerializedName("key")
    @PrimaryKey
    public int key;
    @SerializedName("img")
    public String img;
    @SerializedName("link")
    public String link;
    @SerializedName("aid")
    public String aid;
    @SerializedName("title")
    public String title;
    @SerializedName("chapter")
    public String chapter;
    @SerializedName("state")
    public int state;
    @SerializedName("lastChapter")
    @Ignore
    public SeenObject lastChapter;
    @Ignore
    public static DiffUtil.ItemCallback<SeeingObject> diffCallback = new DiffUtil.ItemCallback<SeeingObject>() {
        @Override
        public boolean areItemsTheSame(@NonNull SeeingObject oldItem, @NonNull SeeingObject newItem) {
            return oldItem.aid.equals(newItem.aid) && oldItem.key == newItem.key;
        }

        @Override
        public boolean areContentsTheSame(@NonNull SeeingObject oldItem, @NonNull SeeingObject newItem) {
            return oldItem.state == newItem.state && oldItem.chapter.equals(newItem.chapter);
        }
    };

    public SeeingObject(int key, String img, String link, String aid, String title, String chapter, int state) {
        this.key = key;
        this.img = img;
        this.link = link;
        this.aid = aid;
        this.title = title;
        this.chapter = chapter;
        this.lastChapter = FavToSeeing.INSTANCE.getLast(CacheDBWrap.INSTANCE.seenDAO().getAllByAid(aid));
        this.state = state;
    }

    @Ignore
    private SeeingObject() {
    }

    @Ignore
    public static SeeingObject fromAnime(FavoriteObject favoriteObject) {
        SeeingObject item = new SeeingObject();
        item.key = Integer.parseInt(favoriteObject.aid);
        item.img = favoriteObject.img;
        item.link = favoriteObject.link;
        item.aid = favoriteObject.aid;
        item.title = favoriteObject.name;
        item.chapter = "No empezado";
        return item;
    }

    @Ignore
    public static SeeingObject fromAnime(AnimeObject animeObject, int state) {
        SeeingObject item = new SeeingObject();
        item.key = Integer.parseInt(animeObject.aid);
        item.img = animeObject.img;
        item.link = animeObject.link;
        item.aid = animeObject.aid;
        item.title = animeObject.name;
        item.chapter = "No empezado";
        item.state = state;
        return item;
    }
}
