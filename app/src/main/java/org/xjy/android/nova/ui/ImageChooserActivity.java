package org.xjy.android.nova.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.xjy.android.nova.R;
import org.xjy.android.nova.graphic.BitmapUtils;
import org.xjy.android.nova.utils.Constants;
import org.xjy.android.nova.utils.DimensionUtils;
import org.xjy.android.nova.utils.NovaLoader;
import org.xjy.android.nova.utils.PromptUtils;
import org.xjy.android.nova.utils.StateListFactory;
import org.xjy.android.nova.widget.NovaRecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageChooserActivity extends AppCompatActivity {
    public static final String EXTRA_CHECKED_PICTURES = "checked_pictures";

    private NovaRecyclerView mRecyclerView;
    private View mMaskView;
    private TextView mToggleBtn;
    private TextView mPreviewBtn;
    private ListPopupWindow mBucketListPopup;

    private PictureAdapter mAdapter;
    private ArrayList<String> mCheckedPictures;
    private View.OnClickListener mOnClickCameraListener;

    private volatile int mTotalCount = -1;
    private volatile int mOffset;

    private ShowBucketListRunnable mShowBucketListRunnable;
    private String mCurrentBucketId;
    private ArrayList<Bucket> mBucketList;
    private String mCameraPhotoPath;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_image_chooser);
        setTitle(R.string.photo);

        mRecyclerView = (NovaRecyclerView) findViewById(R.id.imageList);
        mMaskView = findViewById(R.id.mask);
        mToggleBtn = (TextView) findViewById(R.id.toggleBtn);
        mPreviewBtn = (TextView) findViewById(R.id.previewBtn);

        mCheckedPictures = getIntent().getStringArrayListExtra(EXTRA_CHECKED_PICTURES);

        mPreviewBtn.setTextColor(StateListFactory.createColorStateList(0xffcccccc, 0xff333333));

        final int spanCount = 3;
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        final GridLayoutManager.SpanSizeLookup spanSizeLookup = layoutManager.getSpanSizeLookup();
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        final int space4dp = DimensionUtils.dpToIntPx(4);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int spanIndex = spanSizeLookup.getSpanIndex(parent.getChildAdapterPosition(view), spanCount);
                outRect.left = spanIndex * space4dp / spanCount;
                outRect.right = space4dp - (spanIndex + 1) * space4dp / spanCount;
                outRect.top = space4dp;
            }
        });
        mRecyclerView.addPlaceholderView(DimensionUtils.dpToIntPx(57));
        mRecyclerView.enableLoadMore();
        mAdapter = new PictureAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLoader(new NovaLoader<List<String>>(this) {
            @Override
            public void onComplete(List<String> data) {
                if (mOffset >= mTotalCount) {
                    mRecyclerView.disableLoadMore();
                }
            }

            @Override
            public void onError(Throwable error) {
            }

            @Override
            public List<String> loadInBackground() {
                ArrayList<String> ret = new ArrayList<>();
                Cursor cursor = null;
                try {
                    int limit = mOffset > 0 ? 30 : 32;
                    ContentResolver contentResolver = getContentResolver();
                    String selection = mCurrentBucketId == null ? null : MediaStore.Images.Media.BUCKET_ID + "=?";
                    String[] selectionArgs = mCurrentBucketId == null ? null : new String[]{mCurrentBucketId};
                    if (mOffset == 0) {
                        ret.add(null);
                        if (mTotalCount < 0) {
                            cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{"count(*) AS count"}, selection, selectionArgs, null);
                            if (cursor != null && cursor.moveToNext()) {
                                mTotalCount = cursor.getInt(0);
                                cursor.close();
                            }
                        }
                    }
                    String[] projection = {MediaStore.Images.Media.DATA};
                    cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendQueryParameter("limit", mOffset + "," + limit).build(), projection, selection, selectionArgs, MediaStore.Images.Media.DATE_TAKEN + " desc");
                    while (cursor != null && cursor.moveToNext()) {
                        mOffset++;
                        String path = cursor.getString(0);
                        ret.add(path);
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                return ret;
            }
        });
        mToggleBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mShowBucketListRunnable == null) {
                    mShowBucketListRunnable = new ShowBucketListRunnable(v);
                    v.post(mShowBucketListRunnable);
                }
            }
        });
        mPreviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckedPictures.size() > 0) {
                    // TODO: 2016/12/31
//                    ImageGalleryActivity.launch(ImageChooserActivity.this, mCheckedPictures, mCheckedPictures, 0, true, null);
                }
            }
        });
        mOnClickCameraListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String imageFileName = "TRACK_" + timeStamp + "_";
                    File storageDir = Environment.getExternalStorageDirectory();
                    File image = null;
                    try {
                        image = File.createTempFile(
                                imageFileName,  /* prefix */
                                ".jpg",         /* suffix */
                                storageDir      /* directory */
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (image != null) {
                        mCameraPhotoPath = image.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                    }
                    startActivityForResult(takePictureIntent, Constants.REQUEST_CODES.TAKE_PHOTO);
                } else {
                    PromptUtils.showToast(R.string.no_camera_app);
                }
            }
        };
        checkChanged();
        mRecyclerView.load(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String name = getString(R.string.complete);
        int checkedCount = mCheckedPictures.size();
        if (checkedCount > 0) {
            name += "(" + checkedCount + ")";
        }
        MenuItemCompat.setShowAsAction(menu.add(1, 1, 1, name), MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Intent pictureData = new Intent();
            pictureData.putStringArrayListExtra("pictures", mCheckedPictures);
            setResult(Activity.RESULT_OK, pictureData);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODES.TAKE_PHOTO && resultCode == RESULT_OK) {
            if (mCameraPhotoPath != null) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File(mCameraPhotoPath);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);

                mCheckedPictures.add(mCameraPhotoPath);
                Intent pictureData = new Intent();
                pictureData.putStringArrayListExtra("pictures", mCheckedPictures);
                setResult(Activity.RESULT_OK, pictureData);
                finish();
            }
        } else if (requestCode == Constants.REQUEST_CODES.BROWSE_IMAGE && resultCode == RESULT_OK) {
            int actionType = data.getIntExtra("actionType", 0);
            ArrayList<String> checkedPictures = data.getStringArrayListExtra("checkedPictures");
            if (actionType == 1) {
                Intent pictureData = new Intent();
                pictureData.putStringArrayListExtra("pictures", checkedPictures);
                setResult(Activity.RESULT_OK, pictureData);
                finish();
            } else {
                boolean changed = true;
                int size = checkedPictures.size();
                if (size == mCheckedPictures.size()) {
                    changed = false;
                    for (int i = 0; i < size; i++) {
                        if (!checkedPictures.get(i).equals(mCheckedPictures.get(i))) {
                            changed = true;
                            break;
                        }
                    }
                }
                if (changed) {
                    mCheckedPictures = checkedPictures;
                    mAdapter.notifyDataSetChanged();
                    checkChanged();
                }
            }
        }
    }

    private void checkChanged() {
        invalidateOptionsMenu();
        if (mCheckedPictures.size() > 0) {
            mPreviewBtn.setEnabled(true);
        } else {
            mPreviewBtn.setEnabled(false);
        }
    }

    class VIEW_TYPES extends NovaRecyclerView.VIEW_TYPES {
        public static final int CAMERA = 101;
        public static final int PICTURE = 102;
    }

    class PictureAdapter extends NovaRecyclerView.NovaAdapter<String, NovaRecyclerView.NovaViewHolder> {
        private int mImageSize;

        public PictureAdapter() {
            mImageSize = Math.round((getResources().getDisplayMetrics().widthPixels - DimensionUtils.dpToIntPx(8)) / 3 + 0.5f);
        }

        @Override
        public NovaRecyclerView.NovaViewHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPES.CAMERA) {
                return new CameraViewHolder(LayoutInflater.from(ImageChooserActivity.this).inflate(R.layout.image_chooser_camera, parent, false), mImageSize);
            } else {
                return new PictureViewHolder(LayoutInflater.from(ImageChooserActivity.this).inflate(R.layout.image_chooser_item, parent, false), mImageSize);
            }
        }

        @Override
        public void onBindNormalViewHolder(NovaRecyclerView.NovaViewHolder holder, final int position) {
            int viewType = holder.getItemViewType();
            if (viewType == VIEW_TYPES.CAMERA) {
                CameraViewHolder cameraViewHolder = (CameraViewHolder) holder;
                if (mCheckedPictures.size() >= 4) {
                    cameraViewHolder.maskView.setVisibility(View.VISIBLE);
                } else {
                    cameraViewHolder.maskView.setVisibility(View.GONE);
                    cameraViewHolder.cameraView.setOnClickListener(mOnClickCameraListener);
                }
            } else {
                final String path = getItem(position);
                PictureViewHolder pictureViewHolder = (PictureViewHolder) holder;
                // TODO: 2016/12/31
//                NovaImageLoader.loadImage(pictureViewHolder.imageView, "file:///" + path);
                if (path == null) {
                    return;
                }
                int checkedPosition = -1;
                int size = mCheckedPictures.size();
                for (int i = 0; i < size; i++) {
                    if (path.equals(mCheckedPictures.get(i))) {
                        checkedPosition = i;
                        break;
                    }
                }
                pictureViewHolder.checkView.setVisibility(View.VISIBLE);
                pictureViewHolder.maskView.setSelected(false);
                if (checkedPosition > -1) {
                    pictureViewHolder.checkTextView.setBackgroundResource(R.drawable.ic_select_on);
                    pictureViewHolder.checkTextView.setText(String.valueOf(checkedPosition + 1));
                } else if (size >= 4) {
                    pictureViewHolder.checkView.setVisibility(View.GONE);
                    pictureViewHolder.maskView.setSelected(true);
                } else {
                    pictureViewHolder.checkTextView.setBackgroundResource(R.drawable.ic_select_off);
                    pictureViewHolder.checkTextView.setText("");
                }
                pictureViewHolder.checkView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCheckedPictures.contains(path)) {
                            mCheckedPictures.remove(path);
                        } else if (mCheckedPictures.size() < 4) {
                            Pair<Integer, Integer> dimensions = BitmapUtils.decodeDimensions(path);
                            if (dimensions.first >= 100 && dimensions.second >= 100) {
                                mCheckedPictures.add(path);
                            } else {
                                PromptUtils.showToast(R.string.illegal_image_size);
                                return;
                            }
                        }
                        checkChanged();
                        notifyDataSetChanged();
                    }
                });
                pictureViewHolder.maskView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<String> paths = new ArrayList<>(getItems());
                        paths.remove(0);
                        // TODO: 2016/12/31
//                        ImageGalleryActivity.launch(ImageChooserActivity.this, paths, mCheckedPictures, position - 1, true, null);
                    }
                });
            }
        }

        @Override
        protected int getNormalItemViewType(int position) {
            return position == 0 ? VIEW_TYPES.CAMERA : VIEW_TYPES.PICTURE;
        }
    }

    class CameraViewHolder extends NovaRecyclerView.NovaViewHolder {
        private ImageView cameraView;
        private View maskView;

        public CameraViewHolder(View itemView, int imageSize) {
            super(itemView);
            cameraView = (ImageView) itemView.findViewById(R.id.camera);
            maskView = itemView.findViewById(R.id.mask);
            cameraView.setBackgroundDrawable(StateListFactory.createStateListDrawable(new ColorDrawable(0xcc000000), null, null, new ColorDrawable(0x66000000)));
            ViewGroup.LayoutParams layoutParams = cameraView.getLayoutParams();
            layoutParams.width = imageSize;
            layoutParams.height = imageSize;
            maskView.setBackgroundColor(0xccffffff);
            layoutParams = maskView.getLayoutParams();
            layoutParams.width = imageSize;
            layoutParams.height = imageSize;
        }
    }

    class PictureViewHolder extends NovaRecyclerView.NovaViewHolder {
        private SimpleDraweeView imageView;
        private View maskView;
        private View checkView;
        private TextView checkTextView;

        public PictureViewHolder(View itemView, int imageSize) {
            super(itemView);
            imageView = (SimpleDraweeView) itemView.findViewById(R.id.picture);
            maskView = itemView.findViewById(R.id.mask);
            checkView = itemView.findViewById(R.id.check);
            checkTextView = (TextView) itemView.findViewById(R.id.checkText);
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.width = imageSize;
            layoutParams.height = imageSize;
            maskView.setBackgroundDrawable(StateListFactory.createStateListDrawable(new ColorDrawable(0x66000000), new ColorDrawable(0xccffffff), null, null));
            layoutParams = maskView.getLayoutParams();
            layoutParams.width = imageSize;
            layoutParams.height = imageSize;
        }
    }

    class ShowBucketListRunnable implements Runnable {
        View mAnchorView;

        ShowBucketListRunnable(View anchorView) {
            mAnchorView = anchorView;
        }

        @Override
        public void run() {
            if (ImageChooserActivity.this.isFinishing()) {
                return;
            }
            Resources resources = ImageChooserActivity.this.getResources();
            mBucketListPopup = new ListPopupWindow(ImageChooserActivity.this);
            mBucketListPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mBucketListPopup.dismiss();
                    Bucket bucket = (Bucket) parent.getAdapter().getItem(position);
                    if ((mCurrentBucketId != null && !mCurrentBucketId.equals(bucket.id)) || mCurrentBucketId != bucket.id) {
                        mCurrentBucketId = bucket.id;
                        mOffset = 0;
                        mTotalCount = bucket.count;
                        mToggleBtn.setText(bucket.name);
                        mRecyclerView.reset();
                        mRecyclerView.enableLoadMore();
                        mRecyclerView.load(true);
                    }
                }
            });
            mBucketListPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mMaskView.setVisibility(View.GONE);
                    AlphaAnimation anim = new AlphaAnimation(1, 0);
                    anim.setDuration(300);
                    mMaskView.startAnimation(anim);
                }
            });
            if (mBucketList == null) {
                mBucketList = new ArrayList<>();
                Cursor cursor = null;
                try {
                    String[] projection = {MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, "count(*) as count"};
                    cursor = ImageChooserActivity.this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, "1=1) group by (" + MediaStore.Images.Media.BUCKET_ID, null, null);
                    while (cursor!= null && cursor.moveToNext()) {
                        Bucket bucket = new Bucket(cursor.getString(0), cursor.getString(1), null, cursor.getInt(2));
                        projection = new String[]{MediaStore.Images.Media.DATA};
                        Cursor bucketCursor = ImageChooserActivity.this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendQueryParameter("limit", "1").build(), projection, MediaStore.Images.Media.BUCKET_ID + "=?", new String[]{bucket.id}, MediaStore.Images.Media.DATE_TAKEN + " desc");
                        if (bucketCursor != null && bucketCursor.moveToNext()) {
                            bucket.path= bucketCursor.getString(0);
                            bucketCursor.close();
                        }
                        mBucketList.add(bucket);
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                Bucket allBucket = new Bucket(null, getString(R.string.all_photo), mAdapter.getNormalItemCount() > 1 ? mAdapter.getItem(1) : null, mTotalCount);
                mBucketList.add(0, allBucket);
            }
            mBucketListPopup.setAdapter(new BucketAdapter(mBucketList, LayoutInflater.from(ImageChooserActivity.this)));
            mBucketListPopup.setModal(true);
            mBucketListPopup.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            mBucketListPopup.setAnchorView(mAnchorView);
            mBucketListPopup.setContentWidth(resources.getDisplayMetrics().widthPixels);
            mBucketListPopup.setHeight(resources.getDisplayMetrics().heightPixels / 2);
            mBucketListPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
            mBucketListPopup.show();
            mMaskView.setVisibility(View.VISIBLE);
            AlphaAnimation anim = new AlphaAnimation(0, 1);
            anim.setDuration(300);
            mMaskView.startAnimation(anim);
            ListView listView = mBucketListPopup.getListView();
            listView.setVerticalScrollBarEnabled(true);
            listView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_MENU) {
                        mBucketListPopup.dismiss();
                        return true;
                    }
                    return false;
                }
            });
            mShowBucketListRunnable = null;
        }
    }

    class BucketAdapter extends BaseAdapter {
        LayoutInflater mLayoutInflater;
        ArrayList<Bucket> mList = new ArrayList<>();

        BucketAdapter(ArrayList<Bucket> list, LayoutInflater layoutInflater) {
            mLayoutInflater = layoutInflater;
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null || convertView.getTag() == null) {
                convertView = mLayoutInflater.inflate(R.layout.image_bucket_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (SimpleDraweeView) convertView.findViewById(R.id.image);
                viewHolder.nameView = (TextView) convertView.findViewById(R.id.bucketName);
                viewHolder.countView = (TextView) convertView.findViewById(R.id.count);
                viewHolder.checkView = (ImageView) convertView.findViewById(R.id.check);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.render((Bucket) getItem(position));
            return convertView;
        }

        class ViewHolder {
            SimpleDraweeView imageView;
            TextView nameView;
            TextView countView;
            ImageView checkView;

            void render(Bucket bucket) {
                // TODO: 2016/12/31
//                NovaImageLoader.loadImage(imageView, bucket.path == null ? null : "file:///" + bucket.path);
                nameView.setText(bucket.name);
                countView.setText(bucket.count + "");
                if (mCurrentBucketId == bucket.id || (mCurrentBucketId != null && mCurrentBucketId.equals(bucket.id))) {
                    checkView.setVisibility(View.VISIBLE);
                } else {
                    checkView.setVisibility(View.GONE);
                }
            }
        }
    }

    class Bucket {
        String id;
        String name;
        String path;
        int count;

        Bucket(String id, String name, String uri, int count) {
            this.id = id;
            this.name = name;
            this.path = uri;
            this.count = count;
        }
    }
}
