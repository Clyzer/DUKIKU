package knf.kuma.explorer.creator

import androidx.documentfile.provider.DocumentFile
import knf.kuma.database.CacheDB
import knf.kuma.pojos.ExplorerObject

class DocumentFileCreator(private val rootDF: DocumentFile?) : Creator {

    override fun exist(): Boolean = rootDF?.exists()
            ?: false

    override fun createDirectoryList(progressCallback: (Int, Int) -> Unit): List<ExplorerObject> {
        rootDF ?: return emptyList()
        val directories = rootDF.listFiles().filter { it.isDirectory }
        val list = mutableListOf<ExplorerObject>()
        var progress = 0
        CacheDB.INSTANCE.animeDAO().getAllByFile(directories.mapNotNull { it.name }.toMutableList()).forEach {
            try {
                progress++
                progressCallback(progress, directories.size)
                list.add(ExplorerObject(it, createSubFilesList(it.getFinalName())))
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
        return list
    }

    override fun createSubFilesList(fileName: String): List<SubFile> {
        rootDF ?: return emptyList()
        return rootDF.findFile(fileName)?.listFiles()?.filter { it.name?.endsWith(".mp4") == true }?.map {
            SubFile(it.name ?: "", it.uri.toString())
        } ?: emptyList()
    }
}