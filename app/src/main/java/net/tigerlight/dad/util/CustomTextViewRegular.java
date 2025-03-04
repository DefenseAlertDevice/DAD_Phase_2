package net.tigerlight.dad.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import net.tigerlight.dad.R;


public class CustomTextViewRegular extends TextView {

    public CustomTextViewRegular(Context context) {
        super(context);
    }

    public CustomTextViewRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public CustomTextViewRegular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.customTextView);
        String font = a.getString(R.styleable.customTextView_font_name);
        if (font != null) {
            setCustomFont(font, context);
        } else {
            setCustomFont(context.getString(R.string.font_regular), context);
        }
        a.recycle();
    }

    /**
     * Sets a font on a textView
     *
     * @param font cutom font
     * @param context context
     */
    private void setCustomFont(String font, Context context) {
        if (font == null) {
            return;
        }
        Typeface tf = FontCache.get(font, context);
        if (tf != null) {
            setTypeface(tf);
        }
    }
}
