package com.notekeep.andrekelvin.notekeep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity implements NoteRecyclerViewAdapter.OnItemLongClick,NoteRecyclerViewAdapter.OnItemClick {

    private TextView textNoNote;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private NoteRecyclerViewAdapter viewAdapter;
    private List<NoteItems> noteList, selectedItemID;
    private List<String> deletedTitle, deletedNote, deleteDate;
    private List<Integer> deletedID;
    private NoteDB noteDB;
    private boolean grid = true;
    private ActionMode actionMode;
    private Snackbar snackbar;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final String LINEAR_TO_GRID_LAYOUT = "com.example.andrekelvin.notekeep_change_layout";
    public static final String ITEM_IS_LONG_CLICKED="itemIsLongClicked";
    private String selectedLayout;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseUser firebaseUser;
    private BackupReceiver backupReceiver;
    //private Paint p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textNoNote = findViewById(R.id.textNoNote);
        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        drawerLayout=findViewById(R.id.drawerLayout);
        navigationView=findViewById(R.id.navView);
        Toolbar toolbar=findViewById(R.id.toolbar);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        setSupportActionBar(toolbar);

        //This will place a Navigation Drawer icon on the Tool bar
        //When Clicked it will open the Navigation Drawer and Animation happens on the icon
        ActionBarDrawerToggle actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        noteDB = new NoteDB(this);

        noteList = new ArrayList<>();
        selectedItemID = new ArrayList<>();
        deletedTitle = new ArrayList<>();
        deletedNote = new ArrayList<>();
        deletedID = new ArrayList<>();
        deleteDate = new ArrayList<>();

        backupReceiver=new BackupReceiver();
        registerNetworkBroadcastForNougatAbove();

        /*p = new Paint();
        p.setColor(Color.RED);*/

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        selectedLayout = sharedPreferences.getString(LINEAR_TO_GRID_LAYOUT, "Linear");
        if (selectedLayout.contentEquals("Grid")) {
            grid = false;
            recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
            recyclerView.setHasFixedSize(true);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setHasFixedSize(true);
        }

        viewAdapter = new NoteRecyclerViewAdapter(noteList, this, selectedItemID);
        recyclerView.setAdapter(viewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        viewAdapter.setActionModeReceiver(this,this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddNoteActivity.class));
            }
        });

        snackbar = Snackbar.make(findViewById(R.id.drawerLayout), "Note Deleted", Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.setDuration(10000);

        //Swipe Left or Right to Delete
        //Display Snack bar to undo delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                /*View itemView = viewHolder.itemView;
                ColorDrawable background=new ColorDrawable(Color.RED);
                if (dX > 0) {
                    background.setBounds(itemView.getLeft(), itemView.getTop(), (int) dX, itemView.getBottom());
                    background.draw(c);
                }else {
                    background.setBounds((int)(itemView.getRight()+ dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);
                }*/

                new RecyclerViewSwipeDecorator.Builder(MainActivity.this, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        /*.addSwipeLeftActionIcon(R.drawable.ic_edit)
                        .addSwipeRightActionIcon(R.drawable.ic_delete)
                        .addSwipeLeftBackgroundColor(Color.RED)
                        .addSwipeRightBackgroundColor(Color.BLUE)*/
                        .addBackgroundColor(Color.RED)
                        .addActionIcon(R.drawable.ic_delete)
                        .addSwipeLeftLabel("Delete")
                        .addSwipeRightLabel("Delete")
                        .setSwipeLeftLabelColor(Color.WHITE)
                        .setSwipeRightLabelColor(Color.WHITE)
                        .create()
                        .decorate();



                /*Drawable icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_delete);
                // compute top and left margin to the view bounds
                icon.setBounds(viewHolder.itemView.getLeft() , viewHolder.itemView.getTop(), viewHolder.itemView.getRight(), viewHolder.itemView.getBottom() + icon.getIntrinsicHeight());
                icon.draw(c);*/

                //View itemView = viewHolder.itemView;
                /*if (dX > 0) {
                    // Draw Rect with varying right side, equal to displacement dX
                    c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                            (float) itemView.getBottom(), p);
                } else {
                    // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                            (float) itemView.getRight(), (float) itemView.getBottom(), p);
                }*/

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
                deletedTitle.clear();
                deletedNote.clear();
                deletedID.clear();
                deleteDate.clear();

                final int position = viewHolder.getAdapterPosition();

                deletedID.add(viewAdapter.getNoteAt(position).getId());
                deletedTitle.add(viewAdapter.getNoteAt(position).getTitle());
                deletedNote.add(viewAdapter.getNoteAt(position).getNote());
                deleteDate.add(viewAdapter.getNoteAt(position).getDateTime());

                noteList.remove(position);
                viewAdapter.notifyItemRemoved(position);
                viewAdapter.updateFilterNoteList(noteList);

                snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            //Get ID and Cancel Alarm
                            //and delete the note from database only when the snack bar is Dismissed
                            //and Undo button is not clicked
                            Log.d("My_Log", "Noted Deleted");
                            noteDB.open();
                            Cursor cursor = noteDB.getDeletingPendingIntentID(deletedID.get(0));
                            if (cursor.moveToFirst()) {
                                int pendingIntentID=cursor.getInt(0);
                                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                Intent intent = new Intent(MainActivity.this, ReminderReceiver.class);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,pendingIntentID, intent, 0);
                                alarmManager.cancel(pendingIntent);
                                //Toast.makeText(MainActivity.this, "Reminder Canceled", Toast.LENGTH_SHORT).show();
                            }
                            noteDB.deleteNote(deletedID.get(0));
                            System.out.println("Deleted ID:"+deletedID.get(0));
                            noteDB.close();
                        }
                    }
                });
                snackbar.setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noteList.add(position, new NoteItems(deletedID.get(0), deletedTitle.get(0), deletedNote.get(0), deleteDate.get(0)));
                        viewAdapter.notifyItemInserted(position);
                        viewAdapter.updateFilterNoteList(noteList);
                        Toast.makeText(MainActivity.this, "Deleted Note Restored", Toast.LENGTH_SHORT).show();
                    }
                });
                snackbar.show();
            }
        }).attachToRecyclerView(recyclerView);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){
                    case R.id.allNote:
                        menuItem.setChecked(true);
                        displayAllNote();
                        drawerLayout.closeDrawers();
                        return true;

                    case R.id.allReminderNote:
                        menuItem.setChecked(true);
                        displayAllReminderNote();
                        drawerLayout.closeDrawers();
                        return true;

                    case R.id.backUpNote:
                        menuItem.setChecked(true);
                        if (firebaseUser==null){
                            startActivity(new Intent(MainActivity.this,SignUpActivity.class));
                        }else {
                            startActivity(new Intent(MainActivity.this,BackUpActivity.class));
                        }
                        drawerLayout.closeDrawers();
                        return true;
                }

                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(navigationView.getMenu().findItem(R.id.allNote).isChecked()){
            displayAllNote();
        } else if(navigationView.getMenu().findItem(R.id.allReminderNote).isChecked()){
            displayAllReminderNote();
        } else {
            displayAllNote();
        }

        disableItemClick();
    }

    private void displayAllNote() {
        noteList.clear();
        selectedItemID.clear();

        noteDB.open();
        Cursor cursor = noteDB.getAllNotes();
        while (cursor.moveToNext()) {
            noteList.add(new NoteItems
                    (cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3)));
            //Log.d("My Tag",cursor.getLong(4)+"\t\t"+cursor.getInt(5)+"\t\t"+cursor.getString(6));
        }
        setRecyclerViewVisibility();
        viewAdapter.notifyDataSetChanged();
        noteDB.close();

        viewAdapter.updateFilterNoteList(noteList);
    }

    private void displayAllReminderNote(){
        noteList.clear();
        selectedItemID.clear();

        noteDB.open();
        Cursor cursor = noteDB.getAllReminderNotes();
        while (cursor.moveToNext()) {
            noteList.add(new NoteItems
                    (cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3)));
            //Log.d("My Tag",cursor.getLong(4)+"\t\t"+cursor.getInt(5)+"\t\t"+cursor.getString(6));
        }
        setRecyclerViewVisibility();
        viewAdapter.notifyDataSetChanged();
        noteDB.close();

        viewAdapter.updateFilterNoteList(noteList);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.switch_layout, menu);

        if (selectedLayout.contentEquals("Grid")) {
            menu.findItem(R.id.linear_grid_layout).setIcon(R.drawable.ic_view_headline);
        } else {
            menu.findItem(R.id.linear_grid_layout).setIcon(R.drawable.ic_grid);
        }

        final MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                setItemsVisibility(menu, searchItem, false);
                fab.hide();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                setItemsVisibility(menu, searchItem, true);
                fab.show();
                return true;
            }
        });

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                /*String userInput = s.toLowerCase();
                List<NoteItems> filterList = new ArrayList<>();

                for (NoteItems recyclerData : noteList) {
                    if (recyclerData.getTitle().toLowerCase().contains(userInput) || recyclerData.getNote().toLowerCase().contains(userInput)) {
                        filterList.add(recyclerData);
                        System.out.println(filterList);
                    }
                }

                viewAdapter.filterRecyclerView(filterList);*/
                viewAdapter.getFilter().filter(s);

                return true;
            }
        });

        return true;
    }

    //This make sure the expended search view takes the entire Action Bar space
    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) {
                item.setVisible(visible);
            }
        }
    }

    //change recycler view layout form Linear to Grid and Grid to Linear
    //and menu icon from Grid to Linear and Linear to Grid
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        editor = sharedPreferences.edit();
        if (item.getItemId() == R.id.linear_grid_layout) {
            if (grid) {
                item.setIcon(R.drawable.ic_view_headline);
                grid = false;
                recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                recyclerView.setHasFixedSize(true);
                editor.putString(LINEAR_TO_GRID_LAYOUT, "Grid");
            } else {
                item.setIcon(R.drawable.ic_grid);
                grid = true;
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setHasFixedSize(true);
                editor.putString(LINEAR_TO_GRID_LAYOUT, "Linear");
            }
            editor.apply();
        } else if (item.getItemId() == R.id.search) {
            //item.expandActionView();
        }
        return super.onOptionsItemSelected(item);
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.delete_note, menu);
            actionMode.setTitle("Delete Note");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.deleteIcon:
                    new DeleteNote(MainActivity.this).execute();

                    disableItemClick();

                case R.id.selectAll:
                    viewAdapter.selectAllNote();
                    return true;

                case R.id.unSelectAll:
                    viewAdapter.unSelectAllNote();
                    actionMode.finish();

                    disableItemClick();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            viewAdapter.unSelectAllNote();
            actionMode = null;
        }
    };

    @Override
    public void onItemLongClick() {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);

            editor=sharedPreferences.edit();
            editor.putBoolean(ITEM_IS_LONG_CLICKED,true);
            editor.apply();
        } else {
            if (selectedItemID.isEmpty()) {
                actionMode.finish();

                disableItemClick();
            }
        }
    }

    @Override
    public void onItemClick() {
        if (selectedItemID.isEmpty()) {
            actionMode.finish();

            disableItemClick();
        }
    }

    private void setRecyclerViewVisibility() {
        if (!noteList.isEmpty()) {
            textNoNote.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            textNoNote.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * when item on the recycler is unselected/unHighlighted
     * this change the shared Preferences value to false
     * which means no item is long clicked
     */
    private void disableItemClick(){
        editor=sharedPreferences.edit();
        editor.putBoolean(ITEM_IS_LONG_CLICKED,false);
        editor.apply();
    }

    private static class DeleteNote extends AsyncTask<Void, Void, Void> {

        private WeakReference<MainActivity> weakReference;
        private List<NoteItems> selectedNoteID;

        public DeleteNote(MainActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            final MainActivity activity = weakReference.get();
            if (activity == null || activity.isFinishing()) {

            }
            assert activity != null;

            selectedNoteID = new ArrayList<>(activity.selectedItemID);
            /*activity.deletedID.clear();
            activity.deletedTitle.clear();
            activity.deletedNote.clear();*/
            for (int i = 0; i < activity.selectedItemID.size(); i++) {
                /*activity.deletedID.add(activity.selectedItemID.get(i).getId());
                activity.deletedTitle.add(activity.selectedItemID.get(i).getTitle());
                activity.deletedNote.add(activity.selectedItemID.get(i).getNote());*/

                /*Comparator comparator=Collections.reverseOrder();
                Collections.sort(activity.selectedNotePosition,comparator);*/
                //activity.noteList.remove(Integer.parseInt(String.valueOf(activity.selectedNotePosition.get(i))));
                activity.noteList.remove(activity.selectedItemID.get(i));
                activity.viewAdapter.updateFilterNoteList(activity.noteList);
                //activity.viewAdapter.notifyItemRemoved(activity.selectedNotePosition.get(i));
                activity.viewAdapter.notifyDataSetChanged();
                //System.out.println("Remove Notes at "+Integer.parseInt(String.valueOf(activity.selectedNotePosition.get(i))));
            }
            /*activity.noteList.subList(activity.selectedNotePosition.get(0),activity.selectedNotePosition.size()).clear();
            activity.viewAdapter.notifyItemRangeRemoved(activity.selectedNotePosition.get(0),activity.selectedNotePosition.size());*/
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final MainActivity activity = weakReference.get();
            if (activity == null || activity.isFinishing()) {

            }
            assert activity != null;

            activity.snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    if (event != DISMISS_EVENT_ACTION) {
                        activity.noteDB.open();
                        //Get ID and Cancel Alarm
                        //delete the note from database only when the snack bar is Dismissed
                        //and Undo button is not clicked
                        for (int i = 0; i < selectedNoteID.size(); i++) {
                            Cursor cursor = activity.noteDB.getDeletingPendingIntentID(selectedNoteID.get(i).getId());
                            if (cursor.moveToFirst()) {
                                int pendingIntentID=cursor.getInt(0);
                                AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
                                Intent intent = new Intent(activity, ReminderReceiver.class);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(activity,pendingIntentID, intent, 0);
                                alarmManager.cancel(pendingIntent);
                                //Toast.makeText(activity, "Reminder Canceled", Toast.LENGTH_SHORT).show();
                            }
                            activity.noteDB.deleteNote(selectedNoteID.get(i).getId());
                            //activity.databaseRef.child(activity.firebaseUser.getUid()+"/"+selectedNoteID.get(i).getId()).removeValue();
                        }
                        activity.noteDB.close();
                        //activity.selectedNotePosition.clear();
                        selectedNoteID.clear();
                        activity.setRecyclerViewVisibility();
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final MainActivity activity = weakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.actionMode.finish();

            activity.snackbar.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*for (int i = 0; i < activity.selectedItemID.size(); i++) {
                        activity.noteList.add(Integer.parseInt(String.valueOf(activity.selectedNotePosition.get(i))),
                                new NoteItems(activity.deletedID.get(i), activity.deletedTitle.get(i), activity.deletedNote.get(i)));
                        activity.viewAdapter.notifyItemInserted(activity.selectedNotePosition.get(i));
                        activity.viewAdapter.notifyDataSetChanged();
                        System.out.println("Add Note at "+Integer.parseInt(String.valueOf(activity.selectedNotePosition.get(i))));
                    }
                    System.out.println("All added note "+activity.selectedNotePosition.toString());*/
                    activity.onResume();
                    //activity.selectedNotePosition.clear();
                    activity.selectedItemID.clear();
                    Toast.makeText(activity, "Deleted Note Restored", Toast.LENGTH_SHORT).show();
                }
            });
            activity.snackbar.show();
        }
    }

    private void registerNetworkBroadcastForNougatAbove() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(backupReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(backupReceiver);
    }

}
