package eky.beaconmaps;

import android.app.Application;

import com.estimote.coresdk.common.config.EstimoteSDK;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.ArrayList;
import java.util.List;

import eky.beaconmaps.beacon.estimote.BeaconNotificationsManager;
import eky.beaconmaps.model.BeaconData;
import eky.beaconmaps.model.NotificationData;
import eky.beaconmaps.utils.PreferencesUtil;

/**
 * Created by Enes Kamil YILMAZ on 28.03.2019.
 */

public class BeaconMaps extends Application {

    private static final String TAG = "BeaconReferenceApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    BeaconManager beaconManager;
    private boolean beaconNotificationsEnabled = false;
    public static List<BeaconData> list = new ArrayList<>();;
    private PreferencesUtil preferencesUtil;

    public void onCreate() {
        super.onCreate();

        EstimoteSDK.initialize(getApplicationContext(), "beaconmap-7na"
                , "406b37d7508496b9349808f7634c2b58");

        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        preferencesUtil = new PreferencesUtil(this);
    }

    public void enableBeaconNotifications() {
        if (beaconNotificationsEnabled) {
            return;
        }

        BeaconNotificationsManager beaconNotificationsManager = new BeaconNotificationsManager(this);

        if (preferencesUtil.getRegisteredBeaconList() != null && preferencesUtil.getRegisteredBeaconList().size() > 0 ) {

            for (BeaconData beaconData : preferencesUtil.getRegisteredBeaconList()) {
                if (beaconData.getNotificationData() != null && !beaconData.isBlocked()) {
                    if (!beaconData.isBlocked())
                        list.add(beaconData);
                }
            }

        } else {

            list.add(new BeaconData("E263C169-EB5D-76DA-F938-1BBA59293189", 81, 81,
                    new NotificationData(
                            "Fatih Sultan Mehmet Vakıf Üniversitesine hoşgeldiniz !",
                            "- Gray - ",
                            "Bir daha görüşmek üzere... FSMVÜ",
                            "- Gray - ")));
            list.add(new BeaconData("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 100, 58168,
                    new NotificationData(
                            "Whopper Menüde Sana Özel İndirim - Burger King",
                            "15 dakika içerisinde yapacağın Whopper Menü siparişinde sana özel %20 indirim ! - Lemon - ",
                            "Afiyet Olsun. Tekrar bekleriz.",
                            "- Lemon - ")));
            list.add(new BeaconData("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 100, 21066,
                    new NotificationData(
                            "Sana Özel Büyük Fırsat - De Facto",
                            "10 dakika içinde yapacağın alışverişinde aldığın 2.ürüne %50 indirim ! - Purple - ",
                            " - De Facto",
                            " - Purple - ")));
            list.add(new BeaconData("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 100, 19782,
                    new NotificationData(
                            "Badem'de bugüne özel indirim. - Bilen Kuruyemiş",
                            "Yaş badem bugün %20 indirimli. - Pink - ",
                            " - Bilen Kuruyemiş ",
                            "- Pink - ")));

        }

        for (BeaconData beaconData : list)
            beaconNotificationsManager.addNotification(beaconData);

        beaconNotificationsManager.startMonitoring();
        beaconNotificationsEnabled = true;
    }

    public boolean isBeaconNotificationsEnabled() {
        return beaconNotificationsEnabled;
    }

    public static List<BeaconData> getList() {
        return list;
    }
}
