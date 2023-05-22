package com.wt.ocr.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UriUtils {
    /**
     * 获取真实路径
     * <p>
     * 支持以下
     * <p>
     * file://
     * content://media/external/file/109009
     * FileProvider适配
     * content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Tencent/QQfile_recv/
     * content://com.tencent.mm.external.fileprovider/external/tencent/MicroMsg/Download/
     * content://com.android.providers.downloads.documents"
     * content://com.android.externalstorage.documents
     * content://com.android.providers.media.documents
     * content://com.google.android.apps.photos.content
     */
    public static String getFileFromUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        switch (uri.getScheme()) {
            case ContentResolver.SCHEME_CONTENT:
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};

                    return getFilePathFromContentUri(context, contentUri, selection, selectionArgs);
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getFilePathFromContentUri(context, contentUri, null, null);
                }

                return getFilePathFromContentUri(context, uri, null, null);
            case ContentResolver.SCHEME_FILE:
            default:
                //file://
                return new File(uri.getPath()).getAbsolutePath();
        }
    }

    /**
     * 从uri获取path 或 拷贝
     */
    private static String getFilePathFromContentUri(Context context, Uri uri, String selection, String[] selectionArgs) {
        if (null == uri) return null;
        String data = null;

        String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, selection, selectionArgs, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                if (index > -1) {
                    data = cursor.getString(index);
                    if (data == null || !fileIsExists(data)) {
                        //可能拿不到真实路径 或 文件不存在  走拷贝流程
                        int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                        String fileName = cursor.getString(nameIndex);
                        data = getPathFromInputStreamUri(context, uri, fileName);
                    }
                } else {
                    //拷贝一份
                    int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(nameIndex);
                    data = getPathFromInputStreamUri(context, uri, fileName);
                }
            }
            cursor.close();
        }
        return data;
    }

    /**
     * 用流拷贝文件一份到自己APP私有目录下
     *
     * @param context
     * @param uri
     * @param fileName
     */
    private static String getPathFromInputStreamUri(Context context, Uri uri, String fileName) {
        InputStream inputStream = null;
        String filePath = null;

        if (uri.getAuthority() != null) {
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                File file = createTemporalFileFrom(context, inputStream, fileName);
                filePath = file.getPath();

            } catch (Exception e) {
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                }
            }
        }

        return filePath;
    }

    private static File createTemporalFileFrom(Context context, InputStream inputStream, String fileName)
            throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];
            //自己定义拷贝文件路径
            targetFile = new File(context.getExternalCacheDir(), fileName);
            if (targetFile.exists()) {
                targetFile.delete();
            }
            OutputStream outputStream = new FileOutputStream(targetFile);

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return targetFile;
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    //判断文件是否存在
    private static boolean fileIsExists(String filePath) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
