package com.plumelog.core.util;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import java.io.Closeable;

/**
 * 资源池基类，封装GenericObjectPool中相应的方法
 * @param <T>
 */
public abstract class Pool<T> implements Closeable {

  protected GenericObjectPool<T> internalPool;

  public Pool() {
  }

  public Pool(final GenericObjectPoolConfig poolConfig, PooledObjectFactory<T> factory) {
    poolConfig.setMinIdle(0);
    poolConfig.setMaxIdle(8);
    poolConfig.setMaxTotal(30);
    poolConfig.setMaxWaitMillis(1000);
    initPool(poolConfig, factory);
  }

  @Override
  public void close() {
    destroy();
  }

  public boolean isClosed() {
    return this.internalPool.isClosed();
  }

  public void initPool(final GenericObjectPoolConfig poolConfig, PooledObjectFactory<T> factory) {

    if (this.internalPool != null) {
      try {
        closeInternalPool();
      } catch (Exception e) {
      }
    }

    this.internalPool = new GenericObjectPool<T>(factory, poolConfig);
  }

  /**
   * 获取一个对象资源
   * @return
   */
  public T getResource() {
    try {
      return internalPool.borrowObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  protected void returnResourceObject(final T resource) {
    if (resource == null) {
      return;
    }
    try {
      internalPool.returnObject(resource);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void returnBrokenResource(final T resource) {
    if (resource != null) {
      returnBrokenResourceObject(resource);
    }
  }

  protected void returnResource(final T resource) {
    if (resource != null) {
      returnResourceObject(resource);
    }
  }

  public void destroy() {
    closeInternalPool();
  }

  protected void returnBrokenResourceObject(final T resource) {
    try {
      internalPool.invalidateObject(resource);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void closeInternalPool() {
    try {
      internalPool.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public int getNumActive() {
    if (poolInactive()) {
      return -1;
    }

    return this.internalPool.getNumActive();
  }

  public int getNumIdle() {
    if (poolInactive()) {
      return -1;
    }

    return this.internalPool.getNumIdle();
  }

  public int getNumWaiters() {
    if (poolInactive()) {
      return -1;
    }

    return this.internalPool.getNumWaiters();
  }

  public long getMeanBorrowWaitTimeMillis() {
    if (poolInactive()) {
      return -1;
    }

    return this.internalPool.getMeanBorrowWaitTimeMillis();
  }

  public long getMaxBorrowWaitTimeMillis() {
    if (poolInactive()) {
      return -1;
    }

    return this.internalPool.getMaxBorrowWaitTimeMillis();
  }

  private boolean poolInactive() {
    return this.internalPool == null || this.internalPool.isClosed();
  }

  public void addObjects(int count) {
    try {
      for (int i = 0; i < count; i++) {
        this.internalPool.addObject();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
