package xyz.ttyz.tourfrxohc.dialog;

import android.view.Gravity;

import androidx.databinding.ObservableInt;

import com.trello.rxlifecycle2.LifecycleProvider;

import xyz.ttyz.mylibrary.method.ProgressUtil;
import xyz.ttyz.mylibrary.method.RxOHCUtils;
import xyz.ttyz.mylibrary.socket.SocketUtils;
import xyz.ttyz.tou_example.ActivityManager;
import xyz.ttyz.toubasemvvm.adapter.OnClickAdapter;
import xyz.ttyz.toubasemvvm.utils.PopUtils;
import xyz.ttyz.toubasemvvm.utils.ToastUtil;
import xyz.ttyz.tourfrxohc.BaseApplication;
import xyz.ttyz.tourfrxohc.R;
import xyz.ttyz.tourfrxohc.activity.GameActivity;
import xyz.ttyz.tourfrxohc.activity.RoomWaitActivity;
import xyz.ttyz.tourfrxohc.databinding.FragmentDialogCreateHomeBinding;
import xyz.ttyz.tourfrxohc.http.BaseSubscriber;
import xyz.ttyz.tourfrxohc.models.game.HomeModel;
import xyz.ttyz.tourfrxohc.utils.DefaultUtils;
import xyz.ttyz.tourfrxohc.utils.HomeUtils;
import xyz.ttyz.tourfrxohc.utils.UserUtils;

public class CreateHomeDialogFragment extends BaseDialogFragment<FragmentDialogCreateHomeBinding>{
    public ObservableInt roomLimitNumberFiled = new ObservableInt(4);

    @Override
    protected int initLayoutID() {
        return R.layout.fragment_dialog_create_home;
    }

    @Override
    protected void initViriable(FragmentDialogCreateHomeBinding mBinding) {
        mBinding.setContext(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected int showGravity() {
        return Gravity.CENTER;
    }

    public OnClickAdapter.onClickCommand confirmCreateHomeCommand = new OnClickAdapter.onClickCommand() {
        @Override
        public void click() {
            new RxOHCUtils<HomeModel>(getActivity()).executeApi(BaseApplication.apiService.createRoom(roomLimitNumberFiled.get(), UserUtils.getCurUserModel().getId()), new BaseSubscriber<HomeModel>((LifecycleProvider) getActivity()) {
                @Override
                public void success(HomeModel data) {
                    HomeUtils.inHome(data);
                    ToastUtil.showToast("房间创建成功");
                    SocketUtils.openMinaReceiver(ActivityManager.getInstance().getApplication(), new SocketUtils.SocketDelegate() {
                        @Override
                        public void connectSuccess() {
                            ActivityManager.getInstance().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 2021/5/12 进入房间人员界面
                                    RoomWaitActivity.show();

                                    CreateHomeDialogFragment.this.dismiss();
                                }
                            });

                        }

                        @Override
                        public long roomId() {
                            return data.getRoomId();
                        }

                        @Override
                        public long userId() {
                            return UserUtils.getCurUserModel().getId();
                        }
                    });
                }

                @Override
                public String initCacheKey() {
                    return null;
                }
            });
        }
    };

    public OnClickAdapter.onClickCommand clickReduceCommand = new OnClickAdapter.onClickCommand() {
        @Override
        public void click() {
            if(roomLimitNumberFiled.get() > 4){
                roomLimitNumberFiled.set(roomLimitNumberFiled.get() - 1);
            } else {
                ToastUtil.showToast("最少4人");
            }
        }
    };


    public OnClickAdapter.onClickCommand clickAddCommand = new OnClickAdapter.onClickCommand() {
        @Override
        public void click() {
            if(roomLimitNumberFiled.get() < 9){
                roomLimitNumberFiled.set(roomLimitNumberFiled.get() + 1);
            } else {
                ToastUtil.showToast("最多9人");
            }
        }
    };
}
