package knf.kuma.animeinfo

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import knf.kuma.commons.doOnUI
import knf.kuma.commons.jsoupCookies
import knf.kuma.commons.noCrashLet
import knf.kuma.database.CacheDB
import knf.kuma.pojos.AnimeObject
import knf.kuma.retrofit.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync

class AnimeViewModel : ViewModel() {
    private val repository = Repository()
    val liveData: MutableLiveData<AnimeObject?> = MutableLiveData()

    fun init(context: Context, link: String?, persist: Boolean) {
        link?.let {
            if (it.contains("/ver/")) {
                GlobalScope.launch(Dispatchers.Main) {
                    val nLink = withContext(Dispatchers.IO) { "https://animeflv.net" + noCrashLet { jsoupCookies(it).get().select("a[href~=/anime/]").attr("href") } }
                    repository.getAnime(context, nLink, persist, liveData)
                }
            } else
                repository.getAnime(context, link, persist, liveData)
        }
    }

    fun init(aid: String?) {
        doAsync {
            aid?.let {
                val animeObject = CacheDB.INSTANCE.animeDAO().getAnimeByAid(aid)
                doOnUI { liveData.value = animeObject }
            } ?: doOnUI { liveData.value = null }
        }
    }

    fun reload(context: Context, link: String?, persist: Boolean) {
        init(context, link, persist)
    }
}
