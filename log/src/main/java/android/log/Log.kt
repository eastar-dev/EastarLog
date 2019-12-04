/*
 * Copyright 2017 copyright eastar Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.log

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources.NotFoundException
import android.database.Cursor
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.experimental.and

/** @author eastar*/
object Log {
    const val VERBOSE = android.util.Log.VERBOSE
    const val DEBUG = android.util.Log.DEBUG
    const val INFO = android.util.Log.INFO
    const val WARN = android.util.Log.WARN
    const val ERROR = android.util.Log.ERROR
    const val ASSERT = android.util.Log.ASSERT
    var LOG = true
    var FILE_LOG: File? = null
    var MODE = eMODE.STUDIO
    private const val PREFIX = "``"
    private const val PREFIX_MULTILINE = "$PREFIX▼"
    private const val LF = "\n"
    private const val MAX_LOG_LINE_BYTE_SIZE = 3600
    private val LOG_CLASS = Log::class.java.name
    private var LOG_PASS_REGEX = "^android\\..+|^com\\.android\\..+|^java\\..+".toRegex()
    fun p(priority: Int, vararg args: Any?): Int {
        if (!LOG) return -1
        val info = getStack()
        val tag = getTag(info)
        val locator = getLocator(info)
        val msg = _MESSAGE(*args)
        return println(priority, tag, locator, msg)
    }

    fun ps(priority: Int, info: StackTraceElement?, vararg args: Any): Int {
        if (!LOG) return -1
        val tag = getTag(info)
        val locator = getLocator(info)
        val msg = _MESSAGE(*args)
        return println(priority, tag, locator, msg)
    }

    fun println(priority: Int, tag: String, locator: String, msg: String?): Int {
        if (!LOG) return -1
        val sa = ArrayList<String>(100)
        val st = StringTokenizer(msg, LF, false)
        while (st.hasMoreTokens()) {
            val byte_text = st.nextToken().toByteArray()
            var offset = 0
            val N = byte_text.size
            while (offset < N) {
                val count = safeCut(byte_text, offset)
                sa.add(PREFIX + String(byte_text, offset, count))
                offset += count
            }
        }
        if (MODE == eMODE.STUDIO) {
            val DOTS = "...................................................................................."
            val sb = StringBuilder(DOTS)
            val last_tag = tag.substring(Math.max(tag.length + locator.length - DOTS.length, 0))
            sb.replace(0, last_tag.length, last_tag)
            sb.replace(sb.length - locator.length, sb.length, locator)
            val adj_tag = sb.toString()
            val N = sa.size
            if (N <= 0) return android.util.Log.println(priority, adj_tag, PREFIX)
            if (N == 1) return android.util.Log.println(priority, adj_tag, sa[0])
            var sum = android.util.Log.println(priority, adj_tag, PREFIX_MULTILINE)
            for (s in sa) sum += android.util.Log.println(priority, adj_tag, s)
            return sum
        }
        if (MODE == eMODE.SYSTEMOUT) {
            val DOTS = "...................................................................................."
            val sb = StringBuilder(DOTS)
            val last_tag = tag.substring(Math.max(tag.length + locator.length - DOTS.length, 0))
            sb.replace(0, last_tag.length, last_tag)
            sb.replace(sb.length - locator.length, sb.length, locator)
            val adj_tag = sb.toString()
            val N = sa.size
            if (N <= 0) {
                println(adj_tag + PREFIX)
                return 0
            }
            if (N == 1) {
                println(adj_tag + sa[0])
                return 0
            }
            println(adj_tag + PREFIX_MULTILINE)
            for (s in sa) println(adj_tag + s)
            return 0
        }
        return 0
    }

    private fun getLocator(info: StackTraceElement?): String {
        return if (info == null) "" else String.format(Locale.getDefault(), "(%s:%d)", info.fileName, info.lineNumber)
        //android studio
    }

    private fun getTag(info: StackTraceElement?): String {
        if (info == null) return ""
        var tag = info.methodName
        try {
            val name = info.className
            tag = name.substring(name.lastIndexOf('.') + 1) + "." + info.methodName
        } catch (e: Exception) {
        }
        return tag.replace("\\$".toRegex(), "_")
    }//			final String className = info.getClassName();
//			final String fileName = info.getFileName();
    //			final String methodName = info.getMethodName();
//			android.util.Log.e("DEBUG", className + "," + fileName + "," + methodName + "," + lineNumber);
//            final String fileName = info.getFileName();
//            final int lineNumber = info.getLineNumber();
//            final String methodName = info.getMethodName();
//            android.util.Log.d("DEBUG", className + "," + fileName + "," + methodName + "," + lineNumber);
//            final String fileName = info.getFileName();
//            final int lineNumber = info.getLineNumber();
//            final String methodName = info.getMethodName();
//            android.util.Log.d("DEBUG", className + "," + fileName + "," + methodName + "," + lineNumber);

    //		String methodName = null;
    private fun getStack(): StackTraceElement {
        val stackTraceElements = Exception().stackTrace
        var i = 0
        //		String methodName = null;
        val N = stackTraceElements.size
        var info = stackTraceElements[i]
        while (i < N) {
            info = stackTraceElements[i]
            val className = info.className
            //            final String fileName = info.getFileName();
//            final int lineNumber = info.getLineNumber();
//            final String methodName = info.getMethodName();
//            android.util.Log.d("DEBUG", className + "," + fileName + "," + methodName + "," + lineNumber);
            if (className.startsWith(LOG_CLASS)) {
                i++
                continue
            }
            break
            i++
        }
        while (i < N) {
            info = stackTraceElements[i]
            val className = info.className
            //            final String fileName = info.getFileName();
//            final int lineNumber = info.getLineNumber();
//            final String methodName = info.getMethodName();
//            android.util.Log.d("DEBUG", className + "," + fileName + "," + methodName + "," + lineNumber);
            if (className.matches(LOG_PASS_REGEX)) {
                i++
                continue
            }
            break
            i++
        }
        while (i >= N) i--
        while (i >= 0) {
            info = stackTraceElements[i]
            //			final String className = info.getClassName();
//			final String fileName = info.getFileName();
            val lineNumber = info.lineNumber
            //			final String methodName = info.getMethodName();
//			android.util.Log.e("DEBUG", className + "," + fileName + "," + methodName + "," + lineNumber);
            if (lineNumber < 0) {
                i--
                continue
            }
            break
            i--
        }
        return info
    }

    private fun getStack(methodNameKey: String): StackTraceElement {
        val stackTraceElements = Exception().stackTrace
        var info = stackTraceElements[0]
        val N = stackTraceElements.size
        var s = 0
        while (s < N) {
            info = stackTraceElements[s]
            val className = info.className
            //            final String fileName = info.getFileName();
//            final int lineNumber = info.getLineNumber();
//            final String methodName = info.getMethodName();
//            android.util.Log.d("DEBUG", className + "," + fileName + "," + methodName + "," + lineNumber);
            if (className.startsWith(LOG_CLASS)) { //                android.util.Log.i("pass", className + "," + methodName + "," + fileName + " " + lineNumber);
                s++
                continue
            }
            //            android.util.Log.e("stop", className + "," + methodName + "," + fileName + " " + lineNumber);
            break
            s++
        }
        var e = N - 1
        while (e >= s) {
            info = stackTraceElements[e]
            val methodName = info.methodName
            val className = info.className
            //            final String fileName = info.getFileName();
//            final int lineNumber = info.getLineNumber();
//            android.util.Log.d("DEBUG", className + "," + methodName + "," + fileName + " " + lineNumber);
            if (methodNameKey == methodName && !className.matches(LOG_PASS_REGEX)) { //                android.util.Log.e("stop", className + "," + methodName + "," + fileName + " " + lineNumber);
                break
            }
            e--
        }
        return info
    }

    private fun getStackC(methodNameKey: String): StackTraceElement {
        val stackTraceElements = Exception().stackTrace
        var last_info = stackTraceElements[0]
        val N = stackTraceElements.size
        var s = 0
        while (s < N) {
            val info = stackTraceElements[s]
            last_info = info
            val className = info.className
            if (className.startsWith(LOG_CLASS)) {
                s++
                continue
            }
            break
            s++
        }
        var e = N - 1
        while (e >= s) {
            val info = stackTraceElements[e]
            val methodName = info.methodName
            if (methodNameKey == methodName) break
            last_info = info
            e--
        }
        return last_info
    }

    private fun safeCut(byteArray: ByteArray, startOffset: Int): Int {
        val byteLength = byteArray.size
        if (byteLength <= startOffset) throw ArrayIndexOutOfBoundsException("!!text_length <= start_byte_index")
        if (byteArray[startOffset] and 0xc0.toByte() == 0x80.toByte()) throw java.lang.UnsupportedOperationException("!!start_byte_index must splited index")

        var position = startOffset + MAX_LOG_LINE_BYTE_SIZE
        if (byteLength <= position) return byteLength - startOffset

        while (startOffset <= position) {
            if (byteArray[position] and 0xc0.toByte() != 0x80.toByte()) break
            position--
        }
        if (position <= startOffset) throw UnsupportedOperationException("!!byte_length too small")
        return position - startOffset
    }

    private var last_filter: Long = 0
    fun e_filter(nano: Long, vararg args: Any?): Int {
        if (!LOG) return -1
        if (last_filter < System.nanoTime() - nano) return -1
        last_filter = System.nanoTime()
        return p(android.util.Log.ERROR, *args)
    }

    fun a(vararg args: Any?) {
        if (!LOG) return
        p(ASSERT, *args)
    }

    fun e(vararg args: Any?) {
        if (!LOG) return
        p(ERROR, *args)
    }

    fun w(vararg args: Any?) {
        if (!LOG) return
        p(WARN, *args)
    }

    fun i(vararg args: Any?) {
        if (!LOG) return
        p(INFO, *args)
    }

    fun d(vararg args: Any?) {
        if (!LOG) return
        p(DEBUG, *args)
    }

    fun v(vararg args: Any?) {
        if (!LOG) return
        p(VERBOSE, *args)
    }

    fun pn(priority: Int, depth: Int, vararg args: Any?): Int {
        if (!LOG) return -1
        val info = Exception().stackTrace[1 + depth]
        val tag = getTag(info)
        val locator = getLocator(info)
        val msg = _MESSAGE(*args)
        return println(priority, tag, locator, msg)
    }

    fun viewtree(parent: View, vararg depth: Int): Int {
        if (!LOG) return -1
        val d = if (depth.size > 0) depth[0] else 0
        if (parent !is ViewGroup) {
            return pn(android.util.Log.ERROR, d + 2, _DUMP(parent, 0))
        }
        val vp = parent
        val N = vp.childCount
        var result = 0
        for (i in 0 until N) {
            val child = vp.getChildAt(i)
            result += pn(android.util.Log.ERROR, d + 2, _DUMP(child, d))
            if (child is ViewGroup) result += viewtree(child, d + 1)
        }
        return result
    }

    fun clz(clz: Class<*>) {
        if (!LOG) return
        e(clz)
        i("getName", clz.name)
        i("getPackage", clz.`package`)
        i("getCanonicalName", clz.canonicalName)
        i("getDeclaredClasses", clz.declaredClasses.contentToString())
        i("getClasses", clz.classes.contentToString())
        i("getSigners", clz.signers?.contentToString())
        i("getEnumConstants", clz.enumConstants?.contentToString())
        i("getTypeParameters", clz.typeParameters.contentToString())
        i("getGenericInterfaces", clz.genericInterfaces.contentToString())
        i("getInterfaces", clz.interfaces.contentToString())
        //@formatter:off
        if (clz.isAnnotation                              ) i("classinfo", clz.isAnnotation, "isAnnotation")
        if (clz.isAnonymousClass                          ) i(clz.isAnonymousClass, "isAnonymousClass")
        if (clz.isArray                                   ) i(clz.isArray, "isArray")
        if (clz.isEnum                                    ) i(clz.isEnum, "isEnum")
        if (clz.isInstance(CharSequence::class.java)      ) i(clz.isInstance(CharSequence::class.java), "isInstance")
        if (clz.isAssignableFrom(CharSequence::class.java)) i(clz.isAssignableFrom(CharSequence::class.java), "isAssignableFrom")
        if (clz.isInterface                               ) i(clz.isInterface, "isInterface")
        if (clz.isLocalClass                              ) i(clz.isLocalClass, "isLocalClass")
        if (clz.isMemberClass                             ) i(clz.isMemberClass, "isMemberClass")
        if (clz.isPrimitive                               ) i(clz.isPrimitive, "isPrimitive")
        if (clz.isSynthetic                               ) i(clz.isSynthetic, "isSynthetic")
         //@formatter:on
    }

    ////////////////////////////////////////////////////////////////////////////
    fun toast(context: Context?, vararg args: Any?) {
        if (!LOG) return
        if (context == null) return
        e(*args)
        Toast.makeText(context, _MESSAGE(*args), Toast.LENGTH_SHORT).show()
    }

    ////////////////////////////////////////////////////////////////////////////
//_DUMP
////////////////////////////////////////////////////////////////////////////
    fun _MESSAGE(vararg args: Any?): String {
        if (args.isNullOrEmpty())
            return "null[]"

        return args.joinToString {
            runCatching {
                when {
                    it == null -> "null"
                    it is Class<*> -> _DUMP(it)
                    it is View -> _DUMP(it)
                    it is Intent -> _DUMP(it)
                    it is Bundle -> _DUMP(it)
                    it is ContentValues -> _DUMP(it)
                    it is Throwable -> _DUMP(it)
                    it is Method -> _DUMP(it)
                    it is JSONObject -> it.toString(2)
                    it is JSONArray -> it.toString(2)
                    it is CharSequence -> _DUMP(it.toString())
                    it.javaClass.isArray -> (it as Array<*>).contentToString()
                    else -> it.toString()
                }
            }.getOrDefault("")
        }
    }

    fun _DUMP_json(json: String): String {
        try {
            if (json.length > 0) {
                if (json[0] == '{') {
                    return JSONObject(json).toString(4)
                }
                if (json[0] == '[') {
                    return JSONArray(json).toString(4)
                }
            }
        } catch (ignored: Exception) {
        }
        return json
    }

    fun _DUMP(`object`: String): String {
        val sb = StringBuilder()
        try {
            val s = `object`[0]
            val e = `object`[`object`.length - 1]
            if (s == '[' && e == ']') {
                val ja = JSONArray(`object`).toString(2)
                sb.append("\nJA\n")
                sb.append(ja)
            } else if (s == '{' && e == '}') {
                val jo = JSONObject(`object`).toString(2)
                sb.append("\nJO\n")
                sb.append(jo)
            } else if (s == '<' && e == '>') {
                val xml = PrettyXml.format(`object`)
                sb.append("\nXML\n")
                sb.append(xml)
            } else {
                sb.append(`object`)
            }
        } catch (e: Exception) {
            sb.append(`object`)
        }
        return sb.toString()
    }

    fun _DUMP(method: Method): String {
        val result = StringBuilder(Modifier.toString(method.modifiers))
        if (result.length != 0) {
            result.append(' ')
        }
        result.append(method.returnType.simpleName)
        result.append("                           ")
        result.setLength(20)
        result.append(method.declaringClass.simpleName)
        result.append('.')
        result.append(method.name)
        result.append("(")
        val parameterTypes = method.parameterTypes
        for (parameterType in parameterTypes) {
            result.append(parameterType.simpleName)
            result.append(',')
        }
        if (parameterTypes.size > 0) result.setLength(result.length - 1)
        result.append(")")
        val exceptionTypes = method.exceptionTypes
        if (exceptionTypes.size != 0) {
            result.append(" throws ")
            for (exceptionType in exceptionTypes) {
                result.append(exceptionType.simpleName)
                result.append(',')
            }
            if (exceptionTypes.size > 0) result.setLength(result.length - 1)
        }
        return result.toString()
    }

    private fun _DUMP(v: View, depth: Int = 0): String {
        val SP = "                    "
        val out = StringBuilder(128)
        out.append(SP)
        when (v) {
            is WebView -> out.insert(depth, "W:" + Integer.toHexString(System.identityHashCode(v)) + ":" + v.title)
            is TextView -> out.insert(depth, "T:" + Integer.toHexString(System.identityHashCode(v)) + ":" + v.text)
            else -> out.insert(depth, "N:" + Integer.toHexString(System.identityHashCode(v)) + ":" + v.javaClass.simpleName)
        }
        out.setLength(SP.length)
        appendViewInfo(out, v)
        return out.toString()
    }

    private fun appendViewInfo(out: StringBuilder, v: View) { //		out.append('{');
        val id = v.id
        if (id != View.NO_ID) { // out.append(String.format(" #%08x", id));
            val r = v.resources
            if (id ushr 24 != 0 && r != null) {
                try {
                    val pkgname: String
                    pkgname = when (id and -0x1000000) {
                        0x7f000000 -> "app"
                        0x01000000 -> "android"
                        else -> r.getResourcePackageName(id)
                    }
                    val typename = r.getResourceTypeName(id)
                    val entryname = r.getResourceEntryName(id)
                    out.append(" ")
                    out.append(pkgname)
                    out.append(":")
                    out.append(typename)
                    out.append("/")
                    out.append(entryname)
                } catch (ignored: NotFoundException) {
                }
            }
        }
    }

    private fun _DUMP(c: Cursor?): String {
        if (c == null) return "null_Cursor"
        val sb = StringBuilder()
        val count = c.count
        sb.append("<$count>")
        try {
            val columns = c.columnNames
            sb.append(Arrays.toString(columns))
            sb.append("\n")
        } catch (ignored: Exception) {
        }
        val countColumns = c.columnCount
        if (!c.isBeforeFirst) {
            for (i in 0 until countColumns) {
                try {
                    sb.append(c.getString(i) + ",")
                } catch (e: Exception) {
                    sb.append("BLOB,")
                }
            }
        } else {
            val org_pos = c.position
            while (c.moveToNext()) {
                for (i in 0 until countColumns) {
                    try {
                        sb.append(c.getString(i) + ",")
                    } catch (e: Exception) {
                        sb.append("BLOB,")
                    }
                }
                sb.append("\n")
            }
            c.moveToPosition(org_pos)
        }
        return sb.toString()
    }

    private fun _DUMP(values: ContentValues?): String {
        if (values == null) return "null_ContentValues"
        val sb = StringBuilder()
        for ((key, value1) in values.valueSet()) {
            val value = value1.toString()
            val type = value1.javaClass.simpleName
            sb.append("$key,$type,$value").append("\n")
        }
        return sb.toString()
    }

    private fun _DUMP(bundle: Bundle?): String {
        if (bundle == null) return "null_Bundle"
        val sb = StringBuilder()
        bundle.keySet().forEach {
            val o = bundle[it]
            when {
                o == null -> sb.append("Object $it;//null")
                o.javaClass.isArray -> sb.append(o.javaClass.simpleName + " " + it + ";//" + (o as Array<*>).contentToString())
                else -> sb.append(o.javaClass.simpleName + " " + it + ";//" + o.toString())
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun _DUMP(cls: Class<*>?): String {
        return cls?.simpleName ?: "null_Class<?>"
        //		return cls.getSimpleName() + ((cls.getSuperclass() != null) ? (">>" + cls.getSuperclass().getSimpleName()) : "");
    }

    private fun _DUMP(uri: Uri?): String {
        if (uri == null) return "null_Uri"
        //		return uri.toString();
        val sb = StringBuilder()
        sb.append("\r\n Uri                       ").append(uri.toString())
        sb.append("\r\n Scheme                    ").append(if (uri.scheme != null) uri.scheme else "null")
        sb.append("\r\n Host                      ").append(if (uri.host != null) uri.host else "null")
        //        sb.append("\r\n Port                      ").append(uri.getPort());
        sb.append("\r\n Path                      ").append(if (uri.path != null) uri.path else "null")
        sb.append("\r\n LastPathSegment           ").append(if (uri.lastPathSegment != null) uri.lastPathSegment else "null")
        sb.append("\r\n Query                     ").append(if (uri.query != null) uri.query else "null")
        //        sb.append("\r\n");
        sb.append("\r\n Fragment                  ").append(if (uri.fragment != null) uri.fragment else "null")
//        sb.append("\r\n SchemeSpecificPart        ").append(uri.getSchemeSpecificPart() != null ? uri.getSchemeSpecificPart().toString() : "null");
//        sb.append("\r\n UserInfo                  ").append(uri.getUserInfo() != null ? uri.getUserInfo().toString() : "null");
//        sb.append("\r\n PathSegments              ").append(uri.getPathSegments() != null ? uri.getPathSegments().toString() : "null");
//        sb.append("\r\n Authority                 ").append(uri.getAuthority() != null ? uri.getAuthority().toString() : "null");
//        sb.append("\r\n");
//        sb.append("\r\n EncodedAuthority          ").append(uri.getEncodedAuthority() != null ? uri.getEncodedAuthority().toString() : "null");
//        sb.append("\r\n EncodedPath               ").append(uri.getEncodedPath() != null ? uri.getEncodedPath().toString() : "null");
//        sb.append("\r\n EncodedQuery              ").append(uri.getEncodedQuery() != null ? uri.getEncodedQuery().toString() : "null");
//        sb.append("\r\n EncodedFragment           ").append(uri.getEncodedFragment() != null ? uri.getEncodedFragment().toString() : "null");
//        sb.append("\r\n EncodedSchemeSpecificPart ").append(uri.getEncodedSchemeSpecificPart() != null ? uri.getEncodedSchemeSpecificPart().toString() : "null");
//        sb.append("\r\n EncodedUserInfo           ").append(uri.getEncodedUserInfo() != null ? uri.getEncodedUserInfo().toString() : "null");
//        sb.append("\r\n");
        return sb.toString()
    }

    fun _DUMP(intent: Intent?): String {
        if (intent == null) return "null_Intent"
        val sb = StringBuilder()
        //@formatter:off
        sb.append(if (intent.action       != null)(if (sb.length > 0)"\n" else "") + "Action     " + intent.action               .toString() else "")
        sb.append(if (intent.data         != null)(if (sb.length > 0)"\n" else "") + "Data       " + intent.data                 .toString() else "")
        sb.append(if (intent.categories   != null)(if (sb.length > 0)"\n" else "") + "Categories " + intent.categories           .toString() else "")
        sb.append(if (intent.type         != null)(if (sb.length > 0)"\n" else "") + "Type       " + intent.type                 .toString() else "")
        sb.append(if (intent.scheme       != null)(if (sb.length > 0)"\n" else "") + "Scheme     " + intent.scheme               .toString() else "")
        sb.append(if (intent.`package`    != null)(if (sb.length > 0)"\n" else "") + "Package    " + intent.`package`            .toString() else "")
        sb.append(if (intent.component    != null)(if (sb.length > 0)"\n" else "") + "Component  " + intent.component            .toString() else "")
        sb.append(if (intent.flags        != 0x00)(if (sb.length > 0)"\n" else "") + "Flags      " + Integer.toHexString(intent.flags) else "")
         //@formatter:on
        if (intent.extras != null) sb.append((if (sb.length > 0) "\n" else "") + _DUMP(intent.extras))
        return sb.toString()
    }

    private fun _DUMP(bytearray: ByteArray?): String = bytearray?.joinToString("") { "%02x".format(it) } ?: "!!byte[]"

    fun _DUMP_StackTrace(tr: Throwable?): String {
        return android.util.Log.getStackTraceString(tr)
    }

    fun _DUMP(th: Throwable?): String {
        var message = "Throwable"
        try {
            var cause = th
            while (cause != null) {
                message = cause.javaClass.simpleName + "," + cause.message
                cause = cause.cause
            }
        } catch (ignored: Exception) {
        }
        return message
    }

    private val sf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.getDefault())
    private val LOGN_FORMAT = "%" + java.lang.Long.toString(java.lang.Long.MAX_VALUE).length + "d"
    @JvmOverloads
    fun _DUMP_milliseconds(milliseconds: Long = System.currentTimeMillis()): String {
        return String.format("<%s,$LOGN_FORMAT>", sf.format(Date(milliseconds)), SystemClock.elapsedRealtime())
    }

    private val yyyymmddhhmmss = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    fun _DUMP_yyyymmddhhmmss(milliseconds: Long): String {
        return yyyymmddhhmmss.format(Date(milliseconds))
    }

    fun _DUMP_elapsed(elapsedRealtime: Long): String {
        return _DUMP_milliseconds(System.currentTimeMillis() - (SystemClock.elapsedRealtime() - elapsedRealtime))
    }

    fun _h2s(bytes: ByteArray?): String {
        return _DUMP(bytes)
    }

    fun _s2h(bytes_text: String): ByteArray {
        val bytes = ByteArray(bytes_text.length / 2)
        try {
            for (i in bytes.indices) {
                bytes[i] = bytes_text.substring(2 * i, 2 * i + 2).toInt(16).toByte()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bytes
    }

    fun _DUMP_object(o: Any?): String {
        return _DUMP_object("", o, HashSet())
    }

    private fun _DUMP_object(name: String, value: Any?, duplication: MutableSet<Any>): String {
        val sb = StringBuilder()
        try {
            if (value == null)
                return "null"

            if (value.javaClass.isArray) {
                sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ")
                //@formatter:off
                  val  componentType = value.javaClass.componentType
                when {
                    Boolean::class.javaPrimitiveType!!.isAssignableFrom(componentType!!) -> sb.append(Arrays.toString(value as BooleanArray?))
                    Byte::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(if ((value as ByteArray).size < MAX_LOG_LINE_BYTE_SIZE)String((value as ByteArray?)!!) else "[" + value.size + "]")
                    Char::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(String((value as CharArray?)!!))
                    Double::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as DoubleArray?))
                    Float::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as FloatArray?))
                    Int::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as IntArray?))
                    Long::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as LongArray?))
                    Short::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as ShortArray?))
                    else -> sb.append(Arrays.toString(value as Array<*>))
                }
             //@formatter:on
            } else if (value.javaClass.isPrimitive //
//					|| (value.getClass().getMethod("toString").getDeclaringClass() != Object.class)// toString이 정의된경우만
                    || value.javaClass.isEnum //
                    || value is Rect //
                    || value is RectF //
                    || value is Point //
                    || value is Number //
                    || value is Boolean //
                    || value is CharSequence) //
            {
                sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ")
                sb.append(value.toString())
            } else {
                if (duplication.contains(value)) {
                    sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ")
                    sb.append("[duplication]\n")
                    return sb.toString()
                }
                duplication.add(value)
                if (value is Collection<*>) {
                    sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ").append(":\n")
                    val it = value.iterator()
                    while (it.hasNext()) sb.append(_DUMP_object("  $name[item]", it.next(), duplication))
                } else {
                    val clz: Class<*> = value.javaClass
                    sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ").append(":\n")
                    for (f in clz.declaredFields) {
                        f.isAccessible = true
                        sb.append(_DUMP_object("  " + name + "." + f.name, f[value], duplication))
                    }
                }
            }
            sb.append("\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sb.toString()
    }

    //tic
    private var SEED_S: Long = 0L

    fun tic_s() {
        if (!LOG) return
        val e = System.nanoTime()
        SEED_S = e
    }

    fun tic() {
        if (!LOG) return
        val e = System.nanoTime()
        val s = SEED_S
        e(String.format(Locale.getDefault(), "%,25d", e - s))
        SEED_S = e
    }

    fun tic(vararg args: String) {
        if (!LOG) return
        val e = System.nanoTime()
        val s = SEED_S
        e(String.format(Locale.getDefault(), "%,25d", e - s), args)
        SEED_S = e
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
//image save
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun compress(name: String, data: ByteArray) {
        FILE_LOG ?: return
        runCatching {
            FILE_LOG!!.parentFile?.also {
                it.mkdirs()
                it.canWrite()
                val f = File(it, timeText + "_" + name + ".jpg")
                FileOutputStream(f).use { BitmapFactory.decodeByteArray(data, 0, data.size).compress(CompressFormat.JPEG, 100, it) }
            }
        }
    }

    fun compress(name: String, bmp: Bitmap) {
        FILE_LOG ?: return
        runCatching {
            FILE_LOG!!.parentFile?.also {
                it.mkdirs()
                it.canWrite()
                val f = File(it, timeText + "_" + name + ".jpg")
                FileOutputStream(f).use { bmp.compress(CompressFormat.JPEG, 100, it) }
            }
        }
    }

    val timeText: String get() = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.ENGLISH).format(Date())

    //flog
    fun flog(vararg args: Any?) {
        FILE_LOG ?: return
        runCatching {
            val info = getStack()
            val log: String = _MESSAGE(*args)
            val st = StringTokenizer(log, LF, false)

            val tag = "%-40s%-40d %-100s ``".format(Date().toString(), SystemClock.elapsedRealtime(), info.toString())
            if (st.hasMoreTokens()) {
                val token = st.nextToken()
                FILE_LOG!!.appendText(tag + token + LF)
            }

            val space = "%-40s%-40s %-100s ``".format("", "", "")
            while (st.hasMoreTokens()) {
                val token = st.nextToken()
                FILE_LOG!!.appendText(space + token + LF)
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //life tools
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //상속트리마지막 위치
    fun po(priority: Int, methodNameKey: String, vararg args: Any?): Int {
        if (!LOG) return -1
        val info = getStack(methodNameKey)
        val tag = getTag(info)
        val locator = getLocator(info)
        val msg = _MESSAGE(*args)
        return println(priority, tag, locator, msg)
    }

    fun sendBroadcast(clz: Class<*>?, intent: Intent) {
        if (!LOG) return
        try {
            val target = if (intent.component != null) intent.component!!.shortClassName else intent.toUri(0)
            pc(ERROR, "sendBroadcast", "▶▶", clz, target, intent)
        } catch (ignored: Exception) {
        }
    }

    fun startService(clz: Class<*>?, intent: Intent) {
        if (!LOG) return
        try {
            val target = if (intent.component != null) intent.component!!.shortClassName else intent.toUri(0)
            pc(ERROR, "sendBroadcast", "▶▶", clz, target, intent)
        } catch (ignored: Exception) {
        }
    }

    fun onActivityResult(clz: Class<*>?, requestCode: Int, resultCode: Int, data: Intent?) {
        if (!LOG) return

        val level = if (resultCode == Activity.RESULT_OK) INFO else ERROR
        po(level, "onActivityResult", "◀◀",
                clz,
                "requestCode=0x%08x".format(requestCode),
                when (resultCode) {
                    Activity.RESULT_OK -> "Activity.RESULT_OK"
                    Activity.RESULT_CANCELED -> "Activity.RESULT_CANCELED"
                    else -> ""
                })
        if (data != null && data.extras != null)
            p(level, data.extras)
    }

    fun pc(priority: Int, methodNameKey: String, vararg args: Any?): Int {
        if (!LOG) return -1
        val info = getStackC(methodNameKey)
        val tag = getTag(info)
        val locator = getLocator(info)
        val msg = _MESSAGE(*args)
        return println(priority, tag, locator, msg)
    }

    fun startActivities(clz: Class<*>?, intents: Array<Intent>) {
        if (!LOG) return
        for (intent in intents) {
            try {
                val target = if (intent.component != null) intent.component!!.shortClassName else intent.toUri(0)
                pc(ERROR, "startActivities", "▶▶", clz, target, intent)
                //		printStackTrace();
            } catch (ignored: Exception) {
            }
        }
    }

    fun startActivityForResult(clz: Class<*>?, intent: Intent, requestCode: Int) {
        if (!LOG) return
        try {
            val target = if (intent.component != null) intent.component!!.shortClassName else intent.toUri(0)
            pc(ERROR, if (requestCode == -1) "startActivity" else "startActivityForResult", "▶▶", clz, target, intent, String.format("0x%08X", requestCode))
            //		printStackTrace();
        } catch (ignored: Exception) {
        }
    }

    fun startActivityForResult(clz: Class<*>?, intent: Intent, requestCode: Int, options: Bundle?) {
        if (!LOG) return
        try {
            val target = if (intent.component != null) intent.component!!.shortClassName else intent.toUri(0)
            pc(ERROR, if (requestCode == -1) "startActivity" else "startActivityForResult", "▶▶", clz, target, intent, options, String.format("0x%08X", requestCode))
            //		printStackTrace();
        } catch (ignored: Exception) {
        }
    }

    fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!LOG) return
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        d(String.format("0x%08x,0x%08x", widthMode, heightMode))
        d(String.format("%10d,%10d", widthSize, heightSize))
    }

    fun printStackTrace() {
        if (!LOG) return
        TraceLog().printStackTrace()
    }

    fun printStackTrace(e: Exception) {
        if (!LOG) return
        e.printStackTrace()
    }

    private var LAST_ACTION_MOVE: Long = 0
    fun onTouchEvent(event: MotionEvent) {
        if (!LOG) return
        try {
            val action = event.action and MotionEvent.ACTION_MASK
            if (action == MotionEvent.ACTION_MOVE) {
                val nanoTime = System.nanoTime()
                if (nanoTime - LAST_ACTION_MOVE < 1000000) return
                LAST_ACTION_MOVE = nanoTime
            }
            e(event)
        } catch (ignored: Exception) {
        }
    }

    //호환 팩
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
    enum class eMODE {
        STUDIO, SYSTEMOUT
    }

    //xml
    private object PrettyXml {
        private val formatter = XmlFormatter(2, 80)
        fun format(s: String): String {
            return formatter.format(s, 0)
        }

        private fun buildWhitespace(numChars: Int): String {
            val sb = StringBuilder()
            for (i in 0 until numChars) sb.append(" ")
            return sb.toString()
        }

        private fun lineWrap(s: String?, lineLength: Int, indent: Int, linePrefix: String?): String? {
            if (s == null) return null
            val sb = StringBuilder()
            var lineStartPos = 0
            var lineEndPos: Int
            var firstLine = true
            while (lineStartPos < s.length) {
                if (!firstLine) sb.append("\n") else firstLine = false
                if (lineStartPos + lineLength > s.length) lineEndPos = s.length - 1 else {
                    lineEndPos = lineStartPos + lineLength - 1
                    while (lineEndPos > lineStartPos && s[lineEndPos] != ' ' && s[lineEndPos] != '\t') lineEndPos--
                }
                sb.append(buildWhitespace(indent))
                if (linePrefix != null) sb.append(linePrefix)
                sb.append(s.substring(lineStartPos, lineEndPos + 1))
                lineStartPos = lineEndPos + 1
            }
            return sb.toString()
        }

        private class XmlFormatter(private val indentNumChars: Int, private val lineLength: Int) {
            private var singleLine = false
            @Synchronized
            fun format(s: String, initialIndent: Int): String {
                var indent = initialIndent
                val sb = StringBuilder()
                var i = 0
                while (i < s.length) {
                    val currentChar = s[i]
                    if (currentChar == '<') {
                        val nextChar = s[i + 1]
                        if (nextChar == '/') indent -= indentNumChars
                        if (!singleLine) // Don't indent before closing element if we're creating opening and closing elements on a single line.
                            sb.append(buildWhitespace(indent))
                        if (nextChar != '?' && nextChar != '!' && nextChar != '/') indent += indentNumChars
                        singleLine = false // Reset flag.
                    }
                    sb.append(currentChar)
                    if (currentChar == '>') {
                        if (s[i - 1] == '/') {
                            indent -= indentNumChars
                            sb.append("\n")
                        } else {
                            val nextStartElementPos = s.indexOf('<', i)
                            if (nextStartElementPos > i + 1) {
                                val textBetweenElements = s.substring(i + 1, nextStartElementPos)
                                // If the space between elements is solely newlines, let them through to preserve additional newlines in source document.
                                if (textBetweenElements.replace("\n".toRegex(), "").length == 0) {
                                    sb.append(textBetweenElements + "\n")
                                } else if (textBetweenElements.length <= lineLength * 0.5) {
                                    sb.append(textBetweenElements)
                                    singleLine = true
                                } else {
                                    sb.append("\n" + lineWrap(textBetweenElements, lineLength, indent, null) + "\n")
                                }
                                i = nextStartElementPos - 1
                            } else {
                                sb.append("\n")
                            }
                        }
                    }
                    i++
                }
                return sb.toString()
            }

        }
    }

    class TraceLog : Throwable()
}