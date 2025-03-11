package net.tigerlight.dad.registration.adapter;

import com.net.tigerlight.dad.R;
import net.tigerlight.dad.registration.activity.DadMainActivity;
import net.tigerlight.dad.registration.fragment.DadChangPassWordFragment;
import net.tigerlight.dad.registration.fragment.DadCreatePinFragment;
import net.tigerlight.dad.registration.fragment.DadEditProfileFragment;
import net.tigerlight.dad.registration.fragment.DadRegistrationFragment;
import net.tigerlight.dad.registration.fragment.SearchIBeacon;
import net.tigerlight.dad.registration.util.DadConstant;
import net.tigerlight.dad.registration.util.DadUtils;
import net.tigerlight.dad.webservices.Logout;
import net.tigerlight.dad.util.Preference;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by indianic on 20/10/16.
 */

public class DadAccountFragmentAdapter extends RecyclerView.Adapter<DadAccountFragmentAdapter.RecyclerViewHolder> {

    String[] name = {"Edit Profile", "Change Password", "Create PIN", "Logout", "Search iBeacon"};

    int[] images = {R.drawable.ic_edit_profile, R.drawable.ic_change_password, R.drawable.ic_create_pin, R.drawable.ic_logout, R.drawable.ic_ibeaconlogo,};

    private Context context;
    private LayoutInflater inflater;
    private Fragment fragment;
    private AsyncTaskLogOut asyncTaskLogOut;
//    private AsyncTaskGetUserInfo asyncTaskGetUserInfo;


    public DadAccountFragmentAdapter(Context context, Fragment fragment) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fragment = fragment;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.row_fragment_account, parent, false);

        RecyclerViewHolder viewHolder = new RecyclerViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {

        holder.tv1.setText(name[position]);
        holder.imageView.setBackgroundResource(images[position]);
        holder.imageView.setOnClickListener(clickListener);
        holder.tv1.setOnClickListener(clickListener);
        holder.tv1.setTag(holder);
        holder.imageView.setTag(holder);

    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            RecyclerViewHolder vholder = (RecyclerViewHolder) v.getTag();
            int position = vholder.getPosition();

            switch (position) {
                case 0:
                    addFragment(new DadEditProfileFragment(), DadEditProfileFragment.class.getSimpleName());
                    break;
                case 1:

                    addFragment(new DadChangPassWordFragment(), DadChangPassWordFragment.class.getSimpleName());

                    break;
                case 2:
                    addFragment(new DadCreatePinFragment(), DadChangPassWordFragment.class.getSimpleName());

                    break;

                case 3:
//                    Utills.displayDialog(context,context.getString(R.string));
                    logOut();
                    break;

                case 4:
                    addFragment(new SearchIBeacon(), SearchIBeacon.class.getSimpleName());
                    break;
            }
        }
    };

    private void logOut() {
        if (DadUtils.isInternetAvailable(context)) {
            if (asyncTaskLogOut != null && asyncTaskLogOut.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskLogOut.execute();
            } else if (asyncTaskLogOut == null || asyncTaskLogOut.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskLogOut = new AsyncTaskLogOut();
                asyncTaskLogOut.execute();
            }
        } else {
            DadUtils.displayDialogNormalMessage(context.getString(R.string.app_name), context.getString(R.string.TAG_INTERNET_AVAILABILITY), context);
        }
    }


    @Override
    public int getItemCount() {
        return name.length;
    }


    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView tv1;
        ImageView imageView;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            tv1 = (TextView) itemView.findViewById(R.id.list_title);
            imageView = (ImageView) itemView.findViewById(R.id.list_avatar);

        }


    }

    private void addFragment(Fragment fragment, String tag) {
        FragmentManager manager = ((DadMainActivity) context).getSupportFragmentManager();
        manager.beginTransaction()
                .add(R.id.activity_registartion_fl_container, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager manager = ((DadMainActivity) context).getSupportFragmentManager();
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        manager.beginTransaction()
                .replace(R.id.activity_registartion_fl_container, fragment, tag)
                .commit();
    }


    private class AsyncTaskLogOut extends AsyncTask<Void, Void, Void> {

        private Logout logout;
        private ProgressDialog progressDialog;
        private int user_id;
//        Constant constant = new Constant();
//        Preference.getInstance().mSharedPreferences.getBoolean(constant.USER_ID, false);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(context, "", "Loading, Please wait");

            progressDialog.show();
            progressDialog.setCancelable(false);

        }

        @Override
        protected Void doInBackground(Void... voids) {
            logout = new Logout(context);
            logout.executeService();
            return null;

        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {
                if (logout.isSuccess()) {
                    final DadConstant mConstants = new DadConstant();
                    final Preference preference = Preference.getInstance();
                    preference.clearPreferenceData();
                    preference.savePreferenceData(DadConstant.IS_LOGIN, false);
                    preference.savePreferenceData(DadConstant.IS_PIN_CREATED, false);
                    //Utills.displayDialog((Activity) context, context.getString(R.string.app_name), context.getString(R.string.logout), context.getString(android.R.string.ok), "", false, false);
                    replaceFragment(new DadRegistrationFragment(), DadRegistrationFragment.class.getSimpleName());

                } else {
                    DadUtils.displayDialog((Activity) context, context.getString(R.string.app_name), logout.getMessage(), context.getString(android.R.string.ok), "", false, false);
                    Log.d("Logout", logout.getMessage());
                }
            }
        }
    }
}

