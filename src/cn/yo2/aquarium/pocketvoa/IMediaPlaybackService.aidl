package cn.yo2.aquarium.pocketvoa;

interface IMediaPlaybackService
{
    void openfileAsync(String path);
    boolean isPlaying();
    void stop();
    void pause();
    void play();
    long duration();
    long position();
    long seek(long pos);
    String getPath();
}