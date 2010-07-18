/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\SDK\\eclipse-java-galileo-SR1-win32\\workspace\\Pocket VOA\\src\\cn\\yo2\\aquarium\\pocketvoa\\IMediaPlaybackService.aidl
 */
package cn.yo2.aquarium.pocketvoa;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
public interface IMediaPlaybackService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements cn.yo2.aquarium.pocketvoa.IMediaPlaybackService
{
private static final java.lang.String DESCRIPTOR = "cn.yo2.aquarium.pocketvoa.IMediaPlaybackService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an IMediaPlaybackService interface,
 * generating a proxy if needed.
 */
public static cn.yo2.aquarium.pocketvoa.IMediaPlaybackService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof cn.yo2.aquarium.pocketvoa.IMediaPlaybackService))) {
return ((cn.yo2.aquarium.pocketvoa.IMediaPlaybackService)iin);
}
return new cn.yo2.aquarium.pocketvoa.IMediaPlaybackService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_setArticle:
{
data.enforceInterface(DESCRIPTOR);
cn.yo2.aquarium.pocketvoa.Article _arg0;
if ((0!=data.readInt())) {
_arg0 = cn.yo2.aquarium.pocketvoa.Article.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.setArticle(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getArticle:
{
data.enforceInterface(DESCRIPTOR);
cn.yo2.aquarium.pocketvoa.Article _result = this.getArticle();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getState:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getState();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_isPlaying:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isPlaying();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_stop:
{
data.enforceInterface(DESCRIPTOR);
this.stop();
reply.writeNoException();
return true;
}
case TRANSACTION_pause:
{
data.enforceInterface(DESCRIPTOR);
this.pause();
reply.writeNoException();
return true;
}
case TRANSACTION_init:
{
data.enforceInterface(DESCRIPTOR);
this.init();
reply.writeNoException();
return true;
}
case TRANSACTION_isInitialized:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isInitialized();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_play:
{
data.enforceInterface(DESCRIPTOR);
this.play();
reply.writeNoException();
return true;
}
case TRANSACTION_duration:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.duration();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_position:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.position();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_seek:
{
data.enforceInterface(DESCRIPTOR);
long _arg0;
_arg0 = data.readLong();
long _result = this.seek(_arg0);
reply.writeNoException();
reply.writeLong(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements cn.yo2.aquarium.pocketvoa.IMediaPlaybackService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void setArticle(cn.yo2.aquarium.pocketvoa.Article article) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((article!=null)) {
_data.writeInt(1);
article.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setArticle, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public cn.yo2.aquarium.pocketvoa.Article getArticle() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
cn.yo2.aquarium.pocketvoa.Article _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getArticle, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = cn.yo2.aquarium.pocketvoa.Article.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int getState() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getState, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean isPlaying() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isPlaying, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void stop() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void pause() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_pause, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void init() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_init, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public boolean isInitialized() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isInitialized, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void play() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_play, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public long duration() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_duration, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public long position() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_position, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public long seek(long pos) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeLong(pos);
mRemote.transact(Stub.TRANSACTION_seek, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_setArticle = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getArticle = (IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getState = (IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_isPlaying = (IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_stop = (IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_pause = (IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_init = (IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_isInitialized = (IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_play = (IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_duration = (IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_position = (IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_seek = (IBinder.FIRST_CALL_TRANSACTION + 11);
}
public void setArticle(cn.yo2.aquarium.pocketvoa.Article article) throws android.os.RemoteException;
public cn.yo2.aquarium.pocketvoa.Article getArticle() throws android.os.RemoteException;
public int getState() throws android.os.RemoteException;
public boolean isPlaying() throws android.os.RemoteException;
public void stop() throws android.os.RemoteException;
public void pause() throws android.os.RemoteException;
public void init() throws android.os.RemoteException;
public boolean isInitialized() throws android.os.RemoteException;
public void play() throws android.os.RemoteException;
public long duration() throws android.os.RemoteException;
public long position() throws android.os.RemoteException;
public long seek(long pos) throws android.os.RemoteException;
}
