package util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aftab.rec.R;


public class ToastMessage {
    Context context;
    private static ToastMessage instance;

    /**
     * @param context
     */
    public ToastMessage(Context context) {
        this.context = context;
    }

    /**
     * @param context
     * @return
     */
    public synchronized static ToastMessage getInstance(Context context) {
        if (instance == null) {
            instance = new ToastMessage(context);
        }
        return instance;
    }

    /**
     * @param message
     */
    public void showLongMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param message
     */
    public void showSmallMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * The Toast displayed via this method will display it for short period of time
     *
     * @param message
     */
    public void showLongCustomToast(String message) {
        View layout = LayoutInflater.from(context). inflate(R.layout.stylish_layout_1,null);
        TextView msgTv = (TextView) layout.findViewById(R.id.msg);
        msgTv.setText(message);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    /**
     * The toast displayed by this class will display it for long period of time
     *
     * @param message
     */
    public void showSmallCustomToast(String message) {
        View layout = LayoutInflater.from(context). inflate(R.layout.stylish_layout_1,null);
        TextView msgTv = (TextView) layout.findViewById(R.id.msg);
        msgTv.setText(message);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

}

