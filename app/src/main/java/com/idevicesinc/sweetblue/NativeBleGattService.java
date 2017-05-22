package com.idevicesinc.sweetblue;


import android.bluetooth.BluetoothGattService;


/**
 * Wrapper class which holds an instance of {@link BluetoothGattService}. You should always check {@link #isNull()} before
 * doing anything with the {@link BluetoothGattService} returned from {@link #getService()}.
 */
public final class NativeBleGattService extends P_NativeGattObject<BluetoothGattService>
{

    private NativeBleGattService()
    {
        super();
    }

    NativeBleGattService(BleManager.UhOhListener.UhOh uhoh)
    {
        super(null, uhoh);
    }

    NativeBleGattService(BluetoothGattService service)
    {
        super(service, null);
    }

    NativeBleGattService(BluetoothGattService service, BleManager.UhOhListener.UhOh uhoh)
    {
        super(service, uhoh);
    }

    /**
     * Returns the instance of {@link BluetoothGattService} held in this class.
     */
    public BluetoothGattService getService()
    {
        return getGattObject();
    }


    public final static NativeBleGattService NULL = new NativeBleGattService();

}
