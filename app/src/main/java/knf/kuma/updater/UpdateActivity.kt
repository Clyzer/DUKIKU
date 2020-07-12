package knf.kuma.updater

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import knf.kuma.R
import knf.kuma.commons.getUpdateDir
import knf.kuma.custom.GenericActivity
import knf.kuma.download.DownloadManager
import kotlinx.android.synthetic.main.activity_updater.*
import java.io.File

class UpdateActivity : GenericActivity() {

    private val updaterViewModel: UpdaterViewModel by viewModels()
    private val update: File by lazy { File(filesDir, "update.apk") }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_updater)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        download.setOnClickListener { install() }
        progress.max = 100
        val animationDrawable = rel_back.background as AnimationDrawable
        if (!animationDrawable.isRunning) {
            animationDrawable.setEnterFadeDuration(2500)
            animationDrawable.setExitFadeDuration(2500)
            animationDrawable.start()
        }
        updaterViewModel.start(update, "https://github.com/jordyamc/UKIKU/raw/master/app/$getUpdateDir/app-$getUpdateDir.apk")
                .observe(this, Observer {
                    when (it.first) {
                        UpdaterType.TYPE_IDLE -> {
                            progress.isIndeterminate = true
                        }
                        UpdaterType.TYPE_PROGRESS -> {
                            setDownProgress(it.second as Int)
                        }
                        UpdaterType.TYPE_ERROR -> {
                            finish()
                        }
                        UpdaterType.TYPE_COMPLETED -> {
                            progress.progress = 100
                            progress_text.text = "100%"
                            prepareForInstall()
                        }
                    }
                })
    }

    private fun install() {
        DownloadManager.pauseAll()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE, FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", update))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, false)
                    .putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, packageName)
            startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(Uri.fromFile(update), "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivity(intent)
        }
    }

    private fun setDownProgress(p: Int) {
        try {
            progress.apply {
                isIndeterminate = false
                progress = p
            }
            progress_text.text = String.format("%d%%", p)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun prepareForInstall() {
        setDownProgress(100)
        val fadein = AnimationUtils.loadAnimation(this, R.anim.fadein)
        fadein.duration = 1000
        val fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout)
        fadeout.duration = 1000
        progress_text.post {
            with(progress_text) {
                visibility = View.INVISIBLE
                startAnimation(fadeout)
            }
        }
        download.post {
            with(download) {
                visibility = View.VISIBLE
                startAnimation(fadein)
            }
        }
    }

    companion object {

        fun start(context: Context) {
            context.startActivity(Intent(context, UpdateActivity::class.java))
        }
    }
}
