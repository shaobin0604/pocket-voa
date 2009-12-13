package cn.yo2.aquarium.pocketvoa;

public interface IProgressListener {
	public void setError(int which, String message);
	public void setSuccess(int which);
	public void updateProgress(int which, long pos, long total);
}
