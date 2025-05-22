@file:Suppress("UNCHECKED_CAST")

package com.brally.mobile.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType
import java.util.*

object BindingReflex {

    /**
     * ViewBinding
     *
     * @param <V>    ViewBinding
     * @param aClass
     * @param from   layoutInflater
     * @return viewBinding
    </V> */
    fun <V : ViewBinding> reflexViewBinding(aClass: Class<*>, from: LayoutInflater?): V {
        var exception: java.lang.Exception? = null
        try {
            val actualTypeArguments =
                (Objects.requireNonNull(aClass.genericSuperclass) as ParameterizedType).actualTypeArguments
            for (i in actualTypeArguments.indices) {
                var tClass: Class<Any>? = null
                try {
                    if (actualTypeArguments[i] is Class<*>) {
                        tClass = actualTypeArguments[i] as Class<Any>
                    }
                    if (tClass != null && ViewBinding::class.java.isAssignableFrom(tClass)) {
                        val inflate = tClass.getMethod("inflate", LayoutInflater::class.java)
                        return inflate.invoke(null, from) as V
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    exception = e
                }
            }
        } catch (e: NoSuchMethodException) {
            exception = e
        } catch (e: IllegalAccessException) {
            exception = e
        } catch (e: InvocationTargetException) {
            exception = e
        } catch (e: java.lang.Exception) {
            exception = e
        } finally {
            exception?.printStackTrace()
        }
        throw  exception ?: Throwable("Error binding")
    }

    /**
     * ViewBinding
     */
    fun <V : ViewBinding> reflexViewBinding(
        aClass: Class<*>,
        from: LayoutInflater?,
        viewGroup: ViewGroup?,
        b: Boolean
    ): V {
        var exception: java.lang.Exception? = null
        try {
            val actualTypeArguments =
                (Objects.requireNonNull(aClass.genericSuperclass) as? ParameterizedType)?.actualTypeArguments
            if (actualTypeArguments != null) {
                for (i in actualTypeArguments.indices) {
                    val tClass: Class<Any>
                    try {
                        tClass = actualTypeArguments[i] as Class<Any>
                    } catch (e: Exception) {
                        continue
                    }
                    if (ViewBinding::class.java.isAssignableFrom(tClass)) {
                        val inflate = tClass.getDeclaredMethod(
                            "inflate",
                            LayoutInflater::class.java,
                            ViewGroup::class.java,
                            Boolean::class.javaPrimitiveType
                        )
                        return inflate.invoke(null, from, viewGroup, b) as V
                    }
                }
                try {
                    return reflexViewBinding<ViewBinding>(
                        aClass.superclass,
                        from,
                        viewGroup,
                        b
                    ) as V
                } catch (e: Exception) {
                    e.printStackTrace()
                    exception = e
                }
            }
        } catch (e: NoSuchMethodException) {
            exception = e
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            exception = e
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            exception = e
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            exception = e
            e.printStackTrace()
        } finally {
            exception?.printStackTrace()
        }
        throw exception ?: java.lang.RuntimeException("Error binding")
    }

}