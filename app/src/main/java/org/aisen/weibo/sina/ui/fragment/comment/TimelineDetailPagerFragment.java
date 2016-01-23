package org.aisen.weibo.sina.ui.fragment.comment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.aisen.android.common.utils.Utils;
import org.aisen.android.support.bean.TabItem;
import org.aisen.android.support.inject.InjectUtility;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.ATabsTabLayoutFragment;
import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.sinasdk.bean.StatusContent;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.ui.activity.base.SinaCommonActivity;
import org.aisen.weibo.sina.ui.fragment.timeline.TimelineRepostFragment;

import java.util.ArrayList;

/**
 * 微博详情页
 *
 * Created by wangdan on 16/1/22.
 */
public class TimelineDetailPagerFragment extends ATabsTabLayoutFragment<TabItem>
                                            implements AppBarLayout.OnOffsetChangedListener, View.OnClickListener {

    public static void launch(Activity from, StatusContent status) {
        FragmentArgs args = new FragmentArgs();
        args.add("status", status);

        SinaCommonActivity.launch(from, TimelineDetailPagerFragment.class, args);
    }

    @ViewInject(id = R.id.layHeader)
    RelativeLayout layHeader;
    @ViewInject(id = R.id.appbar)
    AppBarLayout appBarLayout;
    @ViewInject(id = R.id.toolbar)
    Toolbar toolbar;
    @ViewInject(id = R.id.layHeaderDivider)
    View layHeaderDivider;
    @ViewInject(id = R.id.txtAttitudes)
    TextView txtAttitudes;
    @ViewInject(id = R.id.action_menu)
    FloatingActionsMenu action_menu;
    @ViewInject(id = R.id.action_a)
    FloatingActionButton action_a;
    @ViewInject(id = R.id.action_b)
    FloatingActionButton action_b;
    @ViewInject(id = R.id.action_c)
    FloatingActionButton action_c;
    @ViewInject(id = R.id.overlay)
    View overlay;

    private StatusContent mStatusContent;

    @Override
    public int inflateContentView() {
        return -1;
    }

    @Override
    public int inflateActivityContentView() {
        return R.layout.ui_timeline_detail;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        InjectUtility.initInjectedView(this, ((BaseActivity) getActivity()).getRootView());
        layoutInit(inflater, savedInstanceState);

        // 添加HeaderView
        View itemConvertView = inflater.inflate(CommentHeaderItemView.COMMENT_HEADER_01_RES, layHeader, false);
        CommentHeaderItemView headerItemView = new CommentHeaderItemView(this, itemConvertView, mStatusContent);
        headerItemView.onBindData(itemConvertView, null, 0);
        layHeader.addView(itemConvertView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStatusContent = savedInstanceState != null ? (StatusContent) savedInstanceState.getSerializable("status")
                                                    : (StatusContent) getArguments().getSerializable("status");

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.timeline_detail);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setHasOptionsMenu(true);
    }

    @Override
    protected void setupTabLayout(Bundle savedInstanceSate, TabLayout tabLayout) {
        super.setupTabLayout(savedInstanceSate, tabLayout);

        tabLayout.setPadding(Utils.dip2px(16), tabLayout.getPaddingTop(), tabLayout.getPaddingRight(), tabLayout.getPaddingBottom());
        tabLayout.setTabTextColors(getResources().getColor(R.color.text_54),
                getResources().getColor(R.color.text_80));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("status", mStatusContent);
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceState) {
        super.layoutInit(inflater, savedInstanceState);

        appBarLayout.addOnOffsetChangedListener(this);

        // 点赞数
        if (mStatusContent.getAttitudes_count() == 0) {
            txtAttitudes.setText("");
        }
        else {
            txtAttitudes.setText(String.format(getString(R.string.attitudes_format), AisenUtils.getCounter(mStatusContent.getAttitudes_count())));
        }

        action_a.setOnClickListener(this);
        action_b.setOnClickListener(this);
        action_c.setOnClickListener(this);
        overlay.setOnClickListener(this);
        for (int i = 0; i < action_menu.getChildCount(); i++) {
            if (action_menu.getChildAt(i) instanceof AddFloatingActionButton) {
                action_menu.getChildAt(i).setOnClickListener(this);

                break;
            }
        }
    }

    @Override
    protected ArrayList<TabItem> generateTabs() {
        ArrayList<TabItem> tabItems = new ArrayList<>();

        if (mStatusContent.getComments_count() > 0 || mStatusContent.getReposts_count() == 0) {
            tabItems.add(new TabItem("1", String.format(getString(R.string.comment_format), AisenUtils.getCounter(mStatusContent.getComments_count()))));
        }
        if (mStatusContent.getReposts_count() > 0) {
            tabItems.add(new TabItem("2", String.format(getString(R.string.repost_format), AisenUtils.getCounter(mStatusContent.getReposts_count()))));
        }

        return tabItems;
    }

    @Override
    protected Fragment newFragment(TabItem bean) {
        // 微博评论
        if ("1".equals(bean.getType())) {
            return TimelineCommentFragment.newInstance(mStatusContent);
        }
        // 微博转发
        else if ("2".equals(bean.getType())) {
            return TimelineRepostFragment.newInstance(mStatusContent);
        }

        return null;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int visibility = View.VISIBLE;
        // 如果是AppbarLayout滑动到了最顶端，要把这个divider隐藏掉
        if (getTablayout().getHeight() + toolbar.getHeight() - appBarLayout.getHeight() == verticalOffset) {
            visibility = View.GONE;
        }
        if (layHeaderDivider.getVisibility() != visibility)
            layHeaderDivider.setVisibility(visibility);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_cmts, menu);
        menu.removeItem(R.id.fav);
        menu.removeItem(R.id.repost);
        menu.removeItem(R.id.comment);
        if (mStatusContent.getUser() == null ||
                !mStatusContent.getUser().getIdstr().equalsIgnoreCase(AppContext.getAccount().getUser().getIdstr()))
            menu.removeItem(R.id.delete);
        AisenUtils.setStatusShareMenu(menu.findItem(R.id.share), mStatusContent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AisenUtils.onMenuClicked(this, item.getItemId(), mStatusContent);

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        // 点击了+按钮
        if (v instanceof AddFloatingActionButton) {
            if (action_menu.isExpanded()) {
                dismissOverlay();
            }
            else {
                showOverlay();
            }

            action_menu.toggle();

            return;
        }
        // 覆盖层
        else if (v.getId() == R.id.overlay) {
        }
        // 收藏
        else if (v.getId() == R.id.action_a) {
            AisenUtils.onMenuClicked(this, R.id.fav, mStatusContent);
        }
        // 转发
        else if (v.getId() == R.id.action_b) {
            AisenUtils.onMenuClicked(this, R.id.repost, mStatusContent);
        }
        // 评论
        else if (v.getId() == R.id.action_c) {
            AisenUtils.onMenuClicked(this, R.id.comment, mStatusContent);
        }

        dismissOverlay();
        action_menu.collapse();
    }

    private void showOverlay() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(overlay, "alpha", 0.0f, 1.0f);
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                overlay.setVisibility(View.VISIBLE);
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

        });
        animator.start();
    }

    private void dismissOverlay() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(overlay, "alpha", 1.0f, 0.0f);
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                overlay.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

        });
        animator.start();
    }

}