package com.sf0404.rxexample.rxexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.sf0404.rxexample.rxexample.MyLog.log;

public class MainActivity extends AppCompatActivity {

    ExecutorService executor;
    private CompositeDisposable composite = new CompositeDisposable();
    private Single<List<Integer>> mSingle;
    private int result;
    private long currentTime;

    private DisposableObserver<? super List<Integer>> mySubscriber = new DisposableObserver<List<Integer>>() {
        @Override
        public void onNext(@NonNull List<Integer> integers) {
            long time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - currentTime);
            log(time + "------end " + integers);
            for (int i : integers) {
                result += i;
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        executor = Executors.newFixedThreadPool(3);
    }

    public void testFlatMap() {
        composite.clear();
        List<Task<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(new IntegerTask(i));
        }
        currentTime = System.currentTimeMillis();
        log("------Start ");
        Single<List<Integer>> single = getSingle(tasks);
        Disposable disposable = single.subscribe(integers -> {
            long time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - currentTime);
            log(time + "------end " + integers);
            for (int i : integers) {
                result += i;
            }
        });
        composite.add(disposable);
    }

    private Single<List<Integer>> getSingle(List<Task<Integer>> tasks) {
        if (mSingle == null) {
            mSingle = ParallelManager.startParallel(tasks, executor);
        }
        return mSingle;
    }

    public void testZip() {
        Observable.zip(getObservable(5), getObservable(6), (integer, integer2) -> {
            log("onCompleteZip");
            return integer + integer2;
        })
                .subscribeOn(Schedulers.from(executor))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Integer> whenAll(Observable<Integer> tasks1, Observable<Integer> tasks2) {
        return Observable.combineLatest(tasks1, tasks2, (integer, integer2) -> {
            log("apply ");
            return integer + integer2;
        });
    }

    public void start(View view) {
//       testCompileLatest();
        testFlatMap();
//        testParallelWithShare();
    }

    private void testParallelWithShare() {
        // Fixme this not work as expected ->have to create a new subscribe each time
        composite.clear();
        List<Task<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(new IntegerTask(i));
        }
        currentTime = System.currentTimeMillis();
        log("------Start ");
        if (share == null) {
            Disposable disposable;
            initShare(tasks);
            // Subscribe
            share.subscribe(mySubscriber);
            // Connect
            disposable = share.connect();
            composite.add(disposable);
        }
        share.subscribe();

    }

    ConnectableObservable<List<Integer>> share;

    private ConnectableObservable<List<Integer>> initShare(List<Task<Integer>> tasks) {
        if (share == null) {
            share = ParallelManager.getShareParallel(tasks, executor).share().replay();
        }
        return share;
    }

    public class IntegerTask implements Task<Integer> {

        Integer integer;

        public IntegerTask(Integer value) {
            this.integer = value;
        }

        @Override
        public Integer run() throws InterruptedException {
            log("Task is running 1 : " + integer);
            Thread.sleep(3000);
            log("Task is running 2 : " + integer);
            integer += 1;
            return integer;
        }

    }

    private DisposableObserver<Integer> getDisposal() {
        return new DisposableObserver<Integer>() {
            @Override
            public void onNext(@NonNull Integer integer) {
                log("Subscriber - onNext:" + integer);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {
                log("Subscriber - onComplete");
            }
        };
    }

    private void testCompileLatest() {
        composite.clear();
        DisposableObserver<Integer> mySubscribe = getDisposal();
        whenAll(getObservable(1), getObservable(10))
                .subscribeOn(Schedulers.from(executor))
                .observeOn(Schedulers.from(executor))
                .subscribe(mySubscribe);
        composite.add(mySubscribe);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mySubscriber.dispose();
        composite.dispose();
        executor.shutdown();
    }

    private Observable<Integer> getObservable(final int value) {
        return Observable.defer(new Callable<ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> call() throws Exception {
                log("startParallelAndGetResult 1 ");
                Thread.sleep(3000);
                log("startParallelAndGetResult 2 ");
                return Observable.just(value);
            }
        }).subscribeOn(Schedulers.from(executor));
    }

}
