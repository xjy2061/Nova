package org.xjy.android.nova.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.xjy.android.nova.R;
import org.xjy.android.nova.util.NovaLoader;

import java.util.ArrayList;
import java.util.List;

public class NovaRecyclerView extends RecyclerView {

    public static final int LAYOUT_MANAGER_TYPE_LINEAR = 1;
    public static final int LAYOUT_MANAGER_TYPE_GRID = 2;
    public static final int LAYOUT_MANAGER_TYPE_STAGGERED_GRID = 3;

    private NovaAdapter mAdapter;
    private NovaLoader mLoader;
    private boolean mLoadMore;
    private boolean mLoadingMore;
    private boolean mFirstLoad = true;
    private int mLayoutManagerType;
    private int[] mVisibleItemPositions;
    private int mPlaceholderViewHeight;

    public NovaRecyclerView(Context context) {
        this(context, null);
    }

    public NovaRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NovaRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mLoadMore) {
                    LayoutManager layoutManager = getLayoutManager();
                    if (mLayoutManagerType == 0) {
                        if (layoutManager instanceof LinearLayoutManager) {
                            mLayoutManagerType = LAYOUT_MANAGER_TYPE_LINEAR;
                        } else if (layoutManager instanceof GridLayoutManager) {
                            mLayoutManagerType = LAYOUT_MANAGER_TYPE_GRID;
                        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                            mLayoutManagerType = LAYOUT_MANAGER_TYPE_STAGGERED_GRID;
                        }
                    }

                    int visibleItemCount = layoutManager.getChildCount();
                    int itemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = itemCount;
                    if (mLayoutManagerType == LAYOUT_MANAGER_TYPE_LINEAR || mLayoutManagerType == LAYOUT_MANAGER_TYPE_GRID) {
                        firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    } else if (mLayoutManagerType == LAYOUT_MANAGER_TYPE_STAGGERED_GRID) {
                        StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                        if (mVisibleItemPositions == null) {
                            mVisibleItemPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                        }
                        staggeredGridLayoutManager.findFirstVisibleItemPositions(mVisibleItemPositions);
                        int length = mVisibleItemPositions.length;
                        for (int i = 0; i < length; i++) {
                            if (mVisibleItemPositions[i] < firstVisibleItemPosition) {
                                firstVisibleItemPosition = mVisibleItemPositions[i];
                            }
                        }
                    }

                    if (!mLoadingMore && (firstVisibleItemPosition + visibleItemCount) >= itemCount - 1) {
                        load();
                    }
                }
            }
        });
    }

    public void setAdapter(NovaAdapter adapter) {
        mAdapter = adapter;
        super.setAdapter(adapter);
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager != null) {
            delegateSpanSizeLookup(layoutManager, adapter);
        }
        if (mPlaceholderViewHeight > 0) {
            mAdapter.setPlaceholderViewHeight(mPlaceholderViewHeight);
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (mAdapter != null) {
            delegateSpanSizeLookup(layout, mAdapter);
        }
        super.setLayoutManager(layout);
    }

    private void delegateSpanSizeLookup(LayoutManager layoutManager, NovaAdapter adapter) {
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager layout = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookup = layout.getSpanSizeLookup();
            layout.setSpanSizeLookup(new NovaSpanSizeLookupDelegate(spanSizeLookup, layout.getSpanCount(), adapter));
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            //TODO
        }
    }

    public <T> void setLoader(NovaLoader<List<T>> loader) {
        mLoader = loader;
        mLoader.setInterceptor(new NovaLoader.Interceptor<List<T>>() {
            @Override
            public void onPreComplete(List<T> data) {
                mLoadingMore = false;
                mAdapter.hideLoadView();
                if (mFirstLoad) {
                    mAdapter.setItems(data);
                    mFirstLoad = false;
                } else {
                    mAdapter.addItems(data);
                }
            }

            @Override
            public void onPreError(Throwable error) {
                mLoadingMore = false;
                mAdapter.hideLoadView();
            }

            @Override
            public void onPreProgressUpdate(List<T> data) {
                mAdapter.hideLoadView();
            }
        });
    }

    public void load() {
        mLoadingMore = true;
        mAdapter.hideEmptyView();
        if (mAdapter.getLoadView() == null) {
            Context context = getContext();
            RelativeLayout loadView = new RelativeLayout(context);
            loadView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            loadView.addView(new LoadView(context, context.getResources().getDimensionPixelSize(R.dimen.loadViewHeight)));
            mAdapter.setLoadView(loadView);
        } else {
            mAdapter.showLoadView();
        }
        mLoader.forceLoad();
    }

    public void enableLoadMore() {
        mLoadMore = true;
    }

    public void disableLoadMore() {
        mLoadMore = false;
        mAdapter.hideLoadView();
    }

    public void showEmptyView(CharSequence content, OnClickListener onClickListener) {
        if (mAdapter.getEmptyView() == null) {
            Context context = getContext();
            RelativeLayout emptyViewWrapper = new RelativeLayout(context);
            emptyViewWrapper.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            EmptyView emptyView = new EmptyView(context);
            emptyView.setText(content);
            if (onClickListener != null) {
                emptyView.setOnClickListener(onClickListener);
            }
            emptyViewWrapper.addView(emptyView);
            mAdapter.setEmptyView(emptyViewWrapper);
        } else {
            mAdapter.showEmptyView(content);
        }
    }

    public void addPlaceholderView(int height) {
        mPlaceholderViewHeight = height;
        if (mAdapter != null) {
            mAdapter.setPlaceholderViewHeight(mPlaceholderViewHeight);
        }
    }

    public void reset() {
        mFirstLoad = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mLoader != null) {
            mLoader.cancelLoad();
        }
        super.onDetachedFromWindow();
    }

    public static int getOrientation(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getOrientation();
        } else {
            return ((StaggeredGridLayoutManager) layoutManager).getOrientation();
        }
    }

    public static boolean isPreservedViewType(RecyclerView recyclerView, View child) {
        int viewType = recyclerView.getChildViewHolder(child).getItemViewType();
        if (viewType == VIEW_TYPES.EMPTY || viewType == VIEW_TYPES.LOAD || viewType == VIEW_TYPES.PLACEHOLDER) {
            return true;
        }
        return false;
    }

    private class EmptyView extends TextView {

        public EmptyView(Context context) {
            super(context);
            setGravity(Gravity.CENTER);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            LayoutManager layoutManager = getLayoutManager();
            int height = layoutManager.getHeight();
            if (height > 0) {
                int childCount = layoutManager.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = layoutManager.getChildAt(i);
                    if (layoutManager.getItemViewType(child) == VIEW_TYPES.EMPTY) {
                        break;
                    }
                    height -= layoutManager.getDecoratedMeasuredHeight(child);
                }
                super.onMeasure(MeasureSpec.makeMeasureSpec(layoutManager.getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                return;
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private class LoadView extends RelativeLayout {

        private int mMinHeight;

        public LoadView(Context context, int minHeight) {
            super(context);
            mMinHeight = minHeight;
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(CENTER_IN_PARENT);
            addView(new ProgressBar(context), layoutParams);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            LayoutManager layoutManager = getLayoutManager();
            int height = layoutManager.getHeight();
            if (height > 0) {
                int childCount = layoutManager.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = layoutManager.getChildAt(i);
                    if (layoutManager.getItemViewType(child) == VIEW_TYPES.LOAD) {
                        break;
                    }
                    height -= layoutManager.getDecoratedMeasuredHeight(child);
                }
                if (height < mMinHeight) {
                    height = mMinHeight;
                }
                super.onMeasure(MeasureSpec.makeMeasureSpec(layoutManager.getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                return;
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public static class VIEW_TYPES {
        public static final int EMPTY = 0;
        public static final int LOAD = 1;
        public static final int PLACEHOLDER = 2;
        public static final int NORMAL = 3;
    }

    public static abstract class NovaAdapter<T, VH extends NovaViewHolder> extends RecyclerView.Adapter<NovaViewHolder> {

        private ArrayList<T> mItems = new ArrayList<T>();

        private RelativeLayout mEmptyView;
        private RelativeLayout mLoadView;
        private int mPlaceholderViewHeight;

        public void setItems(List<T> items) {
            mItems.clear();
            mItems.addAll(items);
            notifyDataSetChanged();
        }

        public void addItems(List<T> items) {
            int insertPosition = mItems.size();
            mItems.addAll(items);
            notifyItemRangeInserted(insertPosition, items.size());
        }

        public T getItem(int position) {
            return mItems.get(position);
        }

        public List<T> getItems() {
            return mItems;
        }

        public RelativeLayout getEmptyView() {
            return mEmptyView;
        }

        public void setEmptyView(RelativeLayout emptyView) {
            mEmptyView = emptyView;
            notifyItemInserted(mItems.size());
        }

        public void showEmptyView(CharSequence content) {
            EmptyView emptyView = (EmptyView) mEmptyView.getChildAt(0);
            emptyView.setText(content);
            emptyView.setVisibility(View.VISIBLE);
        }

        public void hideEmptyView() {
            if (mEmptyView != null) {
                mEmptyView.getChildAt(0).setVisibility(View.GONE);
            }
        }

        public RelativeLayout getLoadView() {
            return mLoadView;
        }

        void setLoadView(RelativeLayout loadView) {
            mLoadView = loadView;
            notifyItemInserted(mEmptyView != null ? mItems.size() + 1 : mItems.size());
        }

        public void showLoadView() {
            mLoadView.getChildAt(0).setVisibility(View.VISIBLE);
        }

        public void hideLoadView() {
            mLoadView.getChildAt(0).setVisibility(View.GONE);
        }

        public int getPlaceholderViewHeight() {
            return mPlaceholderViewHeight;
        }

        void setPlaceholderViewHeight(int placeholderViewHeight) {
            mPlaceholderViewHeight = placeholderViewHeight;
            notifyDataSetChanged();
        }

        public abstract VH onCreateNormalViewHolder(ViewGroup parent, int viewType);

        @Override
        public NovaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPES.EMPTY) {
                return new NovaViewHolder(mEmptyView);
            } else if (viewType == VIEW_TYPES.LOAD) {
                return new NovaViewHolder(mLoadView);
            } else if (viewType == VIEW_TYPES.PLACEHOLDER) {
                View view = new View(parent.getContext());
                view.setMinimumHeight(mPlaceholderViewHeight);
                return new NovaViewHolder(view);
            } else {
                return onCreateNormalViewHolder(parent, viewType);
            }
        }

        public abstract void onBindNormalViewHolder(VH holder, int position);

        @Override
        public void onBindViewHolder(NovaViewHolder holder, int position) {
            if (position < mItems.size()) {
                onBindNormalViewHolder((VH) holder, position);
            }
        }

        protected int getNormalItemViewType(int position) {
            return VIEW_TYPES.NORMAL;
        }

        @Override
        public int getItemViewType(int position) {
            int itemCount = getItemCount();
            if (mPlaceholderViewHeight > 0 && position == itemCount - 1) {
                return VIEW_TYPES.PLACEHOLDER;
            } else if (mLoadView != null && ((mPlaceholderViewHeight > 0 && position == itemCount - 2) || position == itemCount - 1)) {
                return VIEW_TYPES.LOAD;
            } else if (mEmptyView != null && ((mPlaceholderViewHeight > 0 && mLoadView != null && position == itemCount - 3)
                    || ((mPlaceholderViewHeight > 0 || mLoadView != null) && position == itemCount - 2) || position == itemCount -1)) {
                return VIEW_TYPES.EMPTY;
            } else {
                return getNormalItemViewType(position);
            }
        }

        public int getNormalItemCount() {
            return mItems.size();
        }

        @Override
        public int getItemCount() {
            int count = 0;
            if (mEmptyView != null) {
                count++;
            }
            if (mLoadView != null) {
                count++;
            }
            if (mPlaceholderViewHeight > 0) {
                count++;
            }
            return mItems.size() + count;
        }

        public long getHeaderId(int position) {
            return -1;
        }

        protected NovaViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
            return null;
        }

        protected void onBindHeaderViewHolder(ViewHolder viewHolder, int position) {
        }

    }

    public static class NovaViewHolder extends RecyclerView.ViewHolder {

        int mDividerHeight;
        int mDividerColor;
        int mDividerMarginLeft;
        int mDividerMarginRight;

        public NovaViewHolder(View itemView) {
            super(itemView);
        }

        public void setDivider(int dividerHeight, int dividerColor, int dividerMarginLeft, int dividerMarginRight) {
            mDividerHeight = dividerHeight;
            mDividerColor = dividerColor;
            mDividerMarginLeft = dividerMarginLeft;
            mDividerMarginRight = dividerMarginRight;
        }

    }

    public static class NovaSpanSizeLookupDelegate extends GridLayoutManager.SpanSizeLookup {

        private GridLayoutManager.SpanSizeLookup mSpanSizeLookup;
        private int mSpanCount;
        private NovaAdapter mAdapter;

        public NovaSpanSizeLookupDelegate(GridLayoutManager.SpanSizeLookup spanSizeLookup, int spanCount, NovaAdapter adapter) {
            mSpanSizeLookup = spanSizeLookup;
            mSpanCount = spanCount;
            mAdapter = adapter;
            setSpanIndexCacheEnabled(true);
        }

        @Override
        public int getSpanSize(int position) {
            int viewType = mAdapter.getItemViewType(position);
            if (viewType == VIEW_TYPES.EMPTY || viewType == VIEW_TYPES.LOAD || viewType == VIEW_TYPES.PLACEHOLDER) {
                return mSpanCount;
            }
            return mSpanSizeLookup.getSpanSize(position);
        }

        @Override
        public int getSpanIndex(int position, int spanCount) {
            int viewType = mAdapter.getItemViewType(position);
            if (viewType == VIEW_TYPES.EMPTY || viewType == VIEW_TYPES.LOAD || viewType == VIEW_TYPES.PLACEHOLDER) {
                return 0;
            }
            return mSpanSizeLookup.getSpanIndex(position, spanCount);
        }

        @Override
        public int getSpanGroupIndex(int adapterPosition, int spanCount) {
            int viewType = mAdapter.getItemViewType(adapterPosition);
            if (viewType == VIEW_TYPES.EMPTY || viewType == VIEW_TYPES.LOAD || viewType == VIEW_TYPES.PLACEHOLDER) {
                return super.getSpanGroupIndex(adapterPosition, spanCount);
            }
            return mSpanSizeLookup.getSpanGroupIndex(adapterPosition, spanCount);
        }
    }

    public static class NovaDividerItemDecoration extends ItemDecoration {

        private Paint mPaint;

        public NovaDividerItemDecoration() {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = 0;
            int top = 0;
            int right = 0;
            int bottom = 0;
            int orientation = getOrientation(parent);
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                if (isPreservedViewType(parent, child)) {
                    continue;
                }
                NovaViewHolder viewHolder = (NovaViewHolder) parent.getChildViewHolder(child);
                if (viewHolder.mDividerHeight <= 0) {
                    continue;
                }
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                if (orientation == OrientationHelper.VERTICAL) {
                    top = child.getBottom() + params.bottomMargin;
                    bottom = top + viewHolder.mDividerHeight;
                    left = parent.getPaddingLeft() + viewHolder.mDividerMarginLeft;
                    right = parent.getWidth() - parent.getPaddingRight() - viewHolder.mDividerMarginRight;
                } else {
                    top = parent.getPaddingTop() + viewHolder.mDividerMarginLeft;
                    bottom = parent.getHeight() - parent.getPaddingBottom() - viewHolder.mDividerMarginRight;
                    left = child.getRight() + params.rightMargin;
                    right = left + viewHolder.mDividerHeight;
                }
                mPaint.setColor(viewHolder.mDividerColor);
                mPaint.setStrokeWidth(viewHolder.mDividerHeight);
                c.drawLine(left, top, right, bottom, mPaint);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            NovaViewHolder viewHolder = (NovaViewHolder) parent.getChildViewHolder(view);
            int orientation = getOrientation(parent);
            if (orientation == OrientationHelper.VERTICAL) {
                outRect.set(0, 0, 0, viewHolder.mDividerHeight);
            } else {
                outRect.set(0, 0, viewHolder.mDividerHeight, 0);
            }
        }

    }

    public static class StickyHeaderDecoration extends RecyclerView.ItemDecoration {

        private NovaAdapter mAdapter;
        private LongSparseArray<View> mHeaderViews = new LongSparseArray<View>();

        public StickyHeaderDecoration(NovaAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, State state) {
            int childCount = parent.getChildCount();
            if (childCount <= 0 || mAdapter.getNormalItemCount() <= 0) {
                return;
            }
            for (int i = 0; i < childCount; i++) {
                View itemView = parent.getChildAt(i);
                int position = parent.getChildAdapterPosition(itemView);
                if (position == RecyclerView.NO_POSITION) {
                    continue;
                }
                boolean hasStickyHeader = hasStickyHeader(i, position);
                if (hasStickyHeader || hasGroupHeader(position)) {
                    View headerView = getHeaderView(parent, position);
                    int orientation = getOrientation(parent);
                    int left = 0;
                    int top = 0;
                    int parentPaddingLeft = 0;
                    int parentPaddingTop = 0;
                    int parentPaddingRight = 0;
                    int parentPaddingBottom = 0;
                    boolean clipToPadding = parent.getLayoutManager().getClipToPadding();
                    if (clipToPadding) {
                        parentPaddingLeft = parent.getPaddingLeft();
                        parentPaddingTop = parent.getPaddingTop();
                        parentPaddingRight = parent.getPaddingRight();
                        parentPaddingBottom = parent.getPaddingBottom();
                    }
                    if (orientation == OrientationHelper.VERTICAL) {
                        top = Math.max(itemView.getTop() - headerView.getHeight(), parentPaddingTop);
                    } else {
                        left = Math.max(itemView.getLeft() - headerView.getWidth(), parentPaddingLeft);
                    }
                    Rect bounds = new Rect(left, top, left + headerView.getWidth(), top + headerView.getHeight());
                    if (hasStickyHeader && hasGroupHeader(position + 1)) {
                        View nextHeaderView = getHeaderView(parent, position + 1);
                        View nextItemView = parent.getChildAt(i + 1);
                        if (orientation == OrientationHelper.VERTICAL) {
                            int stickyHeaderTop = nextItemView.getTop() - nextHeaderView.getHeight() - headerView.getHeight();
                            if (stickyHeaderTop < bounds.top) {
                                bounds.top = stickyHeaderTop;
                            }
                        } else {
                            int stickyHeaderLeft = nextItemView.getLeft() - nextHeaderView.getWidth() - headerView.getWidth();
                            if (stickyHeaderLeft < bounds.left) {
                                bounds.left = stickyHeaderLeft;
                            }
                        }
                    }

                    c.save();
                    if (clipToPadding) {
                        c.clipRect(new Rect(parentPaddingLeft, parentPaddingTop, parent.getWidth() - parentPaddingRight, parent.getHeight() - parentPaddingBottom));
                    }
                    c.translate(bounds.left, bounds.top);
                    headerView.draw(c);
                    c.restore();
                }
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            super.getItemOffsets(outRect, view, parent, state);
        }

        private boolean hasStickyHeader(int layoutPosition, int adapterPosition) {
            if (layoutPosition > 0 || mAdapter.getHeaderId(adapterPosition) < 0) {
                return false;
            }
            return true;
        }

        private boolean hasGroupHeader(int position) {
            long headerId;
            if (position < 0 || position >= mAdapter.getNormalItemCount() || (headerId = mAdapter.getHeaderId(position)) < 0) {
                return false;
            }
            return position == 0 || headerId != mAdapter.getHeaderId(position - 1);
        }

        private View getHeaderView(RecyclerView parent, int position) {
            long headerId = mAdapter.getHeaderId(position);
            View headerView = mHeaderViews.get(headerId);
            if (headerView == null) {
                NovaViewHolder viewHolder = mAdapter.onCreateHeaderViewHolder(parent);
                mAdapter.onBindHeaderViewHolder(viewHolder, position);
                headerView = viewHolder.itemView;
                mHeaderViews.put(headerId, headerView);
            }
            return headerView;
        }

    }

}
