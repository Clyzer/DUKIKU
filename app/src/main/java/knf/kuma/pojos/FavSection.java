package knf.kuma.pojos;

import knf.kuma.search.SearchAdvObject;

public class FavSection extends FavoriteObject {
    private FavSection(int key, String aid, String name, String img, String type, String link, String category) {
        super(key, aid, name, img, type, link, category);
    }

    private FavSection(AnimeObject object) {
        super(object);
    }

    public FavSection(String name) {
        super((SearchAdvObject) null);
        if (name.equals(CATEGORY_NONE))
            this.name = "Sin categoría";
        else
            this.name = name;
        this.isSection = true;
    }
}
