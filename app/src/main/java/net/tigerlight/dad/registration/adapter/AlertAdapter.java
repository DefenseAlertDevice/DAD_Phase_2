package net.tigerlight.dad.registration.adapter;

import static net.tigerlight.dad.util.WsConstants.ASSETS_DOMAIN;

import net.tigerlight.dad.R;
import net.tigerlight.dad.registration.fragment.AlertFragment;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.util.BitMapHelper;
import net.tigerlight.dad.util.Preference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created on 12/11/16.
 */

public class AlertAdapter extends BaseAdapter {


    public interface OnDeleteItemClickListner {
        void onDeleteItemClick(int position);
    }

    public static final String TAG_IMAGE = "image";
    private static final String TAG_DATE_TIME = "datetime";
    private final String TAG_USER_NAME = "username";
    private final String TAG_ADDRESS = "address";
    private final String TAG_ALERT_TYPE = "alertType";
    private final String TAG_latitude = "latitude";
    private final String TAG_longitude = "longitude";
    private final String TAG_EMAIL = "email";
    private Context context;
    private ArrayList<Integer> typeList = new ArrayList();
    private ArrayList<String> addressList = new ArrayList();
    private ArrayList<String> nameList = new ArrayList();
    private ArrayList<String> pathList = new ArrayList();
    private ArrayList<String> dateTimeList = new ArrayList();
    private ArrayList<String> mEmailList = new ArrayList();
    private ArrayList<String> lat = new ArrayList();
    private ArrayList<String> longi = new ArrayList();
    final Preference preference = Preference.getInstance();
    String imgUrl = ASSETS_DOMAIN;


    HashMap<String, Drawable> bmpArray = new HashMap<String, Drawable>();
    private int listLength = 0;
    private AlertFragment alertFragment;
    private JSONObject jsonobject;
    private int i = 0;
    private String mCurrentEmail = "";
    private int mColor = -1;

    public AlertAdapter(Context context, AlertFragment alertFragment, JSONArray jsonArray) {
        this.context = context;
        this.alertFragment = alertFragment;

        mCurrentEmail = preference.mSharedPreferences.getString(Constant.KEY_EMAIL, "");

        for (int i = 0; i < jsonArray.length(); i++) {

            int type;
            String name = null;
            String address = null;
            String imagePath = null;
            String dateTime = null;
            String latitude = null;
            String longitude = null;
            String email = null;

            try {
                jsonobject = (JSONObject) jsonArray.get(i);
                type = jsonobject.optInt(TAG_ALERT_TYPE);
                name = jsonobject.optString(TAG_USER_NAME);
                address = jsonobject.optString(TAG_ADDRESS);
                email = jsonobject.optString(TAG_EMAIL);
                try {
//                    Log.d("hope",new String(jsonobject.optString(TAG_ADDRESS).getBytes("UTF-8"), "UTF-8"));
                    Log.d("hope", new String(jsonobject.optString(TAG_ADDRESS).getBytes("ISO-8859-1"), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                imagePath = jsonobject.optString(TAG_IMAGE);
                String lastWord = imagePath.substring(imagePath.lastIndexOf("/") + 1);;

                imagePath=imgUrl+lastWord;
                dateTime = jsonobject.optString(TAG_DATE_TIME);
                latitude = jsonobject.optString(TAG_latitude);
                longitude = jsonobject.optString(TAG_longitude);

                listLength++;
                typeList.add(type);
                nameList.add(name);
                addressList.add(address);
                pathList.add(imagePath);
                dateTimeList.add(dateTime);
                lat.add(latitude);
                longi.add(longitude);
                mEmailList.add(email);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void remove(int position) {
        listLength -= 1;
        typeList.remove(position);
        addressList.remove(position);
        nameList.remove(position);
        pathList.remove(position);
        dateTimeList.remove(position);
        notifyDataSetChanged();
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

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        i = i + 1;


        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.row_alert_listing, null);
        }


//        String strlst = lat.get(position);
//        String strlog = longi.get(position);
//        String strfin = Utills.getAddress(getActivity(), Double.valueOf(strlst), Double.valueOf(strlog));
//        Log.d("Address", strfin);


        TextView name = ((TextView) convertView.findViewById(R.id.elementNameAlert));
        name.setText(nameList.get(position));
        TextView addressView = ((TextView) convertView.findViewById(R.id.elementAdressAlert));
        String address = addressList.get(position);


        try {
            final String encode = URLEncoder.encode(address, "UTF-8");
            Log.d("encode", encode);
            Log.d("Decode", URLDecoder.decode(address));
            Log.d("Decodeen", URLDecoder.decode(address, "UTF-8"));


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        try {

//            byte[] bytes = address.getBytes("UTF-8"); // Charset to encode into
//
//            System.out.println(">>>>>" + bytes);
//            String s2 = new String(bytes, "UTF-8");
//            System.out.println("decode" + s2);

            addressView.setText(URLDecoder.decode(address, "iso-8859-1"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        byte sByte[] = new byte[0];
        sByte = address.getBytes();
        String Cdata = new String(sByte);
        System.out.println(">>>>>" + Cdata);



        String orgstr = "Flensborggade 40, 1669 K\u00D8benhavn V, Denmark && H\u00D4tel de Ville, 75004 Paris, France";
//        String encodedStr = URLEncoder.encode(orgstr);
        String decodedStr = URLDecoder.decode(orgstr);
//        Log.d("EncodedStr", encodedStr);
        Log.d("DEcodedStr", decodedStr);


//        addressView.setText(filterToUTF8(address));


//        System.out.println("xxxxxxxxxxxxxxxx : " + new String(Charset.defaultCharset().encode(address).array()));


        // Charset with which bytes were encoded


//        String addresss = Utills.fixEncoding(addressList.get(position));
//        Log.d("addresss", addresss);


//            byte[] bytes = address.getBytes("UTF-8");
//            String text = new String(bytes, "UTF-8");
//            Log.d("text", text);
//            addressView.setText(Html.fromHtml(text));

//        if (strfin != null) {
//            addressView.setText(strfin);
//        } else {
//            addressView.setText(address);
//        }


//        for (int i = 0; i < address.length(); i++) {
//            String resp = String.valueOf(address.charAt(i));
//            if (resp.equals("\ufffd")) {
//                String respp = address.replace("\ufffd", "\u00f4");
//                Log.d("address", respp);
//                addressView.setText(respp);
//                break;
//
//            } else {
//                addressView.setText(address);
//            }
//
//        }
//        addressView.setText("Hôtel de Ville, 75004 Paris");


//        addressView.setText(Html.fromHtml(addressList.get(position).toString()));

//        addressView.setText(addressList.get(position));

//        String orgStr = deAccent(addressList.get(position));
//        Log.d("orgstr",orgStr);

//        String[] strArr = new String[addressList.get(position).length()];
//        try {
//            Utills.speacialChar(strArr);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        String namee = "";
//        try {
//            namee = new String(addressList.get(position).getBytes("ISO-8859-1"), "UTF-8");
//            Log.d("Encode",namee);
//        } catch (UnsupportedEncodingException e) {
//
//            e.printStackTrace();
//        }
//
//        String decodedName = Html.fromHtml(namee).toString();
//        Log.d("Decode",decodedName);


//        try {
//            String str=URLEncoder.encode(addressList.get(position), "UTF8");
//            Log.d("Spac",str);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        preference.savePreferenceData("alert_count", i);


        TextView dateTimeView = ((TextView) convertView.findViewById(R.id.elementTimeAlert));
        dateTimeView.setText(dateTimeList.get(position));

        ImageView ivPickAlert = (ImageView) convertView.findViewById(R.id.elementPicAlert);

        // if (AlertFragment.isEditing) {
        convertView.findViewById(R.id.btnDeleteAlert).setVisibility(View.GONE);
        convertView.findViewById(R.id.btnEditAlert).setVisibility(View.GONE);
        convertView.findViewById(R.id.btnDeleteAlert).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (alertFragment != null) {
                    //alertFragment.onDeleteItemClick(position);
                }
            }
        });
        //    }
//    else {
//            convertView.findViewById(R.id.btnDeleteAlert).setVisibility(View.VISIBLE);
//            convertView.findViewById(R.id.btnEditAlert).setVisibility(View.GONE);
//        }

        ((ImageView) convertView.findViewById(R.id.elementPicAlert)).setBackgroundDrawable(context.getResources().getDrawable(R.drawable.pf_pic));
//        Glide.with(context)
//                .load(pathList.get(position)).transform(new CircleTransform(context)) // Uri of the picture
//                .placeholder(R.drawable.pf_pic)
//                .into(ivPickAlert);
        setImageInThread((ImageView) convertView.findViewById(R.id.elementPicAlert), pathList.get(position), position);

        switch (typeList.get(position))
        {
            case 0:
            {
                mColor = (mEmailList.get(position).equals(mCurrentEmail)) ? R.color.color_alert_purple : R.color.color_alert_orange;
                break;
            }
            case 1:
            {
                mColor = R.color.color_alert_red;
                break;
            }
            case 2:
            {
                mColor = R.color.color_alert_green;
                break;
            }
            default:
            {
                mColor = R.color.color_alert_blue;
                break;
            }
        }

        name.setTextColor(ContextCompat.getColor(context, mColor));
        addressView.setTextColor(ContextCompat.getColor(context, mColor));
        dateTimeView.setTextColor(ContextCompat.getColor(context, mColor));

        return convertView;
    }

    @SuppressLint("HandlerLeak")
    public void setImageInThread(final ImageView imageView, final String url, final int position) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Drawable result = (Drawable) message.obj;
                imageView.setBackgroundDrawable(result);
            }
        };

        new Thread() {
            @Override
            public void run() {
                if (bmpArray.get(pathList.get(position)) != null) {
                    Message message = handler.obtainMessage(1, bmpArray.get(pathList.get(position)));
                    handler.sendMessage(message);
                    return;
                }
                Drawable drawable = getDrawable(url);
                Message message = handler.obtainMessage(1, drawable);
                handler.sendMessage(message);
            }

        }.start();
    }

    private Drawable getDrawable(String url) {
        Bitmap mIcon11 = null;
        try {
            InputStream in = new URL(url).openConnection().getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            mIcon11 = BitmapFactory.decodeStream(in, null, options);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
            return context.getResources().getDrawable(R.drawable.pf_pic);
        }
        if (mIcon11 == null) {
            return context.getResources().getDrawable(R.drawable.pf_pic);
        }
        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), BitMapHelper.getCircleBitmap(mIcon11));
        bmpArray.put(url, bitmapDrawable);
        return bitmapDrawable;
    }


    public static String convertToUTF8(String theXML) {
        try {
// Convert from Unicode to UTF-8
            byte[] utf8 = theXML.getBytes("UTF-8");
            return new String(utf8, "UTF-8");
        } catch (UnsupportedEncodingException e) {
// TODO: handle error better
            Log.d("error", "error converting string to UTF-8");
            return "";
        }
    }

}
