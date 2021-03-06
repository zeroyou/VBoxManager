
package com.kedzie.vbox.app;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kedzie.vbox.BuildConfig;
import com.kedzie.vbox.R;
import com.kedzie.vbox.api.IMachine;
import com.kedzie.vbox.api.IMedium;

/**
 * Android Utilities
 * 
 * @apiviz.stereotype utility
 */
public class Utils {
	private static final String TAG = "Utils";

	/**
	 * Provides default implementations for {@link AnimationListener}
	 */
	public static class AnimationAdapter implements AnimationListener {
		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	}

	/**
	 * Provides default implementations for {@link TextWatcher}
	 */
	public static abstract class TextAdapter implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable editable) {
		}
	}

	/**
	 * Provides default implementations for {@link OnItemSelectedListener}
	 */
	public static abstract class OnItemSelectedAdapter implements OnItemSelectedListener {
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	public static boolean isIceCreamSandwhich() {
		return isVersion(Build.VERSION_CODES.ICE_CREAM_SANDWICH);
	}

	public static boolean isJellyBean() {
		return isVersion(Build.VERSION_CODES.JELLY_BEAN);
	}

	/**
	 * Check if API level satisfies the minimum API level
	 * 
	 * @param versionCode minimum API version code
	 * @return <code>true</code> if API level matches
	 */
	public static boolean isVersion(int versionCode) {
		return Build.VERSION.SDK_INT >= versionCode;
	}

	/**
	 * Get integer value from default shared preferences
	 * 
	 * @param context the context
	 * @param name preference key
	 * @return The integer value
	 */
	public static int getIntPreference(Context context, String name) {
		return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(name, "0"));
	}

	/**
	 * Get boolean value from default shared preferences
	 * 
	 * @param context the context
	 * @param name preference key
	 * @return The boolean value
	 */
	public static boolean getBooleanPreference(Context context, String name) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, false);
	}

	/**
	 * Get string value from default shared preferences
	 * 
	 * @param context the context
	 * @param name preference key
	 * @return The string value
	 */
	public static String getStringPreference(Context context, String name) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(name, "");
	}

	/**
	 * Show {@link Toast} long notification with {@link String#format}
	 * 
	 * @param context message {@link Context}
	 * @param message Message to show
	 * @param params String formatting parameters
	 */
	public static void toastLong(Context context, String message, Object... params) {
		toast(context, Toast.LENGTH_LONG, message, params);
	}

	/**
	 * Show {@link Toast} short notification
	 * 
	 * @param context message {@link Context}
	 * @param message Message to show
	 * @param params String formatting parameters
	 */
	public static void toastShort(Context context, String message, Object... params) {
		toast(context, Toast.LENGTH_SHORT, message, params);
	}

	private static void toast(Context context, int length, String message, Object... params) {
		Toast.makeText(context, isEmpty(params) ? message : String.format(message, params), length).show();
	}

	/**
	 * Check if String is <code>null</code> or empty string
	 * 
	 * @param s the string
	 * @return <code>true</code> if empty string or null, <code>false</code>
	 *         otherwise
	 */
	public static boolean isEmpty(String s) {
		return s == null || s.equals("");
	}

	/**
	 * Check if array is <code>null</code> or empty
	 * 
	 * @param array the array
	 * @return <code>true</code> if empty or null, <code>false</code> otherwise
	 */
	public static boolean isEmpty(Object[] array) {
		return array == null || array.length == 0;
	}

	/**
	 * Check if list is <code>null</code> or empty
	 * 
	 * @param list the array
	 * @return <code>true</code> if empty or null, <code>false</code> otherwise
	 */
	public static boolean isEmpty(List<?> list) {
		return list == null || list.isEmpty();
	}

	/**
	 * Make a nice multiline printout of map contents.
	 * 
	 * @param title title to show above contents
	 * @param map the data
	 * @return string representation
	 */
	public static String toString(String title, Map<?, ?> map) {
		StringBuffer buf = new StringBuffer(title).append("\n=========================\n{");
		for (Map.Entry<?, ?> entry : map.entrySet())
			buf.append("\n").append(entry.getKey()).append(" ==> ").append(entry.getValue());
		return buf.append("}").toString();
	}

	/**
	 * Append an element to a comma separated string
	 * 
	 * @param base base string
	 * @param append appended element
	 * @return base string with appended element, with comma if needed
	 */
	public static StringBuffer appendWithComma(StringBuffer base, String append) {
		if (base.length() > 0)
			base.append(", ");
		return base.append(append);
	}

	/**
	 * Get type generic type parameter
	 * @param genericType the generic {@link Type}
	 * @param index parameter index
	 * @return type parameter
	 */
	public static Class<?> getTypeParameter(Type genericType, int index) {
		if (!(genericType instanceof ParameterizedType))
			return null;
		return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[index];
	}

	/**
	 * Search an array for a specific type of annotation
	 * 
	 * @param clazz the class
	 * @param a type of annotation
	 * @return the annotation or <code>null</code> if not found
	 */
	public static <T extends Annotation> T getAnnotation(Class<T> clazz, Annotation[] annotations) {
		for (Annotation a : annotations)
			if (a.annotationType().equals(clazz))
				return clazz.cast(a);
		return null;
	}

	/**
	 * Create an {@link IntentFilter} for multiple actions
	 * 
	 * @param actions accepted actions
	 * @return the {@link IntentFilter}
	 */
	public static IntentFilter createIntentFilter(String... actions) {
		IntentFilter filter = new IntentFilter();
		for (String action : actions)
			filter.addAction(action);
		return filter;
	}

	/**
	 * Convert DPI to pixels
	 * 
	 * @param dpi dpi value
	 * @return equivilent pixel value
	 */
	public static int dpiToPx(Context context, int dpi) {
		return (int) (context.getResources().getDisplayMetrics().density * dpi + .5f);
	}

	/**
	 * Specify fragment transition animations
	 * 
	 * @param tx the transaction
	 * @return the transaction
	 */
	public static FragmentTransaction setCustomAnimations(FragmentTransaction tx) {
		return tx.setCustomAnimations(com.kedzie.vbox.R.animator.flip_left_in, com.kedzie.vbox.R.animator.flip_left_out,
				com.kedzie.vbox.R.animator.flip_right_in, com.kedzie.vbox.R.animator.flip_right_out);
	}

	/**
	 * Show a DialogFragment with Back Stack
	 * 
	 * @param manager {@link FragmentManager}
	 * @param tag tag for {@link Fragment}
	 * @param dialog the {@link DialogFragment} implementation
	 */
	public static void showDialog(FragmentManager manager, String tag, DialogFragment dialog) {
		FragmentTransaction tx = manager.beginTransaction().addToBackStack(null);
		Fragment prev = manager.findFragmentByTag(tag);
		if (prev != null)
			tx.remove(prev);
		dialog.show(tx, tag);
	}

	/**
	 * Detach an fragment
	 * 
	 * @param manager fragment manager
	 * @param tx existing transaction
	 * @param tag tag of existing fragment
	 */
	public static void detachFragment(FragmentManager manager, FragmentTransaction tx, String tag) {
		Fragment existing = manager.findFragmentByTag(tag);
		if (existing != null)
			tx.detach(existing);
	}

	/**
	 * Detach an fragment
	 * 
	 * @param manager fragment manager
	 * @param tx existing transaction
	 * @param id container id
	 */
	public static void detachFragment(FragmentManager manager, FragmentTransaction tx, int id) {
		Fragment existing = manager.findFragmentById(id);
		if (existing != null)
			tx.detach(existing);
	}

	/**
	 * Add new or attach existing Fragment
	 * 
	 * @param context context
	 * @param manager fragment manager
	 * @param containerId container
	 * @param info fragment definition
	 */
	public static void addOrAttachFragment(Context context, FragmentManager manager, int containerId, FragmentElement element) {
		FragmentTransaction tx = manager.beginTransaction();
		addOrAttachFragment(context, manager, tx, containerId, element);
		tx.commit();
	}

	/**
	 * Instantiate new or attach existing Fragment
	 * 
	 * @param context context
	 * @param manager fragment manager
	 * @param tx existing transaction
	 * @param containerId container
	 * @param info fragment definition
	 */
	public static void addOrAttachFragment(Context context, FragmentManager manager, FragmentTransaction tx, int containerId, FragmentElement element) {
		if (element.fragment == null)
			element.fragment = manager.findFragmentByTag(element.name);
		if (element.fragment == null) {
			Log.v("FragmentManager", "Instantiated new Fragment: " + element.name);
			tx.add(containerId, element.instantiate(context), element.name);
		} else {
			Log.v("FragmentManager", "Reattaching existing Fragment: " + element.name);
			tx.attach(element.fragment);
		}
	}

	/**
	 * Remove existing fragment with same tag and add new one.
	 *
	 * @param context context
	 * @param manager fragment manager
	 * @param containerId container
	 * @param info fragment definition
	 */
	public static void replaceFragment(Context context, FragmentManager manager, int containerId, FragmentElement element) {
		FragmentTransaction tx = manager.beginTransaction();
		replaceFragment(context, manager, tx, containerId, element);
		tx.commit();
	}

	/**
	 * Remove existing fragment with same tag and add new one.
	 *
	 * @param context context
	 * @param manager fragment manager
	 * @param tx existing transaction
	 * @param containerId container
	 * @param element fragment definition
	 */
	public static void replaceFragment(Context context, FragmentManager manager, FragmentTransaction tx, int containerId, FragmentElement element) {
		Fragment existing = manager.findFragmentByTag(element.name);
		if (existing != null)
			tx.remove(existing);
		Log.d("FragmentManager", "Instantiated new Fragment: " + element.name);
		tx.add(containerId, element.instantiate(context), element.name);
	}

	/**
	 * Set the contents of a {@link TextView}
	 * 
	 * @param parent parent view
	 * @param id textview id
	 * @param text text contents
	 */
	public static void setTextView(View parent, int id, String text) {
		((TextView) parent.findViewById(id)).setText(text);
	}

	/**
	 * Set the contents of a {@link TextView}
	 * 
	 * @param parent parent view
	 * @param id textview id
	 * @param text text contents
	 */
	public static void setTextView(View parent, int id, int text) {
        setTextView(parent, id, text + "");
	}

	/**
	 * Set the contents of a {@link TextView}
	 * 
	 * @param holder view holder
	 * @param id textview id
	 * @param text text contents
	 */
	public static void setTextView(SparseArray<View> holder, int id, String text) {
		((TextView) holder.get(id)).setText(text);
	}

	/**
	 * Set the contents of a {@link TextView}
	 * 
	 * @param holder view holder
	 * @param id textview id
	 * @param text text contents
	 */
	public static void setTextView(SparseArray<View> holder, int id, int text) {
        setTextView(holder, id, text+"");
	}

	/**
	 * Set the contents of a {@link ImageView}
	 * 
	 * @param parent parent view
	 * @param id textview id
	 * @param image contents
	 */
	public static void setImageView(View parent, int id, Drawable image) {
		((ImageView) parent.findViewById(id)).setImageDrawable(image);
	}

	/**
	 * Set the contents of a {@link ImageView}
	 * 
	 * @param parent parent view
	 * @param id textview id
	 * @param image contents
	 */
	public static void setImageView(View parent, int id, Bitmap image) {
		((ImageView) parent.findViewById(id)).setImageBitmap(image);
	}

	/**
	 * Perform a {@link Thread#sleep} and ignore any
	 * {@link InterruptedException}
	 */
	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Get screen layout size
	 * 
	 * @param config the {@link Configuration}
	 * @return screen size
	 */
	public static int getScreenSize(Configuration config) {
		return config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
	}

	/**
	 * Find an element within an array
	 * 
	 * @param array the array
	 * @param object the object to find
	 * @return the index of the object in array, or -1 if not found
	 */
	public static int indexOf(Object[] array, Object object) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(object))
				return i;
		}
		return -1;
	}

	/**
	 * Remove "NULL" element of a <code>enum</code> array
	 * 
	 * @param array array of <code>enum.values()</code>
	 * @return array with the "NULL" element removed
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] removeNull(T[] array) {
		T[] ret = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
		int retIndex = 0;
		for (int i = 0; i < array.length; i++) {
			if (!array[i].toString().equals("Null"))
				ret[retIndex++] = array[i];
		}
		return ret;
	}

	/**
	 * Cache commonly used Medium properties
	 * 
	 * @param m
	 */
	public static void cacheProperties(IMedium medium) {
		synchronized (medium) {
			medium.getName();
			medium.getDescription();
			medium.getSize();
			medium.getType();
			medium.getLocation();
			medium.getLogicalSize();
			medium.getBase().getName();
			medium.getBase().getSize();
			medium.getBase().getType();
			medium.getBase().getLocation();
			medium.getBase().getLogicalSize();
		}
	}

    /**
     * Cache commonly used Machine properties
     *
     * @param machine
     */
    public static void cacheProperties(IMachine machine) {
        synchronized (machine) {
            machine.clearCacheNamed("getName", "getState", "getCurrentStateModified", "gotOSTypeId", "getCurrentSnapshot");
            machine.getName();
            machine.getState();
            machine.getCurrentStateModified();
            machine.getOSTypeId();
            if (machine.getCurrentSnapshot() != null)
                machine.getCurrentSnapshot().getName();
        }
    }

	/**
	 * Launch activity using custom animations. Uses ActivityOptions if on
	 * JellyBean, otherwise overrides transition
	 * 
	 * @param parent parent activity
	 * @param intent intent to launch
	 */
	public static void startActivity(Activity parent, Intent intent) {
		startActivity(parent, intent, R.anim.slide_in_right, R.anim.slide_out_left);
	}

	/**
	 * Launch activity using custom animations. Uses ActivityOptions if on
	 * JellyBean, otherwise overrides transition
	 * 
	 * @param parent parent activity
	 * @param intent intent to launch
	 * @param animIn In animation
	 * @param animOut Out animation
	 */
	public static void startActivity(Activity parent, Intent intent, int animIn, int animOut) {
		if (isJellyBean())
			parent.startActivity(intent, ActivityOptions.makeCustomAnimation(parent, animIn, animOut).toBundle());
		else {
			parent.startActivity(intent);
			parent.overridePendingTransition(animIn, animOut);
		}
	}

	/**
	 * Launch activity using custom animations. Uses ActivityOptions if on
	 * JellyBean, otherwise overrides transition
	 * 
	 * @param parent parent activity
	 * @param intent intent to launch
	 */
	public static void startActivityForResult(Activity parent, Intent intent, int requestCode) {
		startActivityForResult(parent, intent, requestCode, R.anim.slide_in_right, R.anim.slide_out_left);
	}

	/**
	 * Launch activity using custom animations. Uses ActivityOptions if on
	 * JellyBean, otherwise overrides transition
	 * 
	 * @param parent parent activity
	 * @param intent intent to launch
	 * @param animIn In animation
	 * @param animOut Out animation
	 */
	public static void startActivityForResult(Activity parent, Intent intent, int requestCode, int animIn, int animOut) {
		if (isJellyBean())
			parent.startActivityForResult(intent, requestCode, ActivityOptions.makeCustomAnimation(parent, animIn, animOut).toBundle());
		else {
			parent.startActivityForResult(intent, requestCode);
			parent.overridePendingTransition(animIn, animOut);
		}
	}

	/**
	 * Override transition for activity closing.
	 * 
	 * @param activity the activity
	 */
	public static void overrideBackTransition(Activity activity) {
		overrideBackTransition(activity, R.anim.slide_in_left, R.anim.slide_out_right);
	}

	/**
	 * Override transition for activity closing.
	 * 
	 * @param activity the activity
	 */
	public static void overrideBackTransition(Activity activity, int inAnim, int outAnim) {
		activity.overridePendingTransition(inAnim, outAnim);
	}

	/**
	 * Scale a bitmap to fit within the desired size
	 * 
	 * @param bitmap input bitmap
	 * @param width desired width
	 * @param height desired height
	 * @return scaled bitmap which will fit in desired size
	 */
	public static Bitmap scale(Bitmap bitmap, int width, int height) {
		int bWidth = bitmap.getWidth(), bHeight = bitmap.getHeight();
		if (bWidth <= width && bHeight <= height)
			return bitmap;
		if (BuildConfig.DEBUG)
			Log.v(TAG, String.format("Scaling bitmap (%1$dx%2$d) --> (%3$dx%4$d)", bWidth, bHeight, width, height));
		float wScale = ((float) width) / bWidth;
		float hScale = ((float) height) / bHeight;
		float scale = Math.min(wScale, hScale);
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		if (BuildConfig.DEBUG)
			Log.v(TAG, "Scale factor: " + scale);
		return Bitmap.createBitmap(bitmap, 0, 0, bWidth, bHeight, matrix, true);
	}

    public static Point mapPoint(View view, Point point) {
        Point mapped = new Point(point.x, point.y);
        Matrix matrix = view.getMatrix();
        if(!matrix.isIdentity()) {
            Matrix inverse = new Matrix();
            matrix.invert(inverse);
            float []n = { point.x, point.y };
            matrix.mapPoints(n);
            mapped.x= (int) n[0];
            mapped.y= (int) n[1];
        }
        mapped.offset(-view.getLeft(), -view.getTop());
        return mapped;
    }

    /**
     *
     * @param view    View to search
     * @param p     point in view's coordinate space
     * @return  deepest view
     */
    public static View getDeepestView(View view, Point p) {
        Rect hit = new Rect();
        if(view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup)view;
            for(int i=0; i<parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                child.getHitRect(hit);
                if(hit.contains(p.x, p.y)) {
                    return getDeepestView(child, mapPoint(child, p));
                }
            }
        }
        return view;
    }

    public static <T> T getDeepestView(View view, Point p, Class<T> clazz) {
        View deepest = getDeepestView(view, p);
        while(deepest!=null && !(clazz.isAssignableFrom(deepest.getClass()))) {
            if(deepest.getParent() instanceof View)
                deepest = (View)deepest.getParent();
            else
                deepest=null;
        }
        return clazz.cast(deepest);
    }
}
