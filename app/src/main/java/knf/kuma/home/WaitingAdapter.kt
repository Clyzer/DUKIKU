package knf.kuma.home

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import knf.kuma.R
import knf.kuma.animeinfo.ActivityAnime
import knf.kuma.commons.*
import knf.kuma.pojos.SeeingObject
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick

class WaitingAdapter(val fragment: HomeFragment) : UpdateableAdapter<WaitingAdapter.RecentViewHolder>() {

    private var list: List<SeeingObject> = emptyList()

    override fun updateList(list: List<Any>) {
        doAsync {
            this@WaitingAdapter.list = list.transform()
            doOnUI { notifyDataSetChanged() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder = RecentViewHolder(parent.inflate(R.layout.item_fav_grid_card_simple))


    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        val item = list[position]
        holder.img.load(PatternUtil.getCover(item.aid))
        holder.title.text = item.title
        holder.root.onClick { ActivityAnime.open(fragment.activity, item, holder.img) }
    }

    class RecentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View by itemView.bind(R.id.card)
        val img: ImageView by itemView.bind(R.id.img)
        val title: TextView by itemView.bind(R.id.title)
    }
}