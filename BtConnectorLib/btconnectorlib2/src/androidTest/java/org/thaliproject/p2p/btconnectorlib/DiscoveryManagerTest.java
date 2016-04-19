package org.thaliproject.p2p.btconnectorlib;

import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class DiscoveryManagerTest extends AbstractConnectivityManagerTest {

    private DiscoveryManager mDiscoveryManager = null;
    private static DiscoveryManager.DiscoveryMode defaultDiscoveryMode;
    private static boolean defaultBTStatus;
    private static boolean defaultWifiStatus;

    private static void setDiscoveryMode(DiscoveryManager.DiscoveryMode mode) {
        DiscoveryManagerSettings settings = DiscoveryManagerSettings.getInstance(InstrumentationRegistry.getContext());
        settings.setDiscoveryMode(mode);
    }

    private static DiscoveryManager.DiscoveryMode getDiscoveryMode() {
        DiscoveryManagerSettings settings = DiscoveryManagerSettings.getInstance(InstrumentationRegistry.getContext());
        return settings.getDiscoveryMode();
    }

    @Mock
    DiscoveryManager.DiscoveryManagerListener mMockDiscoveryManagerListener;

    @BeforeClass
    public static void init()  throws Exception{
        Looper.prepare();
        // save the state
        defaultDiscoveryMode = getDiscoveryMode();
        defaultBTStatus = getBluetoothStatus();
        defaultWifiStatus = getWifiStatus();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        toggleBluetooth(false);
        toggleWifi(false);

        mDiscoveryManager = new DiscoveryManager(InstrumentationRegistry.getContext(),
                mMockDiscoveryManagerListener,
                UUID.randomUUID(), "MOCK_NAME");
    }

    @After
    public void tearDown() throws Exception {
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        // restore the saved state
        setDiscoveryMode(defaultDiscoveryMode);
        toggleWifi(defaultWifiStatus);
        toggleBluetooth(defaultBTStatus);
    }


    @Test
    public void testAfterConstruction() throws Exception {
        // check state
        assertEquals(DiscoveryManager.DiscoveryManagerState.NOT_STARTED, mDiscoveryManager.getState());

        // check if discovery manager is added as listener
        DiscoveryManagerSettings dmSettings = DiscoveryManagerSettings.getInstance(InstrumentationRegistry.getContext());
        try {
            dmSettings.addListener(mDiscoveryManager);
            fail();
        } catch (IllegalArgumentException exc) {
        }
    }

    @Test
    public void testStartBluetoothDisabled() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE);
        toggleBluetooth(false);
        boolean isRunning = mDiscoveryManager.start(false, false);
        assertThat(isRunning, is(false));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.NOT_STARTED));
    }

    @Test
    public void testStartListeningBluetoothEnabled() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE);
        toggleBluetooth(true);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        boolean isRunning = mDiscoveryManager.start(false, false);
        assertThat(isRunning, is(false));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.NOT_STARTED));
    }

    @Test
    public void testStartListeningBluetoothEnabledStartDiscovery() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE);
        toggleBluetooth(true);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        boolean isRunning = mDiscoveryManager.start(true, false);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_BLE));
    }

    @Test
    public void testStartListeningWifiDisabled() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.WIFI);
        toggleWifi(false);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        boolean isRunning = mDiscoveryManager.start(false, true);
        assertThat(isRunning, is(false));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.NOT_STARTED));
    }

    @Test
    public void testStartListeningWifiEnabled() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(true);
        boolean isRunning = mDiscoveryManager.start(false, false);
        assertThat(isRunning, is(false));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.NOT_STARTED));
    }

    @Test
    public void testStartListeningWifiEnabledStartDiscovery() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(true);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(true, false);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_WIFI));
    }

    @Test
    public void testStartListeningWifiEnabledStartAdvertising() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(true);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(false, true);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_WIFI));
    }

    @Test
    public void testStartListeningWifiEnabledStartDiscoveryAndAdvertising() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(true);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(true, true);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_WIFI));
    }

    @Test
    public void testStartListeningWifiDisabledBW() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE_AND_WIFI);
        toggleWifi(false);
        toggleBluetooth(false);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        boolean isRunning = mDiscoveryManager.start(false, true);
        assertThat(isRunning, is(false));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.NOT_STARTED));
    }

    // WIFI enabled, BT disabled
    @Test
    public void testStartListeningWifiEnabledBW() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE_AND_WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(true);
        toggleBluetooth(false);
        boolean isRunning = mDiscoveryManager.start(false, false);
        assertThat(isRunning, is(false));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.NOT_STARTED));
    }

    @Test
    public void testStartListeningWifiEnabledStartDiscoveryBW() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE_AND_WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(true);
        toggleBluetooth(false);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(true, false);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_WIFI));
    }

    @Test
    public void testStartListeningWifiEnabledStartAdvertisingBW() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE_AND_WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(true);
        toggleBluetooth(false);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(false, true);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_WIFI));
    }

    @Test
    public void testStartListeningWifiEnabledStartDiscoveryAndAdvertisingBW() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE_AND_WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(true);
        toggleBluetooth(false);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(true, true);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_WIFI));
    }

    // Both BT and WIFI enabled
    @Test
    public void testStartListeningWifiBTEnabledStartDiscoveryBW() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE_AND_WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(true);
        toggleBluetooth(true);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(true, false);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_BLE_AND_WIFI));
    }

    @Test
    public void testStartListeningWifiBTEnabledStartAdvertisingBW() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE_AND_WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(true);
        toggleBluetooth(true);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(false, true);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_BLE_AND_WIFI));
    }

    @Test
    public void testStartListeningWifiBTEnabledStartDiscoveryAndAdvertisingBW() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE_AND_WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(true);
        toggleBluetooth(true);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(true, true);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_BLE_AND_WIFI));
    }

    // BT enabled, Wifi disabled
    @Test
    public void testStartListeningBTEnabledStartDiscoveryBW() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE_AND_WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(false);
        toggleBluetooth(true);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(true, false);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_BLE));
    }

    @Test
    public void testStartListeningBTEnabledStartAdvertisingBW() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE_AND_WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(false);
        toggleBluetooth(true);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(false, true);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_BLE));
    }

    @Test
    public void testStartListeningBTEnabledStartDiscoveryAndAdvertisingBW() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.BLE_AND_WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(false);
        toggleBluetooth(true);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(true, true);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_BLE));
    }

    @Test
    public void testStopDiscovery() throws Exception {
        setDiscoveryMode(DiscoveryManager.DiscoveryMode.WIFI);
        when(mMockDiscoveryManagerListener.onPermissionCheckRequired(anyString())).thenReturn(true);
        toggleWifi(true);
        toggleBluetooth(false);
        mDiscoveryManager.setPeerName("TestPeerName");
        boolean isRunning = mDiscoveryManager.start(true, true);
        assertThat(isRunning, is(true));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_WIFI));
        mDiscoveryManager.stopDiscovery();
        assertThat(mDiscoveryManager.isDiscovering(), is(false));
        assertThat(mDiscoveryManager.getState(), is(DiscoveryManager.DiscoveryManagerState.RUNNING_WIFI));
    }
}