/*
 * Copyright (C) 2021-2021 Team Amaze - Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 * Emmanuel Messulam<emmanuelbendavid@gmail.com>, Raymond Lai <airwave209gt at gmail.com>. All Rights reserved.
 *
 * This file is part of Amaze File Utilities.
 *
 * 'Amaze File Utilities' is a registered trademark of Team Amaze. All other product
 * and company names mentioned are trademarks or registered trademarks of their respective owners.
 */

package com.amaze.fileutilities.home_page.ui.analyse

import androidx.lifecycle.*
import com.amaze.fileutilities.home_page.database.ImageAnalysis
import com.amaze.fileutilities.home_page.database.ImageAnalysisDao
import com.amaze.fileutilities.home_page.database.InternalStorageAnalysis
import com.amaze.fileutilities.home_page.database.InternalStorageAnalysisDao
import com.amaze.fileutilities.home_page.ui.files.MediaFileInfo
import com.amaze.fileutilities.utilis.PreferencesConstants
import com.amaze.fileutilities.utilis.invalidate
import kotlinx.coroutines.Dispatchers
import java.io.File

class AnalyseViewModel : ViewModel() {

    fun getBlurImages(dao: ImageAnalysisDao): LiveData<List<MediaFileInfo>?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(null)
            emit(transformAnalysisToMediaFileInfo(dao.getAllBlur(), dao))
        }
    }

    fun getLowLightImages(dao: ImageAnalysisDao): LiveData<List<MediaFileInfo>?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(null)
            emit(transformAnalysisToMediaFileInfo(dao.getAllLowLight(), dao))
        }
    }

    fun getMemeImages(dao: ImageAnalysisDao): LiveData<List<MediaFileInfo>?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(null)
            emit(transformAnalysisToMediaFileInfo(dao.getAllMeme(), dao))
        }
    }

    fun getSleepingImages(dao: ImageAnalysisDao): LiveData<List<MediaFileInfo>?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(null)
            emit(transformAnalysisToMediaFileInfo(dao.getAllSleeping(), dao))
        }
    }

    fun getSadImages(dao: ImageAnalysisDao): LiveData<List<MediaFileInfo>?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(null)
            emit(transformAnalysisToMediaFileInfo(dao.getAllSad(), dao))
        }
    }

    fun getDistractedImages(dao: ImageAnalysisDao): LiveData<List<MediaFileInfo>?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(null)
            emit(transformAnalysisToMediaFileInfo(dao.getAllDistracted(), dao))
        }
    }

    fun getSelfieImages(dao: ImageAnalysisDao): LiveData<List<MediaFileInfo>?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(null)
            emit(transformAnalysisToMediaFileInfo(dao.getAllSelfie(), dao))
        }
    }

    fun getGroupPicImages(dao: ImageAnalysisDao): LiveData<List<MediaFileInfo>?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(null)
            emit(transformAnalysisToMediaFileInfo(dao.getAllGroupPic(), dao))
        }
    }

    fun getClutteredVideos(videosList: List<MediaFileInfo>): LiveData<List<MediaFileInfo>?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.Default) {
            emit(null)
            val countIdx = IntArray(101) { 0 }
            videosList.forEach {
                it.extraInfo?.videoMetaData?.duration?.let {
                    duration ->
                    val idx = duration / 60
                    if (idx < 101) {
                        countIdx[idx.toInt()]++
                    }
                }
            }
            var maxIdxValue = 0
            var maxIdx = 0
            countIdx.forEachIndexed { index, i ->
                if (i > maxIdxValue) {
                    maxIdxValue = i
                    maxIdx = index
                }
            }
            val result = videosList.filter {
                it.extraInfo?.videoMetaData?.duration?.let {
                    duration ->
                    if ((duration / 60).toInt() == maxIdx) {
                        return@filter true
                    }
                }
                return@filter false
            }
            emit(result)
        }
    }

    fun getDuplicateDirectories(
        dao: InternalStorageAnalysisDao,
        searchMediaFiles: Boolean,
        deepSearch: Boolean
    ):
        LiveData<List<List<MediaFileInfo>>?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(null)
            emit(transformInternalStorageAnalysisToMediaFileList(dao, searchMediaFiles, deepSearch))
        }
    }

    fun getEmptyFiles(dao: InternalStorageAnalysisDao): LiveData<List<MediaFileInfo>?> {
        return liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(null)
            emit(transformInternalStorageAnalysisToMediaFile(dao))
        }
    }

    private fun transformInternalStorageAnalysisToMediaFile(dao: InternalStorageAnalysisDao):
        List<MediaFileInfo> {
        val analysis = dao.getAllEmptyFiles()
        val response = analysis.filter {
            it.invalidate(dao)
        }.map {
            MediaFileInfo.fromFile(
                File(it.files[0]),
                MediaFileInfo.ExtraInfo(
                    MediaFileInfo.MEDIA_TYPE_UNKNOWN,
                    null, null, null
                )
            )
        }
        return response
    }

    private fun transformInternalStorageAnalysisToMediaFileList(
        dao: InternalStorageAnalysisDao,
        searchMediaFiles: Boolean,
        deepSearch: Boolean
    ):
        List<List<MediaFileInfo>> {
        val analysis: List<InternalStorageAnalysis> = when {
            searchMediaFiles -> {
                dao.getAllMediaFiles()
            }
            deepSearch -> {
                dao.getAll()
            }
            else -> {
                dao.getAllShallow(PreferencesConstants.DEFAULT_DUPLICATE_SEARCH_DEPTH_INCL)
            }
        }
        val response = analysis.filter {
            it.invalidate(dao)
        }.filter {
            it.files.size > 1
        }.map {
            it.files.map {
                filePath ->
                MediaFileInfo.fromFile(
                    File(filePath),
                    MediaFileInfo.ExtraInfo(
                        MediaFileInfo.MEDIA_TYPE_UNKNOWN,
                        null, null, null
                    )
                )
            }
        }
        return response
    }

    private fun transformAnalysisToMediaFileInfo(
        imageAnalysis: List<ImageAnalysis>,
        dao: ImageAnalysisDao
    ):
        List<MediaFileInfo> {
        val response = imageAnalysis.filter {
            it.invalidate(dao)
        }.map {
            MediaFileInfo.fromFile(
                File(it.filePath),
                MediaFileInfo.ExtraInfo(
                    MediaFileInfo.MEDIA_TYPE_IMAGE,
                    null, null, null
                )
            )
        }
        return response
    }
}
