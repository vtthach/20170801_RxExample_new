package com.sf0404.rxexample.rxexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.sf0404.rxexample.rxexample.MyLog.log;

public class PublishSubjectActivity extends AppCompatActivity {
    PublishSubject<Integer> subject = PublishSubject.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_subject);
        subject.flatMap(new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(@NonNull Integer integer) throws Exception {
                log("On apply1:" + integer);
                return Observable.defer(new Callable<ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> call() throws Exception {
                        log("On apply2:" + integer);
                        return Observable.just(integer * 10);
                    }
                }).subscribeOn(Schedulers.computation()).observeOn(Schedulers.computation());
            }
        }).observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        log("on comsumer:" + integer);
                    }
                });
    }

    public void start(View view) {
        subject.onNext(10);
    }
}
