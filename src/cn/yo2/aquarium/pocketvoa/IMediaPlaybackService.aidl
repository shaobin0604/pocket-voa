package cn.yo2.aquarium.pocketvoa;

import cn.yo2.aquarium.pocketvoa.Article;

interface IMediaPlaybackService
{
    void setArticle(in Article article); 
    Article getArticle();
    int getState();
    boolean isPlaying();
    void stop();
    void pause();
    void init();
    boolean isInitialized();
    void play();
    long duration();
    long position();
    long seek(long pos);
}