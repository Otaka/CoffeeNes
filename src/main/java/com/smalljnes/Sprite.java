package com.smalljnes;

/**
 * @author Dmitry
 */
public class Sprite {

    int id;
    int x;
    int y;
    int tile;
    int attr;
    int dataL;
    int dataH;
    
    public void set(Sprite spr){
        id=spr.id;
        x=spr.x;
        y=spr.y;
        tile=spr.tile;
        attr=spr.attr;
        dataL=spr.dataL;
        dataH=spr.dataH;
    }
}
