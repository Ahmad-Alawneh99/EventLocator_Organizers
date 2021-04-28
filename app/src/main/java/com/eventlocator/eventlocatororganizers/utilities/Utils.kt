package com.eventlocator.eventlocatororganizers.utilities


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.FileDescriptor
import java.io.IOException



class Utils {

    companion object{
        val instance: Utils = Utils()
    }

    fun isEmail(s: String): Boolean{
        if (s.indexOf('@') == -1 || s.indexOf('.') == -1)return false
        else if (!isChar(s[0]) || !isChar(s[s.length - 1])) return false
        var at = -1
        var lastDot = -1
        var atCount = 0
        for(i in s.length-1 downTo 0){
            if (s[i] == '@'){
                if (at == -1)at = i
                atCount++
            }
            else if (s[i] == '.'){
                if (lastDot == -1){
                    lastDot = i
                }
            }
        }
        if (atCount> 1 || at > lastDot) return false

        for (i in at until lastDot){
            if (s[i] == '.')return false;
        }
        return true
    }

    private fun isChar(c: Char): Boolean{
        return c in 'a'..'z' || c in 'A'..'Z'
    }

    private fun isDigit(c: Char): Boolean{
        return c in '0'..'9'
    }

    fun uriToBitmap(selectedFileUri: Uri, context: Context): Bitmap? {
        try {
            val parcelFileDescriptor: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(
                selectedFileUri,
                "r"
            )
            val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor?.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    fun countWords(s: String): Int {
        var count = 0
        var res = s.split(' ')
        for(i in res.indices){
            if (res[i].trim()!="")
                count++
        }
        return count
    }

    fun connectWordsIntoString(words: List<String>): String{
        var res = ""
        for(i in words.indices){
            if (words[i].trim()!="")
                res+=words[i]+' '
        }
        return res.trim()
    }

    fun displayInformationalDialog(context: Context, title: String, message:String, finish: Boolean){
        AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton("OK")
        { di, i -> if (finish) (context as Activity).finish() }.create().show()
    }

    fun createSimpleDialog(context: Context, title: String, message: String): AlertDialog.Builder{
        return AlertDialog.Builder(context).setTitle(title).setMessage(message)
    }

}