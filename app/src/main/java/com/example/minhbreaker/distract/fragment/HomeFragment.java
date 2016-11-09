package com.example.minhbreaker.distract.fragment;

import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;

import com.example.minhbreaker.distract.R;
import com.example.minhbreaker.distract.other.DateColor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private SpeedometerGauge speedometer;
    UsageStatsManager mUsageStatsManager;
    List<UsageStats> queryUsageStats;
    PackageManager packageManager;
    String report;
    final String RED = "#ff0000";
    final String YELLOW = "#ffff00";
    final String GREEN = "#00ff00";
    final String WHITE = "#ffffff";
    static ArrayList<DateColor> dateColorArrayList;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);

    }

    @Override
    public void onStart() {
        double healthyMax=NotificationsFragment.goal;
        double unhealthyMin=NotificationsFragment.unhealthy;
        if (healthyMax==0) healthyMax=2;
        if (unhealthyMin<=healthyMax) unhealthyMin=healthyMax+1;
        super.onStart();
        speedometer = (SpeedometerGauge) getView().findViewById(R.id.speedometer);
        speedometer.setMaxSpeed(50);
        speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        speedometer.setMaxSpeed(unhealthyMin+1);
        speedometer.setMajorTickStep(5);
        speedometer.setMinorTicks(4);
        speedometer.addColoredRange(0, healthyMax, Color.GREEN);
        speedometer.addColoredRange(healthyMax, unhealthyMin, Color.YELLOW);
        speedometer.addColoredRange(unhealthyMin, unhealthyMin+1, Color.RED);

        double currentUsage = 2.3;
        speedometer.setSpeed(currentUsage,true);
        if (currentUsage >= healthyMax){
            int mNotificationId = 001;
            NotificationManager mNotifyMgr =
                    (NotificationManager) getContext().getSystemService(getContext().NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder;
            if (currentUsage < unhealthyMin) {
                mBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                                .setContentTitle("Healthy usage limit exceeded")
                                .setContentText("Please reduce usage of your phone");
            } else {
                mBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                                .setContentTitle("Phone usage reached an unhealthy range")
                                .setContentText("Please reduce usage of your phone");
            }
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }

//        if (hasPermission()) System.out.println("Permission granted");
//        else {
//            System.out.println("Permission not granted");
//            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
//        }

        report = "Top 5 used apps today are:\n";

        packageManager = getContext().getPackageManager();

        mUsageStatsManager = (UsageStatsManager) getContext()
                .getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -1);
        queryUsageStats = mUsageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_DAILY,0, System.currentTimeMillis());

        Collections.sort(queryUsageStats, UsageStatsComparator);
        System.out.println(queryUsageStats.size());
        for (int i=0;i<5;i++) {
            UsageStats usageStats = queryUsageStats.get(i);
            String packageName = usageStats.getPackageName();
            ApplicationInfo applicationInfo;
            try {
                applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException ex) {
                applicationInfo = null;
            }
            String applicationName = (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "(unknown)");
            long duration = usageStats.getTotalTimeInForeground();
            report+=Integer.toString(i+1)+". "+applicationName+": "+Long.toString(duration/60000)+" minutes.\n";
        }
        TextView textView2 = (TextView) getView().findViewById (R.id.top5apps);
        textView2.setText(report);

//        dateColorArrayList = new ArrayList<>();
//        try (BufferedReader br = new BufferedReader(new FileReader(new File(getContext().getFilesDir(),"data.txt")))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                // process the line.
//
//                String date = line.substring(0, 6);
//                int color = Color.parseColor(WHITE);
//                switch (line.charAt(6)) {
//                    case 'r':
//                        color = Color.parseColor(RED);
//                        break;
//                    case 'y':
//                        color = Color.parseColor(YELLOW);
//                        break;
//                    case 'g':
//                        color = Color.parseColor(GREEN);
//                        break;
//                }
//                dateColorArrayList.add(new DateColor(date,color));
//            }
//        } catch (Exception ex) {
//            System.out.println("file not found");
//        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getContext().getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
    public static Date getStartOfDay(Date day) {
        return getStartOfDay(day, Calendar.getInstance());
    }

    public static Date getStartOfDay(Date day, Calendar cal) {
        if (day == null)
            day = new Date();
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }

    public static Comparator<UsageStats> UsageStatsComparator = new Comparator<UsageStats>() {
        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            long lhsTime = lhs.getTotalTimeInForeground();
            long rhsTime = rhs.getTotalTimeInForeground();
            if (lhsTime>rhsTime) return -1;
            else if (lhsTime==rhsTime) return 0;
            else return 1;
        }
    };
}
