//package net.tigerlight.dad.recievers;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//import net.tigerlight.dad.R;
//import net.tigerlight.dad.registration.fragment.ContactFragment;
//import net.tigerlight.dad.util.BitMapHelper;
//import net.tigerlight.dad.util.Preference;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//
//public class RecieveElementAdapter extends BaseAdapter {
//
//    public interface OnDeleteItemClickListner {
//        void onDeleteItemClick(int position);
//    }
//
//    private int listLength;
//    private Context context;
//    private ArrayList<String> nickNameList;
//    private ArrayList<String> fullNameList;
//    private ArrayList<String> numberList;
//    private ArrayList<String> emailList;
//    private RecievingListScreen recievingListInstance;
//
//    private final String TAG_FIRST_NAME = "firstname";
//    private final String TAG_LAST_NAME = "lastname";
//    private final String TAG_NICKNAME = "nickname";
//    private final String TAG_EMAIL = "email";
//    private final String TAG_PHONE = "phone";
//
//    public RecieveElementAdapter(Context context, JSONArray jsonArray, boolean isDataAvailable) {
//        this.context = context;
//        recievingListInstance = (ContactFragment) context;
//        if (isDataAvailable) {
//            listLength = jsonArray.length();
//        } else {
//            listLength = 0;
//        }
//
//        nickNameList = new ArrayList<String>();
//        fullNameList = new ArrayList<String>();
//        numberList = new ArrayList<String>();
//        emailList = new ArrayList<String>();
//        for (int i = 0; i < listLength; i++) {
//            JSONObject jsonobject;
//            String nickName = null;
//            String fullName = null;
//            String phNumber = null;
//            String email = null;
//            try {
//                jsonobject = (JSONObject) jsonArray.get(i);
//                nickName = jsonobject.getString(TAG_NICKNAME).toString();
//                fullName = jsonobject.getString(TAG_FIRST_NAME).toString() + " " + //
//                        jsonobject.getString(TAG_LAST_NAME).toString();
//                phNumber = jsonobject.getString(TAG_PHONE).toString();
//                email = jsonobject.getString(TAG_EMAIL).toString();
//                nickNameList.add(nickName);
//                fullNameList.add(fullName);
//                numberList.add(phNumber);
//                emailList.add(email);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Override
//    public int getCount() {
//        return listLength;
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return null;
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return 0;
//    }
//
//    @Override
//    public View getView(final int position, View convertView, ViewGroup parent) {
//        LayoutInflater inflater = LayoutInflater.from(context);
//        convertView = inflater.inflate(R.layout.receiving_element_item, null);
//
//        TextView name = ((TextView) convertView.findViewById(R.id.elementName));
//        name.setText("" + nickNameList.get(position));
//        TextView fullName = ((TextView) convertView.findViewById(R.id.elementFullName));
//        fullName.setText("" + fullNameList.get(position));
//        TextView number = ((TextView) convertView.findViewById(R.id.elementNumber));
//        number.setText("" + numberList.get(position));
//        if (recievingListInstance.isEditing()) {
//            number.setText("" + numberList.get(position));
//            convertView.findViewById(R.id.elementDelete).setVisibility(View.VISIBLE);
//            convertView.findViewById(R.id.elementEdit).setVisibility(View.GONE);
//            convertView.findViewById(R.id.elementDelete).setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    if (context instanceof OnDeleteItemClickListner) {
//                        ((OnDeleteItemClickListner) context).onDeleteItemClick(position);
//                    }
//                }
//            });
//        } else {
//            convertView.findViewById(R.id.elementDelete).setVisibility(View.GONE);
//            convertView.findViewById(R.id.elementEdit).setVisibility(View.VISIBLE);
//        }
//
//        Bitmap bitmap = BitMapHelper.loadImageFromStorage(context, "" + emailList.get(position), Preference.getInstance().mSharedPreferences.getString(emailList.get(position), ""));
//        if (bitmap == null) {
//
//            Drawable drawable = context.getResources().getDrawable(R.drawable.pf_pic);
//            ((ImageView) convertView.findViewById(R.id.elementPic)).setBackgroundDrawable(drawable);
//        } else {
//            ((ImageView) convertView.findViewById(R.id.elementPic)).setBackgroundDrawable(new BitmapDrawable(BitMapHelper.getCircleBitmap(bitmap)));
//        }
//
//        return convertView;
//    }
//
//}
