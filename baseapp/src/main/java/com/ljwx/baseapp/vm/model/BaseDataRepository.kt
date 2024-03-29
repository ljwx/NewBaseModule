package com.ljwx.baseapp.vm.model

import com.ljwx.baseapp.constant.ConstTag
import com.ljwx.baseapp.response.BaseResponse
import com.ljwx.baseapp.util.Log2

abstract class BaseDataRepository<Server> : IBaseDataRepository<Server> {

    companion object {

        private var globalObserverOnError: ((e: Throwable) -> Unit)? = null

        private var globalResponseFail: ((data: Any?) -> Unit)? = null

        /**
         * 通用的请求错误逻辑(代码执行错误)
         */
        fun setCommonOnError(onError: (e: Throwable) -> Unit) {
            //包含了接口响应404
            globalObserverOnError = onError
        }

        /**
         * 通用的请求失败逻辑(接口返回结果失败)
         */
        fun setCommonResponseFail(responseFail: (data: Any?) -> Unit) {
            globalResponseFail = responseFail
        }
    }

    open val TAG = this.javaClass.simpleName + ConstTag.MVVM

    private var mCompositeDisposable2: io.reactivex.disposables.CompositeDisposable? = null
    private var mCompositeDisposable3: io.reactivex.rxjava3.disposables.CompositeDisposable? = null

    protected val mApiServer
        get() = createServer()


    /**
     * 页面销毁时,自动取消网络请求
     *
     * @param disposable Rxjava2版本的dispose
     */
    override fun autoClear(disposable: io.reactivex.disposables.Disposable) {
        Log2.d(TAG, "添加Rx自动取消")
        if (mCompositeDisposable2 == null) {
            mCompositeDisposable2 = io.reactivex.disposables.CompositeDisposable()
        }
        mCompositeDisposable2?.add(disposable)
    }

    /**
     * 页面销毁时,自动取消网络请求
     *
     * @param disposable Rxjava3版本的dispose
     */
    override fun autoClear(disposable: io.reactivex.rxjava3.disposables.Disposable) {
        Log2.d(TAG, "添加Rx自动取消")
        if (mCompositeDisposable3 == null) {
            mCompositeDisposable3 = io.reactivex.rxjava3.disposables.CompositeDisposable()
        }
        mCompositeDisposable3?.add(disposable)
    }

    /**
     * 自动取消
     */
    override fun onRxCleared() {
        Log2.d(TAG, "执行Rx自动取消")
        mCompositeDisposable2?.clear()
        mCompositeDisposable3?.clear()
    }

    /**
     * RxJava3版本的结果监听
     */
    abstract inner class QuickObserver3<T : Any> : io.reactivex.rxjava3.core.Observer<T>,
        IQuickObserver<T> {
        override fun onSubscribe(d: io.reactivex.rxjava3.disposables.Disposable) {
            autoClear(d)
        }

        override fun onError(e: Throwable) {
            Log2.d(TAG, "本次请求异常报错:" + e.message)
            onErrorGlobal(e)
        }

        override fun onComplete() {
            Log2.d(TAG, "本次请求完成")
        }

        override fun onNext(value: T) {
            onResponse(value)
        }

        /**
         * 接口结果响应
         *
         * @param response 结果
         */
        override fun onResponse(response: T) {
            Log2.d(TAG, "接口返回response")
            if (response is BaseResponse<*>) {
                if (response.isSuccess()) {
                    Log2.d(TAG, "接口返回response,结果为成功")
                    onResponseSuccess(response)
                } else {
                    Log2.d(TAG, "接口返回response,结果为失败")
                    onResponseFail(response)
                }
            }
        }

        /**
         * 接口数据成功
         *
         * @param response 成功的结果
         */
        abstract override fun onResponseSuccess(response: T)

        /**
         * 接口数据失败
         *
         * @param response 失败的结果
         */
        override fun onResponseFail(response: T) {
            onResponseFailGlobal(response)
        }

        override fun onErrorGlobal(e: Throwable) {
            globalObserverOnError?.invoke(e)
        }

        override fun onResponseFailGlobal(response: T) {
            globalResponseFail?.invoke(response)
        }

    }

    /**
     * RxJava2版本的结果监听
     */
    abstract inner class QuickObserver<T : Any> : io.reactivex.Observer<T>, IQuickObserver<T> {
        override fun onSubscribe(d: io.reactivex.disposables.Disposable) {
            autoClear(d)
        }

        override fun onError(e: Throwable) {
            Log2.d(TAG, "本次请求异常报错:" + e.message)
            onErrorGlobal(e)
        }

        override fun onComplete() {
            Log2.d(TAG, "本次请求完成")
        }

        override fun onNext(value: T) {
            onResponse(value)
        }

        /**
         * 接口结果响应
         *
         * @param response 结果
         */
        override fun onResponse(response: T) {
            Log2.d(TAG, "接口返回response")
            if (response is BaseResponse<*>) {
                if (response.isSuccess()) {
                    Log2.d(TAG, "接口返回response,结果为成功")
                    onResponseSuccess(response)
                } else {
                    Log2.d(TAG, "接口返回response,结果为失败")
                    onResponseFail(response)
                }
            }
        }

        /**
         * 接口数据成功
         *
         * @param response 成功的结果
         */
        abstract override fun onResponseSuccess(response: T)

        /**
         * 接口数据失败
         *
         * @param dataResult 失败的结果
         */
        override fun onResponseFail(response: T) {
            onResponseFailGlobal(response)
        }

        override fun onErrorGlobal(e: Throwable) {
            globalObserverOnError?.invoke(e)
        }

        override fun onResponseFailGlobal(response: T) {
            globalResponseFail?.invoke(response)
        }

    }

    /**
     * RxJava2版本的结果监听,简单封装
     */
    abstract inner class ObserverResponse<T> : io.reactivex.Observer<T> {
        override fun onSubscribe(d: io.reactivex.disposables.Disposable) {
            autoClear(d)
        }

        override fun onError(e: Throwable) {
            Log2.d(TAG, "本次请求异常报错:" + e.message)
            globalObserverOnError?.invoke(e)
        }

        override fun onComplete() {
            Log2.d(TAG, "本次请求完成")
        }

        override fun onNext(value: T) {
            Log2.d(TAG, "本次请求结果已返回")
            onResponse(value)
        }

        /**
         * 接口结果响应
         *
         * @param response 结果
         */
        abstract fun onResponse(response: T)

    }

}