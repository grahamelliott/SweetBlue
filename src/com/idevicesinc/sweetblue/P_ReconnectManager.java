package com.idevicesinc.sweetblue;

import com.idevicesinc.sweetblue.BleDevice.ConnectionFailListener;
import com.idevicesinc.sweetblue.BleDeviceConfig.ReconnectLoop;
import com.idevicesinc.sweetblue.BleDeviceConfig.ReconnectLoop.Please;
import com.idevicesinc.sweetblue.utils.Interval;


/**
 * 
 * 
 *
 */
class P_ReconnectManager
{
	private static final double NOT_RUNNING = -1.0;
	private final BleDevice m_device;
	
	private double m_totalTime;
	private int m_attemptCount;
	private double m_delay = 0.0;
	private double m_timeTracker = NOT_RUNNING;
	
	P_ReconnectManager(BleDevice device)
	{
		m_device = device;
	}
	
	void start()
	{
		if( !isRunning() )
		{
			m_device.getManager().pushWakeLock();
		}
		
		m_totalTime = 0.0;
		m_attemptCount = 0;
		m_delay = 0.0;
		m_timeTracker = 0.0;
		
		m_delay = getNextTime(ConnectionFailListener.Info.NULL(m_device));
		
		if( m_delay < 0.0 )
		{
			m_timeTracker = NOT_RUNNING;
		}
		
		//--- DRK > If delay is zero we still wait until the first time step to actually connect.
		//---		May change in future for API-consistency's sake. 
	}
	
	boolean isRunning()
	{
		if( m_timeTracker >= 0.0 )
		{
//			m_device.getManager().ASSERT(m_device.is(E_DeviceState.ATTEMPTING_RECONNECT));
			
			return true;
		}
		else
		{
//			m_device.getManager().ASSERT(!m_device.is(E_DeviceState.ATTEMPTING_RECONNECT));
			
			return false;
		}
	}
	
	private double getNextTime(ConnectionFailListener.Info connectionFailInfo)
	{
		BleDeviceConfig.ReconnectLoop rateLimiter = m_device.conf_device().reconnectLoop;
		rateLimiter = rateLimiter != null ? rateLimiter : m_device.conf_mngr().reconnectLoop;
		
		if( rateLimiter == null )
		{
			return BleManagerConfig.ReconnectLoop.Please.STOP.secs();
		}
		else
		{
			ReconnectLoop.Info info = new ReconnectLoop.Info(m_device, m_attemptCount, Interval.secs(m_totalTime), Interval.secs(m_delay), connectionFailInfo);
			Please please = rateLimiter.onReconnectRequest(info);
			
			Interval delay = please != null ? please.getInterval() : null;
			delay = delay != null ? delay : BleManagerConfig.ReconnectLoop.Please.STOP;
			
			return delay.secs();
		}
	}
	
	boolean onConnectionFailed(ConnectionFailListener.Info connectionFailInfo)
	{
		if( !isRunning() )
		{
			return false;
		}
		
		m_attemptCount++;

		m_timeTracker = 0.0;
		
		double delay = getNextTime(connectionFailInfo);
		
		if( delay < 0.0 )
		{
			stop();
			
			return false;
		}
		else
		{
			m_delay = delay;
			m_timeTracker = 0.0;
			
			return true;
		}
	}
	
	void update(double timeStep)
	{
		if( !isRunning() )  return;
		
		m_totalTime += timeStep;
		
		if( !m_device.is(BleDeviceState.DISCONNECTED) )  return;
		
		m_timeTracker += timeStep;
		
		if( m_timeTracker >= m_delay )
		{
			m_device.attemptReconnect();
		}
	}
	
	void stop()
	{
		if( isRunning() )
		{
			m_device.getManager().popWakeLock();
		}
		
		m_timeTracker = NOT_RUNNING;
		m_attemptCount = 0;
		m_totalTime = 0.0;
	}
}
