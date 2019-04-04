package randoop.util;

import java.io.FileDescriptor;
import java.lang.reflect.ReflectPermission;
import java.net.InetAddress;
import java.security.Permission;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.NonDet;
import org.checkerframework.checker.determinism.qual.PolyDet;

public class RandoopSecurityManager extends SecurityManager {

  public enum Status {
    ON,
    OFF
  }

  public Status status;

  public RandoopSecurityManager(Status status) {
    this.status = status;
  }

  @Override
  public void checkAccept(String host, int port) {
    if (status == Status.OFF) return;
    // super.checkAccept(host, port);
  }

  @Override
  public void checkAccess(Thread t) {
    if (status == Status.OFF) return;
    // super.checkAccess(t);
  }

  @Override
  public void checkAccess(ThreadGroup g) {
    if (status == Status.OFF) return;
    // super.checkAccess(g);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void checkAwtEventQueueAccess() {
    if (status == Status.OFF) return;
    // super.checkAwtEventQueueAccess();
  }

  @Override
  public void checkConnect(String host, int port, Object context) {
    if (status == Status.OFF) return;
    // super.checkConnect(host, port, context);
  }

  @Override
  public void checkConnect(String host, int port) {
    if (status == Status.OFF) return;
    // super.checkConnect(host, port);
  }

  @Override
  public void checkCreateClassLoader() {
    if (status == Status.OFF) return;
    // This must be allowed--when disallowed, Randoop failed to invoke simple
    // methods like "new BitSet()".
    // throw new SecurityException("checkCreateClassLoader: Randoop does not
    // allow this operation by tested code");
  }

  @Override
  public void checkDelete(@Det String file) {
    if (status == Status.OFF) return;
    if (file == null) throw new NullPointerException();
    throw new SecurityException(
        "checkDelete: Randoop does not allow this operation by tested code");
  }

  @Override
  public void checkExec(@Det String cmd) {
    if (status == Status.OFF) return;
    if (cmd == null) throw new NullPointerException();
    throw new SecurityException("checkExec: Randoop does not allow this operation by tested code");
  }

  @Override
  public void checkExit(int stat) {
    if (status == Status.OFF) return;
    throw new SecurityException("checkExit: Randoop does not allow this operation by tested code");
  }

  @Override
  public void checkLink(String lib) {
    if (status == Status.OFF) return;
    // super.checkLink(lib);
  }

  @Override
  public void checkListen(int port) {
    if (status == Status.OFF) return;
    // super.checkListen(port);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void checkMemberAccess(Class<?> clazz, int which) {
    if (status == Status.OFF) return;
    // super.checkMemberAccess(clazz, which);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void checkMulticast(InetAddress maddr, byte ttl) {
    if (status == Status.OFF) return;
    // super.checkMulticast(maddr, ttl);
  }

  @Override
  public void checkMulticast(InetAddress maddr) {
    if (status == Status.OFF) return;
    // super.checkMulticast(maddr);
  }

  @Override
  public void checkPackageAccess(String pkg) {
    if (status == Status.OFF) return;
    // super.checkPackageAccess(pkg);
  }

  @Override
  public void checkPackageDefinition(String pkg) {
    if (status == Status.OFF) return;
    // super.checkPackageDefinition(pkg);
  }

  @Override
  public void checkPermission(@Det Permission perm, @Det Object context) {
    if (status == Status.OFF) return;
    if (perm instanceof ReflectPermission) {
      // Randoop allows reflection operations.
      return;
    }
    super.checkPermission(perm, context);
  }

  @Override
  public void checkPermission(@Det Permission perm) {
    if (status == Status.OFF) return;
    if (perm instanceof ReflectPermission) {
      // Randoop allows reflection operations.
      return;
    }
    super.checkPermission(perm);
  }

  @Override
  public void checkPrintJobAccess() {
    if (status == Status.OFF) return;
    // super.checkPrintJobAccess();
  }

  @Override
  public void checkPropertiesAccess() {
    if (status == Status.OFF) return;
    // super.checkPropertiesAccess();
  }

  @Override
  public void checkPropertyAccess(String key) {
    if (status == Status.OFF) return;
    // super.checkPropertyAccess(key);
  }

  @Override
  public void checkRead(@Det FileDescriptor fd) {
    if (status == Status.OFF) return;
    if (fd == null) throw new NullPointerException();
    throw new SecurityException("checkRead: Randoop does not allow this operation by tested code");
  }

  @Override
  public void checkRead(@Det String file, @Det Object context) {
    if (status == Status.OFF) return;
    if (file == null) throw new NullPointerException();
    throw new SecurityException(
        "checkRead(String,Object): Randoop does not allow this operation by tested code");
  }

  @Override
  public void checkRead(@Det String file) {
    if (status == Status.OFF) return;
    if (file == null) throw new NullPointerException();
    // throw new SecurityException("Randoop does not allow this operation by
    // tested code");
  }

  @Override
  public void checkSecurityAccess(String target) {
    if (status == Status.OFF) return;
    // super.checkSecurityAccess(target);
  }

  @Override
  public void checkSetFactory() {
    if (status == Status.OFF) return;
    // super.checkSetFactory();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void checkSystemClipboardAccess() {
    if (status == Status.OFF) return;
    // super.checkSystemClipboardAccess();
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean checkTopLevelWindow(Object window) {
    if (status == Status.OFF) {
      return true;
    }
    if (window == null) throw new NullPointerException();
    throw new SecurityException(
        "checkTopLevelWindow(Object): Randoop does not allow this operation by tested code");
    // return super.checkTopLevelWindow(window);
  }

  @Override
  public void checkWrite(@Det FileDescriptor fd) {
    if (status == Status.OFF) return;
    if (fd == null) throw new NullPointerException();
    throw new SecurityException(
        "checkWrite(FileDescriptor): Randoop does not allow this operation by tested code");
  }

  @Override
  public void checkWrite(@Det String file) {
    if (status == Status.OFF) return;
    if (file == null) throw new NullPointerException();
    throw new SecurityException(
        "checkWrite(String): Randoop does not allow this operation by tested code");
  }

  @SuppressWarnings("deprecation")
  @Override
  protected int classDepth(String name) {
    return super.classDepth(name);
  }

  @SuppressWarnings("deprecation")
  @Override
  protected int classLoaderDepth() {
    return super.classLoaderDepth();
  }

  @SuppressWarnings("deprecation")
  @Override
  protected ClassLoader currentClassLoader() {
    return super.currentClassLoader();
  }

  @SuppressWarnings("deprecation")
  @Override
  protected Class<?> currentLoadedClass() {
    return super.currentLoadedClass();
  }

  @Override
  protected Class<?>[] getClassContext() {
    return super.getClassContext();
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean getInCheck() {
    return super.getInCheck();
  }

  @Override
  public Object getSecurityContext() {
    return super.getSecurityContext();
  }

  @Override
  public ThreadGroup getThreadGroup() {
    return super.getThreadGroup();
  }

  @SuppressWarnings("deprecation")
  @Override
  protected boolean inClass(String name) {
    return super.inClass(name);
  }

  @SuppressWarnings("deprecation")
  @Override
  protected boolean inClassLoader() {
    return super.inClassLoader();
  }

  @Override
  public @NonDet int hashCode() {
    return super.hashCode();
  }

  @Override
  public @PolyDet("up") boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  @Override
  public @NonDet String toString() {
    return super.toString();
  }
}
