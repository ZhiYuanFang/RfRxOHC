package xyz.ttyz.tourfrxohc.dialog;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.ttyz.mylibrary.method.RxOHCUtils;
import xyz.ttyz.toubasemvvm.adapter.OnClickAdapter;
import xyz.ttyz.toubasemvvm.adapter.utils.BaseEmptyAdapterParent;
import xyz.ttyz.toubasemvvm.adapter.utils.BaseRecyclerAdapter;
import xyz.ttyz.toubasemvvm.ui.BaseTouActivity;
import xyz.ttyz.tourfrxohc.BaseApplication;
import xyz.ttyz.tourfrxohc.R;
import xyz.ttyz.tourfrxohc.databinding.FragmentWaitDialogBinding;
import xyz.ttyz.tourfrxohc.http.BaseSubscriber;
import xyz.ttyz.tourfrxohc.models.UserModel;
import xyz.ttyz.tourfrxohc.utils.DefaultUtils;
import xyz.ttyz.tourfrxohc.utils.UserUtils;
import xyz.ttyz.tourfrxohc.viewholder.MatchItemViewHolder;

public class WaitDialogFragment extends BaseDialogFragment<FragmentWaitDialogBinding> {

    @SuppressLint("StaticFieldLeak")
    private static WaitDialogFragment waitDialogFragment;
    List<UserModel> userModelList;
    WaitDialogDelegate waitDialogDelegate;
    public static WaitDialogFragment getInstance(WaitDialogDelegate waitDialogDelegate) {
        if (waitDialogFragment == null) {
            waitDialogFragment = new WaitDialogFragment(waitDialogDelegate);
        }
        return waitDialogFragment;
    }

    public WaitDialogFragment(WaitDialogDelegate waitDialogDelegate) {
        this.waitDialogDelegate = waitDialogDelegate;
    }

    @Override
    protected int initLayoutID() {
        return R.layout.fragment_wait_dialog;
    }

    BaseEmptyAdapterParent adapter;

    @Override
    protected void initViriable(FragmentWaitDialogBinding mBinding) {
        mBinding.setContext(this);

        adapter = new BaseEmptyAdapterParent(getContext(), new BaseRecyclerAdapter.NormalAdapterDelegate() {
            @Override
            public int getItemViewType(int position) {
                return 0;
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new MatchItemViewHolder(getContext(), parent);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((MatchItemViewHolder) holder).bindData((UserModel) adapter.getItem(position));
            }
        });
        mBinding.setAdapter(adapter);
        if(userModelList != null){
            adapter.setList(userModelList);
        }

    }

    public void refreshList(List<UserModel> list) {
        this.userModelList = list;
        if (adapter != null)
            adapter.setList(list);
    }

    @Override
    protected void initData() {

    }

    public OnClickAdapter.onClickCommand clickCancelMatchCommand = new OnClickAdapter.onClickCommand() {
        @Override
        public void click() {
            new RxOHCUtils<Object>(getActivity()).executeApi(BaseApplication.apiService.leave(DefaultUtils.roomId, UserUtils.getCurUserModel().getId()), new BaseSubscriber<Object>((BaseTouActivity) getActivity()) {
                @Override
                public void success(Object data) {
                    if(waitDialogDelegate != null){
                        waitDialogDelegate.cancelSuccess();
                    }
                    //取消匹配成功
                    WaitDialogFragment.this.dismiss();
                }

                @Override
                public String initCacheKey() {
                    return null;
                }
            });
        }
    };

    public interface WaitDialogDelegate{
        void cancelSuccess();
    }
}
