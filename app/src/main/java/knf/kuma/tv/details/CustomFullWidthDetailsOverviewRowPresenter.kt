package knf.kuma.tv.details

import android.view.ViewGroup
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter
import androidx.leanback.widget.Presenter

class CustomFullWidthDetailsOverviewRowPresenter internal constructor(detailsPresenter: Presenter) : FullWidthDetailsOverviewRowPresenter(detailsPresenter) {

    private var mPreviousState = STATE_FULL

    init {
        initialState = STATE_FULL
    }

    override fun onLayoutLogo(viewHolder: ViewHolder, oldState: Int, logoChanged: Boolean) {
        val v = viewHolder.logoViewHolder.view
        val lp = v.layoutParams as ViewGroup.MarginLayoutParams

        lp.marginStart = v.resources.getDimensionPixelSize(
                androidx.leanback.R.dimen.lb_details_v2_logo_margin_start)
        lp.topMargin = v.resources.getDimensionPixelSize(androidx.leanback.R.dimen.lb_details_v2_blank_height) - lp.height / 2

        val offset = (v.resources.getDimensionPixelSize(androidx.leanback.R.dimen.lb_details_v2_actions_height) + v
                .resources.getDimensionPixelSize(androidx.leanback.R.dimen.lb_details_v2_description_margin_top) + lp.height / 2).toFloat()

        when (viewHolder.state) {
            STATE_FULL -> if (mPreviousState == STATE_HALF) {
                v.animate().translationYBy(-offset)
            }
            STATE_HALF -> if (mPreviousState == STATE_FULL) {
                v.animate().translationYBy(offset)
            }
            else -> if (mPreviousState == STATE_HALF) {
                v.animate().translationYBy(-offset)
            }
        }
        mPreviousState = viewHolder.state
        v.layoutParams = lp
    }
}
