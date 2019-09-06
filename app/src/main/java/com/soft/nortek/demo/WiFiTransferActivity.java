package com.soft.nortek.demo;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.soft.nortek.demo.common.Constants;
import com.soft.nortek.demo.filesmanage.Files_Manage_Activity;
import com.soft.nortek.demo.wifitransfer.PopupMenuDialog;
import com.soft.nortek.demo.wifitransfer.WebService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class WiFiTransferActivity extends Files_Manage_Activity implements Animator.AnimatorListener,View.OnClickListener{
//    private Toolbar mToolBar;
    private FloatingActionButton mFab;
    private RecyclerView mBookList;
    private Button backBtn;
    private TextView titleBarTitle;
    /**刷新控件**/
    private SwipeRefreshLayout mSwipeRefreshLayout;

    List<String> mBooks = new ArrayList<>();
    BookshelfAdapter mBookshelfAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifitransfer);
        initView();
//        setSupportActionBar(mToolBar);
        Timber.plant(new Timber.DebugTree());
        RxBus.get().register(this);
        initRecyclerView();
    }

    private void initView(){
//        mToolBar = findViewById(R.id.toolbar);
        mFab = findViewById(R.id.fab);
        mBookList = findViewById(R.id.recyclerview);
        mSwipeRefreshLayout = findViewById(R.id.content_main);
        mFab.setOnClickListener(this);
        backBtn = findViewById(R.id.back);
        backBtn.setOnClickListener(this);
        titleBarTitle = findViewById(R.id.title_bar_title);
        titleBarTitle.setText("Files List");
    }


    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab:
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mFab, "translationY", 0, mFab.getHeight() * 2).setDuration(200L);
                objectAnimator.setInterpolator(new AccelerateInterpolator());
                objectAnimator.addListener(this);
                objectAnimator.start();
                break;
            case R.id.back:
                finish();
                break;
                default:
                    break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebService.stop(this);
        RxBus.get().unregister(this);
    }

    @Subscribe(tags = {@Tag(Constants.RxBusEventType.POPUP_MENU_DIALOG_SHOW_DISMISS)})
    public void onPopupMenuDialogDismiss(Integer type) {
        if (type == Constants.MSG_DIALOG_DISMISS) {
            WebService.stop(this);
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mFab, "translationY", mFab.getHeight() * 2, 0).setDuration(200L);
            objectAnimator.setInterpolator(new AccelerateInterpolator());
            objectAnimator.start();
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        WebService.start(this);
        new PopupMenuDialog(this).builder().setCancelable(false).setCanceledOnTouchOutside(false).show();
    }

    @Override
    public void onAnimationEnd(Animator animation) {
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    void initRecyclerView() {
        mBookshelfAdapter = new BookshelfAdapter(WiFiTransferActivity.this,mBooks);
        mBookList.setHasFixedSize(true);
        mBookList.setLayoutManager(new GridLayoutManager(this, 3));
        mBookList.setAdapter(mBookshelfAdapter);
        //收到传过来的文件立即刷新
        RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //手动刷新
                RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0);
            }
        });

        mBookshelfAdapter.setOnItemClickListener(new BookshelfAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

               // Toast.makeText(WiFiTransferActivity.this, "clicked " + Constants.DIR+"/"+mBooks.get(position),Toast.LENGTH_SHORT).show();
                openFile(Constants.DIR+"/"+mBooks.get(position));
            }

            @Override
            public void onItemLongClick(View view, int position) {
               // Toast.makeText(WiFiTransferActivity.this, "long clicked " + position,Toast.LENGTH_SHORT).show();
                //将取消
                new AlertDialog.Builder(WiFiTransferActivity.this)
                        .setTitle("请选择要进行的操作")
                        .setMessage("确定要删除文件" + mBooks.get(position))
                        .setNegativeButton("确定", (dialog, which) -> {
                            deleteFile(new File(Constants.DIR+"/"+mBooks.get(position)));
                            RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0);
                        })
                        .setPositiveButton("取消", (dialog, which) -> {
                        }).show();
            }
        });

    }

    @Subscribe(thread = EventThread.IO, tags = {@Tag(Constants.RxBusEventType.LOAD_BOOK_LIST)})
    public void loadBookList(Integer type) {
        Timber.d("loadBookList:" + Thread.currentThread().getName());
        List<String> books = new ArrayList<>();
        File dir = Constants.DIR;
        if (dir.exists() && dir.isDirectory()) {
            String[] fileNames = dir.list();
            if (fileNames != null) {
                for (String fileName : fileNames) {
                    books.add(fileName);
                }
            }
        }
        runOnUiThread(() -> {
            mSwipeRefreshLayout.setRefreshing(false);
            mBooks.clear();
            mBooks.addAll(books);
            mBookshelfAdapter.notifyDataSetChanged();
        });
    }

    @Deprecated
    private void loadBookList() {
        Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                List<String> books = new ArrayList<>();
                File dir = Constants.DIR;
                if (dir.exists() && dir.isDirectory()) {
                    String[] fileNames = dir.list();
                    for (String fileName : fileNames) {
                        books.add(fileName);
                    }
                }
                subscriber.onNext(books);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<String>>() {
            @Override
            public void onCompleted() {
                mBookshelfAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {
                mBookshelfAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNext(List<String> books) {
                mBooks.clear();
                mBooks.addAll(books);
            }
        });
    }

//    public class BookshelfAdapter extends RecyclerView.Adapter<BookshelfAdapter.MyViewHolder> {
//        private OnItemClickListener mOnItemClickListener;
//        @Override
//        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
//                    WiFiTransferActivity.this).inflate(R.layout.book_item, parent,
//                    false));
//            return holder;
//        }
//
//        @Override
//        public void onBindViewHolder(final MyViewHolder holder, int position) {
//            holder.mTvBookName.setText(mBooks.get(position));
//            // item click
//            if (mOnItemClickListener != null) {
//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        mOnItemClickListener.onItemClick(holder.itemView, position);
//                    }
//                });
//            }
//
//            // item long click
//            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    mOnItemClickListener.onItemLongClick(holder.itemView, position);
//                    return true;
//                }
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return mBooks.size();
//        }
//
//       interface OnItemClickListener {
//            void onItemClick(View view, int position);
//            void onItemLongClick(View view, int position);
//        }
//
//        class MyViewHolder extends RecyclerView.ViewHolder {
//            TextView mTvBookName;
//            public MyViewHolder(View view) {
//                super(view);
//                mTvBookName = view.findViewById(R.id.book_name);
//            }
//        }
//    }
}
