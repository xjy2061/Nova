package org.xjy.android.nova.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.xjy.android.nova.utils.NovaLoader;
import org.xjy.android.nova.utils.NovaUtils;

import java.util.ArrayList;
import java.util.List;

public class NovaRecyclerView extends RecyclerView {
    private NovaAdapter mAdapter;
    private NovaLoader mLoader;
    private boolean mLoadMore;
    private boolean mLoadingMore;
    private boolean mFirstLoad = true;
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
                if (mLoadMore) {
                    LayoutManager layoutManager = getLayoutManager();
                    int visibleItemCount = layoutManager.getChildCount();
                    int itemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = itemCount;
                    if (layoutManager instanceof LinearLayoutManager) {
                        firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                        StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                        if (mVisibleItemPositions == null) {
                            mVisibleItemPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                        }
                        staggeredGridLayoutManager.findFirstVisibleItemPositions(mVisibleItemPositions);
                        for (int visibleItemPosition : mVisibleItemPositions) {
                            if (visibleItemPosition < firstVisibleItemPosition) {
                                firstVisibleItemPosition = visibleItemPosition;
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
            GridLayoutManager localLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookup = localLayoutManager.getSpanSizeLookup();
            localLayoutManager.setSpanSizeLookup(new NovaSpanSizeLookupDelegate(spanSizeLookup, localLayoutManager.getSpanCount(), adapter));
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            //IMPROVE
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
        mAdapter.showLoadView();
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
        mAdapter.showEmptyView(content, onClickListener);
    }

    public void hideEmptyView() {
        mAdapter.hideEmptyView();
    }

    public void hideLoadView() {
        mAdapter.hideLoadView();
    }

    public void addPlaceholderView(int height) {
        if (mPlaceholderViewHeight != height) {
            mPlaceholderViewHeight = height;
            if (mAdapter != null) {
                mAdapter.setPlaceholderViewHeight(mPlaceholderViewHeight);
            }
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
        return viewType == VIEW_TYPES.EMPTY || viewType == VIEW_TYPES.LOAD || viewType == VIEW_TYPES.PLACEHOLDER;
    }

    private static class EmptyView extends TextView {

        public EmptyView(Context context) {
            super(context);
            int padding = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics()) + 0.5);
            setPadding(0, padding, 0, padding);
            setGravity(Gravity.CENTER);
        }
    }

    private static class LoadView extends RelativeLayout {
        private int mMinHeight;

        public LoadView(Context context, int minHeight) {
            super(context);
            mMinHeight = minHeight;
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(CENTER_IN_PARENT);
            addView(new ProgressBar(context, null), layoutParams);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            LayoutManager layoutManager = ((RecyclerView) getParent().getParent()).getLayoutManager();
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
        public static final int NORMAL = 100;
    }

    public static abstract class NovaAdapter<T, VH extends NovaViewHolder> extends RecyclerView.Adapter<NovaViewHolder> {
        protected ArrayList<T> mItems = new ArrayList<>();

        private boolean mHasEmptyView;
        private boolean mShowEmptyView;
        private CharSequence mEmptyContent;
        private OnClickListener mEmptyOnClickListener;
        private boolean mHasLoadView;
        private boolean mShowLoadView;
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

        void showEmptyView(CharSequence content, OnClickListener onClickListener) {
            mShowEmptyView = true;
            mEmptyContent = content;
            mEmptyOnClickListener = onClickListener;
            if (!mHasEmptyView) {
                mHasEmptyView = true;
                notifyItemInserted(getEmptyViewAdapterPosition());
            } else {
                notifyItemChanged(getEmptyViewAdapterPosition());
            }
        }

        void hideEmptyView() {
            if (mHasEmptyView) {
                mShowEmptyView = false;
                notifyItemChanged(getEmptyViewAdapterPosition());
            }
        }

        void showLoadView() {
            mShowLoadView = true;
            if (!mHasLoadView) {
                mHasLoadView = true;
                notifyItemInserted(getLoadViewAdapterPosition());
            } else {
                notifyItemChanged(getLoadViewAdapterPosition());
            }
        }

        void hideLoadView() {
            if (mHasLoadView) {
                mShowLoadView = false;
                notifyItemChanged(getLoadViewAdapterPosition());
            }
        }

        private int getEmptyViewAdapterPosition() {
            return mItems.size();
        }

        private int getLoadViewAdapterPosition() {
            return mHasEmptyView ? mItems.size() + 1 : mItems.size();
        }

        void setPlaceholderViewHeight(int placeholderViewHeight) {
            mPlaceholderViewHeight = placeholderViewHeight;
            notifyDataSetChanged();
        }

        public abstract VH onCreateNormalViewHolder(ViewGroup parent, int viewType);

        @Override
        public NovaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPES.EMPTY) {
                Context context = parent.getContext();
                RelativeLayout emptyItemView = new RelativeLayout(context);
                emptyItemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                EmptyView emptyView = new EmptyView(context);
                emptyItemView.addView(emptyView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                return new NovaViewHolder(emptyItemView);
            } else if (viewType == VIEW_TYPES.LOAD) {
                Context context = parent.getContext();
                RelativeLayout loadItemView = new RelativeLayout(context);
                loadItemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                loadItemView.addView(new LoadView(context, NovaUtils.dpToPx(60)));
                return new NovaViewHolder(loadItemView);
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
            int viewType = holder.getItemViewType();
            if (viewType == VIEW_TYPES.EMPTY) {
                View emptyView = ((ViewGroup) holder.itemView).getChildAt(0);
                if (mShowEmptyView) {
                    emptyView.setVisibility(View.VISIBLE);
                    if (emptyView instanceof EmptyView) {
                        ((EmptyView) emptyView).setText(mEmptyContent);
                        emptyView.setOnClickListener(mEmptyOnClickListener);
                    }
                } else {
                    emptyView.setVisibility(View.GONE);
                }
            } else if (viewType == VIEW_TYPES.LOAD) {
                if (mShowLoadView) {
                    ((ViewGroup) holder.itemView).getChildAt(0).setVisibility(View.VISIBLE);
                } else {
                    ((ViewGroup) holder.itemView).getChildAt(0).setVisibility(View.GONE);
                }
            } else if (position < getNormalItemCount()) {
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
            } else if (mHasLoadView && ((mPlaceholderViewHeight > 0 && position == itemCount - 2) || position == itemCount - 1)) {
                return VIEW_TYPES.LOAD;
            } else if (mHasEmptyView && ((mPlaceholderViewHeight > 0 && mHasLoadView && position == itemCount - 3)
                    || ((mPlaceholderViewHeight > 0 || mHasLoadView) && position == itemCount - 2) || position == itemCount -1)) {
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
            if (mHasEmptyView) {
                count++;
            }
            if (mHasLoadView) {
                count++;
            }
            if (mPlaceholderViewHeight > 0) {
                count++;
            }
            return getNormalItemCount() + count;
        }

//        public long getHeaderId(int position) {
//            return -1;
//        }
//
//        protected NovaViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
//            return null;
//        }
//
//        protected void onBindHeaderViewHolder(ViewHolder viewHolder, int position) {}
    }

    public static class NovaViewHolder extends RecyclerView.ViewHolder {
        int mDividerHeight;
        int mDividerColor;
        int mDividerMarginLeft;
        int mDividerMarginRight;
        boolean mNeedOffset;

        public NovaViewHolder(View itemView) {
            super(itemView);
        }

        public void setDivider(int dividerHeight, int dividerColor, int dividerMarginLeft, int dividerMarginRight, boolean needOffset) {
            mDividerHeight = dividerHeight;
            mDividerColor = dividerColor;
            mDividerMarginLeft = dividerMarginLeft;
            mDividerMarginRight = dividerMarginRight;
            mNeedOffset = needOffset;
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
            int left;
            int top;
            int right;
            int bottom;
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
            if (viewHolder.mNeedOffset) {
                int orientation = getOrientation(parent);
                if (orientation == OrientationHelper.VERTICAL) {
                    outRect.set(0, 0, 0, viewHolder.mDividerHeight);
                } else {
                    outRect.set(0, 0, viewHolder.mDividerHeight, 0);
                }
            }
        }
    }

    public static abstract class ExpandableAdapter<VH extends NovaViewHolder> extends NovaAdapter<ExpandableItem, VH> {
        private static final long FLAG_EXPANDED = 0x0000000080000000L;
        private static final long LOWER_32BIT_MASK = 0x00000000ffffffffL;
        private static final long LOWER_31BIT_MASK = 0x000000007fffffffL;
        private static final int FLAG_VIEW_TYPE_GROUP = 0x80000000;

        private ArrayList<Long> mGroupInfo = new ArrayList<>();
        private int mLastCalculatedPosition;
        private int mExpandedChildCount;

        @Override
        public void setItems(List<ExpandableItem> items) {
            mItems.clear();
            mItems.addAll(items);
            mGroupInfo.clear();
            mExpandedChildCount = 0;
            int groupCount = mItems.size();
            for (int i = 0; i < groupCount; i++) {
                ExpandableItem item = mItems.get(i);
                int childCount = item.mChildList.size();
                long adapterPosition = (long) (i + mExpandedChildCount) << 32;
                if (item.mExpanded) {
                    mGroupInfo.add(adapterPosition | FLAG_EXPANDED | childCount);
                    mExpandedChildCount += item.mChildList.size();
                } else {
                    mGroupInfo.add(adapterPosition | childCount);
                }
            }
            mLastCalculatedPosition = Math.max(0, groupCount - 1);
            notifyDataSetChanged();
        }

        @Override
        public void addItems(List<ExpandableItem> items) {
            int groupPosition = mItems.size();
            mItems.addAll(items);
            notifyGroupRangeInserted(groupPosition, items.size());
        }

        public void toggleGroup(int groupPosition, ExpandListener expandListener) {
            long groupInfo = mGroupInfo.get(groupPosition);
            if ((groupInfo & FLAG_EXPANDED) == 0) {
                expandGroup(groupPosition, expandListener);
            } else {
                collapseGroup(groupPosition, expandListener);
            }
        }

        public void expandGroup(int groupPosition, ExpandListener expandListener) {
            long groupInfo = mGroupInfo.get(groupPosition);
            if ((groupInfo & FLAG_EXPANDED) == 0) {
                mGroupInfo.set(groupPosition, groupInfo | FLAG_EXPANDED);
                int childCount = (int) (groupInfo & LOWER_31BIT_MASK);
                mExpandedChildCount += childCount;
                mLastCalculatedPosition = Math.min(mLastCalculatedPosition, groupPosition);
                notifyItemRangeInserted(getAdapterPosition(groupPosition, NO_POSITION) + 1, childCount);
                if (expandListener != null) {
                    expandListener.onExpand();
                }
            }
        }

        public void collapseGroup(int groupPosition, ExpandListener expandListener) {
            long groupInfo = mGroupInfo.get(groupPosition);
            if ((groupInfo & FLAG_EXPANDED) != 0) {
                mGroupInfo.set(groupPosition, groupInfo & (~FLAG_EXPANDED));
                int childCount = (int) (groupInfo & LOWER_31BIT_MASK);
                mExpandedChildCount -= childCount;
                mLastCalculatedPosition = Math.min(mLastCalculatedPosition, groupPosition);
                notifyItemRangeRemoved(getAdapterPosition(groupPosition, NO_POSITION) + 1, childCount);
                if (expandListener != null) {
                    expandListener.onCollapse();
                }
            }
        }

        public void notifyGroupChanged(int groupPosition) {
            int position = getAdapterPosition(groupPosition, NO_POSITION);
            if (position != NO_POSITION) {
                notifyItemChanged(position);
            }
        }

        public void notifyGroupAndChildrenChanged(int groupPosition) {
            int position = getAdapterPosition(groupPosition, NO_POSITION);
            if (position != NO_POSITION) {
                long groupInfo = mGroupInfo.get(groupPosition);
                notifyItemRangeChanged(position, (int) (1 + ((groupInfo & FLAG_EXPANDED) != 0 ? groupInfo & LOWER_31BIT_MASK : 0)));
            }
        }

        public void notifyGroupInserted(int groupPosition) {
            notifyGroupRangeInserted(groupPosition, 1);
        }

        public void notifyGroupRangeInserted(int groupPosition, int count) {
            int insertedCount = insertGroupItems(groupPosition, count);
            int position = getAdapterPosition(groupPosition, NO_POSITION);
            if (position != NO_POSITION) {
                notifyItemRangeInserted(position, insertedCount);
            }
        }

        private int insertGroupItems(int groupPosition, int count) {
            int insertedCount = 0;
            for (int i = groupPosition, end = groupPosition + count; i < end; i++) {
                insertedCount++;
                ExpandableItem item = mItems.get(i);
                int childCount = item.mChildList.size();
                if (item.mExpanded) {
                    mGroupInfo.add(i, ((long) i << 32) | FLAG_EXPANDED | childCount);
                    mExpandedChildCount += childCount;
                    insertedCount += childCount;
                } else {
                    mGroupInfo.add(i, ((long) i << 32) | childCount);
                }
            }
            mLastCalculatedPosition = Math.min(mLastCalculatedPosition, Math.max(0, groupPosition - 1));
            return insertedCount;
        }

        public void notifyGroupRemoved(int groupPosition) {
            notifyGroupRangeRemoved(groupPosition, 1);
        }

        public void notifyGroupRangeRemoved(int groupPosition, int count) {
            int position = getAdapterPosition(groupPosition, NO_POSITION);
            int removedCount = removeGroupItems(groupPosition, count);
            if (position != NO_POSITION) {
                notifyItemRangeRemoved(position, removedCount);
            }
        }

        private int removeGroupItems(int groupPosition, int count) {
            int removedCount = 0;
            while (count-- > 0) {
                removedCount++;
                long groupInfo = mGroupInfo.remove(groupPosition);
                if ((groupInfo & FLAG_EXPANDED) != 0) {
                    int childCount = (int) (groupInfo & LOWER_31BIT_MASK);
                    mExpandedChildCount -= childCount;
                    removedCount += childCount;
                }
            }
            mLastCalculatedPosition = Math.min(mLastCalculatedPosition, Math.max(0, groupPosition - 1));
            return removedCount;
        }

        public void notifyChildChanged(int groupPosition, int childPosition) {
            notifyChildRangeChanged(groupPosition, childPosition, 1);
        }

        public void notifyChildRangeChanged(int groupPosition, int childPosition, int count) {
            int position = getAdapterPosition(groupPosition, childPosition);
            if (position != NO_POSITION) {
                notifyItemRangeChanged(position, count);
            }
        }

        public void notifyChildInserted(int groupPosition, int childPosition) {
            notifyChildRangeInserted(groupPosition, childPosition, 1);
        }

        public void notifyChildRangeInserted(int groupPosition, int childPosition, int count) {
            int insertedCount = insertChildItems(groupPosition, childPosition, count);
            if (insertedCount > 0) {
                int position = getAdapterPosition(groupPosition, childPosition);
                if (position != NO_POSITION) {
                    notifyItemRangeInserted(position, insertedCount);
                }
            }
        }

        private int insertChildItems(int groupPosition, int childPosition, int count) {
            long groupInfo = mGroupInfo.get(groupPosition);
            int childCount = (int) (groupInfo & LOWER_31BIT_MASK);
            if (childPosition < 0 || childPosition > childCount) {
                throw new IllegalStateException("Insert child to invalid position: groupPosition " + groupPosition + " childPosition " + childPosition + " count " + count);
            }
            int insertedCount = 0;
            mGroupInfo.set(groupPosition, groupInfo & (~LOWER_31BIT_MASK) | (childCount + count));
            if ((groupInfo & FLAG_EXPANDED) != 0) {
                mExpandedChildCount += count;
                insertedCount = count;
            }
            mLastCalculatedPosition = Math.min(mLastCalculatedPosition, groupPosition);
            return insertedCount;
        }

        public void notifyChildRemoved(int groupPosition, int childPosition) {
            notifyChildRangeRemoved(groupPosition, childPosition, 1);
        }

        public void notifyChildRangeRemoved(int groupPosition, int childPosition, int count) {
            int position = getAdapterPosition(groupPosition, childPosition);
            int removedCount = removeChildItems(groupPosition, childPosition, count);
            if (removedCount > 0 && position != NO_POSITION) {
                notifyItemRangeRemoved(position, removedCount);
            }
        }

        private int removeChildItems(int groupPosition, int childPosition, int count) {
            long groupInfo = mGroupInfo.get(groupPosition);
            int childCount = (int) (groupInfo & LOWER_31BIT_MASK);
            if (childPosition < 0 || (childPosition + count) > childCount) {
                throw new IllegalStateException("Remove child on invalid position: groupPosition " + groupPosition + " childPosition " + childPosition + " count " + count);
            }
            int removedCount = 0;
            mGroupInfo.set(groupPosition, groupInfo & (~LOWER_31BIT_MASK) | (childCount - count));
            if ((groupInfo & FLAG_EXPANDED) != 0) {
                mExpandedChildCount -= count;
                removedCount = count;
            }
            mLastCalculatedPosition = Math.min(mLastCalculatedPosition, groupPosition);
            return removedCount;
        }

        private Pair<Integer, Integer> getDataPosition(int adapterPosition) {
            if (adapterPosition == 0) {
                return new Pair<>(0, NO_POSITION);
            }
            int lastCalculatedGroupAdapterPosition = (int) (mGroupInfo.get(mLastCalculatedPosition) >>> 32);
            if (adapterPosition == lastCalculatedGroupAdapterPosition) {
                return new Pair<>(mLastCalculatedPosition, NO_POSITION);
            }
            if (adapterPosition < lastCalculatedGroupAdapterPosition) {
                int start = 0;
                int end = mLastCalculatedPosition;
                while (start <= end) {
                    int mid = (start + end) >>> 1;
                    int midVal = (int) (mGroupInfo.get(mid) >>> 32);
                    if (adapterPosition > midVal) {
                        start = mid + 1;
                    } else if (adapterPosition < midVal) {
                        end = mid - 1;
                    } else {
                        return new Pair<>(mid, NO_POSITION);
                    }
                }
                int groupPosition = start - 1;
                return new Pair<>(groupPosition, adapterPosition - groupPosition - 1);
            }
            int start = mLastCalculatedPosition;
            for (int i = start, size = mGroupInfo.size(); i < size; i++) {
                long groupInfo = mGroupInfo.get(i);
                if (i != start) {
                    mGroupInfo.set(i, ((long) lastCalculatedGroupAdapterPosition << 32) | (groupInfo & LOWER_32BIT_MASK));
                    mLastCalculatedPosition = i;
                    if (adapterPosition == lastCalculatedGroupAdapterPosition) {
                        return new Pair<>(i, NO_POSITION);
                    } else {
                        lastCalculatedGroupAdapterPosition++;
                    }
                }
                if ((groupInfo & FLAG_EXPANDED) != 0) {
                    int childCount = (int) (groupInfo & LOWER_31BIT_MASK);
                    if (childCount > 0 && lastCalculatedGroupAdapterPosition + childCount > adapterPosition) {
                        return new Pair<>(i, adapterPosition - lastCalculatedGroupAdapterPosition);
                    } else {
                        lastCalculatedGroupAdapterPosition += childCount;
                    }
                }
            }
            return new Pair<>(NO_POSITION, NO_POSITION);
        }

        private int getAdapterPosition(int groupPosition, int childPosition) {
            long groupInfo = mGroupInfo.get(groupPosition);
            if (childPosition != NO_POSITION && (groupInfo & FLAG_EXPANDED) == 0) {
                return NO_POSITION;
            }
            if (groupPosition <= mLastCalculatedPosition) {
                return (int) (groupInfo >>> 32 + childPosition + 1);
            }
            groupInfo = mGroupInfo.get(mLastCalculatedPosition);
            int lastCalculatedGroupAdapterPosition = (int) (groupInfo >>> 32 + 1 + ((groupInfo & FLAG_EXPANDED) != 0 ? (groupInfo & LOWER_31BIT_MASK) : 0));
            for (int i = mLastCalculatedPosition + 1, size = mGroupInfo.size(); i < size; i++) {
                groupInfo = mGroupInfo.get(i);
                mGroupInfo.set(i, ((long) lastCalculatedGroupAdapterPosition << 32) | (groupInfo & LOWER_32BIT_MASK));
                mLastCalculatedPosition = i;
                if (i == groupPosition) {
                    return lastCalculatedGroupAdapterPosition + childPosition + 1;
                } else {
                    lastCalculatedGroupAdapterPosition += 1 + ((groupInfo & FLAG_EXPANDED) != 0 ? groupInfo & LOWER_31BIT_MASK : 0);
                }
            }
            return NO_POSITION;
        }

        @Override
        public VH onCreateNormalViewHolder(ViewGroup parent, int viewType) {
            if ((viewType & FLAG_VIEW_TYPE_GROUP) != 0) {
                return onCreateGroupViewHolder(parent, viewType & (~FLAG_VIEW_TYPE_GROUP));
            } else {
                return onCreateChildViewHolder(parent, viewType & (~FLAG_VIEW_TYPE_GROUP));
            }
        }

        public abstract VH onCreateGroupViewHolder(ViewGroup parent, int groupViewType);

        public abstract VH onCreateChildViewHolder(ViewGroup parent, int childViewType);

        @Override
        public void onBindNormalViewHolder(VH holder, int position) {
            Pair<Integer, Integer> dataPosition = getDataPosition(position);
            if (dataPosition.second == NO_POSITION) {
                onBindGroupViewHolder(holder, dataPosition.first);
            } else {
                onBindChildViewHolder(holder, dataPosition.first, dataPosition.second);
            }
        }

        public abstract void onBindGroupViewHolder(VH holder, int groupPosition);

        public abstract void onBindChildViewHolder(VH holder, int groupPosition, int childPosition);

        @Override
        protected int getNormalItemViewType(int position) {
            Pair<Integer, Integer> dataPosition = getDataPosition(position);
            if (dataPosition.second == NO_POSITION) {
                return getGroupItemViewType(dataPosition.first) | FLAG_VIEW_TYPE_GROUP;
            } else {
                return getChildItemViewType(dataPosition.first, dataPosition.second);
            }
        }

        public abstract int getGroupItemViewType(int groupPosition);

        public abstract int getChildItemViewType(int groupPosition, int childPosition);

        @Override
        public int getNormalItemCount() {
            return mItems.size() + mExpandedChildCount;
        }
    }

    public static class ExpandableItem<G, C> {
        private G mGroupData;
        private ArrayList<C> mChildList;
        private boolean mExpanded;

        public ExpandableItem(@NonNull G groupData, @NonNull ArrayList<C> childList, boolean expanded) {
            mGroupData = groupData;
            mChildList = childList;
            mExpanded = expanded;
        }

        public G getGroupData() {
            return mGroupData;
        }

        public ArrayList<C> getChildList() {
            return mChildList;
        }
    }

    public interface ExpandListener {
        void onExpand();

        void onCollapse();
    }

//    public static class StickyHeaderDecoration extends RecyclerView.ItemDecoration {
//        private NovaAdapter mAdapter;
//        private LongSparseArray<View> mHeaderViews = new LongSparseArray<>();
//
//        public StickyHeaderDecoration(NovaAdapter adapter) {
//            mAdapter = adapter;
//        }
//
//        @Override
//        public void onDrawOver(Canvas c, RecyclerView parent, State state) {
//            int childCount = parent.getChildCount();
//            if (childCount <= 0 || mAdapter.getNormalItemCount() <= 0) {
//                return;
//            }
//            for (int i = 0; i < childCount; i++) {
//                View itemView = parent.getChildAt(i);
//                int position = parent.getChildAdapterPosition(itemView);
//                if (position == RecyclerView.NO_POSITION) {
//                    continue;
//                }
//                boolean hasStickyHeader = hasStickyHeader(i, position);
//                if (hasStickyHeader || hasGroupHeader(position)) {
//                    View headerView = getHeaderView(parent, position);
//                    int orientation = getOrientation(parent);
//                    int left = 0;
//                    int top = 0;
//                    int parentPaddingLeft = 0;
//                    int parentPaddingTop = 0;
//                    int parentPaddingRight = 0;
//                    int parentPaddingBottom = 0;
//                    boolean clipToPadding = parent.getLayoutManager().getClipToPadding();
//                    if (clipToPadding) {
//                        parentPaddingLeft = parent.getPaddingLeft();
//                        parentPaddingTop = parent.getPaddingTop();
//                        parentPaddingRight = parent.getPaddingRight();
//                        parentPaddingBottom = parent.getPaddingBottom();
//                    }
//                    if (orientation == OrientationHelper.VERTICAL) {
//                        top = Math.max(itemView.getTop() - headerView.getHeight(), parentPaddingTop);
//                    } else {
//                        left = Math.max(itemView.getLeft() - headerView.getWidth(), parentPaddingLeft);
//                    }
//                    Rect bounds = new Rect(left, top, left + headerView.getWidth(), top + headerView.getHeight());
//                    if (hasStickyHeader && hasGroupHeader(position + 1)) {
//                        View nextHeaderView = getHeaderView(parent, position + 1);
//                        View nextItemView = parent.getChildAt(i + 1);
//                        if (orientation == OrientationHelper.VERTICAL) {
//                            int stickyHeaderTop = nextItemView.getTop() - nextHeaderView.getHeight() - headerView.getHeight();
//                            if (stickyHeaderTop < bounds.top) {
//                                bounds.top = stickyHeaderTop;
//                            }
//                        } else {
//                            int stickyHeaderLeft = nextItemView.getLeft() - nextHeaderView.getWidth() - headerView.getWidth();
//                            if (stickyHeaderLeft < bounds.left) {
//                                bounds.left = stickyHeaderLeft;
//                            }
//                        }
//                    }
//
//                    c.save();
//                    if (clipToPadding) {
//                        c.clipRect(new Rect(parentPaddingLeft, parentPaddingTop, parent.getWidth() - parentPaddingRight, parent.getHeight() - parentPaddingBottom));
//                    }
//                    c.translate(bounds.left, bounds.top);
//                    headerView.draw(c);
//                    c.restore();
//                }
//            }
//        }
//
//        @Override
//        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
//            super.getItemOffsets(outRect, view, parent, state);
//        }
//
//        private boolean hasStickyHeader(int layoutPosition, int adapterPosition) {
//            if (layoutPosition > 0 || mAdapter.getHeaderId(adapterPosition) < 0) {
//                return false;
//            }
//            return true;
//        }
//
//        private boolean hasGroupHeader(int position) {
//            long headerId;
//            if (position < 0 || position >= mAdapter.getNormalItemCount() || (headerId = mAdapter.getHeaderId(position)) < 0) {
//                return false;
//            }
//            return position == 0 || headerId != mAdapter.getHeaderId(position - 1);
//        }
//
//        private View getHeaderView(RecyclerView parent, int position) {
//            long headerId = mAdapter.getHeaderId(position);
//            View headerView = mHeaderViews.get(headerId);
//            if (headerView == null) {
//                NovaViewHolder viewHolder = mAdapter.onCreateHeaderViewHolder(parent);
//                mAdapter.onBindHeaderViewHolder(viewHolder, position);
//                headerView = viewHolder.itemView;
//                mHeaderViews.put(headerId, headerView);
//            }
//            return headerView;
//        }
//    }
}
