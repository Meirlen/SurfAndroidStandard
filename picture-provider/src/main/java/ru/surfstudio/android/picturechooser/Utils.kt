/*
  Copyright (c) 2018-present, SurfStudio LLC.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package ru.surfstudio.android.picturechooser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.support.annotation.WorkerThread
import io.reactivex.Observable
import ru.surfstudio.android.core.ui.navigation.ScreenResult
import ru.surfstudio.android.core.ui.navigation.activity.navigator.ActivityNavigator
import ru.surfstudio.android.core.ui.navigation.activity.route.ActivityWithResultRoute
import ru.surfstudio.android.picturechooser.exceptions.ActionInterruptedException
import java.io.*

const val DEFAULT_TEMP_FILE_NAME = "image.jpg"

//region Вспомогательные функции для роутеров
fun getIntentForSingleImageFromGallery(): Intent {
    return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
}

fun getIntentForMultipleImageFromGallery(): Intent {
    return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .apply {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
}

fun createChooser(intent: Intent, message: String): Intent {
    return Intent.createChooser(intent, message)
}

/**
 * Функция парсинга результата выбора единственного фото
 *
 * @param intent интент с результатом
 * @param parseUri лямбда, осуществляющая приведение к типу [T]
 */
fun <T : Serializable> parseSingleResultIntent(intent: Intent?,
                                               parseUri: (uri: Uri) -> T
): T? {
    return if (intent != null && intent.data != null) {
        parseUri(intent.data)
    } else {
        null
    }
}

/**
 * Функция парсинга результата выбора нескольких фото
 *
 * @param intent интент с результатом
 * @param parseUris лямбда, осуществляющая приведение к списку типа [T]
 */
fun <T : Serializable> parseMultipleResultIntent(intent: Intent?,
                                                 parseUris: (uris: ArrayList<Uri>) -> ArrayList<T>
): ArrayList<T>? {
    return when {
        intent == null -> null
        intent.clipData != null -> with(intent.clipData) {
            parseUris((0 until itemCount).mapTo(ArrayList()) { getItemAt(it).uri })
        }
        intent.data != null -> parseUris(arrayListOf((intent.data)))
        else -> null
    }
}
//endregion

//region Вспомогательные функции для обработки результата открытия экрана
fun <T : Serializable> observeSingleScreenResult(activityNavigator: ActivityNavigator,
                                                 route: ActivityWithResultRoute<T>): Observable<T> {
    return activityNavigator.observeResult<T>(route)
            .flatMap { parseScreenResult(it) }
}

fun <T : Serializable> observeMultipleScreenResult(activityNavigator: ActivityNavigator,
                                                   route: ActivityWithResultRoute<ArrayList<T>>): Observable<List<T>> {
    return activityNavigator.observeResult<ArrayList<T>>(route)
            .flatMap { parseScreenResult(it) }
            .map { it as List<T> }
}

fun <T : Serializable> parseScreenResult(screenResult: ScreenResult<T>,
                                         throwable: () -> Throwable = { ActionInterruptedException() }): Observable<T> {
    return if (screenResult.isSuccess) {
        Observable.just(screenResult.data)
    } else {
        Observable.error(throwable)
    }
}
//endregion

//region Вспомогательные функции для парсинга Uri
/**
 * Функция, возвращающая путь ко всем файлам в списке по их Uri
 */
fun ArrayList<Uri>.getRealPaths(activity: Activity) = mapIndexed { i, uri ->
    uri.getRealPath(activity, i.toJpgString())
} as ArrayList

/**
 * Функция, возвращающая путь к файлу по его Uri
 */
fun Uri.getRealPath(activity: Activity, tempFileName: String = DEFAULT_TEMP_FILE_NAME): String {
    val result: String
    val column = MediaStore.Images.ImageColumns.DATA
    val projection = arrayOf(column)

    val cursor = activity.applicationContext.contentResolver
            .query(this, projection, null, null, null)
    if (cursor == null) {
        result = this.path
    } else {
        cursor.moveToFirst()
        val idx = cursor.getColumnIndexOrThrow(column)
        result = if (idx > -1) {
            cursor.getString(idx) ?: createTempBitmap(activity, tempFileName)
        } else {
            this.path
        }
        cursor.close()
    }
    return result
}

/**
 * Функция, преобразующая список Uri в список строк
 */
fun ArrayList<Uri>.toStringArrayList() = map { it.toString() } as ArrayList

/**
 * Функция, преобразующая список Uri в список UriWrapper
 */
fun ArrayList<Uri>.toUriWrapperList() = map { UriWrapper(it) } as ArrayList

/**
 * Функция, создающая из целочисленного числа название для изображения, например 123.jpg
 */
fun Int.toJpgString() = "$this.jpg"
//endregion

//region Вспомогательные функции работы с Bitmap при извлечении путей к изображениям
/**
 * Функция, создающая временный файл для изображения по его Uri.
 *
 * @return String путь к созданному временному файлу
 */
fun Uri.createTempBitmap(context: Context, tempFileName: String): String {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, this)
    return saveTempBitmap(bitmap, tempFileName, context).absolutePath
}

/**
 * Сохранение изображения во временную папку для передачи между интентами
 */
@WorkerThread
@Throws(IOException::class)
fun saveTempBitmap(bitmap: Bitmap, fileName: String, context: Context): File {
    val file = File(context.cacheDir, fileName)
    if (file.exists()) file.delete()
    file.createNewFile()

    val bos = ByteArrayOutputStream()
    bos.use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos)
        val bitmapData = bos.toByteArray()
        val fos = FileOutputStream(file)
        fos.use {
            it.write(bitmapData)
        }
    }
    return file
}
//endregion