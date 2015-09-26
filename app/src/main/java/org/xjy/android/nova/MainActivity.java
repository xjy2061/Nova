package org.xjy.android.nova;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.xjy.android.nova.util.NovaLoader;
import org.xjy.android.nova.widget.NovaRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private PlaceholderFragment mPlaceHolderFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        // Inflate the "decor.xml"
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        DrawerLayout drawer = (DrawerLayout) inflater.inflate(R.layout.decor, null);
//        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
//        View child = decor.getChildAt(0);
//        decor.removeView(child);
//        FrameLayout container = (FrameLayout) findViewById(R.id.container);
//        container.addView(child);
//        decor.addView((View) container.getParent());

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        mPlaceHolderFragment = PlaceholderFragment.newInstance(position + 1);
        fragmentManager.beginTransaction()
                .replace(R.id.container, mPlaceHolderFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_example) {
            mPlaceHolderFragment.load();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
       try {
           moveTaskToBack(false);
       } catch (NullPointerException e) {
           e.printStackTrace();
       }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private NovaRecyclerView mRecyclerView;
        private SimpleAdapter mAdapter;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Context context = getActivity();

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mRecyclerView = (NovaRecyclerView) rootView.findViewById(R.id.list);
            mAdapter = new SimpleAdapter();
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            mRecyclerView.setLoader(new NovaLoader<List<String>>(context) {
                @Override
                public void onComplete(List<String> data) {
                }

                @Override
                public void onError(Throwable error) {

                }

                @Override
                public List<String> loadInBackground() {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    ArrayList<String> list = new ArrayList<String>();
//                    for (int i = 0; i < 10; i++) {
//                        list.add("item " + mAdapter.getNormalItemCount());
//                    }
                    return list;
                }


            });
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < 3; i++) {
                list.add("item " + mAdapter.getNormalItemCount());
            }
            mAdapter.addItems(list);
            mRecyclerView.load();
            return rootView;
        }

        public void load() {
            mRecyclerView.load();
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public static class SimpleAdapter extends NovaRecyclerView.NovaAdapter<String, SimpleViewHolder> {

        @Override
        public SimpleViewHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            SimpleViewHolder viewHolder = new SimpleViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindNormalViewHolder(SimpleViewHolder holder, int position) {
            holder.textView.setText(getItem(position));
        }

        @Override
        public NovaRecyclerView.NovaViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
            return null;
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        }


    }

    public static class SimpleViewHolder extends NovaRecyclerView.NovaViewHolder {

        TextView textView;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
        }

    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

}
