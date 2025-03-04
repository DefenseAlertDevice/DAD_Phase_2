package net.tigerlight.dad.registration.adapter;

import static net.tigerlight.dad.util.WsConstants.ASSETS_DOMAIN;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.tigerlight.dad.R;
import net.tigerlight.dad.util.BitMapHelper;
import net.tigerlight.dad.util.CircleTransform;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class RecieveElementAdapter extends BaseAdapter {


    public interface OnDeleteItemClickListner {
        void onDeleteItemClick(int position);
    }

    private int listLength;
    private Context context;
    private ArrayList<String> nickNameList;
    private ArrayList<String> fullNameList;
    private ArrayList<String> numberList;
    private ArrayList<String> emailList;
    private ArrayList<String> addressList;
    private ArrayList<String> userIdList;
    private OnDeleteItemClickListner contactFragment;
    private final String TAG_FIRST_NAME = "firstname";
    private final String TAG_LAST_NAME = "lastname";
    private final String TAG_NICKNAME = "nickname";
    private final String TAG_EMAIL = "email";
    private final String TAG_PHONE = "phone";
    private final String TAG_ADDRESS = "address";
    private final String TAG_USERID = "userid";


    public RecieveElementAdapter(Context context, OnDeleteItemClickListner contactFragment, JSONArray jsonArray, boolean isDataAvailable) {
        this.context = context;
        this.contactFragment = contactFragment;
        if (isDataAvailable) {
            listLength = jsonArray.length();
        } else {
            listLength = 0;
        }

        nickNameList = new ArrayList<>();
        fullNameList = new ArrayList<>();
        numberList = new ArrayList<>();
        emailList = new ArrayList<>();
        addressList = new ArrayList<>();
        userIdList = new ArrayList<>();
        for (int i = 0; i < listLength; i++) {
            JSONObject jsonobject;
            String nickName = null;
            String fullName = null;
            String phNumber = null;
            String email = null;
            String address = null;
            String userId = null;
            try {
                jsonobject = (JSONObject) jsonArray.get(i);
                userId = jsonobject.optString(TAG_USERID);
                nickName = jsonobject.optString(TAG_NICKNAME);
                fullName = jsonobject.optString(TAG_FIRST_NAME) + " " + //
                        jsonobject.optString(TAG_LAST_NAME);
                phNumber = jsonobject.optString(TAG_PHONE);
                email = jsonobject.optString(TAG_EMAIL);
                address = jsonobject.optString(TAG_ADDRESS);
                nickNameList.add(nickName);
                fullNameList.add(fullName);
                numberList.add(phNumber);
                emailList.add(email);
                addressList.add(address);
                userIdList.add(userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCount() {
        return listLength;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.receiving_element_item, null);

        ImageView ivProfilePic = (ImageView) convertView.findViewById(R.id.elementPic);
        TextView name = ((TextView) convertView.findViewById(R.id.elementName));
        name.setText("" + nickNameList.get(position));
        TextView fullName = ((TextView) convertView.findViewById(R.id.elementFullName));
        fullName.setText("" + fullNameList.get(position));
        TextView number = ((TextView) convertView.findViewById(R.id.elementNumber));
        number.setText("" + numberList.get(position));
        //if (contactFragment.isEditing()) {
        //number.setText("" + numberList.get(position));
        convertView.findViewById(R.id.elementDelete).setVisibility(View.GONE);
        convertView.findViewById(R.id.elementEdit).setVisibility(View.GONE);

        setProfilePicture(convertView.findViewById(R.id.elementPic), userIdList.get(position));
//
//        if (bitmap == null) {
//            ((ImageView) convertView.findViewById(R.id.elementPic)).setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pf_pic));
//        } else {
//            Bitmap circledBitmap = createScaleddBitmapFromFile(bitmap);
//            ((ImageView) convertView.findViewById(R.id.elementPic)).setImageDrawable(new BitmapDrawable(circledBitmap));
//        }

        return convertView;
    }

    private void setProfilePicture(ImageView view, String userId) {
        if (userId != null && !userId.isEmpty()) {
            String url = String.format("%scontact_image_%s.png", ASSETS_DOMAIN, userId);
            Glide.with(context)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).transform(new CircleTransform(context))
                    .placeholder(R.drawable.pf_pic)
                    .into(view);
        } else {
            Glide.with(context)
                    .load(R.drawable.pf_pic)
                    .into(view);
        }
    }

    private Bitmap createScaleddBitmapFromFile(Bitmap bitmap) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
        Bitmap croppedBitmap = BitMapHelper.getCircleBitmap(scaledBitmap);
        return croppedBitmap;
    }

}
