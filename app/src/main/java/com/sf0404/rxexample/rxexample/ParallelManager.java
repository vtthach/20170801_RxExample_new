package com.sf0404.rxexample.rxexample;


import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

class ParallelManager {
    public static <T> Observable<List<T>> startParallelAndGetResult(Task<T>[] tasks, Executor executor) {
        return Observable.fromArray(tasks)
                .flatMap(new Function<Task<T>, ObservableSource<T>>() {
                    @Override
                    public ObservableSource<T> apply(@NonNull Task<T> task) throws Exception {
                        return Observable.defer(new Callable<ObservableSource<T>>() {
                            @Override
                            public ObservableSource<T> call() throws Exception {
                                return Observable.just(task.run());
                            }
                        }).observeOn(Schedulers.from(executor)).subscribeOn(Schedulers.from(executor));
                    }
                })
                .toList()
                .toObservable()
                .subscribeOn(Schedulers.from(executor))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Single<List<T>> startParallel(List<Task<T>> list, Executor executor) {
        return Observable.fromIterable(list)
                .flatMap(new Function<Task<T>, ObservableSource<T>>() {
                    @Override
                    public ObservableSource<T> apply(@NonNull Task<T> task) throws Exception {
                        return Observable.defer(new Callable<ObservableSource<T>>() {
                            @Override
                            public ObservableSource<T> call() throws Exception {
                                return Observable.just(task.run());
                            }
                        }).observeOn(Schedulers.from(executor)).subscribeOn(Schedulers.from(executor));
                    }
                })
                .toList()
                .subscribeOn(Schedulers.from(executor))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Observable<List<T>> getShareParallel(List<Task<T>> list, Executor executor) {
        return Observable.fromIterable(list)
                .flatMap(new Function<Task<T>, ObservableSource<T>>() {
                    @Override
                    public ObservableSource<T> apply(@NonNull Task<T> task) throws Exception {
                        return Observable.defer(new Callable<ObservableSource<T>>() {
                            @Override
                            public ObservableSource<T> call() throws Exception {
                                return Observable.just(task.run());
                            }
                        }).observeOn(Schedulers.from(executor)).subscribeOn(Schedulers.from(executor));
                    }
                })
                .toList()
                .toObservable()
                .subscribeOn(Schedulers.from(executor))
                .observeOn(AndroidSchedulers.mainThread());
    }
}
