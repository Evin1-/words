package mx.evin.apps.words.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mx.evin.apps.words.MainActivity;
import mx.evin.apps.words.R;
import mx.evin.apps.words.WebActivity;
import mx.evin.apps.words.model.entities.parse.Term;
import mx.evin.apps.words.view.fragments.SearchTermFragment;
import mx.evin.apps.words.view.fragments.SearchTermVoiceFragment;
import mx.evin.apps.words.viewmodel.utils.Constants;
import mx.evin.apps.words.viewmodel.utils.MyTagHandler;

/**
 * Created by evin on 1/10/16.
 */
public class MainVM {
    //TODO Offline mode first
    //TODO Check that 2 offline calls are not at the same time
    private static final String TAG_ = "MainVMTAG_";
    public static ArrayList<Term> mTerms;
    public static Term mCurrentTerm;
    public static Context mCurrentContext;

    static {
        mTerms = new ArrayList<>();
    }

    public static void initializeMain() {
        initializeTerms();
    }

    private static void initializeTerms() {
        SearchTermFragment.mTerms = mTerms;
        SearchTermVoiceFragment.mTerms = mTerms;

        ParseQuery<ParseObject> query = new ParseQuery<>("Term");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject term : objects) {
                        SearchTermFragment.mAdapter.notifyDataSetChanged();
                        SearchTermVoiceFragment.mAdapter.notifyDataSetChanged();
                        term.pinInBackground();
                        mTerms.add((Term) term);
                        term.getParseObject("pack").fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject object, ParseException e) {
                                object.pinInBackground();
                                SearchTermFragment.mAdapter.notifyDataSetChanged();
                                SearchTermVoiceFragment.mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    SearchTermFragment.mAdapter.notifyDataSetChanged();
                    SearchTermVoiceFragment.mAdapter.notifyDataSetChanged();
//                    Log.d(TAG_, Integer.toString(mTerms.size()));
                } else {
                    Log.d(TAG_, "Error retrieving terms + " + e.toString());
                }
            }
        });
    }

    public static ArrayList<Term> getTerms() {
        return mTerms;
    }

    public static void refreshMainFragment(Activity activity) {
        //TODO Update hierarchy, related terms and blablabal
        //TODO Remove try/catch and use a better practice
        TextView textViewDoc = (TextView) activity.findViewById(R.id.f_main_doc_txt);
        TextView textViewPack = (TextView) activity.findViewById(R.id.f_main_pack_txt);
        TextView textViewTitle = (TextView) activity.findViewById(R.id.f_main_title_txt);
        TextView textViewHierarchy = (TextView) activity.findViewById(R.id.f_main_hierarchy_txt);
        TextView textURL = (TextView) activity.findViewById(R.id.f_main_url_txt);

        MainActivity mainActivity = (MainActivity) activity;
        ActionBar actionBar = mainActivity.getSupportActionBar();
        if (actionBar != null)
            actionBar.setSubtitle(MainActivity.mTechnology + " | " + mCurrentTerm.getWords());

        textViewDoc.setText(setTextViewHTML(mCurrentTerm.getDocs(), Constants.TYPE_HTML.BODY));
        textViewDoc.setLinksClickable(true);
        textViewDoc.setMovementMethod(LinkMovementMethod.getInstance());

        textViewHierarchy.setText(setTextViewHTML(mCurrentTerm.getHierarchy(), Constants.TYPE_HTML.HIERARCHY));
        textViewHierarchy.setLinksClickable(true);
        textViewHierarchy.setMovementMethod(LinkMovementMethod.getInstance());

        try {
            textViewPack.setText(mCurrentTerm.getPack().getName());
        } catch (Exception e) {
            textViewPack.setText(activity.getString(R.string.f__package_placeholder));
        }

        textViewTitle.setText(mCurrentTerm.getWords());
        textURL.setText(mCurrentTerm.getUrl());

    }

    protected static Spanned setTextViewHTML(String html, Constants.TYPE_HTML type_html) {
        CharSequence sequence = preFormatHTML(html, type_html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        return strBuilder;
    }

    private static CharSequence preFormatHTML(String html, Constants.TYPE_HTML type_html) {
        if (html == null || html.length() < 1){
            return "";
        }

        if (type_html == Constants.TYPE_HTML.HIERARCHY){
            html = html.replace("</tr>\n    \n\n    <tr>", "<br>");
        }

        return Html.fromHtml(html, null, new MyTagHandler());
    }

    protected static void makeLinkClickable(final SpannableStringBuilder strBuilder, final URLSpan span) {
        final int start = strBuilder.getSpanStart(span);
        final int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                char[] aux = new char[end - start];
                strBuilder.getChars(start, end, aux, 0);
                Log.d(TAG_, new String(aux));
                Log.d(TAG_, span.getURL());
                refreshCurrentTermByName(new String(aux), mCurrentContext, span.getURL());
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    public static void refreshCurrentTermById(final String lastTermId, final Context context) {
        mCurrentContext = context;
        ParseObject query = ParseObject.createWithoutData("Term", lastTermId);
        query.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    mCurrentTerm = (Term) object;
                    refreshMainFragment((Activity) mCurrentContext);
                } else {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Term");
                    query.getInBackground(lastTermId, new GetCallback<ParseObject>() {
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                object.pinInBackground();
                                mCurrentTerm = (Term) object;
                                refreshMainFragment((Activity) mCurrentContext);
                            }
                        }
                    });

                }
            }
        });
    }

    public static void refreshCurrentTermByName(final String lastTermWords, final Context context, final String url) {
        //TODO What if it finds 2 objects
        mCurrentContext = context;
        ParseQuery<ParseObject> query = new ParseQuery<>("Term");
        query.fromLocalDatastore();
        query.whereEqualTo("words", lastTermWords);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null ){
                    mCurrentTerm = (Term) object;
                    refreshMainFragment((Activity) mCurrentContext);
                } else {
                    ParseQuery<ParseObject> query = new ParseQuery<>("Term");
                    query.whereEqualTo("words", lastTermWords);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            if (e == null){
                                object.pinInBackground();
                                mCurrentTerm = (Term) object;
                                refreshMainFragment((Activity) mCurrentContext);
                            } else {
                                try {
                                    URL auxURL = new URL(mCurrentTerm.getUrl());
                                    String buildURL = auxURL.getProtocol() + "://" + auxURL.getHost() + "/" + url;

                                    Intent intent = new Intent(mCurrentContext, WebActivity.class);
                                    intent.putExtra(Constants.TITLE_WEB_KEY, lastTermWords);
                                    intent.putExtra(Constants.URL_WEB_KEY, buildURL);
                                    mCurrentContext.startActivity(intent);
                                } catch (MalformedURLException e1) {
                                    Log.e(TAG_, e1.toString());
                                }
                            }
                        }
                    });

                }
            }
        });
    }
}
